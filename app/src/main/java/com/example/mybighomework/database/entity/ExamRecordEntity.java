package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 考试记录实体
 * 添加了索引以优化查询性能
 */
@Entity(
    tableName = "exam_records",
    indices = {
        @Index("examType"),     // 考试类型索引（用于按类型筛选）
        @Index("examMode"),     // 考试模式索引（用于区分练习和模拟考试）
        @Index("examTime"),     // 考试时间索引（用于时间排序）
        @Index("score")         // 分数索引（用于排序和筛选）
    }
)
public class ExamRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String examType; // 四级、六级、托福、雅思等
    private String examMode; // 练习模式、模拟考试
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int score;
    private long duration; // 考试用时（毫秒）
    private long examTime; // 考试时间
    private String weakAreas; // 薄弱环节，JSON格式存储

    // 构造函数
    public ExamRecordEntity() {
        this.examTime = System.currentTimeMillis();
    }

    @Ignore
    public ExamRecordEntity(String examType, String examMode, int totalQuestions) {
        this();
        this.examType = examType;
        this.examMode = examMode;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.score = 0;
        this.duration = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public String getExamMode() { return examMode; }
    public void setExamMode(String examMode) { this.examMode = examMode; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public long getExamTime() { return examTime; }
    public void setExamTime(long examTime) { this.examTime = examTime; }

    public String getWeakAreas() { return weakAreas; }
    public void setWeakAreas(String weakAreas) { this.weakAreas = weakAreas; }

    // 计算正确率
    public double getAccuracyRate() {
        if (totalQuestions == 0) return 0.0;
        return (double) correctAnswers / totalQuestions * 100;
    }

    // 格式化考试用时
    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        }
    }
}