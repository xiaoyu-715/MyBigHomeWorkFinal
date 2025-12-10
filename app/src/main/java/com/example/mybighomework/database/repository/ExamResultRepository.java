package com.example.mybighomework.database.repository;

import android.content.Context;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.ExamResultDao;
import com.example.mybighomework.database.entity.ExamResultEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 考试成绩数据仓库
 * 提供考试成绩的CRUD操作
 */
public class ExamResultRepository {
    
    private final ExamResultDao examResultDao;
    private final ExecutorService executorService;
    
    public ExamResultRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        examResultDao = database.examResultDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 添加考试成绩（异步）
     */
    public void addResult(ExamResultEntity result, ResultCallback callback) {
        executorService.execute(() -> {
            try {
                long id = examResultDao.insert(result);
                result.setId((int) id);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 添加考试成绩（同步）
     */
    public long addResultSync(ExamResultEntity result) {
        return examResultDao.insert(result);
    }
    
    /**
     * 更新考试成绩
     */
    public void updateResult(ExamResultEntity result) {
        executorService.execute(() -> examResultDao.update(result));
    }
    
    /**
     * 删除考试成绩
     */
    public void deleteResult(ExamResultEntity result) {
        executorService.execute(() -> examResultDao.delete(result));
    }
    
    /**
     * 根据ID查询成绩
     */
    public void getResultById(int id, ResultCallback callback) {
        executorService.execute(() -> {
            try {
                ExamResultEntity result = examResultDao.getById(id);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 查询所有成绩
     */
    public void getAllResults(ResultsCallback callback) {
        executorService.execute(() -> {
            try {
                List<ExamResultEntity> results = examResultDao.getAllResults();
                if (callback != null) {
                    callback.onSuccess(results);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 查询最近N条成绩
     */
    public void getRecentResults(int limit, ResultsCallback callback) {
        executorService.execute(() -> {
            try {
                List<ExamResultEntity> results = examResultDao.getRecentResults(limit);
                if (callback != null) {
                    callback.onSuccess(results);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取最高分记录
     */
    public void getHighestScore(ResultCallback callback) {
        executorService.execute(() -> {
            try {
                ExamResultEntity result = examResultDao.getHighestScore();
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取平均分
     */
    public void getAverageScore(ScoreCallback callback) {
        executorService.execute(() -> {
            try {
                float avgScore = examResultDao.getAverageScore();
                if (callback != null) {
                    callback.onSuccess(avgScore);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 单个成绩结果回调接口
     */
    public interface ResultCallback {
        void onSuccess(ExamResultEntity result);
        void onError(String error);
    }
    
    /**
     * 多个成绩结果回调接口
     */
    public interface ResultsCallback {
        void onSuccess(List<ExamResultEntity> results);
        void onError(String error);
    }
    
    /**
     * 分数回调接口
     */
    public interface ScoreCallback {
        void onSuccess(float score);
        void onError(String error);
    }
}

