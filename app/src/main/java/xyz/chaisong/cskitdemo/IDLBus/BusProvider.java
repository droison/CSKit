package xyz.chaisong.cskitdemo.idlbus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

public class BusProvider extends ICallBack.Stub implements IMMBus{
    private static final String TAG = "BusProvider";

    private static BusProvider busProvider;

    //每个进程一个aidlService 一个callback 一个Bus
    private IBusAidlInterface mService;
    private ServiceConnection serviceConnection;

    private MMBus mmBus;

    private BusProvider(Context context){
        mmBus = new MMBus("["+ Process.myPid() +"]MMBus" );
        serviceConnection = new ServiceConnection() {

            @Override public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "onServiceDisconnected");
                try {
                    mService.detach(BusProvider.this);
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
                    mService.attach(BusProvider.this);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
        context.bindService(new Intent(context, BusIDLService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void create(Context context){
        if (busProvider == null)
            busProvider = new BusProvider(context);
    }

    public static void exit(Context context){
        if (busProvider != null) {
            context.unbindService(busProvider.serviceConnection);
        }
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
        Log.i(TAG, System.currentTimeMillis() + " getReceiver() called with: targetInterface = [" + targetInterface + "]");
        ReceiverProxy receiverProxy = new ReceiverProxy(targetInterface, mService);
        return receiverProxy.getProxyObject(targetInterface);
    }

    @Override
    public void addRegisterListener(Object listener) {
        mmBus.addRegisterListener(listener);
    }

    @Override
    public void removeRegisterListener(Object listener) {
        mmBus.removeRegisterListener(listener);
    }

    @Override
    public void invoke(EventHolder eventHolder) throws RemoteException {
        InvocationHandler handler = null;
        Method method = null;
        try {
            Class receiverClass = Class.forName(eventHolder.getClassName());
            method = receiverClass.getDeclaredMethod(eventHolder.getMethodName(), convertParametersType(eventHolder.getParameterTypesName()));
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

    private Class<?>[] convertParametersType(String[] paramStrings) throws ClassNotFoundException{
        if (paramStrings != null && paramStrings.length > 0) {
            Class<?>[] result = new Class[paramStrings.length];
            for (int i = 0; i < paramStrings.length; i++) {
                result[i] = getType(paramStrings[i]);
            }
            return result;
        }
        return null;
    }

    private Class getType(String className) throws ClassNotFoundException{
        if (!className.contains(".")) {
            return getPrimitiveType(className);
        } else {
            return Class.forName(className);
        }
    }

    private Class getPrimitiveType(String name)
    {
        if (name.equals("byte")) return byte.class;
        if (name.equals("short")) return short.class;
        if (name.equals("int")) return int.class;
        if (name.equals("long")) return long.class;
        if (name.equals("char")) return char.class;
        if (name.equals("float")) return float.class;
        if (name.equals("double")) return double.class;
        if (name.equals("boolean")) return boolean.class;
        if (name.equals("void")) return void.class;

        return Object.class;
    }
}
