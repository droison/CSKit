package xyz.chaisong.mmbus.dispatcher;

/**
 * Created by song on 16/9/14.
 */

class PostingThreadDispatcher implements Dispatcher {
    @Override
    public void dispatch(Runnable runnable) {
        runnable.run();
    }

    @Override
    public boolean stop() {
        return true;
    }
}
