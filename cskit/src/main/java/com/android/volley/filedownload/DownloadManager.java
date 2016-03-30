package com.android.volley.filedownload;

import android.os.Looper;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by song on 16/3/30.
 */
public class DownloadManager {
    private final RequestQueue mRequestQueue;

    private final int mParallelTaskCount;

    private final LinkedList<DownloadOperation> mTaskQueue;

    public DownloadManager(RequestQueue queue, int parallelTaskCount) {
        if (parallelTaskCount >= queue.getThreadPoolSize()) {
            throw new IllegalArgumentException("parallelTaskCount[" + parallelTaskCount
                    + "] must less than threadPoolSize[" + queue.getThreadPoolSize() + "] of the RequestQueue.");
        }

        mTaskQueue = new LinkedList<DownloadOperation>();
        mParallelTaskCount = parallelTaskCount;
        mRequestQueue = queue;
    }

    public DownloadOperation add(File storeFile, String url, FileDownloadListener listener) {
        checkTaskThread();

        DownloadOperation controller = new DownloadOperation(storeFile, url, listener);
        synchronized (mTaskQueue) {
            mTaskQueue.add(controller);
        }
        schedule();
        return controller;
    }

    public DownloadOperation add(String storeFilePath, String url, FileDownloadListener listener) {
        return add(new File(storeFilePath), url, listener);
    }

    public DownloadOperation get(File storeFile, String url) {
        synchronized (mTaskQueue) {
            for (DownloadOperation controller : mTaskQueue) {
                if (controller.mStoreFile.equals(storeFile) &&
                        controller.mUrl.equals(url)) return controller;
            }
        }
        return null;
    }

    public DownloadOperation get(String storeFilePath, String url) {
        return get(new File(storeFilePath), url);
    }

    private void schedule() {
        checkTaskThread();
        if (clearing)
            return;

        int parallelTaskCount = 0;
        for (DownloadOperation controller : mTaskQueue) {
            if (controller.isDownloading()) parallelTaskCount++;
        }
        if (parallelTaskCount >= mParallelTaskCount) return;

        // try to deploy all Task if they're await.
        for (DownloadOperation controller : mTaskQueue) {
            if (controller.deploy() && ++parallelTaskCount == mParallelTaskCount) return;
        }
    }

    /**
     * 成功、失败、或者cancel情况下清理线程
     *
     * @param controller The controller which will be remove.
     */
    private void remove(DownloadOperation controller) {
        checkTaskThread();
        mTaskQueue.remove(controller);
        schedule();
    }

    private boolean clearing;
    /**
     * 清理所有任务
     */
    public void clearAll() {
        checkTaskThread();
        clearing = true;
        while (!mTaskQueue.isEmpty()) {
            mTaskQueue.get(0).discard();
        }
        clearing = false;
    }

    /**
     * 检测工作线程，强制下载的调用和回调线程在当前线程
     */
    private void checkTaskThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("FileDownloader must be invoked from the main thread.");
        }
    }

    public FileDownloadRequest buildRequest(File storeFile, String url) {
        return new FileDownloadRequest(storeFile, url);
    }

    /**
     * This class included all such as PAUSE, RESUME, DISCARD to manipulating download task,
     * it created by {@link #add(String, String, FileDownloadListener)},
     * offer three params to constructing {@link FileDownloadRequest} then perform http downloading,
     * you can check the download status whenever you want to know.
     */
    public class DownloadOperation {
        // Persist the Request createing params for re-create it when pause operation gone.
        private FileDownloadListener mListener;
        private File mStoreFile;
        private String mUrl;

        // The download request.
        private FileDownloadRequest mRequest;

        private int mStatus;
        public static final int STATUS_WAITING = 0;
        public static final int STATUS_DOWNLOADING = 1;
        public static final int STATUS_PAUSE = 2;
        public static final int STATUS_SUCCESS = 3;
        public static final int STATUS_DISCARD = 4;

        private DownloadOperation(String storeFilePath, String url, FileDownloadListener listener) {
            this(new File(storeFilePath), url, listener);
        }

        private DownloadOperation(File storeFile, String url, FileDownloadListener listener) {
            mStoreFile = storeFile;
            mListener = listener;
            mUrl = url;
        }

        /**
         * For the parallel reason, only the {@link #schedule()} can call this method.
         *
         * @return true if deploy is successed.
         */
        private boolean deploy() {
            if (mStatus != STATUS_WAITING) return false;

            mRequest = buildRequest(mStoreFile, mUrl);

            // we create a Listener to wrapping that Listener which developer specified,
            // for the onFinish(), onSuccess(), onError() won't call when request was cancel reason.
            mRequest.setListener(new FileDownloadListener() {
                boolean isCanceled;

                @Override
                public void onPreExecute() {
                    mListener.onPreExecute();
                }

                @Override
                public void onSuccess(File response) {
                    // we don't inform SUCCESS when it was cancel.
                    if (!isCanceled) {
                        mListener.onSuccess(response);
                        remove(DownloadOperation.this);
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    // we don't inform ERROR when it was cancel.
                    if (!isCanceled) {
                        mListener.onError(error);
                        remove(DownloadOperation.this);
                    }
                }

                @Override
                public void onCancel() {
                    isCanceled = true;
                    remove(DownloadOperation.this);
                }

                @Override
                public void onProgressChange(long fileSize, long downloadedSize) {
                    mListener.onProgressChange(fileSize, downloadedSize);
                }
            });

            mStatus = STATUS_DOWNLOADING;
            mRequestQueue.add(mRequest);
            return true;
        }

        public int getStatus() {
            return mStatus;
        }

        public boolean isDownloading() {
            return mStatus == STATUS_DOWNLOADING;
        }

        /**
         * Pause this task when it status was DOWNLOADING|WAITING. In fact, we just marked the request should be cancel,
         * http request cannot stop immediately, we assume it will finish soon, thus we set the status as PAUSE,
         * let Task Queue deploy a new Request. That will cause parallel tasks growing beyond maximum task count,
         * but it doesn't matter, we expected that situation never stay longer.
         *
         * @return true if did the pause operation.
         */
        public boolean pause() {
            switch (mStatus) {
                case STATUS_DOWNLOADING:
                    mRequest.cancel();
                case STATUS_WAITING:
                    mStatus = STATUS_PAUSE;
                    schedule();
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Resume this task when it status was PAUSE, we will turn the status as WAITING, then re-schedule the Task Queue,
         * if parallel counter take an idle place, this task will re-deploy instantly,
         * if not, the status will stay WAITING till idle occur.
         *
         * @return true if did the resume operation.
         */
        public boolean resume() {
            if (mStatus == STATUS_PAUSE) {
                mStatus = STATUS_WAITING;
                schedule();
                return true;
            }
            return false;
        }

        /**
         * We will discard this task from the Task Queue, if the status was DOWNLOADING,
         * we first cancel the Request, then remove task from the Task Queue,
         * also re-schedule the Task Queue at last.
         *
         * @return true if did the discard operation.
         */
        public boolean discard() {
            if (mStatus == STATUS_DISCARD) return false;
            if (mStatus == STATUS_SUCCESS) return false;
            if (mStatus == STATUS_DOWNLOADING) mRequest.cancel();
            mStatus = STATUS_DISCARD;
            remove(this);
            return true;
        }
    }
}
