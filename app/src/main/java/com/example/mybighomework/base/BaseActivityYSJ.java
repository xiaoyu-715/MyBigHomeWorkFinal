package com.example.mybighomework.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * BaseActivity - 所有Activity的基类
 * 提供ViewBinding支持和通用功能
 * 
 * 使用方式:
 * public class MyActivity extends BaseActivityYSJ<ActivityMyBinding> {
 *     @Override
 *     protected ActivityMyBinding createBinding(LayoutInflater inflater) {
 *         return ActivityMyBinding.inflate(inflater);
 *     }
 * }
 */
public abstract class BaseActivityYSJ<VB extends ViewBinding> extends AppCompatActivity {
    
    protected VB binding;
    
    /**
     * 子类实现此方法创建ViewBinding
     */
    protected abstract VB createBinding(LayoutInflater inflater);
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = createBinding(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initView();
        initData();
        initListener();
    }
    
    /**
     * 初始化视图 - 子类可重写
     */
    protected void initView() {}
    
    /**
     * 初始化数据 - 子类可重写
     */
    protected void initData() {}
    
    /**
     * 初始化监听器 - 子类可重写
     */
    protected void initListener() {}
    
    /**
     * 显示短Toast
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示长Toast
     */
    protected void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 显示/隐藏加载状态
     * 子类可重写实现自定义加载UI
     */
    protected void showLoading(boolean show) {
        // 默认实现为空，子类可重写
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
