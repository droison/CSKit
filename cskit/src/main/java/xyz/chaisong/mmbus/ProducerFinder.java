package xyz.chaisong.mmbus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import xyz.chaisong.mmbus.annotation.OnSubscribe;

/**
 * Created by song on 16/9/15.
 */
public class ProducerFinder {

    /** 为每一个类缓存符合OnSubscribe规则的方法 */
    private static final Map<Class<?>, Map<Class<?>, Method>> PRODUCERS_CACHE = new HashMap<Class<?>, Map<Class<?>, Method>>();

    /**
     * 获取当前类符合 {@link OnSubscribe} annotated的方法.
     * @param listener 为当前类对象
     * @return 该类符合 {@link OnSubscribe} annotated的方法,key为方法入参
     */
    static Map<Class<?>, Producer> findAllProducers(Object listener) {
        final Class<?> listenerClass = listener.getClass();
        Map<Class<?>, Producer> producersInMethod = new HashMap<Class<?>, Producer>();

        if (!PRODUCERS_CACHE.containsKey(listenerClass)) {
            loadAnnotatedMethods(listenerClass);
        }
        Map<Class<?>, Method> methods = PRODUCERS_CACHE.get(listenerClass);
        if (!methods.isEmpty()) {
            for (Map.Entry<Class<?>, Method> e : methods.entrySet()) {
                Producer producer = new Producer(listener, e.getValue());
                producersInMethod.put(e.getKey(), producer);
            }
        }

        return producersInMethod;
    }


    /**
     * 将所有 annotated 是 {@link OnSubscribe} 的方法保存到缓存中
     */
    private static void loadAnnotatedMethods(Class<?> listenerClass) {
        Map<Class<?>, Method> producerMethods = new HashMap<Class<?>, Method>();

        for (Method method : listenerClass.getDeclaredMethods()) {
            if (method.isBridge()) {
                continue;
            }
            if (method.isAnnotationPresent(OnSubscribe.class)) { //首先应该被OnSubscribe注解
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) { //入参有且只有1个
                    MMBusException.throwException("Method " + method + "has @Produce annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require one arguments.");
                    continue;
                }

                Class<?> eventType = method.getReturnType();

                if (eventType != Void.class && !eventType.equals(Void.TYPE)) { //返回值应该是void
                    MMBusException.throwException("Method " + method
                            + " has a return type of " + eventType.getSimpleName() +".  Must declare a void type.");
                    continue;
                }


                if ((method.getModifiers() & Modifier.PUBLIC) == 0) { //方法必须public
                    MMBusException.throwException("Method " + method + " has @Produce annotation on " + eventType
                            + " but is not 'public'.");
                    continue;
                }

                if (producerMethods.containsKey(eventType)) { //一个类里面只能有一个listener
                    MMBusException.throwException("Producer for type " + eventType + " has already been registered.");
                    continue;
                }

                producerMethods.put(parameterTypes[0], method);
            }
        }

        PRODUCERS_CACHE.put(listenerClass, producerMethods);
    }


}
