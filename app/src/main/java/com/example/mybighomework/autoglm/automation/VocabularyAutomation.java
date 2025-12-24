package com.example.mybighomework.autoglm.automation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;

import java.util.List;
import java.util.Random;

public class VocabularyAutomation implements AutomationTask {
    
    private static final String TAG = "VocabularyAutomation";
    
    private Context context;
    private int wordCount;
    private boolean cancelled = false;
    private Handler mainHandler;
    
    public VocabularyAutomation(Context context, int wordCount) {
        this.context = context;
        this.wordCount = wordCount;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public void execute(AutomationCallback callback) {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                mainHandler.post(callback::onStart);
                
                // 1. 加载词汇
                mainHandler.post(() -> callback.onProgress(0, wordCount, "正在加载词汇..."));
                
                AppDatabase database = AppDatabase.getInstance(context);
                VocabularyDao vocabularyDao = database.vocabularyDao();
                List<VocabularyRecordEntity> allWords = vocabularyDao.getAllVocabulary();
                
                if (allWords == null || allWords.isEmpty()) {
                    throw new Exception("词汇库为空");
                }
                
                // 2. 随机选择词汇
                List<VocabularyRecordEntity> selectedWords = selectRandomWords(allWords, wordCount);
                
                // 3. 自动学习每个单词
                int correctCount = 0;
                for (int i = 0; i < selectedWords.size(); i++) {
                    if (cancelled) {
                        throw new InterruptedException("任务已取消");
                    }
                    
                    VocabularyRecordEntity word = selectedWords.get(i);
                    final int currentIndex = i + 1;
                    
                    mainHandler.post(() -> callback.onProgress(
                        currentIndex, 
                        wordCount, 
                        "正在学习：" + word.getWord()
                    ));
                    
                    // 模拟学习过程（延迟500ms）
                    Thread.sleep(500);
                    
                    // 自动"答题"（模拟正确率85%）
                    boolean correct = autoAnswer(word);
                    if (correct) {
                        correctCount++;
                    }
                    
                    // 保存学习记录
                    saveVocabularyRecord(word, correct);
                }
                
                // 4. 生成结果
                long duration = System.currentTimeMillis() - startTime;
                double accuracy = (double) correctCount / wordCount * 100;
                
                AutomationResult result = new AutomationResult();
                result.setSuccess(true);
                result.setItemsProcessed(wordCount);
                result.setItemsTotal(wordCount);
                result.setAccuracy(accuracy);
                result.setDuration(duration);
                result.setMessage(String.format(
                    "✅ 已完成学习%d个单词！\n" +
                    "- 学习时长：%d秒\n" +
                    "- 正确率：%.1f%%\n" +
                    "- 掌握：%d个\n" +
                    "- 需复习：%d个",
                    wordCount,
                    duration / 1000,
                    accuracy,
                    correctCount,
                    wordCount - correctCount
                ));
                
                mainHandler.post(() -> callback.onComplete(result));
                
            } catch (InterruptedException e) {
                Log.w(TAG, "任务被取消", e);
                mainHandler.post(() -> callback.onError(e));
            } catch (Exception e) {
                Log.e(TAG, "自动化任务执行失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    @Override
    public void cancel() {
        cancelled = true;
    }
    
    @Override
    public String getTaskType() {
        return "vocabulary_learning";
    }
    
    @Override
    public String getTaskDescription() {
        return "自动学习" + wordCount + "个单词";
    }
    
    private List<VocabularyRecordEntity> selectRandomWords(List<VocabularyRecordEntity> allWords, int count) {
        List<VocabularyRecordEntity> selected = new java.util.ArrayList<>();
        Random random = new Random();
        
        int actualCount = Math.min(count, allWords.size());
        java.util.Set<Integer> selectedIndices = new java.util.HashSet<>();
        
        while (selectedIndices.size() < actualCount) {
            int index = random.nextInt(allWords.size());
            if (selectedIndices.add(index)) {
                selected.add(allWords.get(index));
            }
        }
        
        return selected;
    }
    
    private boolean autoAnswer(VocabularyRecordEntity word) {
        // 模拟85%的正确率
        Random random = new Random();
        return random.nextDouble() < 0.85;
    }
    
    private void saveVocabularyRecord(VocabularyRecordEntity word, boolean correct) {
        try {
            AppDatabase database = AppDatabase.getInstance(context);
            
            // 这里应该保存学习记录到数据库
            // 简化实现：仅记录日志
            Log.d(TAG, "保存学习记录：" + word.getWord() + " - " + (correct ? "正确" : "错误"));
            
        } catch (Exception e) {
            Log.e(TAG, "保存学习记录失败", e);
        }
    }
}
