package com.arcsoft.arcfacedemo.util;


import com.github.lazylibrary.util.Base64;

public class Base64Utils {

    //base64字符串转byte[]
    public static byte[] base64String2Byte(String base64Str){
        return Base64.decode(base64Str);
    }
    //byte[]转base64
    public static String byte2Base64String(byte[] b){
        return Base64.encodeToString(b,false);
    }

}