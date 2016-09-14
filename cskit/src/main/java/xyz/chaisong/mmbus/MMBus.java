package xyz.chaisong.mmbus;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import xyz.chaisong.mmanagercenter.MManager;

/**
 * Created by song on 16/9/14.
 */

public class MMBus extends MManager {

    private static MMBus defaultBus;

    public static boolean isDebugMode = true;

    public static final String DEFAULT_IDENTIFIER = "DefaultBus";

    private Map<Class<?>,EventHandler> mReceiverHandlerByInterface = new ConcurrentHashMap<>();

    /**
     * Identifier used to differentiate the event bus instance.
     */
    private final String identifier;

    public static synchronized MMBus getInstance() {
        if (defaultBus == null) {
            defaultBus = new MMBus();
        }
        return defaultBus;
    }

    private MMBus() {
        this(DEFAULT_IDENTIFIER);
    }

    public MMBus(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "[MMBus \"" + identifier + "\"]";
    }

    /**
     * @param targetInterface 目标接口
     * @param receiver   接口对应的实现，具体的要注册的，一个类可以注册和反注册多个接口
     */
    public <T> void register(Class<T> targetInterface, T receiver) {
        if (!targetInterface.isInterface())
            MMBusException.throwException("register keyClass must be a interface");

        if (receiver == null) {
            MMBusException.throwException("Object to register must not be null.");
        }

        EventHandler<T> receiverHandler = mReceiverHandlerByInterface.get(targetInterface);

        if(receiverHandler == null) {
            receiverHandler = new EventHandler<T>(targetInterface, new CopyOnWriteArraySet<T>());
            mReceiverHandlerByInterface.put(targetInterface,receiverHandler);
        }
        receiverHandler.addReceiver(receiver);
    }

    /**
     * @param targetInterface 目标接口
     * @param receiver   接口对应的实现，具体的要反注册的，一个类可以注册和反注册多个接口
     */
    public <T> void unregister(Class<T> targetInterface, T receiver) {

        if (!targetInterface.isInterface())
            MMBusException.throwException("unregister keyClass must be a interface");

        if (receiver == null) {
            MMBusException.throwException("Object to unregister must not be null.");
        }

        EventHandler<T> receiverHandler = mReceiverHandlerByInterface.get(targetInterface);
        if (receiverHandler != null) {
            receiverHandler.removeReceiver(receiver);
            if (receiverHandler.getTargetReceiverCount()==0) {
                mReceiverHandlerByInterface.remove(targetInterface);
            }
        }
    }

    /**
     * @param receiver   接口对应的实现，具体的要反注册的，一个类可以注册和反注册多个接口
     */
    public <T> void unregister(T receiver) {
        if (receiver == null) {
            MMBusException.throwException("Object to register must not be null.");
        }

        Iterator iterator = mReceiverHandlerByInterface.keySet().iterator();
        while (iterator.hasNext()) {
            Class type = (Class) iterator.next();
            if (type.isInstance(receiver)) {
                EventHandler<T> receiverHandler = mReceiverHandlerByInterface.get(type);
                receiverHandler.removeReceiver(receiver);
                if (receiverHandler.getTargetReceiverCount()==0) {
                    iterator.remove();
                }
            }
        }
    }

    public <T> T getReceiver(Class<T> targetInterface) {
        if(!targetInterface.isInterface()) {
            MMBusException.throwException(String.format("receiverType must be a interface , %s is not a interface",targetInterface.getName()));
        }

        EventHandler<T> receiverHandler = mReceiverHandlerByInterface.get(targetInterface);

        if(receiverHandler == null) {
            receiverHandler = new EventHandler<T>(targetInterface, new CopyOnWriteArraySet<T>());
            mReceiverHandlerByInterface.put(targetInterface,receiverHandler);
        }

        return receiverHandler.mReceiverProxy;
    }
}
