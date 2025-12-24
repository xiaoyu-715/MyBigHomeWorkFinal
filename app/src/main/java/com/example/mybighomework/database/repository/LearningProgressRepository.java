package com.example.mybighomework.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.BookWordRelationDao;
import com.example.mybighomework.database.dao.WordLearningProgressDao;
import com.example.mybighomework.database.entity.WordLearningProgressEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 学习进度数据仓库
 * 封装学习进度相关的数据访问逻辑
 */
public class LearningProgressRepository {
    
    private static final String DEFAULT_USER_ID = "default";
    
    private final WordLearningProgressDao progressDao;
    private final BookWordRelationDao relationDao;
    private final ExecutorService executor;
    
    public LearningProgressRepository(Context context) {
        this(AppDatabase.getInstance(context));
    }
    
    public LearningProgressRepository(AppDatabase database) {
        progressDao = database.wordLearningProgressDao();
        relationDao = database.bookWordRelationDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 更新学习进度（简化版，供BookLearningActivity使用）
     */
    public void updateProgress(String userId, String wordId, String bookId, boolean isCorrect) {
        executor.execute(() -> {
            try {
                WordLearningProgressEntity progress = progressDao.getProgress(userId, wordId);
                if (progress == null) {
                    progress = new WordLearningProgressEntity(userId, wordId, bookId);
                }
                
                progress.recordAnswer(isCorrect);
                
                if (progress.getId() == 0) {
                    progressDao.insert(progress);
                } else {
                    progressDao.update(progress);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    // ==================== 进度记录 ====================
    
    /**
     * 获取或创建单词学习进度
     */
    public void getOrCreateProgress(String wordId, String bookId, ProgressCallback callback) {
        getOrCreateProgress(DEFAULT_USER_ID, wordId, bookId, callback);
    }
    
    /**
     * 获取或创建单词学习进度（指定用户）
     */
    public void getOrCreateProgress(String userId, String wordId, String bookId, ProgressCallback callback) {
        executor.execute(() -> {
            try {
                WordLearningProgressEntity progress = progressDao.getProgress(userId, wordId);
                if (progress == null) {
                    progress = new WordLearningProgressEntity(userId, wordId, bookId);
                    long id = progressDao.insert(progress);
                    progress.setId((int) id);
                }
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
    
    /**
     * 记录答题结果
     */
    public void recordAnswer(String wordId, String bookId, boolean isCorrect, ProgressCallback callback) {
        recordAnswer(DEFAULT_USER_ID, wordId, bookId, isCorrect, callback);
    }
    
    /**
     * 记录答题结果（指定用户）
     */
    public void recordAnswer(String userId, String wordId, String bookId, boolean isCorrect, ProgressCallback callback) {
        executor.execute(() -> {
            try {
                WordLearningProgressEntity progress = progressDao.getProgress(userId, wordId);
                if (progress == null) {
                    progress = new WordLearningProgressEntity(userId, wordId, bookId);
                }
                
                progress.recordAnswer(isCorrect);
                
                if (progress.getId() == 0) {
                    long id = progressDao.insert(progress);
                    progress.setId((int) id);
                } else {
                    progressDao.update(progress);
                }
                
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
    
    /**
     * 更新学习进度
     */
    public void updateProgress(WordLearningProgressEntity progress) {
        executor.execute(() -> progressDao.update(progress));
    }
    
    // ==================== 进度查询 ====================
    
    /**
     * 获取词书学习进度
     */
    public LiveData<List<WordLearningProgressEntity>> getProgressByBook(String bookId) {
        return getProgressByBook(DEFAULT_USER_ID, bookId);
    }
    
    /**
     * 获取词书学习进度（指定用户）
     */
    public LiveData<List<WordLearningProgressEntity>> getProgressByBook(String userId, String bookId) {
        return progressDao.getProgressByBook(userId, bookId);
    }
    
    /**
     * 获取词书学习进度（同步）
     */
    public List<WordLearningProgressEntity> getProgressByBookSync(String bookId) {
        return getProgressByBookSync(DEFAULT_USER_ID, bookId);
    }
    
    /**
     * 获取词书学习进度（同步，指定用户）
     */
    public List<WordLearningProgressEntity> getProgressByBookSync(String userId, String bookId) {
        return progressDao.getProgressByBookSync(userId, bookId);
    }
    
    /**
     * 获取所有学习进度
     */
    public LiveData<List<WordLearningProgressEntity>> getAllProgress() {
        return getAllProgress(DEFAULT_USER_ID);
    }
    
    /**
     * 获取所有学习进度（指定用户）
     */
    public LiveData<List<WordLearningProgressEntity>> getAllProgress(String userId) {
        return progressDao.getAllProgress(userId);
    }
    
    // ==================== 复习队列 ====================
    
    /**
     * 获取需要复习的单词
     */
    public LiveData<List<WordLearningProgressEntity>> getWordsNeedingReview() {
        return getWordsNeedingReview(DEFAULT_USER_ID);
    }
    
    /**
     * 获取需要复习的单词（指定用户）
     */
    public LiveData<List<WordLearningProgressEntity>> getWordsNeedingReview(String userId) {
        return progressDao.getWordsNeedingReview(userId, System.currentTimeMillis());
    }
    
    /**
     * 获取需要复习的单词（同步）
     */
    public List<WordLearningProgressEntity> getWordsNeedingReviewSync() {
        return getWordsNeedingReviewSync(DEFAULT_USER_ID);
    }
    
    /**
     * 获取需要复习的单词（同步，指定用户）
     */
    public List<WordLearningProgressEntity> getWordsNeedingReviewSync(String userId) {
        return progressDao.getWordsNeedingReviewSync(userId, System.currentTimeMillis());
    }
    
    /**
     * 获取某词书需要复习的单词
     */
    public List<WordLearningProgressEntity> getWordsNeedingReviewByBook(String bookId) {
        return getWordsNeedingReviewByBook(DEFAULT_USER_ID, bookId);
    }
    
    /**
     * 获取某词书需要复习的单词（指定用户）
     */
    public List<WordLearningProgressEntity> getWordsNeedingReviewByBook(String userId, String bookId) {
        return progressDao.getWordsNeedingReviewByBook(userId, bookId, System.currentTimeMillis());
    }
    
    /**
     * 获取已掌握的单词
     */
    public LiveData<List<WordLearningProgressEntity>> getMasteredWords() {
        return getMasteredWords(DEFAULT_USER_ID);
    }
    
    /**
     * 获取已掌握的单词（指定用户）
     */
    public LiveData<List<WordLearningProgressEntity>> getMasteredWords(String userId) {
        return progressDao.getMasteredWords(userId);
    }
    
    // ==================== 统计 ====================
    
    /**
     * 获取学习统计
     */
    public void getLearningStats(LearningStatsCallback callback) {
        getLearningStats(DEFAULT_USER_ID, callback);
    }
    
    /**
     * 获取学习统计（指定用户）
     */
    public void getLearningStats(String userId, LearningStatsCallback callback) {
        executor.execute(() -> {
            try {
                LearningStats stats = new LearningStats();
                stats.totalLearned = progressDao.getTotalLearnedCount(userId);
                stats.totalMastered = progressDao.getTotalMasteredCount(userId);
                stats.todayReview = progressDao.getTodayReviewCount(userId, System.currentTimeMillis());
                stats.totalCorrect = progressDao.getTotalCorrectCount(userId);
                stats.totalWrong = progressDao.getTotalWrongCount(userId);
                
                if (callback != null) {
                    callback.onSuccess(stats);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取词书学习统计
     */
    public void getBookLearningStats(String bookId, BookLearningStatsCallback callback) {
        getBookLearningStats(DEFAULT_USER_ID, bookId, callback);
    }
    
    /**
     * 获取词书学习统计（指定用户）
     */
    public void getBookLearningStats(String userId, String bookId, BookLearningStatsCallback callback) {
        executor.execute(() -> {
            try {
                BookLearningStats stats = new BookLearningStats();
                stats.bookId = bookId;
                stats.totalWords = relationDao.getWordCountByBookId(bookId);
                stats.learnedWords = progressDao.getLearnedCountByBook(userId, bookId);
                stats.masteredWords = progressDao.getMasteredCountByBook(userId, bookId);
                stats.todayReview = progressDao.getTodayReviewCountByBook(userId, bookId, System.currentTimeMillis());
                
                if (callback != null) {
                    callback.onSuccess(stats);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    // ==================== 回调接口 ====================
    
    public interface ProgressCallback {
        void onSuccess(WordLearningProgressEntity progress);
        void onError(String error);
    }
    
    public interface LearningStatsCallback {
        void onSuccess(LearningStats stats);
        void onError(String error);
    }
    
    public interface BookLearningStatsCallback {
        void onSuccess(BookLearningStats stats);
        void onError(String error);
    }
    
    // ==================== 数据类 ====================
    
    /**
     * 总体学习统计
     */
    public static class LearningStats {
        public int totalLearned;
        public int totalMastered;
        public int todayReview;
        public int totalCorrect;
        public int totalWrong;
        
        public float getAccuracy() {
            int total = totalCorrect + totalWrong;
            return total > 0 ? (float) totalCorrect / total : 0f;
        }
        
        public int getAccuracyPercent() {
            return Math.round(getAccuracy() * 100);
        }
    }
    
    /**
     * 词书学习统计
     */
    public static class BookLearningStats {
        public String bookId;
        public int totalWords;
        public int learnedWords;
        public int masteredWords;
        public int todayReview;
        
        public int getUnlearnedWords() {
            return totalWords - learnedWords;
        }
        
        public int getProgressPercent() {
            return totalWords > 0 ? (learnedWords * 100 / totalWords) : 0;
        }
        
        public int getMasteryPercent() {
            return learnedWords > 0 ? (masteredWords * 100 / learnedWords) : 0;
        }
    }
}
