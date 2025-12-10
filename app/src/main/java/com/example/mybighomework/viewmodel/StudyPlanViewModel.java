package com.example.mybighomework.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.StudyPlan;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.repository.StudyPlanRepository;

import java.util.ArrayList;
import java.util.List;
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
    
    // 状态数据
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    public StudyPlanViewModel(@NonNull Application application) {
        super(application);
        
        // 初始化 Repository
        AppDatabase database = AppDatabase.getInstance(application);
        studyPlanRepository = new StudyPlanRepository(database.studyPlanDao());
        executorService = Executors.newSingleThreadExecutor();
        
        // 初始化 LiveData
        allStudyPlans = studyPlanRepository.getAllStudyPlansLive();
        activeStudyPlans = studyPlanRepository.getActiveStudyPlansLive();
        completedStudyPlans = studyPlanRepository.getCompletedStudyPlansLive();
    }
    
    // ==================== LiveData Getters（为StudyPlan） ====================
    
    /**
     * 获取所有学习计划（StudyPlan）
     */
    public LiveData<List<StudyPlan>> getAllPlans() {
        return allPlans;
    }
    
    /**
     * 获取总计划数
     */
    public LiveData<Integer> getTotalPlansCount() {
        return totalPlansCount;
    }
    
    /**
     * 获取已完成计划数
     */
    public LiveData<Integer> getCompletedPlansCount() {
        return completedPlansCount;
    }
    
    /**
     * 获取今日计划数
     */
    public LiveData<Integer> getTodayPlansCount() {
        return todayPlansCount;
    }
    
    // ==================== LiveData Getters（为Entity） ====================
    
    /**
     * 获取所有学习计划
     */
    public LiveData<List<StudyPlanEntity>> getAllStudyPlans() {
        return allStudyPlans;
    }
    
    /**
     * 获取活跃的学习计划
     */
    public LiveData<List<StudyPlanEntity>> getActiveStudyPlans() {
        return activeStudyPlans;
    }
    
    /**
     * 获取已完成的学习计划
     */
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
    
    // ==================== 业务逻辑方法 ====================
    
    /**
     * 加载所有计划
     */
    public void loadAllPlans() {
        executorService.execute(() -> {
            try {
                // getAllStudyPlans() 直接返回 List<StudyPlan>，无需转换
                List<StudyPlan> plans = studyPlanRepository.getAllStudyPlans();
                allPlans.postValue(plans);
            } catch (Exception e) {
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
                // 获取统计数据
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
     * 按状态筛选计划
     */
    public void filterPlansByStatus(String status) {
        executorService.execute(() -> {
            try {
                List<StudyPlan> plans;
                if ("全部计划".equals(status) || "全部".equals(status)) {
                    plans = studyPlanRepository.getAllStudyPlans();
                } else {
                    plans = studyPlanRepository.getPlansByStatus(status);
                }
                allPlans.postValue(plans);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("筛选失败: " + e.getMessage());
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
                long id = studyPlanRepository.addStudyPlan(studyPlan);
                
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
     * 添加学习计划
     */
    public void addStudyPlan(StudyPlanEntity studyPlan, OnOperationCompleteListener listener) {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                long id = studyPlanRepository.addStudyPlan(studyPlan);
                
                if (id > 0) {
                    successMessage.postValue("学习计划创建成功");
                    if (listener != null) {
                        listener.onSuccess();
                    }
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
                studyPlanRepository.updateStudyPlan(studyPlan);
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
     * 删除学习计划
     */
    public void deleteStudyPlan(StudyPlanEntity studyPlan, OnOperationCompleteListener listener) {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                studyPlanRepository.deleteStudyPlan(studyPlan);
                successMessage.postValue("学习计划已删除");
                
                if (listener != null) {
                    listener.onSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("删除学习计划时出错: " + e.getMessage());
                
                if (listener != null) {
                    listener.onError(e);
                }
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * 标记计划为已完成
     */
    public void markPlanAsCompleted(int planId, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                studyPlanRepository.markPlanAsCompleted(planId);
                successMessage.postValue("计划已标记为完成");
                
                if (listener != null) {
                    listener.onSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("操作失败: " + e.getMessage());
                
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }
    
    /**
     * 更新计划进度
     */
    public void updatePlanProgress(int planId, int progress, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                studyPlanRepository.updatePlanProgress(planId, progress);
                
                if (listener != null) {
                    listener.onSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("更新进度失败: " + e.getMessage());
                
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }
    
    /**
     * 获取今天的学习计划
     */
    public void getTodayStudyPlans(OnDataLoadListener<List<StudyPlanEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<StudyPlanEntity> plans = studyPlanRepository.getTodayStudyPlans();
                
                if (listener != null) {
                    listener.onDataLoaded(plans);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onLoadError(e);
                }
            }
        });
    }
    
    /**
     * 根据优先级获取学习计划
     */
    public void getStudyPlansByPriority(String priority, OnDataLoadListener<List<StudyPlanEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<StudyPlanEntity> plans = studyPlanRepository.getStudyPlansByPriority(priority);
                
                if (listener != null) {
                    listener.onDataLoaded(plans);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onLoadError(e);
                }
            }
        });
    }
    
    // ==================== 回调接口 ====================
    
    /**
     * 操作完成监听器
     */
    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(Exception e);
    }
    
    /**
     * 数据加载监听器
     */
    public interface OnDataLoadListener<T> {
        void onDataLoaded(T data);
        void onLoadError(Exception e);
    }
    
    // ==================== 转换方法 ====================
    
    /**
     * 转换方法：Entity -> StudyPlan
     */
    private StudyPlan convertToStudyPlan(StudyPlanEntity entity) {
        return new StudyPlan(
            entity.getId(),
            entity.getTitle(),
            entity.getCategory(),
            entity.getDescription(),
            entity.getTimeRange(),
            entity.getDuration(),
            entity.getProgress(),
            entity.getPriority(),
            entity.getStatus(),
            entity.isActiveToday()
        );
    }
    
    /**
     * 批量转换：Entity List -> StudyPlan List
     */
    private List<StudyPlan> convertToStudyPlans(List<StudyPlanEntity> entities) {
        List<StudyPlan> plans = new ArrayList<>();
        if (entities != null) {
            for (StudyPlanEntity entity : entities) {
                plans.add(convertToStudyPlan(entity));
            }
        }
        return plans;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // ViewModel 被销毁时，关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
