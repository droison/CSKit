package com.android.volley.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

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

    public static VolleyResponse convertHttpURLConnectionToVolleyResponse(HttpURLConnection connection) throws IOException{
        VolleyResponse volleyResponse = new VolleyResponse();
        volleyResponse.protocolVersion = new VolleyResponse.ProtocolVersion("HTTP", 1, 1);

        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }

        volleyResponse.statusCode = responseCode;
        volleyResponse.statusMessage = connection.getResponseMessage();

        if (hasResponseBody(connection.getRequestMethod(), responseCode)) {
            InputStream inputStream;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException ioe) {
                inputStream = connection.getErrorStream();
            }
            volleyResponse.byteStream = inputStream;
            volleyResponse.contentLength = connection.getContentLength();
            volleyResponse.contentEncoding = connection.getContentEncoding();
            volleyResponse.contentType = connection.getContentType();
        }

        volleyResponse.headers = new HashMap<>();
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                volleyResponse.headers.put(header.getKey(), header.getValue().get(0));
            }
        }

        return volleyResponse;
    }

    /**
     * Checks if a response message contains a body.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3">RFC 7230 section 3.3</a>
     * @param requestMethod request method
     * @param responseCode response status code
     * @return whether the response has a body
     */
    private static boolean hasResponseBody(String requestMethod, int responseCode) {
        return (requestMethod == null || !requestMethod.toLowerCase().equals("head"))
                && !(HttpStatus.SC_CONTINUE <= responseCode && responseCode < HttpStatus.SC_OK)
                && responseCode != HttpStatus.SC_NO_CONTENT
                && responseCode != HttpStatus.SC_NOT_MODIFIED;
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
