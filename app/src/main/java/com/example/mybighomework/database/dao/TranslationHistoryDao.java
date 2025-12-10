package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.TranslationHistoryEntity;

import java.util.List;

/**
 * 翻译历史记录数据访问对象
 */
@Dao
public interface TranslationHistoryDao {
    
    /**
     * 插入一条翻译历史记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TranslationHistoryEntity history);
    
    /**
     * 更新翻译历史记录
     */
    @Update
    void update(TranslationHistoryEntity history);
    
    /**
     * 删除翻译历史记录
     */
    @Delete
    void delete(TranslationHistoryEntity history);
    
    /**
     * 根据ID查询翻译历史
     */
    @Query("SELECT * FROM translation_history WHERE id = :id")
    TranslationHistoryEntity getById(int id);
    
    /**
     * 获取所有翻译历史（按时间降序）
     */
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getAll();
    
    /**
     * 获取最近N条翻译历史
     */
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC LIMIT :limit")
    List<TranslationHistoryEntity> getRecent(int limit);
    
    /**
     * 获取收藏的翻译历史
     */
    @Query("SELECT * FROM translation_history WHERE isFavorited = 1 ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getFavorited();
    
    /**
     * 根据源语言查询
     */
    @Query("SELECT * FROM translation_history WHERE sourceLanguage = :sourceLanguage ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getBySourceLanguage(String sourceLanguage);
    
    /**
     * 搜索翻译历史（源文本或译文包含关键词）
     */
    @Query("SELECT * FROM translation_history WHERE sourceText LIKE '%' || :keyword || '%' OR translatedText LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> search(String keyword);
    
    /**
     * 更新收藏状态
     */
    @Query("UPDATE translation_history SET isFavorited = :isFavorited WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorited);
    
    /**
     * 获取总数
     */
    @Query("SELECT COUNT(*) FROM translation_history")
    int getCount();
    
    /**
     * 删除所有数据
     */
    @Query("DELETE FROM translation_history")
    void deleteAll();
    
    /**
     * 根据时间范围查询
     */
    @Query("SELECT * FROM translation_history WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    List<TranslationHistoryEntity> getByTimeRange(long startTime, long endTime);
}

