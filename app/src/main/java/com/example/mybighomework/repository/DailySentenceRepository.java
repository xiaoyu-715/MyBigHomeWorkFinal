package com.example.mybighomework.repository;

import android.content.Context;
import android.util.Log;

import com.example.mybighomework.api.DailySentenceApiService;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.DailySentenceDao;
import com.example.mybighomework.database.entity.DailySentenceEntity;
import com.example.mybighomework.model.IcibaResponse;
import com.example.mybighomework.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 每日一句仓库类
 * 处理数据逻辑和业务规则
 */
public class DailySentenceRepository {
    
    private static final String TAG = "DailySentenceRepo";
    
    private final DailySentenceDao dailySentenceDao;
    private final ExecutorService executorService;
    private final DailySentenceApiService apiService;
    
    public DailySentenceRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.dailySentenceDao = database.dailySentenceDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.apiService = RetrofitClient.getInstance().getDailySentenceApiService();
    }
    
    /**
     * 获取今日一句（异步）
     * 优先从本地数据库获取，如果没有则从API获取
     */
    public void getTodaySentence(OnDataLoadedCallback<DailySentenceEntity> callback) {
        executorService.execute(() -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(Calendar.getInstance().getTime());
            
            DailySentenceEntity sentence = dailySentenceDao.getByDate(today);
            
            // 如果今天没有句子，从API获取
            if (sentence == null) {
                Log.d(TAG, "本地没有今日句子，从API获取...");
                fetchTodaySentenceFromApi(callback);
            } else {
                // 更新查看信息
                dailySentenceDao.updateViewInfo(sentence.getId(), System.currentTimeMillis());
                Log.d(TAG, "从本地数据库获取今日句子: " + sentence.getEnglishText());
                callback.onDataLoaded(sentence);
            }
        });
    }
    
    /**
     * 从金山词霸API获取今日一句
     */
    public void fetchTodaySentenceFromApi(OnDataLoadedCallback<DailySentenceEntity> callback) {
        Log.d(TAG, "开始从金山词霸API获取数据...");
        
        apiService.getDailySentence().enqueue(new Callback<IcibaResponse>() {
            @Override
            public void onResponse(Call<IcibaResponse> call, Response<IcibaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IcibaResponse apiData = response.body();
                    Log.d(TAG, "API请求成功: " + apiData.toString());
                    
                    // 在后台线程保存到数据库
                    executorService.execute(() -> {
                        DailySentenceEntity sentence = convertApiResponseToEntity(apiData);
                        long id = dailySentenceDao.insert(sentence);
                        sentence.setId((int) id);
                        
                        Log.d(TAG, "成功保存到数据库，ID: " + id);
                        callback.onDataLoaded(sentence);
                    });
                } else {
                    Log.e(TAG, "API请求失败，使用默认数据");
                    // API失败，使用默认数据
                    useFallbackData(callback);
                }
            }
            
            @Override
            public void onFailure(Call<IcibaResponse> call, Throwable t) {
                Log.e(TAG, "API请求异常: " + t.getMessage());
                // 网络失败，使用默认数据
                useFallbackData(callback);
            }
        });
    }
    
    /**
     * 将API响应转换为数据库实体
     */
    private DailySentenceEntity convertApiResponseToEntity(IcibaResponse apiData) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(Calendar.getInstance().getTime());
        
        DailySentenceEntity sentence = new DailySentenceEntity();
        sentence.setEnglishText(apiData.getContent());
        sentence.setChineseText(apiData.getNote());
        sentence.setAuthor(apiData.getTranslation());
        sentence.setDate(today);
        sentence.setCategory("每日一句");
        sentence.setAudioUrl(apiData.getTts());
        sentence.setImageUrl(apiData.getPicture());
        sentence.setSid(apiData.getSid());
        
        // 生成简单的词汇解析（可以后续优化）
        sentence.setVocabularyJson("[]");
        
        return sentence;
    }
    
    /**
     * 使用降级数据（当API失败时）
     */
    private void useFallbackData(OnDataLoadedCallback<DailySentenceEntity> callback) {
        executorService.execute(() -> {
            DailySentenceEntity defaultSentence = createDefaultSentenceForToday();
            long id = dailySentenceDao.insert(defaultSentence);
            defaultSentence.setId((int) id);
            
            Log.d(TAG, "使用默认数据，ID: " + id);
            callback.onDataLoaded(defaultSentence);
        });
    }
    
    /**
     * 获取所有历史记录（异步）
     */
    public void getAllSentences(OnDataLoadedCallback<List<DailySentenceEntity>> callback) {
        executorService.execute(() -> {
            List<DailySentenceEntity> sentences = dailySentenceDao.getAll();
            callback.onDataLoaded(sentences);
        });
    }
    
    /**
     * 获取最近N条记录（异步）
     */
    public void getRecentSentences(int limit, OnDataLoadedCallback<List<DailySentenceEntity>> callback) {
        executorService.execute(() -> {
            List<DailySentenceEntity> sentences = dailySentenceDao.getRecent(limit);
            callback.onDataLoaded(sentences);
        });
    }
    
    /**
     * 获取收藏的句子（异步）
     */
    public void getFavoritedSentences(OnDataLoadedCallback<List<DailySentenceEntity>> callback) {
        executorService.execute(() -> {
            List<DailySentenceEntity> sentences = dailySentenceDao.getFavorited();
            callback.onDataLoaded(sentences);
        });
    }
    
    /**
     * 切换收藏状态（异步）
     */
    public void toggleFavorite(int sentenceId, boolean isFavorited, OnOperationCallback callback) {
        executorService.execute(() -> {
            dailySentenceDao.updateFavoriteStatus(sentenceId, isFavorited);
            callback.onSuccess();
        });
    }
    
    /**
     * 标记为已学习（异步）
     */
    public void markAsLearned(int sentenceId, OnOperationCallback callback) {
        executorService.execute(() -> {
            dailySentenceDao.updateLearnedStatus(sentenceId, true);
            callback.onSuccess();
        });
    }
    
    /**
     * 插入新句子（异步）
     */
    public void insertSentence(DailySentenceEntity sentence, OnOperationCallback callback) {
        executorService.execute(() -> {
            dailySentenceDao.insert(sentence);
            callback.onSuccess();
        });
    }
    
    /**
     * 批量插入句子（异步）
     */
    public void insertSentences(List<DailySentenceEntity> sentences, OnOperationCallback callback) {
        executorService.execute(() -> {
            dailySentenceDao.insertAll(sentences);
            callback.onSuccess();
        });
    }
    
    /**
     * 更新句子（异步）
     */
    public void updateSentence(DailySentenceEntity sentence, OnOperationCallback callback) {
        executorService.execute(() -> {
            dailySentenceDao.update(sentence);
            callback.onSuccess();
        });
    }
    
    /**
     * 删除句子（异步）
     */
    public void deleteSentence(DailySentenceEntity sentence, OnOperationCallback callback) {
        executorService.execute(() -> {
            dailySentenceDao.delete(sentence);
            callback.onSuccess();
        });
    }
    
    /**
     * 初始化示例数据（异步）
     */
    public void initializeSampleData(OnOperationCallback callback) {
        executorService.execute(() -> {
            // 检查是否已有数据
            int count = dailySentenceDao.getCount();
            if (count > 0) {
                callback.onSuccess();
                return;
            }
            
            // 创建示例数据
            List<DailySentenceEntity> sampleSentences = createSampleSentences();
            dailySentenceDao.insertAll(sampleSentences);
            callback.onSuccess();
        });
    }
    
    /**
     * 创建今日默认句子
     */
    private DailySentenceEntity createDefaultSentenceForToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(Calendar.getInstance().getTime());
        
        DailySentenceEntity sentence = new DailySentenceEntity();
        sentence.setEnglishText("The best time to plant a tree was 20 years ago. The second best time is now.");
        sentence.setChineseText("种一棵树最好的时间是20年前，其次是现在。");
        sentence.setAuthor("Chinese Proverb");
        sentence.setDate(today);
        sentence.setCategory("谚语");
        sentence.setVocabularyJson("[{\"word\":\"plant\",\"meaning\":\"v. 种植；n. 植物\"},{\"word\":\"tree\",\"meaning\":\"n. 树木\"},{\"word\":\"ago\",\"meaning\":\"adv. 以前\"},{\"word\":\"second\",\"meaning\":\"adj. 第二的\"}]");
        
        return sentence;
    }
    
    /**
     * 创建示例句子数据
     */
    private List<DailySentenceEntity> createSampleSentences() {
        List<DailySentenceEntity> sentences = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        // 今天
        DailySentenceEntity sentence1 = new DailySentenceEntity();
        sentence1.setEnglishText("The best time to plant a tree was 20 years ago. The second best time is now.");
        sentence1.setChineseText("种一棵树最好的时间是20年前，其次是现在。");
        sentence1.setAuthor("Chinese Proverb");
        sentence1.setDate(dateFormat.format(calendar.getTime()));
        sentence1.setCategory("谚语");
        sentence1.setVocabularyJson("[{\"word\":\"plant\",\"meaning\":\"v. 种植；n. 植物\"},{\"word\":\"tree\",\"meaning\":\"n. 树木\"}]");
        sentences.add(sentence1);
        
        // 昨天
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        DailySentenceEntity sentence2 = new DailySentenceEntity();
        sentence2.setEnglishText("Life is what happens to you while you're busy making other plans.");
        sentence2.setChineseText("生活就是当你忙于制定其他计划时发生在你身上的事情。");
        sentence2.setAuthor("John Lennon");
        sentence2.setDate(dateFormat.format(calendar.getTime()));
        sentence2.setCategory("名人名言");
        sentence2.setVocabularyJson("[{\"word\":\"happen\",\"meaning\":\"v. 发生\"},{\"word\":\"busy\",\"meaning\":\"adj. 忙碌的\"}]");
        sentences.add(sentence2);
        
        // 前天
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        DailySentenceEntity sentence3 = new DailySentenceEntity();
        sentence3.setEnglishText("The only way to do great work is to love what you do.");
        sentence3.setChineseText("做出伟大工作的唯一方法就是热爱你所做的事情。");
        sentence3.setAuthor("Steve Jobs");
        sentence3.setDate(dateFormat.format(calendar.getTime()));
        sentence3.setCategory("名人名言");
        sentence3.setVocabularyJson("[{\"word\":\"great\",\"meaning\":\"adj. 伟大的\"},{\"word\":\"love\",\"meaning\":\"v. 热爱\"}]");
        sentences.add(sentence3);
        
        // 大前天
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        DailySentenceEntity sentence4 = new DailySentenceEntity();
        sentence4.setEnglishText("Innovation distinguishes between a leader and a follower.");
        sentence4.setChineseText("创新区分了领导者和追随者。");
        sentence4.setAuthor("Steve Jobs");
        sentence4.setDate(dateFormat.format(calendar.getTime()));
        sentence4.setCategory("名人名言");
        sentence4.setVocabularyJson("[{\"word\":\"innovation\",\"meaning\":\"n. 创新\"},{\"word\":\"distinguish\",\"meaning\":\"v. 区分\"}]");
        sentences.add(sentence4);
        
        // 4天前
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        DailySentenceEntity sentence5 = new DailySentenceEntity();
        sentence5.setEnglishText("Success is not final, failure is not fatal: it is the courage to continue that counts.");
        sentence5.setChineseText("成功不是终点，失败也不是末日：重要的是继续前进的勇气。");
        sentence5.setAuthor("Winston Churchill");
        sentence5.setDate(dateFormat.format(calendar.getTime()));
        sentence5.setCategory("励志");
        sentence5.setVocabularyJson("[{\"word\":\"courage\",\"meaning\":\"n. 勇气\"},{\"word\":\"count\",\"meaning\":\"v. 重要\"}]");
        sentences.add(sentence5);
        
        return sentences;
    }
    
    /**
     * 数据加载回调接口
     */
    public interface OnDataLoadedCallback<T> {
        void onDataLoaded(T data);
    }
    
    /**
     * 操作完成回调接口
     */
    public interface OnOperationCallback {
        void onSuccess();
    }
}

