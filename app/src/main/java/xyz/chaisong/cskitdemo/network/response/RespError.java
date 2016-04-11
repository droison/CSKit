package xyz.chaisong.cskitdemo.network.response;

import com.android.volley.VolleyError;

/**
 * Created by song on 15/11/30.
 */
public class RespError extends VolleyError{
    public interface ErrorType {
        public static final int paramsError = 4001;
        public static final int parseError = 4002;
        public static final int responseNot200Error = 4003;
        public static final int responseNeedLogin = 10004;
    }

    private int qdResponseErrorType;

    public RespError(Throwable cause) {
        super(cause);
    }

    public RespError(String msg) {
        super(msg);
    }


    public int getQdResponseErrorType() {
        return qdResponseErrorType;
    }

    public static RespError paramsError(String cause) {
        RespError error = new RespError(new Throwable(cause == null? "请求参数错误": cause));
        error.qdResponseErrorType = ErrorType.paramsError;
        return error;
    }

    public static RespError convertError (VolleyError error) {
        if (error instanceof RespError) {
            return (RespError)error;
        }
        return new RespError(error.getCause());
    }

    public static RespError responseNot200Error (String msg) {
        RespError respError = new RespError(msg);
        respError.qdResponseErrorType = ErrorType.responseNot200Error;
        return respError;
    }

    public static RespError responseNeedLogin (String msg) {
        RespError respError = new RespError(msg);
        respError.qdResponseErrorType = ErrorType.responseNeedLogin;
        return respError;
    }
}