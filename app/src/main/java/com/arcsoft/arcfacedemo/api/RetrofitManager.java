package com.arcsoft.arcfacedemo.api;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private static RetrofitManager sInstance;
    private Retrofit mRetrofit;
    private static Context mContext;
    private String baseUrl = "http://124.133.5.194:10002/";
    public static RetrofitManager getInstance() {
        if (null == sInstance) {
            synchronized (RetrofitManager.class) {
                if (null == sInstance) {
                    sInstance = new RetrofitManager();
                }
            }
        }
        return sInstance;
    }
    public Retrofit getRetrofit() {
        if(mRetrofit == null) {
            init();
        }
        return mRetrofit;
    }
    public static void init(Context context) {
        mContext = context;
    }
    public void init() {
        if(mRetrofit == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.addInterceptor(getHttpLoggingInterceptor());
            builder.addInterceptor(HttpRequestLogin());
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Request.Builder requestBuilder = request.newBuilder();
                    requestBuilder.addHeader("Content-Type", "application/json");
//                    setAuthHeader(requestBuilder);
                    return chain.proceed(requestBuilder.build());
                }
            })
                    .connectTimeout(50000, TimeUnit.MILLISECONDS)
                    .readTimeout(50000, TimeUnit.MILLISECONDS)
                    .writeTimeout(50000, TimeUnit.MILLISECONDS);
            OkHttpClient okHttpClient = builder
                    .build();
            mRetrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(okHttpClient)
                    .baseUrl(baseUrl)
                    .build();
        }
    }

    //提供Log日志插值器
    public static HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }
    //基本返回值拦截
    public static Interceptor HttpRequestLogin() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 原始请求
                Request request = chain.request();
                Response response = chain.proceed(request);
                ResponseBody responseBody = response.body();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE);
                String respString = source.buffer().clone().readString(Charset.defaultCharset());
//                Log.d("RetrofitManager", "--->返回报文，respString = " + respString);
                // TODO 这里判断是否是登录超时的情况
                JSONObject j = null;
                try {
                    j = new JSONObject(respString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                if (j != null && j.optString("message").equals("The session is invalid")) {
//                    MMKV.mmkvWithID(Common.MMKVUSERLEVELFILE).clear();
//                    Intent intent = new Intent(mContext, RoleSelectActivity.class);
//                    mContext.startActivity(intent);
//                }
                return response;
            }
        };
    }
    private void setAuthHeader(Request.Builder requestBuilder)
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateStr = df.format(cal.getTime());
        String dateParamStr = "x-date: " + dateStr;
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec(("http://192.168.1.108:8122/").getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] b = dateParamStr.getBytes();
            byte[] bytes = mac.doFinal(b);

//            digest = Base64.encodeBytes(bytes);

        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        }

        HashMap<String, String> map = new HashMap<String, String>();
//        String authStr = "username=\"" + BuildConfig.API_CONSUMER
//                +"\",algorithm=\"hmac-sha256\",headers=\"x-date\",signature=\""+ digest +"\"";

//        requestBuilder.addHeader("X-Date", dateStr);
//        requestBuilder.addHeader("Authorization", "hmac "+authStr);
    }
}
