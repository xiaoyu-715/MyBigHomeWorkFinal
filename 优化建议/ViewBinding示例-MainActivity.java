// ==================== ViewBinding 优化示例 ====================
// 文件：MainActivity.java
// 说明：展示如何将 MainActivity 从 findViewById 迁移到 ViewBinding

package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mybighomework.databinding.ActivityMainBinding;  // ⭐ ViewBinding 类（自动生成）
import com.example.mybighomework.database.entity.UserSettingsEntity;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    // ✅ 使用 ViewBinding 替代所有 View 变量
    private ActivityMainBinding binding;
    
    // ViewModel 和 Repository
    private MainViewModel viewModel;
    private UserSettingsRepository userSettingsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // ✅ ViewBinding 初始化（替代 setContentView + findViewById）
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        userSettingsRepository = new UserSettingsRepository(this);
        
        setupClickListeners();
        observeViewModel();
        updateTaskProgress();
    }
    
    // ==================== 对比：优化前 vs 优化后 ====================
    
    /* ❌ 优化前的代码（繁琐且易出错）：
    
    private LinearLayout navReport;
    private LinearLayout navProfile;
    private LinearLayout navMore;
    private LinearLayout llVocabulary;
    private LinearLayout llRealExam;
    private LinearLayout llMockExam;
    // ... 还有很多变量
    
    private void initViews() {
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);
        llVocabulary = findViewById(R.id.ll_vocabulary);
        llRealExam = findViewById(R.id.ll_real_exam);
        llMockExam = findViewById(R.id.ll_mock_exam);
        // ... 还有很多 findViewById 调用
        
        tvStudyDays = findViewById(R.id.tv_study_days);
        tvVocabularyCount = findViewById(R.id.tv_vocabulary_count);
        tvExamScore = findViewById(R.id.tv_exam_score);
    }
    */
    
    // ✅ 优化后：不需要 initViews()，也不需要声明变量！
    // 直接使用 binding.navReport, binding.tvStudyDays 等
    
    private void setupClickListeners() {
        // ✅ 直接访问 View，类型安全，自动补全
        
        // 词汇训练点击事件
        binding.llVocabulary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VocabularyActivity.class);
            startActivity(intent);
        });
        
        // 真题练习点击事件
        binding.llRealExam.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExamPracticeActivity.class);
            startActivity(intent);
        });
        
        // 模拟考试点击事件
        binding.llMockExam.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MockExamActivity.class);
            startActivity(intent);
        });
        
        // 错题本点击事件
        binding.llErrorBook.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WrongQuestionActivity.class);
            startActivity(intent);
        });
        
        // 学习计划点击事件
        binding.llStudyPlan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudyPlanActivity.class);
            startActivity(intent);
        });
        
        // 每日一句点击事件
        binding.llDailySentence.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DailySentenceActivity.class);
            startActivity(intent);
        });
        
        // 每日任务点击事件
        binding.llDailyTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DailyTaskActivity.class);
            startActivity(intent);
        });
        
        // 底部导航栏
        binding.navReport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
        });
        
        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        
        binding.navMore.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MoreActivity.class);
            startActivity(intent);
        });
    }
    
    private void observeViewModel() {
        // ✅ 观察词汇掌握数量
        viewModel.getMasteredVocabularyCount().observe(this, count -> {
            if (count != null) {
                binding.tvVocabularyCount.setText(String.valueOf(count));  // ✅ 直接使用 binding
            } else {
                binding.tvVocabularyCount.setText("0");
            }
        });
        
        // 获取学习天数
        loadUserSettingsAsync();
        
        // 获取平均考试分数
        viewModel.getAverageExamScore(new MainViewModel.OnResultListener<Double>() {
            @Override
            public void onSuccess(Double result) {
                runOnUiThread(() -> {
                    if (result != null && result > 0) {
                        binding.tvExamScore.setText(String.valueOf(result.intValue()));
                    } else {
                        binding.tvExamScore.setText("--");
                    }
                });
            }
            
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> binding.tvExamScore.setText("--"));
            }
        });
    }
    
    private void loadUserSettingsAsync() {
        new Thread(() -> {
            try {
                UserSettingsEntity settings = userSettingsRepository.getUserSettings();
                runOnUiThread(() -> {
                    if (settings != null) {
                        binding.tvStudyDays.setText(String.valueOf(settings.getStudyStreak()));
                    } else {
                        binding.tvStudyDays.setText("0");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> binding.tvStudyDays.setText("0"));
            }
        }).start();
    }
    
    private void updateTaskProgress() {
        // ✅ 示例：更新任务进度
        int completedTasks = 2;
        int totalTasks = 5;
        String progress = completedTasks + "/" + totalTasks;
        binding.tvTaskProgress.setText(progress);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateTaskProgress();
        loadUserSettingsAsync();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ 防止内存泄漏
        binding = null;
    }
}

// ==================== ViewBinding 优势对比 ====================

/*
📊 代码量对比：

优化前：
- View 变量声明：~15 行
- findViewById 调用：~15 行
- 总计：~30 行重复代码

优化后：
- View 变量声明：0 行
- findViewById 调用：0 行
- 总计：只需 1 行初始化 binding
- 减少代码量：~30 行 → ~1 行（减少 97%）

✅ 优势：
1. 类型安全：编译时检查，避免 ClassCastException
2. Null 安全：如果 View 不存在，编译失败而不是运行时崩溃
3. 自动补全：IDE 自动提示所有可用的 View
4. 代码简洁：不需要声明变量和 findViewById
5. 易于重构：重命名 View ID 时自动更新代码

❌ 优化前的问题：
1. 容易写错 ID（运行时才发现）
2. 需要手动类型转换
3. 可能返回 null 导致 NPE
4. 大量重复代码
5. 难以维护

⚡ 性能：
- ViewBinding 比 findViewById 快（使用直接引用而非查找）
- 编译时生成，运行时零开销
*/

