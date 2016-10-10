package xyz.chaisong.mmbus;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import xyz.chaisong.mmanagercenter.MManager;

/**
 * Created by song on 16/9/14.
 */

public class MMBus extends MManager implements IMMBus{

    public static boolean isDebugMode = true;

    public static final String DEFAULT_IDENTIFIER = "DefaultBus";

    private Map<Class<?>,EventHandler> mReceiverHandlerByInterface = new ConcurrentHashMap<>();

    private final ConcurrentMap<Class<?>, Set<Producer>> producersByType =
            new ConcurrentHashMap<Class<?>, Set<Producer>>();

    private final String identifier;

    @Override
    public void onManagerInit(Context context) {
        super.onManagerInit(context);
    }

    @Override
    public void onManagerTerminate() {
        super.onManagerTerminate();
        mReceiverHandlerByInterface.clear();
        producersByType.clear();
    }

    public MMBus() {
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
    @Override
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

        if (receiverHandler.addReceiver(receiver)) {//不是重复注册,则调用OnSubscribe
            //OnSubscribe调用
            Set<Producer> producers = producersByType.get(targetInterface);
            if (producers != null) {
                for (Producer producer: producers) {
                    if (producer.isValid())
                        producer.dispatchEvent(receiver);
                }
            }
        }
    }

    /**
     * @param targetInterface 目标接口
     * @param receiver   接口对应的实现，具体的要反注册的，一个类可以注册和反注册多个接口
     */
    @Override
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
    @Override
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

    @Override
    public void addRegisterListener(Object listener) {
        //OnSubscribe遍历,将其封装成Producer存到producersByType中
        Map<Class<?>, Producer> foundProducerMap = ProducerFinder.findAllProducers(listener);
        for (Class<?> type : foundProducerMap.keySet()) {
            Set<Producer> producers = producersByType.get(type);
            if (producers == null) {
                //concurrent put if absent
                Set<Producer> handlersCreation = new CopyOnWriteArraySet<Producer>();
                producers = producersByType.putIfAbsent(type, handlersCreation);
                if (producers == null) {
                    producers = handlersCreation;
                }
            }
            final Producer producer = foundProducerMap.get(type);
            producers.add(producer);
        }
    }

    @Override
    public void removeRegisterListener(Object listener) {
        Map<Class<?>, Producer> foundProducerMap = ProducerFinder.findAllProducers(listener);
        for (Map.Entry<Class<?>, Producer> entry : foundProducerMap.entrySet()) {
            Set<Producer> currentProducers = producersByType.get(entry.getKey());
            Producer producer = entry.getValue();

            if (!currentProducers.contains(producer)) {
                MMBusException.throwException("Missing event handler for an annotated method. Is " + listener.getClass()
                        + " addRegisterListener?");
            }

            for (Producer currentProducer: currentProducers) {
                if (currentProducer.equals(producer)) {
                    currentProducer.invalidate();
                    break;
                }
            }
            currentProducers.remove(producer);
        }
    }

    @Override
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

    public InvocationHandler getReceiverProxy(Class receiverInterface){
        if(!receiverInterface.isInterface()) {
            MMBusException.throwException(String.format("receiverType must be a interface , %s is not a interface",receiverInterface.getName()));
        }
        return mReceiverHandlerByInterface.get(receiverInterface);
    }
}
