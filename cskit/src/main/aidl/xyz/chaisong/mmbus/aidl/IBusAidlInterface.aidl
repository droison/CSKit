// IBusAidlInterface.aidl
package xyz.chaisong.mmbus.aidl;

import xyz.chaisong.mmbus.aidl.EventHolder;
import xyz.chaisong.mmbus.aidl.ICallBack;
// Declare any non-default types here with import statements

interface IBusAidlInterface {
    void attach(ICallBack cb);
    void detach(ICallBack cb);
    void invokeEvent(in EventHolder eventHolder);
}
