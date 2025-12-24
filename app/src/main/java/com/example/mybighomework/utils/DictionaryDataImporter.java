package com.example.mybighomework.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.BookDao;
import com.example.mybighomework.database.dao.BookWordRelationDao;
import com.example.mybighomework.database.dao.DictionaryWordDao;
import com.example.mybighomework.database.entity.BookEntity;
import com.example.mybighomework.database.entity.BookWordRelationEntity;
import com.example.mybighomework.database.entity.DictionaryWordEntity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词典数据导入器
 * 负责从 assets 目录导入 DictionaryData 数据集到 Room 数据库
 */
public class DictionaryDataImporter {
    private static final String TAG = "DictionaryDataImporter";
    
    // SharedPreferences 键
    private static final String PREF_NAME = "dictionary_data_prefs";
    private static final String PREF_DATA_IMPORTED = "dictionary_data_imported";
    private static final String PREF_IMPORT_VERSION = "dictionary_data_version";
    private static final int CURRENT_DATA_VERSION = 1;
    
    // 批量插入大小 - 关联数据使用更小的批次以减少内存压力
    private static final int BATCH_SIZE = 1000;
    private static final int RELATION_BATCH_SIZE = 500;
    
    // CSV 文件路径
    private static final String WORD_CSV = "dictionary_data/word.csv";
    private static final String TRANSLATION_CSV = "dictionary_data/word_translation.csv";
    private static final String BOOK_CSV = "dictionary_data/book.csv";
    private static final String RELATION_CSV = "dictionary_data/relation_book_word.csv";
    
    // CSV 分隔符
    private static final String DELIMITER_ARROW = ">";
    private static final String DELIMITER_COMMA = ",";
    
    private final Context context;
    private final AppDatabase database;
    private final ExecutorService executor;
    
    /**
     * 导入进度监听器
     */
    public interface ImportProgressListener {
        void onProgress(int current, int total, String message);
        void onComplete(boolean success, String message);
    }
    
    public DictionaryDataImporter(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 检查数据是否已导入
     */
    public boolean isDataImported() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean imported = prefs.getBoolean(PREF_DATA_IMPORTED, false);
        int version = prefs.getInt(PREF_IMPORT_VERSION, 0);
        return imported && version >= CURRENT_DATA_VERSION;
    }
    
    /**
     * 标记数据已导入
     */
    private void markDataImported() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(PREF_DATA_IMPORTED, true)
            .putInt(PREF_IMPORT_VERSION, CURRENT_DATA_VERSION)
            .apply();
    }
    
    /**
     * 异步导入数据
     */
    public void importDataAsync(ImportProgressListener listener) {
        executor.execute(() -> {
            try {
                importDataInternal(listener);
            } catch (Exception e) {
                Log.e(TAG, "数据导入失败", e);
                if (listener != null) {
                    listener.onComplete(false, "数据导入失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 内部导入逻辑
     */
    private void importDataInternal(ImportProgressListener listener) {
        Log.d(TAG, "开始导入词典数据...");
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 导入单词数据
            notifyProgress(listener, 0, 100, "正在导入单词数据...");
            Map<String, String> translations = parseTranslations();
            int wordCount = importWords(translations, listener);
            Log.d(TAG, "单词导入完成: " + wordCount + " 条");
            
            // 2. 导入词书数据
            notifyProgress(listener, 40, 100, "正在导入词书数据...");
            int bookCount = importBooks(listener);
            Log.d(TAG, "词书导入完成: " + bookCount + " 条");
            
            // 3. 导入关联数据
            notifyProgress(listener, 60, 100, "正在导入词书-单词关联...");
            int relationCount = importRelations(listener);
            Log.d(TAG, "关联导入完成: " + relationCount + " 条");
            
            // 标记导入完成
            markDataImported();
            
            long duration = System.currentTimeMillis() - startTime;
            String message = String.format("导入完成！单词: %d, 词书: %d, 关联: %d (耗时: %.1f秒)",
                    wordCount, bookCount, relationCount, duration / 1000.0);
            Log.d(TAG, message);
            
            notifyProgress(listener, 100, 100, message);
            if (listener != null) {
                listener.onComplete(true, message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "导入过程中发生错误", e);
            if (listener != null) {
                listener.onComplete(false, "导入失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 解析翻译文件，返回 word -> translation 映射
     */
    private Map<String, String> parseTranslations() {
        Map<String, String> translations = new HashMap<>();
        
        try {
            InputStream is = context.getAssets().open(TRANSLATION_CSV);
            List<String[]> rows = CsvParser.parse(is, DELIMITER_COMMA, false, 
                (fields, lineNumber) -> fields);
            
            for (String[] fields : rows) {
                if (fields.length >= 2) {
                    String word = CsvParser.getField(fields, 0, "").toLowerCase();
                    String translation = CsvParser.getField(fields, 1, "");
                    if (!word.isEmpty() && !translation.isEmpty()) {
                        translations.put(word, translation);
                    }
                }
            }
            
            Log.d(TAG, "翻译数据解析完成: " + translations.size() + " 条");
        } catch (Exception e) {
            Log.e(TAG, "解析翻译文件失败", e);
        }
        
        return translations;
    }
    
    /**
     * 导入单词数据 - 使用流式解析减少内存压力
     */
    private int importWords(Map<String, String> translations, ImportProgressListener listener) {
        DictionaryWordDao wordDao = database.dictionaryWordDao();
        final List<DictionaryWordEntity> batch = new ArrayList<>(BATCH_SIZE);
        final int[] totalCount = {0};
        
        try {
            InputStream is = context.getAssets().open(WORD_CSV);
            
            CsvParser.parseStreaming(is, DELIMITER_ARROW, false,
                // 解析回调
                (fields, lineNumber) -> {
                    // word.csv 格式: id>word>phonetic_uk>phonetic_us>frequency>difficulty>acknowledge_rate
                    if (fields.length < 7) return null;
                    
                    String id = CsvParser.getField(fields, 0, "");
                    String word = CsvParser.getField(fields, 1, "");
                    
                    if (id.isEmpty() || word.isEmpty()) return null;
                    
                    DictionaryWordEntity entity = new DictionaryWordEntity();
                    entity.setId(id);
                    entity.setWord(word);
                    entity.setPhoneticUk(CsvParser.getField(fields, 2, ""));
                    entity.setPhoneticUs(CsvParser.getField(fields, 3, ""));
                    entity.setFrequency(CsvParser.getFloatField(fields, 4, 0f));
                    entity.setDifficulty(CsvParser.getIntField(fields, 5, 5));
                    entity.setAcknowledgeRate(CsvParser.getFloatField(fields, 6, 0f));
                    
                    // 合并翻译
                    String translation = translations.get(word.toLowerCase());
                    entity.setTranslation(translation != null ? translation : "");
                    
                    return entity;
                },
                // 流式处理回调
                new CsvParser.StreamingCallback<DictionaryWordEntity>() {
                    @Override
                    public void onItem(DictionaryWordEntity item, int lineNumber) {
                        batch.add(item);
                        
                        if (batch.size() >= BATCH_SIZE) {
                            try {
                                wordDao.insertAll(new ArrayList<>(batch));
                                totalCount[0] += batch.size();
                                batch.clear();
                                
                                notifyProgress(listener, 10 + Math.min(30, totalCount[0] / 2500), 100, 
                                        "正在导入单词: " + totalCount[0]);
                            } catch (Exception e) {
                                Log.e(TAG, "批量插入单词失败", e);
                            }
                        }
                    }
                    
                    @Override
                    public void onComplete(int totalProcessed) {
                        if (!batch.isEmpty()) {
                            try {
                                wordDao.insertAll(new ArrayList<>(batch));
                                totalCount[0] += batch.size();
                                batch.clear();
                            } catch (Exception e) {
                                Log.e(TAG, "插入剩余单词失败", e);
                            }
                        }
                    }
                },
                null);
            
        } catch (Exception e) {
            Log.e(TAG, "导入单词失败", e);
        }
        
        // 释放翻译映射内存
        translations.clear();
        
        return totalCount[0];
    }
    
    /**
     * 导入词书数据 - 使用流式解析
     */
    private int importBooks(ImportProgressListener listener) {
        BookDao bookDao = database.bookDao();
        final List<BookEntity> batch = new ArrayList<>(BATCH_SIZE);
        final int[] totalCount = {0};
        
        try {
            InputStream is = context.getAssets().open(BOOK_CSV);
            
            CsvParser.parseStreaming(is, DELIMITER_ARROW, false,
                // 解析回调
                (fields, lineNumber) -> {
                    // book.csv 格式: id>parent_id>level>order>name>item_num>direct_item_num>author>full_name>comment>organization>publisher>version>flag
                    if (fields.length < 6) return null;
                    
                    String id = CsvParser.getField(fields, 0, "");
                    if (id.isEmpty()) return null;
                    
                    BookEntity entity = new BookEntity();
                    entity.setId(id);
                    entity.setParentId(CsvParser.getField(fields, 1, "0"));
                    entity.setLevel(CsvParser.getIntField(fields, 2, 1));
                    entity.setBookOrder(CsvParser.getFloatField(fields, 3, 0f));
                    entity.setName(CsvParser.getField(fields, 4, ""));
                    entity.setItemNum(CsvParser.getIntField(fields, 5, 0));
                    entity.setDirectItemNum(CsvParser.getIntField(fields, 6, 0));
                    entity.setAuthor(CsvParser.getField(fields, 7, ""));
                    entity.setFullName(CsvParser.getField(fields, 8, ""));
                    entity.setComment(CsvParser.getField(fields, 9, ""));
                    entity.setOrganization(CsvParser.getField(fields, 10, ""));
                    entity.setPublisher(CsvParser.getField(fields, 11, ""));
                    entity.setVersion(CsvParser.getField(fields, 12, ""));
                    entity.setFlag(CsvParser.getField(fields, 13, ""));
                    
                    return entity;
                },
                // 流式处理回调
                new CsvParser.StreamingCallback<BookEntity>() {
                    @Override
                    public void onItem(BookEntity item, int lineNumber) {
                        batch.add(item);
                        
                        if (batch.size() >= BATCH_SIZE) {
                            try {
                                bookDao.insertAll(new ArrayList<>(batch));
                                totalCount[0] += batch.size();
                                batch.clear();
                            } catch (Exception e) {
                                Log.e(TAG, "批量插入词书失败", e);
                            }
                        }
                    }
                    
                    @Override
                    public void onComplete(int totalProcessed) {
                        if (!batch.isEmpty()) {
                            try {
                                bookDao.insertAll(new ArrayList<>(batch));
                                totalCount[0] += batch.size();
                                batch.clear();
                            } catch (Exception e) {
                                Log.e(TAG, "插入剩余词书失败", e);
                            }
                        }
                    }
                },
                null);
            
        } catch (Exception e) {
            Log.e(TAG, "导入词书失败", e);
        }
        
        return totalCount[0];
    }
    
    /**
     * 导入词书-单词关联数据 - 使用流式解析避免内存溢出
     */
    private int importRelations(ImportProgressListener listener) {
        BookWordRelationDao relationDao = database.bookWordRelationDao();
        BookDao bookDao = database.bookDao();
        DictionaryWordDao wordDao = database.dictionaryWordDao();
        
        final List<BookWordRelationEntity> batch = new ArrayList<>(RELATION_BATCH_SIZE);
        final int[] totalCount = {0};
        final int[] skippedCount = {0};
        final int[] batchInsertCount = {0};
        
        try {
            InputStream is = context.getAssets().open(RELATION_CSV);
            
            // 使用流式解析,边读边插入,避免内存溢出
            CsvParser.parseStreaming(is, DELIMITER_ARROW, false,
                // 解析回调
                (fields, lineNumber) -> {
                    // relation_book_word.csv 格式: id>book_id>word_id>flag>tag>order
                    if (fields.length < 3) return null;
                    
                    String id = CsvParser.getField(fields, 0, "");
                    String bookId = CsvParser.getField(fields, 1, "");
                    String wordId = CsvParser.getField(fields, 2, "");
                    
                    if (id.isEmpty() || bookId.isEmpty() || wordId.isEmpty()) return null;
                    
                    BookWordRelationEntity entity = new BookWordRelationEntity();
                    entity.setId(id);
                    entity.setBookId(bookId);
                    entity.setWordId(wordId);
                    entity.setFlag(CsvParser.getField(fields, 3, ""));
                    entity.setTag(CsvParser.getField(fields, 4, ""));
                    entity.setWordOrder(CsvParser.getIntField(fields, 5, 0));
                    
                    return entity;
                },
                // 流式处理回调
                new CsvParser.StreamingCallback<BookWordRelationEntity>() {
                    @Override
                    public void onItem(BookWordRelationEntity item, int lineNumber) {
                        // 验证外键是否存在
                        boolean bookExists = bookDao.getBookByIdSync(item.getBookId()) != null;
                        boolean wordExists = wordDao.getWordByIdSync(item.getWordId()) != null;
                        
                        if (!bookExists || !wordExists) {
                            skippedCount[0]++;
                            if (skippedCount[0] <= 10) {
                                Log.w(TAG, "跳过无效关联记录 (行" + lineNumber + "): bookId=" + item.getBookId() + 
                                          ", wordId=" + item.getWordId() + 
                                          ", bookExists=" + bookExists + ", wordExists=" + wordExists);
                            }
                            return;
                        }
                        
                        batch.add(item);
                        
                        // 达到批次大小时插入数据库
                        if (batch.size() >= RELATION_BATCH_SIZE) {
                            try {
                                relationDao.insertAll(new ArrayList<>(batch));
                                totalCount[0] += batch.size();
                                batchInsertCount[0]++;
                                batch.clear();
                                
                                // 每10个批次通知一次进度,减少UI更新频率
                                if (batchInsertCount[0] % 10 == 0) {
                                    notifyProgress(listener, 60 + Math.min(35, totalCount[0] / 25000), 100,
                                            "正在导入关联数据: " + totalCount[0]);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "批量插入关联数据失败", e);
                                batch.clear();
                            }
                        }
                    }
                    
                    @Override
                    public void onComplete(int totalProcessed) {
                        // 插入剩余数据
                        if (!batch.isEmpty()) {
                            try {
                                relationDao.insertAll(new ArrayList<>(batch));
                                totalCount[0] += batch.size();
                                batch.clear();
                            } catch (Exception e) {
                                Log.e(TAG, "插入剩余关联数据失败", e);
                            }
                        }
                        if (skippedCount[0] > 0) {
                            Log.w(TAG, "跳过了 " + skippedCount[0] + " 条无效关联记录（外键不存在）");
                        }
                        Log.d(TAG, "关联数据流式导入完成: " + totalCount[0] + " 条有效记录");
                    }
                },
                // 进度回调
                (current, total) -> {
                    notifyProgress(listener, 60 + Math.min(35, current / 25000), 100,
                            "正在解析关联数据: " + current);
                });
            
        } catch (Exception e) {
            Log.e(TAG, "导入关联数据失败", e);
        }
        
        return totalCount[0];
    }
    
    /**
     * 通知进度
     */
    private void notifyProgress(ImportProgressListener listener, int current, int total, String message) {
        if (listener != null) {
            listener.onProgress(current, total, message);
        }
    }
    
    /**
     * 重置导入状态（用于调试）
     */
    public void resetImportStatus() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(PREF_DATA_IMPORTED, false)
            .putInt(PREF_IMPORT_VERSION, 0)
            .apply();
    }
    
    /**
     * 获取导入统计信息
     */
    public ImportStats getImportStats() {
        ImportStats stats = new ImportStats();
        stats.wordCount = database.dictionaryWordDao().getWordCount();
        stats.bookCount = database.bookDao().getBookCount();
        stats.relationCount = database.bookWordRelationDao().getRelationCount();
        stats.isImported = isDataImported();
        return stats;
    }
    
    /**
     * 导入统计信息
     */
    public static class ImportStats {
        public int wordCount;
        public int bookCount;
        public int relationCount;
        public boolean isImported;
        
        @Override
        public String toString() {
            return "ImportStats{" +
                    "wordCount=" + wordCount +
                    ", bookCount=" + bookCount +
                    ", relationCount=" + relationCount +
                    ", isImported=" + isImported +
                    '}';
        }
    }
}
