package xyz.chaisong.cskitdemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import java.util.List;

import xyz.chaisong.cskitdemo.idlbus.BusIDLService;
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
        if (isMainProcess()) {
            startService(new Intent(this, BusIDLService.class));
        }
        BusProvider.create(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        BusProvider.exit(this);
    }

    private boolean isMainProcess() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
