package com.example.mybighomework.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.DictionaryWordDao;
import com.example.mybighomework.database.entity.DictionaryWordEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词典单词数据仓库
 * 封装单词相关的数据访问逻辑
 */
public class DictionaryWordRepository {
    
    private final DictionaryWordDao wordDao;
    private final ExecutorService executor;
    
    public DictionaryWordRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        wordDao = database.dictionaryWordDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据ID获取单词
     */
    public void getWordById(String wordId, WordCallback callback) {
        executor.execute(() -> {
            try {
                DictionaryWordEntity word = wordDao.getWordById(wordId);
                if (callback != null) {
                    callback.onSuccess(word);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据单词文本获取单词
     */
    public void getWordByWord(String word, WordCallback callback) {
        executor.execute(() -> {
            try {
                DictionaryWordEntity entity = wordDao.getWordByWord(word);
                if (callback != null) {
                    callback.onSuccess(entity);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据词书ID获取单词列表
     */
    public LiveData<List<DictionaryWordEntity>> getWordsByBookId(String bookId) {
        return wordDao.getWordsByBookId(bookId);
    }
    
    /**
     * 根据词书ID获取单词列表（同步）
     */
    public List<DictionaryWordEntity> getWordsByBookIdSync(String bookId) {
        return wordDao.getWordsByBookIdSync(bookId);
    }
    
    /**
     * 搜索单词
     */
    public LiveData<List<DictionaryWordEntity>> searchWords(String keyword) {
        return wordDao.searchWords(keyword);
    }
    
    /**
     * 搜索单词（同步）
     */
    public List<DictionaryWordEntity> searchWordsSync(String keyword) {
        return wordDao.searchWordsSync(keyword);
    }
    
    /**
     * 按难度筛选单词
     */
    public LiveData<List<DictionaryWordEntity>> getWordsByDifficulty(int minDifficulty, int maxDifficulty) {
        return wordDao.getWordsByDifficulty(minDifficulty, maxDifficulty);
    }
    
    /**
     * 按词频筛选单词
     */
    public LiveData<List<DictionaryWordEntity>> getWordsByFrequency(float minFrequency, float maxFrequency) {
        return wordDao.getWordsByFrequency(minFrequency, maxFrequency);
    }
    
    /**
     * 获取随机单词（用于生成干扰选项）
     */
    public void getRandomWords(int limit, WordsCallback callback) {
        executor.execute(() -> {
            try {
                List<DictionaryWordEntity> words = wordDao.getRandomWords(limit);
                if (callback != null) {
                    callback.onSuccess(words);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取随机单词（排除指定单词）
     */
    public void getRandomWordsExcluding(String excludeWordId, int limit, WordsCallback callback) {
        executor.execute(() -> {
            try {
                List<DictionaryWordEntity> words = wordDao.getRandomWordsExcluding(excludeWordId, limit);
                if (callback != null) {
                    callback.onSuccess(words);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取随机单词（同步）
     */
    public List<DictionaryWordEntity> getRandomWordsSync(int limit) {
        return wordDao.getRandomWords(limit);
    }
    
    /**
     * 获取随机单词（排除指定单词，同步）
     */
    public List<DictionaryWordEntity> getRandomWordsExcludingSync(String excludeWordId, int limit) {
        return wordDao.getRandomWordsExcluding(excludeWordId, limit);
    }
    
    // ==================== 统计操作 ====================
    
    /**
     * 获取单词总数
     */
    public int getWordCount() {
        return wordDao.getWordCount();
    }
    
    /**
     * 检查单词是否存在
     */
    public boolean wordExists(String wordId) {
        return wordDao.wordExists(wordId) > 0;
    }
    
    // ==================== 回调接口 ====================
    
    public interface WordCallback {
        void onSuccess(DictionaryWordEntity word);
        void onError(String error);
    }
    
    public interface WordsCallback {
        void onSuccess(List<DictionaryWordEntity> words);
        void onError(String error);
    }
}
