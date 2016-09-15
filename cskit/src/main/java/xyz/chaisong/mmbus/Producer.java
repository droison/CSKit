package xyz.chaisong.mmbus;

import java.lang.reflect.Method;

import xyz.chaisong.mmbus.annotation.OnSubscribe;
import xyz.chaisong.mmbus.annotation.RunThread;
import xyz.chaisong.mmbus.dispatcher.Dispatcher;
import xyz.chaisong.mmbus.dispatcher.DispatcherFactory;

/**
 * Created by song on 16/9/15.
 */
public class Producer {

    final Object target;

    private final Method method;

    private final int hashCode;

    private boolean valid = true;

    Dispatcher mDispatcher;

    Producer(Object target, Method method) {
        if (target == null) {
            throw new NullPointerException("Producer target cannot be null.");
        }
        if (method == null) {
            throw new NullPointerException("Producer method cannot be null.");
        }

        this.target = target;
        this.method = method;
        final int prime = 31;
        hashCode = (prime + method.hashCode()) * prime + target.hashCode();

        initProducer();
    }

    private void initProducer() {
        method.setAccessible(true);


        RunThread runThread = RunThread.POSTING;
        OnSubscribe subscribeAnnotation = method.getAnnotation(OnSubscribe.class);
        if(subscribeAnnotation!=null) {
            runThread = subscribeAnnotation.runThread();
        }
        mDispatcher = DispatcherFactory.getEventDispatch(runThread);
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
    }

    void dispatchEvent(Object arg) {
        mDispatcher.dispatch(produceEvent(arg));
    }

    private Runnable produceEvent(final Object arg) {
        return new Runnable() {
            @Override
            public void run() {
                if (valid) {
                    try {
                        method.invoke(target, arg);
                    } catch (Exception e) {
                        MMBusException.throwException("UnHandler Exception when method invoke",e);
                    }
                }
            }
        };
    }

    @Override
    public String toString() {
        return "[Producer " + method + "]";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Producer other = (Producer) obj;

        return method.equals(other.method) && target == other.target;
    }
}
