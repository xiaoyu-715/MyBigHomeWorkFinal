package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.ExamResultEntity;

import java.util.List;

/**
 * 考试成绩数据访问对象
 */
@Dao
public interface ExamResultDao {
    
    /**
     * 插入一条考试成绩记录
     */
    @Insert
    long insert(ExamResultEntity examResult);
    
    /**
     * 更新考试成绩记录
     */
    @Update
    void update(ExamResultEntity examResult);
    
    /**
     * 删除考试成绩记录
     */
    @Delete
    void delete(ExamResultEntity examResult);
    
    /**
     * 根据ID查询考试成绩
     */
    @Query("SELECT * FROM exam_results WHERE id = :id")
    ExamResultEntity getById(int id);
    
    /**
     * 查询所有考试成绩，按日期降序排列
     */
    @Query("SELECT * FROM exam_results ORDER BY examDate DESC")
    List<ExamResultEntity> getAllResults();
    
    /**
     * 根据考试标题查询成绩记录
     */
    @Query("SELECT * FROM exam_results WHERE examTitle = :examTitle ORDER BY examDate DESC")
    List<ExamResultEntity> getResultsByTitle(String examTitle);
    
    /**
     * 根据年份查询成绩记录
     */
    @Query("SELECT * FROM exam_results WHERE examYear = :year ORDER BY examDate DESC")
    List<ExamResultEntity> getResultsByYear(String year);
    
    /**
     * 查询最近N条成绩记录
     */
    @Query("SELECT * FROM exam_results ORDER BY examDate DESC LIMIT :limit")
    List<ExamResultEntity> getRecentResults(int limit);
    
    /**
     * 获取最高分记录
     */
    @Query("SELECT * FROM exam_results ORDER BY totalScore DESC LIMIT 1")
    ExamResultEntity getHighestScore();
    
    /**
     * 获取平均分
     */
    @Query("SELECT AVG(totalScore) FROM exam_results")
    float getAverageScore();
    
    /**
     * 删除所有成绩记录
     */
    @Query("DELETE FROM exam_results")
    void deleteAll();
}

