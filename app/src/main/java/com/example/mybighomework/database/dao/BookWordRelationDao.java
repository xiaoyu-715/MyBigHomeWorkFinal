package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mybighomework.database.entity.BookWordRelationEntity;

import java.util.List;

/**
 * 词书-单词关联数据访问对象
 */
@Dao
public interface BookWordRelationDao {
    
    // ==================== 插入操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookWordRelationEntity relation);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookWordRelationEntity> relations);
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据词书ID获取所有关联
     */
    @Query("SELECT * FROM book_word_relations WHERE bookId = :bookId ORDER BY wordOrder")
    LiveData<List<BookWordRelationEntity>> getRelationsByBookId(String bookId);
    
    /**
     * 根据词书ID获取所有关联（同步版本）
     */
    @Query("SELECT * FROM book_word_relations WHERE bookId = :bookId ORDER BY wordOrder")
    List<BookWordRelationEntity> getRelationsByBookIdSync(String bookId);
    
    /**
     * 根据单词ID获取所有关联（查找单词属于哪些词书）
     */
    @Query("SELECT * FROM book_word_relations WHERE wordId = :wordId")
    List<BookWordRelationEntity> getRelationsByWordId(String wordId);
    
    /**
     * 检查单词是否属于某词书
     */
    @Query("SELECT COUNT(*) FROM book_word_relations WHERE bookId = :bookId AND wordId = :wordId")
    int relationExists(String bookId, String wordId);
    
    /**
     * 获取词书中的单词ID列表
     */
    @Query("SELECT wordId FROM book_word_relations WHERE bookId = :bookId ORDER BY wordOrder")
    List<String> getWordIdsByBookId(String bookId);
    
    /**
     * 获取词书中按分组标签分类的关联
     */
    @Query("SELECT * FROM book_word_relations WHERE bookId = :bookId AND tag = :tag ORDER BY wordOrder")
    List<BookWordRelationEntity> getRelationsByBookIdAndTag(String bookId, String tag);
    
    /**
     * 获取词书中所有分组标签
     */
    @Query("SELECT DISTINCT tag FROM book_word_relations WHERE bookId = :bookId AND tag IS NOT NULL ORDER BY wordOrder")
    List<String> getTagsByBookId(String bookId);
    
    // ==================== 统计操作 ====================
    
    /**
     * 获取关联总数
     */
    @Query("SELECT COUNT(*) FROM book_word_relations")
    int getRelationCount();
    
    /**
     * 获取词书中的单词数量
     */
    @Query("SELECT COUNT(*) FROM book_word_relations WHERE bookId = :bookId")
    int getWordCountByBookId(String bookId);
    
    // ==================== 删除操作 ====================
    
    @Query("DELETE FROM book_word_relations")
    void deleteAll();
    
    @Query("DELETE FROM book_word_relations WHERE bookId = :bookId")
    void deleteByBookId(String bookId);
}
