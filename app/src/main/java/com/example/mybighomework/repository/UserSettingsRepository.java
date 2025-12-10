package com.example.mybighomework.repository;

import android.content.Context;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.UserSettingsDao;
import com.example.mybighomework.database.entity.UserSettingsEntity;

public class UserSettingsRepository {
    private UserSettingsDao userSettingsDao;
    
    public UserSettingsRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userSettingsDao = database.userSettingsDao();
    }
    
    // 确保用户设置表存在一条默认记录（id=1）
    private void ensureSettingsRowExists() {
        UserSettingsEntity settings = userSettingsDao.getUserSettings();
        if (settings == null) {
            userSettingsDao.insert(new UserSettingsEntity());
        }
    }
    
    // 获取用户设置
    public UserSettingsEntity getUserSettings() {
        UserSettingsEntity settings = userSettingsDao.getUserSettings();
        if (settings == null) {
            settings = new UserSettingsEntity();
            userSettingsDao.insert(settings);
        }
        return settings;
    }
    
    // 更新用户设置
    public void updateUserSettings(UserSettingsEntity settings) {
        userSettingsDao.update(settings);
    }
    
    // 更新用户名
    public void updateUserName(String userName) {
        userSettingsDao.updateUserName(userName);
    }
    
    // 更新头像
    public void updateUserAvatar(String avatarPath) {
        userSettingsDao.updateUserAvatar(avatarPath);
    }
    
    // 更新每日学习目标
    public void updateDailyStudyGoal(int goal) {
        userSettingsDao.updateDailyStudyGoal(goal);
    }
    
    // 更新通知设置
    public void updateNotificationEnabled(boolean enabled) {
        userSettingsDao.updateNotificationEnabled(enabled);
    }
    
    public void updateNotificationTime(String time) {
        userSettingsDao.updateNotificationTime(time);
    }
    
    // 更新偏好考试类型
    public void updatePreferredExamType(String examType) {
        userSettingsDao.updatePreferredExamType(examType);
    }
    
    // 更新主题模式
    public void updateThemeMode(String theme) {
        userSettingsDao.updateThemeMode(theme);
    }
    
    // 更新音效设置
    public void updateSoundEnabled(boolean enabled) {
        userSettingsDao.updateSoundEnabled(enabled);
    }
    
    // 更新学习连续天数
    public void updateStudyStreak() {
        UserSettingsEntity settings = getUserSettings();
        settings.updateStudyStreak();
        updateUserSettings(settings);
    }
    
    /**
     * 记录学习时长（统一接口）
     * @param durationMillis 学习时长（毫秒）
     * @param activityType 活动类型：vocabulary, mock_exam, real_exam
     */
    public void recordStudyTime(long durationMillis, String activityType) {
        // 确保存在默认设置行，否则UPDATE将不会生效
        ensureSettingsRowExists();
        userSettingsDao.addStudyTime(durationMillis);
        // 同时更新学习连续天数
        updateStudyStreak();
    }
    
    /**
     * 增加学习时长
     * @param durationMillis 学习时长（毫秒）
     */
    public void addStudyTime(long durationMillis) {
        ensureSettingsRowExists();
        userSettingsDao.addStudyTime(durationMillis);
    }
    
    /**
     * 设置总学习时长
     * @param totalTime 总时长（毫秒）
     */
    public void setTotalStudyTime(long totalTime) {
        ensureSettingsRowExists();
        userSettingsDao.setTotalStudyTime(totalTime);
    }
    
    /**
     * 获取总学习时长（毫秒）
     */
    public long getTotalStudyTime() {
        // 确保存在默认设置行，避免查询为空导致异常
        ensureSettingsRowExists();
        long time = userSettingsDao.getTotalStudyTime();
        return time;
    }
    
    /**
     * 获取总学习时长（小时）
     */
    public double getTotalStudyTimeHours() {
        return getTotalStudyTime() / 3600000.0;
    }
    
    /**
     * 获取总学习时长（分钟）
     */
    public long getTotalStudyTimeMinutes() {
        return getTotalStudyTime() / 60000;
    }
    
    // 获取特定设置值
    public int getDailyStudyGoal() {
        return userSettingsDao.getDailyStudyGoal();
    }
    
    public int getStudyStreak() {
        return userSettingsDao.getStudyStreak();
    }
    
    public boolean isNotificationEnabled() {
        return userSettingsDao.isNotificationEnabled();
    }
    
    public boolean isSoundEnabled() {
        return userSettingsDao.isSoundEnabled();
    }
    
    public String getPreferredExamType() {
        return userSettingsDao.getPreferredExamType();
    }
    
    public String getThemeMode() {
        return userSettingsDao.getThemeMode();
    }
    
    /**
     * 增加词汇训练答对次数
     */
    public void incrementVocabularyCorrectCount() {
        ensureSettingsRowExists();
        userSettingsDao.incrementVocabularyCorrectCount();
    }
    
    /**
     * 获取词汇训练答对次数
     */
    public int getVocabularyCorrectCount() {
        ensureSettingsRowExists();
        return userSettingsDao.getVocabularyCorrectCount();
    }
}