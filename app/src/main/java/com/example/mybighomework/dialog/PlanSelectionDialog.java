package com.example.mybighomework.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.StudyPlan;
import com.example.mybighomework.adapter.PlanSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习计划选择对话框
 * 显示AI生成的学习计划列表，用户可以选择要保存的计划
 */
public class PlanSelectionDialog extends DialogFragment {
    
    private List<StudyPlan> planList;
    private PlanSelectionAdapter adapter;
    private OnPlansSelectedListener listener;
    private OnRegenerateClickListener regenerateListener;
    
    /**
     * 创建对话框实例
     */
    public static PlanSelectionDialog newInstance(ArrayList<StudyPlan> plans) {
        PlanSelectionDialog dialog = new PlanSelectionDialog();
        Bundle args = new Bundle();
        args.putSerializable("plans", plans);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 获取参数
        if (getArguments() != null) {
            planList = (List<StudyPlan>) getArguments().getSerializable("plans");
        }
        
        if (planList == null) {
            planList = new ArrayList<>();
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_plan_selection, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 显示统计信息
        updateStatistics(view);
        
        // 初始化RecyclerView
        RecyclerView rvPlans = view.findViewById(R.id.rv_plans);
        rvPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new PlanSelectionAdapter(getContext(), planList);
        rvPlans.setAdapter(adapter);
        
        // 设置编辑监听器
        adapter.setOnEditClickListener((position, plan) -> {
            showEditPlanDialog(position, plan);
        });
        
        // 重新生成按钮
        Button btnRegenerate = view.findViewById(R.id.btn_regenerate);
        btnRegenerate.setOnClickListener(v -> {
            if (regenerateListener != null) {
                // 确认对话框
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("重新生成")
                    .setMessage(R.string.regenerate_confirm)
                    .setPositiveButton("确定", (dialog, which) -> {
                        regenerateListener.onRegenerateClick();
                        dismiss();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            }
        });
        
        // 取消按钮
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dismiss());
        
        // 保存按钮
        Button btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> {
            List<StudyPlan> selectedPlans = adapter.getSelectedPlans();
            if (selectedPlans.isEmpty()) {
                // 提示用户至少选择一个计划
                android.widget.Toast.makeText(getContext(), 
                    "请至少选择一个学习计划", 
                    android.widget.Toast.LENGTH_SHORT).show();
            } else {
                if (listener != null) {
                    listener.onPlansSelected(selectedPlans);
                }
                dismiss();
            }
        });
    }
    
    /**
     * 显示编辑计划对话框
     */
    private void showEditPlanDialog(int position, StudyPlan plan) {
        View editView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_plan, null);
        
        // 获取控件
        android.widget.EditText etTitle = editView.findViewById(R.id.et_title);
        android.widget.Spinner spinnerCategory = editView.findViewById(R.id.spinner_category);
        android.widget.EditText etDescription = editView.findViewById(R.id.et_description);
        android.widget.EditText etTimeRange = editView.findViewById(R.id.et_time_range);
        android.widget.EditText etDuration = editView.findViewById(R.id.et_duration);
        android.widget.Spinner spinnerPriority = editView.findViewById(R.id.spinner_priority);
        
        // 设置分类下拉框
        String[] categories = {"词汇", "语法", "听力", "阅读", "写作", "口语"};
        android.widget.ArrayAdapter<String> categoryAdapter = new android.widget.ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // 设置优先级下拉框
        String[] priorities = {"高", "中", "低"};
        android.widget.ArrayAdapter<String> priorityAdapter = new android.widget.ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        
        // 填充现有数据
        etTitle.setText(plan.getTitle());
        etDescription.setText(plan.getDescription());
        etTimeRange.setText(plan.getTimeRange());
        etDuration.setText(plan.getDuration());
        
        // 设置分类和优先级选中项
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(plan.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equals(plan.getPriority())) {
                spinnerPriority.setSelection(i);
                break;
            }
        }
        
        // 显示编辑对话框
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(editView)
            .setPositiveButton("确定", (dialog, which) -> {
                // 更新计划数据
                plan.setTitle(etTitle.getText().toString());
                plan.setCategory(spinnerCategory.getSelectedItem().toString());
                plan.setDescription(etDescription.getText().toString());
                plan.setTimeRange(etTimeRange.getText().toString());
                plan.setDuration(etDuration.getText().toString());
                plan.setPriority(spinnerPriority.getSelectedItem().toString());
                
                // 刷新列表
                adapter.notifyItemChanged(position);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        
        // 移除标题栏
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        // 设置对话框大小
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // 设置对话框为全屏宽度，高度自适应但不超过屏幕80%
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            
            // 设置对话框内容不被裁剪
            dialog.getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            dialog.getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            
            // 添加进入动画
            dialog.getWindow().setWindowAnimations(R.style.DialogSlideAnimation);
        }
        
        // 为RecyclerView添加列表动画
        RecyclerView rvPlans = getView().findViewById(R.id.rv_plans);
        if (rvPlans != null) {
            android.view.animation.Animation animation = android.view.animation.AnimationUtils
                .loadAnimation(getContext(), R.anim.item_animation_fall_down);
            android.view.animation.LayoutAnimationController controller = 
                new android.view.animation.LayoutAnimationController(animation);
            controller.setDelay(0.1f);
            rvPlans.setLayoutAnimation(controller);
        }
    }
    
    /**
     * 更新统计信息
     */
    private void updateStatistics(View view) {
        if (planList == null || planList.isEmpty()) {
            return;
        }
        
        // 显示计划数量
        android.widget.TextView tvPlanCount = view.findViewById(R.id.tv_plan_count);
        if (tvPlanCount != null) {
            tvPlanCount.setText(String.valueOf(planList.size()));
        }
        
        // 计算总时长
        int totalMinutes = 0;
        for (StudyPlan plan : planList) {
            String duration = plan.getDuration();
            if (duration != null) {
                // 提取分钟数
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                java.util.regex.Matcher matcher = pattern.matcher(duration);
                if (matcher.find()) {
                    totalMinutes += Integer.parseInt(matcher.group(1));
                }
            }
        }
        
        // 显示总时长（转换为小时）
        android.widget.TextView tvTotalDuration = view.findViewById(R.id.tv_total_duration);
        if (tvTotalDuration != null) {
            double totalHours = totalMinutes / 60.0;
            tvTotalDuration.setText(String.format("%.1f", totalHours));
        }
    }
    
    /**
     * 设置选择监听器
     */
    public void setOnPlansSelectedListener(OnPlansSelectedListener listener) {
        this.listener = listener;
    }
    
    /**
     * 设置重新生成监听器
     */
    public void setOnRegenerateClickListener(OnRegenerateClickListener listener) {
        this.regenerateListener = listener;
    }
    
    /**
     * 计划选择监听接口
     */
    public interface OnPlansSelectedListener {
        void onPlansSelected(List<StudyPlan> selectedPlans);
    }
    
    /**
     * 重新生成监听接口
     */
    public interface OnRegenerateClickListener {
        void onRegenerateClick();
    }
}

