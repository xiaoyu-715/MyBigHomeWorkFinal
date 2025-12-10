package com.example.mybighomework.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 模块学习统计管理器
 * 用于记录和管理各模块的答对题数统计
 */
public class ModuleStatisticsManager {
    
    private static final String PREFS_NAME = "module_statistics";
    
    // 各模块统计的键名
    private static final String KEY_VOCABULARY_CORRECT_COUNT = "vocabulary_correct_count";
    private static final String KEY_EXAM_PRACTICE_CORRECT_COUNT = "exam_practice_correct_count";
    private static final String KEY_MOCK_EXAM_CORRECT_COUNT = "mock_exam_correct_count";
    private static final String KEY_ERROR_QUESTION_CORRECT_COUNT = "error_question_correct_count";
    
    private SharedPreferences preferences;
    private static ModuleStatisticsManager instance;
    
    /**
     * 私有构造函数
     */
    private ModuleStatisticsManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized ModuleStatisticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new ModuleStatisticsManager(context);
        }
        return instance;
    }
    
    /**
     * 词汇训练模块：增加答对题数
     */
    public void incrementVocabularyCorrectCount() {
        incrementCount(KEY_VOCABULARY_CORRECT_COUNT);
    }
    
    /**
     * 真题练习模块：增加答对题数
     */
    public void incrementExamPracticeCorrectCount() {
        incrementCount(KEY_EXAM_PRACTICE_CORRECT_COUNT);
    }
    
    /**
     * 模拟考试模块：增加答对题数
     */
    public void incrementMockExamCorrectCount() {
        incrementCount(KEY_MOCK_EXAM_CORRECT_COUNT);
    }
    
    /**
     * 错题本模块：增加答对题数
     */
    public void incrementErrorQuestionCorrectCount() {
        incrementCount(KEY_ERROR_QUESTION_CORRECT_COUNT);
    }
    
    /**
     * 获取词汇训练模块答对题数
     */
    public int getVocabularyCorrectCount() {
        return preferences.getInt(KEY_VOCABULARY_CORRECT_COUNT, 0);
    }
    
    /**
     * 获取真题练习模块答对题数
     */
    public int getExamPracticeCorrectCount() {
        return preferences.getInt(KEY_EXAM_PRACTICE_CORRECT_COUNT, 0);
    }
    
    /**
     * 获取模拟考试模块答对题数
     */
    public int getMockExamCorrectCount() {
        return preferences.getInt(KEY_MOCK_EXAM_CORRECT_COUNT, 0);
    }
    
    /**
     * 获取错题本模块答对题数
     */
    public int getErrorQuestionCorrectCount() {
        return preferences.getInt(KEY_ERROR_QUESTION_CORRECT_COUNT, 0);
    }
    
    /**
     * 重置所有模块统计数据
     */
    public void resetAllStatistics() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_VOCABULARY_CORRECT_COUNT, 0);
        editor.putInt(KEY_EXAM_PRACTICE_CORRECT_COUNT, 0);
        editor.putInt(KEY_MOCK_EXAM_CORRECT_COUNT, 0);
        editor.putInt(KEY_ERROR_QUESTION_CORRECT_COUNT, 0);
        editor.apply();
    }
    
    /**
     * 重置指定模块的统计数据
     */
    public void resetModuleStatistics(String module) {
        SharedPreferences.Editor editor = preferences.edit();
        switch (module) {
            case "vocabulary":
                editor.putInt(KEY_VOCABULARY_CORRECT_COUNT, 0);
                break;
            case "exam_practice":
                editor.putInt(KEY_EXAM_PRACTICE_CORRECT_COUNT, 0);
                break;
            case "mock_exam":
                editor.putInt(KEY_MOCK_EXAM_CORRECT_COUNT, 0);
                break;
            case "error_question":
                editor.putInt(KEY_ERROR_QUESTION_CORRECT_COUNT, 0);
                break;
        }
        editor.apply();
    }
    
    /**
     * 通用的增加计数方法
     */
    private void incrementCount(String key) {
        int currentCount = preferences.getInt(key, 0);
        preferences.edit().putInt(key, currentCount + 1).apply();
    }
    
    /**
     * 获取所有模块的总答对题数
     */
    public int getTotalCorrectCount() {
        return getVocabularyCorrectCount() + 
               getExamPracticeCorrectCount() + 
               getMockExamCorrectCount() + 
               getErrorQuestionCorrectCount();
    }
}

