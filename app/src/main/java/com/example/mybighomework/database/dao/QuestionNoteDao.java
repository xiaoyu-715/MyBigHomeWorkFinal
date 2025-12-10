package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.QuestionNoteEntity;

import java.util.List;

/**
 * 题目笔记DAO
 */
@Dao
public interface QuestionNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(QuestionNoteEntity note);

    @Update
    void updateNote(QuestionNoteEntity note);

    @Delete
    void deleteNote(QuestionNoteEntity note);

    @Query("SELECT * FROM question_notes WHERE exam_title = :examTitle AND question_index = :questionIndex LIMIT 1")
    QuestionNoteEntity getNoteByQuestion(String examTitle, int questionIndex);

    @Query("SELECT * FROM question_notes WHERE exam_title = :examTitle ORDER BY question_index ASC")
    List<QuestionNoteEntity> getNotesByExam(String examTitle);

    @Query("DELETE FROM question_notes WHERE exam_title = :examTitle AND question_index = :questionIndex")
    void deleteNoteByQuestion(String examTitle, int questionIndex);

    @Query("SELECT COUNT(*) FROM question_notes WHERE exam_title = :examTitle")
    int getNotesCountByExam(String examTitle);
}


