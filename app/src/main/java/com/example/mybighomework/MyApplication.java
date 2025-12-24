package com.example.mybighomework;

import android.app.Application;
import android.util.Log;

import com.example.mybighomework.di.ServiceLocatorYSJ;
import com.example.mybighomework.utils.DictionaryDataImporter;

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
        
        // 初始化词典数据（后台执行）
        initDictionaryData();
        
        // 其他初始化...
    }
    
    /**
     * 初始化词典数据
     * 首次启动时从assets导入数据到数据库
     */
    private void initDictionaryData() {
        DictionaryDataImporter importer = new DictionaryDataImporter(this);
        
        if (!importer.isDataImported()) {
            Log.d(TAG, "开始导入词典数据...");
            importer.importDataAsync(new DictionaryDataImporter.ImportProgressListener() {
                @Override
                public void onProgress(int current, int total, String message) {
                    Log.d(TAG, "词典数据导入进度: " + message);
                }
                
                @Override
                public void onComplete(boolean success, String message) {
                    if (success) {
                        Log.d(TAG, "词典数据导入成功: " + message);
                    } else {
                        Log.e(TAG, "词典数据导入失败: " + message);
                    }
                }
            });
        } else {
            Log.d(TAG, "词典数据已导入，跳过初始化");
        }
    }
    
    /**
     * 获取Application实例
     */
    public static MyApplication getInstance() {
        return instance;
    }
}
