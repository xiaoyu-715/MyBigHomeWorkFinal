package com.example.mybighomework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.StudyPhaseEntity;

import java.util.List;
import java.util.Locale;

/**
 * 阶段进度适配器
 * 用于在计划详情页显示学习阶段列表
 * 
 * Requirements: 3.3
 */
public class PhaseProgressAdapter extends RecyclerView.Adapter<PhaseProgressAdapter.ViewHolder> {

    private final Context context;
    private List<StudyPhaseEntity> phases;

    public PhaseProgressAdapter(Context context, List<StudyPhaseEntity> phases) {
        this.context = context;
        this.phases = phases;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_phase_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyPhaseEntity phase = phases.get(position);
        
        // 设置阶段名称
        holder.tvPhaseName.setText(phase.getPhaseName());
        
        // 设置阶段目标
        String goal = phase.getGoal();
        if (goal != null && !goal.isEmpty()) {
            holder.tvPhaseGoal.setText(goal);
            holder.tvPhaseGoal.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhaseGoal.setVisibility(View.GONE);
        }
        
        // 设置进度
        int progress = phase.getProgress();
        holder.progressPhase.setProgress(progress);
        holder.tvPhaseProgress.setText(progress + "%");
        
        // 设置天数信息
        String daysText = String.format(Locale.getDefault(), 
            "%d/%d 天", phase.getCompletedDays(), phase.getDurationDays());
        holder.tvPhaseDays.setText(daysText);
        
        // 根据状态设置外观
        updatePhaseAppearance(holder, phase.getStatus());
    }

    /**
     * 根据阶段状态更新外观
     */
    private void updatePhaseAppearance(ViewHolder holder, String status) {
        int iconRes;
        int iconTint;
        int bgRes;
        String statusText;
        int statusTextColor;
        
        switch (status) {
            case StudyPhaseEntity.STATUS_COMPLETED:
                iconRes = R.drawable.ic_check_circle;
                iconTint = R.color.success;
                bgRes = R.drawable.bg_phase_status_completed;
                statusText = "已完成";
                statusTextColor = R.color.success;
                break;
                
            case StudyPhaseEntity.STATUS_IN_PROGRESS:
                iconRes = R.drawable.ic_status_active;
                iconTint = R.color.primary_blue;
                bgRes = R.drawable.bg_phase_status_active;
                statusText = "进行中";
                statusTextColor = R.color.primary_blue;
                break;
                
            case StudyPhaseEntity.STATUS_NOT_STARTED:
            default:
                iconRes = R.drawable.ic_time;
                iconTint = R.color.text_secondary;
                bgRes = R.drawable.bg_phase_status_pending;
                statusText = "未开始";
                statusTextColor = R.color.text_secondary;
                break;
        }
        
        // 设置状态图标
        holder.ivPhaseStatus.setImageResource(iconRes);
        holder.ivPhaseStatus.setColorFilter(ContextCompat.getColor(context, iconTint));
        
        // 设置状态背景
        holder.viewPhaseStatusBg.setBackgroundResource(bgRes);
        
        // 设置状态文字
        holder.tvPhaseStatus.setText(statusText);
        holder.tvPhaseStatus.setTextColor(ContextCompat.getColor(context, statusTextColor));
        
        // 根据状态调整文字颜色
        if (StudyPhaseEntity.STATUS_NOT_STARTED.equals(status)) {
            holder.tvPhaseName.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.tvPhaseGoal.setTextColor(ContextCompat.getColor(context, R.color.text_hint));
        } else {
            holder.tvPhaseName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.tvPhaseGoal.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }
    }

    @Override
    public int getItemCount() {
        return phases != null ? phases.size() : 0;
    }

    /**
     * 更新数据
     */
    public void updateData(List<StudyPhaseEntity> newPhases) {
        this.phases = newPhases;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewPhaseStatusBg;
        ImageView ivPhaseStatus;
        TextView tvPhaseName;
        TextView tvPhaseStatus;
        TextView tvPhaseGoal;
        ProgressBar progressPhase;
        TextView tvPhaseProgress;
        TextView tvPhaseDays;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPhaseStatusBg = itemView.findViewById(R.id.view_phase_status_bg);
            ivPhaseStatus = itemView.findViewById(R.id.iv_phase_status);
            tvPhaseName = itemView.findViewById(R.id.tv_phase_name);
            tvPhaseStatus = itemView.findViewById(R.id.tv_phase_status);
            tvPhaseGoal = itemView.findViewById(R.id.tv_phase_goal);
            progressPhase = itemView.findViewById(R.id.progress_phase);
            tvPhaseProgress = itemView.findViewById(R.id.tv_phase_progress);
            tvPhaseDays = itemView.findViewById(R.id.tv_phase_days);
        }
    }
}
