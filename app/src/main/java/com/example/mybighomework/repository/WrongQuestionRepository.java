package com.example.mybighomework.repository;

import com.example.mybighomework.database.dao.WrongQuestionDao;
import com.example.mybighomework.database.entity.WrongQuestionEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WrongQuestionRepository {
    private WrongQuestionDao wrongQuestionDao;
    private ExecutorService executorService;

    public WrongQuestionRepository(WrongQuestionDao wrongQuestionDao) {
        this.wrongQuestionDao = wrongQuestionDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void addWrongQuestion(WrongQuestionEntity wrongQuestion) {
        executorService.execute(() -> {
            WrongQuestionEntity existing = wrongQuestionDao.findByQuestionAndCategory(
                wrongQuestion.getQuestionText(), wrongQuestion.getCategory());
            if (existing != null) {
                existing.setWrongCount(existing.getWrongCount() + 1);
                existing.setWrongTime(wrongQuestion.getWrongTime());
                existing.setUserAnswerIndex(wrongQuestion.getUserAnswerIndex());
                existing.setMastered(false); // Reset mastered status on new mistake
                existing.setExplanation(wrongQuestion.getExplanation());
                existing.setOptions(wrongQuestion.getOptions());
                existing.setSource(wrongQuestion.getSource());
                wrongQuestionDao.update(existing);
            } else {
                wrongQuestion.setWrongCount(1);
                wrongQuestionDao.insert(wrongQuestion);
            }
        });
    }

    /**
     * 同步添加错题（用于已经在后台线程中的场景）
     */
    public void addWrongQuestionSync(WrongQuestionEntity wrongQuestion) {
        WrongQuestionEntity existing = wrongQuestionDao.findByQuestionAndCategory(
            wrongQuestion.getQuestionText(), wrongQuestion.getCategory());
        if (existing != null) {
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setWrongTime(wrongQuestion.getWrongTime());
            existing.setUserAnswerIndex(wrongQuestion.getUserAnswerIndex());
            existing.setMastered(false); // Reset mastered status on new mistake
            existing.setExplanation(wrongQuestion.getExplanation());
            existing.setOptions(wrongQuestion.getOptions());
            existing.setSource(wrongQuestion.getSource());
            wrongQuestionDao.update(existing);
        } else {
            wrongQuestion.setWrongCount(1);
            wrongQuestionDao.insert(wrongQuestion);
        }
    }

    public void updateWrongQuestion(WrongQuestionEntity wrongQuestion) {
        executorService.execute(() -> wrongQuestionDao.update(wrongQuestion));
    }

    public void deleteWrongQuestionById(int id) {
        executorService.execute(() -> wrongQuestionDao.deleteById(id));
    }

    public void deleteAllWrongQuestions() {
        executorService.execute(() -> wrongQuestionDao.deleteAll());
    }

    public void getAllWrongQuestions(RepositoryCallback<List<WrongQuestionEntity>> callback) {
        executorService.execute(() -> {
            List<WrongQuestionEntity> result = wrongQuestionDao.getAllWrongQuestions();
            if (callback != null && result != null) {
                callback.onComplete(result);
            }
        });
    }

    public void getWrongQuestionsByCategory(String category, RepositoryCallback<List<WrongQuestionEntity>> callback) {
        executorService.execute(() -> {
            List<WrongQuestionEntity> result = wrongQuestionDao.getWrongQuestionsByCategory(category);
            if (callback != null && result != null) {
                callback.onComplete(result);
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onComplete(T result);
    }
}
