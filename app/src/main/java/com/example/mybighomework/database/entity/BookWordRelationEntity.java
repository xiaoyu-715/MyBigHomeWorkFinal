package com.example.mybighomework.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 词书-单词关联实体类
 * 存储词书与单词的多对多关系
 * 
 * 对应 relation_book_word.csv 数据
 */
@Entity(
    tableName = "book_word_relations",
    indices = {
        @Index("bookId"),                              // 词书ID索引
        @Index("wordId"),                              // 单词ID索引
        @Index(value = {"bookId", "wordId"}, unique = true)  // 复合唯一索引
    },
    foreignKeys = {
        @ForeignKey(
            entity = BookEntity.class,
            parentColumns = "id",
            childColumns = "bookId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = DictionaryWordEntity.class,
            parentColumns = "id",
            childColumns = "wordId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class BookWordRelationEntity {
    
    @PrimaryKey
    @NonNull
    private String id;              // 关系ID
    
    @NonNull
    private String bookId;          // 词书ID
    
    @NonNull
    private String wordId;          // 单词ID
    
    private String flag;            // 分组标记
    private String tag;             // 分组名: Unit 1, Chapter 1 等
    private int wordOrder;          // 在词书中的排序
    
    // 默认构造函数（Room需要）
    public BookWordRelationEntity() {
        this.id = "";
        this.bookId = "";
        this.wordId = "";
    }
    
    @Ignore
    public BookWordRelationEntity(@NonNull String id, @NonNull String bookId, 
                                   @NonNull String wordId, String flag, 
                                   String tag, int wordOrder) {
        this.id = id;
        this.bookId = bookId;
        this.wordId = wordId;
        this.flag = flag;
        this.tag = tag;
        this.wordOrder = wordOrder;
    }
    
    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    
    @NonNull
    public String getBookId() { return bookId; }
    public void setBookId(@NonNull String bookId) { this.bookId = bookId; }
    
    @NonNull
    public String getWordId() { return wordId; }
    public void setWordId(@NonNull String wordId) { this.wordId = wordId; }
    
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    
    public int getWordOrder() { return wordOrder; }
    public void setWordOrder(int wordOrder) { this.wordOrder = wordOrder; }
    
    @Override
    public String toString() {
        return "BookWordRelationEntity{" +
                "id='" + id + '\'' +
                ", bookId='" + bookId + '\'' +
                ", wordId='" + wordId + '\'' +
                ", tag='" + tag + '\'' +
                ", wordOrder=" + wordOrder +
                '}';
    }
}
