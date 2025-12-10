package com.example.mybighomework.api;

import com.example.mybighomework.model.IcibaResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * 每日一句API服务接口
 * 使用金山词霸开放API
 */
public interface DailySentenceApiService {
    
    /**
     * 获取今日一句
     * @return 金山词霸API响应
     */
    @GET("dsapi/")
    Call<IcibaResponse> getDailySentence();
}

