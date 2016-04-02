package xyz.chaisong.mmanagercenter;

import android.content.Context;

/**
 * Created by song on 15/6/14.®
 */
public interface MManagerInterface {

    MManagerState getManagerState();
    // call after yourservice create.
    void onManagerInit(Context context);
    // 切换帐号后，调用。
    void onManagerReloadData();
    // 进入后台运行
    void onManagerEnterBackground();
    // 进入前台运行
    void onManagerEnterForeground();
    // 程序退出
    void onManagerTerminate();
    // 退出登录时调用 用于清理资源.
    void onManagerClearData();

    class MManagerState
    {
        public boolean isManagerRemoved = false;
        public boolean isManagerPersistent = false; //退出应用时常驻
    }
}
