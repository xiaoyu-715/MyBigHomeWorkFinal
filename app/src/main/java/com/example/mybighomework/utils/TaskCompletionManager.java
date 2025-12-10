package com.example.mybighomework.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 任务完成管理器
 * 管理每日任务的完成状态和进度跟踪
 */
public class TaskCompletionManager {
    
    private static final String PREFS_NAME = "daily_tasks";
    private static final String KEY_CURRENT_DATE = "current_date";
    private static final String KEY_VOCABULARY_COUNT = "vocabulary_count";
    private static final String KEY_EXAM_ANSWER_COUNT = "exam_answer_count";
    
    // 任务完成目标
    private static final int VOCABULARY_TARGET = 20;
    private static final int EXAM_ANSWER_TARGET = 20;
    
    private static TaskCompletionManager instance;
    private final SharedPreferences sharedPreferences;
    private final SimpleDateFormat dateFormat;
    
    private TaskCompletionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        checkAndResetIfNewDay();
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized TaskCompletionManager getInstance(Context context) {
        if (instance == null) {
            instance = new TaskCompletionManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 检查是否是新的一天，如果是则重置计数
     */
    private void checkAndResetIfNewDay() {
        String today = dateFormat.format(new Date());
        String savedDate = sharedPreferences.getString(KEY_CURRENT_DATE, "");
        
        if (!today.equals(savedDate)) {
            // 新的一天，重置所有计数
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_DATE, today);
            editor.putInt(KEY_VOCABULARY_COUNT, 0);
            editor.putInt(KEY_EXAM_ANSWER_COUNT, 0);
            editor.apply();
        }
    }
    
    /**
     * 增加词汇学习计数
     * 每答一道词汇题就调用一次
     * 达到目标后自动标记任务完成
     */
    public void incrementVocabularyCount() {
        checkAndResetIfNewDay();
        
        int currentCount = sharedPreferences.getInt(KEY_VOCABULARY_COUNT, 0);
        currentCount++;
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_VOCABULARY_COUNT, currentCount);
        
        // 如果达到目标，自动标记任务完成
        if (currentCount >= VOCABULARY_TARGET) {
            String today = dateFormat.format(new Date());
            editor.putBoolean(today + "_vocabulary", true);
        }
        
        editor.apply();
    }
    
    /**
     * 增加考试答题计数
     * 每答一道考试题就调用一次
     * 达到目标后自动标记任务完成
     */
    public void incrementExamAnswerCount() {
        checkAndResetIfNewDay();
        
        int currentCount = sharedPreferences.getInt(KEY_EXAM_ANSWER_COUNT, 0);
        currentCount++;
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_EXAM_ANSWER_COUNT, currentCount);
        
        // 如果达到目标，自动标记任务完成
        if (currentCount >= EXAM_ANSWER_TARGET) {
            String today = dateFormat.format(new Date());
            editor.putBoolean(today + "_exam_practice", true);
        }
        
        editor.apply();
    }
    
    /**
     * 标记每日一句任务完成
     * 打开页面即完成
     */
    public void markDailySentenceCompleted() {
        checkAndResetIfNewDay();
        
        String today = dateFormat.format(new Date());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(today + "_daily_sentence", true);
        editor.apply();
    }
    
    /**
     * 获取今日词汇学习数
     */
    public int getTodayVocabularyCount() {
        checkAndResetIfNewDay();
        return sharedPreferences.getInt(KEY_VOCABULARY_COUNT, 0);
    }
    
    /**
     * 获取今日考试答题数
     */
    public int getTodayExamAnswerCount() {
        checkAndResetIfNewDay();
        return sharedPreferences.getInt(KEY_EXAM_ANSWER_COUNT, 0);
    }
    
    /**
     * 获取词汇学习目标
     */
    public int getVocabularyTarget() {
        return VOCABULARY_TARGET;
    }
    
    /**
     * 获取考试答题目标
     */
    public int getExamAnswerTarget() {
        return EXAM_ANSWER_TARGET;
    }
    
    /**
     * 检查词汇任务是否完成
     */
    public boolean isVocabularyTaskCompleted() {
        checkAndResetIfNewDay();
        String today = dateFormat.format(new Date());
        return sharedPreferences.getBoolean(today + "_vocabulary", false);
    }
    
    /**
     * 检查考试任务是否完成
     */
    public boolean isExamTaskCompleted() {
        checkAndResetIfNewDay();
        String today = dateFormat.format(new Date());
        return sharedPreferences.getBoolean(today + "_exam_practice", false);
    }
    
    /**
     * 检查每日一句任务是否完成
     */
    public boolean isDailySentenceTaskCompleted() {
        checkAndResetIfNewDay();
        String today = dateFormat.format(new Date());
        return sharedPreferences.getBoolean(today + "_daily_sentence", false);
    }
}

