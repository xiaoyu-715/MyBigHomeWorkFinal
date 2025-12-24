package com.example.mybighomework.utils;

import android.content.Context;
import android.util.Log;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 任务进度追踪器
 * 统一管理所有模块的任务进度追踪和自动完成
 */
public class TaskProgressTracker {
    
    private static final String TAG = "TaskProgressTracker";
    
    private static TaskProgressTracker instance;
    private final Context context;
    private final DailyTaskDao taskDao;
    private final SimpleDateFormat dateFormat;
    
    private TaskProgressTracker(Context context) {
        this.context = context.getApplicationContext();
        this.taskDao = AppDatabase.getInstance(context).dailyTaskDao();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized TaskProgressTracker getInstance(Context context) {
        if (instance == null) {
            instance = new TaskProgressTracker(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 记录进度（用于 count 类型任务）
     * @param actionType 操作类型
     * @param increment 增量（通常为1）
     */
    public void recordProgress(String actionType, int increment) {
        if (actionType == null || actionType.isEmpty()) {
            Log.w(TAG, "recordProgress: actionType is null or empty");
            return;
        }
        
        Log.d(TAG, "========== 智能任务完成系统 ==========");
        Log.d(TAG, "recordProgress called: actionType=" + actionType + ", increment=" + increment);
        
        new Thread(() -> {
            try {
                String today = getTodayDate();
                Log.d(TAG, "查询条件: actionType=" + actionType + ", date=" + today);
                
                // 先查询所有今日任务，用于调试
                List<DailyTaskEntity> allTodayTasks = taskDao.getTasksWithEmptyActionType();
                Log.d(TAG, "数据库中actionType为空的任务数: " + (allTodayTasks != null ? allTodayTasks.size() : 0));
                
                List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
                
                if (tasks == null || tasks.isEmpty()) {
                    Log.w(TAG, "❌ 未找到匹配任务! actionType=" + actionType + ", date=" + today);
                    Log.w(TAG, "提示: 请确认任务的actionType字段已正确设置，且日期匹配");
                    return;
                }
                
                Log.d(TAG, "✓ 找到 " + tasks.size() + " 个匹配任务");
                
                for (DailyTaskEntity task : tasks) {
                    if (task.isCompleted()) {
                        Log.d(TAG, "Task already completed, skipping: " + task.getTaskContent());
                        continue;
                    }
                    
                    // 更新进度
                    int newProgress = task.getCurrentProgress() + increment;
                    task.setCurrentProgress(newProgress);
                    
                    // 检查是否达到目标
                    int target = task.getCompletionTarget();
                    if (target <= 0) {
                        target = 1; // 默认目标为1
                    }
                    
                    Log.d(TAG, "Task: " + task.getTaskContent() + 
                          ", progress: " + newProgress + "/" + target);
                    
                    if (newProgress >= target) {
                        task.setCompleted(true);
                        task.setCompletedAt(System.currentTimeMillis());
                        Log.d(TAG, "✅ Task AUTO-COMPLETED: " + task.getTaskContent() + 
                              " (progress: " + newProgress + "/" + target + ")");
                    }
                    
                    taskDao.update(task);
                    Log.d(TAG, "Progress updated: " + actionType + 
                          " -> " + newProgress + "/" + target);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error recording progress for " + actionType, e);
            }
        }).start();
    }
    
    /**
     * 标记简单型任务完成（用于 simple 类型，如每日一句）
     * @param actionType 操作类型
     */
    public void markSimpleTaskCompleted(String actionType) {
        if (actionType == null || actionType.isEmpty()) {
            Log.w(TAG, "markSimpleTaskCompleted: actionType is null or empty");
            return;
        }
        
        Log.d(TAG, "markSimpleTaskCompleted called: actionType=" + actionType);
        
        new Thread(() -> {
            try {
                String today = getTodayDate();
                Log.d(TAG, "Querying simple tasks for actionType=" + actionType + ", date=" + today);
                
                List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
                
                if (tasks == null || tasks.isEmpty()) {
                    Log.d(TAG, "No tasks found for actionType: " + actionType + " on " + today);
                    return;
                }
                
                Log.d(TAG, "Found " + tasks.size() + " tasks for actionType: " + actionType);
                
                for (DailyTaskEntity task : tasks) {
                    if (task.isCompleted()) {
                        Log.d(TAG, "Task already completed, skipping: " + task.getTaskContent());
                        continue;
                    }
                    
                    // 简单型任务直接标记完成
                    String completionType = task.getCompletionType();
                    if (completionType == null || "simple".equals(completionType)) {
                        task.setCompleted(true);
                        task.setCompletedAt(System.currentTimeMillis());
                        task.setCurrentProgress(1);
                        taskDao.update(task);
                        Log.d(TAG, "✅ Simple task AUTO-COMPLETED: " + task.getTaskContent());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error marking simple task completed for " + actionType, e);
            }
        }).start();
    }

    
    /**
     * 获取今日指定类型任务的进度
     * @param actionType 操作类型
     * @param callback 回调
     */
    public void getProgress(String actionType, ProgressCallback callback) {
        if (callback == null) {
            return;
        }
        
        new Thread(() -> {
            try {
                String today = getTodayDate();
                List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
                
                int totalProgress = 0;
                int totalTarget = 0;
                
                if (tasks != null) {
                    for (DailyTaskEntity task : tasks) {
                        totalProgress += task.getCurrentProgress();
                        int target = task.getCompletionTarget();
                        totalTarget += (target > 0 ? target : 1);
                    }
                }
                
                callback.onResult(totalProgress, totalTarget);
            } catch (Exception e) {
                Log.e(TAG, "Error getting progress for " + actionType, e);
                callback.onResult(0, 0);
            }
        }).start();
    }
    
    /**
     * 检查指定类型是否有未完成任务
     * @param actionType 操作类型
     * @param callback 回调
     */
    public void hasUncompletedTasks(String actionType, BooleanCallback callback) {
        if (callback == null) {
            return;
        }
        
        new Thread(() -> {
            try {
                String today = getTodayDate();
                List<DailyTaskEntity> tasks = taskDao.getUncompletedTasksByActionType(actionType, today);
                callback.onResult(tasks != null && !tasks.isEmpty());
            } catch (Exception e) {
                Log.e(TAG, "Error checking uncompleted tasks for " + actionType, e);
                callback.onResult(false);
            }
        }).start();
    }
    
    /**
     * 获取今日日期字符串
     */
    private String getTodayDate() {
        return dateFormat.format(new Date());
    }
    
    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onResult(int currentProgress, int targetProgress);
    }
    
    /**
     * 布尔值回调接口
     */
    public interface BooleanCallback {
        void onResult(boolean result);
    }
}
