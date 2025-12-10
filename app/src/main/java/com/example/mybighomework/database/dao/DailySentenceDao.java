package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.DailySentenceEntity;

import java.util.List;

/**
 * 每日一句数据访问对象
 */
@Dao
public interface DailySentenceDao {
    
    /**
     * 插入一条每日一句记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DailySentenceEntity sentence);
    
    /**
     * 批量插入每日一句记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DailySentenceEntity> sentences);
    
    /**
     * 更新每日一句记录
     */
    @Update
    void update(DailySentenceEntity sentence);
    
    /**
     * 删除每日一句记录
     */
    @Delete
    void delete(DailySentenceEntity sentence);
    
    /**
     * 根据ID查询每日一句
     */
    @Query("SELECT * FROM daily_sentences WHERE id = :id")
    DailySentenceEntity getById(int id);
    
    /**
     * 根据日期查询每日一句
     */
    @Query("SELECT * FROM daily_sentences WHERE date = :date LIMIT 1")
    DailySentenceEntity getByDate(String date);
    
    /**
     * 获取所有每日一句（按日期降序）
     */
    @Query("SELECT * FROM daily_sentences ORDER BY date DESC")
    List<DailySentenceEntity> getAll();
    
    /**
     * 获取所有已收藏的每日一句
     */
    @Query("SELECT * FROM daily_sentences WHERE isFavorited = 1 ORDER BY lastViewTime DESC")
    List<DailySentenceEntity> getFavorited();
    
    /**
     * 获取最近N条每日一句
     */
    @Query("SELECT * FROM daily_sentences ORDER BY date DESC LIMIT :limit")
    List<DailySentenceEntity> getRecent(int limit);
    
    /**
     * 获取已学习的每日一句
     */
    @Query("SELECT * FROM daily_sentences WHERE hasLearned = 1 ORDER BY date DESC")
    List<DailySentenceEntity> getLearned();
    
    /**
     * 根据分类查询每日一句
     */
    @Query("SELECT * FROM daily_sentences WHERE category = :category ORDER BY date DESC")
    List<DailySentenceEntity> getByCategory(String category);
    
    /**
     * 更新收藏状态
     */
    @Query("UPDATE daily_sentences SET isFavorited = :isFavorited WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorited);
    
    /**
     * 更新学习状态
     */
    @Query("UPDATE daily_sentences SET hasLearned = :hasLearned WHERE id = :id")
    void updateLearnedStatus(int id, boolean hasLearned);
    
    /**
     * 更新查看信息（最后查看时间和查看次数）
     */
    @Query("UPDATE daily_sentences SET lastViewTime = :lastViewTime, viewCount = viewCount + 1 WHERE id = :id")
    void updateViewInfo(int id, long lastViewTime);
    
    /**
     * 获取总数
     */
    @Query("SELECT COUNT(*) FROM daily_sentences")
    int getCount();
    
    /**
     * 获取收藏数
     */
    @Query("SELECT COUNT(*) FROM daily_sentences WHERE isFavorited = 1")
    int getFavoriteCount();
    
    /**
     * 删除所有数据
     */
    @Query("DELETE FROM daily_sentences")
    void deleteAll();
    
    /**
     * 根据日期范围查询
     */
    @Query("SELECT * FROM daily_sentences WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<DailySentenceEntity> getByDateRange(String startDate, String endDate);
}

