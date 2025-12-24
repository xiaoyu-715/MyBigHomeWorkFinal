package com.example.mybighomework.autoglm.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mybighomework.R;

/**
 * 悬浮窗管理器
 * 负责显示AI自动化任务的执行状态
 * 
 * 需求: 4.1-4.8
 */
public class FloatingWindowManager {
    
    private static final String TAG = "FloatingWindowManager";
    
    private final Context context;
    private final WindowManager windowManager;
    private final Handler mainHandler;
    
    // 悬浮窗视图
    private View floatingView;
    private WindowManager.LayoutParams layoutParams;
    
    // UI组件
    private ProgressBar progressIndicator;
    private TextView tvStatusLabel;
    private TextView tvStatusDetail;
    private TextView tvStepProgress;
    private Button btnStop;
    private Button btnReturn;
    private View dragHandle;
    
    // 状态
    private boolean isShowing = false;
    private boolean isTaskRunning = false;
    
    // 拖动相关
    private float lastTouchX;
    private float lastTouchY;
    private int initialX;
    private int initialY;
    
    // 回调
    private OnStopClickListener stopClickListener;
    private OnReturnClickListener returnClickListener;
    
    /**
     * 停止按钮点击回调
     */
    public interface OnStopClickListener {
        void onStopClick();
    }
    
    /**
     * 返回应用按钮点击回调
     */
    public interface OnReturnClickListener {
        void onReturnClick();
    }
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public FloatingWindowManager(Context context) {
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    
    /**
     * 检查悬浮窗权限
     * @return 是否有悬浮窗权限
     */
    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
    
    /**
     * 请求悬浮窗权限
     * @param context Activity上下文
     */
    public static void requestOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
    
    /**
     * 显示悬浮窗
     * @param listener 停止按钮点击回调
     */
    public void show(OnStopClickListener listener) {
        show(listener, null);
    }
    
    /**
     * 显示悬浮窗
     * @param stopListener 停止按钮点击回调
     * @param returnListener 返回应用按钮点击回调
     */
    public void show(OnStopClickListener stopListener, OnReturnClickListener returnListener) {
        if (isShowing) {
            Log.w(TAG, "悬浮窗已经显示");
            return;
        }
        
        if (!hasOverlayPermission(context)) {
            Log.e(TAG, "没有悬浮窗权限");
            return;
        }
        
        this.stopClickListener = stopListener;
        this.returnClickListener = returnListener;
        
        mainHandler.post(() -> {
            try {
                createFloatingView();
                setupLayoutParams();
                setupDragListener();
                setupButtonListeners();
                
                windowManager.addView(floatingView, layoutParams);
                isShowing = true;
                isTaskRunning = true;
                
                // 初始状态
                updateButtonVisibility();
                
                Log.d(TAG, "悬浮窗显示成功");
            } catch (Exception e) {
                Log.e(TAG, "显示悬浮窗失败", e);
            }
        });
    }
    
    /**
     * 隐藏悬浮窗
     */
    public void hide() {
        if (!isShowing) {
            return;
        }
        
        mainHandler.post(() -> {
            try {
                if (floatingView != null && floatingView.isAttachedToWindow()) {
                    windowManager.removeView(floatingView);
                }
                floatingView = null;
                isShowing = false;
                isTaskRunning = false;
                Log.d(TAG, "悬浮窗隐藏成功");
            } catch (Exception e) {
                Log.e(TAG, "隐藏悬浮窗失败", e);
            }
        });
    }
    
    /**
     * 更新状态文本
     * @param status 状态描述
     */
    public void updateStatus(String status) {
        mainHandler.post(() -> {
            if (tvStatusDetail != null) {
                tvStatusDetail.setText(status);
            }
        });
    }
    
    /**
     * 更新状态标签
     * @param label 状态标签（如"思考中..."、"执行中..."）
     */
    public void updateStatusLabel(String label) {
        mainHandler.post(() -> {
            if (tvStatusLabel != null) {
                tvStatusLabel.setText(label);
            }
        });
    }
    
    /**
     * 更新步数进度
     * @param current 当前步数
     * @param total 总步数
     */
    public void updateStepProgress(int current, int total) {
        mainHandler.post(() -> {
            if (tvStepProgress != null) {
                tvStepProgress.setText(String.format("步骤: %d/%d", current, total));
            }
        });
    }

    
    /**
     * 设置任务运行状态
     * 控制停止按钮和返回按钮的显示
     * @param running 是否正在运行
     */
    public void setTaskRunning(boolean running) {
        this.isTaskRunning = running;
        mainHandler.post(this::updateButtonVisibility);
    }
    
    /**
     * 设置悬浮窗可见性
     * 用于截屏时临时隐藏悬浮窗
     * @param visible 是否可见
     */
    public void setVisibility(boolean visible) {
        mainHandler.post(() -> {
            if (floatingView != null) {
                floatingView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }
    
    /**
     * 检查悬浮窗是否正在显示
     * @return 是否显示中
     */
    public boolean isShowing() {
        return isShowing;
    }
    
    /**
     * 检查任务是否正在运行
     * @return 是否运行中
     */
    public boolean isTaskRunning() {
        return isTaskRunning;
    }
    
    /**
     * 显示进度指示器
     * @param show 是否显示
     */
    public void showProgressIndicator(boolean show) {
        mainHandler.post(() -> {
            if (progressIndicator != null) {
                progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
    
    /**
     * 设置完成状态
     * @param message 完成消息
     */
    public void setCompleted(String message) {
        mainHandler.post(() -> {
            isTaskRunning = false;
            updateButtonVisibility();
            
            if (tvStatusLabel != null) {
                tvStatusLabel.setText("已完成");
            }
            if (tvStatusDetail != null) {
                tvStatusDetail.setText(message);
            }
            if (progressIndicator != null) {
                progressIndicator.setVisibility(View.GONE);
            }
        });
    }
    
    /**
     * 设置错误状态
     * @param error 错误消息
     */
    public void setError(String error) {
        mainHandler.post(() -> {
            isTaskRunning = false;
            updateButtonVisibility();
            
            if (tvStatusLabel != null) {
                tvStatusLabel.setText("出错了");
                tvStatusLabel.setTextColor(context.getResources().getColor(R.color.error_red));
            }
            if (tvStatusDetail != null) {
                tvStatusDetail.setText(error);
            }
            if (progressIndicator != null) {
                progressIndicator.setVisibility(View.GONE);
            }
        });
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 创建悬浮窗视图
     */
    private void createFloatingView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        floatingView = inflater.inflate(R.layout.layout_floating_window, null);
        
        // 获取UI组件引用
        progressIndicator = floatingView.findViewById(R.id.progress_indicator);
        tvStatusLabel = floatingView.findViewById(R.id.tv_status_label);
        tvStatusDetail = floatingView.findViewById(R.id.tv_status_detail);
        tvStepProgress = floatingView.findViewById(R.id.tv_step_progress);
        btnStop = floatingView.findViewById(R.id.btn_stop);
        btnReturn = floatingView.findViewById(R.id.btn_return);
        dragHandle = floatingView.findViewById(R.id.drag_handle);
    }
    
    /**
     * 设置窗口布局参数
     */
    private void setupLayoutParams() {
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        
        // 初始位置：屏幕右上角
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = 50;
        layoutParams.y = 200;
    }

    
    /**
     * 设置拖动监听器
     */
    private void setupDragListener() {
        View container = floatingView.findViewById(R.id.floating_container);
        if (container == null) {
            container = floatingView;
        }
        
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录初始位置
                        lastTouchX = event.getRawX();
                        lastTouchY = event.getRawY();
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        // 计算移动距离
                        float deltaX = event.getRawX() - lastTouchX;
                        float deltaY = event.getRawY() - lastTouchY;
                        
                        // 更新位置
                        layoutParams.x = initialX + (int) deltaX;
                        layoutParams.y = initialY + (int) deltaY;
                        
                        // 更新视图位置
                        if (floatingView != null && floatingView.isAttachedToWindow()) {
                            windowManager.updateViewLayout(floatingView, layoutParams);
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        // 检查是否是点击（移动距离很小）
                        float moveX = Math.abs(event.getRawX() - lastTouchX);
                        float moveY = Math.abs(event.getRawY() - lastTouchY);
                        if (moveX < 10 && moveY < 10) {
                            // 这是一个点击，让子视图处理
                            return false;
                        }
                        return true;
                }
                return false;
            }
        });
    }
    
    /**
     * 设置按钮点击监听器
     */
    private void setupButtonListeners() {
        // 停止按钮
        if (btnStop != null) {
            btnStop.setOnClickListener(v -> {
                if (stopClickListener != null) {
                    stopClickListener.onStopClick();
                }
            });
        }
        
        // 返回应用按钮
        if (btnReturn != null) {
            btnReturn.setOnClickListener(v -> {
                if (returnClickListener != null) {
                    returnClickListener.onReturnClick();
                }
                // 隐藏悬浮窗
                hide();
            });
        }
    }
    
    /**
     * 更新按钮可见性
     */
    private void updateButtonVisibility() {
        if (btnStop != null && btnReturn != null) {
            if (isTaskRunning) {
                btnStop.setVisibility(View.VISIBLE);
                btnReturn.setVisibility(View.GONE);
            } else {
                btnStop.setVisibility(View.GONE);
                btnReturn.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * 重置状态
     */
    public void reset() {
        mainHandler.post(() -> {
            isTaskRunning = true;
            updateButtonVisibility();
            
            if (tvStatusLabel != null) {
                tvStatusLabel.setText("思考中...");
                tvStatusLabel.setTextColor(context.getResources().getColor(R.color.primary));
            }
            if (tvStatusDetail != null) {
                tvStatusDetail.setText("正在分析屏幕内容...");
            }
            if (tvStepProgress != null) {
                tvStepProgress.setText("步骤: 0/20");
            }
            if (progressIndicator != null) {
                progressIndicator.setVisibility(View.VISIBLE);
            }
        });
    }
}
