// IBusAidlInterface.aidl
package xyz.chaisong.cskitdemo.idlbus;

import xyz.chaisong.cskitdemo.idlbus.EventHolder;
import xyz.chaisong.cskitdemo.idlbus.ICallBack;
// Declare any non-default types here with import statements

interface IBusAidlInterface {
    void attach(ICallBack cb);
    void detach(ICallBack cb);
    void invokeEvent(in EventHolder eventHolder);
}
