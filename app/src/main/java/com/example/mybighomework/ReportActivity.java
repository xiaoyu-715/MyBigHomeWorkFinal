package com.example.mybighomework;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.UserSettingsEntity;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;
import com.example.mybighomework.repository.StudyRecordRepository;
import com.example.mybighomework.database.dao.QuestionDao;
import com.example.mybighomework.database.dao.StudyRecordDao;
import com.example.mybighomework.database.dao.WrongQuestionDao;
import com.example.mybighomework.view.StudyChartView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private LinearLayout navHome;
    private LinearLayout navReport;
    private LinearLayout navProfile;
    private LinearLayout navMore;
    
    // 数据显示控件
    private TextView tvTotalHours;
    private TextView tvStreakDays;
    private TextView tvAverageScore;
    
    // 各模块统计数据显示控件
    private TextView tvVocabularyCount;
    private TextView tvExamCount;
    private TextView tvMockExamCount;
    private TextView tvErrorCount;
    
    // 学习时长图表
    private StudyChartView studyChart;
    
    // Repository实例
    private UserSettingsRepository userSettingsRepository;
    private ExamRecordRepository examRecordRepository;
    private VocabularyRecordRepository vocabularyRecordRepository;
    private StudyRecordRepository studyRecordRepository;
    private QuestionDao questionDao;
    private WrongQuestionDao wrongQuestionDao;
    
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_report), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // 初始化Repository
        AppDatabase database = AppDatabase.getInstance(this);
        executorService = Executors.newFixedThreadPool(3);
        userSettingsRepository = new UserSettingsRepository(this);
        examRecordRepository = new ExamRecordRepository(database.examDao());
        vocabularyRecordRepository = new VocabularyRecordRepository(database.vocabularyDao());
        studyRecordRepository = new StudyRecordRepository(database.studyRecordDao());
        questionDao = database.questionDao();
        wrongQuestionDao = database.wrongQuestionDao();
        
        initViews();
        setupClickListeners();
        loadReportData();
    }
    
    private void initViews() {
        navHome = findViewById(R.id.nav_home);
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);
        
        // 初始化数据显示控件
        tvTotalHours = findViewById(R.id.tv_total_hours);
        tvStreakDays = findViewById(R.id.tv_streak_days);
        tvAverageScore = findViewById(R.id.tv_average_score);
        
        // 初始化各模块统计数据显示控件
        tvVocabularyCount = findViewById(R.id.tv_vocabulary_count);
        tvExamCount = findViewById(R.id.tv_exam_count);
        tvMockExamCount = findViewById(R.id.tv_mock_exam_count);
        tvErrorCount = findViewById(R.id.tv_error_count);
        
        // 初始化学习时长图表
        studyChart = findViewById(R.id.study_chart);
    }
    
    private void setupClickListeners() {
        // 首页导航点击事件
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回主页面
                Intent intent = new Intent(ReportActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        
        // 学习报告导航点击事件（当前页面，无需操作）
        navReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当前就在学习报告页面，无需操作
            }
        });
        
        // 个人中心导航点击事件
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        // 更多功能导航点击事件
        navMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportActivity.this, MoreActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    
    
    private void loadReportData() {
        executorService.execute(() -> {
            try {
                // 获取用户设置数据
                UserSettingsEntity userSettings = userSettingsRepository.getUserSettings();
                
                // 获取学习统计数据
                int studyStreak = userSettings != null ? userSettings.getStudyStreak() : 0;
                int masteredVocabularyCount = vocabularyRecordRepository.getMasteredVocabularyCount();
                double averageScore = examRecordRepository.getAverageScore();
                
                // 获取各模块的真实统计数据（使用ModuleStatisticsManager获取答对次数）
                com.example.mybighomework.utils.ModuleStatisticsManager statsManager = 
                    com.example.mybighomework.utils.ModuleStatisticsManager.getInstance(ReportActivity.this);
                int totalVocabularyCount = statsManager.getVocabularyCorrectCount(); // 词汇训练答对次数
                int totalExamCount = statsManager.getExamPracticeCorrectCount(); // 真题练习答对次数
                int mockExamCount = statsManager.getMockExamCorrectCount(); // 模拟考试答对次数
                // 【修复】使用WrongQuestionDao统计错题数量，与主页面错题本数据一致
                int errorQuestionCount = wrongQuestionDao.getAllWrongQuestions().size();
                
                // 调试日志
                android.util.Log.d("ReportActivity", "词汇训练答对: " + totalVocabularyCount + 
                    ", 真题练习答对: " + totalExamCount + 
                    ", 模拟考试答对: " + mockExamCount + 
                    ", 错题数: " + errorQuestionCount +
                    ", 已掌握词汇: " + masteredVocabularyCount);
                
                // 【统一时间记录】从用户设置中获取总学习时长（包含词汇训练、模拟考试、真题练习）
                double totalHours = userSettingsRepository.getTotalStudyTimeHours();
                if (totalHours == 0 && masteredVocabularyCount > 0) {
                    // 如果总时长为0但有学习记录，可能是旧数据，使用估算值
                    totalHours = masteredVocabularyCount * 0.05; // 每个词汇约5分钟
                }
                final double finalTotalHours = totalHours;
                
                // 获取最近7天的学习时长数据
                List<StudyRecordDao.DailyStudyTime> dailyStudyTimeList = studyRecordRepository.getDailyStudyTime(7);
                
                // 转换为Map格式
                Map<String, Float> chartData = new HashMap<>();
                for (StudyRecordDao.DailyStudyTime dailyTime : dailyStudyTimeList) {
                    chartData.put(dailyTime.date, (float) dailyTime.totalSeconds);
                }
                
                // 在主线程更新UI
                runOnUiThread(() -> {
                    tvStreakDays.setText(String.valueOf(studyStreak));
                    tvTotalHours.setText(String.format("%.1f", finalTotalHours));
                    tvAverageScore.setText(String.format("%.1f", averageScore));
                    
                    // 更新各模块统计数据
                    tvVocabularyCount.setText(String.valueOf(totalVocabularyCount));
                    tvExamCount.setText(String.valueOf(totalExamCount));
                    tvMockExamCount.setText(String.valueOf(mockExamCount));
                    tvErrorCount.setText(String.valueOf(errorQuestionCount));
                    
                    // 更新学习时长图表
                    studyChart.setData(chartData);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                // 在主线程显示默认值
                runOnUiThread(() -> {
                    tvStreakDays.setText("0");
                    tvTotalHours.setText("0.0");
                    tvAverageScore.setText("0.0");
                    
                    // 各模块统计数据默认值
                    tvVocabularyCount.setText("0");
                    tvExamCount.setText("0");
                    tvMockExamCount.setText("0");
                    tvErrorCount.setText("0");
                    
                    // 使用空数据初始化图表
                    studyChart.setData(new HashMap<>());
                });
            }
        });
    }
    
    
    
    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他Activity返回时，重新加载报告数据
        loadReportData();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}