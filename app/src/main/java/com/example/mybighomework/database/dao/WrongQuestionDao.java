package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mybighomework.database.entity.WrongQuestionEntity;
import java.util.List;

@Dao
public interface WrongQuestionDao {
    @Insert
    void insert(WrongQuestionEntity wrongQuestion);

    @Update
    void update(WrongQuestionEntity wrongQuestion);

    @Query("DELETE FROM wrong_questions WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM wrong_questions")
    void deleteAll();

    @Query("SELECT * FROM wrong_questions ORDER BY wrongTime DESC")
    List<WrongQuestionEntity> getAllWrongQuestions();

    @Query("SELECT * FROM wrong_questions WHERE category = :category ORDER BY wrongTime DESC")
    List<WrongQuestionEntity> getWrongQuestionsByCategory(String category);

    @Query("SELECT * FROM wrong_questions WHERE questionText = :questionText LIMIT 1")
    WrongQuestionEntity findByQuestionText(String questionText);

    @Query("SELECT * FROM wrong_questions WHERE questionText = :questionText AND category = :category LIMIT 1")
    WrongQuestionEntity findByQuestionAndCategory(String questionText, String category);

    @Query("SELECT COUNT(*) FROM wrong_questions WHERE questionText = :questionText AND category = :category")
    int countByQuestionAndCategory(String questionText, String category);
}
