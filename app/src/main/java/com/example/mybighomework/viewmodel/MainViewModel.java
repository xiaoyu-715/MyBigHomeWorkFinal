package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;
import com.example.mybighomework.database.entity.ExamRecordEntity;

import java.util.List;

/**
 * MainActivity 的 ViewModel
 * 负责管理主页面的数据和业务逻辑
 */
public class MainViewModel extends AndroidViewModel {
    
    private final VocabularyRecordRepository vocabularyRepository;
    private final ExamRecordRepository examRepository;
    
    // LiveData 数据源（自动在后台线程查询，UI线程更新）
    private final LiveData<Integer> vocabularyCount;
    private final LiveData<Integer> masteredVocabularyCount;
    
    public MainViewModel(@NonNull Application application) {
        super(application);
        
        // 初始化数据库和Repository
        AppDatabase database = AppDatabase.getInstance(application);
        vocabularyRepository = new VocabularyRecordRepository(database.vocabularyDao());
        examRepository = new ExamRecordRepository(database.examDao());
        
        // 初始化 LiveData（会自动在后台线程执行查询）
        vocabularyCount = vocabularyRepository.getTotalVocabularyCountLive();
        masteredVocabularyCount = vocabularyRepository.getMasteredVocabularyCountLive();
    }
    
    // ==================== 暴露 LiveData 给 UI 层 ====================
    
    /**
     * 获取词汇总数（LiveData）
     * Activity 可以观察这个 LiveData，数据变化时自动更新UI
     */
    public LiveData<Integer> getVocabularyCount() {
        return vocabularyCount;
    }
    
    /**
     * 获取掌握的词汇数量（LiveData）
     */
    public LiveData<Integer> getMasteredVocabularyCount() {
        return masteredVocabularyCount;
    }
    
    /**
     * 获取最近考试记录（LiveData）
     */
    public LiveData<List<ExamRecordEntity>> getRecentExamRecords() {
        return examRepository.getRecentExamRecordsLive(5);
    }
    
    // ==================== 业务逻辑方法 ====================
    
    /**
     * 计算学习天数
     * 这个方法在后台线程执行，不会阻塞UI
     */
    public void calculateStudyDays(OnResultListener<Integer> listener) {
        new Thread(() -> {
            try {
                // TODO: 实现实际的学习天数计算逻辑
                int studyDays = 0; // 示例值
                listener.onSuccess(studyDays);
            } catch (Exception e) {
                listener.onError(e);
            }
        }).start();
    }
    
    /**
     * 获取平均考试分数
     */
    public void getAverageExamScore(OnResultListener<Double> listener) {
        new Thread(() -> {
            try {
                List<ExamRecordEntity> records = examRepository.getAllExamRecords();
                if (records.isEmpty()) {
                    listener.onSuccess(0.0);
                    return;
                }
                
                double totalScore = 0;
                for (ExamRecordEntity record : records) {
                    totalScore += record.getScore();
                }
                double average = totalScore / records.size();
                listener.onSuccess(average);
            } catch (Exception e) {
                listener.onError(e);
            }
        }).start();
    }
    
    // ==================== 回调接口 ====================
    
    public interface OnResultListener<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // ViewModel 被销毁时，关闭Repository中的线程池
        vocabularyRepository.shutdown();
    }
}

