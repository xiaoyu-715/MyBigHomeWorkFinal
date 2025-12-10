package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.mybighomework.database.converter.StringArrayConverter;

/**
 * 词汇记录实体
 * 添加了索引以优化查询性能
 */
@Entity(
    tableName = "vocabulary_records",
    indices = {
        @Index(value = "word", unique = true),  // 单词唯一索引（用于快速查找和避免重复）
        @Index("lastStudyTime"),                  // 最后学习时间索引（用于排序）
        @Index("isMastered"),                     // 掌握状态索引（用于筛选）
        @Index("difficulty"),                     // 难度索引（用于筛选）
        @Index("level")                           // 等级索引（用于按CET4/6筛选）
    }
)
@TypeConverters({StringArrayConverter.class})
public class VocabularyRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // 基本词汇信息
    private String word;
    private String meaning;
    private String pronunciation;
    private String example;
    
    // 扩展词汇信息
    private String[] synonyms;          // 同义词
    private String[] antonyms;          // 反义词
    private String wordType;            // 词性：noun, verb, adjective, adverb等
    private String[] collocations;      // 常用搭配
    private String[] exampleSentences;  // 多个例句
    private String etymology;           // 词源
    private String[] tags;              // 标签：高频词、核心词汇等
    private String level;               // 词汇等级：CET4, CET6, TOEFL, IELTS等
    private int frequency;              // 使用频率（1-10）
    
    // 学习统计
    private int correctCount;
    private int wrongCount;
    private boolean isMastered;
    private long lastStudyTime;
    private long createdTime;
    private String difficulty; // 简单、中等、困难
    
    // 学习进度
    private int reviewCount;            // 复习次数
    private long nextReviewTime;        // 下次复习时间
    private int memoryStrength;         // 记忆强度（1-10）
    private boolean isFavorite;         // 是否收藏
    private String notes;               // 个人笔记

    // 构造函数
    public VocabularyRecordEntity() {
        this.createdTime = System.currentTimeMillis();
        this.lastStudyTime = System.currentTimeMillis();
        this.correctCount = 0;
        this.wrongCount = 0;
        this.isMastered = false;
    }

    @Ignore
    public VocabularyRecordEntity(String word, String meaning, String pronunciation, String example) {
        this();
        this.word = word;
        this.meaning = meaning;
        this.pronunciation = pronunciation;
        this.example = example;
        this.difficulty = "中等";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getPronunciation() { return pronunciation; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getWrongCount() { return wrongCount; }
    public void setWrongCount(int wrongCount) { this.wrongCount = wrongCount; }

    public boolean isMastered() { return isMastered; }
    public void setMastered(boolean mastered) { 
        this.isMastered = mastered; 
        this.lastStudyTime = System.currentTimeMillis();
    }

    public long getLastStudyTime() { return lastStudyTime; }
    public void setLastStudyTime(long lastStudyTime) { this.lastStudyTime = lastStudyTime; }

    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    // 计算掌握度百分比
    public int getMasteryPercentage() {
        int total = correctCount + wrongCount;
        if (total == 0) return 0;
        return (correctCount * 100) / total;
    }
    
    // 新增字段的Getter和Setter方法
    public String[] getSynonyms() { return synonyms; }
    public void setSynonyms(String[] synonyms) { this.synonyms = synonyms; }
    
    public String[] getAntonyms() { return antonyms; }
    public void setAntonyms(String[] antonyms) { this.antonyms = antonyms; }
    
    public String getWordType() { return wordType; }
    public void setWordType(String wordType) { this.wordType = wordType; }
    
    public String[] getCollocations() { return collocations; }
    public void setCollocations(String[] collocations) { this.collocations = collocations; }
    
    public String[] getExampleSentences() { return exampleSentences; }
    public void setExampleSentences(String[] exampleSentences) { this.exampleSentences = exampleSentences; }
    
    public String getEtymology() { return etymology; }
    public void setEtymology(String etymology) { this.etymology = etymology; }
    
    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    
    public long getNextReviewTime() { return nextReviewTime; }
    public void setNextReviewTime(long nextReviewTime) { this.nextReviewTime = nextReviewTime; }
    
    public int getMemoryStrength() { return memoryStrength; }
    public void setMemoryStrength(int memoryStrength) { this.memoryStrength = memoryStrength; }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // 业务方法
    public void incrementReviewCount() {
        this.reviewCount++;
    }
    
    public void updateMemoryStrength(boolean isCorrect) {
        if (isCorrect) {
            this.memoryStrength = Math.min(10, this.memoryStrength + 1);
        } else {
            this.memoryStrength = Math.max(1, this.memoryStrength - 1);
        }
    }
    
    public boolean needsReview() {
        return System.currentTimeMillis() >= nextReviewTime;
    }
    
    public void scheduleNextReview() {
        // 基于记忆强度计算下次复习时间（间隔重复算法）
        long interval = calculateReviewInterval();
        this.nextReviewTime = System.currentTimeMillis() + interval;
    }
    
    private long calculateReviewInterval() {
        // 简单的间隔重复算法
        long baseInterval = 24 * 60 * 60 * 1000; // 1天
        return baseInterval * (long) Math.pow(2, memoryStrength - 1);
    }
}