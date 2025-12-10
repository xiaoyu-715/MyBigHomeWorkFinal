package com.example.mybighomework.repository;

import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.dao.ExamDao;
import com.example.mybighomework.database.entity.ExamRecordEntity;

import java.util.List;

public class ExamRecordRepository {
    private ExamDao examDao;
    
    public ExamRecordRepository(ExamDao examDao) {
        this.examDao = examDao;
    }
    
    // 获取平均分
    public double getAverageScore() {
        return examDao.getAverageScore();
    }
    
    // 根据考试类型获取平均分
    public double getAverageScoreByType(String examType) {
        return examDao.getAverageScoreByType(examType);
    }
    
    // 获取总考试次数
    public int getTotalExamCount() {
        return examDao.getTotalExamCount();
    }
    
    // 获取模拟考试次数
    public int getMockExamCount() {
        return examDao.getMockExamCount();
    }
    
    // 获取所有考试记录
    public List<ExamRecordEntity> getAllExamRecords() {
        return examDao.getAllExamRecords();
    }
    
    // 根据考试类型获取考试记录
    public List<ExamRecordEntity> getExamRecordsByType(String examType) {
        return examDao.getExamRecordsByType(examType);
    }
    
    // 根据考试模式获取考试记录
    public List<ExamRecordEntity> getExamRecordsByMode(String examMode) {
        return examDao.getExamRecordsByMode(examMode);
    }
    
    // 添加考试记录
    public long addExamRecord(ExamRecordEntity examRecord) {
        return examDao.insert(examRecord);
    }
    
    // 更新考试记录
    public void updateExamRecord(ExamRecordEntity examRecord) {
        examDao.update(examRecord);
    }
    
    // 删除考试记录
    public void deleteExamRecord(ExamRecordEntity examRecord) {
        examDao.delete(examRecord);
    }
    
    // 根据ID获取考试记录
    public ExamRecordEntity getExamRecordById(int id) {
        return examDao.getExamRecordById(id);
    }
    
    // 根据时间范围获取考试记录
    public List<ExamRecordEntity> getExamRecordsByTimeRange(long startTime, long endTime) {
        return examDao.getExamRecordsByTimeRange(startTime, endTime);
    }
    
    // 根据考试类型获取最高分
    public int getHighestScoreByType(String examType) {
        return examDao.getHighestScoreByType(examType);
    }
    
    // 根据考试类型获取平均准确率
    public double getAverageAccuracyByType(String examType) {
        return examDao.getAverageAccuracyByType(examType);
    }
    
    // 根据考试类型获取最佳考试记录
    public ExamRecordEntity getBestExamByType(String examType) {
        return examDao.getBestExamByType(examType);
    }
    
    // 根据考试类型获取最新考试记录
    public ExamRecordEntity getLatestExamByType(String examType) {
        return examDao.getLatestExamByType(examType);
    }
    
    // 获取所有考试类型
    public List<String> getAllExamTypes() {
        return examDao.getAllExamTypes();
    }
    
    // 根据最低分数获取考试记录
    public List<ExamRecordEntity> getExamRecordsByMinScore(int minScore) {
        return examDao.getExamRecordsByMinScore(minScore);
    }
    
    // ==================== LiveData 方法 ====================
    
    // 获取所有考试记录（LiveData）
    public LiveData<List<ExamRecordEntity>> getAllExamRecordsLive() {
        return examDao.getAllExamRecordsLive();
    }
    
    // 获取最近的考试记录（LiveData）
    public LiveData<List<ExamRecordEntity>> getRecentExamRecordsLive(int limit) {
        return examDao.getRecentExamRecordsLive(limit);
    }
    
    // 获取考试总数（LiveData）
    public LiveData<Integer> getTotalExamCountLive() {
        return examDao.getTotalExamCountLive();
    }
    
    // 获取平均分（LiveData）
    public LiveData<Double> getAverageScoreLive() {
        return examDao.getAverageScoreLive();
    }
}