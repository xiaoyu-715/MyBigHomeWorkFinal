package com.example.mybighomework.utils;

/**
 * 通用加载状态封装类
 * 用于表示数据加载的三种状态：加载中、成功、失败
 * 
 * 使用示例:
 * LoadingStateYSJ<List<User>> state = LoadingStateYSJ.loading();
 * state = LoadingStateYSJ.success(userList);
 * state = LoadingStateYSJ.error("网络错误");
 */
public class LoadingStateYSJ<T> {
    
    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }
    
    private final Status status;
    private final T data;
    private final String errorMessage;
    
    private LoadingStateYSJ(Status status, T data, String errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 创建加载中状态
     */
    public static <T> LoadingStateYSJ<T> loading() {
        return new LoadingStateYSJ<>(Status.LOADING, null, null);
    }
    
    /**
     * 创建成功状态
     */
    public static <T> LoadingStateYSJ<T> success(T data) {
        return new LoadingStateYSJ<>(Status.SUCCESS, data, null);
    }
    
    /**
     * 创建错误状态
     */
    public static <T> LoadingStateYSJ<T> error(String message) {
        return new LoadingStateYSJ<>(Status.ERROR, null, message);
    }
    
    /**
     * 创建带数据的错误状态（保留旧数据）
     */
    public static <T> LoadingStateYSJ<T> error(String message, T data) {
        return new LoadingStateYSJ<>(Status.ERROR, data, message);
    }
    
    // Getters
    public Status getStatus() {
        return status;
    }
    
    public T getData() {
        return data;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isLoading() {
        return status == Status.LOADING;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isError() {
        return status == Status.ERROR;
    }
    
    public boolean hasData() {
        return data != null;
    }
}
