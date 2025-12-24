package com.example.mybighomework.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.DailyTaskEntity;

import java.util.List;

/**
 * 每日任务详情适配器
 * 用于在计划详情页显示今日任务列表
 * 
 * Requirements: 4.1, 4.2
 */
public class DailyTaskDetailAdapter extends RecyclerView.Adapter<DailyTaskDetailAdapter.ViewHolder> {

    private final Context context;
    private List<DailyTaskEntity> tasks;
    private final OnTaskCompletionListener listener;

    /**
     * 任务完成状态变化监听器
     */
    public interface OnTaskCompletionListener {
        void onTaskCompletionChanged(DailyTaskEntity task, boolean isCompleted);
    }

    public DailyTaskDetailAdapter(Context context, List<DailyTaskEntity> tasks, 
                                   OnTaskCompletionListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_daily_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyTaskEntity task = tasks.get(position);
        
        // 设置任务内容
        holder.tvTitle.setText(task.getTaskContent());
        
        // 设置预计时长
        String durationText = task.getEstimatedMinutes() + " 分钟";
        holder.tvDescription.setText(durationText);
        
        // 设置复选框状态（不触发监听器）
        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(task.isCompleted());
        
        // 根据完成状态更新UI样式
        updateTaskAppearance(holder, task.isCompleted());
        
        // 设置复选框点击监听
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTaskCompletionChanged(task, isChecked);
            }
        });
        
        // 整个item点击也可以切换状态
        holder.itemView.setOnClickListener(v -> {
            holder.checkbox.setChecked(!holder.checkbox.isChecked());
        });
    }

    /**
     * 根据完成状态更新任务外观
     */
    private void updateTaskAppearance(ViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            // 已完成：添加删除线，降低透明度
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_hint));
            holder.ivIcon.setAlpha(0.5f);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.success));
        } else {
            // 未完成：正常显示
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.ivIcon.setAlpha(1.0f);
            holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary_blue));
        }
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    /**
     * 更新数据
     */
    public void updateData(List<DailyTaskEntity> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
        CheckBox checkbox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}
