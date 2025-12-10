package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import java.util.List;

@Dao
public interface StudyRecordDao {
    
    // 基本CRUD操作
    @Insert
    long insertStudyRecord(StudyRecordEntity studyRecord);
    
    @Insert
    void insertStudyRecords(List<StudyRecordEntity> studyRecords);
    
    @Update
    void updateStudyRecord(StudyRecordEntity studyRecord);
    
    @Delete
    void deleteStudyRecord(StudyRecordEntity studyRecord);
    
    @Query("DELETE FROM study_records WHERE id = :id")
    void deleteStudyRecordById(long id);
    
    // 查询操作
    @Query("SELECT * FROM study_records WHERE id = :id")
    StudyRecordEntity getStudyRecordById(long id);
    
    @Query("SELECT * FROM study_records ORDER BY studyDate DESC")
    List<StudyRecordEntity> getAllStudyRecords();
    
    @Query("SELECT * FROM study_records WHERE questionId = :questionId ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsByQuestionId(long questionId);
    
    @Query("SELECT * FROM study_records WHERE vocabularyId = :vocabularyId ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsByVocabularyId(long vocabularyId);
    
    @Query("SELECT * FROM study_records WHERE studyType = :studyType ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsByType(String studyType);
    
    @Query("SELECT * FROM study_records WHERE sessionId = :sessionId ORDER BY studyDate ASC")
    List<StudyRecordEntity> getStudyRecordsBySession(String sessionId);

    @Query("SELECT DISTINCT strftime('%Y-%m-%d', studyDate / 1000, 'unixepoch', 'localtime') as studyDay FROM study_records ORDER BY studyDay DESC")
    List<String> getDistinctStudyDays();
    
    @Query("SELECT * FROM study_records WHERE isCorrect = 0 ORDER BY studyDate DESC")
    List<StudyRecordEntity> getWrongAnswerRecords();
    
    @Query("SELECT * FROM study_records WHERE isCorrect = 1 ORDER BY studyDate DESC")
    List<StudyRecordEntity> getCorrectAnswerRecords();
    
    @Query("SELECT * FROM study_records WHERE needsReview = 1 ORDER BY studyDate DESC")
    List<StudyRecordEntity> getRecordsNeedingReview();
    
    // 时间范围查询
    @Query("SELECT * FROM study_records WHERE studyDate BETWEEN :startTime AND :endTime ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsByDateRange(long startTime, long endTime);
    
    @Query("SELECT * FROM study_records WHERE studyDate >= :startTime ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsSince(long startTime);
    
    // 统计查询
    @Query("SELECT COUNT(*) FROM study_records")
    int getTotalStudyRecordCount();
    
    @Query("SELECT COUNT(*) FROM study_records WHERE isCorrect = 1")
    int getCorrectAnswerCount();
    
    @Query("SELECT COUNT(*) FROM study_records WHERE isCorrect = 0")
    int getWrongAnswerCount();
    
    @Query("SELECT COUNT(*) FROM study_records WHERE questionId = :questionId")
    int getStudyCountByQuestion(long questionId);
    
    @Query("SELECT COUNT(*) FROM study_records WHERE vocabularyId = :vocabularyId")
    int getStudyCountByVocabulary(long vocabularyId);
    
    @Query("SELECT COUNT(*) FROM study_records WHERE studyType = :studyType")
    int getStudyCountByType(String studyType);
    
    @Query("SELECT AVG(responseTime) FROM study_records WHERE questionId = :questionId")
    double getAverageResponseTimeByQuestion(long questionId);
    
    @Query("SELECT AVG(responseTime) FROM study_records WHERE vocabularyId = :vocabularyId")
    double getAverageResponseTimeByVocabulary(long vocabularyId);
    
    @Query("SELECT SUM(responseTime) / 3600000.0 FROM study_records")
    double getTotalStudyTimeHours();

    @Query("SELECT COUNT(*) FROM study_records WHERE studyType = :studyType AND isCorrect = 1")
    int getCorrectAnswerCountByType(String studyType);
    
    // 获取每日学习时长（秒），返回Map<日期, 时长>
    @Query("SELECT strftime('%Y-%m-%d', studyDate / 1000, 'unixepoch', 'localtime') as date, " +
           "SUM(responseTime) / 1000.0 as totalSeconds " +
           "FROM study_records " +
           "WHERE studyDate >= :startTime " +
           "GROUP BY date " +
           "ORDER BY date ASC")
    List<DailyStudyTime> getDailyStudyTime(long startTime);
    
    // 内部类用于每日学习时长
    class DailyStudyTime {
        public String date;
        public double totalSeconds;
    }
    
    // 正确率统计
    @Query("SELECT (COUNT(CASE WHEN isCorrect = 1 THEN 1 END) * 100.0 / COUNT(*)) as accuracy FROM study_records WHERE questionId = :questionId")
    double getAccuracyByQuestion(long questionId);
    
    @Query("SELECT (COUNT(CASE WHEN isCorrect = 1 THEN 1 END) * 100.0 / COUNT(*)) as accuracy FROM study_records WHERE vocabularyId = :vocabularyId")
    double getAccuracyByVocabulary(long vocabularyId);
    
    @Query("SELECT (COUNT(CASE WHEN isCorrect = 1 THEN 1 END) * 100.0 / COUNT(*)) as accuracy FROM study_records WHERE studyType = :studyType")
    double getAccuracyByStudyType(String studyType);
    
    // 学习进度查询
    @Query("SELECT DISTINCT questionId FROM study_records WHERE isCorrect = 1")
    List<Long> getMasteredQuestionIds();
    
    @Query("SELECT DISTINCT vocabularyId FROM study_records WHERE isCorrect = 1")
    List<Long> getMasteredVocabularyIds();
    
    @Query("SELECT DISTINCT questionId FROM study_records WHERE isCorrect = 0")
    List<Long> getWrongQuestionIds();
    
    @Query("SELECT DISTINCT vocabularyId FROM study_records WHERE isCorrect = 0")
    List<Long> getWrongVocabularyIds();
    
    // 复合查询
    @Query("SELECT * FROM study_records WHERE questionId = :questionId AND isCorrect = 0 ORDER BY studyDate DESC")
    List<StudyRecordEntity> getWrongRecordsByQuestion(long questionId);
    
    @Query("SELECT * FROM study_records WHERE vocabularyId = :vocabularyId AND isCorrect = 0 ORDER BY studyDate DESC")
    List<StudyRecordEntity> getWrongRecordsByVocabulary(long vocabularyId);
    
    @Query("SELECT * FROM study_records WHERE examType = :examType ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsByExamType(String examType);
    
    @Query("SELECT * FROM study_records WHERE category = :category ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsByCategory(String category);
    
    @Query("SELECT * FROM study_records WHERE difficulty = :difficulty ORDER BY studyDate DESC")
    List<StudyRecordEntity> getStudyRecordsByDifficulty(String difficulty);
    
    // 最近学习记录
    @Query("SELECT * FROM study_records ORDER BY studyDate DESC LIMIT :limit")
    List<StudyRecordEntity> getRecentStudyRecords(int limit);
    
    @Query("SELECT * FROM study_records WHERE questionId = :questionId ORDER BY studyDate DESC LIMIT 1")
    StudyRecordEntity getLatestStudyRecordByQuestion(long questionId);
    
    @Query("SELECT * FROM study_records WHERE vocabularyId = :vocabularyId ORDER BY studyDate DESC LIMIT 1")
    StudyRecordEntity getLatestStudyRecordByVocabulary(long vocabularyId);
    
    // 清理操作
    @Query("DELETE FROM study_records WHERE studyDate < :beforeDate")
    void deleteStudyRecordsBefore(long beforeDate);
    
    // ==================== 新增方法支持优化功能 ====================
    
    @Insert
    long insert(StudyRecordEntity record);
    
    @Query("SELECT * FROM study_records WHERE createdTime >= :sinceTime ORDER BY createdTime DESC")
    List<StudyRecordEntity> getRecordsSince(long sinceTime);
    
    @Query("SELECT * FROM study_records WHERE createdTime >= :startTime AND createdTime <= :endTime")
    List<StudyRecordEntity> getRecordsBetween(long startTime, long endTime);
    
    @Query("SELECT COUNT(*) FROM study_records WHERE createdTime >= :sinceTime")
    int getRecordCountSince(long sinceTime);
    
    @Query("SELECT AVG(responseTime) FROM study_records WHERE createdTime >= :sinceTime AND responseTime > 0")
    double getAverageResponseTimeSince(long sinceTime);
    
    @Query("SELECT studyType as type, COUNT(*) as count FROM study_records WHERE createdTime >= :sinceTime GROUP BY studyType")
    List<StudyTypeCount> getStudyTypeCountsSince(long sinceTime);
    
    // 学习活动统计的结果类
    static class StudyTypeCount {
        public String type;
        public int count;
    }
    
    @Query("DELETE FROM study_records WHERE sessionId = :sessionId")
    void deleteStudyRecordsBySession(String sessionId);
    
    // 学习会话统计
    @Query("SELECT COUNT(DISTINCT sessionId) FROM study_records")
    int getTotalSessionCount();
    
    @Query("SELECT sessionId, COUNT(*) as recordCount FROM study_records GROUP BY sessionId ORDER BY recordCount DESC")
    List<SessionSummary> getSessionSummaries();
    
    // 内部类用于会话统计
    class SessionSummary {
        public String sessionId;
        public int recordCount;
    }
}