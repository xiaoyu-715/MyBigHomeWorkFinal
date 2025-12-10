package com.example.mybighomework;

public class StudyPlan {
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

    // 构造函数（不包含ID，用于创建新计划）
    public StudyPlan(String title, String category, String description, 
                    String timeRange, String duration, int progress, 
                    String priority, String status, boolean activeToday) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.timeRange = timeRange;
        this.duration = duration;
        this.progress = progress;
        this.priority = priority;
        this.status = status;
        this.activeToday = activeToday;
    }

    // 构造函数（包含ID，用于从数据库加载）
    public StudyPlan(int id, String title, String category, String description, 
                    String timeRange, String duration, int progress, 
                    String priority, String status, boolean activeToday) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.timeRange = timeRange;
        this.duration = duration;
        this.progress = progress;
        this.priority = priority;
        this.status = status;
        this.activeToday = activeToday;
    }

    // 简化构造函数（用于快速创建基本计划）
    public StudyPlan(String title, String category, String description, 
                    String timeRange, String duration, String priority) {
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

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getTimeRange() { return timeRange; }
    public String getDuration() { return duration; }
    public int getProgress() { return progress; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public boolean isActiveToday() { return activeToday; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setProgress(int progress) { this.progress = progress; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setStatus(String status) { this.status = status; }
    public void setActiveToday(boolean activeToday) { this.activeToday = activeToday; }
}