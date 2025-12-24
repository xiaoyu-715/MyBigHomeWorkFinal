package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 每日任务实体类
 * 表示某一天需要完成的具体学习任务
 * 
 * Requirements: 1.2, 1.4
 */
@Entity(
    tableName = "daily_tasks",
    foreignKeys = {
        @ForeignKey(
            entity = StudyPlanEntity.class,
            parentColumns = "id",
            childColumns = "planId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = StudyPhaseEntity.class,
            parentColumns = "id",
            childColumns = "phaseId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "planId"),
        @Index(value = "phaseId"),
        @Index(value = {"planId", "date"}),
        @Index(value = {"planId", "date", "taskOrder"})
    }
)
public class DailyTaskEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    /** 关联的计划ID */
    private int planId;
    
    /** 关联的阶段ID */
    private int phaseId;
    
    /** 任务日期（yyyy-MM-dd） */
    private String date;
    
    /** 任务内容 */
    private String taskContent;
    
    /** 预计时长（分钟） */
    private int estimatedMinutes;
    
    /** 实际时长（分钟） */
    private int actualMinutes;
    
    /** 是否完成 */
    private boolean isCompleted;
    
    /** 完成时间戳 */
    private long completedAt;
    
    /** 任务顺序 */
    private int taskOrder;
    
    /** 操作类型（用于关联应用功能） */
    private String actionType;
    
    /** 完成类型：count（数量型）、duration（时长型）、simple（简单型） */
    private String completionType;
    
    /** 完成目标值 */
    private int completionTarget;
    
    /** 当前进度 */
    private int currentProgress;

    /** 默认构造函数 */
    public DailyTaskEntity() {
        this.actualMinutes = 0;
        this.isCompleted = false;
        this.completedAt = 0;
        this.taskOrder = 0;
        this.completionType = "simple";
        this.completionTarget = 1;
        this.currentProgress = 0;
    }

    /** 带参数的构造函数 */
    @Ignore
    public DailyTaskEntity(int planId, int phaseId, String date, 
                          String taskContent, int estimatedMinutes, int taskOrder) {
        this();
        this.planId = planId;
        this.phaseId = phaseId;
        this.date = date;
        this.taskContent = taskContent;
        this.estimatedMinutes = estimatedMinutes;
        this.taskOrder = taskOrder;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(int phaseId) {
        this.phaseId = phaseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTaskContent() {
        return taskContent;
    }

    public void setTaskContent(String taskContent) {
        this.taskContent = taskContent;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public int getActualMinutes() {
        return actualMinutes;
    }

    public void setActualMinutes(int actualMinutes) {
        this.actualMinutes = actualMinutes;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public int getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(int taskOrder) {
        this.taskOrder = taskOrder;
    }
    
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
     * 标记任务为完成
     * @param actualMinutes 实际花费的时间（分钟）
     */
    @Ignore
    public void markAsCompleted(int actualMinutes) {
        this.isCompleted = true;
        this.completedAt = System.currentTimeMillis();
        this.actualMinutes = actualMinutes;
    }

    /**
     * 取消任务完成状态
     */
    @Ignore
    public void markAsIncomplete() {
        this.isCompleted = false;
        this.completedAt = 0;
        // 保留actualMinutes，因为用户可能已经花了时间
    }

    /**
     * 切换任务完成状态
     * @return 切换后的完成状态
     */
    @Ignore
    public boolean toggleCompletion() {
        if (isCompleted) {
            markAsIncomplete();
        } else {
            markAsCompleted(estimatedMinutes); // 默认使用预计时长
        }
        return isCompleted;
    }

    @Override
    public String toString() {
        return "DailyTaskEntity{" +
                "id=" + id +
                ", planId=" + planId +
                ", phaseId=" + phaseId +
                ", date='" + date + '\'' +
                ", taskContent='" + taskContent + '\'' +
                ", estimatedMinutes=" + estimatedMinutes +
                ", actualMinutes=" + actualMinutes +
                ", isCompleted=" + isCompleted +
                ", taskOrder=" + taskOrder +
                '}';
    }
}
