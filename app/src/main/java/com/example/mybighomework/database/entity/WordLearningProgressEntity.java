package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 单词学习进度实体类
 * 记录用户对每个单词的学习状态和复习计划
 * 
 * 支持艾宾浩斯遗忘曲线的智能复习功能
 */
@Entity(
    tableName = "word_learning_progress",
    indices = {
        @Index(value = {"userId", "wordId"}, unique = true),  // 用户-单词唯一索引
        @Index("nextReviewTime"),                              // 复习时间索引（用于查询待复习单词）
        @Index("isMastered"),                                  // 掌握状态索引
        @Index("bookId"),                                      // 词书索引（用于统计词书进度）
        @Index("userId")                                       // 用户索引
    }
)
public class WordLearningProgressEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String userId;          // 用户ID（预留多用户支持，默认"default"）
    private String wordId;          // 单词ID（关联 DictionaryWordEntity）
    private String bookId;          // 所属词书ID（记录从哪本词书学习的）
    
    // 学习统计
    private int correctCount;       // 正确次数
    private int wrongCount;         // 错误次数
    private boolean isMastered;     // 是否掌握（正确率>=80%且答对>=3次）
    
    // 记忆强度和复习计划
    private int memoryStrength;     // 记忆强度: 1-10
    private long lastStudyTime;     // 最后学习时间
    private long nextReviewTime;    // 下次复习时间
    private int reviewCount;        // 复习次数
    
    // 时间戳
    private long createdTime;       // 首次学习时间
    
    // 艾宾浩斯复习间隔（毫秒）
    private static final long[] REVIEW_INTERVALS = {
        5 * 60 * 1000,           // 5分钟
        30 * 60 * 1000,          // 30分钟
        12 * 60 * 60 * 1000,     // 12小时
        24 * 60 * 60 * 1000,     // 1天
        2 * 24 * 60 * 60 * 1000, // 2天
        4 * 24 * 60 * 60 * 1000, // 4天
        7 * 24 * 60 * 60 * 1000, // 7天
        15 * 24 * 60 * 60 * 1000 // 15天
    };
    
    // 默认构造函数（Room需要）
    public WordLearningProgressEntity() {
        this.userId = "default";
        this.memoryStrength = 1;
        this.createdTime = System.currentTimeMillis();
        this.lastStudyTime = System.currentTimeMillis();
        this.nextReviewTime = System.currentTimeMillis();
    }
    
    @Ignore
    public WordLearningProgressEntity(String userId, String wordId, String bookId) {
        this();
        this.userId = userId != null ? userId : "default";
        this.wordId = wordId;
        this.bookId = bookId;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getWordId() { return wordId; }
    public void setWordId(String wordId) { this.wordId = wordId; }
    
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    
    public int getWrongCount() { return wrongCount; }
    public void setWrongCount(int wrongCount) { this.wrongCount = wrongCount; }
    
    public boolean isMastered() { return isMastered; }
    public void setMastered(boolean mastered) { isMastered = mastered; }
    
    public int getMemoryStrength() { return memoryStrength; }
    public void setMemoryStrength(int memoryStrength) { this.memoryStrength = memoryStrength; }
    
    public long getLastStudyTime() { return lastStudyTime; }
    public void setLastStudyTime(long lastStudyTime) { this.lastStudyTime = lastStudyTime; }
    
    public long getNextReviewTime() { return nextReviewTime; }
    public void setNextReviewTime(long nextReviewTime) { this.nextReviewTime = nextReviewTime; }
    
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    // 业务方法
    
    /**
     * 记录答题结果
     * @param isCorrect 是否答对
     */
    public void recordAnswer(boolean isCorrect) {
        this.lastStudyTime = System.currentTimeMillis();
        
        if (isCorrect) {
            this.correctCount++;
            // 答对增加记忆强度
            this.memoryStrength = Math.min(10, this.memoryStrength + 1);
            // 延长复习间隔
            scheduleNextReview(true);
        } else {
            this.wrongCount++;
            // 答错降低记忆强度
            this.memoryStrength = Math.max(1, this.memoryStrength - 1);
            // 缩短复习间隔
            scheduleNextReview(false);
        }
        
        // 更新掌握状态
        updateMasteryStatus();
    }
    
    /**
     * 计算下次复习时间
     * @param isCorrect 本次是否答对
     */
    private void scheduleNextReview(boolean isCorrect) {
        this.reviewCount++;
        
        int intervalIndex;
        if (isCorrect) {
            // 答对：根据记忆强度选择间隔
            intervalIndex = Math.min(memoryStrength - 1, REVIEW_INTERVALS.length - 1);
        } else {
            // 答错：重置到较短间隔
            intervalIndex = Math.max(0, (memoryStrength / 3) - 1);
        }
        
        intervalIndex = Math.max(0, Math.min(intervalIndex, REVIEW_INTERVALS.length - 1));
        this.nextReviewTime = System.currentTimeMillis() + REVIEW_INTERVALS[intervalIndex];
    }
    
    /**
     * 更新掌握状态
     * 条件：正确率>=80% 且 答对次数>=3
     */
    private void updateMasteryStatus() {
        int total = correctCount + wrongCount;
        if (total >= 3 && correctCount >= 3) {
            float accuracy = (float) correctCount / total;
            this.isMastered = accuracy >= 0.8f;
        }
    }
    
    /**
     * 是否需要复习
     */
    public boolean needsReview() {
        return System.currentTimeMillis() >= nextReviewTime && !isMastered;
    }
    
    /**
     * 获取正确率
     */
    public float getAccuracy() {
        int total = correctCount + wrongCount;
        if (total == 0) return 0f;
        return (float) correctCount / total;
    }
    
    /**
     * 获取正确率百分比
     */
    public int getAccuracyPercent() {
        return Math.round(getAccuracy() * 100);
    }
    
    /**
     * 获取总答题次数
     */
    public int getTotalAttempts() {
        return correctCount + wrongCount;
    }
    
    @Override
    public String toString() {
        return "WordLearningProgressEntity{" +
                "id=" + id +
                ", wordId='" + wordId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", correctCount=" + correctCount +
                ", wrongCount=" + wrongCount +
                ", isMastered=" + isMastered +
                ", memoryStrength=" + memoryStrength +
                '}';
    }
}
