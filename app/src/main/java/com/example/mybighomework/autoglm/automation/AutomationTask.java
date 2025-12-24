package com.example.mybighomework.autoglm.automation;

public interface AutomationTask {
    void execute(AutomationCallback callback);
    void cancel();
    String getTaskType();
    String getTaskDescription();
}
