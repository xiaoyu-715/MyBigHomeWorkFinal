package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mybighomework.database.entity.ExamAnswerEntity;

import java.util.List;

@Dao
public interface ExamAnswerDao {

    @Insert
    void insertAnswer(ExamAnswerEntity answer);

    @Update
    void updateAnswer(ExamAnswerEntity answer);

    @Query("SELECT * FROM exam_answers WHERE examTitle = :examTitle ORDER BY questionIndex")
    List<ExamAnswerEntity> getAnswersByExam(String examTitle);

    @Query("SELECT * FROM exam_answers WHERE questionIndex = :questionIndex AND examTitle = :examTitle")
    ExamAnswerEntity getAnswer(int questionIndex, String examTitle);

    @Query("DELETE FROM exam_answers WHERE examTitle = :examTitle")
    void deleteAnswersByExam(String examTitle);

    @Query("SELECT COUNT(*) FROM exam_answers WHERE examTitle = :examTitle")
    int getAnswerCount(String examTitle);
}
