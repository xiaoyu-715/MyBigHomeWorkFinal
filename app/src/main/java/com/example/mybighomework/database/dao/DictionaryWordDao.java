package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.DictionaryWordEntity;

import java.util.List;

/**
 * 词典单词数据访问对象
 */
@Dao
public interface DictionaryWordDao {
    
    // ==================== 插入操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DictionaryWordEntity word);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DictionaryWordEntity> words);
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据ID获取单词
     */
    @Query("SELECT * FROM dictionary_words WHERE id = :wordId")
    DictionaryWordEntity getWordById(String wordId);
    
    /**
     * 根据ID获取单词（同步版本）
     */
    @Query("SELECT * FROM dictionary_words WHERE id = :wordId")
    DictionaryWordEntity getWordByIdSync(String wordId);
    
    /**
     * 根据单词文本获取单词
     */
    @Query("SELECT * FROM dictionary_words WHERE word = :word")
    DictionaryWordEntity getWordByWord(String word);
    
    /**
     * 根据词书ID获取单词列表（通过关联表）
     */
    @Query("SELECT dw.* FROM dictionary_words dw " +
           "INNER JOIN book_word_relations bwr ON dw.id = bwr.wordId " +
           "WHERE bwr.bookId = :bookId ORDER BY bwr.wordOrder")
    LiveData<List<DictionaryWordEntity>> getWordsByBookId(String bookId);
    
    /**
     * 根据词书ID获取单词列表（同步版本）
     */
    @Query("SELECT dw.* FROM dictionary_words dw " +
           "INNER JOIN book_word_relations bwr ON dw.id = bwr.wordId " +
           "WHERE bwr.bookId = :bookId ORDER BY bwr.wordOrder")
    List<DictionaryWordEntity> getWordsByBookIdSync(String bookId);
    
    /**
     * 搜索单词（前缀匹配）
     */
    @Query("SELECT * FROM dictionary_words WHERE word LIKE :keyword || '%' LIMIT 50")
    LiveData<List<DictionaryWordEntity>> searchWords(String keyword);
    
    /**
     * 搜索单词（同步版本）
     */
    @Query("SELECT * FROM dictionary_words WHERE word LIKE :keyword || '%' LIMIT 50")
    List<DictionaryWordEntity> searchWordsSync(String keyword);
    
    /**
     * 按难度筛选单词
     */
    @Query("SELECT * FROM dictionary_words WHERE difficulty >= :minDifficulty AND difficulty <= :maxDifficulty")
    LiveData<List<DictionaryWordEntity>> getWordsByDifficulty(int minDifficulty, int maxDifficulty);
    
    /**
     * 按词频筛选单词
     */
    @Query("SELECT * FROM dictionary_words WHERE frequency >= :minFrequency AND frequency <= :maxFrequency")
    LiveData<List<DictionaryWordEntity>> getWordsByFrequency(float minFrequency, float maxFrequency);
    
    /**
     * 获取随机单词（用于生成干扰选项）
     */
    @Query("SELECT * FROM dictionary_words ORDER BY RANDOM() LIMIT :limit")
    List<DictionaryWordEntity> getRandomWords(int limit);
    
    /**
     * 获取随机单词（排除指定单词）
     */
    @Query("SELECT * FROM dictionary_words WHERE id != :excludeWordId ORDER BY RANDOM() LIMIT :limit")
    List<DictionaryWordEntity> getRandomWordsExcluding(String excludeWordId, int limit);
    
    // ==================== 统计操作 ====================
    
    /**
     * 获取单词总数
     */
    @Query("SELECT COUNT(*) FROM dictionary_words")
    int getWordCount();
    
    /**
     * 检查单词是否存在
     */
    @Query("SELECT COUNT(*) FROM dictionary_words WHERE id = :wordId")
    int wordExists(String wordId);
    
    // ==================== 更新操作 ====================
    
    @Update
    void update(DictionaryWordEntity word);
    
    /**
     * 更新单词翻译
     */
    @Query("UPDATE dictionary_words SET translation = :translation WHERE word = :word")
    void updateTranslation(String word, String translation);
    
    // ==================== 删除操作 ====================
    
    @Query("DELETE FROM dictionary_words")
    void deleteAll();
}
