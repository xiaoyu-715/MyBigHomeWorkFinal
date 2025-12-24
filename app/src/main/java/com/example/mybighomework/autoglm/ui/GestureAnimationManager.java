package com.example.mybighomework.autoglm.ui;

import android.content.Context;
import android.util.Log;

/**
 * 手势动画管理器
 * 提供统一的手势动画显示接口
 * 
 * 需求: 9.1-9.4
 */
public class GestureAnimationManager {
    
    private static final String TAG = "GestureAnimationManager";
    
    private static volatile GestureAnimationManager instance;
    private static final Object LOCK = new Object();
    
    private Context context;
    private GestureAnimationView animationView;
    private boolean isInitialized = false;
    
    private GestureAnimationManager() {}
    
    /**
     * 获取单例实例
     */
    public static GestureAnimationManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new GestureAnimationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化管理器
     * 
     * @param context 应用上下文
     */
    public void initialize(Context context) {
        if (isInitialized) return;
        
        this.context = context.getApplicationContext();
        this.animationView = new GestureAnimationView(this.context);
        this.isInitialized = true;
        
        Log.d(TAG, "GestureAnimationManager 初始化完成");
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * 显示点击动画
     * 需求: 9.1
     * 
     * @param x 点击X坐标
     * @param y 点击Y坐标
     */
    public void showTapAnimation(float x, float y) {
        if (!checkInitialized()) return;
        
        Log.d(TAG, "显示点击动画: (" + x + ", " + y + ")");
        animationView.showTapAnimation(x, y);
    }
    
    /**
     * 显示双击动画
     * 需求: 9.1 (扩展)
     * 
     * @param x 双击X坐标
     * @param y 双击Y坐标
     */
    public void showDoubleTapAnimation(float x, float y) {
        if (!checkInitialized()) return;
        
        Log.d(TAG, "显示双击动画: (" + x + ", " + y + ")");
        // 双击动画：两次快速点击
        animationView.showTapAnimation(x, y);
    }
    
    /**
     * 显示长按动画
     * 需求: 9.1 (扩展)
     * 
     * @param x 长按X坐标
     * @param y 长按Y坐标
     */
    public void showLongPressAnimation(float x, float y) {
        if (!checkInitialized()) return;
        
        Log.d(TAG, "显示长按动画: (" + x + ", " + y + ")");
        animationView.showLongPressAnimation(x, y);
    }
    
    /**
     * 显示滑动动画
     * 需求: 9.2
     * 
     * @param startX 起点X坐标
     * @param startY 起点Y坐标
     * @param endX 终点X坐标
     * @param endY 终点Y坐标
     */
    public void showSwipeAnimation(float startX, float startY, float endX, float endY) {
        if (!checkInitialized()) return;
        
        Log.d(TAG, "显示滑动动画: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + ")");
        animationView.showSwipeAnimation(startX, startY, endX, endY);
    }
    
    /**
     * 检查是否已初始化
     */
    private boolean checkInitialized() {
        if (!isInitialized) {
            Log.w(TAG, "GestureAnimationManager 未初始化");
            return false;
        }
        return true;
    }
    
    /**
     * 释放资源
     */
    public void release() {
        if (animationView != null) {
            animationView.release();
            animationView = null;
        }
        isInitialized = false;
        instance = null;
        Log.d(TAG, "GestureAnimationManager 资源已释放");
    }
}
