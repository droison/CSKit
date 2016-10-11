package xyz.chaisong.cskitdemo.idlbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BusIDLService extends Service {
    private static final String TAG = "BusIDLService";

    private List<ICallBack> callbacks = new ArrayList<>();

    public BusIDLService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }


    private class ServiceBinder extends IBusAidlInterface.Stub {
        @Override
        public void attach(ICallBack cb) throws RemoteException {
            if (!callbacks.contains(cb)) {
                callbacks.add(cb);
            }
        }

        @Override
        public void detach(ICallBack cb) throws RemoteException {
            if (callbacks.contains(cb)) {
                callbacks.remove(cb);
            }
        }

        @Override
        public void invokeEvent(EventHolder eventHolder) throws RemoteException {
            Log.i(TAG, "invokeEvent: " + eventHolder);
            for (ICallBack cb : callbacks)
                cb.invoke(eventHolder);
        }
    }
}
