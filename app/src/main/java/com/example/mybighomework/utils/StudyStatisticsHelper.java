package com.example.mybighomework.utils;

import com.example.mybighomework.database.entity.UserSettingsEntity;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;

/**
 * 学习统计数据计算辅助类
 * 提供各种学习数据的计算和汇总方法
 */
public class StudyStatisticsHelper {
    
    private UserSettingsRepository userSettingsRepository;
    private ExamRecordRepository examRecordRepository;
    private VocabularyRecordRepository vocabularyRecordRepository;
    
    public StudyStatisticsHelper(UserSettingsRepository userSettingsRepository,
                                ExamRecordRepository examRecordRepository,
                                VocabularyRecordRepository vocabularyRecordRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.examRecordRepository = examRecordRepository;
        this.vocabularyRecordRepository = vocabularyRecordRepository;
    }
    
    /**
     * 获取学习连续天数
     */
    public int getStudyStreak() {
        try {
            UserSettingsEntity userSettings = userSettingsRepository.getUserSettings();
            return userSettings != null ? userSettings.getStudyStreak() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取掌握的词汇数量
     */
    public int getMasteredVocabularyCount() {
        try {
            return vocabularyRecordRepository.getMasteredVocabularyCount();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取总词汇数量
     */
    public int getTotalVocabularyCount() {
        try {
            return vocabularyRecordRepository.getTotalVocabularyCount();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取平均考试分数
     */
    public double getAverageExamScore() {
        try {
            return examRecordRepository.getAverageScore();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * 获取总考试次数
     */
    public int getTotalExamCount() {
        try {
            return examRecordRepository.getTotalExamCount();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 计算估算的总学习时长（小时）
     * 基于掌握的词汇数量，每个词汇约5分钟
     */
    public double getEstimatedStudyHours() {
        int masteredCount = getMasteredVocabularyCount();
        return masteredCount * 0.05; // 每个词汇约5分钟 = 0.05小时
    }
    
    /**
     * 获取词汇掌握率（百分比）
     */
    public double getVocabularyMasteryRate() {
        int totalCount = getTotalVocabularyCount();
        if (totalCount == 0) return 0.0;
        
        int masteredCount = getMasteredVocabularyCount();
        return (double) masteredCount / totalCount * 100;
    }
    
    /**
     * 根据学习连续天数确定用户等级
     */
    public String getUserLevel() {
        int studyStreak = getStudyStreak();
        if (studyStreak >= 100) {
            return "学习大师";
        } else if (studyStreak >= 50) {
            return "高级学习者";
        } else if (studyStreak >= 20) {
            return "中级学习者";
        } else if (studyStreak >= 7) {
            return "初级学习者";
        } else {
            return "新手学习者";
        }
    }
    
    /**
     * 获取学习进度描述
     */
    public String getStudyProgressDescription() {
        int masteredCount = getMasteredVocabularyCount();
        int totalCount = getTotalVocabularyCount();
        double masteryRate = getVocabularyMasteryRate();
        
        if (masteryRate >= 80) {
            return "学习进度优秀！";
        } else if (masteryRate >= 60) {
            return "学习进度良好";
        } else if (masteryRate >= 40) {
            return "学习进度一般";
        } else if (masteryRate >= 20) {
            return "需要加强学习";
        } else {
            return "刚开始学习";
        }
    }
    
    /**
     * 获取考试表现描述
     */
    public String getExamPerformanceDescription() {
        double averageScore = getAverageExamScore();
        
        if (averageScore >= 90) {
            return "考试表现优秀！";
        } else if (averageScore >= 80) {
            return "考试表现良好";
        } else if (averageScore >= 70) {
            return "考试表现一般";
        } else if (averageScore >= 60) {
            return "需要提高";
        } else {
            return "需要加强练习";
        }
    }
    
    /**
     * 获取用户名（如果没有设置则返回默认值）
     */
    public String getUsername() {
        try {
            UserSettingsEntity userSettings = userSettingsRepository.getUserSettings();
            if (userSettings != null && userSettings.getUserName() != null && !userSettings.getUserName().isEmpty()) {
                return userSettings.getUserName();
            }
            return "学习者";
        } catch (Exception e) {
            e.printStackTrace();
            return "学习者";
        }
    }
    
    /**
     * 获取用户加入日期
     */
    public String getJoinDate() {
        try {
            UserSettingsEntity userSettings = userSettingsRepository.getUserSettings();
            if (userSettings != null && userSettings.getRegistrationDate() > 0) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                return sdf.format(new java.util.Date(userSettings.getRegistrationDate()));
            }
            return "2024-01-01";
        } catch (Exception e) {
            e.printStackTrace();
            return "2024-01-01";
        }
    }
}