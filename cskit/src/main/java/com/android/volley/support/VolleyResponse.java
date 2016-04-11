package com.android.volley.support;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by song on 16/4/11.
 */
public class VolleyResponse {
    /*
    * status line
    */
    public int statusCode;
    public String statusMessage;
    public ProtocolVersion protocolVersion;
    /*
    * entity
    */
    public InputStream byteStream;
    public long contentLength;
    public String contentEncoding;
    public String contentType;
    /*
     *Response headers.
     */
    public Map<String, String> headers;

    public static VolleyResponse convertOKResponseToVolleyResponse(Response response) throws IOException{
        VolleyResponse volleyResponse = new VolleyResponse();
        volleyResponse.protocolVersion = parseProtocol(response.protocol());
        volleyResponse.statusCode = response.code();
        volleyResponse.statusMessage = response.message();

        ResponseBody body = response.body();
        volleyResponse.byteStream = body.byteStream();
        volleyResponse.contentLength = body.contentLength();
        volleyResponse.contentEncoding = response.header("Content-Encoding");
        if (body.contentType() != null) {
            volleyResponse.contentType = body.contentType().type();
        }

        volleyResponse.headers = new HashMap<>();
        Headers responseHeaders = response.headers();
        for (int i = 0, len = responseHeaders.size(); i < len; i++) {
            final String name = responseHeaders.name(i), value = responseHeaders.value(i);
            if (name != null) {
                volleyResponse.headers.put(name, value);
            }
        }
        return volleyResponse;
    }



    private static ProtocolVersion parseProtocol(final Protocol p) {
        switch (p) {
            case HTTP_1_0:
                return new ProtocolVersion("HTTP", 1, 0);
            case HTTP_1_1:
                return new ProtocolVersion("HTTP", 1, 1);
            case SPDY_3:
                return new ProtocolVersion("SPDY", 3, 1);
            case HTTP_2:
                return new ProtocolVersion("HTTP", 2, 0);
        }

        throw new IllegalAccessError("Unkwown protocol");
    }

    public static class ProtocolVersion {
        protected final int major;
        protected final int minor;
        protected final String protocol;

        public ProtocolVersion(String protocol, int major, int minor) {
            this.protocol = protocol;
            this.major = major;
            this.minor = minor;
        }

        public final String getProtocol() {
            return this.protocol;
        }

        public final int getMajor() {
            return this.major;
        }

        public final int getMinor() {
            return this.minor;
        }
    }
}
