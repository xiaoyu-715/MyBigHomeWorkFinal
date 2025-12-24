package com.example.mybighomework.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.BookDao;
import com.example.mybighomework.database.dao.BookWordRelationDao;
import com.example.mybighomework.database.dao.DictionaryWordDao;
import com.example.mybighomework.database.dao.WordLearningProgressDao;
import com.example.mybighomework.database.entity.BookEntity;
import com.example.mybighomework.database.entity.DictionaryWordEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词书数据仓库
 * 封装词书相关的数据访问逻辑
 */
public class BookRepository {
    
    private final BookDao bookDao;
    private final DictionaryWordDao wordDao;
    private final BookWordRelationDao relationDao;
    private final WordLearningProgressDao progressDao;
    private final ExecutorService executor;
    
    public BookRepository(Context context) {
        this(AppDatabase.getInstance(context));
    }
    
    public BookRepository(AppDatabase database) {
        bookDao = database.bookDao();
        wordDao = database.dictionaryWordDao();
        relationDao = database.bookWordRelationDao();
        progressDao = database.wordLearningProgressDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 根据父ID获取词书（同步）- 供ViewModel使用
     */
    public List<BookEntity> getBooksByParentIdSync(String parentId) {
        if ("0".equals(parentId)) {
            return bookDao.getTopLevelBooksSync();
        }
        return bookDao.getBooksByParentIdSync(parentId);
    }
    
    /**
     * 获取词书详情（同步）- 供ViewModel使用
     */
    public BookEntity getBookByIdSync(String bookId) {
        return bookDao.getBookByIdSync(bookId);
    }
    
    // ==================== 词书查询 ====================
    
    /**
     * 获取顶级分类
     */
    public LiveData<List<BookEntity>> getTopLevelBooks() {
        return bookDao.getTopLevelBooks();
    }
    
    /**
     * 获取顶级分类（同步）
     */
    public List<BookEntity> getTopLevelBooksSync() {
        return bookDao.getTopLevelBooksSync();
    }
    
    /**
     * 获取子分类/词书
     */
    public LiveData<List<BookEntity>> getChildBooks(String parentId) {
        return bookDao.getBooksByParentId(parentId);
    }
    
    /**
     * 获取子分类/词书（同步）
     */
    public List<BookEntity> getChildBooksSync(String parentId) {
        return bookDao.getBooksByParentIdSync(parentId);
    }
    
    /**
     * 获取词书详情
     */
    public LiveData<BookEntity> getBookDetail(String bookId) {
        return bookDao.getBookById(bookId);
    }
    
    /**
     * 获取词书详情（同步）
     */
    public BookEntity getBookDetailSync(String bookId) {
        return bookDao.getBookByIdSync(bookId);
    }
    
    /**
     * 搜索词书
     */
    public LiveData<List<BookEntity>> searchBooks(String keyword) {
        return bookDao.searchBooks(keyword);
    }
    
    /**
     * 搜索词书（同步）
     */
    public List<BookEntity> searchBooksSync(String keyword) {
        return bookDao.searchBooksSync(keyword);
    }
    
    /**
     * 获取所有可学习的词书
     */
    public LiveData<List<BookEntity>> getLeafBooks() {
        return bookDao.getLeafBooks();
    }
    
    /**
     * 获取所有可学习的词书（同步）
     */
    public List<BookEntity> getLeafBooksSync() {
        return bookDao.getLeafBooksSync();
    }
    
    /**
     * 获取所有非顶级分类的词书（用于词书选择列表）
     */
    public LiveData<List<BookEntity>> getAllLearnableBooks() {
        return bookDao.getAllLearnableBooks();
    }
    
    /**
     * 获取所有非顶级分类的词书（同步版本）
     */
    public List<BookEntity> getAllLearnableBooksSync() {
        return bookDao.getAllLearnableBooksSync();
    }
    
    /**
     * 分页获取词书
     */
    public void getBooksPaged(String parentId, int page, int pageSize, BooksCallback callback) {
        executor.execute(() -> {
            try {
                int offset = page * pageSize;
                List<BookEntity> books = bookDao.getBooksByParentIdPaged(parentId, pageSize, offset);
                if (callback != null) {
                    callback.onSuccess(books);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    // ==================== 词书单词 ====================
    
    /**
     * 获取词书中的单词
     */
    public LiveData<List<DictionaryWordEntity>> getWordsForBook(String bookId) {
        return wordDao.getWordsByBookId(bookId);
    }
    
    /**
     * 获取词书中的单词（同步）
     */
    public List<DictionaryWordEntity> getWordsForBookSync(String bookId) {
        return wordDao.getWordsByBookIdSync(bookId);
    }
    
    /**
     * 获取词书中的单词数量
     */
    public int getWordCountForBook(String bookId) {
        return relationDao.getWordCountByBookId(bookId);
    }
    
    // ==================== 学习进度 ====================
    
    /**
     * 获取词书学习进度
     */
    public void getBookProgress(String bookId, String userId, BookProgressCallback callback) {
        executor.execute(() -> {
            try {
                int totalWords = relationDao.getWordCountByBookId(bookId);
                int learnedWords = progressDao.getLearnedCountByBook(userId, bookId);
                int masteredWords = progressDao.getMasteredCountByBook(userId, bookId);
                int reviewWords = progressDao.getTodayReviewCountByBook(userId, bookId, System.currentTimeMillis());
                
                BookProgress progress = new BookProgress();
                progress.bookId = bookId;
                progress.totalWords = totalWords;
                progress.learnedWords = learnedWords;
                progress.masteredWords = masteredWords;
                progress.reviewWords = reviewWords;
                progress.progressPercent = totalWords > 0 ? (learnedWords * 100 / totalWords) : 0;
                
                if (callback != null) {
                    callback.onSuccess(progress);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    // ==================== 统计 ====================
    
    /**
     * 获取词书总数
     */
    public int getBookCount() {
        return bookDao.getBookCount();
    }
    
    /**
     * 获取单词总数
     */
    public int getWordCount() {
        return wordDao.getWordCount();
    }
    
    // ==================== 回调接口 ====================
    
    public interface BooksCallback {
        void onSuccess(List<BookEntity> books);
        void onError(String error);
    }
    
    public interface BookProgressCallback {
        void onSuccess(BookProgress progress);
        void onError(String error);
    }
    
    // ==================== 数据类 ====================
    
    /**
     * 词书学习进度
     */
    public static class BookProgress {
        public String bookId;
        public int totalWords;
        public int learnedWords;
        public int masteredWords;
        public int reviewWords;
        public int progressPercent;
        
        public int getUnlearnedWords() {
            return totalWords - learnedWords;
        }
        
        public int getUnmasteredWords() {
            return learnedWords - masteredWords;
        }
        
        @Override
        public String toString() {
            return "BookProgress{" +
                    "bookId='" + bookId + '\'' +
                    ", totalWords=" + totalWords +
                    ", learnedWords=" + learnedWords +
                    ", masteredWords=" + masteredWords +
                    ", progressPercent=" + progressPercent +
                    '}';
        }
    }
}
