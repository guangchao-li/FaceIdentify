package com.arcsoft.arcfacedemo.api;

import com.google.gson.annotations.SerializedName;

public class JsonObjBase<T> extends JsonBase{

    @SerializedName("data")
    T data;
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }


}
