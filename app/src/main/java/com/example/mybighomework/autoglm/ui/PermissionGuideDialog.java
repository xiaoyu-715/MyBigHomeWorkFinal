package com.example.mybighomework.autoglm.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.mybighomework.R;
import com.example.mybighomework.autoglm.util.PermissionHelper;

/**
 * 权限引导对话框
 * 用于引导用户开启无障碍服务和悬浮窗权限
 * 
 * 需求: 1.2, 4.9
 */
public class PermissionGuideDialog extends DialogFragment {
    
    /**
     * 权限类型
     */
    public enum PermissionType {
        ACCESSIBILITY,  // 无障碍服务
        OVERLAY         // 悬浮窗权限
    }
    
    private static final String ARG_PERMISSION_TYPE = "permission_type";
    
    private PermissionType permissionType;
    private OnPermissionActionListener listener;
    
    /**
     * 权限操作回调
     */
    public interface OnPermissionActionListener {
        void onGoToSettings();
        void onCancel();
    }
    
    /**
     * 创建无障碍服务引导对话框
     */
    public static PermissionGuideDialog newAccessibilityDialog() {
        return newInstance(PermissionType.ACCESSIBILITY);
    }
    
    /**
     * 创建悬浮窗权限引导对话框
     */
    public static PermissionGuideDialog newOverlayDialog() {
        return newInstance(PermissionType.OVERLAY);
    }
    
    private static PermissionGuideDialog newInstance(PermissionType type) {
        PermissionGuideDialog dialog = new PermissionGuideDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PERMISSION_TYPE, type);
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setOnPermissionActionListener(OnPermissionActionListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            permissionType = (PermissionType) getArguments().getSerializable(ARG_PERMISSION_TYPE);
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_MyBigHomeWork_Dialog);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_permission_guide, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        ImageView ivIcon = view.findViewById(R.id.ivPermissionIcon);
        TextView tvTitle = view.findViewById(R.id.tvPermissionTitle);
        TextView tvDescription = view.findViewById(R.id.tvPermissionDescription);
        TextView tvSteps = view.findViewById(R.id.tvPermissionSteps);
        Button btnGoToSettings = view.findViewById(R.id.btnGoToSettings);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        
        // 根据权限类型设置内容
        if (permissionType == PermissionType.ACCESSIBILITY) {
            setupAccessibilityContent(ivIcon, tvTitle, tvDescription, tvSteps);
        } else {
            setupOverlayContent(ivIcon, tvTitle, tvDescription, tvSteps);
        }
        
        // 设置按钮点击事件
        btnGoToSettings.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoToSettings();
            }
            openSettings();
            dismiss();
        });
        
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    /**
     * 设置无障碍服务引导内容
     */
    private void setupAccessibilityContent(ImageView ivIcon, TextView tvTitle, 
                                           TextView tvDescription, TextView tvSteps) {
        ivIcon.setImageResource(R.drawable.ic_settings);
        tvTitle.setText("开启无障碍服务");
        tvDescription.setText("自动化功能需要无障碍服务权限来截取屏幕内容和模拟手势操作。" +
                "这是实现AI自动操作手机的核心权限。");
        tvSteps.setText("操作步骤：\n\n" +
                "1. 点击\"前往设置\"按钮\n" +
                "2. 在无障碍服务列表中找到\"考研英语备考\"\n" +
                "3. 点击进入并开启服务\n" +
                "4. 在弹出的确认对话框中点击\"允许\"\n" +
                "5. 返回应用即可使用自动化功能");
    }
    
    /**
     * 设置悬浮窗权限引导内容
     */
    private void setupOverlayContent(ImageView ivIcon, TextView tvTitle, 
                                     TextView tvDescription, TextView tvSteps) {
        ivIcon.setImageResource(R.drawable.ic_floating_window);
        tvTitle.setText("开启悬浮窗权限");
        tvDescription.setText("自动化功能需要悬浮窗权限来显示任务执行状态。" +
                "悬浮窗会在AI执行任务时显示当前进度和操作信息。");
        tvSteps.setText("操作步骤：\n\n" +
                "1. 点击\"前往设置\"按钮\n" +
                "2. 找到\"显示在其他应用上层\"选项\n" +
                "3. 开启该权限\n" +
                "4. 返回应用即可使用自动化功能");
    }
    
    /**
     * 打开对应的系统设置
     */
    private void openSettings() {
        Context context = getContext();
        if (context == null) return;
        
        if (permissionType == PermissionType.ACCESSIBILITY) {
            PermissionHelper.openAccessibilitySettings(context);
        } else {
            PermissionHelper.openOverlaySettings(context);
        }
    }
    
    /**
     * 显示对话框
     */
    public void show(FragmentManager manager) {
        show(manager, "PermissionGuideDialog");
    }
}
