package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.ExamResultEntity;
import com.example.mybighomework.database.entity.WrongQuestionEntity;
import com.example.mybighomework.database.repository.ExamResultRepository;
import com.example.mybighomework.repository.WrongQuestionRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 考试成绩详情页面
 */
public class ExamResultActivity extends AppCompatActivity {
    
    private ImageView btnBack;
    private TextView tvExamTitle, tvTotalScore, tvGrade, tvAccuracy, tvDuration;
    private TextView tvClozeScore, tvClozeAccuracy;
    private TextView tvReadingScore, tvReadingAccuracy;
    private TextView tvNewtypeScore, tvNewtypeAccuracy;
    private TextView tvTranslationScore, tvTranslationComment;
    private TextView tvWritingScore, tvWritingComment;
    private Button btnViewDetails, btnAddToWrongQuestions, btnFinish;
    
    private ExamResultRepository examResultRepository;
    private WrongQuestionRepository wrongQuestionRepository;
    private ExamResultEntity examResult;
    private int resultId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_result);
        
        // 初始化数据库
        examResultRepository = new ExamResultRepository(this);
        AppDatabase database = AppDatabase.getInstance(this);
        wrongQuestionRepository = new WrongQuestionRepository(database.wrongQuestionDao());
        
        // 获取成绩ID
        resultId = getIntent().getIntExtra("result_id", -1);
        if (resultId == -1) {
            Toast.makeText(this, "无法加载成绩数据", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 初始化视图
        initViews();
        
        // 加载成绩数据
        loadExamResult();
    }
    
    private void initViews() {
        // 顶部导航
        btnBack = findViewById(R.id.btn_back);
        
        // 总分区域
        tvExamTitle = findViewById(R.id.tv_exam_title);
        tvTotalScore = findViewById(R.id.tv_total_score);
        tvGrade = findViewById(R.id.tv_grade);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvDuration = findViewById(R.id.tv_duration);
        
        // 各部分得分
        tvClozeScore = findViewById(R.id.tv_cloze_score);
        tvClozeAccuracy = findViewById(R.id.tv_cloze_accuracy);
        tvReadingScore = findViewById(R.id.tv_reading_score);
        tvReadingAccuracy = findViewById(R.id.tv_reading_accuracy);
        tvNewtypeScore = findViewById(R.id.tv_newtype_score);
        tvNewtypeAccuracy = findViewById(R.id.tv_newtype_accuracy);
        tvTranslationScore = findViewById(R.id.tv_translation_score);
        tvTranslationComment = findViewById(R.id.tv_translation_comment);
        tvWritingScore = findViewById(R.id.tv_writing_score);
        tvWritingComment = findViewById(R.id.tv_writing_comment);
        
        // 按钮
        btnViewDetails = findViewById(R.id.btn_view_details);
        btnAddToWrongQuestions = findViewById(R.id.btn_add_to_wrong_questions);
        btnFinish = findViewById(R.id.btn_finish);
        
        // 设置点击事件
        btnBack.setOnClickListener(v -> finish());
        btnViewDetails.setOnClickListener(v -> showDetails());
        btnAddToWrongQuestions.setOnClickListener(v -> addWrongQuestions());
        btnFinish.setOnClickListener(v -> finish());
    }
    
    private void loadExamResult() {
        examResultRepository.getResultById(resultId, new ExamResultRepository.ResultCallback() {
            @Override
            public void onSuccess(ExamResultEntity result) {
                examResult = result;
                runOnUiThread(() -> displayExamResult(result));
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ExamResultActivity.this, "加载成绩失败: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void displayExamResult(ExamResultEntity result) {
        // 显示考试信息
        tvExamTitle.setText(result.getExamTitle() + " " + result.getExamYear());
        tvTotalScore.setText(String.format(Locale.getDefault(), "%.1f", result.getTotalScore()));
        tvGrade.setText(result.getGrade());
        
        // 设置等级颜色
        int gradeColor;
        if ("优秀".equals(result.getGrade())) {
            gradeColor = getColor(R.color.correct_green);
        } else if ("良好".equals(result.getGrade())) {
            gradeColor = getColor(R.color.primary_blue);
        } else if ("及格".equals(result.getGrade())) {
            gradeColor = getColor(android.R.color.holo_orange_light);
        } else {
            gradeColor = getColor(android.R.color.holo_red_light);
        }
        tvGrade.setTextColor(gradeColor);
        
        // 显示正确率
        float accuracy = result.getAccuracy() * 100;
        tvAccuracy.setText(String.format(Locale.getDefault(), "正确率: %.1f%%", accuracy));
        
        // 显示考试时长
        long durationMillis = result.getExamDuration();
        long hours = durationMillis / (1000 * 60 * 60);
        long minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60);
        tvDuration.setText(String.format(Locale.getDefault(), "用时: %d小时%d分", hours, minutes));
        
        // 显示各部分得分
        tvClozeScore.setText(String.format(Locale.getDefault(), "%.1f/10", result.getClozeScore()));
        tvClozeAccuracy.setText(String.format(Locale.getDefault(), "(%d/%d)", 
            result.getClozeCorrect(), result.getClozeTotal()));
        
        tvReadingScore.setText(String.format(Locale.getDefault(), "%.1f/25", result.getReadingScore()));
        tvReadingAccuracy.setText(String.format(Locale.getDefault(), "(%d/%d)", 
            result.getReadingCorrect(), result.getReadingTotal()));
        
        tvNewtypeScore.setText(String.format(Locale.getDefault(), "%.1f/10", result.getNewTypeScore()));
        tvNewtypeAccuracy.setText(String.format(Locale.getDefault(), "(%d/%d)", 
            result.getNewTypeCorrect(), result.getNewTypeTotal()));
        
        tvTranslationScore.setText(String.format(Locale.getDefault(), "%.1f/15", result.getTranslationScore()));
        tvTranslationComment.setText(result.getTranslationComment() != null ? 
            result.getTranslationComment() : "无评语");
        
        tvWritingScore.setText(String.format(Locale.getDefault(), "%.1f/15", result.getWritingScore()));
        tvWritingComment.setText(result.getWritingComment() != null ? 
            result.getWritingComment() : "无评语");
    }
    
    /**
     * 显示详细答题情况
     */
    private void showDetails() {
        if (examResult == null) {
            Toast.makeText(this, "成绩数据未加载", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 这里可以创建一个Dialog或新Activity显示详细答题情况
        // 简单实现：使用Toast提示
        String details = String.format(Locale.getDefault(),
            "答对: %d题\n答错: %d题\n总题数: %d题",
            examResult.getCorrectAnswers(),
            examResult.getWrongAnswers(),
            examResult.getTotalQuestions());
        
        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 将错题添加到错题本
     */
    private void addWrongQuestions() {
        if (examResult == null) {
            Toast.makeText(this, "成绩数据未加载", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载提示
        Toast.makeText(this, "正在添加错题到错题本...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                String answerDetails = examResult.getAnswerDetails();
                if (answerDetails == null || answerDetails.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "没有答题详情", Toast.LENGTH_SHORT).show());
                    return;
                }
                
                JSONArray detailsArray = new JSONArray(answerDetails);
                int addedCount = 0;
                
                for (int i = 0; i < detailsArray.length(); i++) {
                    JSONObject questionDetail = detailsArray.getJSONObject(i);
                    boolean isCorrect = questionDetail.getBoolean("isCorrect");
                    
                    // 只添加答错的题目
                    if (!isCorrect) {
                        WrongQuestionEntity wrongQuestion = new WrongQuestionEntity();
                        wrongQuestion.setQuestionText(questionDetail.getString("questionTitle"));
                        
                        // 解析选项（简化处理，实际应用中需要从原题目中获取完整选项）
                        String[] options = new String[]{"选项A", "选项B", "选项C", "选项D"};
                        wrongQuestion.setOptions(options);
                        
                        String correctAnswerStr = questionDetail.getString("correctAnswer");
                        int correctAnswerIndex = correctAnswerStr.charAt(0) - 'A';
                        wrongQuestion.setCorrectAnswerIndex(correctAnswerIndex);
                        
                        String userAnswerStr = questionDetail.getString("userAnswer");
                        int userAnswerIndex = userAnswerStr.length() > 0 ? userAnswerStr.charAt(0) - 'A' : -1;
                        wrongQuestion.setUserAnswerIndex(userAnswerIndex);
                        
                        wrongQuestion.setExplanation(questionDetail.optString("explanation", ""));
                        wrongQuestion.setCategory("真题练习");
                        wrongQuestion.setSource("真题 - " + examResult.getExamTitle());
                        wrongQuestion.setWrongTime(new Date());
                        wrongQuestion.setWrongCount(1);
                        wrongQuestion.setMastered(false);
                        
                        // 添加到错题本
                        wrongQuestionRepository.addWrongQuestion(wrongQuestion);
                        addedCount++;
                    }
                }
                
                final int finalAddedCount = addedCount;
                runOnUiThread(() -> {
                    if (finalAddedCount > 0) {
                        Toast.makeText(this, "已添加 " + finalAddedCount + " 道错题到错题本", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "没有错题需要添加", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e("ExamResultActivity", "添加错题失败", e);
                runOnUiThread(() -> Toast.makeText(this, "添加错题失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

