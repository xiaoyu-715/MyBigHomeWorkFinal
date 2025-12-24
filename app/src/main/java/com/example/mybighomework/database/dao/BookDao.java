package com.example.mybighomework.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.BookEntity;

import java.util.List;

/**
 * 词书数据访问对象
 */
@Dao
public interface BookDao {
    
    // ==================== 插入操作 ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookEntity book);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookEntity> books);
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据ID获取词书
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    LiveData<BookEntity> getBookById(String bookId);
    
    /**
     * 根据ID获取词书（同步版本）
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    BookEntity getBookByIdSync(String bookId);
    
    /**
     * 获取顶级分类（level=1 或 parentId='0'）
     */
    @Query("SELECT * FROM books WHERE level = 1 OR parentId = '0' ORDER BY bookOrder")
    LiveData<List<BookEntity>> getTopLevelBooks();
    
    /**
     * 获取顶级分类（同步版本）
     */
    @Query("SELECT * FROM books WHERE level = 1 OR parentId = '0' ORDER BY bookOrder")
    List<BookEntity> getTopLevelBooksSync();
    
    /**
     * 根据父分类ID获取子分类/词书
     */
    @Query("SELECT * FROM books WHERE parentId = :parentId ORDER BY bookOrder")
    LiveData<List<BookEntity>> getBooksByParentId(String parentId);
    
    /**
     * 根据父分类ID获取子分类/词书（同步版本）
     */
    @Query("SELECT * FROM books WHERE parentId = :parentId ORDER BY bookOrder")
    List<BookEntity> getBooksByParentIdSync(String parentId);
    
    /**
     * 搜索词书（按名称）
     */
    @Query("SELECT * FROM books WHERE name LIKE '%' || :keyword || '%' OR fullName LIKE '%' || :keyword || '%' ORDER BY bookOrder")
    LiveData<List<BookEntity>> searchBooks(String keyword);
    
    /**
     * 搜索词书（同步版本）
     */
    @Query("SELECT * FROM books WHERE name LIKE '%' || :keyword || '%' OR fullName LIKE '%' || :keyword || '%' ORDER BY bookOrder")
    List<BookEntity> searchBooksSync(String keyword);
    
    /**
     * 获取所有可学习的词书（有单词的词书）
     */
    @Query("SELECT * FROM books WHERE directItemNum > 0 OR itemNum > 0 ORDER BY bookOrder")
    LiveData<List<BookEntity>> getLeafBooks();
    
    /**
     * 获取所有可学习的词书（同步版本）
     */
    @Query("SELECT * FROM books WHERE directItemNum > 0 OR itemNum > 0 ORDER BY bookOrder")
    List<BookEntity> getLeafBooksSync();
    
    /**
     * 获取所有非顶级分类的词书（用于词书选择列表）
     */
    @Query("SELECT * FROM books WHERE parentId != '0' AND parentId != '' ORDER BY name")
    LiveData<List<BookEntity>> getAllLearnableBooks();
    
    /**
     * 获取所有非顶级分类的词书（同步版本）
     */
    @Query("SELECT * FROM books WHERE parentId != '0' AND parentId != '' ORDER BY name")
    List<BookEntity> getAllLearnableBooksSync();
    
    /**
     * 获取所有词书
     */
    @Query("SELECT * FROM books ORDER BY level, bookOrder")
    LiveData<List<BookEntity>> getAllBooks();
    
    /**
     * 分页获取词书
     */
    @Query("SELECT * FROM books WHERE parentId = :parentId ORDER BY bookOrder LIMIT :limit OFFSET :offset")
    List<BookEntity> getBooksByParentIdPaged(String parentId, int limit, int offset);
    
    // ==================== 统计操作 ====================
    
    /**
     * 获取词书总数
     */
    @Query("SELECT COUNT(*) FROM books")
    int getBookCount();
    
    /**
     * 获取子分类数量
     */
    @Query("SELECT COUNT(*) FROM books WHERE parentId = :parentId")
    int getChildBookCount(String parentId);
    
    /**
     * 检查词书是否存在
     */
    @Query("SELECT COUNT(*) FROM books WHERE id = :bookId")
    int bookExists(String bookId);
    
    // ==================== 更新操作 ====================
    
    @Update
    void update(BookEntity book);
    
    // ==================== 删除操作 ====================
    
    @Query("DELETE FROM books")
    void deleteAll();
}
