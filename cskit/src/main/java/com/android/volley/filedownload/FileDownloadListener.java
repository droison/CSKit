package com.android.volley.filedownload;

import com.android.volley.VolleyError;

import java.io.File;

/**
 * Created by song on 16/3/30.
 */
public interface FileDownloadListener{
    /**
     * Inform when start to handle this Request.
     */
    void onPreExecute();

    /**
     * Called when response success.
     */
    void onSuccess(File localFile);

    /**
     * Callback method that an error has been occurred with the
     * provided error code and optional user-readable message.
     */
    void onError(VolleyError error);

    void onCancel();
    /**
     * Inform when download progress change, this callback method only available
     * when request was {@link FileDownloadRequest}.
     */
    void onProgressChange(long fileSize, long downloadedSize);
}
