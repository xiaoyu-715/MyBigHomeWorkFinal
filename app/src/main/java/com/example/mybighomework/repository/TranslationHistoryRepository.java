package com.example.mybighomework.repository;

import com.example.mybighomework.database.dao.TranslationHistoryDao;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;

import java.util.List;

/**
 * 翻译历史记录仓库类
 * 负责历史记录的数据管理，提供分页查询、删除、搜索等功能
 */
public class TranslationHistoryRepository {
    
    /**
     * 最大历史记录数量限制
     */
    public static final int MAX_HISTORY_COUNT = 500;
    
    /**
     * 默认每页记录数
     */
    public static final int PAGE_SIZE = 20;
    
    private final TranslationHistoryDao translationHistoryDao;
    
    public TranslationHistoryRepository(TranslationHistoryDao translationHistoryDao) {
        this.translationHistoryDao = translationHistoryDao;
    }
    
    /**
     * 分页获取历史记录
     * @param page 页码（从0开始）
     * @return 历史记录列表
     */
    public List<TranslationHistoryEntity> getHistoryPage(int page) {
        return getHistoryPage(page, PAGE_SIZE);
    }
    
    /**
     * 分页获取历史记录（自定义每页数量）
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return 历史记录列表
     */
    public List<TranslationHistoryEntity> getHistoryPage(int page, int pageSize) {
        int offset = page * pageSize;
        return translationHistoryDao.getPage(pageSize, offset);
    }

    
    /**
     * 搜索历史记录（分页）
     * @param keyword 搜索关键词
     * @param page 页码（从0开始）
     * @return 匹配的历史记录列表
     */
    public List<TranslationHistoryEntity> searchHistory(String keyword, int page) {
        return searchHistory(keyword, page, PAGE_SIZE);
    }
    
    /**
     * 搜索历史记录（分页，自定义每页数量）
     * @param keyword 搜索关键词
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return 匹配的历史记录列表
     */
    public List<TranslationHistoryEntity> searchHistory(String keyword, int page, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getHistoryPage(page, pageSize);
        }
        int offset = page * pageSize;
        return translationHistoryDao.searchWithPagination(keyword.trim(), pageSize, offset);
    }
    
    /**
     * 删除单条历史记录
     * @param id 记录ID
     */
    public void deleteHistory(int id) {
        translationHistoryDao.deleteById(id);
    }
    
    /**
     * 删除历史记录实体
     * @param entity 要删除的实体
     */
    public void deleteHistory(TranslationHistoryEntity entity) {
        if (entity != null) {
            translationHistoryDao.delete(entity);
        }
    }
    
    /**
     * 清理超出限制的旧记录
     * 保留最新的MAX_HISTORY_COUNT条记录
     */
    public void cleanupOldRecords() {
        cleanupOldRecords(MAX_HISTORY_COUNT);
    }
    
    /**
     * 清理超出限制的旧记录（自定义保留数量）
     * @param keepCount 要保留的记录数量
     */
    public void cleanupOldRecords(int keepCount) {
        int currentCount = getTotalCount();
        if (currentCount > keepCount) {
            translationHistoryDao.deleteOldRecords(keepCount);
        }
    }
    
    /**
     * 获取总记录数
     * @return 记录总数
     */
    public int getTotalCount() {
        return translationHistoryDao.getCount();
    }
    
    /**
     * 获取搜索结果总数
     * @param keyword 搜索关键词
     * @return 匹配的记录总数
     */
    public int getSearchCount(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getTotalCount();
        }
        return translationHistoryDao.getSearchCount(keyword.trim());
    }
    
    /**
     * 检查是否有更多数据
     * @param currentPage 当前页码
     * @return 是否还有更多数据
     */
    public boolean hasMoreData(int currentPage) {
        return hasMoreData(currentPage, PAGE_SIZE);
    }
    
    /**
     * 检查是否有更多数据（自定义每页数量）
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 是否还有更多数据
     */
    public boolean hasMoreData(int currentPage, int pageSize) {
        int totalCount = getTotalCount();
        int loadedCount = (currentPage + 1) * pageSize;
        return loadedCount < totalCount;
    }

    
    /**
     * 检查搜索结果是否有更多数据
     * @param keyword 搜索关键词
     * @param currentPage 当前页码
     * @return 是否还有更多数据
     */
    public boolean hasMoreSearchData(String keyword, int currentPage) {
        return hasMoreSearchData(keyword, currentPage, PAGE_SIZE);
    }
    
    /**
     * 检查搜索结果是否有更多数据（自定义每页数量）
     * @param keyword 搜索关键词
     * @param currentPage 当前页码
     * @param pageSize 每页数量
     * @return 是否还有更多数据
     */
    public boolean hasMoreSearchData(String keyword, int currentPage, int pageSize) {
        int totalCount = getSearchCount(keyword);
        int loadedCount = (currentPage + 1) * pageSize;
        return loadedCount < totalCount;
    }
    
    /**
     * 插入新的历史记录
     * 插入后会自动清理超出限制的旧记录
     * @param entity 要插入的历史记录
     * @return 插入的记录ID
     */
    public long insertHistory(TranslationHistoryEntity entity) {
        long id = translationHistoryDao.insert(entity);
        // 插入后自动清理超出限制的旧记录
        cleanupOldRecords();
        return id;
    }
    
    /**
     * 根据ID获取历史记录
     * @param id 记录ID
     * @return 历史记录实体，不存在则返回null
     */
    public TranslationHistoryEntity getHistoryById(int id) {
        return translationHistoryDao.getById(id);
    }
    
    /**
     * 获取最近的历史记录
     * @param limit 数量限制
     * @return 历史记录列表
     */
    public List<TranslationHistoryEntity> getRecentHistory(int limit) {
        return translationHistoryDao.getRecent(limit);
    }
    
    /**
     * 获取所有历史记录
     * @return 所有历史记录列表
     */
    public List<TranslationHistoryEntity> getAllHistory() {
        return translationHistoryDao.getAll();
    }
    
    /**
     * 更新历史记录
     * @param entity 要更新的历史记录
     */
    public void updateHistory(TranslationHistoryEntity entity) {
        translationHistoryDao.update(entity);
    }
    
    /**
     * 更新收藏状态
     * @param id 记录ID
     * @param isFavorited 是否收藏
     */
    public void updateFavoriteStatus(int id, boolean isFavorited) {
        translationHistoryDao.updateFavoriteStatus(id, isFavorited);
    }
    
    /**
     * 获取收藏的历史记录
     * @return 收藏的历史记录列表
     */
    public List<TranslationHistoryEntity> getFavoritedHistory() {
        return translationHistoryDao.getFavorited();
    }
    
    /**
     * 删除所有历史记录
     */
    public void deleteAllHistory() {
        translationHistoryDao.deleteAll();
    }
    
    /**
     * 搜索历史记录（不分页）
     * @param keyword 搜索关键词
     * @return 匹配的历史记录列表
     */
    public List<TranslationHistoryEntity> searchHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllHistory();
        }
        return translationHistoryDao.search(keyword.trim());
    }
}
