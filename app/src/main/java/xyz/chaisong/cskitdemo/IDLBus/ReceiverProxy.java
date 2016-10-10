package xyz.chaisong.cskitdemo.IDLBus;

import android.os.Process;
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

    public ReceiverProxy(String targetInterfaceName, IBusAidlInterface service) {
        mTargetInterface = targetInterfaceName;
        mService = service;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        //此处肯定在当前进程处理,请转发各种Bus
        if (mService != null) {
            try {
                EventHolder eventHolder = new EventHolder(mTargetInterface, method, args);
                mService.invokeEvent(eventHolder);
            } catch (RemoteException e) {
                Log.e(TAG, "invoke: ", e);
            }
        }
        return null;
    }

    public <T> T getProxyObject(Class<T> targetInterface) {
        if (!targetInterface.isInterface() || !(targetInterface.getName().equals(mTargetInterface))) {
            throw new RuntimeException("");
        }
        if (this.mReceiverProxy == null)
            this.mReceiverProxy = Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class[] {targetInterface}, this);

        return (T)mReceiverProxy;
    }
}
