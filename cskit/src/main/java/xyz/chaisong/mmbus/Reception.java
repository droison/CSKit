package xyz.chaisong.mmbus;

import java.lang.reflect.Method;

import xyz.chaisong.mmbus.annotation.RunThread;
import xyz.chaisong.mmbus.annotation.Subscribe;
import xyz.chaisong.mmbus.dispatcher.Dispatcher;
import xyz.chaisong.mmbus.dispatcher.DispatcherFactory;

/**
 * Created by song on 16/9/14.
 */

class Reception {

    private Object mReceiver;
    private Method mInvokedMethod;
    private Object[] mArgs;
    private Runnable mRunnuble;
    Dispatcher mDispatcher;

    Reception(Object receiver, Method invokedMethod, Object[] args) {
        this.mReceiver = receiver;
        this.mInvokedMethod = invokedMethod;
        this.mArgs = args;
        initReception();
    }

    private void initReception() {
        mInvokedMethod.setAccessible(true);
        mRunnuble =produceEvent();
        RunThread runThread = RunThread.MAIN;
        Subscribe subscribeAnnotation = mInvokedMethod.getAnnotation(Subscribe.class);
        if(subscribeAnnotation!=null) {
            runThread = subscribeAnnotation.runThread();
        }
        mDispatcher = DispatcherFactory.getEventDispatch(runThread);
    }

    void dispatchEvent() {
        mDispatcher.dispatch(mRunnuble);
    }

    private Runnable produceEvent() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    mInvokedMethod.invoke(mReceiver,mArgs);
                } catch (Exception e) {
                    MMBusException.throwException("UnHandler Exception when method invoke",e);
                }
            }
        };
    }
}