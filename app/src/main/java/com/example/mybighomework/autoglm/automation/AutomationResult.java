package com.example.mybighomework.autoglm.automation;

public class AutomationResult {
    private boolean success;
    private String message;
    private int itemsProcessed;
    private int itemsTotal;
    private double accuracy;
    private long duration;
    
    public AutomationResult() {
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getItemsProcessed() {
        return itemsProcessed;
    }
    
    public void setItemsProcessed(int itemsProcessed) {
        this.itemsProcessed = itemsProcessed;
    }
    
    public int getItemsTotal() {
        return itemsTotal;
    }
    
    public void setItemsTotal(int itemsTotal) {
        this.itemsTotal = itemsTotal;
    }
    
    public double getAccuracy() {
        return accuracy;
    }
    
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
}
