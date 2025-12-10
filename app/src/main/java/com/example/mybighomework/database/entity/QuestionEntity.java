package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "questions")
public class QuestionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String questionText;        // 题目内容
    private String[] options;           // 选项数组
    private int correctAnswer;          // 正确答案索引
    private String explanation;         // 答案解析
    private String category;            // 题目分类：词汇、语法、阅读、听力等
    private String examType;            // 考试类型：四级、六级、托福、雅思等
    private String difficulty;          // 难度等级：easy、medium、hard
    private String source;              // 题目来源：真题、模拟题、自定义等
    private int year;                   // 年份（真题年份）
    private String tags;                // 标签，用逗号分隔
    private Date createdTime;           // 创建时间
    private Date updatedTime;           // 更新时间
    private boolean isActive;           // 是否启用
    
    // 关联字段
    private Integer relatedVocabularyId; // 关联的词汇ID（如果是词汇题）
    
    // 统计字段
    private int totalAttempts;          // 总答题次数
    private int correctAttempts;        // 正确次数
    private double accuracyRate;        // 正确率
    
    public QuestionEntity() {
        this.createdTime = new Date();
        this.updatedTime = new Date();
        this.isActive = true;
        this.totalAttempts = 0;
        this.correctAttempts = 0;
        this.accuracyRate = 0.0;
    }
    
    @Ignore
    public QuestionEntity(String questionText, String[] options, int correctAnswer, 
                         String explanation, String category, String examType) {
        this();
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.category = category;
        this.examType = examType;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    
    public String[] getOptions() { return options; }
    public void setOptions(String[] options) { this.options = options; }
    
    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }
    
    public Date getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public Integer getRelatedVocabularyId() { return relatedVocabularyId; }
    public void setRelatedVocabularyId(Integer relatedVocabularyId) { this.relatedVocabularyId = relatedVocabularyId; }
    
    public int getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
    
    public int getCorrectAttempts() { return correctAttempts; }
    public void setCorrectAttempts(int correctAttempts) { this.correctAttempts = correctAttempts; }
    
    public double getAccuracyRate() { return accuracyRate; }
    public void setAccuracyRate(double accuracyRate) { this.accuracyRate = accuracyRate; }
    
    // 业务方法
    public void updateAccuracyRate() {
        if (totalAttempts > 0) {
            this.accuracyRate = (double) correctAttempts / totalAttempts * 100;
        } else {
            this.accuracyRate = 0.0;
        }
    }
    
    public void recordAttempt(boolean isCorrect) {
        this.totalAttempts++;
        if (isCorrect) {
            this.correctAttempts++;
        }
        updateAccuracyRate();
        this.updatedTime = new Date();
    }
}