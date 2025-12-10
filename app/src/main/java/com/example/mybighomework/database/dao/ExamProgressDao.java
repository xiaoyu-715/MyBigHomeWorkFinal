package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mybighomework.database.entity.ExamProgressEntity;
import java.util.List;

/**
 * 考试进度DAO接口
 */
@Dao
public interface ExamProgressDao {
    
    /**
     * 插入考试进度
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProgress(ExamProgressEntity progress);
    
    /**
     * 更新考试进度
     */
    @Update
    void updateProgress(ExamProgressEntity progress);
    
    /**
     * 根据考试类型获取未完成的考试进度
     */
    @Query("SELECT * FROM exam_progress WHERE examType = :examType AND isCompleted = 0 ORDER BY lastUpdateTime DESC LIMIT 1")
    ExamProgressEntity getUncompletedProgress(String examType);
    
    /**
     * 根据ID获取考试进度
     */
    @Query("SELECT * FROM exam_progress WHERE id = :id")
    ExamProgressEntity getProgressById(int id);
    
    /**
     * 获取所有考试进度
     */
    @Query("SELECT * FROM exam_progress ORDER BY lastUpdateTime DESC")
    List<ExamProgressEntity> getAllProgress();
    
    /**
     * 删除考试进度
     */
    @Query("DELETE FROM exam_progress WHERE id = :id")
    void deleteProgress(int id);
    
    /**
     * 删除所有已完成的考试进度
     */
    @Query("DELETE FROM exam_progress WHERE isCompleted = 1")
    void deleteCompletedProgress();
    
    /**
     * 标记考试进度为已完成
     */
    @Query("UPDATE exam_progress SET isCompleted = 1 WHERE id = :id")
    void markAsCompleted(int id);
}

