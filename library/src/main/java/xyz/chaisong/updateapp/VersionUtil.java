package xyz.chaisong.updateapp;

import android.util.Log;
/**
 * Created by song on 15/5/31.
 */
public class VersionUtil {
    public static long getVersion(String versionName) {
        String[] versionBitSet = versionName.split("\\.");
        int major = 0, mid = 0, minor = 0, build = 0;
        int i = 0;
        for (String str : versionBitSet) {
            if (i > 3)
                break;
            try {
                switch (i) {
                    case 0:
                        major = Integer.valueOf(str);
                        break;
                    case 1:
                        mid = Integer.valueOf(str);
                        break;
                    case 2:
                        minor = Integer.valueOf(str);
                        break;
                    case 3:
                        build = Integer.valueOf(str);
                        break;
                }
            } catch (NumberFormatException e) {
                Log.e("VersionUtil:getVersion", "versionName:" + versionName + ", curString:" + str, e);
            }
            i++;
        }
        long version = MakeVersion(major, mid, minor, build);
        Log.v("VersionUtil:getVersion", "versionName:" + versionName + ", MakeVersion:" + version);
        return version;
    }

    public static long MakeVersion(int major, int mid, int minor, int build) {
        return (major << 24) + (mid << 16) + (minor << 8) + build;
    }
}
