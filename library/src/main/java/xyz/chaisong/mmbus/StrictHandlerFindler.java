package xyz.chaisong.mmbus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by song on 15/6/17.
 */
class StrictHandlerFindler  implements HandlerFinder {

    /**
     * Cache event bus subscriber methods for each class.
     */
    private final Map<Class<?>, Map<String, Method>> SUBSCRIBERS_CACHE = new HashMap<Class<?>, Map<String, Method>>(); //接口类：（key：方法名）

    private void loadAnnotatedMethods(Class<?> listenerClass) {
        if (!listenerClass.isInterface())
            throw new IllegalArgumentException("Class: " + listenerClass + " must be interface.");

        Map<String, Method> subscriberMethods = new HashMap<String, Method>(); //包含该类所有方法的map， key:入参class，value:method
        for (Method method : listenerClass.getDeclaredMethods()) {
            if (method.isBridge()) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            String key = keyFromSubscribers(listenerClass, method.getName(), parameterTypes);
            subscriberMethods.put(key, method);
        }
        SUBSCRIBERS_CACHE.put(listenerClass, subscriberMethods);
    }

    @Override
    public <T> Map<String, EventHandler<T>> findAllSubscribers(Class<T> cls, T listener) {
        Map<String, EventHandler<T>> handlersInMethod = new HashMap<String, EventHandler<T>>();

        if (!SUBSCRIBERS_CACHE.containsKey(cls)) {
            loadAnnotatedMethods(cls);
        }
        Map<String, Method> methods = SUBSCRIBERS_CACHE.get(cls);
        if (!methods.isEmpty()) {
            for (Map.Entry<String, Method> e : methods.entrySet()) {
                handlersInMethod.put(e.getKey(), new EventHandler<T>(listener, e.getValue()));
            }
        }
        return handlersInMethod;
    }


    @Override
    public String keyFromSubscribers(Class<?> keyClass, String methodName, Class<?>... eventClass) {
        StringBuilder methodParaStr = new StringBuilder();
        for (Class<?> cls: eventClass ) {
            cls = convertBaseDataType(cls);
            methodParaStr.append("-").append(cls.getName());
        }
        return keyClass.getName() + "-" + methodName + methodParaStr.toString();
    }

    private Class<?> convertBaseDataType(Class<?> cls)
    {
        if (cls.equals(int.class))
        {
            cls = Integer.class;
        }
        else if (cls.equals(long.class))
        {
            cls = Long.class;
        }
        else if (cls.equals(short.class))
        {
            cls = Short.class;
        }
        else if (cls.equals(float.class))
        {
            cls = Float.class;
        }
        else if (cls.equals(double.class))
        {
            cls = Double.class;
        }
        else if (cls.equals(char.class))
        {
            cls = Character.class;
        }
        else if (cls.equals(boolean.class))
        {
            cls = Boolean.class;
        }
        else if (cls.equals(byte.class))
        {
            cls = Byte.class;
        }
        return cls;
    }
}

