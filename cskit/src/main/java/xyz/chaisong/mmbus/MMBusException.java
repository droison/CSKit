package xyz.chaisong.mmbus;

import android.util.Log;

/**
 * Created by song on 16/9/14.
 */

public class MMBusException extends RuntimeException {

    public MMBusException(String detailMessage) {
        super(detailMessage);
    }

    public MMBusException(Throwable throwable) {
        super(throwable);
    }

    public MMBusException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public static void throwException(String detailMessage) {
        if (MMBus.isDebugMode)
            throw new MMBusException(detailMessage);
        else
            Log.e("MMBus", detailMessage);
    }

    public static void throwException(Throwable throwable) {
        if (MMBus.isDebugMode)
            throw new MMBusException(throwable);
        else
            Log.e("MMBus", "", throwable);
    }

    public static void throwException(String detailMessage, Throwable throwable) {
        if (MMBus.isDebugMode)
            throw new MMBusException(detailMessage, throwable);
        else
            Log.e("MMBus", detailMessage, throwable);
    }
}