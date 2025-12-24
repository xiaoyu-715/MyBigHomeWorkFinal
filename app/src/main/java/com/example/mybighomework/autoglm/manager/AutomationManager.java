package com.example.mybighomework.autoglm.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.example.mybighomework.autoglm.automation.AutomationAccessibilityService;
import com.example.mybighomework.autoglm.error.AutomationErrorHandler;
import com.example.mybighomework.autoglm.error.AutomationErrorHandler.ErrorInfo;
import com.example.mybighomework.autoglm.error.AutomationErrorHandler.ErrorType;
import com.example.mybighomework.autoglm.model.Action;
import com.example.mybighomework.autoglm.model.ActionExecutor;
import com.example.mybighomework.autoglm.model.ActionParser;
import com.example.mybighomework.autoglm.network.MultimodalModelClient;
import com.example.mybighomework.autoglm.ui.FloatingWindowManager;
import com.example.mybighomework.autoglm.util.PermissionHelper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自动化任务管理器
 * 协调整个自动化任务的执行流程
 * 
 * 需求: 6.1-6.8
 */
public class AutomationManager {
    
    private static final String TAG = "AutomationManager";
    
    // 最大执行步数限制
    private static final int MAX_STEPS = 20;
    
    // 动作执行后等待页面加载的时间（毫秒）
    private static final long PAGE_LOAD_WAIT_MS = 2000;
    
    // 截屏前后悬浮窗隐藏/显示的延迟（毫秒）
    private static final long FLOATING_WINDOW_DELAY_MS = 300;
    
    // API调用重试延迟（毫秒）
    private static final long API_RETRY_DELAY_MS = 2000;
    
    // 单例实例
    private static volatile AutomationManager instance;
    private static final Object LOCK = new Object();
    
    // 上下文
    private Context context;
    
    // API密钥
    private String apiKey;
    
    // 组件
    private MultimodalModelClient modelClient;
    private ActionExecutor actionExecutor;
    private FloatingWindowManager floatingWindowManager;
    
    // 执行状态
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicInteger currentStep = new AtomicInteger(0);
    
    // 重试计数器
    private final AtomicInteger apiRetryCount = new AtomicInteger(0);
    private final AtomicInteger screenshotRetryCount = new AtomicInteger(0);
    private final AtomicInteger actionRetryCount = new AtomicInteger(0);
    
    // 上一次动作执行失败的反馈消息（用于通知AI）
    private String lastActionFailureFeedback = null;
    
    // 线程池
    private ExecutorService executorService;
    
    // 主线程Handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // 回调
    private AutomationCallback callback;
    
    // 当前任务指令
    private String currentCommand;
    
    /**
     * 自动化任务回调接口
     * 需求: 6.1-6.8
     */
    public interface AutomationCallback {
        /**
         * 状态更新
         * @param status 状态描述
         */
        void onStatusUpdate(String status);
        
        /**
         * 动作执行完成
         * @param action 执行的动作
         * @param success 是否成功
         */
        void onActionExecuted(Action action, boolean success);
        
        /**
         * 任务完成
         * @param message 完成消息
         */
        void onTaskComplete(String message);
        
        /**
         * 发生错误
         * @param error 错误信息
         */
        void onError(String error);
    }
    
    /**
     * 私有构造函数（单例模式）
     */
    private AutomationManager() {
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 获取单例实例
     * 需求: 6.1
     */
    public static AutomationManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new AutomationManager();
                }
            }
        }
        return instance;
    }

    
    /**
     * 初始化管理器
     * 需求: 6.1
     * 
     * @param context 应用上下文
     * @param apiKey 智谱AI API密钥
     */
    public void initialize(Context context, String apiKey) {
        this.context = context.getApplicationContext();
        this.apiKey = apiKey;
        
        // 初始化多模态模型客户端
        this.modelClient = new MultimodalModelClient(apiKey);
        
        // 初始化悬浮窗管理器
        this.floatingWindowManager = new FloatingWindowManager(this.context);
        
        Log.d(TAG, "AutomationManager 初始化完成");
    }
    
    /**
     * 检查所有必要权限
     * 需求: 6.1, 1.1, 1.2, 4.9
     * 
     * @param context 上下文
     * @return 是否所有权限都已开启
     */
    public boolean checkPermissions(Context context) {
        PermissionHelper.PermissionStatus status = PermissionHelper.checkAllPermissions(context);
        Log.d(TAG, "权限检查 - 无障碍服务: " + status.accessibilityEnabled + ", 悬浮窗: " + status.overlayEnabled);
        return status.allPermissionsGranted;
    }
    
    /**
     * 获取详细的权限状态
     * 需求: 1.1, 4.9
     * 
     * @param context 上下文
     * @return 权限状态对象
     */
    public PermissionHelper.PermissionStatus getPermissionStatus(Context context) {
        return PermissionHelper.checkAllPermissions(context);
    }
    
    /**
     * 检查无障碍服务是否开启
     * 需求: 1.1
     */
    public boolean isAccessibilityServiceEnabled(Context context) {
        return PermissionHelper.isAccessibilityServiceEnabled(context);
    }
    
    /**
     * 检查悬浮窗权限是否开启
     * 需求: 4.9
     */
    public boolean isOverlayPermissionEnabled(Context context) {
        return PermissionHelper.isOverlayPermissionEnabled(context);
    }
    
    /**
     * 打开无障碍服务设置
     * 需求: 1.2
     */
    public void openAccessibilitySettings(Context context) {
        PermissionHelper.openAccessibilitySettings(context);
    }
    
    /**
     * 打开悬浮窗权限设置
     * 需求: 4.9
     */
    public void openOverlaySettings(Context context) {
        PermissionHelper.openOverlaySettings(context);
    }
    
    /**
     * 检查Android版本是否支持
     */
    public boolean isAndroidVersionSupported() {
        return PermissionHelper.isAndroidVersionSupported();
    }
    
    /**
     * 启动自动化任务
     * 需求: 6.2, 6.3
     * 
     * @param userCommand 用户指令
     * @param callback 任务回调
     */
    public void startTask(String userCommand, AutomationCallback callback) {
        if (isRunning.get()) {
            Log.w(TAG, "任务已在运行中");
            callback.onError("任务已在运行中，请先停止当前任务");
            return;
        }
        
        // 检查权限
        if (!checkPermissions(context)) {
            callback.onError("权限不足，请先开启无障碍服务和悬浮窗权限");
            return;
        }
        
        // 获取无障碍服务实例
        AutomationAccessibilityService service = AutomationAccessibilityService.getInstance();
        if (service == null) {
            callback.onError("无障碍服务未连接，请重新开启");
            return;
        }
        
        // 初始化动作执行器
        this.actionExecutor = new ActionExecutor(service);
        
        // 保存回调和指令
        this.callback = callback;
        this.currentCommand = userCommand;
        
        // 重置状态
        isRunning.set(true);
        stopRequested.set(false);
        currentStep.set(0);
        
        // 重置重试计数器
        apiRetryCount.set(0);
        screenshotRetryCount.set(0);
        actionRetryCount.set(0);
        lastActionFailureFeedback = null;
        
        // 清空模型客户端历史
        modelClient.clearHistory();
        
        Log.d(TAG, "启动自动化任务: " + userCommand);
        
        // 显示悬浮窗
        showFloatingWindow();
        
        // 回到桌面
        mainHandler.postDelayed(() -> {
            service.goHome();
            
            // 等待桌面加载后开始执行循环
            mainHandler.postDelayed(this::startExecutionLoop, PAGE_LOAD_WAIT_MS);
        }, 500);
    }
    
    /**
     * 停止任务
     * 需求: 6.8, 4.5
     */
    public void stopTask() {
        Log.d(TAG, "请求停止任务");
        stopRequested.set(true);
        isRunning.set(false);
        
        // 更新悬浮窗状态
        if (floatingWindowManager != null) {
            floatingWindowManager.setTaskRunning(false);
            floatingWindowManager.updateStatusLabel("已停止");
            floatingWindowManager.updateStatus("任务已被用户停止");
        }
        
        // 通知回调
        if (callback != null) {
            mainHandler.post(() -> callback.onTaskComplete("任务已停止"));
        }
    }
    
    /**
     * 检查任务是否正在运行
     * 需求: 6.1
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    
    /**
     * 显示悬浮窗
     * 需求: 4.1
     */
    private void showFloatingWindow() {
        if (floatingWindowManager != null) {
            floatingWindowManager.show(
                // 停止按钮回调
                () -> stopTask(),
                // 返回应用按钮回调
                () -> {
                    // 返回到应用
                    if (callback != null) {
                        callback.onTaskComplete("用户返回应用");
                    }
                }
            );
            floatingWindowManager.reset();
            floatingWindowManager.updateStepProgress(0, MAX_STEPS);
        }
    }
    
    /**
     * 隐藏悬浮窗
     */
    private void hideFloatingWindow() {
        if (floatingWindowManager != null) {
            floatingWindowManager.hide();
        }
    }
    
    /**
     * 开始执行循环
     * 需求: 6.3
     */
    private void startExecutionLoop() {
        executorService.execute(this::executeLoop);
    }
    
    /**
     * 执行循环主体
     * 需求: 6.3, 6.4, 6.5, 6.6, 6.7, 10.1-10.5
     */
    private void executeLoop() {
        while (isRunning.get() && !stopRequested.get()) {
            int step = currentStep.incrementAndGet();
            
            // 检查最大步数限制
            // 需求: 6.5, 10.5
            if (step > MAX_STEPS) {
                handleMaxStepsReached();
                return;
            }
            
            Log.d(TAG, "执行步骤 " + step + "/" + MAX_STEPS);
            
            // 更新悬浮窗进度
            updateFloatingWindowProgress(step);
            
            // 检查无障碍服务是否仍然连接
            // 需求: 10.4
            if (!checkAccessibilityServiceConnected()) {
                handleAccessibilityServiceDisconnected();
                return;
            }
            
            try {
                // 步骤1: 截屏
                // 需求: 4.7, 10.1 - 截屏时隐藏悬浮窗，截屏失败时停止任务
                Bitmap screenshot = captureScreenWithRetry();
                if (screenshot == null) {
                    // 截屏失败已在 captureScreenWithRetry 中处理
                    return;
                }
                
                // 步骤2: 发送AI请求
                // 需求: 10.2 - API调用失败时停止任务并显示错误原因
                updateFloatingWindowStatus("思考中...", "正在分析屏幕内容...");
                String aiResponse = sendToAIWithRetry(screenshot);
                if (aiResponse == null) {
                    // API错误已在 sendToAIWithRetry 中处理
                    return;
                }
                
                // 重置API重试计数
                apiRetryCount.set(0);
                
                // 步骤3: 解析动作
                Action action = parseAction(aiResponse);
                Log.d(TAG, "解析到动作: " + action.getActionType() + " - " + action.getDescription());
                
                // 步骤4: 检查是否是完成动作
                // 需求: 6.6
                if (action instanceof Action.Finish) {
                    handleTaskFinish((Action.Finish) action);
                    return;
                }
                
                // 步骤5: 检查是否是错误动作
                if (action instanceof Action.Error) {
                    handleActionError((Action.Error) action);
                    return;
                }
                
                // 步骤6: 执行动作
                // 需求: 10.3 - 动作执行失败时通知AI并尝试继续
                updateFloatingWindowStatus("执行中...", action.getDescription());
                boolean success = executeActionWithErrorHandling(action);
                
                // 通知回调
                notifyActionExecuted(action, success);
                
                // 如果动作执行失败，准备反馈消息给AI
                if (!success) {
                    ErrorInfo errorInfo = AutomationErrorHandler.handleActionExecutionError(
                        action.getActionType(), "执行返回失败");
                    lastActionFailureFeedback = AutomationErrorHandler.generateAIFeedbackMessage(errorInfo);
                    Log.d(TAG, "动作执行失败，将在下一轮通知AI: " + lastActionFailureFeedback);
                } else {
                    lastActionFailureFeedback = null;
                    actionRetryCount.set(0);
                }
                
                // 步骤7: 等待页面加载
                // 需求: 6.4
                if (!(action instanceof Action.Wait)) {
                    Thread.sleep(PAGE_LOAD_WAIT_MS);
                }
                
            } catch (InterruptedException e) {
                Log.w(TAG, "执行循环被中断");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Log.e(TAG, "执行循环出错", e);
                ErrorInfo errorInfo = AutomationErrorHandler.handleUnknownError(e);
                handleErrorWithInfo(errorInfo);
                return;
            }
        }
        
        // 循环结束但不是因为完成或错误
        if (stopRequested.get()) {
            Log.d(TAG, "任务被用户停止");
        }
    }
    
    /**
     * 截取屏幕
     * 需求: 4.7 - 截屏时悬浮窗隐藏
     */
    private Bitmap captureScreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.e(TAG, "截屏功能需要 Android 11 或更高版本");
            return null;
        }
        
        AutomationAccessibilityService service = AutomationAccessibilityService.getInstance();
        if (service == null) {
            Log.e(TAG, "无障碍服务不可用");
            return null;
        }
        
        // 隐藏悬浮窗
        if (floatingWindowManager != null) {
            floatingWindowManager.setVisibility(false);
        }
        
        // 等待悬浮窗隐藏
        try {
            Thread.sleep(FLOATING_WINDOW_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 使用同步方式截屏
        final Bitmap[] result = new Bitmap[1];
        final Object lock = new Object();
        final boolean[] completed = new boolean[1];
        final int[] errorCode = new int[1];
        
        service.captureScreen(new AutomationAccessibilityService.ScreenshotCallback() {
            @Override
            public void onScreenshot(Bitmap bitmap) {
                synchronized (lock) {
                    result[0] = bitmap;
                    completed[0] = true;
                    lock.notify();
                }
            }
            
            @Override
            public void onError(int code) {
                Log.e(TAG, "截屏失败，错误码: " + code);
                synchronized (lock) {
                    errorCode[0] = code;
                    completed[0] = true;
                    lock.notify();
                }
            }
        });
        
        // 等待截屏完成
        synchronized (lock) {
            try {
                if (!completed[0]) {
                    lock.wait(5000); // 最多等待5秒
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 恢复悬浮窗显示
        if (floatingWindowManager != null) {
            floatingWindowManager.setVisibility(true);
        }
        
        // 如果有错误码，保存以便后续处理
        if (result[0] == null && errorCode[0] != 0) {
            lastScreenshotErrorCode = errorCode[0];
        }
        
        return result[0];
    }
    
    // 保存最后一次截屏错误码
    private int lastScreenshotErrorCode = 0;
    
    /**
     * 带重试的截屏
     * 需求: 10.1 - 截屏失败时停止任务并提示用户
     */
    private Bitmap captureScreenWithRetry() {
        lastScreenshotErrorCode = 0;
        Bitmap screenshot = captureScreen();
        
        if (screenshot != null) {
            screenshotRetryCount.set(0);
            return screenshot;
        }
        
        // 获取错误信息
        ErrorInfo errorInfo = AutomationErrorHandler.handleScreenshotError(lastScreenshotErrorCode);
        
        // 检查是否可以重试
        if (AutomationErrorHandler.canRetry(errorInfo, screenshotRetryCount.get())) {
            screenshotRetryCount.incrementAndGet();
            Log.d(TAG, "截屏失败，尝试重试 (" + screenshotRetryCount.get() + "/" + errorInfo.maxRetries + ")");
            
            // 等待一段时间后重试
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            
            return captureScreenWithRetry();
        }
        
        // 无法重试，处理错误
        handleErrorWithInfo(errorInfo);
        return null;
    }

    
    /**
     * 发送请求到AI
     * 需求: 3.1-3.7
     */
    private String sendToAI(Bitmap screenshot) {
        try {
            // 构建消息文本
            String messageText;
            if (currentStep.get() == 1) {
                // 第一步，发送用户指令
                messageText = currentCommand;
            } else if (lastActionFailureFeedback != null) {
                // 如果上一步动作执行失败，发送反馈给AI
                // 需求: 10.3 - 动作执行失败时通知AI
                messageText = lastActionFailureFeedback;
                lastActionFailureFeedback = null; // 清除反馈
            } else {
                // 后续步骤，发送继续执行的提示
                messageText = "请继续执行任务";
            }
            
            return modelClient.sendRequest(messageText, screenshot);
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "API调用超时", e);
            throw new RuntimeException("网络超时", e);
        } catch (IOException e) {
            Log.e(TAG, "API调用失败", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * 带重试的AI请求
     * 需求: 10.2 - API调用失败时停止任务并显示错误原因
     */
    private String sendToAIWithRetry(Bitmap screenshot) {
        try {
            String response = sendToAI(screenshot);
            if (response != null) {
                return response;
            }
            
            // 响应为空，创建错误信息
            ErrorInfo errorInfo = AutomationErrorHandler.handleApiError(
                new IOException("AI响应为空"));
            
            // 检查是否可以重试
            if (AutomationErrorHandler.canRetry(errorInfo, apiRetryCount.get())) {
                return retryApiCall(screenshot, errorInfo);
            }
            
            handleErrorWithInfo(errorInfo);
            return null;
            
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            ErrorInfo errorInfo;
            
            if (cause instanceof SocketTimeoutException) {
                errorInfo = AutomationErrorHandler.handleNetworkTimeout();
            } else {
                errorInfo = AutomationErrorHandler.handleApiError(
                    cause instanceof Exception ? (Exception) cause : e);
            }
            
            // 检查是否可以重试
            if (AutomationErrorHandler.canRetry(errorInfo, apiRetryCount.get())) {
                return retryApiCall(screenshot, errorInfo);
            }
            
            handleErrorWithInfo(errorInfo);
            return null;
        }
    }
    
    /**
     * 重试API调用
     */
    private String retryApiCall(Bitmap screenshot, ErrorInfo errorInfo) {
        apiRetryCount.incrementAndGet();
        Log.d(TAG, "API调用失败，尝试重试 (" + apiRetryCount.get() + "/" + errorInfo.maxRetries + ")");
        
        updateFloatingWindowStatus("重试中...", "网络请求失败，正在重试...");
        
        // 等待一段时间后重试
        try {
            Thread.sleep(API_RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        
        return sendToAIWithRetry(screenshot);
    }
    
    /**
     * 解析AI响应为动作
     * 需求: 2.1-2.10
     */
    private Action parseAction(String response) {
        AutomationAccessibilityService service = AutomationAccessibilityService.getInstance();
        int screenWidth = service != null ? service.getScreenWidth() : 1080;
        int screenHeight = service != null ? service.getScreenHeight() : 2400;
        
        return ActionParser.parse(response, screenWidth, screenHeight);
    }
    
    /**
     * 执行动作
     * 需求: 2.11
     */
    private boolean executeAction(Action action) {
        if (actionExecutor == null) {
            Log.e(TAG, "动作执行器未初始化");
            return false;
        }
        return actionExecutor.execute(action);
    }
    
    /**
     * 带错误处理的动作执行
     * 需求: 10.3 - 动作执行失败时通知AI并尝试继续
     */
    private boolean executeActionWithErrorHandling(Action action) {
        boolean success = executeAction(action);
        
        if (!success) {
            Log.w(TAG, "动作执行失败: " + action.getActionType());
            
            // 对于某些动作类型，可以尝试重试
            if (shouldRetryAction(action) && actionRetryCount.get() < 2) {
                actionRetryCount.incrementAndGet();
                Log.d(TAG, "尝试重新执行动作 (" + actionRetryCount.get() + "/2)");
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return executeAction(action);
            }
        }
        
        return success;
    }
    
    /**
     * 判断动作是否应该重试
     */
    private boolean shouldRetryAction(Action action) {
        // 点击、滑动、长按等手势操作可以重试
        return action instanceof Action.Tap 
            || action instanceof Action.DoubleTap
            || action instanceof Action.Swipe 
            || action instanceof Action.LongPress;
    }
    
    // ==================== 错误处理方法 ====================
    
    /**
     * 检查无障碍服务是否仍然连接
     * 需求: 10.4
     */
    private boolean checkAccessibilityServiceConnected() {
        AutomationAccessibilityService service = AutomationAccessibilityService.getInstance();
        return service != null && AutomationAccessibilityService.isServiceEnabled();
    }
    
    /**
     * 处理无障碍服务断开
     * 需求: 10.4 - IF 无障碍服务断开 THEN THE System SHALL 停止任务并提示重新开启
     */
    private void handleAccessibilityServiceDisconnected() {
        ErrorInfo errorInfo = AutomationErrorHandler.handleAccessibilityServiceDisconnected();
        handleErrorWithInfo(errorInfo);
    }
    
    /**
     * 处理达到最大步数
     * 需求: 10.5 - IF 达到最大步数限制 THEN THE System SHALL 停止任务并提示用户
     */
    private void handleMaxStepsReached() {
        Log.w(TAG, "达到最大步数限制: " + MAX_STEPS);
        ErrorInfo errorInfo = AutomationErrorHandler.handleMaxStepsReached(MAX_STEPS);
        handleErrorWithInfo(errorInfo);
    }
    
    /**
     * 处理截屏错误
     * 需求: 10.1 - IF 截屏失败 THEN THE System SHALL 停止任务并提示用户
     */
    private void handleScreenshotError() {
        Log.e(TAG, "截屏失败");
        ErrorInfo errorInfo = AutomationErrorHandler.handleScreenshotError(lastScreenshotErrorCode);
        handleErrorWithInfo(errorInfo);
    }
    
    /**
     * 处理API错误
     * 需求: 10.2 - IF API调用失败 THEN THE System SHALL 停止任务并显示错误原因
     */
    private void handleApiError(String reason) {
        Log.e(TAG, "API调用失败: " + reason);
        ErrorInfo errorInfo = AutomationErrorHandler.handleApiError(new IOException(reason));
        handleErrorWithInfo(errorInfo);
    }
    
    /**
     * 处理动作错误
     * 需求: 10.3
     */
    private void handleActionError(Action.Error error) {
        Log.e(TAG, "动作错误: " + error.reason);
        ErrorInfo errorInfo = AutomationErrorHandler.handleActionExecutionError("Error", error.reason);
        handleErrorWithInfo(errorInfo);
    }
    
    /**
     * 处理任务完成
     * 需求: 6.6
     */
    private void handleTaskFinish(Action.Finish finish) {
        Log.i(TAG, "任务完成: " + finish.message);
        isRunning.set(false);
        
        if (floatingWindowManager != null) {
            floatingWindowManager.setCompleted(finish.message);
        }
        
        if (callback != null) {
            mainHandler.post(() -> callback.onTaskComplete(finish.message));
        }
    }
    
    /**
     * 使用ErrorInfo处理错误
     * 需求: 6.7, 10.1-10.5
     */
    private void handleErrorWithInfo(ErrorInfo errorInfo) {
        Log.e(TAG, "任务错误 [" + errorInfo.type + "]: " + errorInfo.userMessage);
        
        // 判断是否应该停止任务
        if (AutomationErrorHandler.shouldStopTask(errorInfo)) {
            isRunning.set(false);
        }
        
        if (floatingWindowManager != null) {
            floatingWindowManager.setError(errorInfo.userMessage);
        }
        
        if (callback != null) {
            mainHandler.post(() -> callback.onError(errorInfo.userMessage));
        }
    }
    
    /**
     * 通用错误处理
     * 需求: 6.7
     */
    private void handleError(String error) {
        Log.e(TAG, "任务错误: " + error);
        isRunning.set(false);
        
        if (floatingWindowManager != null) {
            floatingWindowManager.setError(error);
        }
        
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
    
    // ==================== UI更新方法 ====================
    
    /**
     * 更新悬浮窗进度
     */
    private void updateFloatingWindowProgress(int step) {
        if (floatingWindowManager != null) {
            mainHandler.post(() -> floatingWindowManager.updateStepProgress(step, MAX_STEPS));
        }
    }
    
    /**
     * 更新悬浮窗状态
     */
    private void updateFloatingWindowStatus(String label, String detail) {
        if (floatingWindowManager != null) {
            mainHandler.post(() -> {
                floatingWindowManager.updateStatusLabel(label);
                floatingWindowManager.updateStatus(detail);
            });
        }
        
        if (callback != null) {
            mainHandler.post(() -> callback.onStatusUpdate(detail));
        }
    }
    
    /**
     * 通知动作执行完成
     */
    private void notifyActionExecuted(Action action, boolean success) {
        if (callback != null) {
            mainHandler.post(() -> callback.onActionExecuted(action, success));
        }
    }
    
    // ==================== 其他公共方法 ====================
    
    /**
     * 获取当前步数
     */
    public int getCurrentStep() {
        return currentStep.get();
    }
    
    /**
     * 获取最大步数
     */
    public int getMaxSteps() {
        return MAX_STEPS;
    }
    
    /**
     * 获取悬浮窗管理器
     */
    public FloatingWindowManager getFloatingWindowManager() {
        return floatingWindowManager;
    }
    
    /**
     * 释放资源
     */
    public void release() {
        stopTask();
        hideFloatingWindow();
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        instance = null;
        Log.d(TAG, "AutomationManager 资源已释放");
    }
}
