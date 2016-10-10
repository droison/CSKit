// ICallBack.aidl
package xyz.chaisong.cskitdemo.idlbus;

import xyz.chaisong.cskitdemo.idlbus.EventHolder;

interface ICallBack {
    void invoke(in EventHolder eventHolder);
}
