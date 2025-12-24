package com.example.mybighomework.autoglm.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import com.example.mybighomework.autoglm.automation.AutomationAccessibilityService;
import com.example.mybighomework.autoglm.ui.FloatingWindowManager;

import java.util.List;

/**
 * 权限检查和引导工具类
 * 需求: 1.1, 1.2, 4.9
 */
public class PermissionHelper {
    
    /**
     * 权限状态
     */
    public static class PermissionStatus {
        public final boolean accessibilityEnabled;
        public final boolean overlayEnabled;
        public final boolean allPermissionsGranted;
        
        public PermissionStatus(boolean accessibilityEnabled, boolean overlayEnabled) {
            this.accessibilityEnabled = accessibilityEnabled;
            this.overlayEnabled = overlayEnabled;
            this.allPermissionsGranted = accessibilityEnabled && overlayEnabled;
        }
    }
    
    /**
     * 检查所有必要权限
     * 需求: 1.1, 4.9
     * 
     * @param context 上下文
     * @return 权限状态对象
     */
    public static PermissionStatus checkAllPermissions(Context context) {
        boolean accessibilityEnabled = isAccessibilityServiceEnabled(context);
        boolean overlayEnabled = isOverlayPermissionEnabled(context);
        return new PermissionStatus(accessibilityEnabled, overlayEnabled);
    }
    
    /**
     * 检查无障碍服务是否开启
     * 需求: 1.1
     * 
     * 使用两种方式检查：
     * 1. 检查服务实例是否存在（运行时检查）
     * 2. 检查系统设置中是否启用（配置检查）
     * 
     * @param context 上下文
     * @return 是否开启
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        // 方式1: 检查服务实例
        if (AutomationAccessibilityService.isServiceEnabled()) {
            return true;
        }
        
        // 方式2: 检查系统设置
        return isAccessibilityServiceEnabledInSettings(context);
    }
    
    /**
     * 通过系统设置检查无障碍服务是否启用
     */
    private static boolean isAccessibilityServiceEnabledInSettings(Context context) {
        try {
            AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am == null) {
                return false;
            }
            
            List<AccessibilityServiceInfo> enabledServices = 
                    am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            
            String packageName = context.getPackageName();
            String serviceName = AutomationAccessibilityService.class.getName();
            
            for (AccessibilityServiceInfo service : enabledServices) {
                ComponentName componentName = ComponentName.unflattenFromString(service.getId());
                if (componentName != null && 
                    packageName.equals(componentName.getPackageName()) &&
                    serviceName.equals(componentName.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        
        return false;
    }
    
    /**
     * 检查悬浮窗权限是否开启
     * 需求: 4.9
     * 
     * @param context 上下文
     * @return 是否开启
     */
    public static boolean isOverlayPermissionEnabled(Context context) {
        return FloatingWindowManager.hasOverlayPermission(context);
    }

    
    /**
     * 打开无障碍服务设置页面
     * 需求: 1.2
     * 
     * @param context 上下文
     */
    public static void openAccessibilitySettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // 如果无法打开无障碍设置，尝试打开系统设置
            openSystemSettings(context);
        }
    }
    
    /**
     * 打开悬浮窗权限设置页面
     * 需求: 4.9
     * 
     * @param context 上下文
     */
    public static void openOverlaySettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                // 如果无法打开悬浮窗设置，尝试打开应用设置
                openAppSettings(context);
            }
        }
    }
    
    /**
     * 打开系统设置
     */
    public static void openSystemSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // 忽略
        }
    }
    
    /**
     * 打开应用设置
     */
    public static void openAppSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            openSystemSettings(context);
        }
    }
    
    /**
     * 获取无障碍服务的完整组件名
     * 用于在设置中定位服务
     * 
     * @param context 上下文
     * @return 组件名字符串
     */
    public static String getAccessibilityServiceComponentName(Context context) {
        return context.getPackageName() + "/" + AutomationAccessibilityService.class.getName();
    }
    
    /**
     * 获取权限缺失的描述信息
     * 
     * @param status 权限状态
     * @return 描述信息
     */
    public static String getMissingPermissionsDescription(PermissionStatus status) {
        if (status.allPermissionsGranted) {
            return "所有权限已开启";
        }
        
        StringBuilder sb = new StringBuilder("需要开启以下权限：\n");
        
        if (!status.accessibilityEnabled) {
            sb.append("• 无障碍服务 - 用于截屏和模拟手势\n");
        }
        
        if (!status.overlayEnabled) {
            sb.append("• 悬浮窗权限 - 用于显示任务状态\n");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 检查Android版本是否支持自动化功能
     * 截屏功能需要 Android 11 (API 30) 或更高版本
     * 
     * @return 是否支持
     */
    public static boolean isAndroidVersionSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }
    
    /**
     * 获取Android版本不支持的提示信息
     * 
     * @return 提示信息
     */
    public static String getUnsupportedVersionMessage() {
        return "自动化功能需要 Android 11 或更高版本。当前版本: Android " + Build.VERSION.SDK_INT;
    }
}
