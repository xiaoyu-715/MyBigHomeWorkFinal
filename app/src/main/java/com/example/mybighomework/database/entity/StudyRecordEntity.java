package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "study_records",
        foreignKeys = {
            @ForeignKey(entity = QuestionEntity.class,
                       parentColumns = "id",
                       childColumns = "questionId",
                       onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = VocabularyRecordEntity.class,
                       parentColumns = "id", 
                       childColumns = "vocabularyId",
                       onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index("questionId"),
            @Index("vocabularyId"),
            @Index("studyDate"),
            @Index(value = {"questionId", "studyDate"}),
            @Index(value = {"vocabularyId", "studyDate"})
        })
public class StudyRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private Integer questionId;         // 关联的题目ID
    private Integer vocabularyId;       // 关联的词汇ID
    private String studyType;           // 学习类型：vocabulary, exam_practice, mock_exam, wrong_question
    private String sessionId;          // 学习会话ID（同一次练习的题目有相同sessionId）
    
    private int userAnswer;             // 用户答案
    private int correctAnswer;          // 正确答案
    private boolean isCorrect;          // 是否答对
    private long responseTime;          // 答题用时（毫秒）
    private Date studyDate;             // 学习时间
    
    // 学习上下文信息
    private String examType;            // 考试类型
    private String category;            // 题目分类
    private String difficulty;          // 难度等级
    private int attemptNumber;          // 第几次尝试该题目
    
    // 学习状态
    private boolean isFirstAttempt;     // 是否首次尝试
    private boolean needsReview;        // 是否需要复习
    private String notes;               // 学习笔记
    
    public StudyRecordEntity() {
        this.studyDate = new Date();
        this.isFirstAttempt = true;
        this.needsReview = false;
    }
    
    @Ignore
    public StudyRecordEntity(Integer questionId, Integer vocabularyId, String studyType, 
                           int userAnswer, int correctAnswer, boolean isCorrect) {
        this();
        this.questionId = questionId;
        this.vocabularyId = vocabularyId;
        this.studyType = studyType;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    
    public Integer getVocabularyId() { return vocabularyId; }
    public void setVocabularyId(Integer vocabularyId) { this.vocabularyId = vocabularyId; }
    
    public String getStudyType() { return studyType; }
    public void setStudyType(String studyType) { this.studyType = studyType; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public int getUserAnswer() { return userAnswer; }
    public void setUserAnswer(int userAnswer) { this.userAnswer = userAnswer; }
    
    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    
    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
    
    public Date getStudyDate() { return studyDate; }
    public void setStudyDate(Date studyDate) { this.studyDate = studyDate; }
    
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
    
    public boolean isFirstAttempt() { return isFirstAttempt; }
    public void setFirstAttempt(boolean firstAttempt) { isFirstAttempt = firstAttempt; }
    
    public boolean isNeedsReview() { return needsReview; }
    public void setNeedsReview(boolean needsReview) { this.needsReview = needsReview; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // 添加缺少的方法
    public void setType(String type) { this.studyType = type; }
    
    // 添加score字段和相关方法
    private int score;
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    // 添加createdTime字段和相关方法  
    private long createdTime;
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
}