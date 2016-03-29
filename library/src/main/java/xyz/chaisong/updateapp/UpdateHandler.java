package xyz.chaisong.updateapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

import xyz.chaisong.cskit.R;

public class UpdateHandler extends Handler {

    /**
     * 更新进度
     */
    private ProgressBar mProgress;

    private Activity context;

    private String appname;
    private AlertDialog dialog;
    private String appFilePath;

    private boolean isReady;

    public UpdateHandler(Activity context, String appname) {
        this.context = context;
        this.appname = appname;
        appFilePath = getExternalCacheDir(context).getAbsolutePath();
        isReady = isFolderExists(appFilePath);
    }

    boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    protected void installApk(File file) {

        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");

        context.startActivity(intent);
    }

    @Override
    public void handleMessage(Message mes) {
        if (!isReady)
            return;

        switch (mes.what) {
            case ApkDownloadService.HANDLER_VERSION_UPDATE:
                CheckVersionBase cvb = (CheckVersionBase) mes.obj;
                showCustomDialog(cvb);
                break;
            case ApkDownloadService.HANDLER_APK_DOWNLOAD_PROGRESS:
                mProgress.setProgress((Integer) mes.obj);
                break;
            case ApkDownloadService.HANDLER_APK_DOWNLOAD_FINISH:
                dialog.dismiss();
                File file = new File(appFilePath, appname);
                installApk(file);
                context.finish();
                break;
            case ApkDownloadService.HANDLER_HTTPSTATUS_ERROR:
                Log.v("update", "检查失败");
                if (dialog!=null && dialog.isShowing())
                      dialog.dismiss();
                break;
            default:
                Log.v("update", "检查失败");
                if (dialog!=null && dialog.isShowing())
                    dialog.dismiss();
                break;
        }
    }

    private void showDefaultDialog(CheckVersionBase cvb){
        if (!cvb.getUrl().startsWith("http://"))
        {
            cvb.setUrl("http://" + cvb.getUrl());
        }

        final String downloadUrl = cvb.getUrl();
        appname += "(" + cvb.getVersion() + ").apk";

        Builder builer = new Builder(context);
        if (cvb.getTitle() == null || cvb.getTitle().equals(""))
        {
            builer.setTitle("升级提示");
        }
        else
        {
            builer.setTitle(cvb.getTitle());
        }

        if (cvb.getExcerpt() == null || cvb.getExcerpt().equals(""))
        {
            builer.setMessage("新版本发布了，请您更新");
        }
        else
        {
            builer.setMessage(cvb.getExcerpt());
        }

        builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Builder builder = new Builder(context);
                builder.setTitle("新版本下载更新中");
                final LayoutInflater inflater = LayoutInflater.from(context);
                View v = inflater.inflate(R.layout.update_progress, null);
                mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
                builder.setView(v);
                Dialog downloadDialog = builder.create();
                downloadDialog.setCancelable(false);
                downloadDialog.show();
                ApkDownloadService downloadService = new ApkDownloadService(downloadUrl, UpdateHandler.this, appFilePath + "/" +appname);
                downloadService.start();
            }
        });

        builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//					context.finish();
            }
        });
        dialog = builer.create();
        dialog.show();
    }

    private void showCustomDialog(CheckVersionBase cvb){
        if (!cvb.getUrl().startsWith("http://"))
        {
            cvb.setUrl("http://" + cvb.getUrl());
        }

        final String downloadUrl = cvb.getUrl();
        appname += "(" + cvb.getVersion() + ").apk";

        dialog = new QDCustomDialog(context);

        if (cvb.getTitle() == null || cvb.getTitle().equals(""))
        {
            ((QDCustomDialog) dialog).buildTitle("升级提示");
        }
        else
        {
            ((QDCustomDialog) dialog).buildTitle(cvb.getTitle());
        }

        if (cvb.getExcerpt() == null || cvb.getExcerpt().equals(""))
        {
            ((QDCustomDialog) dialog).buildMessage("新版本发布了，请您更新");
        }
        else
        {
            ((QDCustomDialog) dialog).buildMessage(cvb.getExcerpt());
        }

        ((QDCustomDialog) dialog).buildCancelButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
//					context.finish();
            }
        });

        ((QDCustomDialog) dialog).buildCommitButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Builder builder = new Builder(context);
                builder.setTitle("新版本下载更新中");
                final LayoutInflater inflater = LayoutInflater.from(context);
                View v = inflater.inflate(R.layout.update_progress, null);
                mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
                builder.setView(v);
                Dialog downloadDialog = builder.create();
                downloadDialog.setCancelable(false);
                downloadDialog.show();
                ApkDownloadService downloadService = new ApkDownloadService(downloadUrl, UpdateHandler.this, appFilePath + "/" +appname);
                downloadService.start();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= 8) {
            File path = context.getExternalCacheDir();

            // In some case, even the sd card is mounted, getExternalCacheDir will return null, may be it is nearly full.
            if (path != null) {
                return path;
            }
        }

        // Before Froyo or the path is null, we need to construct the external cache folder ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
}