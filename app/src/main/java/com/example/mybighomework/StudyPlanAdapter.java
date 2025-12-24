package com.example.mybighomework;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.dao.StudyPhaseDao;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.repository.StudyPlanRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 学习计划列表适配器
 * 显示计划卡片，包含当前阶段、今日任务完成情况和剩余天数
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5
 */
public class StudyPlanAdapter extends RecyclerView.Adapter<StudyPlanAdapter.ViewHolder> {

    private Context context;
    private List<StudyPlan> studyPlanList;
    private StudyPlanRepository studyPlanRepository;
    private StudyPhaseDao studyPhaseDao;
    private DailyTaskDao dailyTaskDao;
    private ExecutorService executorService;
    
    // 缓存当前阶段和任务信息
    private Map<Integer, StudyPhaseEntity> currentPhaseCache = new HashMap<>();
    private Map<Integer, int[]> todayTaskCache = new HashMap<>(); // [completed, total]
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public StudyPlanAdapter(Context context, List<StudyPlan> studyPlanList, StudyPlanRepository repository) {
        this.context = context;
        this.studyPlanList = studyPlanList;
        this.studyPlanRepository = repository;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 设置DAO用于加载阶段和任务信息
     * Requirements: 7.1, 7.2
     */
    public void setDaos(StudyPhaseDao studyPhaseDao, DailyTaskDao dailyTaskDao) {
        this.studyPhaseDao = studyPhaseDao;
        this.dailyTaskDao = dailyTaskDao;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_study_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyPlan plan = studyPlanList.get(position);
        
        // 设置基本信息
        holder.tvPlanTitle.setText(plan.getTitle());
        holder.tvPlanCategory.setText(plan.getCategory());
        holder.tvPlanDescription.setText(plan.getDescription());
        holder.tvPlanTime.setText(plan.getTimeRange());
        holder.tvPlanDuration.setText(plan.getDuration());
        holder.tvPlanStatus.setText(plan.getStatus());
        
        // 设置优先级
        holder.tvPriority.setText(plan.getPriority());
        setPriorityBackground(holder.tvPriority, plan.getPriority());
        
        // 设置进度
        holder.progressPlan.setProgress(plan.getProgress());
        holder.tvProgressText.setText(plan.getProgress() + "%");
        
        // 设置状态指示器颜色
        setStatusIndicatorColor(holder.viewStatusIndicator, plan.getStatus());
        
        // 设置状态图标和文字颜色
        setStatusAppearance(holder.tvPlanStatus, plan.getStatus());
        
        // 设置按钮文字
        setContinueButtonText(holder.btnContinuePlan, plan.getStatus());
        
        // 计算并显示剩余天数 (Requirements: 7.3)
        displayRemainingDays(holder, plan);
        
        // 异步加载当前阶段信息 (Requirements: 7.1)
        loadCurrentPhase(holder, plan);
        
        // 异步加载今日任务完成情况 (Requirements: 7.2)
        loadTodayTasks(holder, plan);
        
        // 设置点击事件 - 跳转到详情页 (Requirements: 7.5)
        holder.itemView.setOnClickListener(v -> {
            navigateToPlanDetail(plan);
        });
        
        holder.btnContinuePlan.setOnClickListener(v -> {
            // 跳转到详情页
            navigateToPlanDetail(plan);
        });

        holder.ivPlanMenu.setOnClickListener(v -> {
            // TODO: 实现更多操作菜单
        });
        
        // 删除按钮点击事件
        holder.ivDeletePlan.setOnClickListener(v -> {
            showDeleteConfirmDialog(plan, position);
        });
    }
    
    /**
     * 跳转到计划详情页
     * Requirements: 7.5
     */
    private void navigateToPlanDetail(StudyPlan plan) {
        Intent intent = new Intent(context, PlanDetailActivity.class);
        intent.putExtra(PlanDetailActivity.EXTRA_PLAN_ID, plan.getId());
        context.startActivity(intent);
    }
    
    /**
     * 计算并显示剩余天数
     * Requirements: 7.3
     */
    private void displayRemainingDays(ViewHolder holder, StudyPlan plan) {
        String timeRange = plan.getTimeRange();
        if (timeRange == null || !timeRange.contains("至")) {
            holder.tvRemainingDays.setVisibility(View.GONE);
            return;
        }
        
        try {
            String[] dates = timeRange.split(" 至 ");
            if (dates.length == 2) {
                Date endDate = dateFormat.parse(dates[1].trim());
                Date today = new Date();
                
                if (endDate != null) {
                    long diffInMillis = endDate.getTime() - today.getTime();
                    long remainingDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
                    
                    if (remainingDays < 0) {
                        holder.tvRemainingDays.setText("已过期");
                        holder.tvRemainingDays.setTextColor(ContextCompat.getColor(context, R.color.error));
                    } else if (remainingDays == 0) {
                        holder.tvRemainingDays.setText("今天截止");
                        holder.tvRemainingDays.setTextColor(ContextCompat.getColor(context, R.color.warning));
                    } else {
                        holder.tvRemainingDays.setText("剩余" + remainingDays + "天");
                        holder.tvRemainingDays.setTextColor(ContextCompat.getColor(context, R.color.info));
                    }
                    holder.tvRemainingDays.setVisibility(View.VISIBLE);
                    return;
                }
            }
        } catch (ParseException e) {
            // 解析失败，隐藏剩余天数
        }
        holder.tvRemainingDays.setVisibility(View.GONE);
    }
    
    /**
     * 异步加载当前阶段信息
     * Requirements: 7.1
     */
    private void loadCurrentPhase(ViewHolder holder, StudyPlan plan) {
        int planId = plan.getId();
        
        // 先检查缓存
        if (currentPhaseCache.containsKey(planId)) {
            displayCurrentPhase(holder, currentPhaseCache.get(planId));
            return;
        }
        
        // 如果没有DAO，隐藏阶段信息
        if (studyPhaseDao == null) {
            holder.layoutCurrentPhase.setVisibility(View.GONE);
            return;
        }
        
        // 异步加载
        executorService.execute(() -> {
            try {
                StudyPhaseEntity currentPhase = studyPhaseDao.getCurrentPhase(planId);
                currentPhaseCache.put(planId, currentPhase);
                
                // 在主线程更新UI
                ((android.app.Activity) context).runOnUiThread(() -> {
                    displayCurrentPhase(holder, currentPhase);
                });
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    holder.layoutCurrentPhase.setVisibility(View.GONE);
                });
            }
        });
    }
    
    /**
     * 显示当前阶段信息
     */
    private void displayCurrentPhase(ViewHolder holder, StudyPhaseEntity phase) {
        if (phase == null) {
            holder.layoutCurrentPhase.setVisibility(View.GONE);
            return;
        }
        
        holder.layoutCurrentPhase.setVisibility(View.VISIBLE);
        holder.tvCurrentPhase.setText("当前阶段：" + phase.getPhaseName());
        holder.tvPhaseProgress.setText(phase.getCompletedDays() + "/" + phase.getDurationDays() + "天");
    }
    
    /**
     * 异步加载今日任务完成情况
     * Requirements: 7.2, 7.4
     */
    private void loadTodayTasks(ViewHolder holder, StudyPlan plan) {
        int planId = plan.getId();
        
        // 先检查缓存
        if (todayTaskCache.containsKey(planId)) {
            int[] taskCounts = todayTaskCache.get(planId);
            displayTodayTasks(holder, taskCounts[0], taskCounts[1]);
            return;
        }
        
        // 如果没有DAO，隐藏任务信息
        if (dailyTaskDao == null) {
            holder.layoutTodayTasks.setVisibility(View.GONE);
            return;
        }
        
        // 异步加载
        String today = dateFormat.format(new Date());
        executorService.execute(() -> {
            try {
                int completedCount = dailyTaskDao.getCompletedTaskCount(planId, today);
                int totalCount = dailyTaskDao.getTotalTaskCount(planId, today);
                todayTaskCache.put(planId, new int[]{completedCount, totalCount});
                
                // 在主线程更新UI
                ((android.app.Activity) context).runOnUiThread(() -> {
                    displayTodayTasks(holder, completedCount, totalCount);
                });
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    holder.layoutTodayTasks.setVisibility(View.GONE);
                });
            }
        });
    }
    
    /**
     * 显示今日任务完成情况
     * Requirements: 7.2, 7.4
     */
    private void displayTodayTasks(ViewHolder holder, int completedCount, int totalCount) {
        if (totalCount == 0) {
            holder.layoutTodayTasks.setVisibility(View.GONE);
            return;
        }
        
        holder.layoutTodayTasks.setVisibility(View.VISIBLE);
        holder.tvTodayTaskCount.setText(completedCount + "/" + totalCount + " 已完成");
        
        // 根据完成情况设置颜色和图标
        if (completedCount == totalCount) {
            // 全部完成
            holder.tvTodayTaskStatus.setText("今日任务");
            holder.tvTodayTaskCount.setTextColor(ContextCompat.getColor(context, R.color.success));
            holder.ivTaskIcon.setColorFilter(ContextCompat.getColor(context, R.color.success));
            holder.layoutTodayTasks.setBackgroundResource(R.drawable.bg_daily_task);
        } else if (completedCount > 0) {
            // 部分完成
            holder.tvTodayTaskStatus.setText("今日任务");
            holder.tvTodayTaskCount.setTextColor(ContextCompat.getColor(context, R.color.info));
            holder.ivTaskIcon.setColorFilter(ContextCompat.getColor(context, R.color.info));
            holder.layoutTodayTasks.setBackgroundResource(R.drawable.bg_daily_task);
        } else {
            // 未开始 - 突出显示提醒用户 (Requirements: 7.4)
            holder.tvTodayTaskStatus.setText("今日任务待完成");
            holder.tvTodayTaskCount.setTextColor(ContextCompat.getColor(context, R.color.warning));
            holder.ivTaskIcon.setColorFilter(ContextCompat.getColor(context, R.color.warning));
            holder.layoutTodayTasks.setBackgroundResource(R.drawable.bg_task_status_pending);
        }
    }

    @Override
    public int getItemCount() {
        return studyPlanList.size();
    }

    public void updateData(List<StudyPlan> newList) {
        this.studyPlanList = newList;
        // 清除缓存
        currentPhaseCache.clear();
        todayTaskCache.clear();
        notifyDataSetChanged();
    }
    
    /**
     * 清除缓存，强制重新加载数据
     */
    public void clearCache() {
        currentPhaseCache.clear();
        todayTaskCache.clear();
    }

    private void setPriorityBackground(TextView textView, String priority) {
        int backgroundRes;
        switch (priority) {
            case "高":
                backgroundRes = R.drawable.bg_priority_tag;
                break;
            case "中":
                backgroundRes = R.drawable.bg_priority_tag_yellow;
                break;
            case "低":
                backgroundRes = R.drawable.bg_priority_tag_green;
                break;
            default:
                backgroundRes = R.drawable.bg_priority_tag;
                break;
        }
        textView.setBackgroundResource(backgroundRes);
    }

    private void setStatusIndicatorColor(View view, String status) {
        int color;
        switch (status) {
            case "进行中":
                color = ContextCompat.getColor(context, R.color.primary_blue);
                break;
            case "已完成":
                color = ContextCompat.getColor(context, R.color.success);
                break;
            case "已暂停":
                color = ContextCompat.getColor(context, R.color.warning);
                break;
            case "即将完成":
                color = ContextCompat.getColor(context, R.color.info);
                break;
            default:
                color = ContextCompat.getColor(context, R.color.text_secondary);
                break;
        }
        view.setBackgroundColor(color);
    }

    private void setStatusAppearance(TextView textView, String status) {
        int textColor;
        int iconRes;
        
        switch (status) {
            case "进行中":
                textColor = ContextCompat.getColor(context, R.color.primary_blue);
                iconRes = R.drawable.ic_status_active;
                break;
            case "已完成":
                textColor = ContextCompat.getColor(context, R.color.success);
                iconRes = R.drawable.ic_status_completed;
                break;
            case "已暂停":
                textColor = ContextCompat.getColor(context, R.color.warning);
                iconRes = R.drawable.ic_status_paused;
                break;
            case "即将完成":
                textColor = ContextCompat.getColor(context, R.color.info);
                iconRes = R.drawable.ic_status_active;
                break;
            default:
                textColor = ContextCompat.getColor(context, R.color.text_secondary);
                iconRes = R.drawable.ic_status_active;
                break;
        }
        
        textView.setTextColor(textColor);
        textView.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
    }

    private void setContinueButtonText(Button button, String status) {
        String buttonText;
        switch (status) {
            case "进行中":
                buttonText = "继续学习";
                break;
            case "已完成":
                buttonText = "查看详情";
                break;
            case "已暂停":
                buttonText = "恢复学习";
                break;
            case "即将完成":
                buttonText = "完成计划";
                break;
            default:
                buttonText = "开始学习";
                break;
        }
        button.setText(buttonText);
    }
    
    // 状态变化监听接口
    public interface OnStatusChangeListener {
        void onStatusChanged();
    }
    
    private OnStatusChangeListener onStatusChangeListener;
    
    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.onStatusChangeListener = listener;
    }
    
    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(StudyPlan plan, int position) {
        new android.app.AlertDialog.Builder(context)
            .setTitle("删除计划")
            .setMessage("确定要删除「" + plan.getTitle() + "」吗？\n\n此操作将同时删除计划的所有阶段和任务数据，且无法恢复。")
            .setPositiveButton("删除", (dialog, which) -> {
                deletePlan(plan, position);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 删除计划
     */
    private void deletePlan(StudyPlan plan, int position) {
        studyPlanRepository.deleteStudyPlanAsync(plan, new StudyPlanRepository.OnPlanDeletedListener() {
            @Override
            public void onPlanDeleted() {
                studyPlanList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, studyPlanList.size());
                
                android.widget.Toast.makeText(context, 
                    "✅ 已删除计划：" + plan.getTitle(), 
                    android.widget.Toast.LENGTH_SHORT).show();
                
                // 通知状态变化
                if (onStatusChangeListener != null) {
                    onStatusChangeListener.onStatusChanged();
                }
            }
            
            @Override
            public void onError(Exception e) {
                android.widget.Toast.makeText(context, 
                    "❌ 删除失败：" + e.getMessage(), 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 释放资源
     */
    public void release() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusIndicator;
        TextView tvPlanTitle, tvPlanCategory, tvPlanDescription;
        TextView tvPlanTime, tvPlanDuration, tvPriority;
        TextView tvProgressText, tvPlanStatus;
        ProgressBar progressPlan;
        Button btnContinuePlan;
        ImageView ivPlanMenu, ivDeletePlan;
        
        // 新增的视图
        LinearLayout layoutCurrentPhase;
        TextView tvCurrentPhase, tvPhaseProgress;
        LinearLayout layoutTodayTasks;
        ImageView ivTaskIcon;
        TextView tvTodayTaskStatus, tvTodayTaskCount;
        TextView tvRemainingDays;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvPlanTitle = itemView.findViewById(R.id.tv_plan_title);
            tvPlanCategory = itemView.findViewById(R.id.tv_plan_category);
            tvPlanDescription = itemView.findViewById(R.id.tv_plan_description);
            tvPlanTime = itemView.findViewById(R.id.tv_plan_time);
            tvPlanDuration = itemView.findViewById(R.id.tv_plan_duration);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            tvProgressText = itemView.findViewById(R.id.tv_progress_text);
            tvPlanStatus = itemView.findViewById(R.id.tv_plan_status);
            progressPlan = itemView.findViewById(R.id.progress_plan);
            btnContinuePlan = itemView.findViewById(R.id.btn_continue_plan);
            ivPlanMenu = itemView.findViewById(R.id.iv_plan_menu);
            ivDeletePlan = itemView.findViewById(R.id.iv_delete_plan);
            
            // 新增的视图绑定
            layoutCurrentPhase = itemView.findViewById(R.id.layout_current_phase);
            tvCurrentPhase = itemView.findViewById(R.id.tv_current_phase);
            tvPhaseProgress = itemView.findViewById(R.id.tv_phase_progress);
            layoutTodayTasks = itemView.findViewById(R.id.layout_today_tasks);
            ivTaskIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTodayTaskStatus = itemView.findViewById(R.id.tv_today_task_status);
            tvTodayTaskCount = itemView.findViewById(R.id.tv_today_task_count);
            tvRemainingDays = itemView.findViewById(R.id.tv_remaining_days);
        }
    }
}
