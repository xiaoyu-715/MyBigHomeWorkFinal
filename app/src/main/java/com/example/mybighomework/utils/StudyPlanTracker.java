package com.example.mybighomework.utils;

import android.content.Context;
import android.util.Log;

import com.example.mybighomework.StudyPlan;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 学习计划进度跟踪器
 * 详细跟踪学习计划的执行情况和完成度
 */
public class StudyPlanTracker {
    
    private static final String TAG = "StudyPlanTracker";
    
    private Context context;
    private AppDatabase database;
    private ExecutorService executorService;
    
    public StudyPlanTracker(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 进度统计结果
     */
    public static class ProgressStats {
        public int totalPlans;           // 总计划数
        public int activePlans;          // 进行中的计划数
        public int completedPlans;       // 已完成的计划数
        public double overallProgress;   // 总体进度 (0-100)
        public int todayStudyMinutes;    // 今日学习时长
        public int weekStudyMinutes;     // 本周学习时长
        public List<StudyPlan> urgentPlans; // 紧急计划
        public Map<String, Integer> categoryProgress; // 各类别进度
        
        public ProgressStats() {
            this.urgentPlans = new ArrayList<>();
            this.categoryProgress = new HashMap<>();
        }
    }
    
    /**
     * 计划执行状态
     */
    public enum ExecutionStatus {
        ON_TRACK("按计划进行"),
        BEHIND("进度落后"),
        AHEAD("超前完成"),
        OVERDUE("已逾期");
        
        private final String displayName;
        
        ExecutionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 获取进度统计
     */
    public void getProgressStats(OnProgressStatsListener listener) {
        executorService.execute(() -> {
            try {
                ProgressStats stats = calculateProgressStats();
                if (listener != null) {
                    listener.onStatsCalculated(stats);
                }
            } catch (Exception e) {
                Log.e(TAG, "计算进度统计失败", e);
                if (listener != null) {
                    listener.onError("统计失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 更新计划进度
     */
    public void updatePlanProgress(int planId, int newProgress, OnProgressUpdateListener listener) {
        executorService.execute(() -> {
            try {
                StudyPlanEntity plan = database.studyPlanDao().getStudyPlanById(planId);
                if (plan != null) {
                    plan.setProgress(Math.max(0, Math.min(100, newProgress)));
                    
                    // 如果进度达到100%，标记为已完成
                    if (newProgress >= 100) {
                        plan.setStatus("已完成");
                    } else if (newProgress > 0) {
                        plan.setStatus("进行中");
                    }
                    
                    database.studyPlanDao().update(plan);
                    
                    if (listener != null) {
                        listener.onProgressUpdated(planId, newProgress);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "更新进度失败", e);
                if (listener != null) {
                    listener.onError("更新失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 记录学习活动
     */
    public void recordStudyActivity(int planId, String activityType, int durationMinutes) {
        executorService.execute(() -> {
            try {
                StudyRecordEntity record = new StudyRecordEntity();
                record.setType(activityType);
                record.setVocabularyId(planId); // 复用字段存储计划ID
                record.setCorrect(true);
                record.setResponseTime(durationMinutes * 60 * 1000L); // 转换为毫秒
                record.setCreatedTime(System.currentTimeMillis());
                
                database.studyRecordDao().insert(record);
                
                // 自动更新计划进度
                autoUpdatePlanProgress(planId, durationMinutes);
                
            } catch (Exception e) {
                Log.e(TAG, "记录学习活动失败", e);
            }
        });
    }
    
    /**
     * 获取计划执行状态
     */
    public ExecutionStatus getPlanExecutionStatus(StudyPlan plan) {
        if (plan.getProgress() >= 100) {
            return ExecutionStatus.AHEAD;
        }
        
        // 简化的状态判断逻辑
        long currentTime = System.currentTimeMillis();
        String timeRange = plan.getTimeRange();
        
        if (timeRange != null && timeRange.contains("至")) {
            // 解析结束时间
            try {
                String[] parts = timeRange.split("至");
                if (parts.length == 2) {
                    String endDateStr = parts[1];
                    long endTime = parseTimeString(endDateStr);
                    
                    if (currentTime > endTime) {
                        return plan.getProgress() >= 100 ? ExecutionStatus.AHEAD : ExecutionStatus.OVERDUE;
                    }
                    
                    // 计算预期进度
                    String startDateStr = parts[0];
                    long startTime = parseTimeString(startDateStr);
                    long totalDuration = endTime - startTime;
                    long elapsed = currentTime - startTime;
                    
                    double expectedProgress = Math.min(100, (elapsed * 100.0) / totalDuration);
                    
                    if (plan.getProgress() >= expectedProgress * 1.1) {
                        return ExecutionStatus.AHEAD;
                    } else if (plan.getProgress() < expectedProgress * 0.8) {
                        return ExecutionStatus.BEHIND;
                    } else {
                        return ExecutionStatus.ON_TRACK;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "解析时间范围失败", e);
            }
        }
        
        return ExecutionStatus.ON_TRACK;
    }
    
    /**
     * 计算进度统计
     */
    private ProgressStats calculateProgressStats() {
        ProgressStats stats = new ProgressStats();
        
        // 获取所有学习计划
        List<StudyPlanEntity> allPlans = database.studyPlanDao().getAllStudyPlans();
        stats.totalPlans = allPlans.size();
        
        int totalProgress = 0;
        for (StudyPlanEntity plan : allPlans) {
            totalProgress += plan.getProgress();
            
            if (plan.getProgress() >= 100) {
                stats.completedPlans++;
            } else if (plan.getProgress() > 0) {
                stats.activePlans++;
            }
            
            // 统计各类别进度
            String category = plan.getCategory();
            stats.categoryProgress.put(category, 
                stats.categoryProgress.getOrDefault(category, 0) + plan.getProgress());
        }
        
        // 计算总体进度
        if (stats.totalPlans > 0) {
            stats.overallProgress = (double) totalProgress / stats.totalPlans;
        }
        
        // 计算学习时长
        calculateStudyTime(stats);
        
        // 找出紧急计划
        findUrgentPlans(stats, allPlans);
        
        return stats;
    }
    
    /**
     * 计算学习时长
     */
    private void calculateStudyTime(ProgressStats stats) {
        long todayStart = getTodayStartTime();
        long weekStart = getWeekStartTime();
        
        List<StudyRecordEntity> todayRecords = database.studyRecordDao().getRecordsSince(todayStart);
        List<StudyRecordEntity> weekRecords = database.studyRecordDao().getRecordsSince(weekStart);
        
        stats.todayStudyMinutes = calculateTotalMinutes(todayRecords);
        stats.weekStudyMinutes = calculateTotalMinutes(weekRecords);
    }
    
    /**
     * 找出紧急计划
     */
    private void findUrgentPlans(ProgressStats stats, List<StudyPlanEntity> allPlans) {
        for (StudyPlanEntity planEntity : allPlans) {
            StudyPlan plan = convertToStudyPlan(planEntity);
            ExecutionStatus status = getPlanExecutionStatus(plan);
            
            if (status == ExecutionStatus.BEHIND || status == ExecutionStatus.OVERDUE) {
                stats.urgentPlans.add(plan);
            }
        }
    }
    
    /**
     * 自动更新计划进度
     */
    private void autoUpdatePlanProgress(int planId, int studyMinutes) {
        try {
            StudyPlanEntity plan = database.studyPlanDao().getStudyPlanById(planId);
            if (plan != null) {
                // 简化的进度更新逻辑：每30分钟学习增加5%进度
                int progressIncrease = studyMinutes / 6; // 30分钟 = 5分钟 * 6
                int newProgress = Math.min(100, plan.getProgress() + progressIncrease);
                
                plan.setProgress(newProgress);
                if (newProgress >= 100) {
                    plan.setStatus("已完成");
                } else if (newProgress > 0) {
                    plan.setStatus("进行中");
                }
                
                database.studyPlanDao().update(plan);
            }
        } catch (Exception e) {
            Log.e(TAG, "自动更新进度失败", e);
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private long parseTimeString(String timeStr) {
        // 简化的时间解析，格式：YYYY-MM
        try {
            String[] parts = timeStr.trim().split("-");
            if (parts.length >= 2) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, 1, 0, 0, 0);
                return calendar.getTimeInMillis();
            }
        } catch (Exception e) {
            Log.w(TAG, "解析时间字符串失败: " + timeStr, e);
        }
        
        return System.currentTimeMillis();
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
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    private int calculateTotalMinutes(List<StudyRecordEntity> records) {
        int totalMinutes = 0;
        for (StudyRecordEntity record : records) {
            totalMinutes += (int) (record.getResponseTime() / (60 * 1000));
        }
        return totalMinutes;
    }
    
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
     * 回调接口
     */
    public interface OnProgressStatsListener {
        void onStatsCalculated(ProgressStats stats);
        void onError(String error);
    }
    
    public interface OnProgressUpdateListener {
        void onProgressUpdated(int planId, int newProgress);
        void onError(String error);
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
