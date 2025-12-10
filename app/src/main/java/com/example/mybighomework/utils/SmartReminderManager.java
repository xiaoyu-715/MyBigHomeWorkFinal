package com.example.mybighomework.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mybighomework.MainActivity;
import com.example.mybighomework.R;
import com.example.mybighomework.StudyPlan;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.StudyPlanEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 智能提醒管理器
 * 基于用户学习习惯的智能化提醒系统
 */
public class SmartReminderManager {
    
    private static final String TAG = "SmartReminderManager";
    private static final String PREF_NAME = "smart_reminder_prefs";
    private static final String CHANNEL_ID = "study_reminder_channel";
    
    // SharedPreferences Keys
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_PREFERRED_TIME_HOUR = "preferred_time_hour";
    private static final String KEY_PREFERRED_TIME_MINUTE = "preferred_time_minute";
    private static final String KEY_REMINDER_FREQUENCY = "reminder_frequency";
    private static final String KEY_LAST_STUDY_TIME = "last_study_time";
    
    private Context context;
    private SharedPreferences preferences;
    private AppDatabase database;
    private ExecutorService executorService;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;
    
    public SmartReminderManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        createNotificationChannel();
    }
    
    /**
     * 提醒频率枚举
     */
    public enum ReminderFrequency {
        DAILY("每天", 1),
        EVERY_OTHER_DAY("隔天", 2),
        WEEKLY("每周", 7),
        SMART("智能", 0); // 0表示根据用户习惯动态调整
        
        private final String displayName;
        private final int intervalDays;
        
        ReminderFrequency(String displayName, int intervalDays) {
            this.displayName = displayName;
            this.intervalDays = intervalDays;
        }
        
        public String getDisplayName() { return displayName; }
        public int getIntervalDays() { return intervalDays; }
    }
    
    /**
     * 智能提醒配置
     */
    public static class ReminderConfig {
        public boolean enabled;
        public int preferredHour;        // 偏好提醒小时 (0-23)
        public int preferredMinute;      // 偏好提醒分钟 (0-59)
        public ReminderFrequency frequency;
        public boolean adaptiveEnabled;  // 是否启用自适应提醒
        public boolean urgentPlansOnly;  // 是否只提醒紧急计划
        
        public ReminderConfig() {
            this.enabled = true;
            this.preferredHour = 20;  // 默认晚上8点
            this.preferredMinute = 0;
            this.frequency = ReminderFrequency.SMART;
            this.adaptiveEnabled = true;
            this.urgentPlansOnly = false;
        }
    }
    
    /**
     * 启用智能提醒
     */
    public void enableSmartReminder(ReminderConfig config) {
        saveReminderConfig(config);
        
        if (config.enabled) {
            // 分析用户学习习惯
            analyzeStudyHabits(new OnHabitsAnalyzedListener() {
                @Override
                public void onHabitsAnalyzed(StudyHabits habits) {
                    // 根据分析结果调整提醒配置
                    ReminderConfig adjustedConfig = adjustConfigBasedOnHabits(config, habits);
                    
                    // 设置提醒
                    scheduleNextReminder(adjustedConfig);
                    
                    Log.d(TAG, "智能提醒已启用，下次提醒时间: " + 
                          adjustedConfig.preferredHour + ":" + adjustedConfig.preferredMinute);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "分析学习习惯失败: " + error);
                    // 使用默认配置
                    scheduleNextReminder(config);
                }
            });
        } else {
            cancelAllReminders();
        }
    }
    
    /**
     * 学习习惯分析结果
     */
    public static class StudyHabits {
        public int mostActiveHour;           // 最活跃的学习时间
        public int averageStudyDuration;     // 平均学习时长
        public int continuousStudyDays;      // 连续学习天数
        public double studyConsistency;      // 学习一致性 (0-1)
        public long lastStudyTime;           // 最后学习时间
        public boolean isRegularLearner;     // 是否规律学习者
        
        @Override
        public String toString() {
            return "StudyHabits{" +
                    "mostActiveHour=" + mostActiveHour +
                    ", avgDuration=" + averageStudyDuration +
                    ", continuousDays=" + continuousStudyDays +
                    ", consistency=" + studyConsistency +
                    ", isRegular=" + isRegularLearner +
                    '}';
        }
    }
    
    /**
     * 分析用户学习习惯
     */
    public void analyzeStudyHabits(OnHabitsAnalyzedListener listener) {
        executorService.execute(() -> {
            try {
                StudyHabits habits = new StudyHabits();
                
                // 获取最近30天的学习记录
                long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
                List<StudyRecordEntity> recentRecords = 
                    database.studyRecordDao().getRecordsSince(thirtyDaysAgo);
                
                if (!recentRecords.isEmpty()) {
                    // 分析最活跃时间
                    habits.mostActiveHour = analyzeMostActiveHour(recentRecords);
                    
                    // 计算平均学习时长
                    habits.averageStudyDuration = calculateAverageStudyDuration(recentRecords);
                    
                    // 计算连续学习天数
                    habits.continuousStudyDays = calculateContinuousStudyDays(recentRecords);
                    
                    // 计算学习一致性
                    habits.studyConsistency = calculateStudyConsistency(recentRecords);
                    
                    // 获取最后学习时间
                    habits.lastStudyTime = getLastStudyTime(recentRecords);
                    
                    // 判断是否为规律学习者
                    habits.isRegularLearner = habits.studyConsistency > 0.7 && 
                                            habits.continuousStudyDays >= 5;
                } else {
                    // 新用户默认值
                    habits.mostActiveHour = 20;
                    habits.averageStudyDuration = 30;
                    habits.continuousStudyDays = 0;
                    habits.studyConsistency = 0.0;
                    habits.lastStudyTime = 0;
                    habits.isRegularLearner = false;
                }
                
                Log.d(TAG, "学习习惯分析结果: " + habits);
                
                if (listener != null) {
                    listener.onHabitsAnalyzed(habits);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "分析学习习惯失败", e);
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据学习习惯调整提醒配置
     */
    private ReminderConfig adjustConfigBasedOnHabits(ReminderConfig originalConfig, StudyHabits habits) {
        ReminderConfig adjustedConfig = new ReminderConfig();
        adjustedConfig.enabled = originalConfig.enabled;
        adjustedConfig.adaptiveEnabled = originalConfig.adaptiveEnabled;
        adjustedConfig.urgentPlansOnly = originalConfig.urgentPlansOnly;
        
        if (originalConfig.adaptiveEnabled && habits.isRegularLearner) {
            // 为规律学习者调整提醒时间
            adjustedConfig.preferredHour = habits.mostActiveHour;
            adjustedConfig.preferredMinute = originalConfig.preferredMinute;
            
            // 根据学习一致性调整频率
            if (habits.studyConsistency > 0.9) {
                adjustedConfig.frequency = ReminderFrequency.EVERY_OTHER_DAY;
            } else if (habits.studyConsistency > 0.7) {
                adjustedConfig.frequency = ReminderFrequency.DAILY;
            } else {
                adjustedConfig.frequency = ReminderFrequency.DAILY;
            }
        } else {
            // 使用原始配置
            adjustedConfig.preferredHour = originalConfig.preferredHour;
            adjustedConfig.preferredMinute = originalConfig.preferredMinute;
            adjustedConfig.frequency = originalConfig.frequency;
        }
        
        return adjustedConfig;
    }
    
    /**
     * 安排下次提醒
     */
    private void scheduleNextReminder(ReminderConfig config) {
        if (!config.enabled) return;
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, config.preferredHour);
        calendar.set(Calendar.MINUTE, config.preferredMinute);
        calendar.set(Calendar.SECOND, 0);
        
        // 如果今天的提醒时间已过，安排到明天
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 根据频率调整间隔
        if (config.frequency != ReminderFrequency.SMART && 
            config.frequency.getIntervalDays() > 1) {
            calendar.add(Calendar.DAY_OF_MONTH, config.frequency.getIntervalDays() - 1);
        }
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("config_enabled", config.enabled);
        intent.putExtra("config_urgent_only", config.urgentPlansOnly);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
        }
        
        Log.d(TAG, "下次提醒已安排: " + calendar.getTime());
    }
    
    /**
     * 取消所有提醒
     */
    public void cancelAllReminders() {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        
        Log.d(TAG, "所有提醒已取消");
    }
    
    /**
     * 显示学习提醒通知
     */
    public static void showStudyReminder(Context context, boolean urgentOnly) {
        AppDatabase database = AppDatabase.getInstance(context);
        
        // 获取需要提醒的计划
        List<StudyPlanEntity> plansToRemind = urgentOnly ? 
            getUrgentPlans(database) : getActivePlans(database);
        
        if (!plansToRemind.isEmpty()) {
            String title = "学习提醒";
            String content = urgentOnly ? 
                "您有 " + plansToRemind.size() + " 个紧急学习计划需要关注" :
                "是时候开始今天的学习了！";
            
            showNotification(context, title, content);
            
            // 更新最后提醒时间
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putLong("last_reminder_time", System.currentTimeMillis()).apply();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "学习提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("智能学习计划提醒通知");
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private void saveReminderConfig(ReminderConfig config) {
        preferences.edit()
            .putBoolean(KEY_REMINDER_ENABLED, config.enabled)
            .putInt(KEY_PREFERRED_TIME_HOUR, config.preferredHour)
            .putInt(KEY_PREFERRED_TIME_MINUTE, config.preferredMinute)
            .putString(KEY_REMINDER_FREQUENCY, config.frequency.name())
            .apply();
    }
    
    private int analyzeMostActiveHour(List<StudyRecordEntity> records) {
        int[] hourCounts = new int[24];
        
        for (StudyRecordEntity record : records) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(record.getCreatedTime());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            hourCounts[hour]++;
        }
        
        int maxCount = 0;
        int mostActiveHour = 20; // 默认晚上8点
        
        for (int i = 0; i < 24; i++) {
            if (hourCounts[i] > maxCount) {
                maxCount = hourCounts[i];
                mostActiveHour = i;
            }
        }
        
        return mostActiveHour;
    }
    
    private int calculateAverageStudyDuration(List<StudyRecordEntity> records) {
        if (records.isEmpty()) return 30;
        
        long totalMinutes = 0;
        for (StudyRecordEntity record : records) {
            totalMinutes += record.getResponseTime() / (60 * 1000);
        }
        
        return (int) (totalMinutes / records.size());
    }
    
    private int calculateContinuousStudyDays(List<StudyRecordEntity> records) {
        // 简化实现：基于记录数量估算
        return Math.min(records.size() / 5, 30); // 假设每天5条记录
    }
    
    private double calculateStudyConsistency(List<StudyRecordEntity> records) {
        if (records.size() < 7) return 0.5; // 数据不足
        
        // 简化实现：基于学习频率计算一致性
        long daySpan = (System.currentTimeMillis() - records.get(0).getCreatedTime()) / (24 * 60 * 60 * 1000);
        double studyDays = records.size() / 5.0; // 假设每天平均5条记录
        
        return Math.min(1.0, studyDays / Math.max(1, daySpan));
    }
    
    private long getLastStudyTime(List<StudyRecordEntity> records) {
        if (records.isEmpty()) return 0;
        
        long lastTime = 0;
        for (StudyRecordEntity record : records) {
            if (record.getCreatedTime() > lastTime) {
                lastTime = record.getCreatedTime();
            }
        }
        
        return lastTime;
    }
    
    private static List<StudyPlanEntity> getUrgentPlans(AppDatabase database) {
        // 获取进度落后或即将到期的计划
        return database.studyPlanDao().getPlansWithProgressLessThan(50);
    }
    
    private static List<StudyPlanEntity> getActivePlans(AppDatabase database) {
        return database.studyPlanDao().getActivePlans();
    }
    
    private static void showNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1001, builder.build());
    }
    
    // ==================== 回调接口 ====================
    
    public interface OnHabitsAnalyzedListener {
        void onHabitsAnalyzed(StudyHabits habits);
        void onError(String error);
    }
    
    /**
     * 提醒接收器
     */
    public static class ReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean enabled = intent.getBooleanExtra("config_enabled", true);
            boolean urgentOnly = intent.getBooleanExtra("config_urgent_only", false);
            
            if (enabled) {
                showStudyReminder(context, urgentOnly);
                
                // 安排下次提醒
                SmartReminderManager manager = new SmartReminderManager(context);
                ReminderConfig config = new ReminderConfig();
                config.enabled = enabled;
                config.urgentPlansOnly = urgentOnly;
                manager.scheduleNextReminder(config);
            }
        }
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
