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
import com.example.mybighomework.utils.TaskGenerator;
import com.example.mybighomework.utils.PhaseDateManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务自动生成服务
 * 负责在打开计划时检查并生成今日任务，处理阶段切换时的任务生成
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
public class TaskGenerationService {
    
    private static final String TAG = "TaskGenerationService";
    
    private final StudyPlanDao studyPlanDao;
    private final StudyPhaseDao studyPhaseDao;
    private final DailyTaskDao dailyTaskDao;
    private final TaskGenerator taskGenerator;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final PhaseDateManager phaseDateManager;
    
    /**
     * 任务生成结果回调接口
     */
    public interface OnTasksGeneratedListener {
        /**
         * 任务生成成功
         * @param tasks 生成的任务列表
         * @param isNewlyGenerated 是否是新生成的（false表示已存在）
         */
        void onTasksGenerated(List<DailyTaskEntity> tasks, boolean isNewlyGenerated);
        
        /**
         * 任务生成失败
         * @param e 异常信息
         */
        void onError(Exception e);
    }
    
    /**
     * 阶段切换结果回调接口
     */
    public interface OnPhaseAdvancedListener {
        /**
         * 阶段切换成功
         * @param newPhase 新的当前阶段
         * @param generatedTasks 为新阶段生成的任务
         */
        void onPhaseAdvanced(StudyPhaseEntity newPhase, List<DailyTaskEntity> generatedTasks);
        
        /**
         * 无需切换阶段
         */
        void onNoAdvanceNeeded();
        
        /**
         * 阶段切换失败
         * @param e 异常信息
         */
        void onError(Exception e);
    }
    
    /**
     * 使用Context构造
     * @param context 应用上下文
     */
    public TaskGenerationService(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.studyPlanDao = database.studyPlanDao();
        this.studyPhaseDao = database.studyPhaseDao();
        this.dailyTaskDao = database.dailyTaskDao();
        this.taskGenerator = new TaskGenerator();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.phaseDateManager = new PhaseDateManager(studyPhaseDao);
    }
    
    /**
     * 使用DAO构造（用于测试）
     */
    public TaskGenerationService(StudyPlanDao studyPlanDao, 
                                  StudyPhaseDao studyPhaseDao, 
                                  DailyTaskDao dailyTaskDao) {
        this.studyPlanDao = studyPlanDao;
        this.studyPhaseDao = studyPhaseDao;
        this.dailyTaskDao = dailyTaskDao;
        this.taskGenerator = new TaskGenerator();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.phaseDateManager = new PhaseDateManager(studyPhaseDao);
    }

    
    /**
     * 在打开计划时检查并生成今日任务
     * 如果今日任务已存在，则直接返回现有任务
     * 如果不存在，则根据当前阶段的任务模板生成新任务
     * 
     * @param planId 计划ID
     * @param listener 回调监听器
     * 
     * Requirements: 6.1, 6.2, 6.4
     */
    public void ensureTodayTasksExist(int planId, OnTasksGeneratedListener listener) {
        executorService.execute(() -> {
            try {
                String today = dateFormat.format(new Date());
                
                // 预先判断是否已有今日任务（用于准确的生成标记）
                boolean hadTasksBefore = dailyTaskDao.hasTasksForDate(planId, today);
                
                // 执行幂等生成
                ensureTodayTasksExistSync(planId, today);
                
                // 获取今日所有任务（包括之前存在的）
                List<DailyTaskEntity> todayTasks = dailyTaskDao.getTasksByDate(planId, today);
                
                // 新生成：之前没有任务，且现在存在任务
                boolean isNewlyGenerated = !hadTasksBefore && todayTasks != null && !todayTasks.isEmpty();
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onTasksGenerated(todayTasks, isNewlyGenerated);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "ensureTodayTasksExist failed", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 同步版本：确保今日任务存在
     * 注意：此方法必须在后台线程调用
     * 
     * @param planId 计划ID
     * @param date 日期（yyyy-MM-dd格式）
     * @return 新生成的任务列表，如果任务已存在则返回空列表
     * 
     * Requirements: 6.1, 6.2, 6.4
     */
    public List<DailyTaskEntity> ensureTodayTasksExistSync(int planId, String date) {
        List<DailyTaskEntity> generatedTasks = new ArrayList<>();
        
        Log.d(TAG, "[任务生成] 开始检查今日任务，planId=" + planId + ", date=" + date);
        
        // 1. 检查是否已有今日任务（幂等性保证）
        boolean hasExistingTasks = dailyTaskDao.hasTasksForDate(planId, date);
        if (hasExistingTasks) {
            Log.d(TAG, "[任务生成] 今日任务已存在，跳过生成，planId=" + planId + ", date=" + date);
            return generatedTasks;
        }
        
        // 2. 获取计划信息
        StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
        if (plan == null) {
            Log.w(TAG, "[任务生成] 计划不存在，planId=" + planId);
            return generatedTasks;
        }
        
        Log.d(TAG, "[任务生成] 找到计划: " + plan.getTitle() + ", 状态: " + plan.getStatus());
        
        // 3. 检查计划状态（暂停或已完成的计划不生成任务）
        if (plan.isPaused()) {
            Log.d(TAG, "[任务生成] 计划已暂停，不生成任务，planId=" + planId);
            return generatedTasks;
        }
        
        if (plan.isCompleted()) {
            Log.d(TAG, "[任务生成] 计划已完成，不生成任务，planId=" + planId);
            return generatedTasks;
        }
        
        // 4. 获取当前进行中的阶段
        StudyPhaseEntity currentPhase = studyPhaseDao.getCurrentPhase(planId);
        
        // 如果没有进行中的阶段，尝试启动第一个未开始的阶段
        if (currentPhase == null) {
            Log.d(TAG, "[任务生成] 没有进行中的阶段，尝试启动第一个阶段");
            currentPhase = startFirstPhaseIfNeeded(planId);
        }
        
        if (currentPhase == null) {
            Log.w(TAG, "[任务生成] 没有可用的阶段，planId=" + planId);
            return generatedTasks;
        }
        
        Log.d(TAG, "[任务生成] 当前阶段: " + currentPhase.getPhaseName() + 
                   ", 阶段ID: " + currentPhase.getId());
        
        // 5. 使用TaskGenerator生成任务
        if (taskGenerator.shouldGenerateTasks(plan, currentPhase, date, false)) {
            Log.d(TAG, "[任务生成] 开始生成任务...");
            generatedTasks = taskGenerator.generateTasksForDate(plan, currentPhase, date);
            
            // 6. 保存生成的任务到数据库
            if (!generatedTasks.isEmpty()) {
                try {
                    List<Long> ids = dailyTaskDao.insertAll(generatedTasks);
                    
                    // 更新任务ID
                    for (int i = 0; i < generatedTasks.size(); i++) {
                        generatedTasks.get(i).setId(ids.get(i).intValue());
                    }
                    
                    Log.d(TAG, "[任务生成] ✅ 成功生成并保存 " + generatedTasks.size() + " 个任务，planId=" 
                        + planId + ", date=" + date);
                    
                    // 输出每个任务的详细信息
                    for (int i = 0; i < generatedTasks.size(); i++) {
                        DailyTaskEntity task = generatedTasks.get(i);
                        Log.d(TAG, "[任务生成]   任务" + (i+1) + ": " + task.getTaskContent() + 
                                   " (" + task.getEstimatedMinutes() + "分钟)");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "[任务生成] ❌ 保存任务到数据库失败", e);
                    generatedTasks.clear();
                }
            } else {
                Log.w(TAG, "[任务生成] 任务生成器返回空列表，可能任务模板为空");
            }
        } else {
            Log.d(TAG, "[任务生成] 不满足任务生成条件，跳过生成");
        }
        
        return generatedTasks;
    }
    
    /**
     * 处理阶段切换时的任务生成
     * 当当前阶段完成时，自动切换到下一阶段并生成任务
     * 
     * @param planId 计划ID
     * @param listener 回调监听器
     * 
     * Requirements: 6.3, 6.5
     */
    public void checkAndAdvancePhase(int planId, OnPhaseAdvancedListener listener) {
        executorService.execute(() -> {
            try {
                PhaseAdvanceResult result = checkAndAdvancePhaseSync(planId);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        if (result.advanced) {
                            listener.onPhaseAdvanced(result.newPhase, result.generatedTasks);
                        } else {
                            listener.onNoAdvanceNeeded();
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "checkAndAdvancePhase failed", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    
    /**
     * 阶段切换结果数据类
     */
    private static class PhaseAdvanceResult {
        boolean advanced;
        StudyPhaseEntity newPhase;
        List<DailyTaskEntity> generatedTasks;
        
        PhaseAdvanceResult(boolean advanced, StudyPhaseEntity newPhase, List<DailyTaskEntity> generatedTasks) {
            this.advanced = advanced;
            this.newPhase = newPhase;
            this.generatedTasks = generatedTasks != null ? generatedTasks : new ArrayList<>();
        }
    }
    
    /**
     * 同步版本：检查并切换阶段
     * 注意：此方法必须在后台线程调用
     * 
     * @param planId 计划ID
     * @return 阶段切换结果
     * 
     * Requirements: 6.3, 6.5
     */
    private PhaseAdvanceResult checkAndAdvancePhaseSync(int planId) {
        Log.d(TAG, "[阶段切换] 开始检查阶段切换，planId=" + planId);
        
        // 1. 获取当前进行中的阶段
        StudyPhaseEntity currentPhase = studyPhaseDao.getCurrentPhase(planId);
        
        if (currentPhase == null) {
            Log.d(TAG, "[阶段切换] 没有进行中的阶段，尝试启动第一个阶段");
            // 没有进行中的阶段，尝试启动第一个
            StudyPhaseEntity firstPhase = startFirstPhaseIfNeeded(planId);
            if (firstPhase != null) {
                String today = dateFormat.format(new Date());
                List<DailyTaskEntity> tasks = generateTasksForPhase(planId, firstPhase, today);
                Log.d(TAG, "[阶段切换] ✅ 成功启动第一个阶段: " + firstPhase.getPhaseName());
                return new PhaseAdvanceResult(true, firstPhase, tasks);
            }
            Log.w(TAG, "[阶段切换] 无法启动第一个阶段");
            return new PhaseAdvanceResult(false, null, null);
        }
        
        Log.d(TAG, "[阶段切换] 当前阶段: " + currentPhase.getPhaseName() + 
                   ", 进度: " + currentPhase.getProgress() + "%");
        
        // 2. 增强的阶段完成判断
        if (!isPhaseCompletedEnhanced(currentPhase, planId)) {
            Log.d(TAG, "[阶段切换] 当前阶段未完成，无需切换");
            return new PhaseAdvanceResult(false, null, null);
        }
        
        Log.d(TAG, "[阶段切换] 当前阶段已完成，准备切换到下一阶段");
        
        // 3. 将当前阶段标记为已完成
        currentPhase.setStatus(StudyPhaseEntity.STATUS_COMPLETED);
        currentPhase.setProgress(100);
        studyPhaseDao.update(currentPhase);
        
        // 4. 查找下一个阶段
        StudyPhaseEntity nextPhase = studyPhaseDao.getNextPhase(planId, currentPhase.getPhaseOrder());
        
        if (nextPhase == null) {
            // 没有下一个阶段，计划可能已完成
            Log.d(TAG, "[阶段切换] 没有下一个阶段，计划已完成，planId=" + planId);
            markPlanAsCompleted(planId);
            return new PhaseAdvanceResult(false, null, null);
        }
        
        // 5. 启动下一个阶段
        nextPhase.setStatus(StudyPhaseEntity.STATUS_IN_PROGRESS);
        String today = dateFormat.format(new Date());
        
        // 使用PhaseDateManager重新计算阶段日期
        Log.d(TAG, "[阶段切换] 使用PhaseDateManager重新计算阶段日期");
        phaseDateManager.recalculatePhasesFrom(planId, nextPhase, today);
        
        studyPhaseDao.update(nextPhase);
        
        Log.d(TAG, "[阶段切换] ✅ 阶段切换成功！");
        Log.d(TAG, "[阶段切换]   从: " + currentPhase.getPhaseName() + " (阶段" + currentPhase.getPhaseOrder() + ")");
        Log.d(TAG, "[阶段切换]   到: " + nextPhase.getPhaseName() + " (阶段" + nextPhase.getPhaseOrder() + ")");
        
        // 6. 为新阶段生成今日任务（带重试机制）
        List<DailyTaskEntity> generatedTasks = generateTasksForPhaseWithRetry(planId, nextPhase, today, 3);
        
        if (generatedTasks.isEmpty()) {
            Log.w(TAG, "[阶段切换] ⚠️ 新阶段任务生成失败，但阶段已切换");
        } else {
            Log.d(TAG, "[阶段切换] 为新阶段生成了 " + generatedTasks.size() + " 个任务");
        }
        
        return new PhaseAdvanceResult(true, nextPhase, generatedTasks);
    }
    
    /**
     * 为指定阶段生成今日任务
     * 
     * @param planId 计划ID
     * @param phase 阶段
     * @param date 日期
     * @return 生成的任务列表
     */
    private List<DailyTaskEntity> generateTasksForPhase(int planId, StudyPhaseEntity phase, String date) {
        List<DailyTaskEntity> generatedTasks = new ArrayList<>();
        
        // 检查是否已有任务
        boolean hasExistingTasks = dailyTaskDao.hasTasksForDate(planId, date);
        if (hasExistingTasks) {
            Log.d(TAG, "该日期已有任务，跳过生成，date=" + date);
            return generatedTasks;
        }
        
        // 获取计划
        StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
        if (plan == null) {
            return generatedTasks;
        }
        
        // 生成任务
        if (taskGenerator.shouldGenerateTasks(plan, phase, date, false)) {
            generatedTasks = taskGenerator.generateTasksForDate(plan, phase, date);
            
            if (!generatedTasks.isEmpty()) {
                List<Long> ids = dailyTaskDao.insertAll(generatedTasks);
                for (int i = 0; i < generatedTasks.size(); i++) {
                    generatedTasks.get(i).setId(ids.get(i).intValue());
                }
                Log.d(TAG, "为新阶段生成了 " + generatedTasks.size() + " 个任务");
            }
        }
        
        return generatedTasks;
    }
    
    /**
     * 为指定阶段生成今日任务（带重试机制）
     * 
     * @param planId 计划ID
     * @param phase 阶段
     * @param date 日期
     * @param maxRetries 最大重试次数
     * @return 生成的任务列表
     */
    private List<DailyTaskEntity> generateTasksForPhaseWithRetry(int planId, StudyPhaseEntity phase, 
                                                                  String date, int maxRetries) {
        List<DailyTaskEntity> generatedTasks = new ArrayList<>();
        int retryCount = 0;
        
        while (retryCount <= maxRetries) {
            try {
                generatedTasks = generateTasksForPhase(planId, phase, date);
                if (!generatedTasks.isEmpty()) {
                    return generatedTasks;
                }
                
                // 如果返回空列表，可能是任务模板为空，不需要重试
                Log.w(TAG, "[任务生成] 任务模板可能为空，停止重试");
                break;
                
            } catch (Exception e) {
                retryCount++;
                Log.e(TAG, "[任务生成] 第" + retryCount + "次尝试失败", e);
                
                if (retryCount <= maxRetries) {
                    try {
                        Thread.sleep(500); // 等待500ms后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        return generatedTasks;
    }
    
    /**
     * 标记计划为已完成
     * 
     * @param planId 计划ID
     */
    private void markPlanAsCompleted(int planId) {
        StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
        if (plan != null && !plan.isCompleted()) {
            plan.setStatus(StudyPlanEntity.STATUS_COMPLETED);
            plan.setProgress(100);
            studyPlanDao.update(plan);
            Log.d(TAG, "[计划完成] ✅ 计划已标记为完成: " + plan.getTitle());
        }
    }
    
    /**
     * 如果需要，启动第一个阶段
     * 
     * @param planId 计划ID
     * @return 启动的阶段，如果没有可启动的阶段则返回null
     */
    private StudyPhaseEntity startFirstPhaseIfNeeded(int planId) {
        // 获取第一个阶段
        StudyPhaseEntity firstPhase = studyPhaseDao.getPhaseByOrder(planId, 1);
        
        if (firstPhase == null) {
            // 尝试获取任意第一个阶段
            List<StudyPhaseEntity> phases = studyPhaseDao.getPhasesByPlanId(planId);
            if (phases != null && !phases.isEmpty()) {
                firstPhase = phases.get(0);
            }
        }
        
        if (firstPhase != null && StudyPhaseEntity.STATUS_NOT_STARTED.equals(firstPhase.getStatus())) {
            firstPhase.setStatus(StudyPhaseEntity.STATUS_IN_PROGRESS);
            String today = dateFormat.format(new Date());
            
            // 使用PhaseDateManager重新计算阶段日期
            Log.d(TAG, "[启动第一阶段] 使用PhaseDateManager重新计算阶段日期");
            phaseDateManager.recalculatePhasesFrom(planId, firstPhase, today);
            
            studyPhaseDao.update(firstPhase);
            Log.d(TAG, "启动第一个阶段，phaseId=" + firstPhase.getId());
            return firstPhase;
        }
        
        return firstPhase;
    }
    
    /**
     * 检查阶段是否已完成（基础版本）
     * 基于阶段的已完成天数和总天数判断
     * 
     * @param phase 阶段
     * @return 是否已完成
     */
    private boolean isPhaseCompleted(StudyPhaseEntity phase) {
        if (phase == null) {
            return false;
        }
        
        // 如果状态已经是已完成，直接返回true
        if (StudyPhaseEntity.STATUS_COMPLETED.equals(phase.getStatus())) {
            return true;
        }
        
        // 如果进度达到100%，认为已完成
        if (phase.getProgress() >= 100) {
            return true;
        }
        
        // 如果已完成天数达到总天数，认为已完成
        if (phase.getDurationDays() > 0 && phase.getCompletedDays() >= phase.getDurationDays()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 增强的阶段完成判断
     * 综合考虑进度、任务完成情况和日期范围
     * 
     * @param phase 阶段
     * @param planId 计划ID
     * @return 是否已完成
     */
    private boolean isPhaseCompletedEnhanced(StudyPhaseEntity phase, int planId) {
        if (phase == null) {
            return false;
        }
        
        // 1. 检查状态
        if (StudyPhaseEntity.STATUS_COMPLETED.equals(phase.getStatus())) {
            Log.d(TAG, "[阶段判断] 阶段状态已标记为完成");
            return true;
        }
        
        // 2. 检查进度
        if (phase.getProgress() >= 100) {
            Log.d(TAG, "[阶段判断] 阶段进度达到100%");
            return true;
        }
        
        // 3. 检查已完成天数
        if (phase.getDurationDays() > 0 && phase.getCompletedDays() >= phase.getDurationDays()) {
            Log.d(TAG, "[阶段判断] 已完成天数达到总天数: " + phase.getCompletedDays() + "/" + phase.getDurationDays());
            return true;
        }
        
        // 4. 检查是否超过阶段结束日期
        String endDate = phase.getEndDate();
        if (endDate != null && !endDate.isEmpty()) {
            String today = dateFormat.format(new Date());
            if (today.compareTo(endDate) > 0) {
                Log.d(TAG, "[阶段判断] 已超过阶段结束日期: " + endDate);
                return true;
            }
        }
        
        // 5. 检查阶段内所有任务是否完成
        List<DailyTaskEntity> phaseTasks = dailyTaskDao.getTasksByPhase(phase.getId());
        if (!phaseTasks.isEmpty()) {
            boolean allCompleted = true;
            for (DailyTaskEntity task : phaseTasks) {
                if (!task.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            if (allCompleted) {
                Log.d(TAG, "[阶段判断] 阶段内所有任务已完成");
                return true;
            }
        }
        
        return false;
    }

    
    /**
     * 为计划生成指定日期范围内的所有任务（优化版本）
     * 使用批量插入提高性能
     * 
     * @param planId 计划ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param listener 回调监听器
     */
    public void generateTasksForDateRangeOptimized(int planId, String startDate, String endDate, 
                                                   OnTasksGeneratedListener listener) {
        executorService.execute(() -> {
            try {
                List<DailyTaskEntity> allGeneratedTasks = new ArrayList<>();
                
                // 获取计划
                StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
                if (plan == null) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onError(new IllegalArgumentException("计划不存在: " + planId));
                        }
                    });
                    return;
                }
                
                // 获取所有阶段
                List<StudyPhaseEntity> phases = studyPhaseDao.getPhasesByPlanId(planId);
                if (phases == null || phases.isEmpty()) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onTasksGenerated(allGeneratedTasks, false);
                        }
                    });
                    return;
                }
                
                // 批量收集所有需要生成的任务
                List<DailyTaskEntity> tasksToInsert = new ArrayList<>();
                List<String> dates = generateDateRange(startDate, endDate);
                
                Log.d(TAG, "[批量生成] 开始批量生成任务，日期范围: " + startDate + " ~ " + endDate + ", 共" + dates.size() + "天");
                
                for (String date : dates) {
                    // 检查是否已有任务
                    if (dailyTaskDao.hasTasksForDate(planId, date)) {
                        continue;
                    }
                    
                    // 找到该日期对应的阶段
                    StudyPhaseEntity phase = findPhaseForDate(phases, date);
                    if (phase == null) {
                        continue;
                    }
                    
                    // 生成任务
                    if (taskGenerator.shouldGenerateTasks(plan, phase, date, false)) {
                        List<DailyTaskEntity> tasks = taskGenerator.generateTasksForDate(plan, phase, date);
                        if (!tasks.isEmpty()) {
                            tasksToInsert.addAll(tasks);
                        }
                    }
                }
                
                // 批量插入所有任务
                if (!tasksToInsert.isEmpty()) {
                    try {
                        // 分批插入，避免一次性插入过多数据
                        int batchSize = 100;
                        int totalInserted = 0;
                        
                        for (int i = 0; i < tasksToInsert.size(); i += batchSize) {
                            int endIndex = Math.min(i + batchSize, tasksToInsert.size());
                            List<DailyTaskEntity> batch = tasksToInsert.subList(i, endIndex);
                            
                            List<Long> ids = dailyTaskDao.insertAll(batch);
                            
                            // 更新任务ID
                            for (int j = 0; j < batch.size(); j++) {
                                batch.get(j).setId(ids.get(j).intValue());
                            }
                            
                            totalInserted += batch.size();
                            Log.d(TAG, "[批量生成] 已插入第" + (i/batchSize + 1) + "批，共" + batch.size() + "个任务");
                        }
                        
                        allGeneratedTasks.addAll(tasksToInsert);
                        Log.d(TAG, "[批量生成] ✅ 批量生成完成，共生成" + totalInserted + "个任务");
                        
                    } catch (Exception e) {
                        Log.e(TAG, "[批量生成] ❌ 批量插入失败", e);
                        // 如果批量插入失败，尝试逐个插入
                        Log.d(TAG, "[批量生成] 尝试逐个插入作为备选方案");
                        for (DailyTaskEntity task : tasksToInsert) {
                            try {
                                long id = dailyTaskDao.insert(task);
                                task.setId((int) id);
                                allGeneratedTasks.add(task);
                            } catch (Exception singleError) {
                                Log.e(TAG, "[批量生成] 单个任务插入失败: " + task.getTaskContent(), singleError);
                            }
                        }
                    }
                }
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onTasksGenerated(allGeneratedTasks, !allGeneratedTasks.isEmpty());
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "generateTasksForDateRangeOptimized failed", e);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 生成日期范围内的所有日期
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期列表
     */
    private List<String> generateDateRange(String startDate, String endDate) {
        List<String> dates = new ArrayList<>();
        
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            
            if (start == null || end == null) {
                return dates;
            }
            
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(start);
            
            while (!calendar.getTime().after(end)) {
                dates.add(dateFormat.format(calendar.getTime()));
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "generateDateRange failed", e);
        }
        
        return dates;
    }
    
    /**
     * 根据日期找到对应的阶段
     * 
     * @param phases 阶段列表
     * @param date 日期
     * @return 对应的阶段，如果没有找到则返回null
     */
    private StudyPhaseEntity findPhaseForDate(List<StudyPhaseEntity> phases, String date) {
        for (StudyPhaseEntity phase : phases) {
            String phaseStart = phase.getStartDate();
            String phaseEnd = phase.getEndDate();
            
            // 如果阶段没有设置日期范围，检查是否是进行中的阶段
            if (phaseStart == null || phaseStart.isEmpty() || 
                phaseEnd == null || phaseEnd.isEmpty()) {
                if (StudyPhaseEntity.STATUS_IN_PROGRESS.equals(phase.getStatus())) {
                    return phase;
                }
                continue;
            }
            
            // 检查日期是否在阶段范围内
            if (date.compareTo(phaseStart) >= 0 && date.compareTo(phaseEnd) <= 0) {
                return phase;
            }
        }
        
        // 如果没有找到，返回第一个进行中的阶段
        for (StudyPhaseEntity phase : phases) {
            if (StudyPhaseEntity.STATUS_IN_PROGRESS.equals(phase.getStatus())) {
                return phase;
            }
        }
        
        // 如果还是没有，返回第一个未开始的阶段
        for (StudyPhaseEntity phase : phases) {
            if (StudyPhaseEntity.STATUS_NOT_STARTED.equals(phase.getStatus())) {
                return phase;
            }
        }
        
        return null;
    }
    
    /**
     * 获取TaskGenerator实例（用于测试）
     */
    public TaskGenerator getTaskGenerator() {
        return taskGenerator;
    }
    
    /**
     * 关闭服务，释放资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
