package com.hitqz.robot.watchtower.net;


import com.hitqz.robot.commonlib.net.HttpCommonInterceptor;
import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {

    private static final String SEVER_URL = "http://192.168.3.8:8080";

    private static final int DEFAULT_TIME_OUT = 5;//超时时间 5s
    private static final int DEFAULT_READ_TIME_OUT = 10;

    private Retrofit mRetrofit;
    private static RetrofitManager mManager;

    private RetrofitManager() {
        if (mRetrofit == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//连接超时时间
            builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//写操作 超时时间
            builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//读操作超时时间
            // 添加公共参数拦截器
            HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
                    .addHeaderParams("Content-Type", "application/json")
                    .build();
            builder.addInterceptor(commonInterceptor);

            // if (BuildConfig.DEBUG) {
            // Log信息拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLogger());
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //设置 Debug Log 模式
            builder.addInterceptor(loggingInterceptor);
            //}

            // 创建Retrofit
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(SEVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(builder.build())
                    .build();
        }

    }


    /**
     * 获取RetrofitManager
     *
     * @return RetrofitManager
     */
    public static RetrofitManager getInstance() {
        if (mManager == null) {
            synchronized (RetrofitManager.class) {
                if (mManager == null) {
                    mManager = new RetrofitManager();
                }
            }
        }
        return mManager;
    }

    /**
     * 获取对应的Service
     *
     * @param service Service 的 class
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

    private class HttpLogger implements HttpLoggingInterceptor.Logger {

        @Override
        public void log(String message) {
            // 使用Logger打印
            Logger.t("OkHttp").d(message);
        }
    }
}
