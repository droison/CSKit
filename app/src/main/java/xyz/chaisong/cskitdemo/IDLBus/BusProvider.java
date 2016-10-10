package xyz.chaisong.cskitdemo.idlbus;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import xyz.chaisong.mmbus.IMMBus;
import xyz.chaisong.mmbus.MMBus;

/**
 * Created by song on 16/10/10.
 */

public class BusProvider implements IMMBus{
    private static final String TAG = "BusProvider";

    private static BusProvider busProvider;

    //每个进程一个aidlService 一个callback 一个Bus
    private IBusAidlInterface mService;
    private CallBack mCallBack;
    private ServiceConnection serviceConnection;

    private MMBus mmBus;

    private BusProvider(){
        mCallBack = new CallBack();
        mmBus = new MMBus("["+ Process.myPid() +"]MMBus" );
        serviceConnection = new ServiceConnection() {

            @Override public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "onServiceDisconnected");
                try {
                    mService.detach(mCallBack);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mService = null;
            }

            @Override public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "onServiceConnected");
                mService = IBusAidlInterface.Stub.asInterface(service);
                if (mService == null) {
                    Log.e(TAG, "connected interface fail!");
                    return;
                }
                // set call back
                try {
                    mService.attach(mCallBack);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void init(){
        if (busProvider == null)
            busProvider = new BusProvider();
    }

    public static IMMBus getBus(){
        return busProvider;
    }

    @Override
    public <T> void register(Class<T> targetInterface, T receiver) {
        mmBus.register(targetInterface, receiver);
    }

    @Override
    public <T> void unregister(Class<T> targetInterface, T receiver) {
        mmBus.unregister(targetInterface, receiver);
    }

    @Override
    public <T> void unregister(T receiver) {
        mmBus.unregister(receiver);
    }

    @Override
    public <T> T getReceiver(Class<T> targetInterface) {
        return new ReceiverProxy(targetInterface.getName(), mService).getProxyObject(targetInterface);
    }

    @Override
    public void addRegisterListener(Object listener) {
        mmBus.addRegisterListener(listener);
    }

    @Override
    public void removeRegisterListener(Object listener) {
        mmBus.removeRegisterListener(listener);
    }

    // client callback
    private class CallBack extends ICallBack.Stub {
        
        @Override
        public void invoke(EventHolder eventHolder) throws RemoteException {
            InvocationHandler handler = null;
            Method method = null;
            try {
                Class receiverClass = Class.forName(eventHolder.getClassName());
                method = receiverClass.getMethod(eventHolder.getMethodName(), convertParametersType(eventHolder.getParameterTypesName()));
                handler = mmBus.getReceiverProxy(receiverClass);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "invoke: ", e);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "invoke: ", e);
            }

            if (handler != null && method != null) {
                try {
                    handler.invoke(null, method, eventHolder.getArgs());
                } catch (Throwable e) {
                    Log.e(TAG, "invoke: ", e);
                }
            }
        }
    }

    private Class<?>[] convertParametersType(String[] paramStrings) throws ClassNotFoundException{
        if (paramStrings != null && paramStrings.length > 0) {
            Class<?>[] result = new Class[paramStrings.length];
            for (int i = 0; i < paramStrings.length; i++) {
                result[i] = Class.forName(paramStrings[i]);
            }
            return result;
        }
        return null;
    }
}
