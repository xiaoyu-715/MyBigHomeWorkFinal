package com.example.mybighomework.autoglm.error;

import android.util.Log;

/**
 * 自动化任务错误处理器
 * 统一处理各种错误情况，提供用户友好的错误消息
 * 
 * 需求: 10.1-10.5
 */
public class AutomationErrorHandler {
    
    private static final String TAG = "AutomationErrorHandler";
    
    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        /** 截屏失败 - 需求 10.1 */
        SCREENSHOT_FAILED,
        /** API调用失败 - 需求 10.2 */
        API_CALL_FAILED,
        /** 动作执行失败 - 需求 10.3 */
        ACTION_EXECUTION_FAILED,
        /** 无障碍服务断开 - 需求 10.4 */
        ACCESSIBILITY_SERVICE_DISCONNECTED,
        /** 达到最大步数限制 - 需求 10.5 */
        MAX_STEPS_REACHED,
        /** 网络超时 */
        NETWORK_TIMEOUT,
        /** 权限不足 */
        PERMISSION_DENIED,
        /** 应用启动失败 */
        APP_LAUNCH_FAILED,
        /** 输入框未找到 */
        INPUT_FIELD_NOT_FOUND,
        /** 未知错误 */
        UNKNOWN
    }
    
    /**
     * 错误严重程度
     */
    public enum ErrorSeverity {
        /** 致命错误 - 必须停止任务 */
        FATAL,
        /** 可恢复错误 - 可以尝试继续 */
        RECOVERABLE,
        /** 警告 - 记录但继续执行 */
        WARNING
    }
    
    /**
     * 错误信息类
     */
    public static class ErrorInfo {
        public final ErrorType type;
        public final ErrorSeverity severity;
        public final String userMessage;
        public final String technicalMessage;
        public final boolean shouldRetry;
        public final int maxRetries;
        
        public ErrorInfo(ErrorType type, ErrorSeverity severity, String userMessage, 
                        String technicalMessage, boolean shouldRetry, int maxRetries) {
            this.type = type;
            this.severity = severity;
            this.userMessage = userMessage;
            this.technicalMessage = technicalMessage;
            this.shouldRetry = shouldRetry;
            this.maxRetries = maxRetries;
        }
    }
    
    /**
     * 处理截屏失败错误
     * 需求: 10.1 - IF 截屏失败 THEN THE System SHALL 停止任务并提示用户
     * 
     * @param errorCode 截屏错误码
     * @return 错误信息
     */
    public static ErrorInfo handleScreenshotError(int errorCode) {
        String technicalMessage = getScreenshotErrorMessage(errorCode);
        Log.e(TAG, "截屏失败: " + technicalMessage + " (错误码: " + errorCode + ")");
        
        String userMessage;
        switch (errorCode) {
            case -1:
                userMessage = "截屏功能需要 Android 11 或更高版本";
                break;
            case -2:
                userMessage = "截屏结果处理失败，请重试";
                break;
            case -3:
                userMessage = "截屏数据转换失败，请重试";
                break;
            case 1: // ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR
                userMessage = "系统截屏服务出错，请重启应用后重试";
                break;
            case 2: // ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS
                userMessage = "无障碍服务权限不足，请重新开启无障碍服务";
                break;
            case 3: // ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT
                userMessage = "截屏间隔太短，请稍后重试";
                break;
            case 4: // ERROR_TAKE_SCREENSHOT_INVALID_DISPLAY
                userMessage = "无法获取屏幕显示，请检查设备状态";
                break;
            default:
                userMessage = "截屏失败，请检查无障碍服务权限";
        }
        
        // 截屏间隔太短可以重试
        boolean shouldRetry = (errorCode == 3);
        
        return new ErrorInfo(
            ErrorType.SCREENSHOT_FAILED,
            ErrorSeverity.FATAL,
            userMessage,
            technicalMessage,
            shouldRetry,
            shouldRetry ? 2 : 0
        );
    }
    
    /**
     * 处理API调用失败错误
     * 需求: 10.2 - IF API调用失败 THEN THE System SHALL 停止任务并显示错误原因
     * 
     * @param exception 异常信息
     * @return 错误信息
     */
    public static ErrorInfo handleApiError(Exception exception) {
        String technicalMessage = exception != null ? exception.getMessage() : "未知API错误";
        Log.e(TAG, "API调用失败: " + technicalMessage, exception);
        
        String userMessage;
        boolean shouldRetry = false;
        int maxRetries = 0;
        
        if (technicalMessage != null) {
            if (technicalMessage.contains("timeout") || technicalMessage.contains("超时")) {
                userMessage = "AI服务响应超时，请检查网络连接后重试";
                shouldRetry = true;
                maxRetries = 1;
            } else if (technicalMessage.contains("401") || technicalMessage.contains("Unauthorized")) {
                userMessage = "API密钥无效或已过期，请检查配置";
            } else if (technicalMessage.contains("429") || technicalMessage.contains("rate limit")) {
                userMessage = "请求过于频繁，请稍后再试";
                shouldRetry = true;
                maxRetries = 1;
            } else if (technicalMessage.contains("500") || technicalMessage.contains("Internal Server")) {
                userMessage = "AI服务暂时不可用，请稍后重试";
                shouldRetry = true;
                maxRetries = 1;
            } else if (technicalMessage.contains("网络") || technicalMessage.contains("network") 
                    || technicalMessage.contains("connect")) {
                userMessage = "网络连接失败，请检查网络设置";
                shouldRetry = true;
                maxRetries = 1;
            } else if (technicalMessage.contains("响应格式错误")) {
                userMessage = "AI响应格式异常，请重试";
                shouldRetry = true;
                maxRetries = 1;
            } else {
                userMessage = "AI服务调用失败: " + extractUserFriendlyMessage(technicalMessage);
            }
        } else {
            userMessage = "AI服务调用失败，请检查网络连接";
        }
        
        return new ErrorInfo(
            ErrorType.API_CALL_FAILED,
            ErrorSeverity.FATAL,
            userMessage,
            technicalMessage,
            shouldRetry,
            maxRetries
        );
    }

    
    /**
     * 处理动作执行失败错误
     * 需求: 10.3 - IF 动作执行失败 THEN THE System SHALL 通知AI并尝试继续
     * 
     * @param actionType 动作类型
     * @param reason 失败原因
     * @return 错误信息
     */
    public static ErrorInfo handleActionExecutionError(String actionType, String reason) {
        String technicalMessage = "动作执行失败 [" + actionType + "]: " + reason;
        Log.e(TAG, technicalMessage);
        
        String userMessage;
        boolean shouldRetry = true; // 动作执行失败通常可以尝试继续
        
        switch (actionType) {
            case "Tap":
            case "DoubleTap":
                userMessage = "点击操作失败，AI将尝试其他方式";
                break;
            case "Swipe":
                userMessage = "滑动操作失败，AI将尝试调整";
                break;
            case "LongPress":
                userMessage = "长按操作失败，AI将尝试其他方式";
                break;
            case "Type":
                userMessage = "文本输入失败，可能未找到输入框";
                break;
            case "Launch":
                userMessage = "应用启动失败，请确认应用已安装";
                shouldRetry = false; // 应用启动失败不应重试
                break;
            case "Back":
            case "Home":
                userMessage = "系统操作失败，请检查无障碍服务";
                break;
            default:
                userMessage = "操作执行失败，AI将尝试继续";
        }
        
        return new ErrorInfo(
            ErrorType.ACTION_EXECUTION_FAILED,
            ErrorSeverity.RECOVERABLE,
            userMessage,
            technicalMessage,
            shouldRetry,
            2
        );
    }
    
    /**
     * 处理无障碍服务断开错误
     * 需求: 10.4 - IF 无障碍服务断开 THEN THE System SHALL 停止任务并提示重新开启
     * 
     * @return 错误信息
     */
    public static ErrorInfo handleAccessibilityServiceDisconnected() {
        String technicalMessage = "无障碍服务已断开连接";
        Log.e(TAG, technicalMessage);
        
        return new ErrorInfo(
            ErrorType.ACCESSIBILITY_SERVICE_DISCONNECTED,
            ErrorSeverity.FATAL,
            "无障碍服务已断开，请重新开启后再试",
            technicalMessage,
            false,
            0
        );
    }
    
    /**
     * 处理达到最大步数限制错误
     * 需求: 10.5 - IF 达到最大步数限制 THEN THE System SHALL 停止任务并提示用户
     * 
     * @param maxSteps 最大步数
     * @return 错误信息
     */
    public static ErrorInfo handleMaxStepsReached(int maxSteps) {
        String technicalMessage = "已达到最大执行步数限制: " + maxSteps;
        Log.w(TAG, technicalMessage);
        
        return new ErrorInfo(
            ErrorType.MAX_STEPS_REACHED,
            ErrorSeverity.FATAL,
            "已执行" + maxSteps + "步，任务自动停止。如需继续，请重新发送指令",
            technicalMessage,
            false,
            0
        );
    }
    
    /**
     * 处理网络超时错误
     * 
     * @return 错误信息
     */
    public static ErrorInfo handleNetworkTimeout() {
        String technicalMessage = "网络请求超时";
        Log.e(TAG, technicalMessage);
        
        return new ErrorInfo(
            ErrorType.NETWORK_TIMEOUT,
            ErrorSeverity.FATAL,
            "网络连接超时，请检查网络后重试",
            technicalMessage,
            true,
            1
        );
    }
    
    /**
     * 处理权限不足错误
     * 
     * @param permissionType 权限类型
     * @return 错误信息
     */
    public static ErrorInfo handlePermissionDenied(String permissionType) {
        String technicalMessage = "权限不足: " + permissionType;
        Log.e(TAG, technicalMessage);
        
        String userMessage;
        if ("accessibility".equals(permissionType)) {
            userMessage = "请先开启无障碍服务权限";
        } else if ("overlay".equals(permissionType)) {
            userMessage = "请先开启悬浮窗权限";
        } else {
            userMessage = "权限不足，请检查应用权限设置";
        }
        
        return new ErrorInfo(
            ErrorType.PERMISSION_DENIED,
            ErrorSeverity.FATAL,
            userMessage,
            technicalMessage,
            false,
            0
        );
    }
    
    /**
     * 处理应用启动失败错误
     * 
     * @param appName 应用名称
     * @param reason 失败原因
     * @return 错误信息
     */
    public static ErrorInfo handleAppLaunchFailed(String appName, String reason) {
        String technicalMessage = "应用启动失败 [" + appName + "]: " + reason;
        Log.e(TAG, technicalMessage);
        
        String userMessage;
        if (reason != null && reason.contains("未知应用")) {
            userMessage = "未找到应用\"" + appName + "\"，请确认应用名称正确";
        } else if (reason != null && reason.contains("Intent")) {
            userMessage = "无法启动\"" + appName + "\"，请确认应用已安装";
        } else {
            userMessage = "启动\"" + appName + "\"失败，请确认应用已安装且可用";
        }
        
        return new ErrorInfo(
            ErrorType.APP_LAUNCH_FAILED,
            ErrorSeverity.RECOVERABLE,
            userMessage,
            technicalMessage,
            false,
            0
        );
    }
    
    /**
     * 处理输入框未找到错误
     * 
     * @return 错误信息
     */
    public static ErrorInfo handleInputFieldNotFound() {
        String technicalMessage = "未找到可编辑的输入框";
        Log.w(TAG, technicalMessage);
        
        return new ErrorInfo(
            ErrorType.INPUT_FIELD_NOT_FOUND,
            ErrorSeverity.RECOVERABLE,
            "未找到输入框，AI将尝试先点击输入区域",
            technicalMessage,
            true,
            2
        );
    }
    
    /**
     * 处理未知错误
     * 
     * @param exception 异常
     * @return 错误信息
     */
    public static ErrorInfo handleUnknownError(Exception exception) {
        String technicalMessage = exception != null ? exception.getMessage() : "未知错误";
        Log.e(TAG, "未知错误: " + technicalMessage, exception);
        
        return new ErrorInfo(
            ErrorType.UNKNOWN,
            ErrorSeverity.FATAL,
            "发生未知错误，请重试",
            technicalMessage,
            false,
            0
        );
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取截屏错误消息
     */
    private static String getScreenshotErrorMessage(int errorCode) {
        switch (errorCode) {
            case -1:
                return "Android版本不支持";
            case -2:
                return "截屏结果为空";
            case -3:
                return "截屏数据处理异常";
            case 1:
                return "内部错误";
            case 2:
                return "无无障碍访问权限";
            case 3:
                return "截屏间隔太短";
            case 4:
                return "无效的显示器";
            default:
                return "未知错误(" + errorCode + ")";
        }
    }
    
    /**
     * 提取用户友好的错误消息
     */
    private static String extractUserFriendlyMessage(String technicalMessage) {
        if (technicalMessage == null) {
            return "未知错误";
        }
        
        // 移除技术细节，只保留关键信息
        String message = technicalMessage;
        
        // 移除HTTP状态码详情
        if (message.contains(" - ")) {
            int dashIndex = message.indexOf(" - ");
            if (dashIndex > 0 && dashIndex < 50) {
                message = message.substring(dashIndex + 3);
            }
        }
        
        // 截断过长的消息
        if (message.length() > 100) {
            message = message.substring(0, 100) + "...";
        }
        
        return message;
    }
    
    /**
     * 判断错误是否应该停止任务
     * 
     * @param errorInfo 错误信息
     * @return 是否应该停止任务
     */
    public static boolean shouldStopTask(ErrorInfo errorInfo) {
        return errorInfo.severity == ErrorSeverity.FATAL;
    }
    
    /**
     * 判断错误是否可以重试
     * 
     * @param errorInfo 错误信息
     * @param currentRetryCount 当前重试次数
     * @return 是否可以重试
     */
    public static boolean canRetry(ErrorInfo errorInfo, int currentRetryCount) {
        return errorInfo.shouldRetry && currentRetryCount < errorInfo.maxRetries;
    }
    
    /**
     * 生成发送给AI的错误反馈消息
     * 用于通知AI动作执行失败，让AI尝试其他方式
     * 
     * @param errorInfo 错误信息
     * @return AI反馈消息
     */
    public static String generateAIFeedbackMessage(ErrorInfo errorInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("上一步操作执行失败: ");
        sb.append(errorInfo.userMessage);
        sb.append("\n请尝试其他方式完成任务。");
        
        // 根据错误类型添加具体建议
        switch (errorInfo.type) {
            case ACTION_EXECUTION_FAILED:
                sb.append("\n建议: 可以尝试调整点击位置或使用其他操作方式。");
                break;
            case INPUT_FIELD_NOT_FOUND:
                sb.append("\n建议: 请先点击输入框使其获得焦点，然后再输入文本。");
                break;
            case APP_LAUNCH_FAILED:
                sb.append("\n建议: 可以尝试从桌面手动找到并点击应用图标。");
                break;
            default:
                break;
        }
        
        return sb.toString();
    }
}
