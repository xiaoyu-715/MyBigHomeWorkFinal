package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.WordLearningProgressEntity;

import java.util.List;

/**
 * 单词学习进度数据访问对象
 */
@Dao
public interface WordLearningProgressDao {
    
    // ==================== 插入操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WordLearningProgressEntity progress);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WordLearningProgressEntity> progressList);
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据用户ID和单词ID获取学习进度
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND wordId = :wordId")
    WordLearningProgressEntity getProgress(String userId, String wordId);
    
    /**
     * 获取用户对某词书的所有学习进度
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId")
    LiveData<List<WordLearningProgressEntity>> getProgressByBook(String userId, String bookId);
    
    /**
     * 获取用户对某词书的所有学习进度（同步版本）
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId")
    List<WordLearningProgressEntity> getProgressByBookSync(String userId, String bookId);
    
    /**
     * 获取用户所有学习进度
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId")
    LiveData<List<WordLearningProgressEntity>> getAllProgress(String userId);
    
    /**
     * 获取需要复习的单词（nextReviewTime <= 当前时间）
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND nextReviewTime <= :currentTime AND isMastered = 0 ORDER BY nextReviewTime")
    LiveData<List<WordLearningProgressEntity>> getWordsNeedingReview(String userId, long currentTime);
    
    /**
     * 获取需要复习的单词（同步版本）
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND nextReviewTime <= :currentTime AND isMastered = 0 ORDER BY nextReviewTime")
    List<WordLearningProgressEntity> getWordsNeedingReviewSync(String userId, long currentTime);
    
    /**
     * 获取某词书需要复习的单词
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId AND nextReviewTime <= :currentTime AND isMastered = 0 ORDER BY nextReviewTime")
    List<WordLearningProgressEntity> getWordsNeedingReviewByBook(String userId, String bookId, long currentTime);
    
    /**
     * 获取已掌握的单词
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND isMastered = 1")
    LiveData<List<WordLearningProgressEntity>> getMasteredWords(String userId);
    
    /**
     * 获取某词书已掌握的单词
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId AND isMastered = 1")
    List<WordLearningProgressEntity> getMasteredWordsByBook(String userId, String bookId);
    
    /**
     * 获取未学习的单词ID列表（词书中的单词但未在学习进度表中）
     */
    @Query("SELECT wordId FROM book_word_relations " +
           "WHERE bookId = :bookId AND wordId NOT IN " +
           "(SELECT wordId FROM word_learning_progress " +
           "WHERE bookId = :bookId AND userId = :userId) " +
           "ORDER BY wordOrder")
    List<String> getUnlearnedWordIds(String bookId, String userId);
    
    /**
     * 获取需要复习的单词ID列表（按复习时间排序）
     */
    @Query("SELECT wordId FROM word_learning_progress " +
           "WHERE bookId = :bookId AND userId = :userId " +
           "AND nextReviewTime <= :currentTime AND isMastered = 0 " +
           "ORDER BY nextReviewTime LIMIT :limit")
    List<String> getReviewWordIds(String bookId, String userId, long currentTime, int limit);
    
    /**
     * 根据用户ID、词书ID和单词ID获取学习进度
     */
    @Query("SELECT * FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId AND wordId = :wordId")
    WordLearningProgressEntity getProgressByUserBookWord(String userId, String bookId, String wordId);
    
    // ==================== 统计操作 ====================
    
    /**
     * 获取用户学习的单词总数
     */
    @Query("SELECT COUNT(*) FROM word_learning_progress WHERE userId = :userId")
    int getTotalLearnedCount(String userId);
    
    /**
     * 获取用户已掌握的单词总数
     */
    @Query("SELECT COUNT(*) FROM word_learning_progress WHERE userId = :userId AND isMastered = 1")
    int getTotalMasteredCount(String userId);
    
    /**
     * 获取某词书已学习的单词数
     */
    @Query("SELECT COUNT(*) FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId")
    int getLearnedCountByBook(String userId, String bookId);
    
    /**
     * 获取某词书已掌握的单词数
     */
    @Query("SELECT COUNT(*) FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId AND isMastered = 1")
    int getMasteredCountByBook(String userId, String bookId);
    
    /**
     * 获取今日需要复习的单词数
     */
    @Query("SELECT COUNT(*) FROM word_learning_progress WHERE userId = :userId AND nextReviewTime <= :currentTime AND isMastered = 0")
    int getTodayReviewCount(String userId, long currentTime);
    
    /**
     * 获取某词书今日需要复习的单词数
     */
    @Query("SELECT COUNT(*) FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId AND nextReviewTime <= :currentTime AND isMastered = 0")
    int getTodayReviewCountByBook(String userId, String bookId, long currentTime);
    
    /**
     * 获取用户总正确次数
     */
    @Query("SELECT SUM(correctCount) FROM word_learning_progress WHERE userId = :userId")
    int getTotalCorrectCount(String userId);
    
    /**
     * 获取用户总错误次数
     */
    @Query("SELECT SUM(wrongCount) FROM word_learning_progress WHERE userId = :userId")
    int getTotalWrongCount(String userId);
    
    // ==================== 更新操作 ====================
    
    @Update
    void update(WordLearningProgressEntity progress);
    
    /**
     * 批量更新
     */
    @Update
    void updateAll(List<WordLearningProgressEntity> progressList);
    
    // ==================== 删除操作 ====================
    
    @Query("DELETE FROM word_learning_progress WHERE userId = :userId")
    void deleteAllByUser(String userId);
    
    @Query("DELETE FROM word_learning_progress WHERE userId = :userId AND bookId = :bookId")
    void deleteByUserAndBook(String userId, String bookId);
    
    @Query("DELETE FROM word_learning_progress")
    void deleteAll();
}
