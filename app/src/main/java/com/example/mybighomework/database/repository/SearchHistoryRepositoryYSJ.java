package com.example.mybighomework.database.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.SearchHistoryDao;
import com.example.mybighomework.database.entity.SearchHistoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 搜索历史数据仓库
 * 封装搜索历史相关的数据访问逻辑
 */
public class SearchHistoryRepositoryYSJ {
    
    private final SearchHistoryDao historyDao;
    private final ExecutorService executor;
    
    private static final int DEFAULT_HISTORY_LIMIT = 10;
    private static final long HISTORY_EXPIRY_DAYS = 30;
    
    public SearchHistoryRepositoryYSJ(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        historyDao = database.searchHistoryDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 添加搜索记录
     */
    public void addSearchHistory(String keyword, String userId) {
        executor.execute(() -> {
            SearchHistoryEntity entity = new SearchHistoryEntity();
            entity.setKeyword(keyword);
            entity.setUserId(userId);
            entity.setSearchTime(System.currentTimeMillis());
            historyDao.insert(entity);
        });
    }
    
    /**
     * 获取最近搜索（LiveData）
     */
    public LiveData<List<SearchHistoryEntity>> getRecentSearches(String userId) {
        return historyDao.getRecentSearches(userId, DEFAULT_HISTORY_LIMIT);
    }
    
    /**
     * 获取最近搜索（LiveData，指定数量）
     */
    public LiveData<List<SearchHistoryEntity>> getRecentSearches(String userId, int limit) {
        return historyDao.getRecentSearches(userId, limit);
    }
    
    /**
     * 获取最近搜索（同步）
     */
    public void getRecentSearchesSync(String userId, int limit, HistoryCallback callback) {
        executor.execute(() -> {
            try {
                List<SearchHistoryEntity> history = historyDao.getRecentSearchesSync(userId, limit);
                if (callback != null) {
                    callback.onSuccess(history);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 搜索建议（根据关键词前缀）
     */
    public void getSearchSuggestions(String userId, String keyword, int limit, SuggestionsCallback callback) {
        executor.execute(() -> {
            try {
                List<String> suggestions = historyDao.searchKeywords(userId, keyword, limit);
                if (callback != null) {
                    callback.onSuccess(suggestions);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 清除用户搜索历史
     */
    public void clearHistory(String userId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                historyDao.deleteAllByUser(userId);
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
     * 清除过期历史记录
     */
    public void clearOldHistory() {
        executor.execute(() -> {
            long expiryTime = System.currentTimeMillis() - (HISTORY_EXPIRY_DAYS * 24 * 60 * 60 * 1000);
            historyDao.deleteOldHistory(expiryTime);
        });
    }
    
    // ==================== 回调接口 ====================
    
    public interface HistoryCallback {
        void onSuccess(List<SearchHistoryEntity> history);
        void onError(String error);
    }
    
    public interface SuggestionsCallback {
        void onSuccess(List<String> suggestions);
        void onError(String error);
    }
    
    public interface SimpleCallback {
        void onComplete();
        void onError(String error);
    }
}
