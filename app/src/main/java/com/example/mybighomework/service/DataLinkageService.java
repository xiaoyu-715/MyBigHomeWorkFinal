package com.example.mybighomework.service;

import android.content.Context;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.QuestionDao;
import com.example.mybighomework.database.dao.StudyRecordDao;
import com.example.mybighomework.database.dao.VocabularyDao;
import com.example.mybighomework.database.entity.QuestionEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 数据联动服务类
 * 负责建立词汇、题目和学习记录之间的关联机制
 */
public class DataLinkageService {
    
    private final QuestionDao questionDao;
    private final StudyRecordDao studyRecordDao;
    private final VocabularyDao vocabularyDao;
    
    public DataLinkageService(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.questionDao = database.questionDao();
        this.studyRecordDao = database.studyRecordDao();
        this.vocabularyDao = database.vocabularyDao();
    }
    
    /**
     * 记录学习行为并更新相关数据
     */
    public void recordStudyActivity(long questionId, String userAnswer, String correctAnswer, 
                                  long responseTime, String studyType) {
        boolean isCorrect = userAnswer.equals(correctAnswer);
        String sessionId = generateSessionId();
        
        // 获取题目信息
        QuestionEntity question = questionDao.getQuestionById((int) questionId);
        if (question == null) return;
        
        // 创建学习记录
        StudyRecordEntity studyRecord = new StudyRecordEntity();
        studyRecord.setQuestionId((int) questionId);
        studyRecord.setStudyType(studyType);
        studyRecord.setSessionId(sessionId);
        studyRecord.setUserAnswer(Integer.parseInt(userAnswer));
        studyRecord.setCorrectAnswer(Integer.parseInt(correctAnswer));
        studyRecord.setCorrect(isCorrect);
        studyRecord.setResponseTime(responseTime);
        studyRecord.setStudyDate(new Date());
        studyRecord.setExamType(question.getExamType());
        studyRecord.setCategory(question.getCategory());
        studyRecord.setDifficulty(question.getDifficulty());
        
        // 如果是词汇相关题目，关联词汇记录
        if (question.getRelatedVocabularyId() != null) {
            studyRecord.setVocabularyId(question.getRelatedVocabularyId());
            updateVocabularyProgress(question.getRelatedVocabularyId(), isCorrect);
        }
        
        // 更新题目统计信息
        updateQuestionStatistics(questionId, isCorrect);
        
        // 保存学习记录
        studyRecordDao.insertStudyRecord(studyRecord);
        
        // 判断是否需要复习
        studyRecord.setNeedsReview(!isCorrect || shouldScheduleReview(questionId));
    }
    
    /**
     * 更新词汇学习进度
     */
    private void updateVocabularyProgress(long vocabularyId, boolean isCorrect) {
        VocabularyRecordEntity vocabulary = vocabularyDao.getVocabularyById((int)vocabularyId);
        if (vocabulary == null) return;
        
        if (isCorrect) {
            vocabulary.setCorrectCount(vocabulary.getCorrectCount() + 1);
        } else {
            vocabulary.setWrongCount(vocabulary.getWrongCount() + 1);
        }
        
        // 更新记忆强度
        vocabulary.updateMemoryStrength(isCorrect);
        
        // 增加复习次数
        vocabulary.incrementReviewCount();
        
        // 更新最后学习时间
        vocabulary.setLastStudyTime(System.currentTimeMillis());
        
        // 判断是否掌握
        int totalAttempts = vocabulary.getCorrectCount() + vocabulary.getWrongCount();
        if (totalAttempts >= 3 && vocabulary.getMasteryPercentage() >= 80) {
            vocabulary.setMastered(true);
        }
        
        // 安排下次复习时间
        vocabulary.scheduleNextReview();
        
        vocabularyDao.update(vocabulary);
    }
    
    /**
     * 更新题目统计信息
     */
    private void updateQuestionStatistics(long questionId, boolean isCorrect) {
        QuestionEntity question = questionDao.getQuestionById((int) questionId);
        if (question == null) return;
        
        question.recordAttempt(isCorrect);
        questionDao.updateQuestion(question);
    }
    
    /**
     * 获取错题本数据
     */
    public List<QuestionEntity> getWrongQuestions() {
        // 获取准确率低于60%且至少答过1次的题目
        return questionDao.getWrongQuestions(60.0, 1);
    }
    
    /**
     * 获取需要复习的词汇
     */
    public List<VocabularyRecordEntity> getVocabulariesNeedingReview() {
        return vocabularyDao.getVocabulariesNeedingReview(System.currentTimeMillis());
    }
    
    /**
     * 获取需要复习的题目
     */
    public List<QuestionEntity> getQuestionsNeedingReview() {
        // 基于学习记录和正确率判断需要复习的题目
        return questionDao.getQuestionsByAccuracyRange(0, 60, 1, 50);
    }
    
    /**
     * 获取学习统计信息
     */
    public StudyStatistics getStudyStatistics() {
        StudyStatistics stats = new StudyStatistics();
        
        // 总体统计
        stats.totalStudyRecords = studyRecordDao.getTotalStudyRecordCount();
        stats.correctAnswers = studyRecordDao.getCorrectAnswerCount();
        stats.wrongAnswers = studyRecordDao.getWrongAnswerCount();
        
        // 词汇统计
        stats.totalVocabularies = vocabularyDao.getTotalVocabularyCount();
        stats.masteredVocabularies = vocabularyDao.getMasteredVocabularyCount();
        
        // 题目统计
        stats.totalQuestions = questionDao.getTotalQuestionCount();
        stats.masteredQuestions = studyRecordDao.getMasteredQuestionIds().size();
        
        // 计算正确率
        if (stats.totalStudyRecords > 0) {
            stats.overallAccuracy = (double) stats.correctAnswers / stats.totalStudyRecords * 100;
        }
        
        return stats;
    }
    
    /**
     * 获取个性化推荐题目
     */
    public List<QuestionEntity> getRecommendedQuestions(int limit) {
        // 基于学习历史推荐题目
        // 1. 优先推荐错误率高的题目
        List<QuestionEntity> lowAccuracyQuestions = questionDao.getQuestionsByAccuracyRange(0, 50, 1, limit);
        
        // 2. 推荐未练习的题目
        List<QuestionEntity> unpracticedQuestions = questionDao.getUnpracticedQuestions(limit);
        
        // 合并并限制数量（这里简化处理）
        if (!lowAccuracyQuestions.isEmpty()) {
            return lowAccuracyQuestions.subList(0, Math.min(limit, lowAccuracyQuestions.size()));
        } else if (!unpracticedQuestions.isEmpty()) {
            return unpracticedQuestions.subList(0, Math.min(limit, unpracticedQuestions.size()));
        } else {
            // 如果没有低准确率或未练习的题目，返回随机题目
            return questionDao.getRandomQuestionsByExamType("四级", limit);
        }
    }
    
    /**
     * 建立词汇和题目的关联
     */
    public void linkVocabularyToQuestions(long vocabularyId, List<Long> questionIds) {
        for (Long questionId : questionIds) {
            QuestionEntity question = questionDao.getQuestionById(questionId.intValue());
            if (question != null) {
                question.setRelatedVocabularyId((int) vocabularyId);
                questionDao.updateQuestion(question);
            }
        }
    }
    
    /**
     * 自动关联词汇和题目
     */
    public void autoLinkVocabularyAndQuestions() {
        List<VocabularyRecordEntity> vocabularies = vocabularyDao.getAllVocabulary();
        
        for (VocabularyRecordEntity vocabulary : vocabularies) {
            // 搜索包含该词汇的题目
            List<QuestionEntity> relatedQuestions = questionDao.searchQuestions(vocabulary.getWord());
            
            for (QuestionEntity question : relatedQuestions) {
                if (question.getRelatedVocabularyId() == null) {
                    question.setRelatedVocabularyId(vocabulary.getId());
                    questionDao.updateQuestion(question);
                }
            }
        }
    }
    
    /**
     * 判断是否需要安排复习
     */
    private boolean shouldScheduleReview(long questionId) {
        // 基于遗忘曲线和学习记录判断
        List<StudyRecordEntity> records = studyRecordDao.getStudyRecordsByQuestionId(questionId);
        
        if (records.isEmpty()) return true;
        
        // 如果最近答错了，需要复习
        StudyRecordEntity latestRecord = records.get(0);
        if (!latestRecord.isCorrect()) return true;
        
        // 如果很久没有练习，需要复习
        long daysSinceLastStudy = (System.currentTimeMillis() - latestRecord.getStudyDate().getTime()) / (24 * 60 * 60 * 1000);
        return daysSinceLastStudy > 7;
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 学习统计信息类
     */
    public static class StudyStatistics {
        public int totalStudyRecords;
        public int correctAnswers;
        public int wrongAnswers;
        public int totalVocabularies;
        public int masteredVocabularies;
        public int totalQuestions;
        public int masteredQuestions;
        public double overallAccuracy;
    }
}