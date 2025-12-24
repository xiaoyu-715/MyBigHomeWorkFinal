package com.example.mybighomework.autoglm.automation;

public interface AutomationCallback {
    void onStart();
    void onProgress(int current, int total, String message);
    void onComplete(AutomationResult result);
    void onError(Exception e);
}
