package xyz.chaisong.mmbus;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by song on 16/9/14.
 */

class EventHandler<T> implements InvocationHandler {

  private Class<T> mTargetInterface;
  private Set<T> mReceivers;
  private AtomicInteger targetReceiverCount = new AtomicInteger(0);
  T mReceiverProxy;

  EventHandler(Class<T> targetInterface, Set<T> receivers) {
    this.mTargetInterface = targetInterface;
    this.mReceivers = receivers;
    this.mReceiverProxy = (T) Proxy.newProxyInstance(mTargetInterface.getClassLoader(), new Class[] {mTargetInterface}, this);
  }

  void addReceiver(T receiver) {
    mReceivers.add(receiver);
  }

  void removeReceiver(T receiver) {
    mReceivers.remove(receiver);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    for(T receiver : mReceivers) {
      Reception reception = new Reception(receiver,method,args);
      reception.dispatchEvent();
    }
    return null;
  }

  int getTargetReceiverCount() {
    targetReceiverCount.set(0);
    for(Object receiver : mReceivers) {
      if(mTargetInterface.isInstance(receiver)) {
        targetReceiverCount.incrementAndGet();
      }
    }
    return targetReceiverCount.get();
  }

}