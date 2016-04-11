package xyz.chaisong.cskitdemo.network.response;

import java.util.Map;

/**
 * Created by song on 15/11/27.
 */
public class RespEntity<T extends RespBaseMeta> {
    private T responseMeta;
    private HttpResponseMeta httpResponseMeta;
    private boolean isCache;
    private String responseString;

    public boolean isCache() {
        return isCache;
    }

    public void setCache(boolean cache) {
        isCache = cache;
    }

    public HttpResponseMeta getHttpResponseMeta() {
        return httpResponseMeta;
    }

    public void setHttpResponseMeta(HttpResponseMeta httpResponseMeta) {
        this.httpResponseMeta = httpResponseMeta;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }

    public T getResponseMeta() {
        return responseMeta;
    }

    public void setResponseMeta(T responseMeta) {
        this.responseMeta = responseMeta;
    }

    public static class HttpResponseMeta {
        public final int statusCode;
        /** Response headers. */
        public final Map<String, String> headers;
        /** True if the server returned a 304 (Not Modified). */
        public final boolean notModified;
        /** Network roundtrip time in milliseconds. */
        public final long networkTimeMs;
        public HttpResponseMeta(int statusCode, Map<String, String> headers,
                                boolean notModified, long networkTimeMs) {
            this.statusCode = statusCode;
            this.headers = headers;
            this.notModified = notModified;
            this.networkTimeMs = networkTimeMs;
        }
    }

    public RespEntity<RespBaseMeta> convertBase() {
        RespEntity<RespBaseMeta> result = new RespEntity<RespBaseMeta>();
        result.setResponseMeta((RespBaseMeta) responseMeta);
        result.setHttpResponseMeta(httpResponseMeta);
        return result;
    }
}