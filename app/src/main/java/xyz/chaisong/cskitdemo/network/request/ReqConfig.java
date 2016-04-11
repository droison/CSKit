package xyz.chaisong.cskitdemo.network.request;

/**
 * Created by song on 15/11/27.
 */
public class ReqConfig {
    public enum RequestCache {
        UserCacheAndServer,
        OnlyUseCache,
        UseCachePrimary,
        NotUseCache;
    }

    private RequestCache requestCache;
    public ReqConfig(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    public RequestCache getRequestCache() {
        return requestCache;
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
