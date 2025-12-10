package com.example.mybighomework.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;

/**
 * UI辅助工具类
 * 提供通用的UI操作方法
 */
public class UIHelperYSJ {
    
    /**
     * 显示空数据状态
     * @param emptyLayout 空数据布局容器
     * @param contentView 内容视图（会被隐藏）
     * @param title 标题文字
     * @param message 描述文字
     */
    public static void showEmptyState(View emptyLayout, View contentView, 
                                       String title, String message) {
        if (emptyLayout == null) return;
        
        emptyLayout.setVisibility(View.VISIBLE);
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
        
        TextView tvTitle = emptyLayout.findViewById(R.id.tv_empty_title);
        TextView tvMessage = emptyLayout.findViewById(R.id.tv_empty_message);
        
        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }
        if (tvMessage != null && message != null) {
            tvMessage.setText(message);
        }
    }
    
    /**
     * 隐藏空数据状态
     */
    public static void hideEmptyState(View emptyLayout, View contentView) {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(View.GONE);
        }
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 显示加载状态
     */
    public static void showLoading(View loadingLayout, String message) {
        if (loadingLayout == null) return;
        
        loadingLayout.setVisibility(View.VISIBLE);
        
        TextView tvMessage = loadingLayout.findViewById(R.id.tv_loading_message);
        if (tvMessage != null && message != null) {
            tvMessage.setText(message);
        }
    }
    
    /**
     * 隐藏加载状态
     */
    public static void hideLoading(View loadingLayout) {
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.GONE);
        }
    }
    
    /**
     * 为View应用淡入动画
     */
    public static void fadeIn(View view, long duration) {
        if (view == null) return;
        
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start();
    }
    
    /**
     * 为View应用淡出动画
     */
    public static void fadeOut(View view, long duration) {
        if (view == null) return;
        
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction(() -> view.setVisibility(View.GONE))
            .start();
    }
    
    /**
     * 为View应用缩放动画（点击效果）
     */
    public static void scaleClick(View view) {
        if (view == null) return;
        
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> 
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            )
            .start();
    }
    
    /**
     * 为RecyclerView设置列表动画
     */
    public static void setRecyclerViewAnimation(Context context, RecyclerView recyclerView) {
        if (recyclerView == null || context == null) return;
        
        try {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            animation.setDuration(300);
        } catch (Exception e) {
            // 忽略动画加载错误
        }
    }
    
    /**
     * 根据数据状态自动切换空数据/内容显示
     */
    public static void updateListState(View emptyLayout, View contentView, 
                                        int itemCount, String emptyTitle, String emptyMessage) {
        if (itemCount == 0) {
            showEmptyState(emptyLayout, contentView, emptyTitle, emptyMessage);
        } else {
            hideEmptyState(emptyLayout, contentView);
        }
    }
    
    /**
     * 设置沉浸式状态栏
     */
    public static void setImmersiveStatusBar(Activity activity) {
        if (activity == null) return;
        
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
}
