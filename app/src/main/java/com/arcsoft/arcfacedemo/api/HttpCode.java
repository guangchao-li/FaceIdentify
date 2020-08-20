package com.arcsoft.arcfacedemo.api;

import android.util.SparseArray;

public class HttpCode {
    public static final int ERROR_UNKNOWN = -1;
    public static final int ERROR_SUCCESS = 0;
    public static final int ERROR_NETWORK = 1000;
    public static final int ERROR_TIMEOUT = 1001;
    public static final int ERROR_SERVER_EXCEPTION = 1002;
    public static final int ERROR_EMPTY_OBJ = 1011;
    public static final SparseArray<String> ERRORS = new SparseArray<>();

    static {
        ERRORS.append(ERROR_UNKNOWN, "unknow");
        ERRORS.append(ERROR_SUCCESS, "success");
        ERRORS.append(ERROR_NETWORK, "net error");
        ERRORS.append(ERROR_TIMEOUT, "time out");
        ERRORS.append(ERROR_SERVER_EXCEPTION, "server error");
        ERRORS.append(ERROR_EMPTY_OBJ, "no data");
    }
}
