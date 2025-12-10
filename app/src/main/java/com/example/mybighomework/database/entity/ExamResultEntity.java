package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mybighomework.database.converter.DateConverter;

import java.util.Date;

/**
 * 考试成绩实体类
 * 存储真题练习的完整成绩信息
 */
@Entity(tableName = "exam_results")
@TypeConverters({DateConverter.class})
public class ExamResultEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // 考试基本信息
    private String examTitle;           // 考试标题（如"2025年考研英语二"）
    private String examYear;            // 考试年份
    private Date examDate;              // 考试日期
    private long examDuration;          // 考试时长（毫秒）
    
    // 各部分得分
    private float clozeScore;           // 完形填空得分（满分10分）
    private float readingScore;         // 阅读理解得分（满分25分）
    private float newTypeScore;         // 新题型得分（满分10分）
    private float translationScore;     // 翻译得分（满分15分）
    private float writingScore;         // 写作得分（满分15分）
    
    // 总分和统计
    private float totalScore;           // 总分（满分75分）
    private float accuracy;             // 正确率（0-1之间）
    private int totalQuestions;         // 总题数
    private int correctAnswers;         // 答对题数
    private int wrongAnswers;           // 答错题数
    
    // 各部分详细统计
    private int clozeCorrect;           // 完形填空答对题数
    private int clozeTotal;             // 完形填空总题数
    private int readingCorrect;         // 阅读理解答对题数
    private int readingTotal;           // 阅读理解总题数
    private int newTypeCorrect;         // 新题型答对题数
    private int newTypeTotal;           // 新题型总题数
    
    // AI评语
    private String translationComment;  // 翻译批改评语
    private String writingComment;      // 写作批改评语
    
    // 答题详情（JSON格式）
    private String answerDetails;       // 每题的详细答题情况
    
    // 等级评定
    private String grade;               // 成绩等级（优秀/良好/及格/不及格）
    
    // 构造函数
    public ExamResultEntity() {
        this.examDate = new Date();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getExamTitle() {
        return examTitle;
    }
    
    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }
    
    public String getExamYear() {
        return examYear;
    }
    
    public void setExamYear(String examYear) {
        this.examYear = examYear;
    }
    
    public Date getExamDate() {
        return examDate;
    }
    
    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }
    
    public long getExamDuration() {
        return examDuration;
    }
    
    public void setExamDuration(long examDuration) {
        this.examDuration = examDuration;
    }
    
    public float getClozeScore() {
        return clozeScore;
    }
    
    public void setClozeScore(float clozeScore) {
        this.clozeScore = clozeScore;
    }
    
    public float getReadingScore() {
        return readingScore;
    }
    
    public void setReadingScore(float readingScore) {
        this.readingScore = readingScore;
    }
    
    public float getNewTypeScore() {
        return newTypeScore;
    }
    
    public void setNewTypeScore(float newTypeScore) {
        this.newTypeScore = newTypeScore;
    }
    
    public float getTranslationScore() {
        return translationScore;
    }
    
    public void setTranslationScore(float translationScore) {
        this.translationScore = translationScore;
    }
    
    public float getWritingScore() {
        return writingScore;
    }
    
    public void setWritingScore(float writingScore) {
        this.writingScore = writingScore;
    }
    
    public float getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(float totalScore) {
        this.totalScore = totalScore;
    }
    
    public float getAccuracy() {
        return accuracy;
    }
    
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public int getWrongAnswers() {
        return wrongAnswers;
    }
    
    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }
    
    public int getClozeCorrect() {
        return clozeCorrect;
    }
    
    public void setClozeCorrect(int clozeCorrect) {
        this.clozeCorrect = clozeCorrect;
    }
    
    public int getClozeTotal() {
        return clozeTotal;
    }
    
    public void setClozeTotal(int clozeTotal) {
        this.clozeTotal = clozeTotal;
    }
    
    public int getReadingCorrect() {
        return readingCorrect;
    }
    
    public void setReadingCorrect(int readingCorrect) {
        this.readingCorrect = readingCorrect;
    }
    
    public int getReadingTotal() {
        return readingTotal;
    }
    
    public void setReadingTotal(int readingTotal) {
        this.readingTotal = readingTotal;
    }
    
    public int getNewTypeCorrect() {
        return newTypeCorrect;
    }
    
    public void setNewTypeCorrect(int newTypeCorrect) {
        this.newTypeCorrect = newTypeCorrect;
    }
    
    public int getNewTypeTotal() {
        return newTypeTotal;
    }
    
    public void setNewTypeTotal(int newTypeTotal) {
        this.newTypeTotal = newTypeTotal;
    }
    
    public String getTranslationComment() {
        return translationComment;
    }
    
    public void setTranslationComment(String translationComment) {
        this.translationComment = translationComment;
    }
    
    public String getWritingComment() {
        return writingComment;
    }
    
    public void setWritingComment(String writingComment) {
        this.writingComment = writingComment;
    }
    
    public String getAnswerDetails() {
        return answerDetails;
    }
    
    public void setAnswerDetails(String answerDetails) {
        this.answerDetails = answerDetails;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
}

