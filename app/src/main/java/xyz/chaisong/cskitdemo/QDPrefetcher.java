package xyz.chaisong.cskitdemo;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.android.volley.ExecutorDelivery;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ResponseDelivery;
import com.android.volley.VolleyError;
import com.android.volley.filedownload.FileBasedCache;
import com.android.volley.filedownload.FileDownloadListener;
import com.android.volley.filedownload.FileDownloadManager;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;

import java.io.File;
import java.util.LinkedList;

import xyz.chaisong.cskitdemo.network.DeliveryExecutor;
import xyz.chaisong.cskitdemo.network.QDNetUtil;
import xyz.chaisong.cskitdemo.network.QDNetWorkCallBack;
import xyz.chaisong.cskitdemo.network.QDRequest;
import xyz.chaisong.cskitdemo.network.request.ReqEntity;
import xyz.chaisong.cskitdemo.network.response.RespEntity;
import xyz.chaisong.cskitdemo.network.response.RespError;

/**
 * Created by song on 16/4/1.
 * wifi预加载使用
 */
public class QDPrefetcher implements QDNetWorkCallBack<ArticleDetailEntity>, FileDownloadListener {
    public final static String SUFFIX_URL = "http://app3.qdaily.com/app3/articles/detail/%s.json";
    private Context mContext;

    private FileDownloadManager mFileDownloadManager;

    private static PrefetcherThread prefetcherThread;
    private static ResponseDelivery prefetcherDelivery;

    private final LinkedList<RequestOperation> mTaskQueue;

    private PrefetcherCallback mListener;

    private final int DefaultParallelTaskCount = 1;

    private int resourseNumPerArticle = 5;
    private int totalNum;
    private int completeNum;

    public static QDPrefetcher defaultInstance = null;

    public final String TAG = "QDPrefetcher";

    public QDPrefetcher(Context context){
        this.mContext = context.getApplicationContext();
        mTaskQueue = new LinkedList<>();
        if (prefetcherThread == null) {
            prefetcherThread = new PrefetcherThread("QDPrefetcherThread");
            prefetcherThread.start();
            prefetcherDelivery = new ExecutorDelivery(new Handler(prefetcherThread.getLooper(), prefetcherThread));
        }
    }

    public static void init(Context context) {
        defaultInstance = new QDPrefetcher(context);
    }

    public FileDownloadManager getFileDownloadManager() {
        if (mFileDownloadManager == null) {
            File dirFile = getPrefetcherFileCacheDir();
            RequestQueue queue = new RequestQueue(new FileBasedCache(dirFile), new BasicNetwork(new HurlStack()));
            mFileDownloadManager = new FileDownloadManager(queue, prefetcherDelivery, 2);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("QDPrefetcher", "Unable to create cache dir " + dirFile.getAbsolutePath());
                }
            }
            queue.start();
        }
        return mFileDownloadManager;
    }

    public void setListener(PrefetcherCallback mListener) {
        this.mListener = mListener;
    }

    public RequestOperation add(int id) {
        RequestOperation controller = new RequestOperation(id, this);
        synchronized (mTaskQueue) {
            mTaskQueue.add(controller);
            totalNum += resourseNumPerArticle;
        }
        schedule();
        return controller;
    }

    //下载文件使用
    public void add(String url) {
        getFileDownloadManager().add(url, this);
    }

    public void clearCache(){

    }

    public File getPrefetcherFileCacheDir(){
        File cacheDir = mContext.getExternalCacheDir();
        return new File(cacheDir, "resource");
    }

    private void schedule() {
        if (clearing)
            return;
        synchronized (mTaskQueue) {
            int parallelTaskCount = 0;
            for (RequestOperation operation : mTaskQueue) {
                if (operation.isDownloading()) parallelTaskCount++;
            }
            if (parallelTaskCount >= DefaultParallelTaskCount) return;

            // try to deploy all Task if they're await.
            for (RequestOperation operation : mTaskQueue) {
                if (operation.deploy() && ++parallelTaskCount == DefaultParallelTaskCount) return;
            }
        }
    }

    /**
     * 成功、失败、或者cancel情况下清理线程
     *
     * @param controller The controller which will be remove.
     */
    private void remove(RequestOperation controller) {
        // also make sure one thread operation
        synchronized (mTaskQueue) {
            mTaskQueue.remove(controller);
        }
        schedule();
    }

    private boolean clearing;
    /**
     * 清理所有任务
     */
    public void cancelAll() {
        clearing = true;
        // make sure only one thread can manipulate the Task Queue.
        synchronized (mTaskQueue) {
            while (!mTaskQueue.isEmpty()) {
                mTaskQueue.get(0).discard();
            }
        }
        clearing = false;
    }


    // prefetcherThread 线程
    @Override
    public void onSuccess(ReqEntity<ArticleDetailEntity> netParams, RespEntity<ArticleDetailEntity> data) {
        ArticleDetailEntity articleDetailEntity = data.getResponseMeta();
        if (articleDetailEntity != null && articleDetailEntity.getMeta().getStatus()==200 && articleDetailEntity.getResponse().getArticle() != null) {
            int resourseNum = articleDetailEntity.getResponse().getArticle().getCss().size() + articleDetailEntity.getResponse().getArticle().getJs().size();
            updateArticleResourceCount(resourseNum + 1);

            for (String url : articleDetailEntity.getResponse().getArticle().getJs()) {
                getFileDownloadManager().add(url, this);
            }
            for (String url : articleDetailEntity.getResponse().getArticle().getCss()) {
                getFileDownloadManager().add(url, this);
            }
        } else {
            updateArticleResourceCount(1); //只有1个资源
        }
        completeOne();
    }

    // prefetcherThread 线程
    @Override
    public void onFail(ReqEntity<ArticleDetailEntity> netParams, RespError failData) {
        updateArticleResourceCount(1); //认为只有1个资源
        completeOne();
    }

    // prefetcherThread 线程
    @Override
    public void onPreExecute() {}

    // prefetcherThread 线程
    @Override
    public void onSuccess(File localFile) {
        completeOne();
    }

    // prefetcherThread 线程
    @Override
    public void onError(VolleyError error) {
        completeOne();
    }

    // prefetcherThread 线程
    @Override
    public void onCancel() {
        completeOne();
    }

    // prefetcherThread 线程
    @Override
    public void onProgressChange(long fileSize, long downloadedSize) {}

    //每完成一个请求更新进度使用，默认认为有资源5个（html+2css+2js），这里服务器返回看下真实是多少。工作在 prefetcherThread 线程
    private void updateArticleResourceCount(int count) {
        totalNum += (count - resourseNumPerArticle);
    }

    //注意  此处应该是在prefetcherThread 线程
    private void completeOne() {
        completeNum++;

        if (mListener != null) {
            DeliveryExecutor.getInstance().postMainThread(new Runnable() {
                @Override
                public void run() {
                    if (completeNum >= totalNum) {
                        mListener.onPrefetcherComplete();
                    } else {
                        mListener.onPrefetcherUpdateProgress(totalNum, completeNum);
                    }
                }
            });
        }
    }

    private static class PrefetcherThread extends HandlerThread implements Handler.Callback {
        public PrefetcherThread(String name) {
            super(name);
        }

        @Override
        public boolean handleMessage(Message msg) {
            System.out.println("handleMessage CurrentThread = " + Thread.currentThread().getName());
            return true;
        }
    }

    public class RequestOperation {
        private QDNetWorkCallBack<ArticleDetailEntity> mListener;
        private int mArticleId;
        private QDRequest<ArticleDetailEntity> mRequest;
        private int mStatus;

        public static final int STATUS_WAITING = 0; //default
        public static final int STATUS_DOWNLOADING = 1;
        public static final int STATUS_SUCCESS = 2;
        public static final int STATUS_DISCARD = 3;

        private RequestOperation(int articleId, QDNetWorkCallBack<ArticleDetailEntity> listener) {
            mArticleId = articleId;
            mListener = listener;
        }

        public void buildRequest(int articleId) {
            mRequest = new QDRequest<>(Request.Method.GET, String.format(SUFFIX_URL, articleId), null, null, new Response.Listener<RespEntity<ArticleDetailEntity>>() {
                @Override
                public void onResponse(RespEntity<ArticleDetailEntity> response) {
                    mStatus = STATUS_SUCCESS;
                    mListener.onSuccess(null, response);
                    remove(RequestOperation.this);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mStatus = STATUS_DISCARD;
                    mListener.onFail(null, RespError.convertError(error));
                    remove(RequestOperation.this);
                }
            });
            mRequest.setDelivery(prefetcherDelivery);
            mRequest.setResponseClass(ArticleDetailEntity.class);
        }

        private boolean deploy() {
            if (mStatus != STATUS_WAITING) return false;
            buildRequest(mArticleId);
            mStatus = STATUS_DOWNLOADING;
            QDNetUtil.getInstance().getRequestQueue().add(mRequest);
            return true;
        }

        public int getStatus() {
            return mStatus;
        }

        public boolean isDownloading() {
            return mStatus == STATUS_DOWNLOADING;
        }

        public boolean discard() {
            if (mStatus == STATUS_DISCARD) return false;
            if (mStatus == STATUS_SUCCESS) return false;
            if (mStatus == STATUS_DOWNLOADING) mRequest.cancel();
            mStatus = STATUS_DISCARD;
            remove(this);
            return true;
        }
    }

    public interface PrefetcherCallback {
        void onPrefetcherUpdateProgress(int totalCount, int completeCount);
        void onPrefetcherComplete();
    }
}
