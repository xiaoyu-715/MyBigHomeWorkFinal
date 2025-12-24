package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 学习计划实体类
 * 用户的整体学习计划，包含基本信息和多个学习阶段
 * 
 * Requirements: 1.1
 */
@Entity(tableName = "study_plans")
public class StudyPlanEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String category;
    private String description;
    private String timeRange;
    private String duration;
    private int progress;
    private String priority;
    private String status;
    private boolean activeToday;
    private long createdTime;
    private long lastModifiedTime;
    
    // ========== 新增字段（结构化学习计划升级） ==========
    
    /** 计划简介（替代原description用于结构化计划） */
    private String summary;
    
    /** 总天数 */
    private int totalDays;
    
    /** 已完成天数 */
    private int completedDays;
    
    /** 连续学习天数 */
    private int streakDays;
    
    /** 累计学习时长（毫秒） */
    private long totalStudyTime;
    
    /** 是否AI生成 */
    private boolean isAiGenerated;
    
    /** 每日学习分钟数 */
    private int dailyMinutes;
    
    // 状态常量
    @Ignore
    public static final String STATUS_NOT_STARTED = "未开始";
    @Ignore
    public static final String STATUS_IN_PROGRESS = "进行中";
    @Ignore
    public static final String STATUS_COMPLETED = "已完成";
    @Ignore
    public static final String STATUS_PAUSED = "已暂停";

    // 构造函数
    public StudyPlanEntity() {
        this.createdTime = System.currentTimeMillis();
        this.lastModifiedTime = System.currentTimeMillis();
    }

    @Ignore
    public StudyPlanEntity(String title, String category, String description, 
                          String timeRange, String duration, String priority) {
        this();
        this.title = title;
        this.category = category;
        this.description = description;
        this.timeRange = timeRange;
        this.duration = duration;
        this.priority = priority;
        this.progress = 0;
        this.status = "未开始";
        this.activeToday = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { 
        this.progress = progress; 
        this.lastModifiedTime = System.currentTimeMillis();
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.lastModifiedTime = System.currentTimeMillis();
    }

    public boolean isActiveToday() { return activeToday; }
    public void setActiveToday(boolean activeToday) { this.activeToday = activeToday; }

    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }

    public long getLastModifiedTime() { return lastModifiedTime; }
    public void setLastModifiedTime(long lastModifiedTime) { this.lastModifiedTime = lastModifiedTime; }

    // ========== 新增字段的Getters和Setters ==========
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getCompletedDays() { return completedDays; }
    public void setCompletedDays(int completedDays) { 
        this.completedDays = completedDays;
        this.lastModifiedTime = System.currentTimeMillis();
    }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public long getTotalStudyTime() { return totalStudyTime; }
    public void setTotalStudyTime(long totalStudyTime) { this.totalStudyTime = totalStudyTime; }

    public boolean isAiGenerated() { return isAiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { isAiGenerated = aiGenerated; }

    public int getDailyMinutes() { return dailyMinutes; }
    public void setDailyMinutes(int dailyMinutes) { this.dailyMinutes = dailyMinutes; }

    // ========== 辅助方法 ==========
    
    /**
     * 检查计划是否已完成
     */
    @Ignore
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    /**
     * 检查计划是否正在进行中
     */
    @Ignore
    public boolean isInProgress() {
        return STATUS_IN_PROGRESS.equals(status);
    }

    /**
     * 检查计划是否未开始
     */
    @Ignore
    public boolean isNotStarted() {
        return STATUS_NOT_STARTED.equals(status);
    }

    /**
     * 检查计划是否已暂停
     */
    @Ignore
    public boolean isPaused() {
        return STATUS_PAUSED.equals(status);
    }

    /**
     * 增加学习时长
     * @param minutes 学习分钟数
     */
    @Ignore
    public void addStudyTime(int minutes) {
        this.totalStudyTime += minutes * 60 * 1000L; // 转换为毫秒
        this.lastModifiedTime = System.currentTimeMillis();
    }

    /**
     * 获取学习时长（分钟）
     */
    @Ignore
    public int getTotalStudyTimeMinutes() {
        return (int) (totalStudyTime / (60 * 1000));
    }

    /**
     * 获取学习时长（小时）
     */
    @Ignore
    public double getTotalStudyTimeHours() {
        return totalStudyTime / (60.0 * 60 * 1000);
    }

    @Override
    public String toString() {
        return "StudyPlanEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", progress=" + progress +
                ", status='" + status + '\'' +
                ", totalDays=" + totalDays +
                ", completedDays=" + completedDays +
                ", streakDays=" + streakDays +
                ", isAiGenerated=" + isAiGenerated +
                '}';
    }
}