package com.example.mybighomework.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.QuestionDao;
import com.example.mybighomework.database.entity.QuestionEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 题目数据仓库
 * 负责管理题目数据的CRUD操作
 */
public class QuestionRepository {
    
    private final QuestionDao questionDao;
    private final ExecutorService executorService;
    
    public QuestionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        questionDao = database.questionDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    // ==================== 插入操作 ====================
    
    /**
     * 插入单个题目
     */
    public void insertQuestion(QuestionEntity question, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                long id = questionDao.insertQuestion(question);
                if (listener != null) {
                    listener.onSuccess("题目添加成功，ID: " + id);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("添加题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 批量插入题目
     */
    public void insertQuestions(List<QuestionEntity> questions, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                List<Long> ids = questionDao.insertQuestions(questions);
                if (listener != null) {
                    listener.onSuccess("成功添加 " + ids.size() + " 个题目");
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("批量添加题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    // ==================== 更新操作 ====================
    
    /**
     * 更新题目
     */
    public void updateQuestion(QuestionEntity question, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                questionDao.updateQuestion(question);
                if (listener != null) {
                    listener.onSuccess("题目更新成功");
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("更新题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 记录答题情况并更新统计
     */
    public void recordQuestionAttempt(int questionId, boolean isCorrect, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                QuestionEntity question = questionDao.getQuestionById(questionId);
                if (question != null) {
                    question.recordAttempt(isCorrect);
                    questionDao.updateQuestion(question);
                    if (listener != null) {
                        listener.onSuccess("答题记录已保存");
                    }
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("保存答题记录失败: " + e.getMessage());
                }
            }
        });
    }
    
    // ==================== 删除操作 ====================
    
    /**
     * 删除题目
     */
    public void deleteQuestion(QuestionEntity question, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                questionDao.deleteQuestion(question);
                if (listener != null) {
                    listener.onSuccess("题目删除成功");
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("删除题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 删除指定ID的题目
     */
    public void deleteQuestionById(int id, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                questionDao.deleteQuestionById(id);
                if (listener != null) {
                    listener.onSuccess("题目删除成功");
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("删除题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    // ==================== 查询操作 ====================
    
    /**
     * 获取所有激活的题目
     */
    public void getAllActiveQuestions(OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.getAllActiveQuestions();
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("加载题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据分类获取题目
     */
    public void getQuestionsByCategory(String category, OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.getQuestionsByCategory(category);
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("加载题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据考试类型获取题目
     */
    public void getQuestionsByExamType(String examType, OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.getQuestionsByExamType(examType);
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("加载题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取随机题目（按分类和考试类型）
     */
    public void getRandomQuestions(String category, String examType, int limit, 
                                   OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.getRandomQuestionsByCategoryAndType(
                    category, examType, limit);
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("加载题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取随机题目（仅按考试类型）
     */
    public void getRandomQuestionsByExamType(String examType, int limit, 
                                            OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.getRandomQuestionsByExamType(examType, limit);
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("加载题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据来源获取题目
     */
    public void getQuestionsBySource(String source, OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.getQuestionsBySource(source);
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("加载题目失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 搜索题目
     */
    public void searchQuestions(String keyword, OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.searchQuestions(keyword);
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("搜索失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取错题
     */
    public void getWrongQuestions(double threshold, int minAttempts, 
                                 OnDataLoadListener<List<QuestionEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<QuestionEntity> questions = questionDao.getWrongQuestions(threshold, minAttempts);
                if (listener != null) {
                    listener.onDataLoaded(questions);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("加载错题失败: " + e.getMessage());
                }
            }
        });
    }
    
    // ==================== 统计操作 ====================
    
    /**
     * 获取题目总数
     */
    public void getTotalQuestionCount(OnCountLoadListener listener) {
        executorService.execute(() -> {
            try {
                int count = questionDao.getTotalQuestionCount();
                if (listener != null) {
                    listener.onCountLoaded(count);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("获取统计失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据分类获取题目数量
     */
    public void getQuestionCountByCategory(String category, OnCountLoadListener listener) {
        executorService.execute(() -> {
            try {
                int count = questionDao.getQuestionCountByCategory(category);
                if (listener != null) {
                    listener.onCountLoaded(count);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError("获取统计失败: " + e.getMessage());
                }
            }
        });
    }
    
    // ==================== 回调接口 ====================
    
    public interface OnOperationCompleteListener {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface OnDataLoadListener<T> {
        void onDataLoaded(T data);
        void onError(String error);
    }
    
    public interface OnCountLoadListener {
        void onCountLoaded(int count);
        void onError(String error);
    }
}

