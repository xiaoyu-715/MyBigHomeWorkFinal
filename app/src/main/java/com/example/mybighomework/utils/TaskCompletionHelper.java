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
 * 任务完成辅助类
 * 
 * @deprecated 此类已废弃，请使用 {@link TaskProgressTracker} 代替。
 * TaskProgressTracker 提供了更完善的任务进度追踪功能，支持：
 * - recordProgress() 用于累计型任务
 * - markSimpleTaskCompleted() 用于简单型任务
 * - 自动检测是否达到目标并标记完成
 */
@Deprecated
public class TaskCompletionHelper {
    
    private static final String TAG = "TaskCompletionHelper";
    
    public static void markTaskAsCompleted(Context context, String actionType) {
        if (context == null || actionType == null || actionType.isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            try {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());
                
                AppDatabase database = AppDatabase.getInstance(context);
                DailyTaskDao taskDao = database.dailyTaskDao();
                
                List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
                
                if (tasks != null && !tasks.isEmpty()) {
                    for (DailyTaskEntity task : tasks) {
                        if (!task.isCompleted()) {
                            task.setCompleted(true);
                            task.setCompletedAt(System.currentTimeMillis());
                            taskDao.update(task);
                            
                            Log.d(TAG, "自动标记任务完成: " + task.getTaskContent() + 
                                " (actionType=" + actionType + ")");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "标记任务失败", e);
            }
        }).start();
    }
    
    public static void markTaskAsCompletedWithDuration(Context context, String actionType, int actualMinutes) {
        if (context == null || actionType == null || actionType.isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            try {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());
                
                AppDatabase database = AppDatabase.getInstance(context);
                DailyTaskDao taskDao = database.dailyTaskDao();
                
                List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
                
                if (tasks != null && !tasks.isEmpty()) {
                    for (DailyTaskEntity task : tasks) {
                        if (!task.isCompleted()) {
                            task.setCompleted(true);
                            task.setCompletedAt(System.currentTimeMillis());
                            task.setActualMinutes(actualMinutes);
                            taskDao.update(task);
                            
                            Log.d(TAG, "自动标记任务完成: " + task.getTaskContent() + 
                                " (actionType=" + actionType + ", duration=" + actualMinutes + "分钟)");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "标记任务失败", e);
            }
        }).start();
    }
}
