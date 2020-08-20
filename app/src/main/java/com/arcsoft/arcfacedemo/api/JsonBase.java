package com.arcsoft.arcfacedemo.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class JsonBase implements Serializable {
    private static final long serialVersionUID = -6182189632617616248L;
    @SerializedName("msg")
    private String msg;
    private int code = -1;

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }



}
