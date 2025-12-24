package com.example.mybighomework.autoglm.callback;

public interface AutoGLMCallback {
    void onSuccess(String response);
    void onError(Exception e);
}
