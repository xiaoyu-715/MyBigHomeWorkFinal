package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.ExamRecordEntity;

import java.util.List;

@Dao
public interface ExamDao {
    
    @Insert
    long insert(ExamRecordEntity examRecord);
    
    @Update
    void update(ExamRecordEntity examRecord);
    
    @Delete
    void delete(ExamRecordEntity examRecord);
    
    @Query("SELECT * FROM exam_records ORDER BY examTime DESC")
    List<ExamRecordEntity> getAllExamRecords();
    
    @Query("SELECT * FROM exam_records WHERE id = :id")
    ExamRecordEntity getExamRecordById(int id);
    
    @Query("SELECT * FROM exam_records WHERE examType = :examType ORDER BY examTime DESC")
    List<ExamRecordEntity> getExamRecordsByType(String examType);
    
    @Query("SELECT * FROM exam_records WHERE examMode = :examMode ORDER BY examTime DESC")
    List<ExamRecordEntity> getExamRecordsByMode(String examMode);
    
    @Query("SELECT * FROM exam_records WHERE examTime >= :startTime AND examTime <= :endTime ORDER BY examTime DESC")
    List<ExamRecordEntity> getExamRecordsByTimeRange(long startTime, long endTime);
    
    @Query("SELECT COUNT(*) FROM exam_records")
    int getTotalExamCount();
    
    @Query("SELECT COUNT(*) FROM exam_records WHERE examType = :examType")
    int getExamCountByType(String examType);
    
    @Query("SELECT COUNT(*) FROM exam_records WHERE examType = '模拟考试'")
    int getMockExamCount();
    
    @Query("SELECT COALESCE(AVG(score), 0) FROM exam_records WHERE examType = :examType")
    double getAverageScoreByType(String examType);
    
    @Query("SELECT COALESCE(AVG(score), 0) FROM exam_records")
    double getAverageScore();
    
    @Query("SELECT MAX(score) FROM exam_records WHERE examType = :examType")
    int getHighestScoreByType(String examType);
    
    @Query("SELECT AVG(CAST(correctAnswers AS FLOAT) / totalQuestions * 100) FROM exam_records WHERE examType = :examType")
    double getAverageAccuracyByType(String examType);
    
    @Query("SELECT * FROM exam_records WHERE examType = :examType ORDER BY score DESC LIMIT 1")
    ExamRecordEntity getBestExamByType(String examType);
    
    @Query("SELECT * FROM exam_records WHERE examType = :examType ORDER BY examTime DESC LIMIT 1")
    ExamRecordEntity getLatestExamByType(String examType);
    
    @Query("SELECT DISTINCT examType FROM exam_records")
    List<String> getAllExamTypes();
    
    @Query("SELECT COUNT(*) FROM exam_records WHERE examTime >= :startTime AND examTime <= :endTime")
    int getExamCountInTimeRange(long startTime, long endTime);
    
    @Query("DELETE FROM exam_records WHERE examTime < :timestamp")
    void deleteOldExamRecords(long timestamp);
    
    @Query("SELECT * FROM exam_records WHERE score >= :minScore ORDER BY examTime DESC")
    List<ExamRecordEntity> getExamRecordsByMinScore(int minScore);
    
    // ==================== LiveData 方法 ====================
    
    @Query("SELECT * FROM exam_records ORDER BY examTime DESC")
    LiveData<List<ExamRecordEntity>> getAllExamRecordsLive();
    
    @Query("SELECT * FROM exam_records ORDER BY examTime DESC LIMIT :limit")
    LiveData<List<ExamRecordEntity>> getRecentExamRecordsLive(int limit);
    
    @Query("SELECT COUNT(*) FROM exam_records")
    LiveData<Integer> getTotalExamCountLive();
    
    @Query("SELECT COALESCE(AVG(score), 0) FROM exam_records")
    LiveData<Double> getAverageScoreLive();
}