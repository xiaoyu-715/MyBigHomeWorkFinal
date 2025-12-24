package com.example.mybighomework.autoglm.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * 手势动画视图
 * 用于显示点击和滑动的视觉反馈
 * 
 * 需求: 9.1-9.4
 */
public class GestureAnimationView extends View {
    
    private static final String TAG = "GestureAnimationView";
    
    // 动画类型
    public enum AnimationType {
        TAP,        // 点击
        LONG_PRESS, // 长按
        SWIPE       // 滑动
    }
    
    // 画笔
    private Paint circlePaint;
    private Paint pathPaint;
    private Paint dotPaint;
    
    // 动画参数
    private float tapX, tapY;
    private float tapRadius = 0;
    private float tapAlpha = 1.0f;
    
    // 滑动路径
    private Path swipePath;
    private PathMeasure pathMeasure;
    private float swipeProgress = 0;
    private float[] swipePoint = new float[2];
    
    // 颜色 - 半透明黄色
    private static final int GESTURE_COLOR = Color.argb(180, 255, 193, 7); // 半透明黄色
    private static final int GESTURE_COLOR_LIGHT = Color.argb(100, 255, 193, 7);
    
    // 动画时长
    private static final long TAP_ANIMATION_DURATION = 400;
    private static final long LONG_PRESS_ANIMATION_DURATION = 800;
    private static final long SWIPE_ANIMATION_DURATION = 600;
    
    // 窗口管理
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private boolean isAttached = false;
    
    // Handler
    private Handler mainHandler;
    
    public GestureAnimationView(Context context) {
        super(context);
        init(context);
    }
    
    private void init(Context context) {
        mainHandler = new Handler(Looper.getMainLooper());
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        // 初始化点击圆形画笔
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(GESTURE_COLOR);
        circlePaint.setStyle(Paint.Style.FILL);
        
        // 初始化路径画笔
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(GESTURE_COLOR);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(8);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        
        // 初始化滑动点画笔
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(GESTURE_COLOR);
        dotPaint.setStyle(Paint.Style.FILL);
        
        // 设置窗口参数
        setupLayoutParams();
    }
    
    private void setupLayoutParams() {
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制点击圆形
        if (tapRadius > 0) {
            circlePaint.setAlpha((int) (tapAlpha * 180));
            canvas.drawCircle(tapX, tapY, tapRadius, circlePaint);
            
            // 绘制外圈
            circlePaint.setAlpha((int) (tapAlpha * 80));
            canvas.drawCircle(tapX, tapY, tapRadius * 1.5f, circlePaint);
        }
        
        // 绘制滑动路径
        if (swipePath != null && swipeProgress > 0) {
            // 绘制已经过的路径
            Path drawnPath = new Path();
            pathMeasure.getSegment(0, pathMeasure.getLength() * swipeProgress, drawnPath, true);
            canvas.drawPath(drawnPath, pathPaint);
            
            // 绘制当前位置的点
            pathMeasure.getPosTan(pathMeasure.getLength() * swipeProgress, swipePoint, null);
            dotPaint.setAlpha(200);
            canvas.drawCircle(swipePoint[0], swipePoint[1], 20, dotPaint);
        }
    }

    
    /**
     * 显示点击动画
     * 需求: 9.1
     * 
     * @param x 点击X坐标
     * @param y 点击Y坐标
     */
    public void showTapAnimation(float x, float y) {
        mainHandler.post(() -> {
            attachToWindow();
            
            tapX = x;
            tapY = y;
            tapRadius = 0;
            tapAlpha = 1.0f;
            
            // 创建动画
            AnimatorSet animatorSet = new AnimatorSet();
            
            // 半径动画：从0到60
            ValueAnimator radiusAnimator = ValueAnimator.ofFloat(0, 60);
            radiusAnimator.addUpdateListener(animation -> {
                tapRadius = (float) animation.getAnimatedValue();
                invalidate();
            });
            
            // 透明度动画：从1到0
            ValueAnimator alphaAnimator = ValueAnimator.ofFloat(1.0f, 0f);
            alphaAnimator.addUpdateListener(animation -> {
                tapAlpha = (float) animation.getAnimatedValue();
                invalidate();
            });
            
            animatorSet.playTogether(radiusAnimator, alphaAnimator);
            animatorSet.setDuration(TAP_ANIMATION_DURATION);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tapRadius = 0;
                    detachFromWindow();
                }
            });
            
            animatorSet.start();
        });
    }
    
    /**
     * 显示长按动画
     * 需求: 9.1 (扩展)
     * 
     * @param x 长按X坐标
     * @param y 长按Y坐标
     */
    public void showLongPressAnimation(float x, float y) {
        mainHandler.post(() -> {
            attachToWindow();
            
            tapX = x;
            tapY = y;
            tapRadius = 0;
            tapAlpha = 1.0f;
            
            // 创建动画 - 长按动画更慢，有脉冲效果
            AnimatorSet animatorSet = new AnimatorSet();
            
            // 半径动画：脉冲效果
            ValueAnimator radiusAnimator = ValueAnimator.ofFloat(30, 50, 30, 60);
            radiusAnimator.addUpdateListener(animation -> {
                tapRadius = (float) animation.getAnimatedValue();
                invalidate();
            });
            
            // 透明度动画
            ValueAnimator alphaAnimator = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f, 0f);
            alphaAnimator.addUpdateListener(animation -> {
                tapAlpha = (float) animation.getAnimatedValue();
                invalidate();
            });
            
            animatorSet.playTogether(radiusAnimator, alphaAnimator);
            animatorSet.setDuration(LONG_PRESS_ANIMATION_DURATION);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tapRadius = 0;
                    detachFromWindow();
                }
            });
            
            animatorSet.start();
        });
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
        mainHandler.post(() -> {
            attachToWindow();
            
            // 创建滑动路径
            swipePath = new Path();
            swipePath.moveTo(startX, startY);
            swipePath.lineTo(endX, endY);
            pathMeasure = new PathMeasure(swipePath, false);
            swipeProgress = 0;
            
            // 创建动画
            ValueAnimator progressAnimator = ValueAnimator.ofFloat(0, 1);
            progressAnimator.addUpdateListener(animation -> {
                swipeProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
            
            progressAnimator.setDuration(SWIPE_ANIMATION_DURATION);
            progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            progressAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 延迟一点再消失，让用户看清终点
                    mainHandler.postDelayed(() -> {
                        swipePath = null;
                        swipeProgress = 0;
                        detachFromWindow();
                    }, 200);
                }
            });
            
            progressAnimator.start();
        });
    }
    
    /**
     * 附加到窗口
     */
    private void attachToWindow() {
        if (isAttached) return;
        
        try {
            windowManager.addView(this, layoutParams);
            isAttached = true;
            Log.d(TAG, "手势动画视图已附加到窗口");
        } catch (Exception e) {
            Log.e(TAG, "附加手势动画视图失败", e);
        }
    }
    
    /**
     * 从窗口分离
     */
    private void detachFromWindow() {
        if (!isAttached) return;
        
        try {
            if (isAttachedToWindow()) {
                windowManager.removeView(this);
            }
            isAttached = false;
            Log.d(TAG, "手势动画视图已从窗口分离");
        } catch (Exception e) {
            Log.e(TAG, "分离手势动画视图失败", e);
        }
    }
    
    /**
     * 释放资源
     */
    public void release() {
        mainHandler.post(this::detachFromWindow);
    }
}
