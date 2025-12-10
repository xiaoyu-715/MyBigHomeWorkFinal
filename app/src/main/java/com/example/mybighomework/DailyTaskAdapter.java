package com.example.mybighomework;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.tvDescription.setText(task.getDescription());
        holder.checkBox.setChecked(task.isCompleted());
        
        // 设置任务图标
        int iconRes = getTaskIcon(task.getType());
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
    
    private int getTaskIcon(String type) {
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
            default:
                return R.drawable.ic_task;
        }
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