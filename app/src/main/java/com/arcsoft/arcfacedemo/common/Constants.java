package com.arcsoft.arcfacedemo.common;

/**
 * 一些常量设置
 * <p>
 * 双目偏移可在 [参数设置] -> [识别界面适配] 界面中进行设置
 */
public class Constants {

    /**
     * 方式一： 填好APP_ID等参数，进入激活界面激活
     */
    public static final String APP_ID = "AbVJr8QtzruZvhTPHzFT4DNyoKQBzMebCYnRNtR7m8jQ";
    public static final String SDK_KEY = "aY43ojgUvG5a2gKdpgdRWZXgoqiem1cRCyuC3ipNAcw";
//    public static final String APP_ID = "Bd9n7mCgG5b7PPmvWjh1h7Gdb9HBhFiQqpRmFH6ZqfU2";
//    public static final String SDK_KEY = "9LamVon7KCnJKtHhPxt5kwsWn2Gdhq4KDWtqn6jJxzVy";
//    public static final String APP_ID = "AbVJr8QtzruZvhTPHzFT4DNrdv927yKYXEisXt9YTWi6";
//    public static final String SDK_KEY = "48eLJewBnNHgJ7t8NZB83rrf1PKxFqJHQFZvTJ11CZau";
//    public static final String ACTIVE_KEY = "85T1-113L-N11F-MGYX";//新
    public static final String ACTIVE_KEY = "85T1-1149-W112-6XK1";//旧
    /**
     * 方式二： 在激活界面读取本地配置文件进行激活
     *
     * 配置文件名称，格式如下：
     * APP_ID:XXXXXXXXXXXXX
     * SDK_KEY:XXXXXXXXXXXXXXX
     * ACTIVE_KEY:XXXX-XXXX-XXXX-XXXX
     */
    public static final String ACTIVE_CONFIG_FILE_NAME = "activeConfig.txt";


    /**
     * 注册图所在路径
     */
    public static final String DEFAULT_REGISTER_FACES_DIR = "sdcard/arcfacedemo/register";
}
