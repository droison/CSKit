// ICallBack.aidl
package xyz.chaisong.cskitdemo.idlBus;

import xyz.chaisong.cskitdemo.idlBus.EventHolder;

interface ICallBack {
    void invoke(in EventHolder eventHolder);
}
