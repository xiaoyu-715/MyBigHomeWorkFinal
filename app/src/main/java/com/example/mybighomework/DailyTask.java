package com.example.mybighomework;

public class DailyTask {
    private int taskId;
    private int planId;
    private String title;
    private String description;
    private String type;
    private boolean completed;
    
    // 智能任务完成系统新增字段
    private String actionType;       // 操作类型
    private String completionType;   // 完成类型：count/simple
    private int completionTarget;    // 完成目标
    private int currentProgress;     // 当前进度
    
    public DailyTask(String title, String description, String type, boolean completed) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.completed = completed;
        this.completionType = "simple";
        this.completionTarget = 1;
        this.currentProgress = 0;
    }
    
    public DailyTask(String title, String description, String type, boolean completed,
                     String actionType, String completionType, int completionTarget, int currentProgress) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.completed = completed;
        this.actionType = actionType;
        this.completionType = completionType;
        this.completionTarget = completionTarget;
        this.currentProgress = currentProgress;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    
    public int getPlanId() {
        return planId;
    }
    
    public void setPlanId(int planId) {
        this.planId = planId;
    }
    
    // 新增字段的getter/setter
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getCompletionType() {
        return completionType;
    }
    
    public void setCompletionType(String completionType) {
        this.completionType = completionType;
    }
    
    public int getCompletionTarget() {
        return completionTarget;
    }
    
    public void setCompletionTarget(int completionTarget) {
        this.completionTarget = completionTarget;
    }
    
    public int getCurrentProgress() {
        return currentProgress;
    }
    
    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }
    
    /**
     * 获取格式化的进度文本
     */
    public String getProgressText() {
        if (completed) {
            return "已完成";
        }
        
        if ("count".equals(completionType)) {
            return String.format("进度 %d/%d", currentProgress, completionTarget);
        } else {
            return "未完成";
        }
    }
}