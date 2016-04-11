package com.android.volley.filedownload;

import android.text.TextUtils;

import com.android.volley.support.HTTP;
import com.android.volley.support.VolleyResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by song on 16/3/30.
 */
public class HttpUtils {
    private HttpUtils() {
        /* cannot be instantiated */
    }

    /**
     * Returns the charset specified in the Content-Type of this header.
     */
    public static String getCharset(VolleyResponse response) {
        String contentType = response.headers.get(HTTP.CONTENT_TYPE);
        if (!TextUtils.isEmpty(contentType)) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2 && pair[0].equals("charset")) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    public static String getHeader(VolleyResponse response, String key) {
        return response.headers.get(key);
    }

    public static boolean isSupportRange(VolleyResponse response) {
        if (TextUtils.equals(getHeader(response, "Accept-Ranges"), "bytes")) {
            return true;
        }
        String value = getHeader(response, "Content-Range");
        return value != null && value.startsWith("bytes");
    }

    public static boolean isGzipContent(VolleyResponse response) {
        return TextUtils.equals(getHeader(response, "Content-Encoding"), "gzip");
    }

    public static String md5(String key) {
        if (key == null || key.equals(""))
            return "";
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
