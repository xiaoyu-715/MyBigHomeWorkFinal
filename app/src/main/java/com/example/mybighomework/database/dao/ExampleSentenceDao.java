package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mybighomework.database.entity.ExampleSentenceEntity;

import java.util.List;

@Dao
public interface ExampleSentenceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExampleSentenceEntity sentence);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExampleSentenceEntity> sentences);
    
    @Query("SELECT * FROM example_sentences WHERE wordId = :wordId ORDER BY difficulty")
    LiveData<List<ExampleSentenceEntity>> getExamplesByWordId(String wordId);
    
    @Query("SELECT * FROM example_sentences WHERE wordId = :wordId ORDER BY difficulty LIMIT :limit")
    List<ExampleSentenceEntity> getExamplesByWordIdSync(String wordId, int limit);
    
    @Query("SELECT * FROM example_sentences WHERE wordId = :wordId AND difficulty <= :maxDifficulty ORDER BY difficulty LIMIT :limit")
    List<ExampleSentenceEntity> getExamplesByDifficulty(String wordId, int maxDifficulty, int limit);
    
    @Query("SELECT COUNT(*) FROM example_sentences WHERE wordId = :wordId")
    int getExampleCount(String wordId);
    
    @Query("DELETE FROM example_sentences WHERE wordId = :wordId")
    void deleteByWordId(String wordId);
    
    @Query("DELETE FROM example_sentences")
    void deleteAll();
}
