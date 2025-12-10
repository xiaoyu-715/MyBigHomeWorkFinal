package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mybighomework.database.entity.QuestionEntity;
import java.util.List;

@Dao
public interface QuestionDao {
    
    // 基本CRUD操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertQuestion(QuestionEntity question);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertQuestions(List<QuestionEntity> questions);
    
    @Update
    void updateQuestion(QuestionEntity question);
    
    @Delete
    void deleteQuestion(QuestionEntity question);
    
    @Query("DELETE FROM questions WHERE id = :id")
    void deleteQuestionById(int id);
    
    // 查询操作
    @Query("SELECT * FROM questions WHERE id = :id")
    QuestionEntity getQuestionById(int id);
    
    @Query("SELECT * FROM questions WHERE isActive = 1 ORDER BY createdTime DESC")
    List<QuestionEntity> getAllActiveQuestions();
    
    @Query("SELECT * FROM questions WHERE category = :category AND isActive = 1 ORDER BY createdTime DESC")
    List<QuestionEntity> getQuestionsByCategory(String category);
    
    @Query("SELECT * FROM questions WHERE examType = :examType AND isActive = 1 ORDER BY createdTime DESC")
    List<QuestionEntity> getQuestionsByExamType(String examType);
    
    @Query("SELECT * FROM questions WHERE difficulty = :difficulty AND isActive = 1 ORDER BY createdTime DESC")
    List<QuestionEntity> getQuestionsByDifficulty(String difficulty);
    
    @Query("SELECT * FROM questions WHERE source = :source AND isActive = 1 ORDER BY createdTime DESC")
    List<QuestionEntity> getQuestionsBySource(String source);
    
    @Query("SELECT * FROM questions WHERE year = :year AND isActive = 1 ORDER BY createdTime DESC")
    List<QuestionEntity> getQuestionsByYear(int year);
    
    // 复合查询
    @Query("SELECT * FROM questions WHERE category = :category AND examType = :examType AND isActive = 1 ORDER BY RANDOM() LIMIT :limit")
    List<QuestionEntity> getRandomQuestionsByCategoryAndType(String category, String examType, int limit);
    
    @Query("SELECT * FROM questions WHERE category = :category AND difficulty = :difficulty AND isActive = 1 ORDER BY RANDOM() LIMIT :limit")
    List<QuestionEntity> getRandomQuestionsByCategoryAndDifficulty(String category, String difficulty, int limit);
    
    @Query("SELECT * FROM questions WHERE examType = :examType AND isActive = 1 ORDER BY RANDOM() LIMIT :limit")
    List<QuestionEntity> getRandomQuestionsByExamType(String examType, int limit);
    
    // 搜索功能
    @Query("SELECT * FROM questions WHERE (questionText LIKE '%' || :keyword || '%' OR explanation LIKE '%' || :keyword || '%' OR tags LIKE '%' || :keyword || '%') AND isActive = 1 ORDER BY createdTime DESC")
    List<QuestionEntity> searchQuestions(String keyword);
    
    // 统计查询
    @Query("SELECT COUNT(*) FROM questions WHERE isActive = 1")
    int getTotalQuestionCount();
    
    @Query("SELECT COUNT(*) FROM questions WHERE category = :category AND isActive = 1")
    int getQuestionCountByCategory(String category);
    
    @Query("SELECT COUNT(*) FROM questions WHERE examType = :examType AND isActive = 1")
    int getQuestionCountByExamType(String examType);
    
    @Query("SELECT AVG(accuracyRate) FROM questions WHERE category = :category AND totalAttempts > 0 AND isActive = 1")
    double getAverageAccuracyByCategory(String category);
    
    @Query("SELECT AVG(accuracyRate) FROM questions WHERE examType = :examType AND totalAttempts > 0 AND isActive = 1")
    double getAverageAccuracyByExamType(String examType);
    
    // 错题相关查询
    @Query("SELECT * FROM questions WHERE accuracyRate < :threshold AND totalAttempts >= :minAttempts AND isActive = 1 ORDER BY accuracyRate ASC")
    List<QuestionEntity> getWrongQuestions(double threshold, int minAttempts);
    
    @Query("SELECT * FROM questions WHERE accuracyRate < :threshold AND category = :category AND totalAttempts >= :minAttempts AND isActive = 1 ORDER BY accuracyRate ASC")
    List<QuestionEntity> getWrongQuestionsByCategory(double threshold, String category, int minAttempts);
    
    // 掌握情况查询
    @Query("SELECT * FROM questions WHERE accuracyRate >= :threshold AND totalAttempts >= :minAttempts AND isActive = 1 ORDER BY accuracyRate DESC")
    List<QuestionEntity> getMasteredQuestions(double threshold, int minAttempts);
    
    // 练习推荐
    @Query("SELECT * FROM questions WHERE totalAttempts = 0 AND isActive = 1 ORDER BY RANDOM() LIMIT :limit")
    List<QuestionEntity> getUnpracticedQuestions(int limit);
    
    @Query("SELECT * FROM questions WHERE accuracyRate BETWEEN :minRate AND :maxRate AND totalAttempts >= :minAttempts AND isActive = 1 ORDER BY RANDOM() LIMIT :limit")
    List<QuestionEntity> getQuestionsByAccuracyRange(double minRate, double maxRate, int minAttempts, int limit);
    
    // 词汇关联查询
    @Query("SELECT * FROM questions WHERE relatedVocabularyId = :vocabularyId AND isActive = 1")
    List<QuestionEntity> getQuestionsByVocabularyId(int vocabularyId);
    
    @Query("SELECT * FROM questions WHERE relatedVocabularyId IS NOT NULL AND isActive = 1")
    List<QuestionEntity> getVocabularyRelatedQuestions();
    
    // 批量操作
    @Query("UPDATE questions SET isActive = 0 WHERE category = :category")
    void deactivateQuestionsByCategory(String category);
    
    @Query("UPDATE questions SET isActive = 0 WHERE examType = :examType")
    void deactivateQuestionsByExamType(String examType);
    
    @Query("DELETE FROM questions WHERE isActive = 0")
    void deleteInactiveQuestions();
    
    // 数据维护
    @Query("UPDATE questions SET accuracyRate = CASE WHEN totalAttempts > 0 THEN (correctAttempts * 100.0 / totalAttempts) ELSE 0 END")
    void recalculateAllAccuracyRates();
}