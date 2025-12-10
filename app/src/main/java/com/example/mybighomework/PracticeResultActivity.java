package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * 练习结果Activity
 * 功能：
 * - 显示练习统计数据
 * - 提供继续练习、查看错题本等操作
 */
public class PracticeResultActivity extends AppCompatActivity {

    private ImageView ivResultIcon;
    private TextView tvResultTitle, tvResultMessage;
    private TextView tvTotalQuestions, tvCorrectCount, tvWrongCount, tvAccuracy, tvElapsedTime;
    private Button btnContinuePractice, btnViewWrongQuestions, btnBackHome;

    private int totalQuestions;
    private int correctCount;
    private int wrongCount;
    private long elapsedTime;
    private String practiceMode;
    private String categoryFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_result);

        // 获取传递的数据
        Intent intent = getIntent();
        totalQuestions = intent.getIntExtra("total_questions", 0);
        correctCount = intent.getIntExtra("correct_count", 0);
        wrongCount = intent.getIntExtra("wrong_count", 0);
        elapsedTime = intent.getLongExtra("elapsed_time", 0);
        practiceMode = intent.getStringExtra("practice_mode");
        categoryFilter = intent.getStringExtra("category_filter");

        initViews();
        displayResults();
        setupClickListeners();
    }

    private void initViews() {
        ivResultIcon = findViewById(R.id.iv_result_icon);
        tvResultTitle = findViewById(R.id.tv_result_title);
        tvResultMessage = findViewById(R.id.tv_result_message);
        
        tvTotalQuestions = findViewById(R.id.tv_total_questions);
        tvCorrectCount = findViewById(R.id.tv_correct_count);
        tvWrongCount = findViewById(R.id.tv_wrong_count);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvElapsedTime = findViewById(R.id.tv_elapsed_time);
        
        btnContinuePractice = findViewById(R.id.btn_continue_practice);
        btnViewWrongQuestions = findViewById(R.id.btn_view_wrong_questions);
        btnBackHome = findViewById(R.id.btn_back_home);
    }

    private void displayResults() {
        // 计算正确率
        double accuracy = totalQuestions > 0 ? (correctCount * 100.0 / totalQuestions) : 0;
        
        // 设置统计数据
        tvTotalQuestions.setText(String.valueOf(totalQuestions));
        tvCorrectCount.setText(String.valueOf(correctCount));
        tvWrongCount.setText(String.valueOf(wrongCount));
        tvAccuracy.setText(String.format("%.1f%%", accuracy));
        
        // 格式化时间
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        tvElapsedTime.setText(String.format("%d分%d秒", minutes, seconds));
        
        // 根据正确率设置结果图标和文字
        if (accuracy >= 90) {
            ivResultIcon.setImageResource(R.drawable.ic_celebration);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.success));
            tvResultTitle.setText("太棒了！");
            tvResultTitle.setTextColor(ContextCompat.getColor(this, R.color.success));
            tvResultMessage.setText("您的掌握程度非常好，继续保持！");
        } else if (accuracy >= 70) {
            ivResultIcon.setImageResource(R.drawable.ic_check_circle);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.primary));
            tvResultTitle.setText("做得不错！");
            tvResultTitle.setTextColor(ContextCompat.getColor(this, R.color.primary));
            tvResultMessage.setText("继续努力，争取更好的成绩！");
        } else if (accuracy >= 50) {
            ivResultIcon.setImageResource(R.drawable.ic_info);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.warning));
            tvResultTitle.setText("还需努力");
            tvResultTitle.setTextColor(ContextCompat.getColor(this, R.color.warning));
            tvResultMessage.setText("建议再次练习这些错题，加强记忆");
        } else {
            ivResultIcon.setImageResource(R.drawable.ic_error);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.error));
            tvResultTitle.setText("需要加强");
            tvResultTitle.setTextColor(ContextCompat.getColor(this, R.color.error));
            tvResultMessage.setText("建议重点复习这些知识点");
        }
    }

    private void setupClickListeners() {
        // 继续练习
        btnContinuePractice.setOnClickListener(v -> {
            Intent intent = new Intent(this, WrongQuestionPracticeActivity.class);
            intent.putExtra("practice_mode", practiceMode);
            intent.putExtra("category_filter", categoryFilter);
            startActivity(intent);
            finish();
        });
        
        // 查看错题本
        btnViewWrongQuestions.setOnClickListener(v -> {
            Intent intent = new Intent(this, WrongQuestionActivity.class);
            startActivity(intent);
            finish();
        });
        
        // 返回首页
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // 按返回键直接回到错题本页面
        Intent intent = new Intent(this, WrongQuestionActivity.class);
        startActivity(intent);
        finish();
    }
}

