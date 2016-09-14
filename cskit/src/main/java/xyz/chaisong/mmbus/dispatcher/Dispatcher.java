package xyz.chaisong.mmbus.dispatcher;

/**
 * Created by song on 16/9/14.
 */

public interface Dispatcher {
    void dispatch(Runnable runnable);
    boolean stop();
}
