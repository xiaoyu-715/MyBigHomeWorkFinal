package com.example.mybighomework.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VocabularyRecordRepository {
    private VocabularyDao vocabularyDao;
    private ExecutorService executorService;
    
    public VocabularyRecordRepository(VocabularyDao vocabularyDao) {
        this.vocabularyDao = vocabularyDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    // 新增构造函数，支持Application参数
    public VocabularyRecordRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        this.vocabularyDao = database.vocabularyDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    // 获取掌握的词汇数量
    public int getMasteredVocabularyCount() {
        return vocabularyDao.getMasteredVocabularyCount();
    }
    
    // 获取总词汇数量
    public int getTotalVocabularyCount() {
        return vocabularyDao.getTotalVocabularyCount();
    }
    
    // 获取未掌握的词汇数量
    public int getUnmasteredVocabularyCount() {
        return vocabularyDao.getUnmasteredVocabularyCount();
    }
    
    // 获取所有词汇
    public List<VocabularyRecordEntity> getAllVocabulary() {
        return vocabularyDao.getAllVocabulary();
    }
    
    // 获取掌握的词汇
    public List<VocabularyRecordEntity> getMasteredVocabulary() {
        return vocabularyDao.getMasteredVocabulary();
    }
    
    // 获取未掌握的词汇
    public List<VocabularyRecordEntity> getUnmasteredVocabulary() {
        return vocabularyDao.getUnmasteredVocabulary();
    }
    
    // 添加词汇记录
    public long addVocabularyRecord(VocabularyRecordEntity vocabulary) {
        return vocabularyDao.insert(vocabulary);
    }
    
    // 批量添加词汇记录
    public void addVocabularyRecords(List<VocabularyRecordEntity> vocabularies) {
        executorService.execute(() -> {
            for (VocabularyRecordEntity vocab : vocabularies) {
                try {
                    vocabularyDao.insert(vocab);
                } catch (Exception e) {
                    // 忽略重复插入错误
                }
            }
        });
    }
    
    // 更新词汇记录
    public void updateVocabularyRecord(VocabularyRecordEntity vocabulary) {
        vocabularyDao.update(vocabulary);
    }
    
    // 删除词汇记录
    public void deleteVocabularyRecord(VocabularyRecordEntity vocabulary) {
        vocabularyDao.delete(vocabulary);
    }
    
    // 根据ID获取词汇
    public VocabularyRecordEntity getVocabularyById(int id) {
        return vocabularyDao.getVocabularyById(id);
    }
    
    // 根据单词获取词汇
    public VocabularyRecordEntity getVocabularyByWord(String word) {
        return vocabularyDao.getVocabularyByWord(word);
    }
    
    // 增加正确次数
    public void incrementCorrectCount(int id, long studyTime) {
        vocabularyDao.incrementCorrectCount(id, studyTime);
    }
    
    // 增加错误次数
    public void incrementWrongCount(int id, long studyTime) {
        vocabularyDao.incrementWrongCount(id, studyTime);
    }
    
    // 更新掌握状态
    public void updateMasteryStatus(int id, boolean mastered, long studyTime) {
        vocabularyDao.updateMasteryStatus(id, mastered, studyTime);
    }
    
    // 搜索词汇
    public List<VocabularyRecordEntity> searchVocabulary(String keyword) {
        return vocabularyDao.searchVocabulary(keyword);
    }
    
    // 获取随机词汇
    public List<VocabularyRecordEntity> getRandomVocabulary(int limit) {
        return vocabularyDao.getRandomVocabulary(limit);
    }
    
    // 获取需要复习的词汇
    public List<VocabularyRecordEntity> getReviewVocabulary(int limit) {
        return vocabularyDao.getReviewVocabulary(limit);
    }
    
    // ==================== LiveData 方法（推荐使用，自动异步） ====================
    
    // 获取所有词汇（LiveData）
    public LiveData<List<VocabularyRecordEntity>> getAllVocabularyLive() {
        return vocabularyDao.getAllVocabularyLive();
    }
    
    // 获取掌握的词汇（LiveData）
    public LiveData<List<VocabularyRecordEntity>> getMasteredVocabularyLive() {
        return vocabularyDao.getMasteredVocabularyLive();
    }
    
    // 获取未掌握的词汇（LiveData）
    public LiveData<List<VocabularyRecordEntity>> getUnmasteredVocabularyLive() {
        return vocabularyDao.getUnmasteredVocabularyLive();
    }
    
    // 获取词汇总数（LiveData）
    public LiveData<Integer> getTotalVocabularyCountLive() {
        return vocabularyDao.getTotalVocabularyCountLive();
    }
    
    // 获取掌握词汇数量（LiveData）
    public LiveData<Integer> getMasteredVocabularyCountLive() {
        return vocabularyDao.getMasteredVocabularyCountLive();
    }
    
    // 获取未掌握词汇数量（LiveData）
    public LiveData<Integer> getUnmasteredVocabularyCountLive() {
        return vocabularyDao.getUnmasteredVocabularyCountLive();
    }
    
    // ==================== 异步写操作方法 ====================
    
    // 异步添加词汇记录
    public void addVocabularyRecordAsync(VocabularyRecordEntity vocabulary, OnCompleteListener listener) {
        executorService.execute(() -> {
            try {
                long id = vocabularyDao.insert(vocabulary);
                if (listener != null) {
                    listener.onSuccess(id);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }
    
    // 异步更新词汇记录
    public void updateVocabularyRecordAsync(VocabularyRecordEntity vocabulary, OnCompleteListener listener) {
        executorService.execute(() -> {
            try {
                vocabularyDao.update(vocabulary);
                if (listener != null) {
                    listener.onSuccess(0);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }
    
    // 异步删除词汇记录
    public void deleteVocabularyRecordAsync(VocabularyRecordEntity vocabulary, OnCompleteListener listener) {
        executorService.execute(() -> {
            try {
                vocabularyDao.delete(vocabulary);
                if (listener != null) {
                    listener.onSuccess(0);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }
    
    // 异步增加正确次数
    public void incrementCorrectCountAsync(int id, long studyTime) {
        executorService.execute(() -> {
            vocabularyDao.incrementCorrectCount(id, studyTime);
        });
    }
    
    // 异步增加错误次数
    public void incrementWrongCountAsync(int id, long studyTime) {
        executorService.execute(() -> {
            vocabularyDao.incrementWrongCount(id, studyTime);
        });
    }
    
    // 异步更新掌握状态
    public void updateMasteryStatusAsync(int id, boolean mastered, long studyTime) {
        executorService.execute(() -> {
            vocabularyDao.updateMasteryStatus(id, mastered, studyTime);
        });
    }
    
    // 关闭线程池
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // 回调接口
    public interface OnCompleteListener {
        void onSuccess(long result);
        void onError(Exception e);
    }
}