package xyz.chaisong.mmbus;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import xyz.chaisong.mmanagercenter.MManager;

public class MMBus extends MManager {

    private static MMBus strictBus;

    private boolean isStrictMode;

    public static boolean isDebugMode;

    public static MMBus getStrictBus() {
        if (strictBus == null) {
            strictBus = new MMBus(ThreadEnforcer.MAIN, "StrictBus", new StrictHandlerFindler());
        }
        return strictBus;
    }

    public static final String DEFAULT_IDENTIFIER = "DefaultBus";

    /**
     * All registered event handlers, indexed by event type.
     */
    private final ConcurrentMap<String, Set<EventHandler>> handlersByType = new ConcurrentHashMap<String, Set<EventHandler>>();

    /**
     * Identifier used to differentiate the event bus instance.
     */
    private final String identifier;

    /**
     * Thread enforcer for register, unregister, and posting events.
     */
    private final ThreadEnforcer enforcer;

    /**
     * Used to find handler methods in register and unregister.
     */
    private final HandlerFinder handlerFinder;

    /**
     * Queues of events for the current thread to dispatch.
     */
    private final ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>> eventsToDispatch =
            new ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>>() {
                @Override
                protected ConcurrentLinkedQueue<EventWithHandler> initialValue() {
                    return new ConcurrentLinkedQueue<EventWithHandler>();
                }
            };

    /**
     * True if the current thread is currently dispatching an event.
     */
    private final ThreadLocal<Boolean> isDispatching = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };


    public MMBus() {
        this(DEFAULT_IDENTIFIER);
    }

    public MMBus(String identifier) {
        this(ThreadEnforcer.MAIN, identifier);
    }

    public MMBus(ThreadEnforcer enforcer) {
        this(enforcer, DEFAULT_IDENTIFIER);
    }

    public MMBus(ThreadEnforcer enforcer, String identifier) {
        this(enforcer, identifier, new DefaultHandlerFinder());
    }

    public MMBus(ThreadEnforcer enforcer, String identifier, HandlerFinder handlerFinder) {
        this.enforcer = enforcer;
        this.identifier = identifier;
        this.handlerFinder = handlerFinder;
        isStrictMode = handlerFinder instanceof StrictHandlerFindler;
    }

    @Override
    public String toString() {
        return "[MMBus \"" + identifier + "\"]";
    }

    /**
     * @param keyClass 目标接口
     * @param object   接口对应的实现，具体的要注册的，一个类可以注册和反注册多个接口
     */
    public <T> void register(Class<T> keyClass, T object) {

        if (!keyClass.isInterface())
            throw new IllegalStateException("register keyClass must be a interface");

        if (object == null) {
            throw new NullPointerException("Object to register must not be null.");
        }
        enforcer.enforce(this);

        Map<String, EventHandler<T>> foundHandlersMap = handlerFinder.findAllSubscribers(keyClass, object);
        for (String type : foundHandlersMap.keySet()) {
            Set<EventHandler> handlers = handlersByType.get(type);
            if (handlers == null) {
                //concurrent put if absent
                Set<EventHandler> handlersCreation = new CopyOnWriteArraySet<EventHandler>();
                handlers = handlersByType.putIfAbsent(type, handlersCreation);
                if (handlers == null) {
                    handlers = handlersCreation;
                }
            }
            final EventHandler<T> foundHandlers = foundHandlersMap.get(type);
            handlers.add(foundHandlers);
        }
    }

    /**
     * @param keyClass 目标接口
     * @param object   接口对应的实现，具体的要反注册的，一个类可以注册和反注册多个接口
     */
    public <T> void unregister(Class<T> keyClass, T object) {

        if (!keyClass.isInterface())
            throw new IllegalStateException("unregister keyClass must be a interface");

        if (object == null) {
            throw new NullPointerException("Object to unregister must not be null.");
        }
        enforcer.enforce(this);

        Map<String, EventHandler<T>> handlersInListener = handlerFinder.findAllSubscribers(keyClass, object);

        for (String key : handlersInListener.keySet()) {
            Set<EventHandler> currentHandlers = getHandlersForEventType(key);

            EventHandler<T> eventMethodsInListener = handlersInListener.get(key);

            if (currentHandlers == null || !currentHandlers.contains(eventMethodsInListener)) {
                throw new IllegalArgumentException("Missing event handler for an annotated method. Is " + object.getClass() + " registered?");
            }
            eventMethodsInListener.invalidate();
            currentHandlers.remove(eventMethodsInListener);
        }
    }

    public void post(Class<?> keyClass, Object event) {
        if (!keyClass.isInterface())
            throw new IllegalStateException("post keyClass must be a interface");

        if (event == null) {
            throw new NullPointerException("Event to post must not be null.");
        }

        enforcer.enforce(this);
        if (isStrictMode)
            throw new IllegalStateException("when isStrictMode is true, post must contain targeMethodName");


        Set<Class<?>> dispatchTypes = flattenHierarchy(event.getClass());

        for (Class<?> eventType : dispatchTypes) {
            Set<EventHandler> wrappers = getHandlersForEventType(handlerFinder.keyFromSubscribers(keyClass, "", eventType));

            if (wrappers != null && !wrappers.isEmpty()) {
                for (EventHandler wrapper : wrappers) {
                    enqueueEvent(wrapper, event);
                }
            }
        }
        dispatchQueuedEvents();
    }

    /**
     * @param args            : 需要发送给消息的接口
     * @param targeMethodName : 该接口对应的方法名称
     * @param args            : 传入该方法对应的参数值，一定要写正确
     */
    public void post(Class<?> keyClass, String targeMethodName, Object... args) {
        if (!keyClass.isInterface())
            throw new IllegalStateException("post keyClass must be a interface");

        if (args == null) {
            throw new NullPointerException("Event to post must not be null.");
        }

        enforcer.enforce(this);
        if (!isStrictMode)
            throw new IllegalStateException("when isStrictMode is false, post cannot contain targeMethodName");

        //20150702完成TODO 和上面的post相比，这个却别在于要严格要求传入参数的类型，不能为子类传输，否则报错，这里以后可以做优化
        Class<?> curClassArray[] = getMethodParameterTypes(args);
        Set<Class<?>[]> allClassArry = flattenHierarchy(curClassArray);

        for (Class<?>[] array : allClassArry)
        {
            Set<EventHandler> wrappers = getHandlersForEventType(handlerFinder.keyFromSubscribers(keyClass, targeMethodName, array));
            if (wrappers != null && !wrappers.isEmpty()) {
                for (EventHandler wrapper : wrappers) {
                    enqueueEvent(wrapper, args);
                }
            } else {
                if (isDebugMode)
                {
                    StringBuilder methodParaStr = new StringBuilder();
                    for (Class<?> cls : getMethodParameterTypes(args)) {
                        methodParaStr.append(" ").append(cls.getName());
                    }

                    try {
                        keyClass.getMethod(targeMethodName, getMethodParameterTypes(args));
                    }
                    catch (NoSuchMethodException e)
                    {
                        Log.e("MMBus", "post方法名错误或者传参错误：keyClass=" + keyClass.getName() + ",targeMethodName=" + targeMethodName + ",args=" + methodParaStr.toString(), e);
                    }
                }
            }
        }

        dispatchQueuedEvents();
    }

    public Class<?>[] getMethodParameterTypes(Object[] args) {
        if (args == null)
            return null;
        Class<?>[] args1 = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            args1[i] = args[i].getClass();
        }
        return args1;
    }


    /**
     * Queue the {@code event} for dispatch during {@link #dispatchQueuedEvents()}. Events are queued in-order of
     * occurrence so they can be dispatched in the same order.
     */
    protected void enqueueEvent(EventHandler handler, Object... event) {
        eventsToDispatch.get().offer(new EventWithHandler(handler, event));
    }

    /**
     * Drain the queue of events to be dispatched. As the queue is being drained, new events may be posted to the end of
     * the queue.
     */
    protected void dispatchQueuedEvents() {
        // don't dispatch if we're already dispatching, that would allow reentrancy and out-of-order events. Instead, leave
        // the events to be dispatched after the in-progress dispatch is complete.
        if (isDispatching.get()) {
            return;
        }

        isDispatching.set(true);
        try {
            while (true) {
                EventWithHandler eventWithHandler = eventsToDispatch.get().poll();
                if (eventWithHandler == null) {
                    break;
                }

                if (eventWithHandler.handler.isValid()) {
                    dispatch( eventWithHandler.handler, eventWithHandler.event);
                }
            }
        } finally {
            isDispatching.set(false);
        }
    }

    /**
     * Dispatches {@code event} to the handler in {@code wrapper}.  This method is an appropriate override point for
     * subclasses that wish to make event delivery asynchronous.
     *
     * @param args   event to dispatch.
     * @param wrapper wrapper that will call the handler.
     */
    protected void dispatch( EventHandler wrapper, Object... args) {
        try {
            wrapper.handleEvent(args);
        } catch (InvocationTargetException e) {
            StringBuilder methodParaStr = new StringBuilder();
            for (Class<?> cls : getMethodParameterTypes(args)) {
                methodParaStr.append(" ").append(cls.getName());
            }
            throwRuntimeException("Could not dispatch event: " + methodParaStr.toString() + " to handler " + wrapper, e);
        }
    }

    /**
     * Retrieves a mutable set of the currently registered handlers for {@code type}.  If no handlers are currently
     * registered for {@code type}, this method may either return {@code null} or an empty set.
     *
     * @param type type of handlers to retrieve.
     * @return currently registered handlers, or {@code null}.
     */
    Set<EventHandler> getHandlersForEventType(String type) {
        return handlersByType.get(type);
    }

    /**
     * Flattens a class's type hierarchy into a set of Class objects.  The set will include all superclasses
     * (transitively), and all interfaces implemented by these superclasses.
     *
     * @param concreteClass class whose type hierarchy will be retrieved.
     * @return {@code concreteClass}'s complete type hierarchy, flattened and uniqued.
     */
    Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
        Set<Class<?>> classes = flattenHierarchyCache.get(concreteClass);
        if (classes == null) {
            classes = getClassesFor(concreteClass);
            flattenHierarchyCache.put(concreteClass, classes);
        }

        return classes;
    }

    Set<Class<?>[]> flattenHierarchy(Class<?>[] concreteClass) {
        Set<Class<?>[]> classes = flattenArrayHierarchyCache.get(concreteClass);
        if (classes == null) {
            int length = concreteClass.length;
            List<Set<Class<?>>> multiClassList = new ArrayList<>();
            for (int i = 0; i < length; i++)
            {
                Class<?> curClass = concreteClass[i];
                multiClassList.add(flattenHierarchy(curClass));
            }
            final Set<Class<?>[]> resultSet = new HashSet<>();
            Class<?>[] result = new Class[length];
            classMultiFlatten(multiClassList, 0, result, new ClassMultiFlattenInterface() {
                @Override
                public void complete(Class<?>[] result) {
                    resultSet.add(result);
                }
            });
            flattenArrayHierarchyCache.put(concreteClass, resultSet);
            classes = resultSet;
        }

        return classes;
    }

    void classMultiFlatten(List<Set<Class<?>>> concreteClass, int index, Class<?>[] result, ClassMultiFlattenInterface callback)
    {
        Set<Class<?>> curClasses = concreteClass.get(index);
        for (Class<?> cur : curClasses)
        {
            result[index] = cur;
            if (index == concreteClass.size() - 1)
            {
                Class<?>[] clone = result.clone();
                callback.complete(clone);
            }
            else
            {
                classMultiFlatten(concreteClass, index + 1, result, callback);
            }
        }
    }

    interface ClassMultiFlattenInterface
    {
        void complete(Class<?>[] result);
    }


    private Set<Class<?>> getClassesFor(Class<?> concreteClass) {
        List<Class<?>> parents = new LinkedList<Class<?>>();
        Set<Class<?>> classes = new HashSet<Class<?>>();

        parents.add(concreteClass);

        while (!parents.isEmpty()) {
            Class<?> clazz = parents.remove(0);
            classes.add(clazz);

            Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                parents.add(parent);
            }
        }
        return classes;
    }

    /**
     * Throw a {@link RuntimeException} with given message and cause lifted from an {@link
     * InvocationTargetException}. If the specified {@link InvocationTargetException} does not have a
     * cause, neither will the {@link RuntimeException}.
     */
    private static void throwRuntimeException(String msg, InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            throw new RuntimeException(msg + ": " + cause.getMessage(), cause);
        } else {
            throw new RuntimeException(msg + ": " + e.getMessage(), e);
        }
    }

    private final Map<Class<?>, Set<Class<?>>> flattenHierarchyCache =
            new HashMap<Class<?>, Set<Class<?>>>();

    private final Map<Class<?>[], Set<Class<?>[]>> flattenArrayHierarchyCache =
            new HashMap<Class<?>[], Set<Class<?>[]>>();

    /**
     * Simple struct representing an event and its handler.
     */
    static class EventWithHandler {
        final Object[] event;
        final EventHandler handler;

        public EventWithHandler(EventHandler handler, Object... event) {
            this.event = event;
            this.handler = handler;
        }
    }
}
