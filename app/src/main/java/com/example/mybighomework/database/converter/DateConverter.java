package com.example.mybighomework.database.converter;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Date类型转换器
 * 用于在Room数据库中存储和读取Date类型数据
 */
public class DateConverter {
    
    /**
     * 将Date转换为Long（时间戳）
     * @param date 要转换的Date对象
     * @return 时间戳，如果date为null则返回null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * 将Long（时间戳）转换为Date
     * @param timestamp 时间戳
     * @return Date对象，如果timestamp为null则返回null
     */
    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}