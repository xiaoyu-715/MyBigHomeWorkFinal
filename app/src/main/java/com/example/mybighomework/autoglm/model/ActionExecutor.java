package com.example.mybighomework.autoglm.model;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mybighomework.autoglm.automation.AutomationAccessibilityService;
import com.example.mybighomework.autoglm.error.AutomationErrorHandler;
import com.example.mybighomework.autoglm.ui.GestureAnimationManager;

/**
 * 动作执行器 - 执行解析后的Action
 * 需求: 2.11, 8.1-8.4, 9.1-9.4
 * 
 * 将Action对象转换为实际的屏幕操作
 */
public class ActionExecutor {
    
    private static final String TAG = "ActionExecutor";
    
    // 无障碍服务引用
    private final AutomationAccessibilityService service;
    
    // 主线程Handler
    private final Handler mainHandler;
    
    // 手势动画管理器
    private GestureAnimationManager gestureAnimationManager;
    
    // 是否启用手势动画
    private boolean gestureAnimationEnabled = true;
    
    /**
     * 执行结果回调
     */
    public interface ExecutionCallback {
        void onSuccess();
        void onFailure(String reason);
    }
    
    /**
     * 执行结果详情
     */
    public static class ExecutionResult {
        public final boolean success;
        public final String errorMessage;
        public final AutomationErrorHandler.ErrorType errorType;
        
        public ExecutionResult(boolean success) {
            this.success = success;
            this.errorMessage = null;
            this.errorType = null;
        }
        
        public ExecutionResult(boolean success, String errorMessage, AutomationErrorHandler.ErrorType errorType) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.errorType = errorType;
        }
    }
    
    /**
     * 构造函数
     * 
     * @param service 无障碍服务实例
     */
    public ActionExecutor(AutomationAccessibilityService service) {
        this.service = service;
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // 初始化手势动画管理器
        if (service != null) {
            gestureAnimationManager = GestureAnimationManager.getInstance();
            if (!gestureAnimationManager.isInitialized()) {
                gestureAnimationManager.initialize(service.getApplicationContext());
            }
        }
    }
    
    /**
     * 设置是否启用手势动画
     * 需求: 9.1-9.4
     * 
     * @param enabled 是否启用
     */
    public void setGestureAnimationEnabled(boolean enabled) {
        this.gestureAnimationEnabled = enabled;
    }
    
    /**
     * 执行动作（同步版本）
     * 需求: 2.11
     * 
     * @param action 要执行的动作
     * @return 是否执行成功
     */
    public boolean execute(Action action) {
        if (action == null) {
            Log.e(TAG, "动作为空");
            return false;
        }
        
        if (service == null) {
            Log.e(TAG, "无障碍服务未初始化");
            return false;
        }
        
        Log.d(TAG, "执行动作: " + action.getActionType() + " - " + action.getDescription());
        
        try {
            if (action instanceof Action.Tap) {
                return executeTap((Action.Tap) action);
            } else if (action instanceof Action.DoubleTap) {
                return executeDoubleTap((Action.DoubleTap) action);
            } else if (action instanceof Action.LongPress) {
                return executeLongPress((Action.LongPress) action);
            } else if (action instanceof Action.Swipe) {
                return executeSwipe((Action.Swipe) action);
            } else if (action instanceof Action.Type) {
                return executeType((Action.Type) action);
            } else if (action instanceof Action.Launch) {
                return executeLaunch((Action.Launch) action);
            } else if (action instanceof Action.Back) {
                return executeBack();
            } else if (action instanceof Action.Home) {
                return executeHome();
            } else if (action instanceof Action.Wait) {
                return executeWait((Action.Wait) action);
            } else if (action instanceof Action.Finish) {
                return executeFinish((Action.Finish) action);
            } else if (action instanceof Action.Error) {
                return executeError((Action.Error) action);
            } else {
                Log.w(TAG, "未知动作类型: " + action.getClass().getSimpleName());
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "执行动作时出错: " + e.getMessage(), e);
            return false;
        }
    }

    
    /**
     * 执行点击动作
     * 需求: 9.1 - 在点击位置显示圆形动画
     */
    private boolean executeTap(Action.Tap action) {
        Log.d(TAG, "执行点击: (" + action.x + ", " + action.y + ")");
        
        // 显示点击动画
        showTapAnimation(action.x, action.y);
        
        return service.performTap(action.x, action.y);
    }
    
    /**
     * 执行双击动作
     * 需求: 9.1 - 在点击位置显示圆形动画
     */
    private boolean executeDoubleTap(Action.DoubleTap action) {
        Log.d(TAG, "执行双击: (" + action.x + ", " + action.y + ")");
        
        // 显示点击动画
        showTapAnimation(action.x, action.y);
        
        // 执行两次点击，中间间隔150ms
        boolean success1 = service.performTap(action.x, action.y);
        
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean success2 = service.performTap(action.x, action.y);
        
        return success1 && success2;
    }
    
    /**
     * 执行长按动作
     * 需求: 9.1 - 在点击位置显示圆形动画
     */
    private boolean executeLongPress(Action.LongPress action) {
        Log.d(TAG, "执行长按: (" + action.x + ", " + action.y + "), 时长: " + action.durationMs + "ms");
        
        // 显示长按动画
        showLongPressAnimation(action.x, action.y);
        
        return service.performLongPress(action.x, action.y, action.durationMs);
    }
    
    /**
     * 执行滑动动作
     * 需求: 9.2 - 显示从起点到终点的轨迹动画
     */
    private boolean executeSwipe(Action.Swipe action) {
        Log.d(TAG, "执行滑动: (" + action.startX + ", " + action.startY + ") -> (" 
                + action.endX + ", " + action.endY + ")");
        
        // 显示滑动动画
        showSwipeAnimation(action.startX, action.startY, action.endX, action.endY);
        
        return service.performSwipe(
                action.startX, action.startY,
                action.endX, action.endY,
                action.durationMs
        );
    }
    
    /**
     * 显示点击动画
     * 需求: 9.1, 9.3
     */
    private void showTapAnimation(float x, float y) {
        if (!gestureAnimationEnabled || gestureAnimationManager == null) return;
        
        try {
            gestureAnimationManager.showTapAnimation(x, y);
        } catch (Exception e) {
            Log.w(TAG, "显示点击动画失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示长按动画
     * 需求: 9.1, 9.3
     */
    private void showLongPressAnimation(float x, float y) {
        if (!gestureAnimationEnabled || gestureAnimationManager == null) return;
        
        try {
            gestureAnimationManager.showLongPressAnimation(x, y);
        } catch (Exception e) {
            Log.w(TAG, "显示长按动画失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示滑动动画
     * 需求: 9.2, 9.3
     */
    private void showSwipeAnimation(float startX, float startY, float endX, float endY) {
        if (!gestureAnimationEnabled || gestureAnimationManager == null) return;
        
        try {
            gestureAnimationManager.showSwipeAnimation(startX, startY, endX, endY);
        } catch (Exception e) {
            Log.w(TAG, "显示滑动动画失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行文本输入动作
     * 需求: 8.1-8.4, 10.3
     */
    private boolean executeType(Action.Type action) {
        Log.d(TAG, "执行输入: " + action.text);
        
        // 需求: 8.1 - 查找当前可编辑的输入框
        // 需求: 8.2 - 使用AccessibilityNodeInfo设置文本
        boolean success = service.setTextInEditableNode(action.text);
        
        if (!success) {
            // 需求: 8.3, 10.3 - 未找到可编辑输入框，返回执行失败
            Log.w(TAG, "未找到可编辑的输入框");
            // 记录错误信息以便后续处理
            AutomationErrorHandler.handleInputFieldNotFound();
            return false;
        }
        
        // 需求: 8.4 - 等待短暂时间确保输入完成
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return true;
    }
    
    /**
     * 执行启动应用动作
     */
    private boolean executeLaunch(Action.Launch action) {
        Log.d(TAG, "执行启动应用: " + action.appName);
        return launchApp(action.appName);
    }
    
    /**
     * 启动应用
     * 需求: 10.3 - 应用启动失败时记录错误
     * 
     * @param appName 应用名称
     * @return 是否成功启动
     */
    private boolean launchApp(String appName) {
        // 获取包名
        String packageName = AppMapper.getPackageName(appName);
        
        if (packageName == null) {
            Log.e(TAG, "未知应用: " + appName);
            AutomationErrorHandler.handleAppLaunchFailed(appName, "未知应用");
            return false;
        }
        
        Log.d(TAG, "应用 " + appName + " 映射到包名: " + packageName);
        
        try {
            // 获取启动Intent
            Intent intent = service.getPackageManager().getLaunchIntentForPackage(packageName);
            
            if (intent == null) {
                Log.e(TAG, "无法获取应用启动Intent: " + packageName);
                AutomationErrorHandler.handleAppLaunchFailed(appName, "无法获取启动Intent，应用可能未安装");
                return false;
            }
            
            // 添加新任务标志
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // 启动应用
            service.startActivity(intent);
            Log.d(TAG, "应用启动成功: " + packageName);
            
            // 等待应用启动
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "启动应用失败: " + e.getMessage(), e);
            AutomationErrorHandler.handleAppLaunchFailed(appName, e.getMessage());
            return false;
        }
    }

    
    /**
     * 执行返回动作
     */
    private boolean executeBack() {
        Log.d(TAG, "执行返回");
        return service.goBack();
    }
    
    /**
     * 执行回到桌面动作
     */
    private boolean executeHome() {
        Log.d(TAG, "执行回到桌面");
        return service.goHome();
    }
    
    /**
     * 执行等待动作
     */
    private boolean executeWait(Action.Wait action) {
        Log.d(TAG, "执行等待: " + action.durationMs + "ms");
        
        try {
            Thread.sleep(action.durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        return true;
    }
    
    /**
     * 执行完成动作
     */
    private boolean executeFinish(Action.Finish action) {
        Log.i(TAG, "任务完成: " + action.message);
        return true;
    }
    
    /**
     * 执行错误动作
     */
    private boolean executeError(Action.Error action) {
        Log.e(TAG, "动作错误: " + action.reason);
        return false;
    }
    
    /**
     * 执行动作（异步版本）
     * 
     * @param action 要执行的动作
     * @param callback 执行结果回调
     */
    public void executeAsync(Action action, ExecutionCallback callback) {
        new Thread(() -> {
            boolean success = execute(action);
            
            mainHandler.post(() -> {
                if (success) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("动作执行失败: " + action.getDescription());
                }
            });
        }).start();
    }
    
    /**
     * 检查服务是否可用
     */
    public boolean isServiceAvailable() {
        return service != null && AutomationAccessibilityService.isServiceEnabled();
    }
    
    /**
     * 获取屏幕宽度
     */
    public int getScreenWidth() {
        return service != null ? service.getScreenWidth() : 0;
    }
    
    /**
     * 获取屏幕高度
     */
    public int getScreenHeight() {
        return service != null ? service.getScreenHeight() : 0;
    }
}
