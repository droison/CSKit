
package xyz.chaisong.updateapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CheckVersionService extends Thread {

    private UpdateHandler mHandler;

    private Context context;

    private String url;

    public CheckVersionService(Context context, UpdateHandler mHandler, String url) {
        this.mHandler = mHandler;
        this.context = context;
        this.url = url;
    }

    /**
     * 接口样式
     * {
     * "changelog" : "",
     * "installUrl" : "http://fir.im/api/v2/app/install/548b151eedc03d4006000147",
     * "name" : "好奇心日报",
     * "update_url" : "http://fir.im/qdailyAD",
     * "version" : "20",
     * "versionShort" : "1.9.0"
     * }
     */
    @Override
    public void run() {
        HttpClient httpClient = new DefaultHttpClient();
        //请求超时
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        //读取超时
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
        boolean isNeedUpdate = false;
        CheckVersionBase cvb = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String firResponse = EntityUtils.toString(httpEntity);
                JSONObject versionJsonObj = new JSONObject(firResponse);
                //FIR上当前的versionCode
                int firVersionCode = Integer.parseInt(versionJsonObj.getString("version"));
                String firVersionName = versionJsonObj.getString("versionShort");
                Log.i("CheckVersionService", "firResponse：" + firResponse);

                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                        PackageManager.GET_ACTIVITIES);
                if (pi != null) {
                    int currentVersionCode = pi.versionCode;
                    String currentVersionName = pi.versionName;
                    if (firVersionCode > currentVersionCode || VersionUtil.getVersion(firVersionName) > VersionUtil.getVersion(currentVersionName)) {
                        cvb = new CheckVersionBase();
                        cvb.setTitle(versionJsonObj.getString("name"));
                        cvb.setUrl(versionJsonObj.getString("installUrl"));
                        cvb.setVersion(firVersionCode);
                        cvb.setVersionName(firVersionName);
                        cvb.setExcerpt(versionJsonObj.getString("changelog"));
                        isNeedUpdate = true;
                    } else {
                        //不需要更新,当前版本高于FIR上的app版本.
                        Log.i("CheckVersionService", " no need update");
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isNeedUpdate) {
            mHandler.sendMessage(mHandler.obtainMessage(ApkDownloadService.HANDLER_VERSION_UPDATE, cvb));
        } else {
            mHandler.sendEmptyMessage(ApkDownloadService.HANDLER_HTTPSTATUS_ERROR);
        }
    }

}
