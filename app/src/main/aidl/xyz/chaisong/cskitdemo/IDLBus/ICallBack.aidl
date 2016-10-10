// ICallBack.aidl
package xyz.chaisong.cskitdemo.IDLBus;

import xyz.chaisong.cskitdemo.IDLBus.EventHolder;

interface ICallBack {
    void invoke(in EventHolder eventHolder);
}
