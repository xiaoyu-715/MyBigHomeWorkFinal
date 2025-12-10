package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.VocabularyRecordEntity;

import java.util.List;

@Dao
public interface VocabularyDao {
    
    @Insert
    long insert(VocabularyRecordEntity vocabulary);
    
    @Insert
    void insertVocabularies(List<VocabularyRecordEntity> vocabularies);
    
    @Update
    void update(VocabularyRecordEntity vocabulary);
    
    @Delete
    void delete(VocabularyRecordEntity vocabulary);
    
    // ==================== 同步方法（保留用于后台线程） ====================
    @Query("SELECT * FROM vocabulary_records ORDER BY lastStudyTime DESC")
    List<VocabularyRecordEntity> getAllVocabulary();
    
    @Query("SELECT * FROM vocabulary_records WHERE id = :id")
    VocabularyRecordEntity getVocabularyById(int id);
    
    @Query("SELECT * FROM vocabulary_records WHERE word = :word")
    VocabularyRecordEntity getVocabularyByWord(String word);
    
    @Query("SELECT * FROM vocabulary_records WHERE isMastered = 1")
    List<VocabularyRecordEntity> getMasteredVocabulary();
    
    @Query("SELECT * FROM vocabulary_records WHERE isMastered = 0")
    List<VocabularyRecordEntity> getUnmasteredVocabulary();
    
    @Query("SELECT * FROM vocabulary_records WHERE difficulty = :difficulty")
    List<VocabularyRecordEntity> getVocabularyByDifficulty(String difficulty);
    
    @Query("SELECT COUNT(*) FROM vocabulary_records")
    int getTotalVocabularyCount();
    
    @Query("SELECT COUNT(*) FROM vocabulary_records WHERE isMastered = 1")
    int getMasteredVocabularyCount();
    
    @Query("SELECT COUNT(*) FROM vocabulary_records WHERE isMastered = 0")
    int getUnmasteredVocabularyCount();
    
    // ==================== LiveData 方法（推荐使用） ====================
    @Query("SELECT * FROM vocabulary_records ORDER BY lastStudyTime DESC")
    LiveData<List<VocabularyRecordEntity>> getAllVocabularyLive();
    
    @Query("SELECT * FROM vocabulary_records WHERE isMastered = 1")
    LiveData<List<VocabularyRecordEntity>> getMasteredVocabularyLive();
    
    @Query("SELECT * FROM vocabulary_records WHERE isMastered = 0")
    LiveData<List<VocabularyRecordEntity>> getUnmasteredVocabularyLive();
    
    @Query("SELECT COUNT(*) FROM vocabulary_records")
    LiveData<Integer> getTotalVocabularyCountLive();
    
    @Query("SELECT COUNT(*) FROM vocabulary_records WHERE isMastered = 1")
    LiveData<Integer> getMasteredVocabularyCountLive();
    
    @Query("SELECT COUNT(*) FROM vocabulary_records WHERE isMastered = 0")
    LiveData<Integer> getUnmasteredVocabularyCountLive();
    
    @Query("UPDATE vocabulary_records SET correctCount = correctCount + 1, lastStudyTime = :studyTime WHERE id = :id")
    void incrementCorrectCount(int id, long studyTime);
    
    @Query("UPDATE vocabulary_records SET wrongCount = wrongCount + 1, lastStudyTime = :studyTime WHERE id = :id")
    void incrementWrongCount(int id, long studyTime);
    
    @Query("UPDATE vocabulary_records SET isMastered = :mastered, lastStudyTime = :studyTime WHERE id = :id")
    void updateMasteryStatus(int id, boolean mastered, long studyTime);
    
    @Query("SELECT * FROM vocabulary_records WHERE word LIKE '%' || :keyword || '%' OR meaning LIKE '%' || :keyword || '%'")
    List<VocabularyRecordEntity> searchVocabulary(String keyword);
    
    @Query("SELECT * FROM vocabulary_records WHERE lastStudyTime >= :startTime AND lastStudyTime <= :endTime")
    List<VocabularyRecordEntity> getVocabularyByTimeRange(long startTime, long endTime);
    
    @Query("SELECT * FROM vocabulary_records ORDER BY RANDOM() LIMIT :limit")
    List<VocabularyRecordEntity> getRandomVocabulary(int limit);
    
    @Query("SELECT * FROM vocabulary_records WHERE isMastered = 0 ORDER BY wrongCount DESC, lastStudyTime ASC LIMIT :limit")
    List<VocabularyRecordEntity> getReviewVocabulary(int limit);
    
    @Query("SELECT * FROM vocabulary_records WHERE nextReviewTime <= :currentTime OR (isMastered = 0 AND wrongCount > 0)")
    List<VocabularyRecordEntity> getVocabulariesNeedingReview(long currentTime);
}