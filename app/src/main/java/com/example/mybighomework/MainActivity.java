package com.example.mybighomework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.UserSettingsEntity;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;
import com.example.mybighomework.viewmodel.MainViewModel;
import com.example.mybighomework.utils.QuestionDataInitializer;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navReport;
    private LinearLayout navProfile;
    private LinearLayout navMore;
    private LinearLayout llVocabulary;
    private LinearLayout llRealExam;
    private LinearLayout llMockExam;
    private LinearLayout llErrorBook;
    private LinearLayout llStudyPlan;
    private LinearLayout llDailySentence;
    private LinearLayout llDailyTask;
    private LinearLayout llCameraTranslation;
    private LinearLayout llAiAssistant;
    private LinearLayout llAutoGlmAssistant;
    private LinearLayout llTextTranslation;
    private LinearLayout llVocabularyBook;
    private TextView tvTaskProgress;
    private ImageButton btnWordSearch;

    // 学习进度相关的TextView
    private TextView tvStudyDays;
    private TextView tvVocabularyCount;
    private TextView tvExamScore;
    
    // ViewModel（推荐使用）
    private MainViewModel viewModel;
    
    // Repository实例（保留用于其他功能）
    private UserSettingsRepository userSettingsRepository;
    private VocabularyRecordRepository vocabularyRecordRepository;
    private ExamRecordRepository examRecordRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // 初始化 ViewModel（自动管理生命周期）
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        // 初始化 Repository（用于其他功能）
        userSettingsRepository = new UserSettingsRepository(this);
        AppDatabase database = AppDatabase.getInstance(this);
        vocabularyRecordRepository = new VocabularyRecordRepository(database.vocabularyDao());
        examRecordRepository = new ExamRecordRepository(database.examDao());
        
        // 初始化题目数据（首次运行或数据更新时）
        QuestionDataInitializer.initializeIfNeeded(getApplication());
        
        // 修复旧任务的actionType字段（用于智能任务完成系统）
        AppDatabase.fixOldTasksActionType(this);
        
        initViews();
        setupClickListeners();
        observeViewModel(); // 观察 ViewModel 的数据变化
        updateTaskProgress();
        // loadStudyProgressData(); // 不再需要手动调用，LiveData 会自动更新
    }
    
    private void initViews() {
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);
        llVocabulary = findViewById(R.id.ll_vocabulary);
        llRealExam = findViewById(R.id.ll_real_exam);
        llMockExam = findViewById(R.id.ll_mock_exam);
        llErrorBook = findViewById(R.id.ll_error_book);
        llStudyPlan = findViewById(R.id.ll_study_plan);
        llDailySentence = findViewById(R.id.ll_daily_sentence);
        llDailyTask = findViewById(R.id.ll_daily_task);
        llCameraTranslation = findViewById(R.id.ll_camera_translation);
        llAiAssistant = findViewById(R.id.ll_ai_assistant);
        llAutoGlmAssistant = findViewById(R.id.ll_autoglm_assistant);
        llTextTranslation = findViewById(R.id.ll_text_translation);
        llVocabularyBook = findViewById(R.id.ll_vocabulary_book);
        tvTaskProgress = findViewById(R.id.tv_task_progress);
        btnWordSearch = findViewById(R.id.btn_word_search);
        
        // 学习进度相关的TextView
        tvStudyDays = findViewById(R.id.tv_study_days);
        tvVocabularyCount = findViewById(R.id.tv_vocabulary_count);
        tvExamScore = findViewById(R.id.tv_exam_score);
    }
    
    private void setupClickListeners() {
        // 词汇训练点击事件
        llVocabulary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查是否有上次学习的词书
                String lastBookId = BookSelectionActivityYSJ.getLastSelectedBookId(MainActivity.this);
                String lastBookName = BookSelectionActivityYSJ.getLastSelectedBookName(MainActivity.this);
                
                if (lastBookId != null && !lastBookId.isEmpty()) {
                    // 继续学习上次的词书
                    Intent intent = new Intent(MainActivity.this, VocabularyActivity.class);
                    intent.putExtra(VocabularyActivity.EXTRA_SOURCE_TYPE, VocabularyActivity.SOURCE_TYPE_BOOK);
                    intent.putExtra(VocabularyActivity.EXTRA_BOOK_ID, lastBookId);
                    intent.putExtra(VocabularyActivity.EXTRA_BOOK_NAME, lastBookName);
                    intent.putExtra(VocabularyActivity.EXTRA_MODE, "learn");
                    startActivity(intent);
                } else {
                    // 首次使用,跳转到分类导航界面
                    Intent intent = new Intent(MainActivity.this, BookCategoryActivityYSJ.class);
                    startActivity(intent);
                }
            }
        });
        
        // 真题练习点击事件
        llRealExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到试卷列表界面
                Intent intent = new Intent(MainActivity.this, ExamListActivity.class);
                startActivity(intent);
            }
        });
        
        // 模拟考试点击事件
        llMockExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MockExamActivity.class);
                startActivity(intent);
            }
        });
        
        // 学习报告导航点击事件
        navReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
            }
        });
        
        // 个人中心导航点击事件
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        
        // 更多功能导航点击事件
        navMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MoreActivity.class);
                startActivity(intent);
            }
        });
        
        // 错题本点击事件
        llErrorBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WrongQuestionActivity.class);
                startActivity(intent);
            }
        });
        
        // 学习计划点击事件
        llStudyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StudyPlanActivity.class);
                startActivity(intent);
            }
        });
        
        // 每日一句点击事件
        llDailySentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DailySentenceActivity.class);
                startActivity(intent);
            }
        });
        
        // 今日任务点击事件
        llDailyTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DailyTaskActivity.class);
                startActivity(intent);
            }
        });

        // 拍照翻译点击事件
        llCameraTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraTranslationActivity.class);
                startActivity(intent);
            }
        });

        // AI学习助手点击事件
        llAiAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AIChatActivity.class);
                startActivity(intent);
            }
        });

        // 输入翻译点击事件
        llTextTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TextTranslationActivity.class);
                startActivity(intent);
            }
        });

        // AutoGLM智能助手点击事件
        llAutoGlmAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.example.mybighomework.autoglm.ui.AIAssistantActivity.class);
                startActivity(intent);
            }
        });

        // 单词书点击事件
        llVocabularyBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VocabularyBookActivity.class);
                startActivity(intent);
            }
        });

        // 单词搜索点击事件
        btnWordSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WordSearchActivityYSJ.class);
                startActivity(intent);
            }
        });
    }
    
    private void updateTaskProgress() {
        // 在后台线程中从数据库查询任务完成状态
        new Thread(() -> {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String today = sdf.format(new java.util.Date());
                
                AppDatabase database = AppDatabase.getInstance(this);
                com.example.mybighomework.database.dao.DailyTaskDao taskDao = database.dailyTaskDao();
                
                // 获取所有活跃计划
                java.util.List<com.example.mybighomework.database.entity.StudyPlanEntity> activePlans = 
                        database.studyPlanDao().getActivePlans();
                
                int totalTasks = 0;
                int completedTasks = 0;
                
                if (activePlans != null && !activePlans.isEmpty()) {
                    // 从数据库获取今日任务统计
                    for (com.example.mybighomework.database.entity.StudyPlanEntity plan : activePlans) {
                        totalTasks += taskDao.getTotalTaskCount(plan.getId(), today);
                        completedTasks += taskDao.getCompletedTaskCount(plan.getId(), today);
                    }
                }
                
                // 如果数据库中没有任务，使用SharedPreferences作为备选（兼容默认任务）
                if (totalTasks == 0) {
                    SharedPreferences sharedPreferences = getSharedPreferences("daily_tasks", MODE_PRIVATE);
                    String[] taskTypes = {"vocabulary", "exam_practice", "daily_sentence"};
                    totalTasks = taskTypes.length;
                    completedTasks = 0;
                    for (String taskType : taskTypes) {
                        if (sharedPreferences.getBoolean(today + "_" + taskType, false)) {
                            completedTasks++;
                        }
                    }
                }
                
                final int finalTotal = totalTasks;
                final int finalCompleted = completedTasks;
                
                runOnUiThread(() -> {
                    tvTaskProgress.setText(finalCompleted + "/" + finalTotal);
                });
            } catch (Exception e) {
                e.printStackTrace();
                // 出错时显示默认值
                runOnUiThread(() -> {
                    tvTaskProgress.setText("0/0");
                });
            }
        }).start();
    }
    
    private void loadStudyProgressData() {
        // 在后台线程中加载数据
        new Thread(() -> {
            try {
                // 获取用户设置数据
                UserSettingsEntity userSettings = null;
                int masteredVocabularyCount = 0;
                double averageScore = 0.0;
                
                try {
                    userSettings = userSettingsRepository.getUserSettings();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                try {
                    masteredVocabularyCount = vocabularyRecordRepository.getMasteredVocabularyCount();
                } catch (Exception e) {
                    e.printStackTrace();
                    masteredVocabularyCount = 0;
                }
                
                try {
                    averageScore = examRecordRepository.getAverageScore();
                    // 检查是否为NaN或无穷大
                    if (Double.isNaN(averageScore) || Double.isInfinite(averageScore)) {
                        averageScore = 0.0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    averageScore = 0.0;
                }
                
                // 在主线程中更新UI
                final UserSettingsEntity finalUserSettings = userSettings;
                final int finalMasteredCount = masteredVocabularyCount;
                final double finalAverageScore = averageScore;
                
                runOnUiThread(() -> {
                    try {
                        if (finalUserSettings != null) {
                            // 显示学习连续天数
                            tvStudyDays.setText(String.valueOf(finalUserSettings.getStudyStreak()));
                        } else {
                            tvStudyDays.setText("0");
                        }
                        
                        // 显示词汇掌握量
                        tvVocabularyCount.setText(String.valueOf(finalMasteredCount));
                        
                        // 显示平均考试成绩
                        if (finalAverageScore > 0) {
                            tvExamScore.setText(String.valueOf((int) finalAverageScore));
                        } else {
                            tvExamScore.setText("--");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 如果UI更新出错，设置默认值
                        tvStudyDays.setText("0");
                        tvVocabularyCount.setText("0");
                        tvExamScore.setText("--");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // 如果出错，在主线程中显示默认值
                runOnUiThread(() -> {
                    try {
                        tvStudyDays.setText("0");
                        tvVocabularyCount.setText("0");
                        tvExamScore.setText("--");
                    } catch (Exception uiException) {
                        uiException.printStackTrace();
                    }
                });
            }
        }).start();
    }
    
    /**
     * 观察 ViewModel 的数据变化
     * LiveData 会自动在后台线程查询数据，在主线程更新UI
     * ✅ 推荐使用这个方法替代 loadStudyProgressData()
     */
    private void observeViewModel() {
        // 观察词汇掌握数量（LiveData 自动异步查询和更新）
        viewModel.getMasteredVocabularyCount().observe(this, count -> {
            if (count != null) {
                tvVocabularyCount.setText(String.valueOf(count));
            } else {
                tvVocabularyCount.setText("0");
            }
        });
        
        // 获取学习天数（使用异步方法）
        loadUserSettingsAsync();
        
        // 获取平均考试分数（使用异步方法）
        viewModel.getAverageExamScore(new MainViewModel.OnResultListener<Double>() {
            @Override
            public void onSuccess(Double result) {
                runOnUiThread(() -> {
                    if (result != null && result > 0) {
                        tvExamScore.setText(String.valueOf(result.intValue()));
                    } else {
                        tvExamScore.setText("--");
                    }
                });
            }
            
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvExamScore.setText("--"));
            }
        });
    }
    
    /**
     * 异步加载用户设置
     */
    private void loadUserSettingsAsync() {
        new Thread(() -> {
            try {
                UserSettingsEntity settings = userSettingsRepository.getUserSettings();
                runOnUiThread(() -> {
                    if (settings != null) {
                        tvStudyDays.setText(String.valueOf(settings.getStudyStreak()));
                    } else {
                        tvStudyDays.setText("0");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvStudyDays.setText("0"));
            }
        }).start();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他Activity返回时，刷新所有学习进度数据
        refreshAllStudyProgress();
    }
    
    /**
     * 刷新所有学习进度数据
     */
    private void refreshAllStudyProgress() {
        // 更新任务进度
        updateTaskProgress();
        
        // 刷新学习天数
        loadUserSettingsAsync();
        
        // 由于使用了LiveData，词汇掌握量会自动更新
        // 但为了确保及时性，我们可以手动触发一次查询
        new Thread(() -> {
            try {
                // 强制刷新词汇掌握量
                int masteredCount = vocabularyRecordRepository.getMasteredVocabularyCount();
                runOnUiThread(() -> {
                    tvVocabularyCount.setText(String.valueOf(masteredCount));
                });
                
                // 强制刷新平均考试分数
                double averageScore = examRecordRepository.getAverageScore();
                if (Double.isNaN(averageScore) || Double.isInfinite(averageScore)) {
                    averageScore = 0.0;
                }
                final double finalScore = averageScore;
                runOnUiThread(() -> {
                    if (finalScore > 0) {
                        tvExamScore.setText(String.valueOf((int) finalScore));
                    } else {
                        tvExamScore.setText("--");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}