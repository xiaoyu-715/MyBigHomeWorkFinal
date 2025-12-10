package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.StudyRecordDao;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.dao.ExamDao;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import com.example.mybighomework.database.entity.ExamRecordEntity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 学习报告页面的ViewModel
 * 负责获取和处理学习数据统计
 */
public class ReportViewModelYSJ extends AndroidViewModel {
    
    private final StudyRecordDao studyRecordDao;
    private final VocabularyDao vocabularyDao;
    private final ExamDao examDao;
    private final ExecutorService executorService;
    
    // LiveData - 学习统计数据
    private final MutableLiveData<Integer> totalStudyDays = new MutableLiveData<>();
    private final MutableLiveData<Integer> masteredVocabularyCount = new MutableLiveData<>();
    private final MutableLiveData<Double> averageExamScore = new MutableLiveData<>();
    private final MutableLiveData<Long> todayStudyTime = new MutableLiveData<>();
    private final MutableLiveData<List<StudyRecordDao.DailyStudyTime>> weeklyStudyData = new MutableLiveData<>();
    private final MutableLiveData<List<ExamRecordEntity>> recentExams = new MutableLiveData<>();
    
    // 加载状态
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public ReportViewModelYSJ(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        studyRecordDao = db.studyRecordDao();
        vocabularyDao = db.vocabularyDao();
        examDao = db.examDao();
        executorService = Executors.newSingleThreadExecutor();
        
        // 初始加载数据
        loadAllData();
    }
    
    /**
     * 加载所有报告数据
     */
    public void loadAllData() {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                // 获取学习天数
                List<String> studyDays = studyRecordDao.getDistinctStudyDays();
                totalStudyDays.postValue(studyDays != null ? studyDays.size() : 0);
                
                // 获取已掌握词汇数
                int mastered = vocabularyDao.getMasteredVocabularyCount();
                masteredVocabularyCount.postValue(mastered);
                
                // 获取平均考试成绩
                List<ExamRecordEntity> exams = examDao.getAllExamRecords();
                if (exams != null && !exams.isEmpty()) {
                    double totalScore = 0;
                    for (ExamRecordEntity exam : exams) {
                        totalScore += exam.getScore();
                    }
                    averageExamScore.postValue(totalScore / exams.size());
                    recentExams.postValue(exams.subList(0, Math.min(5, exams.size())));
                } else {
                    averageExamScore.postValue(0.0);
                }
                
                // 获取今日学习时长
                long todayStart = getTodayStartTime();
                List<StudyRecordEntity> todayRecords = studyRecordDao.getStudyRecordsSince(todayStart);
                long totalTime = 0;
                if (todayRecords != null) {
                    for (StudyRecordEntity record : todayRecords) {
                        totalTime += record.getResponseTime();
                    }
                }
                todayStudyTime.postValue(totalTime);
                
                // 获取一周学习数据
                long weekStart = getWeekStartTime();
                List<StudyRecordDao.DailyStudyTime> weekData = studyRecordDao.getDailyStudyTime(weekStart);
                weeklyStudyData.postValue(weekData);
                
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("加载数据失败: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 刷新数据
     */
    public void refresh() {
        loadAllData();
    }
    
    private long getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    private long getWeekStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    // Getters for LiveData
    public LiveData<Integer> getTotalStudyDays() { return totalStudyDays; }
    public LiveData<Integer> getMasteredVocabularyCount() { return masteredVocabularyCount; }
    public LiveData<Double> getAverageExamScore() { return averageExamScore; }
    public LiveData<Long> getTodayStudyTime() { return todayStudyTime; }
    public LiveData<List<StudyRecordDao.DailyStudyTime>> getWeeklyStudyData() { return weeklyStudyData; }
    public LiveData<List<ExamRecordEntity>> getRecentExams() { return recentExams; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
