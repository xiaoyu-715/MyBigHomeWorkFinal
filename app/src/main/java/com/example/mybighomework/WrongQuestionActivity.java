package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.WrongQuestionEntity;
import com.example.mybighomework.repository.WrongQuestionRepository;

public class WrongQuestionActivity extends AppCompatActivity {

    // UI组件
    private ImageView btnBack, btnClearAll;
    private TextView tvTotalWrong, tvMasteredCount, tvAccuracyRate;
    private Button btnFilterAll, btnFilterVocabulary, btnFilterGrammar, btnFilterReading;
    private Button btnPracticeWrong, btnExportWrong;
    private RecyclerView rvWrongQuestions;
    private LinearLayout layoutEmpty;
    private LinearLayout navHome, navReport, navProfile, navMore;

    // 数据相关
    private List<WrongQuestionEntity> wrongQuestions;
    private List<WrongQuestionEntity> filteredQuestions;
    private WrongQuestionAdapter adapter;
    private String currentFilter = "全部";
    private WrongQuestionRepository wrongQuestionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_wrong_question);
            
            initViews();
            initDatabase();
            initData();
            setupClickListeners();
            setupBackPressedCallback();
            setupRecyclerView();
            loadWrongQuestions();
        } catch (Exception e) {
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initViews() {
        try {
            // 顶部控件
            btnBack = findViewById(R.id.btn_back);
            btnClearAll = findViewById(R.id.btn_clear_all);
            
            // 统计信息
            tvTotalWrong = findViewById(R.id.tv_total_wrong);
            tvMasteredCount = findViewById(R.id.tv_mastered_count);
            tvAccuracyRate = findViewById(R.id.tv_accuracy_rate);
            
            // 筛选按钮
            btnFilterAll = findViewById(R.id.btn_filter_all);
            btnFilterVocabulary = findViewById(R.id.btn_filter_vocabulary);
            btnFilterGrammar = findViewById(R.id.btn_filter_grammar);
            btnFilterReading = findViewById(R.id.btn_filter_reading);
            
            // 操作按钮
            btnPracticeWrong = findViewById(R.id.btn_practice_wrong);
            btnExportWrong = findViewById(R.id.btn_export_wrong);
            
            // 列表和空状态
            rvWrongQuestions = findViewById(R.id.rv_wrong_questions);
            layoutEmpty = findViewById(R.id.layout_empty);
            
            // 底部导航
            navHome = findViewById(R.id.nav_home);
            navReport = findViewById(R.id.nav_report);
            navProfile = findViewById(R.id.nav_profile);
            navMore = findViewById(R.id.nav_more);
            
            // 检查关键组件
            if (btnBack == null || rvWrongQuestions == null || layoutEmpty == null) {
                Toast.makeText(this, "布局初始化失败", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "初始化视图时发生错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initDatabase() {
        wrongQuestionRepository = new WrongQuestionRepository(AppDatabase.getInstance(this).wrongQuestionDao());
    }
    
    private void loadWrongQuestions() {
        wrongQuestionRepository.getAllWrongQuestions(entities -> {
            runOnUiThread(() -> {
                if (entities != null) {
                    wrongQuestions.clear();
                    wrongQuestions.addAll(entities);
                    updateStatistics();
                    filterQuestions(currentFilter);
                }
            });
        });
    }

    private void initData() {
        wrongQuestions = new ArrayList<>();
        filteredQuestions = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new WrongQuestionAdapter(this, filteredQuestions, new WrongQuestionAdapter.OnItemActionListener() {
            @Override
            public void onShowExplanation(WrongQuestionEntity question) {
                // 显示解析逻辑已在adapter中处理
            }

            @Override
            public void onMarkMastered(WrongQuestionEntity question) {
                question.setMastered(true);
                wrongQuestionRepository.updateWrongQuestion(question);
                adapter.notifyDataSetChanged();
                updateStatistics();
                Toast.makeText(WrongQuestionActivity.this, "已标记为掌握", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemove(WrongQuestionEntity question) {
                showRemoveConfirmDialog(question);
            }
        });
        
        rvWrongQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvWrongQuestions.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
        
        // 清空所有错题
        btnClearAll.setOnClickListener(v -> showClearAllConfirmDialog());
        
        // 筛选按钮
        btnFilterAll.setOnClickListener(v -> filterQuestions("全部"));
        btnFilterVocabulary.setOnClickListener(v -> filterQuestions("词汇训练"));
        btnFilterGrammar.setOnClickListener(v -> filterQuestions("真题练习"));
        btnFilterReading.setOnClickListener(v -> filterQuestions("模拟考试"));
        
        // 错题练习
        btnPracticeWrong.setOnClickListener(v -> startWrongQuestionPractice());
        
        // 导出错题
        btnExportWrong.setOnClickListener(v -> exportWrongQuestions());
        
        // 底部导航
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(WrongQuestionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        
        navReport.setOnClickListener(v -> {
            Intent intent = new Intent(WrongQuestionActivity.this, ReportActivity.class);
            startActivity(intent);
            finish();
        });
        
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(WrongQuestionActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
        
        navMore.setOnClickListener(v -> {
            Intent intent = new Intent(WrongQuestionActivity.this, MoreActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void filterQuestions(String category) {
        currentFilter = category;
        filteredQuestions.clear();
        
        if ("全部".equals(category)) {
            filteredQuestions.addAll(wrongQuestions);
        } else {
            for (WrongQuestionEntity question : wrongQuestions) {
                if (category.equals(question.getCategory())) {
                    filteredQuestions.add(question);
                }
            }
        }
        
        // 更新筛选按钮状态
        updateFilterButtonStates(category);
        
        // 更新列表显示
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        // 显示/隐藏空状态
        updateEmptyState();
    }

    private void updateFilterButtonStates(String activeFilter) {
        // 重置所有按钮状态
        btnFilterAll.setBackgroundResource(R.drawable.button_secondary_background);
        btnFilterVocabulary.setBackgroundResource(R.drawable.button_secondary_background);
        btnFilterGrammar.setBackgroundResource(R.drawable.button_secondary_background);
        btnFilterReading.setBackgroundResource(R.drawable.button_secondary_background);
        
        // 设置激活按钮状态
        switch (activeFilter) {
            case "全部":
                btnFilterAll.setBackgroundResource(R.drawable.button_primary_background);
                break;
            case "词汇训练":
                btnFilterVocabulary.setBackgroundResource(R.drawable.button_primary_background);
                break;
            case "真题练习":
                btnFilterGrammar.setBackgroundResource(R.drawable.button_primary_background);
                break;
            case "模拟考试":
                btnFilterReading.setBackgroundResource(R.drawable.button_primary_background);
                break;
        }
    }

    private void updateStatistics() {
        int totalWrong = wrongQuestions.size();
        int masteredCount = 0;
        
        for (WrongQuestionEntity question : wrongQuestions) {
            if (question.isMastered()) {
                masteredCount++;
            }
        }
        
        tvTotalWrong.setText(String.valueOf(totalWrong));
        tvMasteredCount.setText(String.valueOf(masteredCount));
        
        double accuracyRate = totalWrong > 0 ? (double) masteredCount / totalWrong * 100 : 0;
        tvAccuracyRate.setText(String.format("%.1f%%", accuracyRate));
    }

    private void updateEmptyState() {
        if (filteredQuestions.isEmpty()) {
            rvWrongQuestions.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvWrongQuestions.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showRemoveConfirmDialog(WrongQuestionEntity question) {
        new AlertDialog.Builder(this)
            .setTitle("移除错题")
            .setMessage("确定要移除这道错题吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                wrongQuestionRepository.deleteWrongQuestionById(question.getId());
                wrongQuestions.remove(question);
                filteredQuestions.remove(question);
                adapter.notifyDataSetChanged();
                updateStatistics();
                updateEmptyState();
                Toast.makeText(this, "已移除错题", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void showClearAllConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("清空错题本")
            .setMessage("确定要清空所有错题吗？此操作不可恢复。")
            .setPositiveButton("确定", (dialog, which) -> {
                wrongQuestionRepository.deleteAllWrongQuestions();
                wrongQuestions.clear();
                filteredQuestions.clear();
                adapter.notifyDataSetChanged();
                updateStatistics();
                updateEmptyState();
                Toast.makeText(this, "已清空错题本", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void startWrongQuestionPractice() {
        if (filteredQuestions.isEmpty()) {
            Toast.makeText(this, "当前没有可练习的错题", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示错题练习选项
        showPracticeOptionsDialog();
    }
    
    /**
     * 显示错题练习选项对话框
     */
    private void showPracticeOptionsDialog() {
        String[] options = {
            "顺序练习 (按时间顺序)",
            "随机练习 (打乱顺序)"
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择练习模式\n\n共有 " + filteredQuestions.size() + " 道错题可供练习")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // 顺序练习
                        startPracticeMode("sequential");
                        break;
                    case 1: // 随机练习
                        startPracticeMode("random");
                        break;
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 启动错题练习
     */
    private void startPracticeMode(String mode) {
        Intent intent = new Intent(this, WrongQuestionPracticeActivity.class);
        intent.putExtra("practice_mode", mode);
        intent.putExtra("category_filter", currentFilter);
        startActivity(intent);
    }

    private void exportWrongQuestions() {
        if (wrongQuestions.isEmpty()) {
            Toast.makeText(this, "没有错题可导出", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示导出选项对话框
        String[] options = {"导出为文本文件", "分享错题列表"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("导出错题")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // 导出为文本文件
                        exportToTextFile();
                        break;
                    case 1: // 分享错题列表
                        shareWrongQuestions();
                        break;
                }
            })
            .show();
    }
    
    /**
     * 导出错题到文本文件
     */
    private void exportToTextFile() {
        new Thread(() -> {
            try {
                // 创建导出目录
                File exportDir = new File(getExternalFilesDir(null), "WrongQuestions");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                // 生成文件名(包含时间戳)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String fileName = "错题本_" + sdf.format(new Date()) + ".txt";
                File exportFile = new File(exportDir, fileName);
                
                // 写入文件
                FileWriter writer = new FileWriter(exportFile);
                writer.write("=== 我的错题本 ===\n");
                writer.write("导出时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "\n");
                writer.write("错题总数: " + wrongQuestions.size() + "\n");
                writer.write("==================\n\n");
                
                int index = 1;
                for (WrongQuestionEntity question : wrongQuestions) {
                    writer.write("【题目 " + index + "】\n");
                    writer.write("类型: " + (question.getCategory() != null ? question.getCategory() : "未分类") + "\n");
                    writer.write("题目: " + question.getQuestionText() + "\n");
                    
                    // 格式化用户答案
                    String userAnswer = question.getUserAnswerIndex() >= 0 && 
                                      question.getOptions() != null && 
                                      question.getUserAnswerIndex() < question.getOptions().length
                                      ? question.getOptions()[question.getUserAnswerIndex()]
                                      : "未作答";
                    writer.write("我的答案: " + userAnswer + "\n");
                    
                    // 格式化正确答案
                    String correctAnswer = question.getCorrectAnswerIndex() >= 0 && 
                                         question.getOptions() != null && 
                                         question.getCorrectAnswerIndex() < question.getOptions().length
                                         ? question.getOptions()[question.getCorrectAnswerIndex()]
                                         : "未知";
                    writer.write("正确答案: " + correctAnswer + "\n");
                    
                    if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
                        writer.write("解析: " + question.getExplanation() + "\n");
                    }
                    
                    if (question.getWrongTime() != null) {
                        writer.write("错误时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(question.getWrongTime()) + "\n");
                    }
                    writer.write("\n" + "=" + "=".repeat(40) + "\n\n");
                    index++;
                }
                
                writer.close();
                
                // 在主线程显示成功提示
                runOnUiThread(() -> {
                    Toast.makeText(this, "导出成功!\n位置: " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                });
                
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    /**
     * 分享错题列表
     */
    private void shareWrongQuestions() {
        // 构建分享文本
        StringBuilder shareText = new StringBuilder();
        shareText.append("我的错题本 (").append(wrongQuestions.size()).append("题)\n");
        shareText.append("==================\n\n");
        
        int index = 1;
        int maxShare = Math.min(wrongQuestions.size(), 10); // 最多分享10题
        for (int i = 0; i < maxShare; i++) {
            WrongQuestionEntity question = wrongQuestions.get(i);
            shareText.append("【题目 ").append(index).append("】\n");
            shareText.append("类型: ").append(question.getCategory() != null ? question.getCategory() : "未分类").append("\n");
            shareText.append("题目: ").append(question.getQuestionText()).append("\n");
            
            // 格式化正确答案
            String correctAnswer = question.getCorrectAnswerIndex() >= 0 && 
                                 question.getOptions() != null && 
                                 question.getCorrectAnswerIndex() < question.getOptions().length
                                 ? question.getOptions()[question.getCorrectAnswerIndex()]
                                 : "未知";
            shareText.append("正确答案: ").append(correctAnswer).append("\n\n");
            index++;
        }
        
        if (wrongQuestions.size() > maxShare) {
            shareText.append("... 还有 ").append(wrongQuestions.size() - maxShare).append(" 道错题\n");
        }
        
        // 创建分享Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "我的错题本");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        
        try {
            startActivity(Intent.createChooser(shareIntent, "分享错题"));
        } catch (Exception e) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWrongQuestions();
    }
}