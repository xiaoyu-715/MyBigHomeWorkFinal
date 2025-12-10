package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 每日一句数据库实体
 */
@Entity(
    tableName = "daily_sentences",
    indices = {
        @Index("date"),         // 日期索引（用于查询某日的句子）
        @Index("isFavorited"),  // 收藏状态索引（用于查询收藏）
        @Index("category")      // 分类索引
    }
)
public class DailySentenceEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String englishText;      // 英文句子
    private String chineseText;      // 中文翻译
    private String author;           // 作者/来源
    private String date;             // 日期（格式：yyyy-MM-dd）
    private String category;         // 分类（励志、名人名言、谚语等）
    private boolean isFavorited;     // 是否收藏
    private boolean hasLearned;      // 是否已学习
    private long createTime;         // 创建时间
    private long lastViewTime;       // 最后查看时间
    private int viewCount;           // 查看次数
    private String vocabularyJson;   // 词汇解析（JSON格式存储）
    private String audioUrl;         // 音频URL
    private String imageUrl;         // 图片URL
    private String sid;              // 句子ID（来自API）
    
    public DailySentenceEntity() {
        this.createTime = System.currentTimeMillis();
        this.lastViewTime = System.currentTimeMillis();
        this.isFavorited = false;
        this.hasLearned = false;
        this.viewCount = 0;
    }
    
    @Ignore
    public DailySentenceEntity(String englishText, String chineseText, String author, String date) {
        this();
        this.englishText = englishText;
        this.chineseText = chineseText;
        this.author = author;
        this.date = date;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getEnglishText() {
        return englishText;
    }
    
    public void setEnglishText(String englishText) {
        this.englishText = englishText;
    }
    
    public String getChineseText() {
        return chineseText;
    }
    
    public void setChineseText(String chineseText) {
        this.chineseText = chineseText;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public boolean isFavorited() {
        return isFavorited;
    }
    
    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }
    
    public boolean isHasLearned() {
        return hasLearned;
    }
    
    public void setHasLearned(boolean hasLearned) {
        this.hasLearned = hasLearned;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public long getLastViewTime() {
        return lastViewTime;
    }
    
    public void setLastViewTime(long lastViewTime) {
        this.lastViewTime = lastViewTime;
    }
    
    public int getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
    
    public String getVocabularyJson() {
        return vocabularyJson;
    }
    
    public void setVocabularyJson(String vocabularyJson) {
        this.vocabularyJson = vocabularyJson;
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getSid() {
        return sid;
    }
    
    public void setSid(String sid) {
        this.sid = sid;
    }
}

