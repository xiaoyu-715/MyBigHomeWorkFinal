package com.example.mybighomework.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 搜索历史实体类
 * 存储用户的搜索历史记录
 */
@Entity(
    tableName = "search_history",
    indices = {
        @Index(value = "searchTime", orders = Index.Order.DESC),
        @Index("userId")
    }
)
public class SearchHistoryEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String keyword;
    
    private long searchTime;
    
    @NonNull
    private String userId;
    
    public SearchHistoryEntity() {
        this.keyword = "";
        this.userId = "default";
        this.searchTime = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @NonNull
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(@NonNull String keyword) {
        this.keyword = keyword;
    }
    
    public long getSearchTime() {
        return searchTime;
    }
    
    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }
    
    @NonNull
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
}
