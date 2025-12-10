package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_settings")
public class UserSettingsEntity {
    @PrimaryKey
    private int id = 1; // 单例设置，只有一条记录
    
    private String userName;
    private String userAvatar; // 头像路径
    private int dailyStudyGoal; // 每日学习目标（分钟）
    private boolean notificationEnabled; // 是否开启通知
    private String notificationTime; // 通知时间
    private String preferredExamType; // 偏好的考试类型
    private String themeMode; // 主题模式：light, dark, auto
    private boolean soundEnabled; // 是否开启音效
    private int studyStreak; // 连续学习天数
    private long lastStudyDate; // 最后学习日期
    private long registrationDate; // 注册日期
    private long totalStudyTime; // 总学习时长（毫秒）- 统一存储所有学习活动的累计时间
    private int vocabularyCorrectCount; // 词汇训练答对次数

    // 构造函数
    public UserSettingsEntity() {
        this.registrationDate = System.currentTimeMillis();
        this.lastStudyDate = System.currentTimeMillis();
        this.dailyStudyGoal = 30; // 默认30分钟
        this.notificationEnabled = true;
        this.notificationTime = "20:00";
        this.preferredExamType = "四级";
        this.themeMode = "auto";
        this.soundEnabled = true;
        this.studyStreak = 0;
        this.userName = "学习者";
        this.totalStudyTime = 0;
        this.vocabularyCorrectCount = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public int getDailyStudyGoal() { return dailyStudyGoal; }
    public void setDailyStudyGoal(int dailyStudyGoal) { this.dailyStudyGoal = dailyStudyGoal; }

    public boolean isNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }

    public String getNotificationTime() { return notificationTime; }
    public void setNotificationTime(String notificationTime) { this.notificationTime = notificationTime; }

    public String getPreferredExamType() { return preferredExamType; }
    public void setPreferredExamType(String preferredExamType) { this.preferredExamType = preferredExamType; }

    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }

    public int getStudyStreak() { return studyStreak; }
    public void setStudyStreak(int studyStreak) { this.studyStreak = studyStreak; }

    public long getLastStudyDate() { return lastStudyDate; }
    public void setLastStudyDate(long lastStudyDate) { this.lastStudyDate = lastStudyDate; }

    public long getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(long registrationDate) { this.registrationDate = registrationDate; }

    public long getTotalStudyTime() { return totalStudyTime; }
    public void setTotalStudyTime(long totalStudyTime) { this.totalStudyTime = totalStudyTime; }

    // 更新学习连续天数
    public void updateStudyStreak() {
        long today = System.currentTimeMillis();
        
        // 将时间戳转换为日期（yyyy-MM-dd格式），去除时分秒
        String todayDate = getDateString(today);
        String lastDate = getDateString(lastStudyDate);
        
        // 如果是同一天，不增加连续天数
        if (todayDate.equals(lastDate)) {
            // 同一天内多次学习，不重复计数
            return;
        }
        
        // 计算日期差（天数）
        long daysDiff = getDaysDifference(lastStudyDate, today);
        
        if (daysDiff == 1) {
            // 连续的下一天，天数+1
            studyStreak++;
        } else if (daysDiff > 1) {
            // 中断了，重新开始计算
            studyStreak = 1;
        }
        // daysDiff == 0 的情况已经在上面处理了（同一天）
        
        lastStudyDate = today;
    }
    
    /**
     * 将时间戳转换为日期字符串（yyyy-MM-dd）
     */
    private String getDateString(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
    
    /**
     * 计算两个时间戳之间相差的天数
     */
    private long getDaysDifference(long fromTime, long toTime) {
        String fromDate = getDateString(fromTime);
        String toDate = getDateString(toTime);
        
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date1 = sdf.parse(fromDate);
            java.util.Date date2 = sdf.parse(toDate);
            
            long diff = date2.getTime() - date1.getTime();
            return diff / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // 增加学习时长（毫秒）
    public void addStudyTime(long durationMillis) {
        this.totalStudyTime += durationMillis;
    }
    
    // 获取总学习时长（小时）
    public double getTotalStudyTimeHours() {
        return totalStudyTime / 3600000.0;
    }
    
    // 获取总学习时长（分钟）
    public long getTotalStudyTimeMinutes() {
        return totalStudyTime / 60000;
    }
    
    // 词汇训练答对次数
    public int getVocabularyCorrectCount() { return vocabularyCorrectCount; }
    public void setVocabularyCorrectCount(int vocabularyCorrectCount) { this.vocabularyCorrectCount = vocabularyCorrectCount; }
    
    // 增加词汇训练答对次数
    public void incrementVocabularyCorrectCount() {
        this.vocabularyCorrectCount++;
    }
}