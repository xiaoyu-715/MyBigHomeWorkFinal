package com.example.mybighomework.utils;

import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进度计算工具类
 * 提供学习计划和阶段进度的计算方法
 * 
 * Requirements: 5.1, 5.2, 5.5
 */
public class ProgressCalculator {

    /**
     * 计算阶段进度
     * 基于已完成天数 / 总天数的百分比
     * 
     * 算法说明：
     * 1. 将任务按日期分组
     * 2. 检查每天是否所有任务都已完成
     * 3. 统计完全完成的天数
     * 4. 进度 = (完全完成的天数 / 阶段总天数) * 100
     * 
     * @param phase 学习阶段实体
     * @param tasks 该阶段的所有任务列表
     * @return 阶段进度（0-100）
     * 
     * Requirements: 5.1, 5.5
     */
    public static int calculatePhaseProgress(StudyPhaseEntity phase, List<DailyTaskEntity> tasks) {
        // 边界条件：阶段天数为0时返回0
        if (phase == null || phase.getDurationDays() == 0) {
            return 0;
        }
        
        // 边界条件：没有任务时返回0
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        // 将任务按日期分组
        Map<String, List<DailyTaskEntity>> tasksByDate = groupTasksByDate(tasks);
        
        // 统计完全完成的天数（某天所有任务都完成才算完成）
        int fullyCompletedDays = 0;
        for (Map.Entry<String, List<DailyTaskEntity>> entry : tasksByDate.entrySet()) {
            List<DailyTaskEntity> dailyTasks = entry.getValue();
            boolean allCompleted = true;
            
            for (DailyTaskEntity task : dailyTasks) {
                if (!task.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted && !dailyTasks.isEmpty()) {
                fullyCompletedDays++;
            }
        }
        
        // 计算进度百分比，确保在0-100范围内
        int progress = (fullyCompletedDays * 100) / phase.getDurationDays();
        return Math.max(0, Math.min(100, progress));
    }

    /**
     * 计算计划总进度
     * 基于所有阶段的加权平均（按天数加权）
     * 
     * 算法说明：
     * 1. 计算所有阶段的总天数
     * 2. 计算每个阶段贡献的完成天数（阶段进度 * 阶段天数 / 100）
     * 3. 总进度 = (所有阶段贡献的完成天数之和 / 总天数) * 100
     * 
     * @param phases 所有阶段列表
     * @return 计划总进度（0-100）
     * 
     * Requirements: 5.2
     */
    public static int calculatePlanProgress(List<StudyPhaseEntity> phases) {
        // 边界条件：没有阶段时返回0
        if (phases == null || phases.isEmpty()) {
            return 0;
        }
        
        int totalDays = 0;
        int weightedCompletedDays = 0;
        
        for (StudyPhaseEntity phase : phases) {
            int phaseDays = phase.getDurationDays();
            totalDays += phaseDays;
            
            // 每个阶段贡献的完成天数 = 阶段进度 * 阶段天数 / 100
            weightedCompletedDays += (phase.getProgress() * phaseDays) / 100;
        }
        
        // 边界条件：总天数为0时返回0
        if (totalDays == 0) {
            return 0;
        }
        
        // 计算总进度百分比，确保在0-100范围内
        int progress = (weightedCompletedDays * 100) / totalDays;
        return Math.max(0, Math.min(100, progress));
    }

    /**
     * 将任务按日期分组
     * 
     * @param tasks 任务列表
     * @return 按日期分组的任务Map，key为日期字符串（yyyy-MM-dd），value为该日期的任务列表
     */
    public static Map<String, List<DailyTaskEntity>> groupTasksByDate(List<DailyTaskEntity> tasks) {
        Map<String, List<DailyTaskEntity>> tasksByDate = new HashMap<>();
        
        if (tasks == null || tasks.isEmpty()) {
            return tasksByDate;
        }
        
        for (DailyTaskEntity task : tasks) {
            String date = task.getDate();
            if (date != null && !date.isEmpty()) {
                if (!tasksByDate.containsKey(date)) {
                    tasksByDate.put(date, new ArrayList<>());
                }
                tasksByDate.get(date).add(task);
            }
        }
        
        return tasksByDate;
    }

    /**
     * 计算某一天的任务完成率
     * 
     * @param tasks 某一天的任务列表
     * @return 完成率（0-100）
     */
    public static int calculateDailyCompletionRate(List<DailyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int completedCount = 0;
        for (DailyTaskEntity task : tasks) {
            if (task.isCompleted()) {
                completedCount++;
            }
        }
        
        return (completedCount * 100) / tasks.size();
    }

    /**
     * 检查某一天的任务是否全部完成
     * 
     * @param tasks 某一天的任务列表
     * @return 是否全部完成
     */
    public static boolean isDayFullyCompleted(List<DailyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }
        
        for (DailyTaskEntity task : tasks) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 统计已完成的任务数量
     * 
     * @param tasks 任务列表
     * @return 已完成的任务数量
     */
    public static int countCompletedTasks(List<DailyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (DailyTaskEntity task : tasks) {
            if (task.isCompleted()) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * 计算任务列表的总预计时长（分钟）
     * 
     * @param tasks 任务列表
     * @return 总预计时长（分钟）
     */
    public static int calculateTotalEstimatedMinutes(List<DailyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int total = 0;
        for (DailyTaskEntity task : tasks) {
            total += task.getEstimatedMinutes();
        }
        
        return total;
    }

    /**
     * 计算已完成任务的实际时长总和（分钟）
     * 
     * @param tasks 任务列表
     * @return 已完成任务的实际时长总和（分钟）
     */
    public static int calculateTotalActualMinutes(List<DailyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int total = 0;
        for (DailyTaskEntity task : tasks) {
            if (task.isCompleted()) {
                total += task.getActualMinutes();
            }
        }
        
        return total;
    }
}
