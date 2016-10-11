package xyz.chaisong.cskitdemo;

import android.app.Application;
import android.os.Process;
import android.util.Log;

import xyz.chaisong.cskitdemo.idlbus.BusProvider;

/**
 * Created by song on 16/10/11.
 */

public class UIApplication extends Application {
    private static final String TAG = "UIApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + Process.myPid());
        BusProvider.create(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        BusProvider.exit(this);
    }
}
