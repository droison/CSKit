package xyz.chaisong.mmbus.dispatcher;

import android.os.Handler;
import android.os.Looper;


/**
 * Created by song on 16/9/14.
 */

class MainThreadDispatcher implements Dispatcher {
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void dispatch(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    @Override
    public boolean stop() {
        mMainHandler.removeCallbacksAndMessages(null);
        return true;
    }
}
