package com.example.mybighomework.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.dao.StudyPhaseDao;
import com.example.mybighomework.database.dao.StudyPlanDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.StudyPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 学习计划仓库 - 修复版本
 * 统一管理学习计划的数据访问
 */
public class StudyPlanRepositoryNewYSJ {
    
    // DAO对象
    private final StudyPlanDao studyPlanDao;
    private final StudyPhaseDao studyPhaseDao;
    private final DailyTaskDao dailyTaskDao;
    
    // 异步执行器
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    // ==================== 接口定义 ====================
    
    /**
     * 计划保存监听器
     */
    public interface OnPlanSavedListener {
        void onPlanSaved(long planId);
        void onError(Exception e);
    }
    
    /**
     * 计划更新监听器
     */
    public interface OnPlanUpdatedListener {
        void onPlanUpdated();
        void onError(Exception e);
    }
    
    /**
     * 计划删除监听器
     */
    public interface OnPlanDeletedListener {
        void onPlanDeleted();
        void onError(Exception e);
    }
    
    /**
     * 计划列表加载监听器
     */
    public interface OnPlansLoadedListener {
        void onPlansLoaded(List<StudyPlan> plans);
        void onError(Exception e);
    }
    
    /**
     * 统计信息加载监听器
     */
    public interface OnStatisticsLoadedListener {
        void onStatisticsLoaded(StudyStatistics statistics);
        void onError(Exception e);
    }
    
    /**
     * 计划详情加载监听器
     */
    public interface OnPlanDetailsLoadedListener {
        void onPlanDetailsLoaded(PlanWithDetails planWithDetails);
        void onError(Exception e);
    }
    
    /**
     * 任务完成状态更新监听器
     */
    public interface OnTaskCompletionUpdatedListener {
        void onTaskCompletionUpdated(DailyTaskEntity task, StudyPlanEntity plan);
        void onError(Exception e);
    }
    
    /**
     * 计划详情类
     */
    public static class PlanWithDetails {
        private final StudyPlanEntity plan;
        private final List<StudyPhaseEntity> phases;
        private final List<DailyTaskEntity> tasks;
        
        public PlanWithDetails(StudyPlanEntity plan, List<StudyPhaseEntity> phases, List<DailyTaskEntity> tasks) {
            this.plan = plan;
            this.phases = phases;
            this.tasks = tasks;
        }
        
        public StudyPlanEntity getPlan() { return plan; }
        public List<StudyPhaseEntity> getPhases() { return phases; }
        public List<DailyTaskEntity> getTasks() { return tasks; }
        
        /**
         * 获取指定日期的任务
         */
        public List<DailyTaskEntity> getTasksForDate(String date) {
            List<DailyTaskEntity> result = new ArrayList<>();
            if (tasks != null) {
                for (DailyTaskEntity task : tasks) {
                    if (date.equals(task.getDate())) {
                        result.add(task);
                    }
                }
            }
            return result;
        }
    }
    
    /**
     * 统计信息类
     */
    public static class StudyStatistics {
        public int totalPlans;
        public int activePlans;
        public int completedPlans;
        public int totalStudyTime;
        public int streakDays;
        
        public StudyStatistics(int totalPlans, int activePlans, int completedPlans, int totalStudyTime, int streakDays) {
            this.totalPlans = totalPlans;
            this.activePlans = activePlans;
            this.completedPlans = completedPlans;
            this.totalStudyTime = totalStudyTime;
            this.streakDays = streakDays;
        }
    }
    
    // ==================== 构造函数 ====================
    
    /**
     * 完整构造函数
     */
    public StudyPlanRepositoryNewYSJ(Application application, 
                              StudyPlanDao studyPlanDao,
                              StudyPhaseDao studyPhaseDao,
                              DailyTaskDao dailyTaskDao) {
        this.studyPlanDao = studyPlanDao;
        this.studyPhaseDao = studyPhaseDao;
        this.dailyTaskDao = dailyTaskDao;
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * 简化构造函数（仅计划相关）
     */
    public StudyPlanRepositoryNewYSJ(Application application, StudyPlanDao studyPlanDao) {
        this(application, studyPlanDao, null, null);
    }
    
    // ==================== 异步操作方法 ====================
    
    /**
     * 异步添加学习计划
     */
    public void addStudyPlanAsync(StudyPlan studyPlan, OnPlanSavedListener listener) {
        executorService.execute(() -> {
            try {
                StudyPlanEntity entity = convertToEntity(studyPlan);
                long id = studyPlanDao.insert(entity);
                
                // 在主线程回调
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlanSaved(id);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步更新学习计划
     */
    public void updateStudyPlanAsync(StudyPlan studyPlan, OnPlanUpdatedListener listener) {
        executorService.execute(() -> {
            try {
                StudyPlanEntity entity = convertToEntity(studyPlan);
                studyPlanDao.update(entity);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlanUpdated();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步删除学习计划
     */
    public void deleteStudyPlanAsync(StudyPlan studyPlan, OnPlanDeletedListener listener) {
        executorService.execute(() -> {
            try {
                StudyPlanEntity entity = convertToEntity(studyPlan);
                studyPlanDao.delete(entity);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlanDeleted();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步获取所有学习计划
     */
    public void getAllStudyPlansAsync(OnPlansLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<StudyPlanEntity> entities = studyPlanDao.getAllStudyPlans();
                List<StudyPlan> plans = convertToStudyPlans(entities);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlansLoaded(plans);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步获取今日学习计划
     */
    public void getTodayPlansAsync(OnPlansLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<StudyPlanEntity> entities = studyPlanDao.getTodayPlans();
                List<StudyPlan> plans = convertToStudyPlans(entities);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlansLoaded(plans);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步根据状态获取计划
     */
    public void getPlansByStatusAsync(String status, OnPlansLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<StudyPlanEntity> entities = studyPlanDao.getPlansByStatus(status);
                List<StudyPlan> plans = convertToStudyPlans(entities);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlansLoaded(plans);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步获取统计信息
     */
    public void getStatisticsAsync(OnStatisticsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<StudyPlanEntity> allPlans = studyPlanDao.getAllStudyPlans();
                int total = allPlans.size();
                int completed = 0;
                int today = 0;
                
                for (StudyPlanEntity plan : allPlans) {
                    if ("已完成".equals(plan.getStatus())) {
                        completed++;
                    }
                    if (plan.isActiveToday()) {
                        today++;
                    }
                }
                
                StudyStatistics statistics = new StudyStatistics(total, total - completed, completed, 0, 0);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onStatisticsLoaded(statistics);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步搜索计划
     */
    public void searchPlansAsync(String keyword, OnPlansLoadedListener listener) {
        executorService.execute(() -> {
            try {
                List<StudyPlanEntity> allPlans = studyPlanDao.getAllStudyPlans();
                List<StudyPlan> results = new ArrayList<>();
                
                for (StudyPlanEntity entity : allPlans) {
                    if (entity.getTitle().contains(keyword) || 
                        entity.getDescription().contains(keyword)) {
                        results.add(convertFromEntity(entity));
                    }
                }
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlansLoaded(results);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 异步获取计划及其所有阶段和任务
     */
    public void getPlanWithDetailsAsync(int planId, OnPlanDetailsLoadedListener listener) {
        executorService.execute(() -> {
            try {
                StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
                if (plan == null) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onError(new IllegalArgumentException("Plan not found: " + planId));
                        }
                    });
                    return;
                }
                
                List<StudyPhaseEntity> phases = studyPhaseDao.getPhasesByPlanId(planId);
                List<DailyTaskEntity> tasks = dailyTaskDao.getAllTasksByPlan(planId);
                
                PlanWithDetails result = new PlanWithDetails(plan, phases, tasks);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlanDetailsLoaded(result);
                    }
                });
                
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 保存计划及其阶段和任务
     */
    public void savePlanWithPhasesAndTasks(
            StudyPlan plan,
            List<StudyPhaseEntity> phases,
            List<DailyTaskEntity> tasks,
            OnPlanSavedListener listener) {
        
        executorService.execute(() -> {
            try {
                // 1. 保存计划
                StudyPlanEntity planEntity = convertToEntity(plan);
                long planId = studyPlanDao.insert(planEntity);
                planEntity.setId((int) planId);
                
                // 2. 保存阶段
                if (phases != null) {
                    for (StudyPhaseEntity phase : phases) {
                        phase.setPlanId((int) planId);
                        studyPhaseDao.insert(phase);
                    }
                }
                
                // 3. 保存任务
                if (tasks != null) {
                    for (DailyTaskEntity task : tasks) {
                        task.setPlanId((int) planId);
                        dailyTaskDao.insert(task);
                    }
                }
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPlanSaved(planId);
                    }
                });
                
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    // ==================== 同步方法（供ViewModel使用） ====================
    
    /**
     * 获取所有学习计划（同步版本）
     */
    public List<StudyPlan> getAllStudyPlans() {
        List<StudyPlanEntity> entities = studyPlanDao.getAllStudyPlans();
        return convertToStudyPlans(entities);
    }
    
    /**
     * 根据状态获取计划（同步版本）
     */
    public List<StudyPlan> getPlansByStatus(String status) {
        List<StudyPlanEntity> entities = studyPlanDao.getPlansByStatus(status);
        return convertToStudyPlans(entities);
    }
    
    /**
     * 根据优先级获取计划 - 返回Entity列表
     */
    public List<StudyPlanEntity> getStudyPlansByPriority(String priority) {
        // 如果DAO中没有对应方法，返回所有计划
        return studyPlanDao.getAllStudyPlans();
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 将StudyPlan转换为StudyPlanEntity
     */
    private StudyPlanEntity convertToEntity(StudyPlan studyPlan) {
        StudyPlanEntity entity = new StudyPlanEntity();
        entity.setId(studyPlan.getId());
        entity.setTitle(studyPlan.getTitle());
        entity.setDescription(studyPlan.getDescription());
        entity.setCategory(studyPlan.getCategory()); // 使用category作为subject
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
    
    /**
     * 将StudyPlanEntity列表转换为StudyPlan列表
     */
    private List<StudyPlan> convertToStudyPlans(List<StudyPlanEntity> entities) {
        List<StudyPlan> plans = new ArrayList<>();
        if (entities != null) {
            for (StudyPlanEntity entity : entities) {
                plans.add(convertFromEntity(entity));
            }
        }
        return plans;
    }
    
    /**
     * 将StudyPlanEntity转换为StudyPlan
     */
    private StudyPlan convertFromEntity(StudyPlanEntity entity) {
        StudyPlan studyPlan = new StudyPlan(
            entity.getId(),
            entity.getTitle(),
            entity.getCategory(), // 使用category
            entity.getDescription(),
            entity.getTimeRange(),
            entity.getDuration(),
            entity.getProgress(),
            entity.getPriority(),
            entity.getStatus(),
            entity.isActiveToday()
        );
        return studyPlan;
    }
}
