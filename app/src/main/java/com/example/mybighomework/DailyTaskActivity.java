package com.example.mybighomework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTaskActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvDate;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayout navHome, navReport, navProfile, navMore;
    
    private DailyTaskAdapter adapter;
    private List<DailyTask> taskList;
    private SharedPreferences sharedPreferences;
    
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
        
        // 创建今日任务列表（只保留三个核心任务）
        taskList.add(new DailyTask("词汇练习", "完成20个单词学习", "vocabulary", false));
        taskList.add(new DailyTask("模拟考试练习", "完成20次答题", "exam_practice", false));
        taskList.add(new DailyTask("每日一句练习", "打开学习页面", "daily_sentence", false));
        
        // 从SharedPreferences加载任务完成状态
        loadTaskStatus();
        
        adapter = new DailyTaskAdapter(taskList, new DailyTaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(DailyTask task, int position) {
                handleTaskClick(task, position);
            }
            
            @Override
            public void onTaskComplete(DailyTask task, int position) {
                task.setCompleted(!task.isCompleted());
                saveTaskStatus();
                updateProgress();
                adapter.notifyItemChanged(position);
                
                if (task.isCompleted()) {
                    Toast.makeText(DailyTaskActivity.this, "任务完成！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        recyclerView.setAdapter(adapter);
    }
    
    private void handleTaskClick(DailyTask task, int position) {
        Intent intent = null;
        
        switch (task.getType()) {
            case "vocabulary":
                intent = new Intent(this, VocabularyActivity.class);
                break;
            case "exam_practice":
                // 跳转到模拟考试
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
    
    private void saveTaskStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        for (int i = 0; i < taskList.size(); i++) {
            DailyTask task = taskList.get(i);
            editor.putBoolean(today + "_" + task.getType(), task.isCompleted());
        }
        editor.apply();
    }
    
    private void loadTaskStatus() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        for (DailyTask task : taskList) {
            boolean isCompleted = sharedPreferences.getBoolean(today + "_" + task.getType(), false);
            task.setCompleted(isCompleted);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他Activity返回时，重新加载任务状态
        loadTaskStatus();
        updateProgress();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}