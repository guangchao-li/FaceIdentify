package com.arcsoft.arcfacedemo.api;

public class ErrorException extends Exception {

    private int mErrorCode;
    private String mMessage;

    public ErrorException(int errorCode, String message) {
        super();
        mErrorCode = errorCode;
        mMessage = message;
    }
    public int getErrorCode() {
        return mErrorCode;
    }
    public void setErrorCode(int mErrorCode) {
        this.mErrorCode = mErrorCode;
    }
    public String getMessage() {
        return mMessage;
    }
    public void setMessage(String message) {
        this.mMessage = message;
    }
}
