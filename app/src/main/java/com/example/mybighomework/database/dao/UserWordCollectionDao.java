package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.UserWordCollectionEntity;

import java.util.List;

@Dao
public interface UserWordCollectionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserWordCollectionEntity collection);
    
    @Update
    void update(UserWordCollectionEntity collection);
    
    @Query("SELECT * FROM user_word_collection WHERE userId = :userId ORDER BY collectedAt DESC")
    LiveData<List<UserWordCollectionEntity>> getAllCollections(String userId);
    
    @Query("SELECT * FROM user_word_collection WHERE userId = :userId ORDER BY collectedAt DESC")
    List<UserWordCollectionEntity> getAllCollectionsSync(String userId);
    
    @Query("SELECT * FROM user_word_collection WHERE wordId = :wordId AND userId = :userId LIMIT 1")
    UserWordCollectionEntity getCollectionByWordId(String wordId, String userId);
    
    @Query("SELECT COUNT(*) FROM user_word_collection WHERE wordId = :wordId AND userId = :userId")
    int isWordCollected(String wordId, String userId);
    
    @Query("SELECT COUNT(*) FROM user_word_collection WHERE userId = :userId")
    int getCollectionCount(String userId);
    
    @Query("DELETE FROM user_word_collection WHERE wordId = :wordId AND userId = :userId")
    void deleteByWordId(String wordId, String userId);
    
    @Query("DELETE FROM user_word_collection WHERE userId = :userId")
    void deleteAllByUser(String userId);
    
    @Query("DELETE FROM user_word_collection")
    void deleteAll();
}
