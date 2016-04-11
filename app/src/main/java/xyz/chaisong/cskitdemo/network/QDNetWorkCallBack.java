package xyz.chaisong.cskitdemo.network;

import xyz.chaisong.cskitdemo.network.request.ReqEntity;
import xyz.chaisong.cskitdemo.network.response.RespBaseMeta;
import xyz.chaisong.cskitdemo.network.response.RespEntity;
import xyz.chaisong.cskitdemo.network.response.RespError;

/**
 * 网络回调基类
 */
public interface QDNetWorkCallBack<T extends RespBaseMeta> {
    void onSuccess(ReqEntity<T> netParams, RespEntity<T> data);
    void onFail(ReqEntity<T> netParams, RespError failData);
}