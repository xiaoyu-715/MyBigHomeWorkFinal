package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.StudyPlanEntity;

import java.util.List;

@Dao
public interface StudyPlanDao {
    
    @Insert
    long insert(StudyPlanEntity studyPlan);
    
    @Update
    void update(StudyPlanEntity studyPlan);
    
    @Delete
    void delete(StudyPlanEntity studyPlan);
    
    @Query("SELECT * FROM study_plans ORDER BY createdTime DESC")
    List<StudyPlanEntity> getAllStudyPlans();
    
    @Query("SELECT * FROM study_plans WHERE id = :id")
    StudyPlanEntity getStudyPlanById(int id);
    
    @Query("SELECT * FROM study_plans WHERE activeToday = 1")
    List<StudyPlanEntity> getTodayPlans();
    
    @Query("SELECT * FROM study_plans WHERE status = :status")
    List<StudyPlanEntity> getPlansByStatus(String status);
    
    @Query("SELECT * FROM study_plans WHERE priority = :priority")
    List<StudyPlanEntity> getPlansByPriority(String priority);
    
    @Query("SELECT * FROM study_plans WHERE category = :category")
    List<StudyPlanEntity> getPlansByCategory(String category);
    
    @Query("SELECT COUNT(*) FROM study_plans")
    int getTotalPlansCount();
    
    @Query("SELECT COUNT(*) FROM study_plans WHERE status = '已完成'")
    int getCompletedPlansCount();
    
    @Query("SELECT COUNT(*) FROM study_plans WHERE activeToday = 1")
    int getTodayPlansCount();
    
    // ==================== 新增方法支持优化功能 ====================
    
    @Query("SELECT * FROM study_plans WHERE progress < :progressThreshold")
    List<StudyPlanEntity> getPlansWithProgressLessThan(int progressThreshold);
    
    @Query("SELECT * FROM study_plans WHERE status != '已完成' AND status != '暂停'")
    List<StudyPlanEntity> getActivePlans();
    
    @Query("SELECT * FROM study_plans WHERE priority = '高' AND progress < 80")
    List<StudyPlanEntity> getHighPriorityUnfinishedPlans();
    
    @Query("UPDATE study_plans SET progress = :progress, lastModifiedTime = :timestamp WHERE id = :planId")
    void updateProgress(int planId, int progress, long timestamp);
    
    @Query("UPDATE study_plans SET status = :status, lastModifiedTime = :timestamp WHERE id = :planId")
    void updateStatus(int planId, String status, long timestamp);
    
    @Query("SELECT * FROM study_plans WHERE category = :category AND status != '已完成'")
    List<StudyPlanEntity> getActivePlansByCategory(String category);
    
    @Query("SELECT AVG(progress) FROM study_plans WHERE status != '已完成'")
    double getAverageProgress();
    
    @Query("UPDATE study_plans SET progress = :progress, status = :status WHERE id = :id")
    void updateProgress(int id, int progress, String status);
    
    @Query("UPDATE study_plans SET activeToday = :activeToday WHERE id = :id")
    void updateActiveToday(int id, boolean activeToday);
    
    @Query("DELETE FROM study_plans WHERE status = '已完成' AND lastModifiedTime < :timestamp")
    void deleteOldCompletedPlans(long timestamp);
    
    @Query("SELECT * FROM study_plans WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%'")
    List<StudyPlanEntity> searchPlans(String keyword);
    
    // ==================== LiveData 方法（推荐使用） ====================
    
    /**
     * 获取所有学习计划（LiveData）
     */
    @Query("SELECT * FROM study_plans ORDER BY createdTime DESC")
    LiveData<List<StudyPlanEntity>> getAllStudyPlansLive();
    
    /**
     * 获取活跃的学习计划（LiveData）
     */
    @Query("SELECT * FROM study_plans WHERE status != '已完成' AND status != '暂停' ORDER BY priority DESC")
    LiveData<List<StudyPlanEntity>> getActiveStudyPlansLive();
    
    /**
     * 获取已完成的学习计划（LiveData）
     */
    @Query("SELECT * FROM study_plans WHERE status = '已完成' ORDER BY lastModifiedTime DESC")
    LiveData<List<StudyPlanEntity>> getCompletedStudyPlansLive();
    
    /**
     * 获取今日计划（LiveData）
     */
    @Query("SELECT * FROM study_plans WHERE activeToday = 1 ORDER BY priority DESC")
    LiveData<List<StudyPlanEntity>> getTodayPlansLive();
    
    /**
     * 根据优先级获取计划（LiveData）
     */
    @Query("SELECT * FROM study_plans WHERE priority = :priority ORDER BY createdTime DESC")
    LiveData<List<StudyPlanEntity>> getPlansByPriorityLive(String priority);
    
    /**
     * 根据分类获取计划（LiveData）
     */
    @Query("SELECT * FROM study_plans WHERE category = :category ORDER BY createdTime DESC")
    LiveData<List<StudyPlanEntity>> getPlansByCategoryLive(String category);
    
    /**
     * 获取学习计划总数（LiveData）
     */
    @Query("SELECT COUNT(*) FROM study_plans")
    LiveData<Integer> getTotalPlansCountLive();
    
    /**
     * 获取已完成计划数（LiveData）
     */
    @Query("SELECT COUNT(*) FROM study_plans WHERE status = '已完成'")
    LiveData<Integer> getCompletedPlansCountLive();
    
    /**
     * 获取今日计划数（LiveData）
     */
    @Query("SELECT COUNT(*) FROM study_plans WHERE activeToday = 1")
    LiveData<Integer> getTodayPlansCountLive();
}