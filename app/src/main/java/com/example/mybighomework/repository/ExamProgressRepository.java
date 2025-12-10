package com.example.mybighomework.repository;

import com.example.mybighomework.database.dao.ExamProgressDao;
import com.example.mybighomework.database.entity.ExamProgressEntity;
import java.util.List;

/**
 * 考试进度Repository
 */
public class ExamProgressRepository {
    
    private final ExamProgressDao examProgressDao;
    
    public ExamProgressRepository(ExamProgressDao examProgressDao) {
        this.examProgressDao = examProgressDao;
    }
    
    /**
     * 保存考试进度
     */
    public void saveProgress(ExamProgressEntity progress) {
        new Thread(() -> {
            examProgressDao.insertProgress(progress);
        }).start();
    }
    
    /**
     * 同步保存考试进度(返回ID)
     */
    public long saveProgressSync(ExamProgressEntity progress) {
        return examProgressDao.insertProgress(progress);
    }
    
    /**
     * 更新考试进度
     */
    public void updateProgress(ExamProgressEntity progress) {
        new Thread(() -> {
            examProgressDao.updateProgress(progress);
        }).start();
    }
    
    /**
     * 获取未完成的考试进度
     */
    public void getUncompletedProgress(String examType, ProgressCallback callback) {
        new Thread(() -> {
            ExamProgressEntity progress = examProgressDao.getUncompletedProgress(examType);
            if (callback != null) {
                callback.onResult(progress);
            }
        }).start();
    }
    
    /**
     * 同步获取未完成的考试进度
     */
    public ExamProgressEntity getUncompletedProgressSync(String examType) {
        return examProgressDao.getUncompletedProgress(examType);
    }
    
    /**
     * 获取所有进度
     */
    public void getAllProgress(AllProgressCallback callback) {
        new Thread(() -> {
            List<ExamProgressEntity> progressList = examProgressDao.getAllProgress();
            if (callback != null) {
                callback.onResult(progressList);
            }
        }).start();
    }
    
    /**
     * 删除进度
     */
    public void deleteProgress(int id) {
        new Thread(() -> {
            examProgressDao.deleteProgress(id);
        }).start();
    }
    
    /**
     * 标记为已完成
     */
    public void markAsCompleted(int id) {
        new Thread(() -> {
            examProgressDao.markAsCompleted(id);
        }).start();
    }
    
    /**
     * 删除所有已完成的进度
     */
    public void deleteCompletedProgress() {
        new Thread(() -> {
            examProgressDao.deleteCompletedProgress();
        }).start();
    }
    
    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onResult(ExamProgressEntity progress);
    }
    
    /**
     * 所有进度回调接口
     */
    public interface AllProgressCallback {
        void onResult(List<ExamProgressEntity> progressList);
    }
}

