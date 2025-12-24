package com.example.mybighomework.utils;

import android.util.Log;

import com.example.mybighomework.database.dao.WordLearningProgressDao;
import com.example.mybighomework.database.entity.WordLearningProgressEntity;

/**
 * 学习进度管理器
 * 负责更新和管理单词学习进度
 */
public class ProgressManagerYSJ {
    
    private static final String TAG = "ProgressManager";
    
    private WordLearningProgressDao progressDao;
    
    public ProgressManagerYSJ(WordLearningProgressDao progressDao) {
        this.progressDao = progressDao;
    }
    
    /**
     * 更新学习进度
     * @param bookId 词书ID
     * @param wordId 单词ID
     * @param userId 用户ID
     * @param isCorrect 是否答对
     */
    public void updateProgress(String bookId, String wordId, String userId, boolean isCorrect) {
        try {
            WordLearningProgressEntity progress = 
                progressDao.getProgressByUserBookWord(userId, bookId, wordId);
            
            if (progress == null) {
                progress = new WordLearningProgressEntity(userId, wordId, bookId);
                Log.d(TAG, "创建新的学习进度记录: bookId=" + bookId + ", wordId=" + wordId);
            }
            
            progress.recordAnswer(isCorrect);
            
            if (progress.getId() == 0) {
                progressDao.insert(progress);
                Log.d(TAG, "插入学习进度: " + progress);
            } else {
                progressDao.update(progress);
                Log.d(TAG, "更新学习进度: " + progress);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "更新学习进度失败", e);
        }
    }
    
    /**
     * 获取词书学习统计
     * @param bookId 词书ID
     * @param userId 用户ID
     * @return 学习统计数据
     */
    public BookLearningStats getBookStats(String bookId, String userId) {
        try {
            int learnedCount = progressDao.getLearnedCountByBook(userId, bookId);
            int masteredCount = progressDao.getMasteredCountByBook(userId, bookId);
            long currentTime = System.currentTimeMillis();
            int reviewCount = progressDao.getTodayReviewCountByBook(userId, bookId, currentTime);
            
            return new BookLearningStats(learnedCount, masteredCount, reviewCount);
        } catch (Exception e) {
            Log.e(TAG, "获取词书统计失败", e);
            return new BookLearningStats(0, 0, 0);
        }
    }
    
    /**
     * 词书学习统计数据类
     */
    public static class BookLearningStats {
        private int learnedCount;
        private int masteredCount;
        private int reviewCount;
        
        public BookLearningStats(int learnedCount, int masteredCount, int reviewCount) {
            this.learnedCount = learnedCount;
            this.masteredCount = masteredCount;
            this.reviewCount = reviewCount;
        }
        
        public int getLearnedCount() { return learnedCount; }
        public int getMasteredCount() { return masteredCount; }
        public int getReviewCount() { return reviewCount; }
    }
}
