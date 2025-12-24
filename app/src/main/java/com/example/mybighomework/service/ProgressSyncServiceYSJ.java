package com.example.mybighomework.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.dao.StudyPhaseDao;
import com.example.mybighomework.database.dao.StudyPlanDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.utils.PlanStatusManager;
import com.example.mybighomework.utils.ProgressCalculator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 进度同步服务
 * 负责在任务完成后自动同步阶段和计划的进度
 * 整合ProgressCalculator和PlanStatusManager，提供统一的进度更新接口
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4
 */
public class ProgressSyncServiceYSJ {
    
    private static final String TAG = "ProgressSyncService";
    
    private final StudyPlanDao studyPlanDao;
    private final StudyPhaseDao studyPhaseDao;
    private final DailyTaskDao dailyTaskDao;
    private final PlanStatusManager planStatusManager;
    private final TaskGenerationService taskGenerationService;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    /**
     * 进度同步结果回调接口
     */
    public interface OnProgressSyncedListener {
        /**
         * 进度同步成功
         * @param phaseProgress 阶段进度
         * @param planProgress 计划进度
         * @param phaseAdvanced 是否触发了阶段切换
         */
        void onProgressSynced(int phaseProgress, int planProgress, boolean phaseAdvanced);
        
        /**
         * 进度同步失败
         * @param e 异常信息
         */
        void onError(Exception e);
    }
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public ProgressSyncServiceYSJ(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.studyPlanDao = database.studyPlanDao();
        this.studyPhaseDao = database.studyPhaseDao();
        this.dailyTaskDao = database.dailyTaskDao();
        this.planStatusManager = new PlanStatusManager(studyPlanDao, studyPhaseDao, dailyTaskDao);
        this.taskGenerationService = new TaskGenerationService(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * 任务完成后自动同步进度
     * 1. 更新任务状态
     * 2. 计算并更新阶段进度
     * 3. 计算并更新计划进度
     * 4. 检查是否需要切换阶段
     * 
     * @param taskId 任务ID
     * @param listener 回调监听器
     */
    public void syncProgressAfterTaskCompletion(int taskId, OnProgressSyncedListener listener) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "[进度同步] 开始同步进度，taskId=" + taskId);
                
                // 1. 获取任务信息
                DailyTaskEntity task = dailyTaskDao.getTaskById(taskId);
                if (task == null) {
                    throw new IllegalArgumentException("任务不存在，taskId=" + taskId);
                }
                
                int phaseId = task.getPhaseId();
                int planId = task.getPlanId();
                
                // 2. 更新阶段进度
                StudyPhaseEntity updatedPhase = planStatusManager.updatePhaseStatus(phaseId);
                if (updatedPhase == null) {
                    throw new IllegalStateException("阶段不存在，phaseId=" + phaseId);
                }
                
                int phaseProgress = updatedPhase.getProgress();
                Log.d(TAG, "[进度同步] 阶段进度已更新: " + phaseProgress + "%");
                
                // 3. 更新计划进度
                StudyPlanEntity updatedPlan = planStatusManager.updatePlanStatus(planId);
                if (updatedPlan == null) {
                    throw new IllegalStateException("计划不存在，planId=" + planId);
                }
                
                int planProgress = updatedPlan.getProgress();
                Log.d(TAG, "[进度同步] 计划进度已更新: " + planProgress + "%");
                
                // 4. 检查是否需要切换阶段
                final boolean[] phaseAdvanced = {false};
                if (updatedPhase.getProgress() >= 100) {
                    Log.d(TAG, "[进度同步] 阶段已完成，检查是否需要切换");
                    taskGenerationService.checkAndAdvancePhase(planId, 
                        new TaskGenerationService.OnPhaseAdvancedListener() {
                            @Override
                            public void onPhaseAdvanced(StudyPhaseEntity newPhase, List<DailyTaskEntity> generatedTasks) {
                                phaseAdvanced[0] = true;
                                Log.d(TAG, "[进度同步] ✅ 阶段已切换到: " + newPhase.getPhaseName());
                            }
                            
                            @Override
                            public void onNoAdvanceNeeded() {
                                Log.d(TAG, "[进度同步] 无需切换阶段");
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "[进度同步] 阶段切换失败", e);
                            }
                        });
                }
                
                // 5. 回调成功
                final int finalPhaseProgress = phaseProgress;
                final int finalPlanProgress = planProgress;
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onProgressSynced(finalPhaseProgress, finalPlanProgress, phaseAdvanced[0]);
                    }
                });
                
                Log.d(TAG, "[进度同步] ✅ 进度同步完成");
                
            } catch (Exception e) {
                Log.e(TAG, "[进度同步] 进度同步失败", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 批量任务完成后同步进度
     * 优化性能，避免多次重复计算
     * 
     * @param taskIds 任务ID列表
     * @param listener 回调监听器
     */
    public void syncProgressAfterBatchCompletion(List<Integer> taskIds, OnProgressSyncedListener listener) {
        if (taskIds == null || taskIds.isEmpty()) {
            if (listener != null) {
                mainHandler.post(() -> listener.onError(new IllegalArgumentException("任务列表为空")));
            }
            return;
        }
        
        executorService.execute(() -> {
            try {
                Log.d(TAG, "[进度同步] 开始批量同步进度，任务数量=" + taskIds.size());
                
                // 1. 获取第一个任务的planId和phaseId（假设所有任务属于同一计划和阶段）
                DailyTaskEntity firstTask = dailyTaskDao.getTaskById(taskIds.get(0));
                if (firstTask == null) {
                    throw new IllegalArgumentException("任务不存在");
                }
                
                int phaseId = firstTask.getPhaseId();
                int planId = firstTask.getPlanId();
                
                // 2. 更新阶段进度（一次性计算）
                StudyPhaseEntity updatedPhase = planStatusManager.updatePhaseStatus(phaseId);
                int phaseProgress = updatedPhase != null ? updatedPhase.getProgress() : 0;
                Log.d(TAG, "[进度同步] 批量更新阶段进度: " + phaseProgress + "%");
                
                // 3. 更新计划进度（一次性计算）
                StudyPlanEntity updatedPlan = planStatusManager.updatePlanStatus(planId);
                int planProgress = updatedPlan != null ? updatedPlan.getProgress() : 0;
                Log.d(TAG, "[进度同步] 批量更新计划进度: " + planProgress + "%");
                
                // 4. 检查是否需要切换阶段
                final boolean[] phaseAdvanced = {false};
                if (updatedPhase != null && updatedPhase.getProgress() >= 100) {
                    Log.d(TAG, "[进度同步] 阶段已完成，检查是否需要切换");
                    taskGenerationService.checkAndAdvancePhase(planId, 
                        new TaskGenerationService.OnPhaseAdvancedListener() {
                            @Override
                            public void onPhaseAdvanced(StudyPhaseEntity newPhase, List<DailyTaskEntity> generatedTasks) {
                                phaseAdvanced[0] = true;
                                Log.d(TAG, "[进度同步] ✅ 阶段已切换到: " + newPhase.getPhaseName());
                            }
                            
                            @Override
                            public void onNoAdvanceNeeded() {
                                Log.d(TAG, "[进度同步] 无需切换阶段");
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "[进度同步] 阶段切换失败", e);
                            }
                        });
                }
                
                // 5. 回调成功
                final int finalPhaseProgress = phaseProgress;
                final int finalPlanProgress = planProgress;
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onProgressSynced(finalPhaseProgress, finalPlanProgress, phaseAdvanced[0]);
                    }
                });
                
                Log.d(TAG, "[进度同步] ✅ 批量进度同步完成");
                
            } catch (Exception e) {
                Log.e(TAG, "[进度同步] 批量进度同步失败", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 手动触发进度同步
     * 用于用户手动刷新或定时同步
     * 
     * @param planId 计划ID
     * @param listener 回调监听器
     */
    public void manualSyncProgress(int planId, OnProgressSyncedListener listener) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "[进度同步] 手动触发进度同步，planId=" + planId);
                
                // 1. 获取计划的所有阶段
                List<StudyPhaseEntity> phases = studyPhaseDao.getPhasesByPlanId(planId);
                
                // 2. 更新每个阶段的进度
                for (StudyPhaseEntity phase : phases) {
                    planStatusManager.updatePhaseStatus(phase.getId());
                }
                
                // 3. 更新计划进度
                StudyPlanEntity updatedPlan = planStatusManager.updatePlanStatus(planId);
                int planProgress = updatedPlan != null ? updatedPlan.getProgress() : 0;
                
                // 4. 获取当前阶段进度
                StudyPhaseEntity currentPhase = studyPhaseDao.getCurrentPhase(planId);
                int phaseProgress = currentPhase != null ? currentPhase.getProgress() : 0;
                
                // 5. 回调成功
                final int finalPhaseProgress = phaseProgress;
                final int finalPlanProgress = planProgress;
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onProgressSynced(finalPhaseProgress, finalPlanProgress, false);
                    }
                });
                
                Log.d(TAG, "[进度同步] ✅ 手动进度同步完成");
                
            } catch (Exception e) {
                Log.e(TAG, "[进度同步] 手动进度同步失败", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 释放资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
