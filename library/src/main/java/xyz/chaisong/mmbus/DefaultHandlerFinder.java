package xyz.chaisong.mmbus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

class DefaultHandlerFinder implements HandlerFinder {

    /**
     * Cache event bus subscriber methods for each class.
     */
    private final Map<Class<?>, Map<Class<?>, Method>> SUBSCRIBERS_CACHE = new HashMap<Class<?>, Map<Class<?>, Method>>(); //接口类：（方法入参类：方法名） ，因此能保证一一对应，禁止一个接口有两个函数传参相同

    private void loadAnnotatedMethods(Class<?> listenerClass) {
        if (!listenerClass.isInterface())
            throw new IllegalArgumentException("Class: " + listenerClass + " must be interface.");

        Map<Class<?>, Method> subscriberMethods = new HashMap<Class<?>, Method>(); //包含该类所有方法的map， key:入参class，value:method
        for (Method method : listenerClass.getDeclaredMethods()) {
            if (method.isBridge()) {
                continue;
            }


            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation but requires "
                        + parameterTypes.length + " arguments.  Methods must require a single argument.");
            }

            Class<?> eventType = parameterTypes[0];
            if (eventType.isInterface()) {
                throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + eventType
                        + " which is an interface.  Subscription must be on a concrete class type.");
            }

            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + eventType
                        + " but is not 'public'.");
            }

            if (subscriberMethods.containsKey(eventType))
            {
                throw new IllegalArgumentException("Method " + method + " has parameterType " + eventType + " but has already exist.");
            }
            subscriberMethods.put(eventType, method);
        }
        SUBSCRIBERS_CACHE.put(listenerClass, subscriberMethods);

    }

    @Override
    public <T> Map<String, EventHandler<T>> findAllSubscribers(Class<T> cls, T listener) {
        Map<String, EventHandler<T>> handlersInMethod = new HashMap<String, EventHandler<T>>();

        if (!SUBSCRIBERS_CACHE.containsKey(cls)) {
            loadAnnotatedMethods(cls);
        }
        Map<Class<?>, Method> methods = SUBSCRIBERS_CACHE.get(cls);
        if (!methods.isEmpty()) {
            for (Map.Entry<Class<?>, Method> e : methods.entrySet()) {
                handlersInMethod.put(keyFromSubscribers(cls,"", e.getKey()), new EventHandler<T>(listener, e.getValue()));
            }
        }
        return handlersInMethod;
    }

    @Override
    public String keyFromSubscribers(Class<?> keyClass, String methodName, Class<?>... eventClass) {
        StringBuilder methodParaStr = new StringBuilder();
        for (Class<?> cls: eventClass ) {
            methodParaStr.append("-").append(cls.getName());
        }
        return keyClass.getName() + "-" + methodParaStr.toString();
    }
}
