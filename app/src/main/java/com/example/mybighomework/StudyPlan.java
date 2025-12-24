package com.example.mybighomework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StudyPlan implements Serializable {
    private static final long serialVersionUID = 2L;
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
    
    // 阶段预览信息
    private List<PhasePreview> phases;
    
    /**
     * 阶段预览数据类
     */
    public static class PhasePreview implements Serializable {
        private static final long serialVersionUID = 1L;
        private int order;
        private String name;
        private String goal;
        private int durationDays;
        private List<TaskPreview> tasks;
        
        public PhasePreview() {
            this.tasks = new ArrayList<>();
        }
        
        public PhasePreview(int order, String name, String goal, int durationDays) {
            this.order = order;
            this.name = name;
            this.goal = goal;
            this.durationDays = durationDays;
            this.tasks = new ArrayList<>();
        }
        
        // Getters and Setters
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGoal() { return goal; }
        public void setGoal(String goal) { this.goal = goal; }
        public int getDurationDays() { return durationDays; }
        public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
        public List<TaskPreview> getTasks() { return tasks; }
        public void setTasks(List<TaskPreview> tasks) { this.tasks = tasks; }
        public void addTask(TaskPreview task) { this.tasks.add(task); }
    }
    
    /**
     * 任务预览数据类
     */
    public static class TaskPreview implements Serializable {
        private static final long serialVersionUID = 1L;
        private String content;
        private int minutes;
        
        public TaskPreview() {}
        
        public TaskPreview(String content, int minutes) {
            this.content = content;
            this.minutes = minutes;
        }
        
        // Getters and Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public int getMinutes() { return minutes; }
        public void setMinutes(int minutes) { this.minutes = minutes; }
    }

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
    
    // 阶段相关的Getter和Setter
    public List<PhasePreview> getPhases() { return phases; }
    public void setPhases(List<PhasePreview> phases) { this.phases = phases; }
    public boolean hasPhases() { return phases != null && !phases.isEmpty(); }
}