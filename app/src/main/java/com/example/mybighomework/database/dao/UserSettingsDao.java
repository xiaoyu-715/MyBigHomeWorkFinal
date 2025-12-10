package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.UserSettingsEntity;

@Dao
public interface UserSettingsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSettingsEntity userSettings);
    
    @Update
    void update(UserSettingsEntity userSettings);
    
    @Query("SELECT * FROM user_settings WHERE id = 1")
    UserSettingsEntity getUserSettings();
    
    @Query("UPDATE user_settings SET userName = :userName WHERE id = 1")
    void updateUserName(String userName);
    
    @Query("UPDATE user_settings SET userAvatar = :avatarPath WHERE id = 1")
    void updateUserAvatar(String avatarPath);
    
    @Query("UPDATE user_settings SET dailyStudyGoal = :goal WHERE id = 1")
    void updateDailyStudyGoal(int goal);
    
    @Query("UPDATE user_settings SET notificationEnabled = :enabled WHERE id = 1")
    void updateNotificationEnabled(boolean enabled);
    
    @Query("UPDATE user_settings SET notificationTime = :time WHERE id = 1")
    void updateNotificationTime(String time);
    
    @Query("UPDATE user_settings SET preferredExamType = :examType WHERE id = 1")
    void updatePreferredExamType(String examType);
    
    @Query("UPDATE user_settings SET themeMode = :theme WHERE id = 1")
    void updateThemeMode(String theme);
    
    @Query("UPDATE user_settings SET soundEnabled = :enabled WHERE id = 1")
    void updateSoundEnabled(boolean enabled);
    
    @Query("UPDATE user_settings SET studyStreak = :streak, lastStudyDate = :date WHERE id = 1")
    void updateStudyStreak(int streak, long date);
    
    @Query("UPDATE user_settings SET totalStudyTime = totalStudyTime + :durationMillis WHERE id = 1")
    void addStudyTime(long durationMillis);
    
    @Query("UPDATE user_settings SET totalStudyTime = :totalTime WHERE id = 1")
    void setTotalStudyTime(long totalTime);
    
    @Query("SELECT dailyStudyGoal FROM user_settings WHERE id = 1")
    int getDailyStudyGoal();
    
    @Query("SELECT studyStreak FROM user_settings WHERE id = 1")
    int getStudyStreak();
    
    @Query("SELECT lastStudyDate FROM user_settings WHERE id = 1")
    long getLastStudyDate();
    
    @Query("SELECT totalStudyTime FROM user_settings WHERE id = 1")
    long getTotalStudyTime();
    
    @Query("SELECT notificationEnabled FROM user_settings WHERE id = 1")
    boolean isNotificationEnabled();
    
    @Query("SELECT soundEnabled FROM user_settings WHERE id = 1")
    boolean isSoundEnabled();
    
    @Query("SELECT preferredExamType FROM user_settings WHERE id = 1")
    String getPreferredExamType();
    
    @Query("SELECT themeMode FROM user_settings WHERE id = 1")
    String getThemeMode();
    
    @Query("UPDATE user_settings SET vocabularyCorrectCount = vocabularyCorrectCount + 1 WHERE id = 1")
    void incrementVocabularyCorrectCount();
    
    @Query("SELECT vocabularyCorrectCount FROM user_settings WHERE id = 1")
    int getVocabularyCorrectCount();
}