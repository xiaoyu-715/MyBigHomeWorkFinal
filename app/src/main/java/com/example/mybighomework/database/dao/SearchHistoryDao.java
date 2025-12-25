package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mybighomework.database.entity.SearchHistoryEntity;

import java.util.List;

@Dao
public interface SearchHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SearchHistoryEntity history);
    
    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY searchTime DESC LIMIT :limit")
    LiveData<List<SearchHistoryEntity>> getRecentSearches(String userId, int limit);
    
    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY searchTime DESC LIMIT :limit")
    List<SearchHistoryEntity> getRecentSearchesSync(String userId, int limit);
    
    @Query("SELECT DISTINCT keyword FROM search_history WHERE userId = :userId AND keyword LIKE :keyword || '%' ORDER BY searchTime DESC LIMIT :limit")
    List<String> searchKeywords(String userId, String keyword, int limit);
    
    @Query("DELETE FROM search_history WHERE userId = :userId")
    void deleteAllByUser(String userId);
    
    @Query("DELETE FROM search_history WHERE searchTime < :timestamp")
    void deleteOldHistory(long timestamp);
    
    @Query("DELETE FROM search_history")
    void deleteAll();
}
