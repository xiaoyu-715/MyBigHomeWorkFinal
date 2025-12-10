package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybighomework.database.entity.WrongQuestionEntity;
import com.example.mybighomework.repository.WrongQuestionRepository;
import com.example.mybighomework.database.AppDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 错题练习Activity
 * 功能：
 * - 支持顺序练习、随机练习
 * - 实时反馈答题结果
 * - 统计练习数据
 * - 自动更新错题掌握状态
 */
public class WrongQuestionPracticeActivity extends AppCompatActivity {

    // UI组件
    private ImageView btnBack;
    private TextView tvProgress, tvScore, tvPracticeMode, tvTimer;
    private ProgressBar progressBar;
    private TextView tvQuestion, tvCorrectAnswer, tvExplanation;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private LinearLayout layoutOptions, layoutResult;
    private Button btnNext;
    private ImageView ivResultIcon;
    private TextView tvResultText;

    // 数据相关
    private List<WrongQuestionEntity> practiceQuestions;
    private int currentQuestionIndex = 0;
    private int correctCount = 0;
    private int wrongCount = 0;
    private boolean isAnswered = false;
    private String practiceMode; // "sequential" 或 "random"
    private String categoryFilter; // 分类筛选
    
    // 计时器
    private long startTime;
    private CountDownTimer timer;
    
    // Repository
    private WrongQuestionRepository wrongQuestionRepository;
    
    // 记录每道题的答题情况
    private List<PracticeRecord> practiceRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_question_practice);

        // 获取传递的参数
        Intent intent = getIntent();
        practiceMode = intent.getStringExtra("practice_mode");
        categoryFilter = intent.getStringExtra("category_filter");
        
        if (practiceMode == null) {
            practiceMode = "sequential";
        }

        initViews();
        initDatabase();
        setupClickListeners();
        setupBackPressedCallback();
        loadPracticeQuestions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvProgress = findViewById(R.id.tv_progress);
        tvScore = findViewById(R.id.tv_score);
        tvPracticeMode = findViewById(R.id.tv_practice_mode);
        tvTimer = findViewById(R.id.tv_timer);
        progressBar = findViewById(R.id.progress_bar);
        
        tvQuestion = findViewById(R.id.tv_question);
        tvCorrectAnswer = findViewById(R.id.tv_correct_answer);
        tvExplanation = findViewById(R.id.tv_explanation);
        
        btnOptionA = findViewById(R.id.btn_option_a);
        btnOptionB = findViewById(R.id.btn_option_b);
        btnOptionC = findViewById(R.id.btn_option_c);
        btnOptionD = findViewById(R.id.btn_option_d);
        
        layoutOptions = findViewById(R.id.layout_options);
        layoutResult = findViewById(R.id.layout_result);
        btnNext = findViewById(R.id.btn_next);
        
        ivResultIcon = findViewById(R.id.iv_result_icon);
        tvResultText = findViewById(R.id.tv_result_text);
        
        // 设置练习模式显示
        if ("random".equals(practiceMode)) {
            tvPracticeMode.setText("随机练习");
        } else {
            tvPracticeMode.setText("顺序练习");
        }
    }

    private void initDatabase() {
        wrongQuestionRepository = new WrongQuestionRepository(
            AppDatabase.getInstance(this).wrongQuestionDao()
        );
        practiceRecords = new ArrayList<>();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> showExitConfirmDialog());
        
        btnOptionA.setOnClickListener(v -> selectOption(0));
        btnOptionB.setOnClickListener(v -> selectOption(1));
        btnOptionC.setOnClickListener(v -> selectOption(2));
        btnOptionD.setOnClickListener(v -> selectOption(3));
        
        btnNext.setOnClickListener(v -> nextQuestion());
    }

    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmDialog();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void loadPracticeQuestions() {
        if (categoryFilter != null && !categoryFilter.equals("全部")) {
            wrongQuestionRepository.getWrongQuestionsByCategory(categoryFilter, entities -> {
                runOnUiThread(() -> {
                    if (entities != null && !entities.isEmpty()) {
                        initPracticeQuestions(entities);
                    } else {
                        Toast.makeText(this, "该分类下暂无错题", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            });
        } else {
            wrongQuestionRepository.getAllWrongQuestions(entities -> {
                runOnUiThread(() -> {
                    if (entities != null && !entities.isEmpty()) {
                        initPracticeQuestions(entities);
                    } else {
                        Toast.makeText(this, "暂无错题可练习", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            });
        }
    }

    private void initPracticeQuestions(List<WrongQuestionEntity> questions) {
        practiceQuestions = new ArrayList<>(questions);
        
        // 只选择未掌握的题目
        List<WrongQuestionEntity> unmasteredQuestions = new ArrayList<>();
        for (WrongQuestionEntity question : practiceQuestions) {
            if (!question.isMastered()) {
                unmasteredQuestions.add(question);
            }
        }
        
        // 如果所有题目都已掌握，则练习全部题目
        if (unmasteredQuestions.isEmpty()) {
            Toast.makeText(this, "所有错题都已掌握，将练习全部错题", Toast.LENGTH_SHORT).show();
        } else {
            practiceQuestions = unmasteredQuestions;
        }
        
        // 随机模式下打乱题目顺序
        if ("random".equals(practiceMode)) {
            Collections.shuffle(practiceQuestions);
        }
        
        if (practiceQuestions.isEmpty()) {
            Toast.makeText(this, "暂无错题可练习", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 开始计时
        startTime = System.currentTimeMillis();
        startTimer();
        
        // 显示第一道题
        showCurrentQuestion();
    }

    private void startTimer() {
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                int minutes = (int) (elapsedTime / 60);
                int seconds = (int) (elapsedTime % 60);
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
    }

    private void showCurrentQuestion() {
        if (currentQuestionIndex >= practiceQuestions.size()) {
            showPracticeResult();
            return;
        }

        WrongQuestionEntity currentQuestion = practiceQuestions.get(currentQuestionIndex);
        
        // 更新进度
        tvProgress.setText((currentQuestionIndex + 1) + "/" + practiceQuestions.size());
        progressBar.setProgress((currentQuestionIndex + 1) * 100 / practiceQuestions.size());
        tvScore.setText("正确: " + correctCount + " | 错误: " + wrongCount);

        // 显示题目
        tvQuestion.setText("[" + (currentQuestion.getCategory() != null ? currentQuestion.getCategory() : "未分类") + "] " + currentQuestion.getQuestionText());

        // 设置选项
        String[] options = currentQuestion.getOptions();
        if (options != null && options.length >= 4) {
            btnOptionA.setText("A. " + options[0]);
            btnOptionB.setText("B. " + options[1]);
            btnOptionC.setText("C. " + options[2]);
            btnOptionD.setText("D. " + options[3]);
        } else {
            Toast.makeText(this, "选项数据不完整", Toast.LENGTH_SHORT).show();
            nextQuestion();
            return;
        }

        // 重置按钮状态
        resetOptionButtons();

        // 显示选项，隐藏结果
        layoutOptions.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        
        isAnswered = false;
    }

    private void selectOption(int selectedOption) {
        if (isAnswered) {
            return;
        }
        
        isAnswered = true;
        WrongQuestionEntity currentQuestion = practiceQuestions.get(currentQuestionIndex);
        int correctAnswer = currentQuestion.getCorrectAnswerIndex();
        
        // 记录答题情况
        PracticeRecord record = new PracticeRecord();
        record.questionId = currentQuestion.getId();
        record.userAnswer = selectedOption;
        record.correctAnswer = correctAnswer;
        record.isCorrect = (selectedOption == correctAnswer);
        practiceRecords.add(record);
        
        // 更新统计
        if (selectedOption == correctAnswer) {
            correctCount++;
            showCorrectFeedback(selectedOption);
            
            // 如果答对了，标记为掌握
            currentQuestion.setMastered(true);
            wrongQuestionRepository.updateWrongQuestion(currentQuestion);
        } else {
            wrongCount++;
            showWrongFeedback(selectedOption, correctAnswer);
            
            // 如果答错了，增加错误次数
            currentQuestion.setWrongCount(currentQuestion.getWrongCount() + 1);
            wrongQuestionRepository.updateWrongQuestion(currentQuestion);
        }
        
        // 更新分数显示
        tvScore.setText("正确: " + correctCount + " | 错误: " + wrongCount);
        
        // 显示结果区域
        showResultArea(currentQuestion, selectedOption == correctAnswer);
    }

    private void showCorrectFeedback(int selectedOption) {
        Button selectedButton = getOptionButton(selectedOption);
        selectedButton.setBackgroundResource(R.drawable.button_option_correct);
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void showWrongFeedback(int selectedOption, int correctAnswer) {
        Button selectedButton = getOptionButton(selectedOption);
        selectedButton.setBackgroundResource(R.drawable.button_option_wrong);
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        
        Button correctButton = getOptionButton(correctAnswer);
        correctButton.setBackgroundResource(R.drawable.button_option_correct);
        correctButton.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void showResultArea(WrongQuestionEntity question, boolean isCorrect) {
        layoutResult.setVisibility(View.VISIBLE);
        
        // 设置结果图标和文字
        if (isCorrect) {
            ivResultIcon.setImageResource(R.drawable.ic_check_circle);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.success));
            tvResultText.setText("回答正确！");
            tvResultText.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else {
            ivResultIcon.setImageResource(R.drawable.ic_cancel);
            ivResultIcon.setColorFilter(ContextCompat.getColor(this, R.color.error));
            tvResultText.setText("回答错误");
            tvResultText.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
        
        // 显示正确答案
        String[] options = question.getOptions();
        if (options != null && question.getCorrectAnswerIndex() >= 0 && question.getCorrectAnswerIndex() < options.length) {
            tvCorrectAnswer.setText("正确答案: " + options[question.getCorrectAnswerIndex()]);
        }
        
        // 显示解析
        if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
            tvExplanation.setText("解析: " + question.getExplanation());
            tvExplanation.setVisibility(View.VISIBLE);
        } else {
            tvExplanation.setVisibility(View.GONE);
        }
    }

    private Button getOptionButton(int index) {
        switch (index) {
            case 0: return btnOptionA;
            case 1: return btnOptionB;
            case 2: return btnOptionC;
            case 3: return btnOptionD;
            default: return btnOptionA;
        }
    }

    private void resetOptionButtons() {
        btnOptionA.setBackgroundResource(R.drawable.button_option_background);
        btnOptionB.setBackgroundResource(R.drawable.button_option_background);
        btnOptionC.setBackgroundResource(R.drawable.button_option_background);
        btnOptionD.setBackgroundResource(R.drawable.button_option_background);
        
        btnOptionA.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        btnOptionB.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        btnOptionC.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        btnOptionD.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        
        btnOptionA.setEnabled(true);
        btnOptionB.setEnabled(true);
        btnOptionC.setEnabled(true);
        btnOptionD.setEnabled(true);
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showCurrentQuestion();
    }

    private void showPracticeResult() {
        if (timer != null) {
            timer.cancel();
        }
        
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        
        // 跳转到结果页面
        Intent intent = new Intent(this, PracticeResultActivity.class);
        intent.putExtra("total_questions", practiceQuestions.size());
        intent.putExtra("correct_count", correctCount);
        intent.putExtra("wrong_count", wrongCount);
        intent.putExtra("elapsed_time", elapsedTime);
        intent.putExtra("practice_mode", practiceMode);
        intent.putExtra("category_filter", categoryFilter);
        startActivity(intent);
        finish();
    }

    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("退出练习")
            .setMessage("确定要退出练习吗？当前进度将会丢失。")
            .setPositiveButton("确定", (dialog, which) -> finish())
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * 练习记录内部类
     */
    private static class PracticeRecord {
        int questionId;
        int userAnswer;
        int correctAnswer;
        boolean isCorrect;
    }
}

