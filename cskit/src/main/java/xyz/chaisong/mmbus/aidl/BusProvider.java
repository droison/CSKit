package xyz.chaisong.mmbus.aidl;

import android.app.ActivityManager;
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
import java.util.List;

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
        if (isMainProcess(context)) {
            context.startService(new Intent(context, BusIDLService.class));
        }

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
            method = receiverClass.getDeclaredMethod(eventHolder.getMethodName(), eventHolder.getParametersType());
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

    private boolean isMainProcess(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
