package com.example.mybighomework.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.ExampleSentenceDao;
import com.example.mybighomework.database.entity.ExampleSentenceEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 例句数据仓库
 * 封装例句相关的数据访问逻辑
 */
public class ExampleSentenceRepositoryYSJ {
    
    private final ExampleSentenceDao sentenceDao;
    private final ExecutorService executor;
    
    public ExampleSentenceRepositoryYSJ(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        sentenceDao = database.exampleSentenceDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 插入例句
     */
    public void insert(ExampleSentenceEntity sentence) {
        executor.execute(() -> sentenceDao.insert(sentence));
    }
    
    /**
     * 批量插入例句
     */
    public void insertAll(List<ExampleSentenceEntity> sentences) {
        executor.execute(() -> sentenceDao.insertAll(sentences));
    }
    
    /**
     * 根据单词ID获取例句（LiveData）
     */
    public LiveData<List<ExampleSentenceEntity>> getExamplesByWordId(String wordId) {
        return sentenceDao.getExamplesByWordId(wordId);
    }
    
    /**
     * 根据单词ID获取例句（同步）
     */
    public void getExamplesByWordIdSync(String wordId, int limit, SentencesCallback callback) {
        executor.execute(() -> {
            try {
                List<ExampleSentenceEntity> sentences = sentenceDao.getExamplesByWordIdSync(wordId, limit);
                if (callback != null) {
                    callback.onSuccess(sentences);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据难度获取例句
     */
    public void getExamplesByDifficulty(String wordId, int maxDifficulty, int limit, SentencesCallback callback) {
        executor.execute(() -> {
            try {
                List<ExampleSentenceEntity> sentences = sentenceDao.getExamplesByDifficulty(wordId, maxDifficulty, limit);
                if (callback != null) {
                    callback.onSuccess(sentences);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取单词例句数量
     */
    public void getExampleCount(String wordId, CountCallback callback) {
        executor.execute(() -> {
            try {
                int count = sentenceDao.getExampleCount(wordId);
                if (callback != null) {
                    callback.onSuccess(count);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 删除单词的所有例句
     */
    public void deleteByWordId(String wordId) {
        executor.execute(() -> sentenceDao.deleteByWordId(wordId));
    }
    
    /**
     * 删除所有例句
     */
    public void deleteAll() {
        executor.execute(() -> sentenceDao.deleteAll());
    }
    
    // ==================== 回调接口 ====================
    
    public interface SentencesCallback {
        void onSuccess(List<ExampleSentenceEntity> sentences);
        void onError(String error);
    }
    
    public interface CountCallback {
        void onSuccess(int count);
        void onError(String error);
    }
}
