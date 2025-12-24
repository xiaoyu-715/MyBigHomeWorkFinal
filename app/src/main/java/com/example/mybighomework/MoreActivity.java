package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MoreActivity extends AppCompatActivity {

    // 顶部导航
    private ImageView btnBack;
    
    // 学习工具
    private LinearLayout btnVocabulary;
    private LinearLayout btnWrongQuestions;
    private LinearLayout btnStudyPlan;
    
    // 设置与帮助
    private LinearLayout btnAppSettings;
    private LinearLayout btnAIChat;
    private LinearLayout btnHelpFeedback;
    private LinearLayout btnAbout;
    
    // 其他功能
    private LinearLayout btnBackup;
    private LinearLayout btnClearCache;
    
    // 底部导航
    private LinearLayout navHome;
    private LinearLayout navReport;
    private LinearLayout navProfile;
    private LinearLayout navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_more);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // 顶部导航
        btnBack = findViewById(R.id.btn_back);
        
        // 学习工具
        btnVocabulary = findViewById(R.id.btn_vocabulary);
        btnWrongQuestions = findViewById(R.id.btn_wrong_questions);
        btnStudyPlan = findViewById(R.id.btn_study_plan);
        
        // 设置与帮助
        btnAppSettings = findViewById(R.id.btn_app_settings);
        btnAIChat = findViewById(R.id.btn_ai_chat);
        btnHelpFeedback = findViewById(R.id.btn_help_feedback);
        btnAbout = findViewById(R.id.btn_about);
        
        // 其他功能
        btnBackup = findViewById(R.id.btn_backup);
        btnClearCache = findViewById(R.id.btn_clear_cache);
        
        // 底部导航
        navHome = findViewById(R.id.nav_home);
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);
    }

    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
        
        // 学习工具点击事件
        btnVocabulary.setOnClickListener(v -> {
            // 跳转到词汇训练页面
            Intent intent = new Intent(MoreActivity.this, VocabularyActivity.class);
            startActivity(intent);
        });
        
        btnWrongQuestions.setOnClickListener(v -> {
            // 跳转到错题本页面
            Intent intent = new Intent(MoreActivity.this, WrongQuestionActivity.class);
            startActivity(intent);
        });
        
        btnStudyPlan.setOnClickListener(v -> {
            // 跳转到学习计划页面
            Intent intent = new Intent(MoreActivity.this, StudyPlanActivity.class);
            startActivity(intent);
        });
        
        // 设置与帮助点击事件
        btnAppSettings.setOnClickListener(v -> {
            // 跳转到设置页面
            Intent intent = new Intent(MoreActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnAIChat.setOnClickListener(v -> {
            Intent intent = new Intent(MoreActivity.this, AIChatActivity.class);
            startActivity(intent);
        });

        btnHelpFeedback.setOnClickListener(v -> {
            // 显示帮助与反馈对话框
            showHelpFeedbackDialog();
        });
        
        btnAbout.setOnClickListener(v -> {
            showAboutDialog();
        });
        
        // 其他功能点击事件
        btnBackup.setOnClickListener(v -> {
            // 显示数据备份对话框
            showBackupDialog();
        });
        
        btnClearCache.setOnClickListener(v -> {
            showClearCacheDialog();
        });
        
        // 底部导航点击事件
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(MoreActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        
        navReport.setOnClickListener(v -> {
            Intent intent = new Intent(MoreActivity.this, ReportActivity.class);
            startActivity(intent);
            finish();
        });
        
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MoreActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
        
        navMore.setOnClickListener(v -> {
            // 更多功能导航点击事件（当前页面，无需操作）
        });
    }
    
    private void showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("关于应用")
                .setMessage("英语学习助手 v2.0\n\n这是一个专为英语学习者设计的应用，提供单词学习、真题练习、模拟考试、学习报告、AI辅导等功能。\n\n✨ 主要功能：\n• 词汇训练（含发音）\n• 真题练习系统\n• 模拟考试\n• 错题本管理\n• AI学习助手\n• 学习数据分析\n• 每日一句\n• 拍照翻译\n\n开发者：学习团队")
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    /**
     * 显示帮助与反馈对话框
     */
    private void showHelpFeedbackDialog() {
        String[] options = {"使用帮助", "意见反馈", "联系我们"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("帮助与反馈")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // 使用帮助
                        showHelpDialog();
                        break;
                    case 1: // 意见反馈
                        showFeedbackDialog();
                        break;
                    case 2: // 联系我们
                        showContactDialog();
                        break;
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 显示使用帮助
     */
    private void showHelpDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("使用帮助")
                .setMessage("📖 快速入门：\n\n" +
                        "1. 词汇训练：点击主页的词汇训练，开始学习单词\n" +
                        "2. 真题练习：选择考研英语真题进行练习\n" +
                        "3. 模拟考试：参加四级模拟考试\n" +
                        "4. 学习报告：查看学习数据和进度\n" +
                        "5. AI助手：使用AI进行英语辅导\n\n" +
                        "💡 提示：\n" +
                        "• 坚持每日学习可提高连续天数\n" +
                        "• 错题会自动收录到错题本\n" +
                        "• 可在设置中调整学习目标")
                .setPositiveButton("我知道了", null)
                .show();
    }
    
    /**
     * 显示意见反馈对话框
     */
    private void showFeedbackDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("请输入您的宝贵意见...");
        input.setMinLines(3);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("意见反馈")
                .setView(input)
                .setPositiveButton("提交", (dialog, which) -> {
                    String feedback = input.getText().toString().trim();
                    if (feedback.isEmpty()) {
                        Toast.makeText(this, "请输入反馈内容", Toast.LENGTH_SHORT).show();
                    } else {
                        // 这里可以实现将反馈发送到服务器的逻辑
                        Toast.makeText(this, "感谢您的反馈！我们会认真考虑您的建议。", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    /**
     * 显示联系我们对话框
     */
    private void showContactDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("联系我们")
                .setMessage("📧 邮箱：support@englishlearning.com\n" +
                        "🌐 网站：www.englishlearning.com\n" +
                        "💬 QQ群：123456789\n" +
                        "📱 微信公众号：英语学习助手\n\n" +
                        "工作时间：周一至周五 9:00-18:00")
                .setPositiveButton("确定", null)
                .show();
    }
    
    /**
     * 显示数据备份对话框
     */
    private void showBackupDialog() {
        String[] options = {"导出学习数据", "导出错题本", "查看备份位置"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("数据备份")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // 导出学习数据
                        exportStudyData();
                        break;
                    case 1: // 导出错题本
                        Intent intent = new Intent(MoreActivity.this, WrongQuestionActivity.class);
                        startActivity(intent);
                        Toast.makeText(this, "请在错题本页面点击导出按钮", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: // 查看备份位置
                        showBackupLocation();
                        break;
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 导出学习数据
     */
    private void exportStudyData() {
        Toast.makeText(this, "正在导出学习数据...", Toast.LENGTH_SHORT).show();
        // 这里可以实现导出所有学习数据的功能
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 模拟导出过程
                runOnUiThread(() -> {
                    String exportPath = getExternalFilesDir(null) + "/StudyData";
                    Toast.makeText(this, "学习数据导出成功！\n位置: " + exportPath, Toast.LENGTH_LONG).show();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * 显示备份位置
     */
    private void showBackupLocation() {
        String backupPath = getExternalFilesDir(null).getAbsolutePath();
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("备份位置")
                .setMessage("所有导出的数据都保存在以下位置：\n\n" + backupPath + "\n\n" +
                        "您可以通过文件管理器访问这些文件。")
                .setPositiveButton("确定", null)
                .show();
    }
    
    private void showClearCacheDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("清除缓存")
                .setMessage("确定要清除应用缓存吗？这将删除临时文件，但不会影响您的学习数据。")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 这里可以添加清除缓存的逻辑
                    Toast.makeText(MoreActivity.this, "缓存清除成功", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }
}