package com.example.mybighomework.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 用户单词收藏实体类
 * 存储用户收藏的单词（生词本）
 */
@Entity(
    tableName = "user_word_collection",
    foreignKeys = @ForeignKey(
        entity = DictionaryWordEntity.class,
        parentColumns = "id",
        childColumns = "wordId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = {"wordId", "userId"}, unique = true),
        @Index("userId"),
        @Index("collectedAt")
    }
)
public class UserWordCollectionEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String wordId;
    
    @NonNull
    private String userId;
    
    private long collectedAt;
    
    private String note;
    
    public UserWordCollectionEntity() {
        this.wordId = "";
        this.userId = "default";
        this.collectedAt = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @NonNull
    public String getWordId() {
        return wordId;
    }
    
    public void setWordId(@NonNull String wordId) {
        this.wordId = wordId;
    }
    
    @NonNull
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
    
    public long getCollectedAt() {
        return collectedAt;
    }
    
    public void setCollectedAt(long collectedAt) {
        this.collectedAt = collectedAt;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}
