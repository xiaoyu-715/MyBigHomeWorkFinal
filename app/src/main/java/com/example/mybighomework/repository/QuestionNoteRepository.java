package com.example.mybighomework.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.mybighomework.database.dao.QuestionNoteDao;
import com.example.mybighomework.database.entity.QuestionNoteEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 题目笔记仓库
 * 管理题目笔记的增删改查
 */
public class QuestionNoteRepository {

    private final QuestionNoteDao noteDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public QuestionNoteRepository(QuestionNoteDao noteDao) {
        this.noteDao = noteDao;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 保存或更新笔记
     */
    public void saveNote(String examTitle, int questionIndex, String noteContent, NoteCallback callback) {
        executorService.execute(() -> {
            try {
                QuestionNoteEntity existingNote = noteDao.getNoteByQuestion(examTitle, questionIndex);
                
                if (existingNote != null) {
                    // 更新现有笔记
                    existingNote.setNoteContent(noteContent);
                    existingNote.setUpdateTime(new Date());
                    noteDao.updateNote(existingNote);
                } else {
                    // 创建新笔记
                    QuestionNoteEntity newNote = new QuestionNoteEntity();
                    newNote.setExamTitle(examTitle);
                    newNote.setQuestionIndex(questionIndex);
                    newNote.setNoteContent(noteContent);
                    newNote.setCreateTime(new Date());
                    newNote.setUpdateTime(new Date());
                    noteDao.insertNote(newNote);
                }
                
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    /**
     * 获取指定题目的笔记
     */
    public void getNote(String examTitle, int questionIndex, NoteQueryCallback callback) {
        executorService.execute(() -> {
            try {
                QuestionNoteEntity note = noteDao.getNoteByQuestion(examTitle, questionIndex);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onResult(note);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    /**
     * 删除指定题目的笔记
     */
    public void deleteNote(String examTitle, int questionIndex, NoteCallback callback) {
        executorService.execute(() -> {
            try {
                noteDao.deleteNoteByQuestion(examTitle, questionIndex);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    /**
     * 获取某个考试的所有笔记
     */
    public void getNotesByExam(String examTitle, NoteListCallback callback) {
        executorService.execute(() -> {
            try {
                List<QuestionNoteEntity> notes = noteDao.getNotesByExam(examTitle);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onResult(notes);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    // 回调接口
    public interface NoteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface NoteQueryCallback {
        void onResult(QuestionNoteEntity note);
        void onError(Exception e);
    }

    public interface NoteListCallback {
        void onResult(List<QuestionNoteEntity> notes);
        void onError(Exception e);
    }
}


