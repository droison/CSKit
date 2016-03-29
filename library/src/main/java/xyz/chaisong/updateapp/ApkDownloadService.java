package xyz.chaisong.updateapp;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ApkDownloadService extends Thread {

	public static final int HANDLER_APK_DOWNLOAD_PROGRESS = 0x1001;
	public static final int HANDLER_APK_DOWNLOAD_FINISH = 0x1002;
	public static final int HANDLER_VERSION_UPDATE = 0x1003;
	public static final int HANDLER_HTTPSTATUS_ERROR = 0x1004;


	private static final String TAG = "APK_DOWNLOAD_SERVICE";

	private String downloadUrl = null;

	private Handler mHandler = null;

	private String apkFilePath;
	
	public ApkDownloadService(String downloadUrl, Handler hanlder, String apkFilePath) {
		this.mHandler = hanlder;
		this.downloadUrl = downloadUrl;
		this.apkFilePath = apkFilePath;
	}

	@Override
	public void run() {
		try {
			URL url = new URL(downloadUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			int length = conn.getContentLength();
			InputStream is = conn.getInputStream();

			String tempPath = apkFilePath + ".tmp";

			FileOutputStream fos = new FileOutputStream(tempPath);

			int count = 0;
			byte buf[] = new byte[1024];

			do {
				int numread = is.read(buf);
				count += numread;
				int progress = (int) (((float) count / length) * 100);
				// 更新进度
				mHandler.sendMessage(mHandler.obtainMessage(HANDLER_APK_DOWNLOAD_PROGRESS, progress));
				if (numread <= 0) {
					// 下载完成通知安装
					(new File(tempPath)).renameTo(new File(apkFilePath));
					mHandler.sendEmptyMessage(HANDLER_APK_DOWNLOAD_FINISH);
					break;
				}
				fos.write(buf, 0, numread);
			} while (true);

			fos.close();
			is.close();
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}

	}

}
