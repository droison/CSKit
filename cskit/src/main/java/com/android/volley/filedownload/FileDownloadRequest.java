package com.android.volley.filedownload;

import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ResponseDelivery;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

/**
 * Created by song on 16/3/30.
 */
class FileDownloadRequest extends Request<File>{
    private File mStoreFile;
    private File mTemporaryFile;
    private FileDownloadListener mListener;

    public FileDownloadRequest(String storeFilePath, String url) {
        this(new File(storeFilePath), url);
    }

    public FileDownloadRequest(File storeFile, String url) {
        super(Method.GET, url, null);
        mStoreFile = storeFile;
        mTemporaryFile = new File(storeFile + ".tmp");

        // Turn the retries frequency greater.
        setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 200, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        setShouldCache(false);
    }

    @Override
    public void prepare() {
        super.prepare();
        addHeader("Range", "bytes=" + mTemporaryFile.length() + "-");
    }

    @Override
    public String getCacheKey() {
        return getUrl();
    }

    /**
     * Ignore the response content, just rename the TemporaryFile to StoreFile.
     */
    @Override
    protected Response<File> parseNetworkResponse(NetworkResponse response) {
        if (!isCanceled()) {
            if (mTemporaryFile.canRead() && mTemporaryFile.length() > 0) {
                if (mTemporaryFile.renameTo(mStoreFile)) {
                    VolleyLog.d("download success:"+ getUrl());
                    return Response.success(mStoreFile, null);
                } else {
                    return Response.error(new VolleyError("Can't rename the download temporary file!"));
                }
            } else if (mStoreFile.exists()) { //走了缓存
                VolleyLog.d("read cache:"+ getUrl());
                return Response.success(mStoreFile, null);
            } else {
                return Response.error(new VolleyError("Download temporary file was invalid!"));
            }
        }
        return Response.error(new VolleyError("Request was Canceled!"));
    }

    @Override
    public boolean isHandleResponse() {
        return true;
    }


    /**
     * In this method, we got the Content-Length, with the TemporaryFile length,
     * we can calculate the actually size of the whole file, if TemporaryFile not exists,
     * we'll take the store file length then compare to actually size, and if equals,
     * we consider this download was already done.
     * We used {@link RandomAccessFile} to continue download, when download success,
     * the TemporaryFile will be rename to StoreFile.
     */
    @Override
    public byte[] handleResponse(HttpResponse response, ResponseDelivery delivery) throws IOException, ServerError {
        // Content-Length might be negative when use HttpURLConnection because it default header Accept-Encoding is gzip,
        // we can force set the Accept-Encoding as identity in prepare() method to slove this problem but also disable gzip response.
        HttpEntity entity = response.getEntity();
        long fileSize = entity.getContentLength();
        if (fileSize <= 0) {
            VolleyLog.d("Response doesn't present Content-Length!");
        }

        long downloadedSize = mTemporaryFile.length();
        boolean isSupportRange = HttpUtils.isSupportRange(response);
        if (isSupportRange) {
            fileSize += downloadedSize;

            // Verify the Content-Range Header, to ensure temporary file is part of the whole file.
            // Sometime, temporary file length add response content-length might greater than actual file length,
            // in this situation, we consider the temporary file is invalid, then throw an exception.
            String realRangeValue = HttpUtils.getHeader(response, "Content-Range");
            // response Content-Range may be null when "Range=bytes=0-"
            if (!TextUtils.isEmpty(realRangeValue)) {
                String assumeRangeValue = "bytes " + downloadedSize + "-" + (fileSize - 1);
                if (TextUtils.indexOf(realRangeValue, assumeRangeValue) == -1) {
                    throw new IllegalStateException(
                            "The Content-Range Header is invalid Assume[" + assumeRangeValue + "] vs Real[" + realRangeValue + "], " +
                                    "please remove the temporary file [" + mTemporaryFile + "].");
                }
            }
        }

        // Compare the store file size(after download successes have) to server-side Content-Length.
        // temporary file will rename to store file after download success, so we compare the
        // Content-Length to ensure this request already download or not.
        if (fileSize > 0 && mStoreFile.length() == fileSize) {
            // Rename the store file to temporary file, mock the download success. ^_^
            mStoreFile.renameTo(mTemporaryFile);

            // Deliver download progress.
            delivery.postDownloadProgress(this, fileSize, fileSize);

            return null;
        }

        RandomAccessFile tmpFileRaf = new RandomAccessFile(mTemporaryFile, "rw");

        // If server-side support range download, we seek to last point of the temporary file.
        if (isSupportRange) {
            tmpFileRaf.seek(downloadedSize);
        } else {
            // If not, truncate the temporary file then start download from beginning.
            tmpFileRaf.setLength(0);
            downloadedSize = 0;
        }

        InputStream in = null;
        try {
            in = entity.getContent();
            // Determine the response gzip encoding, support for HttpClientStack download.
            if (HttpUtils.isGzipContent(response) && !(in instanceof GZIPInputStream)) {
                in = new GZIPInputStream(in);
            }
            byte[] buffer = new byte[6 * 1024]; // 6K buffer
            int offset;

            while ((offset = in.read(buffer)) != -1) {
                tmpFileRaf.write(buffer, 0, offset);

                downloadedSize += offset;
                delivery.postDownloadProgress(this, fileSize, downloadedSize);

                if (isCanceled()) {
                    delivery.postCancel(this);
                    break;
                }
            }
        } finally {
            try {
                // Close the InputStream
                if (in != null) in.close();
            } catch (Exception e) {
                VolleyLog.v("Error occured when calling InputStream.close");
            }

            try {
                // release the resources by "consuming the content".
                entity.consumeContent();
            } catch (Exception e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                VolleyLog.v("Error occured when calling consumingContent");
            }
            tmpFileRaf.close();
        }

        return null;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    @Override
    public void deliverPreExecute() {
        if (mListener != null) {
            mListener.onPreExecute();
        }
    }

    @Override
    protected void deliverResponse(File response) {
        if (mListener != null) {
            mListener.onSuccess(response);
        }
    }

    public void deliverDownloadProgress(long fileSize, long downloadedSize) {
        if (mListener != null) {
            mListener.onProgressChange(fileSize, downloadedSize);
        }
    }

    public void deliverError(VolleyError error) {
        if (mListener != null) {
            mListener.onError(error);
        }
    }

    @Override
    public void deliverCancel() {
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    public void setListener(FileDownloadListener listener) {
        this.mListener = listener;
    }
}
