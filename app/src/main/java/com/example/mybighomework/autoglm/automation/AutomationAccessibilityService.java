package com.example.mybighomework.autoglm.automation;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

/**
 * 无障碍服务类，提供屏幕截取和手势模拟的底层能力
 * 需求: 1.3, 1.4, 1.5, 1.6, 1.7, 1.8
 */
public class AutomationAccessibilityService extends AccessibilityService {

    private static final String TAG = "AutomationAccessibility";
    
    // 单例实例
    private static AutomationAccessibilityService instance;
    
    // 当前应用包名
    private String currentPackageName;
    
    // 主线程Handler
    private Handler mainHandler;
    
    /**
     * 截屏回调接口
     */
    public interface ScreenshotCallback {
        void onScreenshot(Bitmap bitmap);
        void onError(int errorCode);
    }
    
    /**
     * 获取单例实例
     */
    public static AutomationAccessibilityService getInstance() {
        return instance;
    }
    
    /**
     * 检查服务是否可用
     */
    public static boolean isServiceEnabled() {
        return instance != null;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "无障碍服务已连接");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        Log.d(TAG, "无障碍服务已断开");
        return super.onUnbind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event != null && event.getPackageName() != null) {
            currentPackageName = event.getPackageName().toString();
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "无障碍服务被中断");
    }


    /**
     * 获取屏幕宽度
     */
    public int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return windowManager.getCurrentWindowMetrics().getBounds().width();
        } else {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels;
        }
    }
    
    /**
     * 获取屏幕高度
     */
    public int getScreenHeight() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return windowManager.getCurrentWindowMetrics().getBounds().height();
        } else {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            return metrics.heightPixels;
        }
    }
    
    /**
     * 获取当前应用包名
     */
    public String getCurrentPackageName() {
        return currentPackageName;
    }
    
    /**
     * 截取屏幕 (Android 11+)
     * 需求: 1.4
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void captureScreen(ScreenshotCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.e(TAG, "截屏功能需要 Android 11 或更高版本");
            callback.onError(-1);
            return;
        }
        
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int displayId = display.getDisplayId();
        
        takeScreenshot(displayId, getMainExecutor(), new TakeScreenshotCallback() {
            @Override
            public void onSuccess(ScreenshotResult screenshotResult) {
                try {
                    Bitmap hardwareBitmap = Bitmap.wrapHardwareBuffer(
                            screenshotResult.getHardwareBuffer(),
                            screenshotResult.getColorSpace()
                    );
                    // 转换为软件位图以便处理
                    Bitmap softwareBitmap = hardwareBitmap != null ? 
                            hardwareBitmap.copy(Bitmap.Config.ARGB_8888, false) : null;
                    screenshotResult.getHardwareBuffer().close();
                    
                    if (softwareBitmap != null) {
                        callback.onScreenshot(softwareBitmap);
                    } else {
                        callback.onError(-2);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "处理截屏结果时出错", e);
                    callback.onError(-3);
                }
            }
            
            @Override
            public void onFailure(int errorCode) {
                String errorMsg;
                switch (errorCode) {
                    case ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR:
                        errorMsg = "内部错误";
                        break;
                    case ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS:
                        errorMsg = "无无障碍访问权限";
                        break;
                    case ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT:
                        errorMsg = "截屏间隔太短";
                        break;
                    case ERROR_TAKE_SCREENSHOT_INVALID_DISPLAY:
                        errorMsg = "无效的显示器";
                        break;
                    default:
                        errorMsg = "未知错误(" + errorCode + ")";
                }
                Log.e(TAG, "截屏失败: " + errorMsg);
                callback.onError(errorCode);
            }
        });
    }


    /**
     * 执行点击操作
     * 需求: 1.5
     */
    public boolean performTap(float x, float y) {
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();
        
        if (x < 0 || x > screenWidth || y < 0 || y > screenHeight) {
            Log.w(TAG, "点击坐标超出屏幕范围: (" + x + ", " + y + ")");
            return false;
        }
        
        Log.d(TAG, "执行点击: (" + x + ", " + y + ")");
        
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
        
        return dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "点击完成: (" + x + ", " + y + ")");
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.w(TAG, "点击被取消: (" + x + ", " + y + ")");
            }
        }, null);
    }
    
    /**
     * 执行滑动操作
     * 需求: 1.6
     */
    public boolean performSwipe(float startX, float startY, float endX, float endY, long duration) {
        Log.d(TAG, "执行滑动: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + "), 时长: " + duration + "ms");
        
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        // 使用固定的500ms作为实际手势时长，确保能被识别为滑动
        long gestureDuration = Math.min(duration, 500);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, gestureDuration));
        
        return dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "滑动完成");
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.w(TAG, "滑动被取消");
            }
        }, null);
    }
    
    /**
     * 执行长按操作
     * 需求: 1.7
     */
    public boolean performLongPress(float x, float y, long duration) {
        Log.d(TAG, "执行长按: (" + x + ", " + y + "), 时长: " + duration + "ms");
        
        // 长按实际上是在同一位置的滑动
        return performSwipe(x, y, x, y, duration);
    }


    /**
     * 执行返回操作
     * 需求: 1.8
     */
    public boolean goBack() {
        Log.d(TAG, "执行返回操作");
        return performGlobalAction(GLOBAL_ACTION_BACK);
    }
    
    /**
     * 执行回到桌面操作
     * 需求: 1.8
     */
    public boolean goHome() {
        Log.d(TAG, "执行回到桌面操作");
        return performGlobalAction(GLOBAL_ACTION_HOME);
    }
    
    /**
     * 查找可编辑的输入框节点
     * 需求: 8.1, 8.2
     */
    public AccessibilityNodeInfo findEditableNode() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.w(TAG, "无法获取根节点");
            return null;
        }
        return findEditableNodeRecursive(rootNode);
    }
    
    /**
     * 递归查找可编辑节点
     */
    private AccessibilityNodeInfo findEditableNodeRecursive(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        
        // 检查当前节点是否可编辑
        if (node.isEditable() && node.isFocused()) {
            return node;
        }
        
        // 先查找已聚焦的可编辑节点
        if (node.isEditable()) {
            return node;
        }
        
        // 递归查找子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findEditableNodeRecursive(child);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 在输入框中设置文本
     * 需求: 8.2
     */
    public boolean setTextInEditableNode(String text) {
        AccessibilityNodeInfo editableNode = findEditableNode();
        if (editableNode == null) {
            Log.w(TAG, "未找到可编辑的输入框");
            return false;
        }
        
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        boolean result = editableNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        
        Log.d(TAG, "设置文本" + (result ? "成功" : "失败") + ": " + text);
        return result;
    }
    
    /**
     * 在主线程执行操作
     */
    public void runOnMainThread(Runnable runnable) {
        if (mainHandler != null) {
            mainHandler.post(runnable);
        }
    }
}
