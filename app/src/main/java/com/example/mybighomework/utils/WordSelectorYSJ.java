package com.example.mybighomework.utils;

import com.example.mybighomework.database.dao.BookWordRelationDao;
import com.example.mybighomework.database.dao.WordLearningProgressDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 单词选择器
 * 根据不同的学习模式选择合适的单词
 */
public class WordSelectorYSJ {
    
    private WordLearningProgressDao progressDao;
    private BookWordRelationDao relationDao;
    
    public WordSelectorYSJ(WordLearningProgressDao progressDao, BookWordRelationDao relationDao) {
        this.progressDao = progressDao;
        this.relationDao = relationDao;
    }
    
    /**
     * 选择新词
     * @param bookId 词书ID
     * @param userId 用户ID
     * @param count 需要的单词数量
     * @return 单词ID列表
     */
    public List<String> selectNewWords(String bookId, String userId, int count) {
        List<String> unlearnedIds = progressDao.getUnlearnedWordIds(bookId, userId);
        
        if (unlearnedIds == null || unlearnedIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 打乱顺序
        Collections.shuffle(unlearnedIds);
        
        // 返回指定数量
        int actualCount = Math.min(count, unlearnedIds.size());
        return new ArrayList<>(unlearnedIds.subList(0, actualCount));
    }
    
    /**
     * 选择复习词
     * @param bookId 词书ID
     * @param userId 用户ID
     * @param count 需要的单词数量
     * @return 单词ID列表
     */
    public List<String> selectReviewWords(String bookId, String userId, int count) {
        long currentTime = System.currentTimeMillis();
        List<String> reviewIds = progressDao.getReviewWordIds(bookId, userId, currentTime, count);
        
        if (reviewIds == null) {
            return new ArrayList<>();
        }
        
        return reviewIds;
    }
    
    /**
     * 随机选择单词
     * @param bookId 词书ID
     * @param count 需要的单词数量
     * @return 单词ID列表
     */
    public List<String> selectRandomWords(String bookId, int count) {
        List<String> allWordIds = relationDao.getWordIdsByBookId(bookId);
        
        if (allWordIds == null || allWordIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 打乱顺序
        Collections.shuffle(allWordIds);
        
        // 返回指定数量
        int actualCount = Math.min(count, allWordIds.size());
        return new ArrayList<>(allWordIds.subList(0, actualCount));
    }
    
    /**
     * 根据学习模式选择单词
     * @param bookId 词书ID
     * @param userId 用户ID
     * @param mode 学习模式: "learn"(新词), "review"(复习), "random"(随机)
     * @param count 需要的单词数量
     * @return 单词ID列表
     */
    public List<String> selectWords(String bookId, String userId, String mode, int count) {
        if (mode == null) {
            mode = "learn";
        }
        
        switch (mode.toLowerCase()) {
            case "review":
                return selectReviewWords(bookId, userId, count);
            case "random":
                return selectRandomWords(bookId, count);
            case "learn":
            default:
                return selectNewWords(bookId, userId, count);
        }
    }
}
