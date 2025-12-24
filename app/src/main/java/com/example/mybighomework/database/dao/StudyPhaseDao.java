package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.StudyPhaseEntity;

import java.util.List;

/**
 * 学习阶段数据访问对象
 * 提供对study_phases表的CRUD操作
 * 
 * Requirements: 1.3
 */
@Dao
public interface StudyPhaseDao {
    
    // ==================== 基本CRUD操作 ====================
    
    /**
     * 插入单个阶段
     * @param phase 阶段实体
     * @return 插入后的ID
     */
    @Insert
    long insert(StudyPhaseEntity phase);
    
    /**
     * 批量插入阶段
     * @param phases 阶段列表
     * @return 插入后的ID列表
     */
    @Insert
    List<Long> insertAll(List<StudyPhaseEntity> phases);
    
    /**
     * 更新阶段
     * @param phase 阶段实体
     */
    @Update
    void update(StudyPhaseEntity phase);
    
    /**
     * 删除阶段
     * @param phase 阶段实体
     */
    @Delete
    void delete(StudyPhaseEntity phase);

    
    // ==================== 查询方法 ====================
    
    /**
     * 根据计划ID获取所有阶段（按顺序排列）
     * @param planId 计划ID
     * @return 阶段列表
     */
    @Query("SELECT * FROM study_phases WHERE planId = :planId ORDER BY phaseOrder")
    List<StudyPhaseEntity> getPhasesByPlanId(int planId);
    
    /**
     * 根据计划ID获取所有阶段（LiveData）
     * @param planId 计划ID
     * @return 阶段列表LiveData
     */
    @Query("SELECT * FROM study_phases WHERE planId = :planId ORDER BY phaseOrder")
    LiveData<List<StudyPhaseEntity>> getPhasesByPlanIdLive(int planId);
    
    /**
     * 获取当前进行中的阶段
     * @param planId 计划ID
     * @return 当前阶段，如果没有则返回null
     */
    @Query("SELECT * FROM study_phases WHERE planId = :planId AND status = '进行中' LIMIT 1")
    StudyPhaseEntity getCurrentPhase(int planId);
    
    /**
     * 获取当前进行中的阶段（LiveData）
     * @param planId 计划ID
     * @return 当前阶段LiveData
     */
    @Query("SELECT * FROM study_phases WHERE planId = :planId AND status = '进行中' LIMIT 1")
    LiveData<StudyPhaseEntity> getCurrentPhaseLive(int planId);
    
    /**
     * 根据ID获取阶段
     * @param phaseId 阶段ID
     * @return 阶段实体
     */
    @Query("SELECT * FROM study_phases WHERE id = :phaseId")
    StudyPhaseEntity getPhaseById(int phaseId);
    
    /**
     * 获取指定顺序的阶段
     * @param planId 计划ID
     * @param phaseOrder 阶段顺序
     * @return 阶段实体
     */
    @Query("SELECT * FROM study_phases WHERE planId = :planId AND phaseOrder = :phaseOrder")
    StudyPhaseEntity getPhaseByOrder(int planId, int phaseOrder);
    
    /**
     * 获取下一个阶段
     * @param planId 计划ID
     * @param currentOrder 当前阶段顺序
     * @return 下一个阶段，如果没有则返回null
     */
    @Query("SELECT * FROM study_phases WHERE planId = :planId AND phaseOrder > :currentOrder ORDER BY phaseOrder LIMIT 1")
    StudyPhaseEntity getNextPhase(int planId, int currentOrder);
    
    // ==================== 更新方法 ====================
    
    /**
     * 更新阶段进度和状态
     * @param phaseId 阶段ID
     * @param status 状态
     * @param progress 进度（0-100）
     */
    @Query("UPDATE study_phases SET status = :status, progress = :progress WHERE id = :phaseId")
    void updatePhaseProgress(int phaseId, String status, int progress);
    
    /**
     * 更新阶段已完成天数
     * @param phaseId 阶段ID
     * @param completedDays 已完成天数
     */
    @Query("UPDATE study_phases SET completedDays = :completedDays WHERE id = :phaseId")
    void updateCompletedDays(int phaseId, int completedDays);
    
    /**
     * 更新阶段状态
     * @param phaseId 阶段ID
     * @param status 状态
     */
    @Query("UPDATE study_phases SET status = :status WHERE id = :phaseId")
    void updateStatus(int phaseId, String status);
    
    /**
     * 更新阶段日期范围
     * @param phaseId 阶段ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    @Query("UPDATE study_phases SET startDate = :startDate, endDate = :endDate WHERE id = :phaseId")
    void updateDateRange(int phaseId, String startDate, String endDate);
    
    // ==================== 统计方法 ====================
    
    /**
     * 获取计划的阶段数量
     * @param planId 计划ID
     * @return 阶段数量
     */
    @Query("SELECT COUNT(*) FROM study_phases WHERE planId = :planId")
    int getPhaseCount(int planId);
    
    /**
     * 获取已完成的阶段数量
     * @param planId 计划ID
     * @return 已完成阶段数量
     */
    @Query("SELECT COUNT(*) FROM study_phases WHERE planId = :planId AND status = '已完成'")
    int getCompletedPhaseCount(int planId);
    
    /**
     * 检查所有阶段是否都已完成
     * @param planId 计划ID
     * @return 如果所有阶段都已完成返回true
     */
    @Query("SELECT COUNT(*) = 0 FROM study_phases WHERE planId = :planId AND status != '已完成'")
    boolean areAllPhasesCompleted(int planId);
    
    // ==================== 删除方法 ====================
    
    /**
     * 删除计划的所有阶段
     * @param planId 计划ID
     */
    @Query("DELETE FROM study_phases WHERE planId = :planId")
    void deleteByPlanId(int planId);
}
