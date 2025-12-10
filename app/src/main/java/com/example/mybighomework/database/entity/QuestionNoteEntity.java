package com.example.mybighomework.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * 题目笔记实体类
 * 用于存储用户对各题目的笔记
 */
@Entity(tableName = "question_notes")
public class QuestionNoteEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "exam_title")
    private String examTitle; // 考试标题

    @ColumnInfo(name = "question_index")
    private int questionIndex; // 题目索引（从0开始）

    @ColumnInfo(name = "note_content")
    private String noteContent; // 笔记内容

    @ColumnInfo(name = "create_time")
    private Date createTime; // 创建时间

    @ColumnInfo(name = "update_time")
    private Date updateTime; // 更新时间

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}


