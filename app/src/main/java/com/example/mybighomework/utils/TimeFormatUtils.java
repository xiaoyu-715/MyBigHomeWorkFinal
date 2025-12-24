package com.example.mybighomework.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间格式化工具类
 * 用于将时间戳转换为人类可读的格式
 * 
 * 支持的格式：
 * - 今天的时间显示为 "今天 HH:mm"
 * - 昨天的时间显示为 "昨天 HH:mm"
 * - 更早的时间显示为 "MM-dd HH:mm"
 * - 跨年的时间显示为 "yyyy-MM-dd HH:mm"
 */
public class TimeFormatUtils {
    
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat FULL_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    /**
     * 格式化时间戳为人类可读的字符串
     * 
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的时间字符串
     */
    public static String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);
        
        // 检查是否是今天
        if (isSameDay(now, target)) {
            return "今天 " + TIME_FORMAT.format(new Date(timestamp));
        }
        
        // 检查是否是昨天
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(yesterday, target)) {
            return "昨天 " + TIME_FORMAT.format(new Date(timestamp));
        }
        
        // 检查是否是同一年
        if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
            return DATE_TIME_FORMAT.format(new Date(timestamp));
        }
        
        // 不同年份，显示完整日期
        return FULL_DATE_TIME_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * 格式化时间戳，使用自定义的当前时间（用于测试）
     * 
     * @param timestamp 时间戳（毫秒）
     * @param currentTime 当前时间（毫秒）
     * @return 格式化后的时间字符串
     */
    public static String formatTimestamp(long timestamp, long currentTime) {
        if (timestamp <= 0) {
            return "";
        }
        
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);
        
        // 检查是否是今天
        if (isSameDay(now, target)) {
            return "今天 " + TIME_FORMAT.format(new Date(timestamp));
        }
        
        // 检查是否是昨天
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(currentTime);
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(yesterday, target)) {
            return "昨天 " + TIME_FORMAT.format(new Date(timestamp));
        }
        
        // 检查是否是同一年
        if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
            return DATE_TIME_FORMAT.format(new Date(timestamp));
        }
        
        // 不同年份，显示完整日期
        return FULL_DATE_TIME_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * 判断两个Calendar是否是同一天
     * 
     * @param cal1 第一个Calendar
     * @param cal2 第二个Calendar
     * @return 是否是同一天
     */
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * 获取相对时间描述（如：刚刚、5分钟前、1小时前等）
     * 
     * @param timestamp 时间戳（毫秒）
     * @return 相对时间描述
     */
    public static String getRelativeTime(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < 0) {
            return formatTimestamp(timestamp);
        }
        
        // 1分钟内
        if (diff < 60 * 1000) {
            return "刚刚";
        }
        
        // 1小时内
        if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + "分钟前";
        }
        
        // 24小时内
        if (diff < 24 * 60 * 60 * 1000) {
            long hours = diff / (60 * 60 * 1000);
            return hours + "小时前";
        }
        
        // 超过24小时，使用标准格式
        return formatTimestamp(timestamp);
    }
}
