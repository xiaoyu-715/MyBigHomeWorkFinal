package com.example.mybighomework;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.DailyTaskDetailAdapter;
import com.example.mybighomework.adapter.PhaseProgressAdapter;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.StudyPhaseDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.repository.StudyPlanRepository;
import com.example.mybighomework.service.ProgressSyncServiceYSJ;
import com.example.mybighomework.service.TaskGenerationService;
import com.example.mybighomework.utils.StudyStatisticsHelper;
import com.example.mybighomework.utils.TaskTemplateValidator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 学习计划详情页
 * 显示计划概览、今日任务和阶段进度
 * 
 * Requirements: 3.1, 3.2, 3.3, 4.1
 */
public class PlanDetailActivity extends AppCompatActivity implements 
        DailyTaskDetailAdapter.OnTaskCompletionListener {

    public static final String EXTRA_PLAN_ID = "plan_id";
    
    // Views
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvPlanTitle;
    private TextView tvPlanStatus;
    private TextView tvProgressPercent;
    private ProgressBar progressTotal;
    private TextView tvStreakDays;
    private TextView tvWeeklyTime;
    private TextView tvCompletedTasks;
    private TextView tvTodayTaskCount;
    private RecyclerView rvDailyTasks;
    private LinearLayout layoutEmptyTasks;
    private TextView tvEmptyTasksHint;
    private TextView tvCurrentPhase;
    private RecyclerView rvPhases;
    private LinearLayout layoutEmptyPhases;
    private CardView cardCompletion;
    private TextView tvCompletionStats;
    private Button btnCompleteToday;

    // Data
    private int planId;
    private StudyPlanRepository repository;
    private TaskGenerationService taskGenerationService;
    private ProgressSyncServiceYSJ progressSyncService;
    private StudyPlanEntity currentPlan;
    private List<StudyPhaseEntity> phases = new ArrayList<>();
    private List<DailyTaskEntity> todayTasks = new ArrayList<>();
    
    // Adapters
    private DailyTaskDetailAdapter taskAdapter;
    private PhaseProgressAdapter phaseAdapter;
    
    // Date format
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);
        
        // 获取计划ID
        planId = getIntent().getIntExtra(EXTRA_PLAN_ID, -1);
        if (planId == -1) {
            Toast.makeText(this, "无效的计划ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 初始化Repository和Services
        AppDatabase database = AppDatabase.getInstance(this);
        repository = new StudyPlanRepository(
            this.getApplication(),
            database.studyPlanDao(),
            database.studyPhaseDao(),
            database.dailyTaskDao()
        );
        taskGenerationService = new TaskGenerationService(this);
        progressSyncService = new ProgressSyncServiceYSJ(this);
        
        // 初始化视图
        initViews();
        
        // 设置RecyclerView
        setupRecyclerViews();
        
        // 确保今日任务存在，然后加载数据
        ensureTodayTasksAndLoadDetails();
    }
    
    private void initViews() {
        // 顶部栏
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        
        // 概览卡片
        tvPlanTitle = findViewById(R.id.tv_plan_title);
        tvPlanStatus = findViewById(R.id.tv_plan_status);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        progressTotal = findViewById(R.id.progress_total);
        tvStreakDays = findViewById(R.id.tv_streak_days);
        tvWeeklyTime = findViewById(R.id.tv_weekly_time);
        tvCompletedTasks = findViewById(R.id.tv_completed_tasks);
        
        // 今日任务区域
        tvTodayTaskCount = findViewById(R.id.tv_today_task_count);
        rvDailyTasks = findViewById(R.id.rv_daily_tasks);
        layoutEmptyTasks = findViewById(R.id.layout_empty_tasks);
        tvEmptyTasksHint = findViewById(R.id.tv_empty_tasks_hint);
        
        // 阶段进度区域
        tvCurrentPhase = findViewById(R.id.tv_current_phase);
        rvPhases = findViewById(R.id.rv_phases);
        layoutEmptyPhases = findViewById(R.id.layout_empty_phases);
        
        // 完成祝贺卡片
        cardCompletion = findViewById(R.id.card_completion);
        tvCompletionStats = findViewById(R.id.tv_completion_stats);
        
        // 底部按钮
        btnCompleteToday = findViewById(R.id.btn_complete_today);
        
        // 设置点击事件
        ivBack.setOnClickListener(v -> finish());
        btnCompleteToday.setOnClickListener(v -> completeAllTodayTasks());
    }
    
    private void setupRecyclerViews() {
        // 今日任务列表
        taskAdapter = new DailyTaskDetailAdapter(this, todayTasks, this);
        rvDailyTasks.setLayoutManager(new LinearLayoutManager(this));
        rvDailyTasks.setAdapter(taskAdapter);
        
        // 阶段进度列表
        phaseAdapter = new PhaseProgressAdapter(this, phases);
        rvPhases.setLayoutManager(new LinearLayoutManager(this));
        rvPhases.setAdapter(phaseAdapter);
    }
    
    /**
     * 确保今日任务存在，然后加载计划详情
     * 在打开计划时自动检查并生成今日任务
     * 
     * Requirements: 6.1, 6.2, 6.4
     */
    private void ensureTodayTasksAndLoadDetails() {
        // 首先确保今日任务存在
        taskGenerationService.ensureTodayTasksExist(planId, 
            new TaskGenerationService.OnTasksGeneratedListener() {
                @Override
                public void onTasksGenerated(List<DailyTaskEntity> tasks, boolean isNewlyGenerated) {
                    if (isNewlyGenerated && !tasks.isEmpty()) {
                        // 显示更详细的任务生成提示
                        String message = String.format("✅ 已生成%d个今日学习任务", tasks.size());
                        Toast.makeText(PlanDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                    // 加载完整的计划详情
                    loadPlanDetails();
                }
                
                @Override
                public void onError(Exception e) {
                    // 显示重试对话框
                    showTaskGenerationFailedDialog(e);
                }
            });
    }

    private void showTaskGenerationFailedDialog(Exception e) {
        if (isFinishing()) return;
        
        new AlertDialog.Builder(this)
            .setTitle("任务生成失败")
            .setMessage("原因：" + e.getMessage() + "\n\n是否重试？")
            .setPositiveButton("重试", (dialog, which) -> {
                ensureTodayTasksAndLoadDetails();
            })
            .setNegativeButton("稍后", (dialog, which) -> {
                loadPlanDetails(); // 尝试加载现有数据
            })
            .setCancelable(false)
            .show();
    }
    
    private void loadPlanDetails() {
        repository.getPlanWithDetailsAsync(planId, new StudyPlanRepository.OnPlanDetailsLoadedListener() {
            @Override
            public void onPlanDetailsLoaded(StudyPlanRepository.PlanWithDetails planWithDetails) {
                currentPlan = planWithDetails.getPlan();
                phases.clear();
                phases.addAll(planWithDetails.getPhases());
                
                // 获取今日任务
                String today = dateFormat.format(new Date());
                todayTasks.clear();
                todayTasks.addAll(planWithDetails.getTasksForDate(today));
                
                // 更新UI
                updateUI();
            }
            
            @Override
            public void onError(Exception e) {
                Toast.makeText(PlanDetailActivity.this, 
                    "加载计划详情失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    
    private void updateUI() {
        if (currentPlan == null) return;
        
        // 更新标题
        tvPlanTitle.setText(currentPlan.getTitle());
        tvTitle.setText(currentPlan.getTitle());
        
        // 更新状态
        tvPlanStatus.setText(currentPlan.getStatus());
        updateStatusBadge(currentPlan.getStatus());
        
        // 更新进度
        int progress = currentPlan.getProgress();
        tvProgressPercent.setText(progress + "%");
        progressTotal.setProgress(progress);
        
        // 更新统计数据
        tvStreakDays.setText(String.valueOf(currentPlan.getStreakDays()));
        
        // 计算本周学习时长（分钟）
        int weeklyMinutes = currentPlan.getTotalStudyTimeMinutes();
        tvWeeklyTime.setText(String.valueOf(weeklyMinutes));
        
        // 计算已完成任务数
        int completedCount = countCompletedTasks();
        tvCompletedTasks.setText(String.valueOf(completedCount));
        
        // 更新今日任务
        updateTodayTasksUI();
        
        // 更新阶段列表
        updatePhasesUI();
        
        // 检查是否显示完成祝贺
        updateCompletionCard();
        
        // 更新底部按钮状态
        updateBottomButton();
    }
    
    private void updateStatusBadge(String status) {
        int bgRes;
        switch (status) {
            case StudyPlanEntity.STATUS_IN_PROGRESS:
                bgRes = R.drawable.bg_priority_tag;
                break;
            case StudyPlanEntity.STATUS_COMPLETED:
                bgRes = R.drawable.bg_priority_tag_green;
                break;
            case StudyPlanEntity.STATUS_PAUSED:
                bgRes = R.drawable.bg_priority_tag_yellow;
                break;
            default:
                bgRes = R.drawable.bg_phase_status_tag;
                break;
        }
        tvPlanStatus.setBackgroundResource(bgRes);
    }
    
    private int countCompletedTasks() {
        int count = 0;
        for (DailyTaskEntity task : todayTasks) {
            if (task.isCompleted()) {
                count++;
            }
        }
        return count;
    }
    
    private void updateTodayTasksUI() {
        int total = todayTasks.size();
        int completed = countCompletedTasks();
        
        tvTodayTaskCount.setText(completed + "/" + total + " 已完成");
        
        if (total == 0) {
            rvDailyTasks.setVisibility(View.GONE);
            layoutEmptyTasks.setVisibility(View.VISIBLE);
            tvEmptyTasksHint.setText("今日无任务");
        } else {
            rvDailyTasks.setVisibility(View.VISIBLE);
            layoutEmptyTasks.setVisibility(View.GONE);
            taskAdapter.updateData(todayTasks);
        }
    }
    
    private void updatePhasesUI() {
        if (phases.isEmpty()) {
            rvPhases.setVisibility(View.GONE);
            layoutEmptyPhases.setVisibility(View.VISIBLE);
        } else {
            rvPhases.setVisibility(View.VISIBLE);
            layoutEmptyPhases.setVisibility(View.GONE);
            phaseAdapter.updateData(phases);
            
            // 更新当前阶段显示
            StudyPhaseEntity currentPhase = findCurrentPhase();
            if (currentPhase != null) {
                tvCurrentPhase.setText("当前: " + currentPhase.getPhaseName());
            } else {
                tvCurrentPhase.setText("");
            }
        }
    }
    
    private StudyPhaseEntity findCurrentPhase() {
        for (StudyPhaseEntity phase : phases) {
            if (StudyPhaseEntity.STATUS_IN_PROGRESS.equals(phase.getStatus())) {
                return phase;
            }
        }
        return null;
    }
    
    private void updateCompletionCard() {
        if (currentPlan != null && StudyPlanEntity.STATUS_COMPLETED.equals(currentPlan.getStatus())) {
            cardCompletion.setVisibility(View.VISIBLE);
            String stats = String.format(Locale.getDefault(),
                "累计学习 %d 天，完成 %d 个任务",
                currentPlan.getCompletedDays(),
                countCompletedTasks());
            tvCompletionStats.setText(stats);
        } else {
            cardCompletion.setVisibility(View.GONE);
        }
    }
    
    private void updateBottomButton() {
        int total = todayTasks.size();
        int completed = countCompletedTasks();
        
        if (total == 0) {
            btnCompleteToday.setEnabled(false);
            btnCompleteToday.setText("今日无任务");
        } else if (completed == total) {
            btnCompleteToday.setEnabled(false);
            btnCompleteToday.setText("✅ 今日学习已完成");
        } else {
            btnCompleteToday.setEnabled(true);
            btnCompleteToday.setText("✅ 完成今日学习");
        }
    }

    
    /**
     * 处理任务完成状态变化
     * Requirements: 4.1, 4.2
     */
    @Override
    public void onTaskCompletionChanged(DailyTaskEntity task, boolean isCompleted) {
        // 更新任务完成状态
        repository.updateTaskCompletion(
            task.getId(),
            isCompleted,
            isCompleted ? task.getEstimatedMinutes() : 0,
            new StudyPlanRepository.OnTaskCompletionUpdatedListener() {
                @Override
                public void onTaskCompletionUpdated(DailyTaskEntity updatedTask, StudyPlanEntity updatedPlan) {
                    // 更新本地数据
                    if (updatedPlan != null) {
                        currentPlan = updatedPlan;
                    }
                    
                    // 更新任务列表中的任务状态
                    for (int i = 0; i < todayTasks.size(); i++) {
                        if (todayTasks.get(i).getId() == updatedTask.getId()) {
                            todayTasks.set(i, updatedTask);
                            break;
                        }
                    }
                    
                    // 刷新列表UI
                    taskAdapter.updateData(todayTasks);
                    
                    // 使用ProgressSyncService同步进度并检查阶段切换
                    progressSyncService.syncProgressAfterTaskCompletion(updatedTask.getId(), 
                        new ProgressSyncServiceYSJ.OnProgressSyncedListener() {
                            @Override
                            public void onProgressSynced(int phaseProgress, int planProgress, boolean phaseAdvanced) {
                                // 如果触发了阶段切换，重新加载整个页面
                                if (phaseAdvanced) {
                                    Toast.makeText(PlanDetailActivity.this, "恭喜！已自动进入下一阶段", Toast.LENGTH_LONG).show();
                                    loadPlanDetails();
                                    return;
                                }
                                
                                // 否则只更新UI进度
                                if (currentPlan != null) {
                                    currentPlan.setProgress(planProgress);
                                }
                                updateUI();
                                
                                // 检查是否所有任务都完成
                                if (isCompleted && areAllTodayTasksCompleted()) {
                                    showTodayCompletedMessage();
                                }
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                // 即使同步失败，也更新UI
                                updateUI();
                                Toast.makeText(PlanDetailActivity.this, "进度同步失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                }
                
                @Override
                public void onError(Exception e) {
                    Toast.makeText(PlanDetailActivity.this,
                        "更新任务状态失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // 恢复复选框状态
                    taskAdapter.notifyDataSetChanged();
                }
            }
        );
    }
    
    private boolean areAllTodayTasksCompleted() {
        if (todayTasks.isEmpty()) return false;
        for (DailyTaskEntity task : todayTasks) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        return true;
    }
    
    private void showTodayCompletedMessage() {
        Toast.makeText(this, "🎉 太棒了！今日学习任务已全部完成！", Toast.LENGTH_LONG).show();
    }
    
    /**
     * 一键完成所有今日任务
     */
    private void completeAllTodayTasks() {
        if (todayTasks.isEmpty()) return;
        
        // 找出未完成的任务
        List<DailyTaskEntity> incompleteTasks = new ArrayList<>();
        for (DailyTaskEntity task : todayTasks) {
            if (!task.isCompleted()) {
                incompleteTasks.add(task);
            }
        }
        
        if (incompleteTasks.isEmpty()) {
            Toast.makeText(this, "所有任务已完成", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 逐个完成任务
        completeTasksSequentially(incompleteTasks, 0);
    }
    
    private void completeTasksSequentially(List<DailyTaskEntity> tasks, int index) {
        if (index >= tasks.size()) {
            // 所有任务完成，进行批量进度同步
            List<Integer> taskIds = new ArrayList<>();
            for(DailyTaskEntity t : tasks) {
                taskIds.add(t.getId());
            }
            
            progressSyncService.syncProgressAfterBatchCompletion(taskIds, new ProgressSyncServiceYSJ.OnProgressSyncedListener() {
                @Override
                public void onProgressSynced(int phaseProgress, int planProgress, boolean phaseAdvanced) {
                    // 重新加载数据
                    loadPlanDetails();
                    showTodayCompletedMessage();
                    
                    if (phaseAdvanced) {
                        Toast.makeText(PlanDetailActivity.this, "恭喜！已自动进入下一阶段", Toast.LENGTH_LONG).show();
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    // 即使同步失败也刷新页面
                    loadPlanDetails();
                    showTodayCompletedMessage();
                }
            });
            return;
        }
        
        DailyTaskEntity task = tasks.get(index);
        repository.updateTaskCompletion(
            task.getId(),
            true,
            task.getEstimatedMinutes(),
            new StudyPlanRepository.OnTaskCompletionUpdatedListener() {
                @Override
                public void onTaskCompletionUpdated(DailyTaskEntity updatedTask, StudyPlanEntity updatedPlan) {
                    // 继续完成下一个任务
                    completeTasksSequentially(tasks, index + 1);
                }
                
                @Override
                public void onError(Exception e) {
                    Toast.makeText(PlanDetailActivity.this,
                        "完成任务失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回页面时刷新数据，并确保今日任务存在
        if (planId != -1) {
            ensureTodayTasksAndLoadDetails();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            // Do not shutdown repository here as it might be used by other components or if this activity is recreated
            // But if we created it, we should shut it down if it's not shared. 
            // The existing code called shutdown, so we keep it.
            repository.shutdown();
        }
        if (taskGenerationService != null) {
            taskGenerationService.shutdown();
        }
        if (progressSyncService != null) {
            progressSyncService.shutdown();
        }
    }
}
