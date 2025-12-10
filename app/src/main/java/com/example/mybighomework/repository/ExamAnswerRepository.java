package com.example.mybighomework.repository;

import android.os.AsyncTask;
import com.example.mybighomework.database.dao.ExamAnswerDao;
import com.example.mybighomework.database.entity.ExamAnswerEntity;

import java.util.List;

public class ExamAnswerRepository {

    private ExamAnswerDao examAnswerDao;

    public ExamAnswerRepository(ExamAnswerDao examAnswerDao) {
        this.examAnswerDao = examAnswerDao;
    }

    public void addAnswer(ExamAnswerEntity answer) {
        new InsertAnswerAsyncTask(examAnswerDao).execute(answer);
    }

    public void updateAnswer(ExamAnswerEntity answer) {
        new UpdateAnswerAsyncTask(examAnswerDao).execute(answer);
    }

    public List<ExamAnswerEntity> getAnswersByExam(String examTitle) {
        return examAnswerDao.getAnswersByExam(examTitle);
    }

    public ExamAnswerEntity getAnswer(int questionIndex, String examTitle) {
        return examAnswerDao.getAnswer(questionIndex, examTitle);
    }

    public void deleteAnswersByExam(String examTitle) {
        new DeleteAnswersAsyncTask(examAnswerDao).execute(examTitle);
    }

    public int getAnswerCount(String examTitle) {
        return examAnswerDao.getAnswerCount(examTitle);
    }

    // AsyncTask classes for database operations
    private static class InsertAnswerAsyncTask extends AsyncTask<ExamAnswerEntity, Void, Void> {
        private ExamAnswerDao dao;

        InsertAnswerAsyncTask(ExamAnswerDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(ExamAnswerEntity... answers) {
            dao.insertAnswer(answers[0]);
            return null;
        }
    }

    private static class UpdateAnswerAsyncTask extends AsyncTask<ExamAnswerEntity, Void, Void> {
        private ExamAnswerDao dao;

        UpdateAnswerAsyncTask(ExamAnswerDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(ExamAnswerEntity... answers) {
            dao.updateAnswer(answers[0]);
            return null;
        }
    }

    private static class DeleteAnswersAsyncTask extends AsyncTask<String, Void, Void> {
        private ExamAnswerDao dao;

        DeleteAnswersAsyncTask(ExamAnswerDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(String... examTitles) {
            dao.deleteAnswersByExam(examTitles[0]);
            return null;
        }
    }
}
