package xyz.chaisong.cskitdemo.network.request;

import java.util.Map;

import xyz.chaisong.cskitdemo.network.response.RespBaseMeta;

/**
 * Created by song on 15/9/19.
 */
public abstract class ReqEntity<T> {
    private Class<T> responseClass;

    public abstract Map<String, Object> getParams(); //get会以?&形式放到末尾，post、Deleta、PUT会放在body里

    public abstract String getUrl();

    public ReqConfig getRequestConfig() {
        return new ReqConfig(ReqConfig.RequestCache.NotUseCache); //默认不使用cache
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }

    public ReqEntity<T> setResponseClass(Class<T> responseClass) {
        this.responseClass = responseClass;
        return this;
    }

    public ReqEntity<RespBaseMeta> convertBase() {
        return new ReqEntity<RespBaseMeta>() {
            @Override
            public Map<String, Object> getParams() {
                return ReqEntity.this.getParams();
            }

            @Override
            public String getUrl() {
                return ReqEntity.this.getUrl();
            }

            @Override
            public Class<RespBaseMeta> getResponseClass() {
                return RespBaseMeta.class;
            }

            public ReqConfig getRequestConfig() {
                return ReqEntity.this.getRequestConfig(); //默认不使用cache
            }
        };
    }
}
