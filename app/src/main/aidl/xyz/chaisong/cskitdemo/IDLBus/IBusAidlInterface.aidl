// IBusAidlInterface.aidl
package xyz.chaisong.cskitdemo.IDLBus;

import xyz.chaisong.cskitdemo.IDLBus.EventHolder;
import xyz.chaisong.cskitdemo.IDLBus.ICallBack;
// Declare any non-default types here with import statements

interface IBusAidlInterface {
    void attach(ICallBack cb);
    void detach(ICallBack cb);
    void invokeEvent(in EventHolder eventHolder);
}
