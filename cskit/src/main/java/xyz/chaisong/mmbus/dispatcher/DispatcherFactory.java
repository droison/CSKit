package xyz.chaisong.mmbus.dispatcher;

import android.os.Looper;

import xyz.chaisong.mmbus.annotation.RunThread;

/**
 * Created by SilenceDut on 16/8/2.
 */

public class DispatcherFactory {

    private static final Dispatcher MAIN_THREAD_DISPATCHER = new MainThreadDispatcher();
    private static final Dispatcher POSTING_THREAD_DISPATCHER = new PostingThreadDispatcher();

    public static Dispatcher getEventDispatch(RunThread runThread) {
        switch (runThread) {
            case MAIN:
                return isMainThread()?POSTING_THREAD_DISPATCHER:MAIN_THREAD_DISPATCHER;

            case POSTING:
                return POSTING_THREAD_DISPATCHER;
        }
        return MAIN_THREAD_DISPATCHER;
    }

    private static boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
}
