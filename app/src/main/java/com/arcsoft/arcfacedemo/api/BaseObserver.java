package com.arcsoft.arcfacedemo.api;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.HttpException;

public abstract class BaseObserver<T> implements Observer<T> {
    private String TAG = "BaseObserver";

    private Context context;

    public BaseObserver(Context context) {
        this.context = context;
    }

    @Override
    public void onSubscribe(Disposable d) {

//        if (!NetworkUtil.isNetworkAvailable(context)) {
//
//            Toast.makeText(context, "当前网络不可用，请检查网络情况", Toast.LENGTH_SHORT).show();
//            // **一定要主动调用下面这一句**
//            onComplete();
//
//            return;
//        }
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onNext(T t) {
        if (t == null) {
            onError(HttpCode.ERROR_EMPTY_OBJ, getErrorMessage(HttpCode.ERROR_EMPTY_OBJ));
        } else {
            onSuccess(t);
        }
    }
    public abstract void onError(int errorCode, String message);
    public abstract void onSuccess(T t);
    public boolean isShowErrorToast() {
        return true;
    }
    @Override
    public void onError(Throwable e) {
        int errorCode = -1;
        String errMsg = "";

        ResponseBody body = null;
        if (e instanceof HttpException) {
            body = ((HttpException) e).response().errorBody();
        }
        if(body!=null){
            BufferedSource source = body.source();
            try {
                source.request(Long.MAX_VALUE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String respString = source.buffer().clone().readString(Charset.defaultCharset());
            JSONObject j = null;
            try {
                j = new JSONObject(respString);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (j != null) {
                errMsg = j.optString("message");
            }
        }

        if (e instanceof ErrorException) {
            ErrorException exception = (ErrorException) e;
            errorCode = exception.getErrorCode();
//            errMsg = exception.getMessage();
            handleErrorCode(errorCode);
            if (isShowErrorToast()) {
            }
        } else if (e instanceof NullPointerException) {
            errorCode = HttpCode.ERROR_EMPTY_OBJ;
            errMsg = getErrorMessage(HttpCode.ERROR_EMPTY_OBJ);
        } else if (e instanceof SocketTimeoutException) {
            errorCode = HttpCode.ERROR_TIMEOUT;
            errMsg = getErrorMessage(HttpCode.ERROR_TIMEOUT);
        } else if (e instanceof NetworkErrorException) {
            errorCode = HttpCode.ERROR_NETWORK;
            errMsg = getErrorMessage(HttpCode.ERROR_NETWORK);
        } else if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
//            errMsg = httpException != null ? httpException.getMessage() : getErrorMessage(HttpCode.ERROR_SERVER_EXCEPTION);
            int httpErrorCode = httpException != null ? httpException.code() : HttpCode.ERROR_UNKNOWN;
            Log.d(TAG, "Http request error:" + "message=" + errMsg + " :::: " + "httpErrorCode=" + httpErrorCode);
        } else {
            errMsg = e != null ? e.getMessage() : getErrorMessage(HttpCode.ERROR_UNKNOWN);
        }
        onError(errorCode, errMsg);
    }

    /**
     * 处理数据异常code
     * @param errorCode
     */
    private void handleErrorCode(int errorCode) {

    }
    private String getErrorMessage(int errorCode) {
        String message = HttpCode.ERRORS.get(errorCode);
        if (TextUtils.isEmpty(message)) {
            message = HttpCode.ERRORS.get(HttpCode.ERROR_UNKNOWN);
        }
        return message;
    }
    public interface Result<T>{
        void success(T t);
        void failure(int errorCode, String message);
    }
}
