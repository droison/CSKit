package xyz.chaisong.mmanagercenter;

import android.content.Context;

/**
 * Created by song on 15/6/15.
 */
public abstract class MManager implements MManagerInterface{

    protected MManagerState state = new MManagerState();
    protected final String TAG = getClass().getSimpleName();

    @Override
    public MManagerState getManagerState() {
        return state;
    }

    @Override
    public void onManagerInit(Context context) {

    }

    @Override
    public void onManagerReloadData() {

    }

    @Override
    public void onManagerEnterForeground() {

    }

    @Override
    public void onManagerEnterBackground() {

    }

    @Override
    public void onManagerClearData() {

    }

    @Override
    public void onManagerTerminate() {
        state.isManagerRemoved = true;
    }
}
