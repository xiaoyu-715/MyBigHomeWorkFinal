package com.example.mybighomework.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 例句实体类
 * 存储单词的例句数据
 */
@Entity(
    tableName = "example_sentences",
    foreignKeys = @ForeignKey(
        entity = DictionaryWordEntity.class,
        parentColumns = "id",
        childColumns = "wordId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("wordId"),
        @Index("difficulty")
    }
)
public class ExampleSentenceEntity {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    @NonNull
    private String wordId;
    
    @NonNull
    private String englishSentence;
    
    @NonNull
    private String chineseSentence;
    
    private String source;
    
    private int difficulty;
    
    private String category;
    
    public ExampleSentenceEntity() {
        this.id = "";
        this.wordId = "";
        this.englishSentence = "";
        this.chineseSentence = "";
        this.difficulty = 5;
    }
    
    @NonNull
    public String getId() {
        return id;
    }
    
    public void setId(@NonNull String id) {
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
    public String getEnglishSentence() {
        return englishSentence;
    }
    
    public void setEnglishSentence(@NonNull String englishSentence) {
        this.englishSentence = englishSentence;
    }
    
    @NonNull
    public String getChineseSentence() {
        return chineseSentence;
    }
    
    public void setChineseSentence(@NonNull String chineseSentence) {
        this.chineseSentence = chineseSentence;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}
