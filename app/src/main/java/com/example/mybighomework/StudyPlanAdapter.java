package com.example.mybighomework;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.example.mybighomework.repository.StudyPlanRepository;

public class StudyPlanAdapter extends RecyclerView.Adapter<StudyPlanAdapter.ViewHolder> {

    private Context context;
    private List<StudyPlan> studyPlanList;
    private StudyPlanRepository studyPlanRepository;

    public StudyPlanAdapter(Context context, List<StudyPlan> studyPlanList, StudyPlanRepository repository) {
        this.context = context;
        this.studyPlanList = studyPlanList;
        this.studyPlanRepository = repository;
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
        holder.tvProgressText.setText(getProgressText(plan.getProgress()));
        
        // 设置状态指示器颜色
        setStatusIndicatorColor(holder.viewStatusIndicator, plan.getStatus());
        
        // 设置状态图标和文字颜色
        setStatusAppearance(holder.tvPlanStatus, plan.getStatus());
        
        // 设置按钮文字
        setContinueButtonText(holder.btnContinuePlan, plan.getStatus());
        
        // 设置点击事件
        holder.btnContinuePlan.setOnClickListener(v -> {
            updatePlanStatus(plan, position);
        });

        holder.ivPlanMenu.setOnClickListener(v -> {
            // TODO: 实现更多操作菜单
        });
    }

    @Override
    public int getItemCount() {
        return studyPlanList.size();
    }

    public void updateData(List<StudyPlan> newList) {
        this.studyPlanList = newList;
        notifyDataSetChanged();
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
                backgroundRes = R.drawable.bg_priority_tag_yellow;
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

    private String getProgressText(int progress) {
        return "学习进度                    " + progress + "%";
    }
    
    private void updatePlanStatus(StudyPlan plan, int position) {
        String currentStatus = plan.getStatus();
        
        switch (currentStatus) {
            case "未开始":
                plan.setStatus("进行中");
                plan.setActiveToday(true);
                if (plan.getProgress() == 0) {
                    plan.setProgress(10); // 开始学习，设置初始进度
                }
                break;
            case "进行中":
                // 增加进度
                int currentProgress = plan.getProgress();
                int newProgress = Math.min(currentProgress + 25, 100);
                plan.setProgress(newProgress);
                
                if (newProgress >= 100) {
                    plan.setStatus("已完成");
                    plan.setActiveToday(false);
                }
                break;
            case "已完成":
                // 已完成的计划可以重新开始
                plan.setStatus("进行中");
                plan.setActiveToday(true);
                plan.setProgress(0);
                break;
        }
        
        // 异步保存到数据库
        if (studyPlanRepository != null) {
            studyPlanRepository.updateStudyPlanAsync(plan, new StudyPlanRepository.OnPlanUpdatedListener() {
                @Override
                public void onPlanUpdated() {
                    // 通知数据更新
                    notifyItemChanged(position);
                    
                    // 如果有回调接口，通知Activity更新统计数据
                    if (onStatusChangeListener != null) {
                        onStatusChangeListener.onStatusChanged();
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    // 可以在这里显示错误提示
                }
            });
        }
    }
    
    // 状态变化监听接口
    public interface OnStatusChangeListener {
        void onStatusChanged();
    }
    
    private OnStatusChangeListener onStatusChangeListener;
    
    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.onStatusChangeListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusIndicator;
        TextView tvPlanTitle, tvPlanCategory, tvPlanDescription;
        TextView tvPlanTime, tvPlanDuration, tvPriority;
        TextView tvProgressText, tvPlanStatus;
        ProgressBar progressPlan;
        Button btnContinuePlan;
        ImageView ivPlanMenu;

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
        }
    }
}