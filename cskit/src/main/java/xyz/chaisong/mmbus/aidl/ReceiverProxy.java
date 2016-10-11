package xyz.chaisong.mmbus.aidl;

import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by song on 16/10/10.
 */

public class ReceiverProxy implements InvocationHandler {
    private static final String TAG = "ReceiverProxy";
    private String mTargetInterface;

    private Object mReceiverProxy;

    private IBusAidlInterface mService;

    public ReceiverProxy(Class targetInterface, IBusAidlInterface service) {
        this.mReceiverProxy = Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class[] {targetInterface}, this);
        mTargetInterface = targetInterface.getName();
        mService = service;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        EventHolder eventHolder = new EventHolder(mTargetInterface, method, args);
        //此处肯定在当前进程处理,请转发各种Bus
        if (mService != null) {
            try {
                mService.invokeEvent(eventHolder);
            } catch (RemoteException e) {
                Log.e(TAG, "invoke: ", e);
            }
        }
        return null;
    }

    public <T> T getProxyObject(Class<T> targetInterface) {
        return (T)mReceiverProxy;
    }
}
