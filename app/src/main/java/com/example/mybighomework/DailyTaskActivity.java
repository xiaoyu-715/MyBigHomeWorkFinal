package com.example.mybighomework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.utils.ActionTypeInferrer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DailyTaskActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvDate;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayout navHome, navReport, navProfile, navMore;
    private CardView cardTaskSource;
    private TextView tvTaskSourceHint;

    private DailyTaskAdapter adapter;
    private List<DailyTask> taskList;
    private SharedPreferences sharedPreferences;
    private DailyTaskDao dailyTaskDao;
    private boolean useDatabase = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_task);

        initViews();
        initData();
        setupClickListeners();
        updateProgress();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvDate = findViewById(R.id.tv_date);
        tvProgress = findViewById(R.id.tv_progress);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        cardTaskSource = findViewById(R.id.cardTaskSource);
        tvTaskSourceHint = findViewById(R.id.tvTaskSourceHint);
        navHome = findViewById(R.id.nav_home);
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);

        // 设置当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);
        tvDate.setText(sdf.format(new Date()));

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        sharedPreferences = getSharedPreferences("daily_tasks", MODE_PRIVATE);
        taskList = new ArrayList<>();

        // 初始化数据库DAO
        AppDatabase database = AppDatabase.getInstance(this);
        dailyTaskDao = database.dailyTaskDao();

        // 尝试从数据库加载今日任务
        loadTasksFromDatabase();

        if (adapter == null) {
            adapter = new DailyTaskAdapter(taskList, new DailyTaskAdapter.OnTaskClickListener() {
                @Override
                public void onTaskClick(DailyTask task, int position) {
                    handleTaskClick(task, position);
                }

                @Override
                public void onTaskComplete(DailyTask task, int position) {
                    task.setCompleted(!task.isCompleted());
                    saveTaskStatus(task);
                    updateProgress();
                    adapter.notifyItemChanged(position);

                    if (task.isCompleted()) {
                        Toast.makeText(DailyTaskActivity.this, "任务完成！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void handleTaskClick(DailyTask task, int position) {
        if (useDatabase && task.getPlanId() > 0) {
            // 数据库任务：优先使用actionType跳转到对应功能
            String actionType = task.getActionType();
            if (actionType != null && !actionType.isEmpty()) {
                Class<?> targetActivity = ActionTypeInferrer.getTargetActivity(actionType);
                if (targetActivity != null) {
                    String description = ActionTypeInferrer.getActionDescription(actionType);
                    Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, targetActivity));
                    return;
                }
            }
            
            // 如果没有actionType或无法识别，跳转到计划详情
            Intent intent = new Intent(this, PlanDetailActivity.class);
            intent.putExtra(PlanDetailActivity.EXTRA_PLAN_ID, task.getPlanId());
            startActivity(intent);
            Toast.makeText(this, "查看任务所属的学习计划", Toast.LENGTH_SHORT).show();
        } else {
            // 默认任务：优先使用actionType，其次使用type
            String actionType = task.getActionType();
            if (actionType != null && !actionType.isEmpty()) {
                Class<?> targetActivity = ActionTypeInferrer.getTargetActivity(actionType);
                if (targetActivity != null) {
                    String description = ActionTypeInferrer.getActionDescription(actionType);
                    Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, targetActivity));
                    return;
                }
            }
            
            // 兼容旧的type字段
            Intent intent = null;
            switch (task.getType()) {
                case "vocabulary":
                    intent = new Intent(this, VocabularyActivity.class);
                    break;
                case "exam_practice":
                    intent = new Intent(this, MockExamActivity.class);
                    break;
                case "daily_sentence":
                    intent = new Intent(this, DailySentenceActivity.class);
                    break;
                default:
                    Toast.makeText(this, "未知任务类型", Toast.LENGTH_SHORT).show();
                    return;
            }

            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // 底部导航点击事件
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        navMore.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoreActivity.class);
            startActivity(intent);
        });
    }

    private void updateProgress() {
        int completedTasks = 0;
        for (DailyTask task : taskList) {
            if (task.isCompleted()) {
                completedTasks++;
            }
        }

        int totalTasks = taskList.size();
        tvProgress.setText(completedTasks + "/" + totalTasks);
        progressBar.setMax(totalTasks);
        progressBar.setProgress(completedTasks);
    }

    private void saveTaskStatus(DailyTask task) {
        if (useDatabase && task.getTaskId() > 0) {
            // 保存到数据库
            new Thread(() -> {
                try {
                    DailyTaskEntity entity = dailyTaskDao.getTaskById(task.getTaskId());
                    if (entity != null) {
                        entity.setCompleted(task.isCompleted());
                        if (task.isCompleted()) {
                            entity.setCompletedAt(System.currentTimeMillis());
                        } else {
                            entity.setCompletedAt(0);
                        }
                        dailyTaskDao.update(entity);
                    }
                } catch (Exception e) {
                    Log.e("DailyTaskActivity", "保存任务状态到数据库失败", e);
                }
            }).start();
        } else {
            // 保存到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            editor.putBoolean(today + "_" + task.getType(), task.isCompleted());
            editor.apply();
        }
    }

    private void loadTasksFromDatabase() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            try {
                // 获取所有活跃计划的今日任务
                // 首先查询所有活跃的学习计划
                AppDatabase database = AppDatabase.getInstance(this);
                List<StudyPlanEntity> activePlans = database.studyPlanDao().getActivePlans();

                List<DailyTaskEntity> dbTasks = new ArrayList<>();
                // 在后台线程中预先获取计划名称映射
                Map<Integer, String> planNames = new java.util.HashMap<>();
                
                if (activePlans != null && !activePlans.isEmpty()) {
                    // 获取每个活跃计划的今日任务，同时构建计划名称映射
                    for (StudyPlanEntity plan : activePlans) {
                        planNames.put(plan.getId(), plan.getTitle() != null && !plan.getTitle().trim().isEmpty() 
                                ? plan.getTitle() : "学习计划");
                        List<DailyTaskEntity> planTasks = dailyTaskDao.getTasksByDate(plan.getId(), today);
                        if (planTasks != null) {
                            dbTasks.addAll(planTasks);
                        }
                    }
                }

                // 将数据传递给UI线程
                final List<DailyTaskEntity> finalDbTasks = dbTasks;
                final Map<Integer, String> finalPlanNames = planNames;

                runOnUiThread(() -> {
                    if (finalDbTasks != null && !finalDbTasks.isEmpty()) {
                        // 从数据库加载任务
                        useDatabase = true;
                        taskList.clear();

                        for (DailyTaskEntity entity : finalDbTasks) {
                            String planName = finalPlanNames.getOrDefault(entity.getPlanId(), "学习计划");
                            String title = "[" + planName + "] " + entity.getTaskContent();
                            String description = "预计" + entity.getEstimatedMinutes() + "分钟";

                            // 使用包含智能任务完成字段的构造函数
                            DailyTask task = new DailyTask(
                                    title,
                                    description,
                                    "task_" + entity.getId(),
                                    entity.isCompleted(),
                                    entity.getActionType(),
                                    entity.getCompletionType(),
                                    entity.getCompletionTarget(),
                                    entity.getCurrentProgress()
                            );
                            task.setTaskId(entity.getId());
                            task.setPlanId(entity.getPlanId());
                            taskList.add(task);
                            
                            Log.d("DailyTaskActivity", "加载任务: " + entity.getTaskContent() + 
                                  ", actionType=" + entity.getActionType() + 
                                  ", progress=" + entity.getCurrentProgress() + "/" + entity.getCompletionTarget());
                        }

                        showTaskSourceHint(true, finalDbTasks.size(), finalPlanNames.size());
                    } else {
                        // 数据库中没有任务，使用默认静态任务
                        useDatabase = false;
                        loadDefaultTasks();
                        showTaskSourceHint(false, 0, 0);
                    }

                    setupAdapter();
                    updateProgress();
                });
            } catch (Exception e) {
                Log.e("DailyTaskActivity", "加载数据库任务失败", e);
                runOnUiThread(() -> {
                    // 加载失败，使用默认静态任务
                    useDatabase = false;
                    loadDefaultTasks();
                    showTaskSourceHint(false, 0, 0);
                    setupAdapter();
                    updateProgress();
                });
            }
        }).start();
    }

    private void loadDefaultTasks() {
        taskList.clear();
        // 默认任务也需要设置actionType以支持智能任务完成
        DailyTask vocabTask = new DailyTask("词汇练习", "完成20个单词学习", "vocabulary", false,
                "vocabulary_training", "count", 20, 0);
        DailyTask examTask = new DailyTask("模拟考试练习", "完成20次答题", "exam_practice", false,
                "mock_exam", "count", 20, 0);
        DailyTask sentenceTask = new DailyTask("每日一句练习", "打开学习页面", "daily_sentence", false,
                "daily_sentence", "simple", 1, 0);
        
        taskList.add(vocabTask);
        taskList.add(examTask);
        taskList.add(sentenceTask);

        // 从SharedPreferences加载任务完成状态
        loadTaskStatusFromPrefs();
    }

    private void loadTaskStatusFromPrefs() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (DailyTask task : taskList) {
            boolean isCompleted = sharedPreferences.getBoolean(today + "_" + task.getType(), false);
            task.setCompleted(isCompleted);
        }
    }

    private void setupAdapter() {
        if (adapter == null) {
            adapter = new DailyTaskAdapter(taskList, new DailyTaskAdapter.OnTaskClickListener() {
                @Override
                public void onTaskClick(DailyTask task, int position) {
                    handleTaskClick(task, position);
                }

                @Override
                public void onTaskComplete(DailyTask task, int position) {
                    task.setCompleted(!task.isCompleted());
                    saveTaskStatus(task);
                    updateProgress();
                    adapter.notifyItemChanged(position);

                    if (task.isCompleted()) {
                        Toast.makeText(DailyTaskActivity.this, "任务完成！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private String getPlanName(int planId) {
        try {
            AppDatabase database = AppDatabase.getInstance(this);
            StudyPlanEntity plan = database.studyPlanDao().getStudyPlanById(planId);
            if (plan != null) {
                String title = plan.getTitle();
                if (title != null && !title.trim().isEmpty()) {
                    return title;
                }
            }
            // 兜底：不再暴露planId数字，使用通用名称
            return "学习计划";
        } catch (Exception e) {
            Log.e("DailyTaskActivity", "获取计划名称失败", e);
            return "学习计划";
        }
    }

    private void showTaskSourceHint(boolean fromDatabase, int taskCount, int planCount) {
        if (cardTaskSource == null || tvTaskSourceHint == null) {
            return;
        }

        cardTaskSource.setVisibility(View.VISIBLE);

        if (fromDatabase) {
            String hint = "这里汇总了您" + planCount + "个学习计划的今日任务（共" + taskCount + "个）。\n" +
                    "点击任务可查看所属计划的详细信息。";
            tvTaskSourceHint.setText(hint);
        } else {
            String hint = "您还没有创建学习计划，这里显示的是默认任务。\n" +
                    "建议使用AI助手生成个性化学习计划！";
            tvTaskSourceHint.setText(hint);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他Activity返回时，重新加载任务状态
        loadTasksFromDatabase();
        updateProgress();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}