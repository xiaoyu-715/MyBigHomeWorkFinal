package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.WrongQuestionDao;
import com.example.mybighomework.database.entity.WrongQuestionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 错题本页面的ViewModel
 * 负责错题的增删改查和分类管理
 */
public class WrongQuestionViewModelYSJ extends AndroidViewModel {
    
    private final WrongQuestionDao wrongQuestionDao;
    private final ExecutorService executorService;
    
    // LiveData
    private final MutableLiveData<List<WrongQuestionEntity>> allWrongQuestions = new MutableLiveData<>();
    private final MutableLiveData<List<WrongQuestionEntity>> filteredWrongQuestions = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("全部");
    
    public WrongQuestionViewModelYSJ(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        wrongQuestionDao = db.wrongQuestionDao();
        executorService = Executors.newSingleThreadExecutor();
        
        loadAllWrongQuestions();
    }
    
    /**
     * 加载所有错题
     */
    public void loadAllWrongQuestions() {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                List<WrongQuestionEntity> questions = wrongQuestionDao.getAllWrongQuestions();
                allWrongQuestions.postValue(questions);
                filteredWrongQuestions.postValue(questions);
                totalCount.postValue(questions != null ? questions.size() : 0);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("加载错题失败: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 根据类型筛选错题
     */
    public void filterByType(String type) {
        currentFilter.setValue(type);
        List<WrongQuestionEntity> all = allWrongQuestions.getValue();
        if (all == null) {
            filteredWrongQuestions.setValue(new ArrayList<>());
            return;
        }
        
        if ("全部".equals(type)) {
            filteredWrongQuestions.setValue(all);
        } else {
            List<WrongQuestionEntity> filtered = new ArrayList<>();
            for (WrongQuestionEntity question : all) {
                if (type.equals(question.getCategory())) {
                    filtered.add(question);
                }
            }
            filteredWrongQuestions.setValue(filtered);
        }
    }
    
    /**
     * 删除错题
     */
    public void deleteWrongQuestion(WrongQuestionEntity question) {
        executorService.execute(() -> {
            try {
                wrongQuestionDao.deleteById(question.getId());
                loadAllWrongQuestions(); // 刷新列表
            } catch (Exception e) {
                errorMessage.postValue("删除失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 标记错题为已掌握
     */
    public void markAsMastered(WrongQuestionEntity question) {
        executorService.execute(() -> {
            try {
                question.setMastered(true);
                wrongQuestionDao.update(question);
                loadAllWrongQuestions();
            } catch (Exception e) {
                errorMessage.postValue("更新失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 刷新数据
     */
    public void refresh() {
        loadAllWrongQuestions();
    }
    
    // Getters
    public LiveData<List<WrongQuestionEntity>> getAllWrongQuestions() { return allWrongQuestions; }
    public LiveData<List<WrongQuestionEntity>> getFilteredWrongQuestions() { return filteredWrongQuestions; }
    public LiveData<Integer> getTotalCount() { return totalCount; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getCurrentFilter() { return currentFilter; }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
