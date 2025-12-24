package com.example.mybighomework.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 词典单词实体类
 * 存储从 DictionaryData 数据集导入的单词信息
 * 
 * 对应 word.csv 和 word_translation.csv 的合并数据
 */
@Entity(
    tableName = "dictionary_words",
    indices = {
        @Index(value = "word", unique = true),  // 单词唯一索引
        @Index("difficulty"),                    // 难度索引（用于筛选）
        @Index("frequency")                      // 词频索引（用于筛选和排序）
    }
)
public class DictionaryWordEntity {
    
    @PrimaryKey
    @NonNull
    private String id;              // 原始ID: 57067c89a172044907c6698e
    
    private String word;            // 单词: superspecies
    private String phoneticUk;      // 英式音标: [su:pərsˈpi:ʃi:z]
    private String phoneticUs;      // 美式音标: [supɚsˈpiʃiz]
    private float frequency;        // 词频: 0.0-1.0
    private int difficulty;         // 难度: 1-10
    private float acknowledgeRate;  // 认识率: 0.0-1.0
    private String translation;     // 中文翻译（从word_translation合并）
    
    // 默认构造函数（Room需要）
    public DictionaryWordEntity() {
        this.id = "";
    }
    
    @Ignore
    public DictionaryWordEntity(@NonNull String id, String word, String phoneticUk, 
                                 String phoneticUs, float frequency, int difficulty, 
                                 float acknowledgeRate, String translation) {
        this.id = id;
        this.word = word;
        this.phoneticUk = phoneticUk;
        this.phoneticUs = phoneticUs;
        this.frequency = frequency;
        this.difficulty = difficulty;
        this.acknowledgeRate = acknowledgeRate;
        this.translation = translation;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    
    public String getPhoneticUk() { return phoneticUk; }
    public void setPhoneticUk(String phoneticUk) { this.phoneticUk = phoneticUk; }
    
    public String getPhoneticUs() { return phoneticUs; }
    public void setPhoneticUs(String phoneticUs) { this.phoneticUs = phoneticUs; }
    
    public float getFrequency() { return frequency; }
    public void setFrequency(float frequency) { this.frequency = frequency; }
    
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    
    public float getAcknowledgeRate() { return acknowledgeRate; }
    public void setAcknowledgeRate(float acknowledgeRate) { this.acknowledgeRate = acknowledgeRate; }
    
    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }
    
    /**
     * 获取显示用的音标（优先英式）
     */
    public String getDisplayPhonetic() {
        if (phoneticUk != null && !phoneticUk.isEmpty()) {
            return phoneticUk;
        }
        return phoneticUs;
    }
    
    /**
     * 获取难度描述
     */
    public String getDifficultyDescription() {
        if (difficulty <= 3) return "简单";
        if (difficulty <= 6) return "中等";
        return "困难";
    }
    
    @Override
    public String toString() {
        return "DictionaryWordEntity{" +
                "id='" + id + '\'' +
                ", word='" + word + '\'' +
                ", translation='" + translation + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }
}
