package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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
}