package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mybighomework.database.converter.DateConverter;
import com.example.mybighomework.database.converter.StringArrayConverter;

import java.util.Date;

@Entity(tableName = "wrong_questions")
@TypeConverters({DateConverter.class, StringArrayConverter.class})
public class WrongQuestionEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String questionText;    // 题目内容
    private String[] options;       // 选项
    private int correctAnswerIndex; // 正确答案索引
    private int userAnswerIndex;    // 用户答案索引
    private String explanation;     // 解析
    private String category;        // 分类 (e.g., "词汇训练", "真题练习", "模拟考试")
    private String source;          // 来源（用于区分是哪个活动产生的）
    private Date wrongTime;         // 答错时间
    private int wrongCount;         // 答错次数
    private boolean mastered;       // 是否已掌握

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public int getUserAnswerIndex() {
        return userAnswerIndex;
    }

    public void setUserAnswerIndex(int userAnswerIndex) {
        this.userAnswerIndex = userAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getWrongTime() {
        return wrongTime;
    }

    public void setWrongTime(Date wrongTime) {
        this.wrongTime = wrongTime;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public boolean isMastered() {
        return mastered;
    }

    public void setMastered(boolean mastered) {
        this.mastered = mastered;
    }
}
