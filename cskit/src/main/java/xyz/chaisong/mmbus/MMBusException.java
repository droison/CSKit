package xyz.chaisong.mmbus;

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
        throw new MMBusException(detailMessage);
    }

    public static void throwException(Throwable throwable) {
        throw new MMBusException(throwable);
    }

    public static void throwException(String detailMessage, Throwable throwable) {
        throw new MMBusException(detailMessage, throwable);
    }
}