package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 考试进度实体类
 * 用于保存用户答题进度,支持暂停和继续答题
 */
@Entity(tableName = "exam_progress")
public class ExamProgressEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String examType;          // 考试类型(如"考研英语")
    private int currentQuestionIndex; // 当前题目索引
    private long timeLeftInMillis;    // 剩余时间(毫秒)
    private String userAnswersJson;   // 用户答案JSON字符串
    private String bookmarkedQuestionsJson; // 标记的题目JSON字符串
    private long startTime;           // 开始时间
    private long lastUpdateTime;      // 最后更新时间
    private boolean isCompleted;      // 是否已完成

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    public void setTimeLeftInMillis(long timeLeftInMillis) {
        this.timeLeftInMillis = timeLeftInMillis;
    }

    public String getUserAnswersJson() {
        return userAnswersJson;
    }

    public void setUserAnswersJson(String userAnswersJson) {
        this.userAnswersJson = userAnswersJson;
    }

    public String getBookmarkedQuestionsJson() {
        return bookmarkedQuestionsJson;
    }

    public void setBookmarkedQuestionsJson(String bookmarkedQuestionsJson) {
        this.bookmarkedQuestionsJson = bookmarkedQuestionsJson;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}

