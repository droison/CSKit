package xyz.chaisong.mmservicecenter;

import android.content.Context;

/**
 * Created by song on 15/6/14.
 */
public interface MMServiceInterface {

    MMServiceState getServiceState();
    // call after yourservice create.
    void onServiceInit(Context context);
    // 切换帐号后，调用。
    void onServiceReloadData();
    // 进入后台运行
    void onServiceEnterBackground();
    // 进入前台运行
    void onServiceEnterForeground();
    // 程序退出
    void onServiceTerminate();
    // 退出登录时调用 用于清理资源.
    void onServiceClearData();

    class MMServiceState
    {
        public boolean isServiceRemoved = false;
        public boolean isServicePersistent = false; //退出应用时常驻
    }
}
