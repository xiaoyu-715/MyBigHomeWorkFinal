package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.DailySentenceDao;
import com.example.mybighomework.database.entity.DailySentenceEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 每日一句页面的ViewModel
 * 负责每日句子的获取和历史记录管理
 */
public class DailySentenceViewModelYSJ extends AndroidViewModel {
    
    private final DailySentenceDao dailySentenceDao;
    private final ExecutorService executorService;
    
    // LiveData
    private final MutableLiveData<DailySentenceEntity> todaySentence = new MutableLiveData<>();
    private final MutableLiveData<List<DailySentenceEntity>> sentenceHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isFavorite = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public DailySentenceViewModelYSJ(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        dailySentenceDao = db.dailySentenceDao();
        executorService = Executors.newSingleThreadExecutor();
        
        loadTodaySentence();
    }
    
    /**
     * 加载今日句子
     */
    public void loadTodaySentence() {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                // 获取最新的句子
                List<DailySentenceEntity> recent = dailySentenceDao.getRecent(1);
                DailySentenceEntity sentence = (recent != null && !recent.isEmpty()) ? recent.get(0) : null;
                todaySentence.postValue(sentence);
                if (sentence != null) {
                    isFavorite.postValue(sentence.isFavorited());
                }
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("加载失败: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 加载历史句子
     */
    public void loadHistory() {
        executorService.execute(() -> {
            try {
                List<DailySentenceEntity> history = dailySentenceDao.getAll();
                sentenceHistory.postValue(history);
            } catch (Exception e) {
                errorMessage.postValue("加载历史失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 切换收藏状态
     */
    public void toggleFavorite() {
        DailySentenceEntity sentence = todaySentence.getValue();
        if (sentence == null) return;
        
        executorService.execute(() -> {
            try {
                boolean newFavoriteStatus = !sentence.isFavorited();
                sentence.setFavorited(newFavoriteStatus);
                dailySentenceDao.update(sentence);
                isFavorite.postValue(newFavoriteStatus);
                todaySentence.postValue(sentence);
            } catch (Exception e) {
                errorMessage.postValue("收藏失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 保存新句子
     */
    public void saveSentence(DailySentenceEntity sentence) {
        executorService.execute(() -> {
            try {
                dailySentenceDao.insert(sentence);
                todaySentence.postValue(sentence);
            } catch (Exception e) {
                errorMessage.postValue("保存失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 刷新数据
     */
    public void refresh() {
        loadTodaySentence();
    }
    
    // Getters
    public LiveData<DailySentenceEntity> getTodaySentence() { return todaySentence; }
    public LiveData<List<DailySentenceEntity>> getSentenceHistory() { return sentenceHistory; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsFavorite() { return isFavorite; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
