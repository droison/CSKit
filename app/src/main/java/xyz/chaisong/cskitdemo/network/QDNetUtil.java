package xyz.chaisong.cskitdemo.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import java.io.File;
import java.util.Map;
import java.util.Set;

import xyz.chaisong.cskitdemo.network.request.ReqConfig;
import xyz.chaisong.cskitdemo.network.request.ReqEntity;
import xyz.chaisong.cskitdemo.network.request.ReqPrepare;
import xyz.chaisong.cskitdemo.network.response.RespBaseMeta;
import xyz.chaisong.cskitdemo.network.response.RespEntity;
import xyz.chaisong.cskitdemo.network.response.RespError;

/**
 * Created by song on 15/11/27.
 * 中间层，分割volley和上层逻辑
 */
public class QDNetUtil {
    private static QDNetUtil mQDNetUtil;

    private Context mContext;
    private final String LOG_TAG = "QDNetUtil";

    private RequestQueue mQueue;

    private QDNetUtil(Context context) {
        this.mContext = context;
    }

    public RequestQueue getRequestQueue(){
        if (mQueue == null) {
            File cacheDir = mContext.getExternalCacheDir();

            Network network = new BasicNetwork(new HurlStack());
            mQueue = new RequestQueue(new DiskBasedCache(new File(cacheDir, "request")), network, 6);
            mQueue.start();
        }
        return mQueue;
    }

    public static void init(Context context) {
        if (mQDNetUtil == null) {
            mQDNetUtil = new QDNetUtil(context);
        }
    }

    public static QDNetUtil getInstance() {
        if (mQDNetUtil == null) {
            throw new RuntimeException("mQDNetUtil == null, you need use getInstance() after init(Context context)");
        }
        return mQDNetUtil;
    }

    public static QDNetUtil getInstance(Context context) { //仅仅用于兼容接口
        if (mQDNetUtil == null) {
            throw new RuntimeException("mQDNetUtil == null, you need use getInstance() after init(Context context)");
        }
        return mQDNetUtil;
    }

    public <T extends RespBaseMeta> void get(final ReqEntity<T> requestEntity, final QDNetWorkCallBack<T> QDNetWorkCallBack) {
        if (null == requestEntity || TextUtils.isEmpty(requestEntity.getUrl())) {
            Log.d(LOG_TAG, "the get is broken");
            callBackFailResponse(QDNetWorkCallBack, requestEntity, RespError.paramsError(null));
            return;
        }

        final String requestUrl = convertGetUrl(requestEntity.getUrl(), requestEntity.getParams());
        QDRequest<T> request = new QDRequest<T>(Request.Method.GET, requestUrl, null, null, new Response.Listener<RespEntity<T>>() {
            @Override
            public void onResponse(RespEntity<T> response) {
                //先回包，再缓存。
                callBackServerResponse(QDNetWorkCallBack, requestEntity, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBackFailResponse(QDNetWorkCallBack, requestEntity, RespError.convertError(error));
            }
        });
        request.setResponseClass(requestEntity.getResponseClass());


        if (requestEntity.getRequestConfig().getRequestCache() != ReqConfig.RequestCache.NotUseCache) {
            Cache.Entry entry = getRequestQueue().getCache().get(request.getCacheKey());
            if (entry != null) { //有缓存
                Response<RespEntity<T>> response = request.parseNetworkResponse(
                        new NetworkResponse(entry.data, entry.responseHeaders));

                if (!entry.isExpired()) { //未过3天的有效期
                    // Completely unexpired cache hit. Just deliver the response.
                    RespEntity<T> cacheResponse = response.result;
                    cacheResponse.setCache(true);
                    callBackCacheResponse(QDNetWorkCallBack, requestEntity, cacheResponse);
                }

                if (requestEntity.getRequestConfig().getRequestCache() == ReqConfig.RequestCache.UseCachePrimary) {
                    return;
                }
            }
            if (requestEntity.getRequestConfig().getRequestCache() == ReqConfig.RequestCache.OnlyUseCache)
                return;
        }


        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(request);
    }

    //私有方法，PUT DELETE POST走这里
    private <T extends RespBaseMeta> QDRequest<T> request(int method, final ReqEntity<T> requestEntity, ReqPrepare prepare, final QDNetWorkCallBack<T> QDNetWorkCallBack) {
        if (null == requestEntity || TextUtils.isEmpty(requestEntity.getUrl())) {
            Log.d(LOG_TAG, "the request is broken, method=" + method);
            callBackFailResponse(QDNetWorkCallBack, requestEntity, RespError.paramsError(null));
            return null;
        }

        QDRequest<T> request = new QDRequest<T>(method, requestEntity.getUrl(), requestEntity.getParams(), prepare, new Response.Listener<RespEntity<T>>() {
            @Override
            public void onResponse(RespEntity<T> response) {
                callBackServerResponse(QDNetWorkCallBack, requestEntity, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBackFailResponse(QDNetWorkCallBack, requestEntity, RespError.convertError(error));
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 0));
        request.setResponseClass(requestEntity.getResponseClass());
        getRequestQueue().add(request);
        return request;
    }

    public <T extends RespBaseMeta> void postFile(ReqEntity<T> requestEntity, ReqPrepare prepare, QDNetWorkCallBack<T> QDNetWorkCallBack) {
        request(Request.Method.POST, requestEntity, prepare, QDNetWorkCallBack);
    }

    public <T extends RespBaseMeta> void post(ReqEntity<T> requestEntity, QDNetWorkCallBack<T> QDNetWorkCallBack) {
        postFile(requestEntity, null, QDNetWorkCallBack);
    }

    public <T extends RespBaseMeta> void putFile(ReqEntity<T> requestEntity, ReqPrepare prepare, QDNetWorkCallBack<T> QDNetWorkCallBack) {
        request(Request.Method.PUT, requestEntity, prepare, QDNetWorkCallBack);
    }

    public <T extends RespBaseMeta> void put(ReqEntity<T> requestEntity, QDNetWorkCallBack<T> QDNetWorkCallBack) {
        putFile(requestEntity, null, QDNetWorkCallBack);
    }

    public <T extends RespBaseMeta> void delete(ReqEntity<T> requestEntity, QDNetWorkCallBack<T> QDNetWorkCallBack) {
        request(Request.Method.DELETE, requestEntity, null, QDNetWorkCallBack);
    }

    //logout由于特殊性,cookie必须先传入
    public <T extends RespBaseMeta> void logout(ReqEntity<T> requestEntity, QDNetWorkCallBack<T> QDNetWorkCallBack) {
        request(Request.Method.DELETE, requestEntity, null, QDNetWorkCallBack);
    }

    // TODO: 15/11/27 参数拼装
    private String convertGetUrl(@NonNull String url, Map<String, Object> params) {
        if (params == null || params.size() == 0)
            return url;

        StringBuilder sb = new StringBuilder(url);
        Set<String> keys = params.keySet();
        int i = 0;
        for (String key : keys) {
            sb.append(i > 0 ? "&" : "?");
            sb.append(key).append("=").append(params.get(key));
            i++;
        }

        return sb.toString();
    }

    private <T extends RespBaseMeta> void callBackServerResponse(QDNetWorkCallBack<T> callBack, ReqEntity<T> netParams, RespEntity<T> data) {
        if (callBack != null) {
            callBack.onSuccess(netParams, data);
        }
    }

    //cache会在子线程返回
    private <T extends RespBaseMeta> void callBackCacheResponse(final QDNetWorkCallBack<T> callBack, final ReqEntity<T> netParams, final RespEntity<T> data) {
        DeliveryExecutor.getInstance().postMainThread(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onSuccess(netParams, data);
                }
            }
        });
    }

    private <T extends RespBaseMeta> void callBackFailResponse(QDNetWorkCallBack<T> callBack, ReqEntity<T> netParams, RespError failData) {
        if (callBack != null) {
            callBack.onFail(netParams, failData);
        }
        Log.d("QDNetUtil",""+netParams.toString()+"\r\r\r"+failData.toString());
    }
}