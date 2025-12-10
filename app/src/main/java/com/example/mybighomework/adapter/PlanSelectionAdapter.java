package com.example.mybighomework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.StudyPlan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 学习计划选择适配器
 * 用于在对话框中显示可选择的学习计划列表
 */
public class PlanSelectionAdapter extends RecyclerView.Adapter<PlanSelectionAdapter.ViewHolder> {
    
    private Context context;
    private List<StudyPlan> planList;
    private Set<Integer> selectedPositions; // 记录选中的位置
    private OnEditClickListener editClickListener;
    
    public PlanSelectionAdapter(Context context, List<StudyPlan> planList) {
        this.context = context;
        this.planList = planList;
        this.selectedPositions = new HashSet<>();
        
        // 默认全部选中
        for (int i = 0; i < planList.size(); i++) {
            selectedPositions.add(i);
        }
    }
    
    /**
     * 设置编辑按钮点击监听器
     */
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
            R.layout.item_plan_selection, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyPlan plan = planList.get(position);
        
        holder.tvTitle.setText(plan.getTitle());
        holder.tvCategory.setText(plan.getCategory());
        holder.tvPriority.setText(plan.getPriority() + "优先级");
        holder.tvDescription.setText(plan.getDescription());
        holder.tvTimeRange.setText(plan.getTimeRange());
        holder.tvDuration.setText(plan.getDuration());
        
        // 根据优先级设置左侧指示条颜色和图标
        setPriorityStyle(holder, plan.getPriority());
        
        // 设置CheckBox状态
        holder.checkboxSelect.setChecked(selectedPositions.contains(position));
        
        // CheckBox点击事件
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
        });
        
        // 整个item点击切换选中状态
        holder.itemView.setOnClickListener(v -> {
            holder.checkboxSelect.toggle();
        });
        
        // 编辑按钮点击事件
        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(position, plan);
            }
        });
    }
    
    /**
     * 设置优先级样式（颜色和图标）
     */
    private void setPriorityStyle(ViewHolder holder, String priority) {
        int indicatorColor;
        int backgroundRes;
        int iconRes;
        
        switch (priority) {
            case "高":
                indicatorColor = context.getColor(R.color.priority_high_indicator);
                backgroundRes = R.drawable.bg_priority_high;
                iconRes = R.drawable.ic_priority_high_fire;
                break;
            case "中":
                indicatorColor = context.getColor(R.color.priority_medium_indicator);
                backgroundRes = R.drawable.bg_priority_medium;
                iconRes = R.drawable.ic_priority_medium_bolt;
                break;
            case "低":
                indicatorColor = context.getColor(R.color.priority_low_indicator);
                backgroundRes = R.drawable.bg_priority_low;
                iconRes = R.drawable.ic_priority_low_bulb;
                break;
            default:
                indicatorColor = context.getColor(R.color.text_meta);
                backgroundRes = R.drawable.bg_priority_tag;
                iconRes = R.drawable.ic_priority_medium_bolt;
                break;
        }
        
        // 设置指示条颜色
        if (holder.priorityIndicator != null) {
            holder.priorityIndicator.setBackgroundColor(indicatorColor);
        }
        
        // 设置优先级容器背景
        if (holder.priorityContainer != null) {
            holder.priorityContainer.setBackgroundResource(backgroundRes);
        }
        
        // 设置优先级图标
        if (holder.ivPriorityIcon != null) {
            holder.ivPriorityIcon.setImageResource(iconRes);
        }
    }
    
    @Override
    public int getItemCount() {
        return planList.size();
    }
    
    /**
     * 获取用户选中的学习计划
     */
    public List<StudyPlan> getSelectedPlans() {
        List<StudyPlan> selectedPlans = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < planList.size()) {
                selectedPlans.add(planList.get(position));
            }
        }
        return selectedPlans;
    }
    
    /**
     * ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxSelect;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvPriority;
        TextView tvDescription;
        TextView tvTimeRange;
        TextView tvDuration;
        android.widget.ImageButton btnEdit;
        View priorityIndicator;
        android.widget.LinearLayout priorityContainer;
        android.widget.ImageView ivPriorityIcon;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxSelect = itemView.findViewById(R.id.checkbox_select);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTimeRange = itemView.findViewById(R.id.tv_time_range);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            priorityContainer = itemView.findViewById(R.id.priority_container);
            ivPriorityIcon = itemView.findViewById(R.id.iv_priority_icon);
        }
    }
    
    /**
     * 编辑按钮点击监听接口
     */
    public interface OnEditClickListener {
        void onEditClick(int position, StudyPlan plan);
    }
}

