package com.example.mybighomework.network;

import com.example.mybighomework.api.DailySentenceApiService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit网络客户端管理类
 * 单例模式，统一管理所有API服务
 */
public class RetrofitClient {
    
    // 金山词霸API基础URL
    private static final String ICIBA_BASE_URL = "http://open.iciba.com/";
    
    // 单例实例
    private static volatile RetrofitClient instance;
    
    // Retrofit实例
    private final Retrofit retrofit;
    
    // API服务
    private final DailySentenceApiService dailySentenceApiService;
    
    /**
     * 私有构造函数
     */
    private RetrofitClient() {
        // 创建日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // 创建OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        
        // 创建Retrofit实例
        retrofit = new Retrofit.Builder()
                .baseUrl(ICIBA_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // 创建API服务
        dailySentenceApiService = retrofit.create(DailySentenceApiService.class);
    }
    
    /**
     * 获取RetrofitClient单例
     * @return RetrofitClient实例
     */
    public static RetrofitClient getInstance() {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) {
                    instance = new RetrofitClient();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取每日一句API服务
     * @return DailySentenceApiService
     */
    public DailySentenceApiService getDailySentenceApiService() {
        return dailySentenceApiService;
    }
    
    /**
     * 获取Retrofit实例（用于扩展）
     * @return Retrofit
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }
}

