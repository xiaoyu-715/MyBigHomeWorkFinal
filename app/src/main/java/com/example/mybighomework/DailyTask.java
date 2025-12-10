package com.example.mybighomework;

public class DailyTask {
    private String title;
    private String description;
    private String type;
    private boolean completed;
    
    public DailyTask(String title, String description, String type, boolean completed) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.completed = completed;
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
}