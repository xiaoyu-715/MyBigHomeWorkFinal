package com.example.mybighomework.autoglm.service;

import com.example.mybighomework.autoglm.model.ChatRequest;
import com.example.mybighomework.autoglm.model.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AutoGLMService {
    @POST("chat/completions")
    Call<ChatResponse> chat(@Body ChatRequest request);
}
