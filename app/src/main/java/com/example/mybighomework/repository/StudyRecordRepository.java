package com.example.mybighomework.repository;

import com.example.mybighomework.database.dao.StudyRecordDao;
import com.example.mybighomework.database.entity.StudyRecordEntity;

import java.util.List;

public class StudyRecordRepository {
    private StudyRecordDao studyRecordDao;
    
    public StudyRecordRepository(StudyRecordDao studyRecordDao) {
        this.studyRecordDao = studyRecordDao;
    }
    
    // 获取总学习时长（小时）
    public double getTotalStudyTimeHours() {
        return studyRecordDao.getTotalStudyTimeHours();
    }

    // 根据学习类型获取正确答案数
    public int getCorrectAnswerCountByType(String studyType) {
        return studyRecordDao.getCorrectAnswerCountByType(studyType);
    }

    // 获取所有学习过的日期
    public List<String> getDistinctStudyDays() {
        return studyRecordDao.getDistinctStudyDays();
    }
    
    // 获取所有学习记录
    public List<StudyRecordEntity> getAllStudyRecords() {
        return studyRecordDao.getAllStudyRecords();
    }
    
    // 根据题目ID获取学习记录
    public List<StudyRecordEntity> getStudyRecordsByQuestionId(long questionId) {
        return studyRecordDao.getStudyRecordsByQuestionId(questionId);
    }
    
    // 根据词汇ID获取学习记录
    public List<StudyRecordEntity> getStudyRecordsByVocabularyId(long vocabularyId) {
        return studyRecordDao.getStudyRecordsByVocabularyId(vocabularyId);
    }
    
    // 根据学习类型获取学习记录
    public List<StudyRecordEntity> getStudyRecordsByType(String studyType) {
        return studyRecordDao.getStudyRecordsByType(studyType);
    }
    
    // 获取总学习记录数
    public int getTotalStudyRecordCount() {
        return studyRecordDao.getTotalStudyRecordCount();
    }
    
    // 获取正确答案数
    public int getCorrectAnswerCount() {
        return studyRecordDao.getCorrectAnswerCount();
    }
    
    // 获取每日学习时长数据（最近N天）
    public List<com.example.mybighomework.database.dao.StudyRecordDao.DailyStudyTime> getDailyStudyTime(int days) {
        long startTime = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);
        return studyRecordDao.getDailyStudyTime(startTime);
    }
    
    // 获取错误答案数
    public int getWrongAnswerCount() {
        return studyRecordDao.getWrongAnswerCount();
    }
    
    // 添加学习记录
    public long addStudyRecord(StudyRecordEntity studyRecord) {
        return studyRecordDao.insertStudyRecord(studyRecord);
    }
    
    // 更新学习记录
    public void updateStudyRecord(StudyRecordEntity studyRecord) {
        studyRecordDao.updateStudyRecord(studyRecord);
    }
    
    // 删除学习记录
    public void deleteStudyRecord(StudyRecordEntity studyRecord) {
        studyRecordDao.deleteStudyRecord(studyRecord);
    }
    
    // 根据ID获取学习记录
    public StudyRecordEntity getStudyRecordById(long id) {
        return studyRecordDao.getStudyRecordById(id);
    }
    
    // 根据时间范围获取学习记录
    public List<StudyRecordEntity> getStudyRecordsByDateRange(long startTime, long endTime) {
        return studyRecordDao.getStudyRecordsByDateRange(startTime, endTime);
    }
    
    // 获取需要复习的记录
    public List<StudyRecordEntity> getRecordsNeedingReview() {
        return studyRecordDao.getRecordsNeedingReview();
    }
    
    // 获取错误答案记录
    public List<StudyRecordEntity> getWrongAnswerRecords() {
        return studyRecordDao.getWrongAnswerRecords();
    }
    
    // 获取正确答案记录
    public List<StudyRecordEntity> getCorrectAnswerRecords() {
        return studyRecordDao.getCorrectAnswerRecords();
    }
}