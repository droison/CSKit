// IBusAidlInterface.aidl
package xyz.chaisong.cskitdemo.idlBus;

import xyz.chaisong.cskitdemo.idlBus.EventHolder;
import xyz.chaisong.cskitdemo.idlBus.ICallBack;
// Declare any non-default types here with import statements

interface IBusAidlInterface {
    void attach(ICallBack cb);
    void detach(ICallBack cb);
    void invokeEvent(in EventHolder eventHolder);
}
