package com.example.mybighomework.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.UserWordCollectionDao;
import com.example.mybighomework.database.entity.UserWordCollectionEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户单词收藏（生词本）数据仓库
 * 封装生词本相关的数据访问逻辑
 */
public class UserWordCollectionRepositoryYSJ {
    
    private final UserWordCollectionDao collectionDao;
    private final ExecutorService executor;
    
    public UserWordCollectionRepositoryYSJ(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        collectionDao = database.userWordCollectionDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 收藏单词
     */
    public void collectWord(String wordId, String userId, String note, CollectCallback callback) {
        executor.execute(() -> {
            try {
                UserWordCollectionEntity entity = new UserWordCollectionEntity();
                entity.setWordId(wordId);
                entity.setUserId(userId);
                entity.setCollectedAt(System.currentTimeMillis());
                entity.setNote(note);
                long id = collectionDao.insert(entity);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 更新收藏笔记
     */
    public void updateNote(UserWordCollectionEntity entity) {
        executor.execute(() -> collectionDao.update(entity));
    }
    
    /**
     * 获取用户的所有收藏（LiveData）
     */
    public LiveData<List<UserWordCollectionEntity>> getAllCollections(String userId) {
        return collectionDao.getAllCollections(userId);
    }
    
    /**
     * 获取用户的所有收藏（同步）
     */
    public void getAllCollectionsSync(String userId, CollectionsCallback callback) {
        executor.execute(() -> {
            try {
                List<UserWordCollectionEntity> collections = collectionDao.getAllCollectionsSync(userId);
                if (callback != null) {
                    callback.onSuccess(collections);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 检查单词是否已收藏
     */
    public void isWordCollected(String wordId, String userId, CheckCallback callback) {
        executor.execute(() -> {
            try {
                boolean isCollected = collectionDao.isWordCollected(wordId, userId) > 0;
                if (callback != null) {
                    callback.onResult(isCollected);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onResult(false);
                }
            }
        });
    }
    
    /**
     * 获取收藏数量
     */
    public void getCollectionCount(String userId, CountCallback callback) {
        executor.execute(() -> {
            try {
                int count = collectionDao.getCollectionCount(userId);
                if (callback != null) {
                    callback.onSuccess(count);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 取消收藏
     */
    public void removeCollection(String wordId, String userId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                collectionDao.deleteByWordId(wordId, userId);
                if (callback != null) {
                    callback.onComplete();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 切换收藏状态
     */
    public void toggleCollection(String wordId, String userId, String note, ToggleCallback callback) {
        executor.execute(() -> {
            try {
                boolean isCurrentlyCollected = collectionDao.isWordCollected(wordId, userId) > 0;
                if (isCurrentlyCollected) {
                    collectionDao.deleteByWordId(wordId, userId);
                    if (callback != null) {
                        callback.onToggled(false);
                    }
                } else {
                    UserWordCollectionEntity entity = new UserWordCollectionEntity();
                    entity.setWordId(wordId);
                    entity.setUserId(userId);
                    entity.setCollectedAt(System.currentTimeMillis());
                    entity.setNote(note);
                    collectionDao.insert(entity);
                    if (callback != null) {
                        callback.onToggled(true);
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 删除用户所有收藏
     */
    public void deleteAllByUser(String userId) {
        executor.execute(() -> collectionDao.deleteAllByUser(userId));
    }
    
    // ==================== 回调接口 ====================
    
    public interface CollectCallback {
        void onSuccess(long id);
        void onError(String error);
    }
    
    public interface CollectionsCallback {
        void onSuccess(List<UserWordCollectionEntity> collections);
        void onError(String error);
    }
    
    public interface CheckCallback {
        void onResult(boolean isCollected);
    }
    
    public interface CountCallback {
        void onSuccess(int count);
        void onError(String error);
    }
    
    public interface SimpleCallback {
        void onComplete();
        void onError(String error);
    }
    
    public interface ToggleCallback {
        void onToggled(boolean isNowCollected);
        void onError(String error);
    }
}
