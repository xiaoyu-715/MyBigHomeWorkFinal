package com.example.mybighomework;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.utils.ActionTypeInferrer;

import java.util.List;

public class DailyTaskAdapter extends RecyclerView.Adapter<DailyTaskAdapter.TaskViewHolder> {
    
    private List<DailyTask> taskList;
    private OnTaskClickListener listener;
    
    public interface OnTaskClickListener {
        void onTaskClick(DailyTask task, int position);
        void onTaskComplete(DailyTask task, int position);
    }
    
    public DailyTaskAdapter(List<DailyTask> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_task, parent, false);
        return new TaskViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        DailyTask task = taskList.get(position);
        
        holder.tvTitle.setText(task.getTitle());
        
        // 显示进度信息
        String progressText = task.getProgressText();
        String description = task.getDescription();
        if (progressText != null && !progressText.isEmpty() && !"未完成".equals(progressText)) {
            holder.tvDescription.setText(description + " · " + progressText);
        } else {
            holder.tvDescription.setText(description);
        }
        
        holder.checkBox.setChecked(task.isCompleted());
        
        // 设置任务图标（优先使用actionType）
        int iconRes = getTaskIcon(task.getActionType(), task.getType());
        holder.ivIcon.setImageResource(iconRes);
        
        // 设置完成状态的视觉效果
        float alpha = task.isCompleted() ? 0.6f : 1.0f;
        holder.itemView.setAlpha(alpha);
        
        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task, position);
            }
        });
        
        // 复选框点击事件
        holder.checkBox.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskComplete(task, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return taskList.size();
    }
    
    /**
     * 获取任务图标
     * 优先使用actionType，其次使用type
     */
    private int getTaskIcon(String actionType, String type) {
        // 优先根据actionType获取图标
        if (actionType != null && !actionType.isEmpty()) {
            switch (actionType) {
                case "daily_sentence":
                    return R.drawable.ic_quote;
                case "real_exam":
                    return R.drawable.ic_exam;
                case "mock_exam":
                    return R.drawable.ic_exam;
                case "wrong_question_practice":
                    return R.drawable.ic_refresh;
                case "vocabulary_training":
                    return R.drawable.ic_vocabulary;
                case "translation_practice":
                    return R.drawable.ic_translate;
            }
        }
        
        // 兼容旧的type字段
        if (type != null) {
            switch (type) {
                case "vocabulary":
                    return R.drawable.ic_vocabulary;
                case "exam_practice":
                    return R.drawable.ic_exam;
                case "listening":
                    return R.drawable.ic_headphones;
                case "writing":
                    return R.drawable.ic_edit;
                case "daily_sentence":
                    return R.drawable.ic_quote;
            }
        }
        
        return R.drawable.ic_task;
    }
    
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
        CheckBox checkBox;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}