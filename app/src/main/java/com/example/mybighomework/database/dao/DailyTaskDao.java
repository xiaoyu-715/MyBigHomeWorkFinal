package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.DailyTaskEntity;

import java.util.List;

/**
 * 每日任务数据访问对象
 * 提供对daily_tasks表的CRUD操作
 * 
 * Requirements: 1.4, 3.2
 */
@Dao
public interface DailyTaskDao {
    
    // ==================== 基本CRUD操作 ====================
    
    /**
     * 插入单个任务
     * @param task 任务实体
     * @return 插入后的ID
     */
    @Insert
    long insert(DailyTaskEntity task);
    
    /**
     * 批量插入任务
     * @param tasks 任务列表
     * @return 插入后的ID列表
     */
    @Insert
    List<Long> insertAll(List<DailyTaskEntity> tasks);
    
    /**
     * 更新任务
     * @param task 任务实体
     */
    @Update
    void update(DailyTaskEntity task);
    
    /**
     * 删除任务
     * @param task 任务实体
     */
    @Delete
    void delete(DailyTaskEntity task);

    
    // ==================== 按日期查询 ====================
    
    /**
     * 获取指定日期的任务（按顺序排列）
     * @param planId 计划ID
     * @param date 日期（yyyy-MM-dd）
     * @return 任务列表
     */
    @Query("SELECT * FROM daily_tasks WHERE planId = :planId AND date = :date ORDER BY taskOrder")
    List<DailyTaskEntity> getTasksByDate(int planId, String date);
    
    /**
     * 获取指定日期的任务（LiveData）
     * @param planId 计划ID
     * @param date 日期（yyyy-MM-dd）
     * @return 任务列表LiveData
     */
    @Query("SELECT * FROM daily_tasks WHERE planId = :planId AND date = :date ORDER BY taskOrder")
    LiveData<List<DailyTaskEntity>> getTasksByDateLive(int planId, String date);
    
    /**
     * 获取日期范围内的任务
     * @param planId 计划ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 任务列表
     */
    @Query("SELECT * FROM daily_tasks WHERE planId = :planId AND date >= :startDate AND date <= :endDate ORDER BY date, taskOrder")
    List<DailyTaskEntity> getTasksByDateRange(int planId, String startDate, String endDate);
    
    // ==================== 按阶段查询 ====================
    
    /**
     * 获取指定阶段的所有任务
     * @param phaseId 阶段ID
     * @return 任务列表
     */
    @Query("SELECT * FROM daily_tasks WHERE phaseId = :phaseId ORDER BY date, taskOrder")
    List<DailyTaskEntity> getTasksByPhase(int phaseId);
    
    /**
     * 获取指定阶段的所有任务（LiveData）
     * @param phaseId 阶段ID
     * @return 任务列表LiveData
     */
    @Query("SELECT * FROM daily_tasks WHERE phaseId = :phaseId ORDER BY date, taskOrder")
    LiveData<List<DailyTaskEntity>> getTasksByPhaseLive(int phaseId);
    
    // ==================== 统计查询 ====================
    
    /**
     * 获取指定日期已完成的任务数量
     * @param planId 计划ID
     * @param date 日期
     * @return 已完成任务数量
     */
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE planId = :planId AND date = :date AND isCompleted = 1")
    int getCompletedTaskCount(int planId, String date);
    
    /**
     * 获取指定日期的任务总数
     * @param planId 计划ID
     * @param date 日期
     * @return 任务总数
     */
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE planId = :planId AND date = :date")
    int getTotalTaskCount(int planId, String date);
    
    /**
     * 获取阶段已完成的任务数量
     * @param phaseId 阶段ID
     * @return 已完成任务数量
     */
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE phaseId = :phaseId AND isCompleted = 1")
    int getCompletedTaskCountByPhase(int phaseId);
    
    /**
     * 获取阶段的任务总数
     * @param phaseId 阶段ID
     * @return 任务总数
     */
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE phaseId = :phaseId")
    int getTotalTaskCountByPhase(int phaseId);
    
    /**
     * 获取计划已完成的任务总数
     * @param planId 计划ID
     * @return 已完成任务数量
     */
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE planId = :planId AND isCompleted = 1")
    int getCompletedTaskCountByPlan(int planId);
    
    /**
     * 获取计划的任务总数
     * @param planId 计划ID
     * @return 任务总数
     */
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE planId = :planId")
    int getTotalTaskCountByPlan(int planId);

    
    // ==================== 更新方法 ====================
    
    /**
     * 更新任务完成状态
     * @param taskId 任务ID
     * @param completed 是否完成
     * @param completedAt 完成时间戳
     * @param actualMinutes 实际花费时间（分钟）
     */
    @Query("UPDATE daily_tasks SET isCompleted = :completed, completedAt = :completedAt, actualMinutes = :actualMinutes WHERE id = :taskId")
    void updateTaskCompletion(int taskId, boolean completed, long completedAt, int actualMinutes);
    
    /**
     * 标记任务为完成
     * @param taskId 任务ID
     * @param completedAt 完成时间戳
     * @param actualMinutes 实际花费时间
     */
    @Query("UPDATE daily_tasks SET isCompleted = 1, completedAt = :completedAt, actualMinutes = :actualMinutes WHERE id = :taskId")
    void markTaskCompleted(int taskId, long completedAt, int actualMinutes);
    
    /**
     * 标记任务为未完成
     * @param taskId 任务ID
     */
    @Query("UPDATE daily_tasks SET isCompleted = 0, completedAt = 0 WHERE id = :taskId")
    void markTaskIncomplete(int taskId);
    
    // ==================== 检查方法 ====================
    
    /**
     * 检查指定日期是否有任务
     * @param planId 计划ID
     * @param date 日期
     * @return 如果有任务返回true
     */
    @Query("SELECT EXISTS(SELECT 1 FROM daily_tasks WHERE planId = :planId AND date = :date)")
    boolean hasTasksForDate(int planId, String date);
    
    /**
     * 检查指定日期的任务是否全部完成
     * @param planId 计划ID
     * @param date 日期
     * @return 如果全部完成返回true
     */
    @Query("SELECT COUNT(*) = 0 FROM daily_tasks WHERE planId = :planId AND date = :date AND isCompleted = 0")
    boolean areAllTasksCompletedForDate(int planId, String date);
    
    /**
     * 检查阶段的任务是否全部完成
     * @param phaseId 阶段ID
     * @return 如果全部完成返回true
     */
    @Query("SELECT COUNT(*) = 0 FROM daily_tasks WHERE phaseId = :phaseId AND isCompleted = 0")
    boolean areAllTasksCompletedForPhase(int phaseId);
    
    // ==================== 学习时长统计 ====================
    
    /**
     * 获取指定日期的总学习时长（分钟）
     * @param planId 计划ID
     * @param date 日期
     * @return 总学习时长
     */
    @Query("SELECT COALESCE(SUM(actualMinutes), 0) FROM daily_tasks WHERE planId = :planId AND date = :date AND isCompleted = 1")
    int getTotalStudyMinutesForDate(int planId, String date);
    
    /**
     * 获取计划的总学习时长（分钟）
     * @param planId 计划ID
     * @return 总学习时长
     */
    @Query("SELECT COALESCE(SUM(actualMinutes), 0) FROM daily_tasks WHERE planId = :planId AND isCompleted = 1")
    int getTotalStudyMinutesByPlan(int planId);
    
    /**
     * 获取日期范围内的总学习时长（分钟）
     * @param planId 计划ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总学习时长
     */
    @Query("SELECT COALESCE(SUM(actualMinutes), 0) FROM daily_tasks WHERE planId = :planId AND date >= :startDate AND date <= :endDate AND isCompleted = 1")
    int getTotalStudyMinutesInRange(int planId, String startDate, String endDate);
    
    // ==================== 连续学习统计 ====================
    
    /**
     * 获取有完成任务的日期列表（用于计算连续学习天数）
     * @param planId 计划ID
     * @return 日期列表（降序）
     */
    @Query("SELECT DISTINCT date FROM daily_tasks WHERE planId = :planId AND isCompleted = 1 ORDER BY date DESC")
    List<String> getCompletedDates(int planId);
    
    /**
     * 获取所有有完成任务的日期列表（跨计划）
     * @return 日期列表（降序）
     */
    @Query("SELECT DISTINCT date FROM daily_tasks WHERE isCompleted = 1 ORDER BY date DESC")
    List<String> getAllCompletedDates();
    
    /**
     * 根据操作类型和日期查询任务
     * @param actionType 操作类型
     * @param date 日期
     * @return 任务列表
     */
    @Query("SELECT * FROM daily_tasks WHERE actionType = :actionType AND date = :date")
    List<DailyTaskEntity> getTasksByActionType(String actionType, String date);
    
    /**
     * 查询actionType为空或null的任务（用于修复旧数据）
     * @return 需要修复的任务列表
     */
    @Query("SELECT * FROM daily_tasks WHERE actionType IS NULL OR actionType = ''")
    List<DailyTaskEntity> getTasksWithEmptyActionType();
    
    /**
     * 查询今日未完成的指定类型任务
     * @param actionType 操作类型
     * @param date 日期
     * @return 未完成任务列表
     */
    @Query("SELECT * FROM daily_tasks WHERE actionType = :actionType AND date = :date AND isCompleted = 0")
    List<DailyTaskEntity> getUncompletedTasksByActionType(String actionType, String date);
    
    /**
     * 更新任务进度
     * @param taskId 任务ID
     * @param progress 当前进度
     */
    @Query("UPDATE daily_tasks SET currentProgress = :progress WHERE id = :taskId")
    void updateProgress(int taskId, int progress);
    
    /**
     * 标记任务完成（带进度）
     * @param taskId 任务ID
     * @param progress 最终进度
     * @param completedAt 完成时间戳
     */
    @Query("UPDATE daily_tasks SET isCompleted = 1, completedAt = :completedAt, currentProgress = :progress WHERE id = :taskId")
    void markCompletedWithProgress(int taskId, int progress, long completedAt);
    
    // ==================== 删除方法 ====================
    
    /**
     * 删除计划的所有任务
     * @param planId 计划ID
     */
    @Query("DELETE FROM daily_tasks WHERE planId = :planId")
    void deleteByPlanId(int planId);
    
    /**
     * 删除阶段的所有任务
     * @param phaseId 阶段ID
     */
    @Query("DELETE FROM daily_tasks WHERE phaseId = :phaseId")
    void deleteByPhaseId(int phaseId);
    
    /**
     * 删除指定日期的任务
     * @param planId 计划ID
     * @param date 日期
     */
    @Query("DELETE FROM daily_tasks WHERE planId = :planId AND date = :date")
    void deleteByDate(int planId, String date);
    
    // ==================== 其他查询 ====================
    
    /**
     * 根据ID获取任务
     * @param taskId 任务ID
     * @return 任务实体
     */
    @Query("SELECT * FROM daily_tasks WHERE id = :taskId")
    DailyTaskEntity getTaskById(int taskId);
    
    /**
     * 获取计划的所有任务
     * @param planId 计划ID
     * @return 任务列表
     */
    @Query("SELECT * FROM daily_tasks WHERE planId = :planId ORDER BY date, taskOrder")
    List<DailyTaskEntity> getAllTasksByPlan(int planId);
}
