// ICallBack.aidl
package xyz.chaisong.mmbus.aidl;

import xyz.chaisong.mmbus.aidl.EventHolder;

interface ICallBack {
    void invoke(in EventHolder eventHolder);
}
