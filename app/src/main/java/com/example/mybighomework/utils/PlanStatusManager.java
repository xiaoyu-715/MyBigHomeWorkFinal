package com.example.mybighomework.utils;

import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.dao.StudyPhaseDao;
import com.example.mybighomework.database.dao.StudyPlanDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 计划状态管理器
 * 负责根据任务完成情况自动更新阶段和计划的状态
 * 
 * Requirements: 5.3, 5.4
 */
public class PlanStatusManager {

    private final StudyPlanDao studyPlanDao;
    private final StudyPhaseDao studyPhaseDao;
    private final DailyTaskDao dailyTaskDao;

    /**
     * 构造函数
     * @param studyPlanDao 学习计划DAO
     * @param studyPhaseDao 学习阶段DAO
     * @param dailyTaskDao 每日任务DAO
     */
    public PlanStatusManager(StudyPlanDao studyPlanDao, 
                            StudyPhaseDao studyPhaseDao, 
                            DailyTaskDao dailyTaskDao) {
        this.studyPlanDao = studyPlanDao;
        this.studyPhaseDao = studyPhaseDao;
        this.dailyTaskDao = dailyTaskDao;
    }

    /**
     * 根据任务完成情况更新阶段状态
     * 
     * 逻辑说明：
     * 1. 获取阶段的所有任务
     * 2. 使用ProgressCalculator计算阶段进度
     * 3. 根据进度更新阶段状态：
     *    - 进度为0且无任务完成：保持当前状态
     *    - 进度大于0但小于100：进行中
     *    - 进度为100：已完成
     * 4. 更新数据库中的阶段状态和进度
     * 
     * @param phaseId 阶段ID
     * @return 更新后的阶段实体，如果阶段不存在返回null
     * 
     * Requirements: 5.3
     */
    public StudyPhaseEntity updatePhaseStatus(int phaseId) {
        // 获取阶段信息
        StudyPhaseEntity phase = studyPhaseDao.getPhaseById(phaseId);
        if (phase == null) {
            return null;
        }

        // 获取阶段的所有任务
        List<DailyTaskEntity> tasks = dailyTaskDao.getTasksByPhase(phaseId);
        
        // 计算阶段进度
        int progress = ProgressCalculator.calculatePhaseProgress(phase, tasks);
        
        // 确定新状态
        String newStatus = determinePhaseStatus(phase, tasks, progress);
        
        // 计算已完成天数
        int completedDays = calculateCompletedDays(tasks);
        
        // 更新阶段信息
        phase.setProgress(progress);
        phase.setStatus(newStatus);
        phase.setCompletedDays(completedDays);
        
        // 保存到数据库
        studyPhaseDao.update(phase);
        
        return phase;
    }

    /**
     * 根据阶段完成情况更新计划状态
     * 
     * 逻辑说明：
     * 1. 获取计划的所有阶段
     * 2. 使用ProgressCalculator计算计划总进度
     * 3. 根据阶段状态更新计划状态：
     *    - 所有阶段都未开始：未开始
     *    - 有阶段进行中或已完成：进行中
     *    - 所有阶段都已完成：已完成
     * 4. 更新数据库中的计划状态和进度
     * 
     * @param planId 计划ID
     * @return 更新后的计划实体，如果计划不存在返回null
     * 
     * Requirements: 5.4
     */
    public StudyPlanEntity updatePlanStatus(int planId) {
        // 获取计划信息
        StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
        if (plan == null) {
            return null;
        }
        
        // 获取计划的所有阶段
        List<StudyPhaseEntity> phases = studyPhaseDao.getPhasesByPlanId(planId);
        
        // 计算计划总进度
        int progress = ProgressCalculator.calculatePlanProgress(phases);
        
        // 确定新状态
        String newStatus = determinePlanStatus(phases, progress);
        
        // 计算已完成天数
        int completedDays = calculatePlanCompletedDays(phases);
        
        // 更新计划信息
        plan.setProgress(progress);
        plan.setStatus(newStatus);
        plan.setCompletedDays(completedDays);
        plan.setLastModifiedTime(System.currentTimeMillis());
        
        // 保存到数据库
        studyPlanDao.update(plan);
        
        return plan;
    }

    /**
     * 检查是否需要进入下一阶段，如果需要则自动切换
     * 
     * 逻辑说明：
     * 1. 获取当前进行中的阶段
     * 2. 检查当前阶段是否已完成（进度100%）
     * 3. 如果已完成，查找下一个阶段
     * 4. 将下一个阶段设置为"进行中"
     * 5. 如果没有下一个阶段，说明计划已完成
     * 
     * @param planId 计划ID
     * @return 切换结果：
     *         - 返回新的当前阶段（如果成功切换）
     *         - 返回null（如果无需切换或计划已完成）
     * 
     * Requirements: 5.3, 5.4
     */
    public StudyPhaseEntity checkAndAdvancePhase(int planId) {
        // 获取当前进行中的阶段
        StudyPhaseEntity currentPhase = studyPhaseDao.getCurrentPhase(planId);
        
        // 如果没有进行中的阶段，尝试启动第一个未开始的阶段
        if (currentPhase == null) {
            return startFirstPhase(planId);
        }
        
        // 检查当前阶段是否已完成
        if (!isPhaseCompleted(currentPhase)) {
            // 当前阶段未完成，无需切换
            return null;
        }
        
        // 将当前阶段标记为已完成
        currentPhase.setStatus(StudyPhaseEntity.STATUS_COMPLETED);
        currentPhase.setProgress(100);
        studyPhaseDao.update(currentPhase);
        
        // 查找下一个阶段
        StudyPhaseEntity nextPhase = studyPhaseDao.getNextPhase(planId, currentPhase.getPhaseOrder());
        
        if (nextPhase != null) {
            // 启动下一个阶段
            nextPhase.setStatus(StudyPhaseEntity.STATUS_IN_PROGRESS);
            studyPhaseDao.update(nextPhase);
            
            // 更新计划状态
            updatePlanStatus(planId);
            
            return nextPhase;
        } else {
            // 没有下一个阶段，计划已完成
            updatePlanStatus(planId);
            return null;
        }
    }


    /**
     * 任务完成后的完整状态更新流程
     * 
     * 这是一个便捷方法，在任务完成状态变化后调用，
     * 会自动更新阶段状态、检查阶段切换、更新计划状态
     * 
     * @param taskId 任务ID
     * @return 更新后的计划实体
     */
    public StudyPlanEntity onTaskCompletionChanged(int taskId) {
        // 获取任务信息
        DailyTaskEntity task = dailyTaskDao.getTaskById(taskId);
        if (task == null) {
            return null;
        }
        
        int phaseId = task.getPhaseId();
        int planId = task.getPlanId();
        
        // 1. 更新阶段状态
        updatePhaseStatus(phaseId);
        
        // 2. 检查是否需要切换阶段 (由ProgressSyncService或TaskGenerationService统一处理)
        // checkAndAdvancePhase(planId);
        
        // 3. 更新计划状态
        return updatePlanStatus(planId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 确定阶段状态
     * 
     * @param phase 阶段实体
     * @param tasks 阶段任务列表
     * @param progress 计算出的进度
     * @return 新状态
     */
    private String determinePhaseStatus(StudyPhaseEntity phase, 
                                        List<DailyTaskEntity> tasks, 
                                        int progress) {
        // 如果没有任务，保持当前状态
        if (tasks == null || tasks.isEmpty()) {
            return phase.getStatus();
        }
        
        // 进度为100%，阶段已完成
        if (progress >= 100) {
            return StudyPhaseEntity.STATUS_COMPLETED;
        }
        
        // 检查是否有任何任务完成
        boolean hasCompletedTask = false;
        for (DailyTaskEntity task : tasks) {
            if (task.isCompleted()) {
                hasCompletedTask = true;
                break;
            }
        }
        
        // 有任务完成，阶段进行中
        if (hasCompletedTask || progress > 0) {
            return StudyPhaseEntity.STATUS_IN_PROGRESS;
        }
        
        // 没有任务完成，保持当前状态（可能是未开始或进行中）
        // 如果当前是进行中，保持进行中（用户可能刚开始）
        if (StudyPhaseEntity.STATUS_IN_PROGRESS.equals(phase.getStatus())) {
            return StudyPhaseEntity.STATUS_IN_PROGRESS;
        }
        
        return StudyPhaseEntity.STATUS_NOT_STARTED;
    }

    /**
     * 确定计划状态
     * 
     * @param phases 计划的所有阶段
     * @param progress 计算出的进度
     * @return 新状态
     */
    private String determinePlanStatus(List<StudyPhaseEntity> phases, int progress) {
        // 如果没有阶段，返回未开始
        if (phases == null || phases.isEmpty()) {
            return StudyPlanEntity.STATUS_NOT_STARTED;
        }
        
        // 进度为100%，计划已完成
        if (progress >= 100) {
            return StudyPlanEntity.STATUS_COMPLETED;
        }
        
        // 检查阶段状态
        boolean allNotStarted = true;
        boolean allCompleted = true;
        
        for (StudyPhaseEntity phase : phases) {
            String status = phase.getStatus();
            
            if (!StudyPhaseEntity.STATUS_NOT_STARTED.equals(status)) {
                allNotStarted = false;
            }
            
            if (!StudyPhaseEntity.STATUS_COMPLETED.equals(status)) {
                allCompleted = false;
            }
        }
        
        // 所有阶段都已完成
        if (allCompleted) {
            return StudyPlanEntity.STATUS_COMPLETED;
        }
        
        // 所有阶段都未开始
        if (allNotStarted) {
            return StudyPlanEntity.STATUS_NOT_STARTED;
        }
        
        // 有阶段进行中或部分完成
        return StudyPlanEntity.STATUS_IN_PROGRESS;
    }

    /**
     * 计算阶段已完成的天数
     * 
     * @param tasks 阶段任务列表
     * @return 已完成天数
     */
    private int calculateCompletedDays(List<DailyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        // 按日期分组并检查每天是否全部完成
        java.util.Map<String, java.util.List<DailyTaskEntity>> tasksByDate = 
            ProgressCalculator.groupTasksByDate(tasks);
        
        int completedDays = 0;
        for (java.util.Map.Entry<String, java.util.List<DailyTaskEntity>> entry : tasksByDate.entrySet()) {
            if (ProgressCalculator.isDayFullyCompleted(entry.getValue())) {
                completedDays++;
            }
        }
        
        return completedDays;
    }

    /**
     * 计算计划已完成的总天数
     * 
     * @param phases 计划的所有阶段
     * @return 已完成天数
     */
    private int calculatePlanCompletedDays(List<StudyPhaseEntity> phases) {
        if (phases == null || phases.isEmpty()) {
            return 0;
        }
        
        int totalCompletedDays = 0;
        for (StudyPhaseEntity phase : phases) {
            totalCompletedDays += phase.getCompletedDays();
        }
        
        return totalCompletedDays;
    }

    /**
     * 检查阶段是否已完成
     * 
     * @param phase 阶段实体
     * @return 是否已完成
     */
    private boolean isPhaseCompleted(StudyPhaseEntity phase) {
        // 检查状态
        if (StudyPhaseEntity.STATUS_COMPLETED.equals(phase.getStatus())) {
            return true;
        }
        
        // 检查进度
        if (phase.getProgress() >= 100) {
            return true;
        }
        
        // 检查任务完成情况
        List<DailyTaskEntity> tasks = dailyTaskDao.getTasksByPhase(phase.getId());
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }
        
        // 重新计算进度
        int progress = ProgressCalculator.calculatePhaseProgress(phase, tasks);
        return progress >= 100;
    }

    /**
     * 启动计划的第一个阶段
     * 
     * @param planId 计划ID
     * @return 启动的阶段，如果没有阶段返回null
     */
    private StudyPhaseEntity startFirstPhase(int planId) {
        // 获取第一个阶段（按顺序）
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
            studyPhaseDao.update(firstPhase);
            
            // 同时更新计划状态为进行中
            StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
            if (plan != null && StudyPlanEntity.STATUS_NOT_STARTED.equals(plan.getStatus())) {
                plan.setStatus(StudyPlanEntity.STATUS_IN_PROGRESS);
                studyPlanDao.update(plan);
            }
            
            return firstPhase;
        }
        
        return null;
    }

    // ==================== 批量更新方法 ====================

    /**
     * 更新计划的所有阶段状态
     * 
     * @param planId 计划ID
     */
    public void updateAllPhaseStatuses(int planId) {
        List<StudyPhaseEntity> phases = studyPhaseDao.getPhasesByPlanId(planId);
        if (phases != null) {
            for (StudyPhaseEntity phase : phases) {
                updatePhaseStatus(phase.getId());
            }
        }
    }

    /**
     * 批量更新多个阶段的状态（优化版本）
     * 减少数据库查询次数，提高性能
     * 
     * @param phaseIds 阶段ID列表
     */
    public void batchUpdatePhaseStatuses(List<Integer> phaseIds) {
        if (phaseIds == null || phaseIds.isEmpty()) {
            return;
        }
        
        // 批量获取阶段信息
        List<StudyPhaseEntity> phases = new ArrayList<>();
        for (int phaseId : phaseIds) {
            StudyPhaseEntity phase = studyPhaseDao.getPhaseById(phaseId);
            if (phase != null) {
                phases.add(phase);
            }
        }
        
        // 批量获取任务信息
        List<DailyTaskEntity> allTasks = new ArrayList<>();
        for (StudyPhaseEntity phase : phases) {
            List<DailyTaskEntity> tasks = dailyTaskDao.getTasksByPhase(phase.getId());
            if (tasks != null) {
                allTasks.addAll(tasks);
            }
        }
        
        // 按阶段分组任务
        java.util.Map<Integer, List<DailyTaskEntity>> tasksByPhase = new java.util.HashMap<>();
        for (DailyTaskEntity task : allTasks) {
            int phaseId = task.getPhaseId();
            if (!tasksByPhase.containsKey(phaseId)) {
                tasksByPhase.put(phaseId, new ArrayList<>());
            }
            tasksByPhase.get(phaseId).add(task);
        }
        
        // 批量更新阶段状态
        List<StudyPhaseEntity> phasesToUpdate = new ArrayList<>();
        for (StudyPhaseEntity phase : phases) {
            List<DailyTaskEntity> tasks = tasksByPhase.get(phase.getId());
            if (tasks == null) {
                tasks = new ArrayList<>();
            }
            
            // 计算新状态
            int progress = ProgressCalculator.calculatePhaseProgress(phase, tasks);
            String newStatus = determinePhaseStatus(phase, tasks, progress);
            int completedDays = calculateCompletedDays(tasks);
            
            // 检查是否有变化
            if (phase.getProgress() != progress || 
                !java.util.Objects.equals(phase.getStatus(), newStatus) ||
                phase.getCompletedDays() != completedDays) {
                
                phase.setProgress(progress);
                phase.setStatus(newStatus);
                phase.setCompletedDays(completedDays);
                phasesToUpdate.add(phase);
            }
        }
        
        // 批量保存到数据库
        if (!phasesToUpdate.isEmpty()) {
            for (StudyPhaseEntity phase : phasesToUpdate) {
                studyPhaseDao.update(phase);
            }
        }
    }

    /**
     * 批量更新多个计划的状态（优化版本）
     * 
     * @param planIds 计划ID列表
     * @return 更新后的计划列表
     */
    public List<StudyPlanEntity> batchUpdatePlanStatuses(List<Integer> planIds) {
        List<StudyPlanEntity> updatedPlans = new ArrayList<>();
        
        if (planIds == null || planIds.isEmpty()) {
            return updatedPlans;
        }
        
        // 批量获取计划信息
        List<StudyPlanEntity> plans = new ArrayList<>();
        for (int planId : planIds) {
            StudyPlanEntity plan = studyPlanDao.getStudyPlanById(planId);
            if (plan != null) {
                plans.add(plan);
            }
        }
        
        // 批量更新每个计划的状态
        List<StudyPlanEntity> plansToUpdate = new ArrayList<>();
        for (StudyPlanEntity plan : plans) {
            // 获取计划的所有阶段
            List<StudyPhaseEntity> phases = studyPhaseDao.getPhasesByPlanId(plan.getId());
            
            // 计算新状态
            int progress = ProgressCalculator.calculatePlanProgress(phases);
            String newStatus = determinePlanStatus(phases, progress);
            int completedDays = calculatePlanCompletedDays(phases);
            
            // 检查是否有变化
            if (plan.getProgress() != progress || 
                !java.util.Objects.equals(plan.getStatus(), newStatus) ||
                plan.getCompletedDays() != completedDays) {
                
                plan.setProgress(progress);
                plan.setStatus(newStatus);
                plan.setCompletedDays(completedDays);
                plan.setLastModifiedTime(System.currentTimeMillis());
                plansToUpdate.add(plan);
            }
        }
        
        // 批量保存到数据库
        for (StudyPlanEntity plan : plansToUpdate) {
            studyPlanDao.update(plan);
            updatedPlans.add(plan);
        }
        
        return updatedPlans;
    }

    /**
     * 完整的状态同步
     * 更新所有阶段状态，然后更新计划状态
     * 
     * @param planId 计划ID
     * @return 更新后的计划实体
     */
    public StudyPlanEntity syncPlanStatus(int planId) {
        // 1. 更新所有阶段状态
        updateAllPhaseStatuses(planId);
        
        // 2. 检查阶段切换
        checkAndAdvancePhase(planId);
        
        // 3. 更新计划状态
        return updatePlanStatus(planId);
    }
    
    /**
     * 智能状态同步（优化版本）
     * 只更新需要更新的阶段和计划，减少不必要的数据库操作
     * 
     * @param planId 计划ID
     * @param affectedTaskIds 受影响的任务ID列表（可选）
     * @return 更新后的计划实体
     */
    public StudyPlanEntity smartSyncPlanStatus(int planId, List<Integer> affectedTaskIds) {
        // 如果有受影响的任务，只更新相关阶段
        if (affectedTaskIds != null && !affectedTaskIds.isEmpty()) {
            // 获取受影响的阶段ID
            java.util.Set<Integer> affectedPhaseIds = new java.util.HashSet<>();
            for (int taskId : affectedTaskIds) {
                DailyTaskEntity task = dailyTaskDao.getTaskById(taskId);
                if (task != null) {
                    affectedPhaseIds.add(task.getPhaseId());
                }
            }
            
            // 批量更新受影响的阶段
            if (!affectedPhaseIds.isEmpty()) {
                batchUpdatePhaseStatuses(new ArrayList<>(affectedPhaseIds));
            }
        } else {
            // 没有指定受影响的任务，更新所有阶段
            updateAllPhaseStatuses(planId);
        }
        
        // 检查阶段切换
        checkAndAdvancePhase(planId);
        
        // 更新计划状态
        return updatePlanStatus(planId);
    }
    
    /**
     * 定期状态检查和更新
     * 用于后台定期同步所有活跃计划的状态
     * 
     * @param maxPlans 最大处理计划数
     * @return 更新的计划数量
     */
    public int periodicStatusCheck(int maxPlans) {
        // 获取所有进行中的计划
        List<StudyPlanEntity> activePlans = studyPlanDao.getActivePlans();
        
        // 限制处理数量
        if (activePlans != null && activePlans.size() > maxPlans) {
            activePlans = activePlans.subList(0, maxPlans);
        }
        
        if (activePlans == null || activePlans.isEmpty()) {
            return 0;
        }
        
        int updatedCount = 0;
        for (StudyPlanEntity plan : activePlans) {
            try {
                StudyPlanEntity updated = syncPlanStatus(plan.getId());
                if (updated != null) {
                    updatedCount++;
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他计划
                e.printStackTrace();
            }
        }
        
        return updatedCount;
    }
}
