package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.StudyPlan;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.StudyPlanDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.repository.StudyPlanRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * StudyPlanActivity 的 ViewModel
 * 负责管理学习计划的数据和业务逻辑
 */
public class StudyPlanViewModel extends AndroidViewModel {
    
    private final StudyPlanRepository studyPlanRepository;
    private final ExecutorService executorService;
    
    // LiveData 数据源（为Entity）
    private final LiveData<List<StudyPlanEntity>> allStudyPlans;
    private final LiveData<List<StudyPlanEntity>> activeStudyPlans;
    private final LiveData<List<StudyPlanEntity>> completedStudyPlans;
    
    // LiveData 数据源（为StudyPlan）
    private final MutableLiveData<List<StudyPlan>> allPlans = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalPlansCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> completedPlansCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> todayPlansCount = new MutableLiveData<>(0);
    
    // 计划详情相关LiveData
    private final MutableLiveData<StudyPlanRepository.PlanWithDetails> planDetails = new MutableLiveData<>();
    private final MutableLiveData<List<DailyTaskEntity>> todayTasks = new MutableLiveData<>();
    private final MutableLiveData<List<StudyPhaseEntity>> planPhases = new MutableLiveData<>();
    
    // 统计数据LiveData
    private final MutableLiveData<Integer> streakDays = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> weeklyStudyMinutes = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> completedTasksCount = new MutableLiveData<>(0);
    
    // 状态数据
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    // 日期格式化
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public StudyPlanViewModel(@NonNull Application application) {
        super(application);
        
        // 初始化 Repository（使用完整构造函数以支持阶段和任务操作）
        AppDatabase database = AppDatabase.getInstance(application);
        studyPlanRepository = new StudyPlanRepository(
            application,
            database.studyPlanDao(),
            database.studyPhaseDao(),
            database.dailyTaskDao()
        );
        executorService = Executors.newSingleThreadExecutor();
        
        // 初始化 LiveData
        allStudyPlans = studyPlanRepository.getAllStudyPlansLive();
        activeStudyPlans = studyPlanRepository.getActiveStudyPlansLive();
        completedStudyPlans = studyPlanRepository.getCompletedStudyPlansLive();
    }
    
    // ==================== LiveData Getters（为StudyPlan） ====================
    
    public LiveData<List<StudyPlan>> getAllPlans() {
        return allPlans;
    }
    
    public LiveData<Integer> getTotalPlansCount() {
        return totalPlansCount;
    }
    
    public LiveData<Integer> getCompletedPlansCount() {
        return completedPlansCount;
    }
    
    public LiveData<Integer> getTodayPlansCount() {
        return todayPlansCount;
    }
    
    // ==================== LiveData Getters（为Entity） ====================
    
    public LiveData<List<StudyPlanEntity>> getAllStudyPlans() {
        return allStudyPlans;
    }
    
    public LiveData<List<StudyPlanEntity>> getActiveStudyPlans() {
        return activeStudyPlans;
    }
    
    public LiveData<List<StudyPlanEntity>> getCompletedStudyPlans() {
        return completedStudyPlans;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    // ==================== 计划详情相关 Getters ====================
    
    public LiveData<StudyPlanRepository.PlanWithDetails> getPlanDetails() {
        return planDetails;
    }
    
    public LiveData<List<DailyTaskEntity>> getTodayTasks() {
        return todayTasks;
    }
    
    public LiveData<List<StudyPhaseEntity>> getPlanPhases() {
        return planPhases;
    }
    
    // ==================== 统计数据 Getters ====================
    
    public LiveData<Integer> getStreakDays() {
        return streakDays;
    }
    
    public LiveData<Integer> getWeeklyStudyMinutes() {
        return weeklyStudyMinutes;
    }
    
    public LiveData<Integer> getCompletedTasksCount() {
        return completedTasksCount;
    }
    
    // ==================== 业务逻辑方法 ====================
    
    /**
     * 加载所有计划
     */
    public void loadAllPlans() {
        android.util.Log.d("StudyPlanViewModel", "loadAllPlans 开始执行");
        executorService.execute(() -> {
            try {
                android.util.Log.d("StudyPlanViewModel", "后台线程开始加载计划");
                List<StudyPlan> plans = studyPlanRepository.getAllStudyPlans();
                android.util.Log.d("StudyPlanViewModel", "加载到计划数量: " + (plans != null ? plans.size() : 0));
                allPlans.postValue(plans);
            } catch (Exception e) {
                android.util.Log.e("StudyPlanViewModel", "加载计划失败", e);
                e.printStackTrace();
                errorMessage.postValue("加载计划失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 加载统计数据
     */
    public void loadStatistics() {
        executorService.execute(() -> {
            try {
                int total = studyPlanRepository.getAllStudyPlans().size();
                int completed = studyPlanRepository.getPlansByStatus("已完成").size();
                int today = studyPlanRepository.getTodayStudyPlans().size();
                
                totalPlansCount.postValue(total);
                completedPlansCount.postValue(completed);
                todayPlansCount.postValue(today);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("加载统计数据失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 加载计划详情
     */
    public void loadPlanDetails(int planId) {
        isLoading.setValue(true);
        
        studyPlanRepository.getPlanWithDetailsAsync(planId, new StudyPlanRepository.OnPlanDetailsLoadedListener() {
            @Override
            public void onPlanDetailsLoaded(StudyPlanRepository.PlanWithDetails details) {
                planDetails.postValue(details);
                
                // 同时更新阶段列表
                if (details != null) {
                    planPhases.postValue(details.getPhases());
                    
                    // 更新今日任务
                    String today = dateFormat.format(new Date());
                    List<DailyTaskEntity> tasks = details.getTasksForDate(today);
                    todayTasks.postValue(tasks);
                    
                    // 更新统计数据
                    if (details.getPlan() != null) {
                        streakDays.postValue(details.getPlan().getStreakDays());
                        weeklyStudyMinutes.postValue(details.getPlan().getTotalStudyTimeMinutes());
                    }
                    
                    // 计算已完成任务数
                    int completed = 0;
                    for (DailyTaskEntity task : tasks) {
                        if (task.isCompleted()) {
                            completed++;
                        }
                    }
                    completedTasksCount.postValue(completed);
                }
                
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(Exception e) {
                errorMessage.postValue("加载计划详情失败: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 添加学习计划（兼容旧的StudyPlan接口）
     */
    public void addStudyPlan(StudyPlan studyPlan, StudyPlanRepository.OnPlanSavedListener listener) {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                // 转换为Entity
                StudyPlanEntity entity = convertToEntity(studyPlan);
                // 使用DAO直接插入
                AppDatabase database = AppDatabase.getInstance(getApplication());
                long id = database.studyPlanDao().insert(entity);
                
                if (id > 0) {
                    successMessage.postValue("学习计划创建成功");
                    if (listener != null) {
                        listener.onPlanSaved(id);
                    }
                    // 重新加载数据
                    loadAllPlans();
                    loadStatistics();
                } else {
                    errorMessage.postValue("学习计划创建失败");
                    if (listener != null) {
                        listener.onError(new Exception("插入失败"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("创建学习计划时出错: " + e.getMessage());
                if (listener != null) {
                    listener.onError(e);
                }
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 更新学习计划
     */
    public void updateStudyPlan(StudyPlanEntity studyPlan, OnOperationCompleteListener listener) {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                // 使用DAO直接更新
                AppDatabase database = AppDatabase.getInstance(getApplication());
                database.studyPlanDao().update(studyPlan);
                successMessage.postValue("学习计划更新成功");
                
                if (listener != null) {
                    listener.onSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("更新学习计划时出错: " + e.getMessage());
                
                if (listener != null) {
                    listener.onError(e);
                }
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 根据状态过滤计划
     */
    public void filterPlansByStatus(String status) {
        executorService.execute(() -> {
            try {
                List<StudyPlan> filteredPlans;
                // 修复：正确处理"全部计划"和"全部"
                if ("全部".equals(status) || "全部计划".equals(status)) {
                    filteredPlans = studyPlanRepository.getAllStudyPlans();
                } else {
                    filteredPlans = studyPlanRepository.getPlansByStatus(status);
                }
                android.util.Log.d("StudyPlanViewModel", "过滤计划, 状态: " + status + ", 数量: " + (filteredPlans != null ? filteredPlans.size() : 0));
                allPlans.postValue(filteredPlans);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("过滤计划失败: " + e.getMessage());
            }
        });
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 转换方法：StudyPlan -> Entity
     */
    private StudyPlanEntity convertToEntity(StudyPlan studyPlan) {
        StudyPlanEntity entity = new StudyPlanEntity();
        entity.setId(studyPlan.getId());
        entity.setTitle(studyPlan.getTitle());
        entity.setDescription(studyPlan.getDescription());
        entity.setCategory(studyPlan.getCategory());
        entity.setStatus(studyPlan.getStatus());
        entity.setPriority(studyPlan.getPriority());
        entity.setProgress(studyPlan.getProgress());
        entity.setTimeRange(studyPlan.getTimeRange());
        entity.setDuration(studyPlan.getDuration());
        entity.setActiveToday(studyPlan.isActiveToday());
        // 设置默认值
        entity.setTotalDays(30);
        entity.setCompletedDays(0);
        entity.setTotalStudyTime(0);
        entity.setStreakDays(0);
        entity.setCreatedTime(System.currentTimeMillis());
        entity.setLastModifiedTime(System.currentTimeMillis());
        entity.setAiGenerated(false);
        entity.setDailyMinutes(120);
        return entity;
    }
    
    // ==================== 回调接口 ====================
    
    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(Exception e);
    }
    
    public interface OnDataLoadListener<T> {
        void onDataLoaded(T data);
        void onLoadError(Exception e);
    }
    
    public interface OnTaskCompletionListener {
        void onTaskCompletionUpdated(DailyTaskEntity task, StudyPlanEntity plan);
        void onError(Exception e);
    }
    
    public interface OnStatisticsRefreshedListener {
        void onStatisticsRefreshed(int streakDays, int weeklyMinutes, int completedTasks);
        void onError(Exception e);
    }
}
