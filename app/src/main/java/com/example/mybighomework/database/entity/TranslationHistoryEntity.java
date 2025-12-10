package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 翻译历史记录数据库实体
 */
@Entity(
    tableName = "translation_history",
    indices = {
        @Index("timestamp"),
        @Index("sourceLanguage"),
        @Index("targetLanguage")
    }
)
public class TranslationHistoryEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String sourceText;       // 源文本
    private String translatedText;   // 翻译文本
    private String sourceLanguage;   // 源语言（zh, en）
    private String targetLanguage;   // 目标语言（zh, en）
    private long timestamp;          // 时间戳
    private boolean isFavorited;     // 是否收藏
    
    public TranslationHistoryEntity() {
        this.timestamp = System.currentTimeMillis();
        this.isFavorited = false;
    }
    
    @Ignore
    public TranslationHistoryEntity(String sourceText, String translatedText,
                                   String sourceLanguage, String targetLanguage) {
        this();
        this.sourceText = sourceText;
        this.translatedText = translatedText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getSourceText() {
        return sourceText;
    }
    
    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }
    
    public String getTranslatedText() {
        return translatedText;
    }
    
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
    
    public String getSourceLanguage() {
        return sourceLanguage;
    }
    
    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }
    
    public String getTargetLanguage() {
        return targetLanguage;
    }
    
    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isFavorited() {
        return isFavorited;
    }
    
    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }
}

