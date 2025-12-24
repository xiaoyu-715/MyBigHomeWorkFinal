package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 学习阶段实体类
 * 表示学习计划中的一个阶段，如"基础巩固"、"能力提升"、"冲刺强化"
 * 
 * Requirements: 1.1, 1.3
 */
@Entity(
    tableName = "study_phases",
    foreignKeys = @ForeignKey(
        entity = StudyPlanEntity.class,
        parentColumns = "id",
        childColumns = "planId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "planId"),
        @Index(value = {"planId", "phaseOrder"})
    }
)
public class StudyPhaseEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    /** 关联的计划ID */
    private int planId;
    
    /** 阶段顺序（1, 2, 3...） */
    private int phaseOrder;
    
    /** 阶段名称 */
    private String phaseName;
    
    /** 阶段目标 */
    private String goal;
    
    /** 持续天数 */
    private int durationDays;
    
    /** 任务模板JSON */
    private String taskTemplateJson;
    
    /** 已完成天数 */
    private int completedDays;
    
    /** 阶段进度（0-100） */
    private int progress;
    
    /** 状态：未开始/进行中/已完成 */
    private String status;
    
    /** 阶段开始日期（yyyy-MM-dd） */
    private String startDate;
    
    /** 阶段结束日期（yyyy-MM-dd） */
    private String endDate;

    // 状态常量
    @Ignore
    public static final String STATUS_NOT_STARTED = "未开始";
    @Ignore
    public static final String STATUS_IN_PROGRESS = "进行中";
    @Ignore
    public static final String STATUS_COMPLETED = "已完成";

    /** 默认构造函数 */
    public StudyPhaseEntity() {
        this.completedDays = 0;
        this.progress = 0;
        this.status = STATUS_NOT_STARTED;
    }

    /** 带参数的构造函数 */
    @Ignore
    public StudyPhaseEntity(int planId, int phaseOrder, String phaseName, 
                           String goal, int durationDays, String taskTemplateJson) {
        this();
        this.planId = planId;
        this.phaseOrder = phaseOrder;
        this.phaseName = phaseName;
        this.goal = goal;
        this.durationDays = durationDays;
        this.taskTemplateJson = taskTemplateJson;
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

    public int getPhaseOrder() {
        return phaseOrder;
    }

    public void setPhaseOrder(int phaseOrder) {
        this.phaseOrder = phaseOrder;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getTaskTemplateJson() {
        return taskTemplateJson;
    }

    public void setTaskTemplateJson(String taskTemplateJson) {
        this.taskTemplateJson = taskTemplateJson;
    }

    public int getCompletedDays() {
        return completedDays;
    }

    public void setCompletedDays(int completedDays) {
        this.completedDays = completedDays;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * 检查阶段是否已完成
     */
    @Ignore
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    /**
     * 检查阶段是否正在进行中
     */
    @Ignore
    public boolean isInProgress() {
        return STATUS_IN_PROGRESS.equals(status);
    }

    /**
     * 检查阶段是否未开始
     */
    @Ignore
    public boolean isNotStarted() {
        return STATUS_NOT_STARTED.equals(status);
    }

    @Override
    public String toString() {
        return "StudyPhaseEntity{" +
                "id=" + id +
                ", planId=" + planId +
                ", phaseOrder=" + phaseOrder +
                ", phaseName='" + phaseName + '\'' +
                ", goal='" + goal + '\'' +
                ", durationDays=" + durationDays +
                ", completedDays=" + completedDays +
                ", progress=" + progress +
                ", status='" + status + '\'' +
                '}';
    }
}
