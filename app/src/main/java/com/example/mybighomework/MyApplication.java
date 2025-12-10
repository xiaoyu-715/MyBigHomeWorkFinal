package com.example.mybighomework;

import android.app.Application;
import android.util.Log;

import com.example.mybighomework.di.ServiceLocatorYSJ;

/**
 * 应用程序入口类
 * 负责全局初始化工作
 */
public class MyApplication extends Application {
    
    private static final String TAG = "MyApplication";
    
    private static MyApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // 初始化服务定位器（依赖注入）
        ServiceLocatorYSJ.init(this);
        Log.d(TAG, "ServiceLocator initialized");
        
        // 其他初始化...
    }
    
    /**
     * 获取Application实例
     */
    public static MyApplication getInstance() {
        return instance;
    }
}
