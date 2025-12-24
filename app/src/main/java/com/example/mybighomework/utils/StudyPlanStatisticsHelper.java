package com.example.mybighomework.utils;

import com.example.mybighomework.database.dao.DailyTaskDao;
import com.example.mybighomework.database.entity.DailyTaskEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 学习计划统计工具类
 * 提供学习计划相关的统计计算方法
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5
 */
public class StudyPlanStatisticsHelper {
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final DailyTaskDao dailyTaskDao;
    
    /**
     * 构造函数
     * @param dailyTaskDao 每日任务DAO
     */
    public StudyPlanStatisticsHelper(DailyTaskDao dailyTaskDao) {
        this.dailyTaskDao = dailyTaskDao;
    }
    
    /**
     * 计算连续学习天数
     * 从今天往前计算连续有完成任务的天数
     * 
     * Requirements: 9.1, 9.5
     * 
     * @param planId 计划ID，如果为-1则计算所有计划的连续天数
     * @return 连续学习天数
     */
    public int calculateStreakDays(int planId) {
        List<String> completedDates;
        
        if (planId == -1) {
            // 获取所有计划的完成日期
            completedDates = dailyTaskDao.getAllCompletedDates();
        } else {
            // 获取指定计划的完成日期
            completedDates = dailyTaskDao.getCompletedDates(planId);
        }
        
        return calculateStreakFromDates(completedDates);
    }
    
    /**
     * 从日期列表计算连续学习天数
     * 纯函数，便于测试
     * 
     * @param completedDates 已完成任务的日期列表（降序排列）
     * @return 连续学习天数
     */
    public int calculateStreakFromDates(List<String> completedDates) {
        if (completedDates == null || completedDates.isEmpty()) {
            return 0;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        int streakDays = 0;
        Calendar expectedDate = (Calendar) today.clone();
        
        for (String dateStr : completedDates) {
            try {
                Date date = sdf.parse(dateStr);
                if (date == null) continue;
                
                Calendar completedDate = Calendar.getInstance();
                completedDate.setTime(date);
                completedDate.set(Calendar.HOUR_OF_DAY, 0);
                completedDate.set(Calendar.MINUTE, 0);
                completedDate.set(Calendar.SECOND, 0);
                completedDate.set(Calendar.MILLISECOND, 0);
                
                // 检查是否是期望的日期（今天或连续的前一天）
                if (isSameDay(completedDate, expectedDate)) {
                    streakDays++;
                    // 期望日期往前推一天
                    expectedDate.add(Calendar.DAY_OF_YEAR, -1);
                } else if (completedDate.before(expectedDate)) {
                    // 如果日期比期望日期更早，说明连续中断了
                    break;
                }
                // 如果日期比期望日期更晚，跳过（可能是重复数据）
                
            } catch (ParseException e) {
                // 日期解析失败，跳过
                continue;
            }
        }
        
        return streakDays;
    }

    
    /**
     * 计算累计学习时长（分钟）
     * 
     * Requirements: 9.2, 9.4
     * 
     * @param planId 计划ID
     * @return 累计学习时长（分钟）
     */
    public int calculateTotalStudyTime(int planId) {
        return dailyTaskDao.getTotalStudyMinutesByPlan(planId);
    }
    
    /**
     * 从任务列表计算累计学习时长
     * 纯函数，便于测试
     * 
     * @param tasks 任务列表
     * @return 累计学习时长（分钟）
     */
    public int calculateTotalStudyTimeFromTasks(List<DailyTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int totalMinutes = 0;
        for (DailyTaskEntity task : tasks) {
            if (task.isCompleted()) {
                totalMinutes += task.getActualMinutes();
            }
        }
        return totalMinutes;
    }
    
    /**
     * 计算本周学习时长（分钟）
     * 
     * Requirements: 9.3
     * 
     * @param planId 计划ID
     * @return 本周学习时长（分钟）
     */
    public int calculateWeeklyStudyTime(int planId) {
        String[] weekRange = getWeekDateRange();
        return dailyTaskDao.getTotalStudyMinutesInRange(planId, weekRange[0], weekRange[1]);
    }
    
    /**
     * 从任务列表计算本周学习时长
     * 纯函数，便于测试
     * 
     * @param tasks 任务列表
     * @param weekStartDate 本周开始日期（yyyy-MM-dd）
     * @param weekEndDate 本周结束日期（yyyy-MM-dd）
     * @return 本周学习时长（分钟）
     */
    public int calculateWeeklyStudyTimeFromTasks(List<DailyTaskEntity> tasks, 
                                                  String weekStartDate, 
                                                  String weekEndDate) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int totalMinutes = 0;
        for (DailyTaskEntity task : tasks) {
            if (task.isCompleted() && isDateInRange(task.getDate(), weekStartDate, weekEndDate)) {
                totalMinutes += task.getActualMinutes();
            }
        }
        return totalMinutes;
    }
    
    /**
     * 获取已完成任务数量
     * 
     * Requirements: 9.3
     * 
     * @param planId 计划ID
     * @return 已完成任务数量
     */
    public int getCompletedTasksCount(int planId) {
        return dailyTaskDao.getCompletedTaskCountByPlan(planId);
    }
    
    /**
     * 从任务列表获取已完成任务数量
     * 纯函数，便于测试
     * 
     * @param tasks 任务列表
     * @return 已完成任务数量
     */
    public int getCompletedTasksCountFromList(List<DailyTaskEntity> tasks) {
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
     * 获取任务总数
     * 
     * @param planId 计划ID
     * @return 任务总数
     */
    public int getTotalTasksCount(int planId) {
        return dailyTaskDao.getTotalTaskCountByPlan(planId);
    }
    
    /**
     * 获取今日已完成任务数量
     * 
     * @param planId 计划ID
     * @return 今日已完成任务数量
     */
    public int getTodayCompletedTasksCount(int planId) {
        String today = getTodayDateString();
        return dailyTaskDao.getCompletedTaskCount(planId, today);
    }
    
    /**
     * 获取今日任务总数
     * 
     * @param planId 计划ID
     * @return 今日任务总数
     */
    public int getTodayTotalTasksCount(int planId) {
        String today = getTodayDateString();
        return dailyTaskDao.getTotalTaskCount(planId, today);
    }
    
    /**
     * 获取今日学习时长（分钟）
     * 
     * @param planId 计划ID
     * @return 今日学习时长（分钟）
     */
    public int getTodayStudyTime(int planId) {
        String today = getTodayDateString();
        return dailyTaskDao.getTotalStudyMinutesForDate(planId, today);
    }
    
    /**
     * 计算任务完成率（百分比）
     * 
     * @param planId 计划ID
     * @return 完成率（0-100）
     */
    public int calculateCompletionRate(int planId) {
        int total = getTotalTasksCount(planId);
        if (total == 0) {
            return 0;
        }
        int completed = getCompletedTasksCount(planId);
        return (completed * 100) / total;
    }

    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取今天的日期字符串
     * 
     * @return 今天的日期（yyyy-MM-dd）
     */
    public String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }
    
    /**
     * 获取本周的日期范围
     * 
     * @return 包含两个元素的数组：[开始日期, 结束日期]
     */
    public String[] getWeekDateRange() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        // 设置为本周第一天（周一）
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String startDate = sdf.format(calendar.getTime());
        
        // 设置为本周最后一天（周日）
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String endDate = sdf.format(calendar.getTime());
        
        return new String[]{startDate, endDate};
    }
    
    /**
     * 检查两个Calendar是否是同一天
     * 
     * @param cal1 第一个Calendar
     * @param cal2 第二个Calendar
     * @return 如果是同一天返回true
     */
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * 检查日期是否在指定范围内
     * 
     * @param dateStr 要检查的日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 如果在范围内返回true
     */
    private boolean isDateInRange(String dateStr, String startDate, String endDate) {
        if (dateStr == null || startDate == null || endDate == null) {
            return false;
        }
        return dateStr.compareTo(startDate) >= 0 && dateStr.compareTo(endDate) <= 0;
    }
    
    /**
     * 格式化学习时长为可读字符串
     * 
     * @param minutes 分钟数
     * @return 格式化的字符串（如：1小时30分钟）
     */
    public static String formatStudyTime(int minutes) {
        if (minutes < 60) {
            return minutes + "分钟";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (remainingMinutes == 0) {
            return hours + "小时";
        }
        return hours + "小时" + remainingMinutes + "分钟";
    }
    
    /**
     * 格式化学习时长为简短字符串
     * 
     * @param minutes 分钟数
     * @return 格式化的字符串（如：1.5h）
     */
    public static String formatStudyTimeShort(int minutes) {
        if (minutes < 60) {
            return minutes + "min";
        }
        double hours = minutes / 60.0;
        return String.format(Locale.getDefault(), "%.1fh", hours);
    }
    
    // ==================== 统计数据类 ====================
    
    /**
     * 学习统计数据封装类
     */
    public static class StudyStats {
        public final int streakDays;
        public final int totalStudyMinutes;
        public final int weeklyStudyMinutes;
        public final int completedTasks;
        public final int totalTasks;
        public final int completionRate;
        
        public StudyStats(int streakDays, int totalStudyMinutes, int weeklyStudyMinutes,
                         int completedTasks, int totalTasks, int completionRate) {
            this.streakDays = streakDays;
            this.totalStudyMinutes = totalStudyMinutes;
            this.weeklyStudyMinutes = weeklyStudyMinutes;
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
            this.completionRate = completionRate;
        }
        
        /**
         * 获取格式化的总学习时长
         */
        public String getFormattedTotalTime() {
            return formatStudyTime(totalStudyMinutes);
        }
        
        /**
         * 获取格式化的本周学习时长
         */
        public String getFormattedWeeklyTime() {
            return formatStudyTime(weeklyStudyMinutes);
        }
    }
    
    /**
     * 获取计划的完整统计数据
     * 
     * @param planId 计划ID
     * @return 统计数据对象
     */
    public StudyStats getStudyStats(int planId) {
        int streakDays = calculateStreakDays(planId);
        int totalStudyMinutes = calculateTotalStudyTime(planId);
        int weeklyStudyMinutes = calculateWeeklyStudyTime(planId);
        int completedTasks = getCompletedTasksCount(planId);
        int totalTasks = getTotalTasksCount(planId);
        int completionRate = calculateCompletionRate(planId);
        
        return new StudyStats(streakDays, totalStudyMinutes, weeklyStudyMinutes,
                             completedTasks, totalTasks, completionRate);
    }
}
