package com.example.mybighomework.autoglm.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mybighomework.autoglm.callback.AutoGLMCallback;
import com.example.mybighomework.autoglm.model.ChatMessage;
import com.example.mybighomework.autoglm.model.ChatRequest;
import com.example.mybighomework.autoglm.model.ChatResponse;
import com.example.mybighomework.autoglm.service.AuthInterceptor;
import com.example.mybighomework.autoglm.service.AutoGLMService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AutoGLMManager {
    private static final String TAG = "AutoGLMManager";
    private static final String PREFS_NAME = "AutoGLMPrefs";
    private static final String KEY_API_KEY = "api_key";
    
    private static volatile AutoGLMManager instance;
    private static final Object LOCK = new Object();
    
    private Context context;
    private AutoGLMService autoGLMService;
    private String apiKey;
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/";
    private String modelName = "glm-4";
    
    private String currentSessionId;
    private List<ChatMessage> messageHistory;
    private int maxHistorySize = 50;
    
    private boolean isInitialized = false;
    private boolean isProcessing = false;
    private TaskExecutor taskExecutor;
    
    private AutoGLMManager() {
        messageHistory = new ArrayList<>();
    }
    
    public static AutoGLMManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new AutoGLMManager();
                }
            }
        }
        return instance;
    }
    
    public void initialize(Context context, String apiKey) {
        this.context = context.getApplicationContext();
        this.apiKey = apiKey;
        
        saveApiKey(apiKey);
        initializeService();
        createNewSession();
        
        // 初始化任务执行器
        taskExecutor = new TaskExecutor(context);
        
        isInitialized = true;
        Log.d(TAG, "AutoGLMManager initialized successfully");
    }
    
    public void initializeWithSavedKey(Context context) {
        this.context = context.getApplicationContext();
        this.apiKey = loadApiKey();
        
        if (apiKey != null && !apiKey.isEmpty()) {
            initializeService();
            createNewSession();
            isInitialized = true;
            Log.d(TAG, "AutoGLMManager initialized with saved key");
        } else {
            Log.w(TAG, "No saved API key found");
        }
    }
    
    private void initializeService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(apiKey))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        autoGLMService = retrofit.create(AutoGLMService.class);
    }
    
    public void createNewSession() {
        currentSessionId = UUID.randomUUID().toString();
        messageHistory = new ArrayList<>();
        
        ChatMessage systemMessage = new ChatMessage("system",
                "你是英语学习助手应用的AI助手。你的职责是帮助用户学习英语，" +
                "包括解答问题、生成学习计划、分析学习数据、提供学习建议等。" +
                "请用友好、专业的语气与用户交流。");
        messageHistory.add(systemMessage);
        
        Log.d(TAG, "New session created: " + currentSessionId);
    }
    
    public void sendMessage(String message, AutoGLMCallback callback) {
        if (!isInitialized) {
            callback.onError(new IllegalStateException("AutoGLMManager未初始化"));
            return;
        }
        
        if (isProcessing) {
            callback.onError(new IllegalStateException("正在处理中，请稍候"));
            return;
        }
        
        isProcessing = true;
        
        ChatMessage userMessage = new ChatMessage("user", message);
        messageHistory.add(userMessage);
        
        ChatRequest request = new ChatRequest();
        request.setModel(modelName);
        request.setMessages(messageHistory);
        request.setTemperature(0.7);
        request.setMaxTokens(2000);
        
        autoGLMService.chat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                isProcessing = false;
                
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                        String assistantMessage = chatResponse.getChoices().get(0)
                                .getMessage().getContent();
                        
                        ChatMessage assistantMsg = new ChatMessage("assistant", assistantMessage);
                        messageHistory.add(assistantMsg);
                        
                        manageHistorySize();
                        
                        callback.onSuccess(assistantMessage);
                        Log.d(TAG, "Message sent successfully");
                    } else {
                        callback.onError(new Exception("响应格式错误"));
                    }
                } else {
                    callback.onError(new Exception("API调用失败: " + response.code()));
                    Log.e(TAG, "API call failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                isProcessing = false;
                callback.onError(new Exception("网络请求失败", t));
                Log.e(TAG, "Network request failed", t);
            }
        });
    }
    
    private void manageHistorySize() {
        if (messageHistory.size() > maxHistorySize) {
            List<ChatMessage> newHistory = new ArrayList<>();
            newHistory.add(messageHistory.get(0));
            newHistory.addAll(messageHistory.subList(
                    messageHistory.size() - maxHistorySize + 1,
                    messageHistory.size()
            ));
            messageHistory = newHistory;
        }
    }
    
    public void clearSession() {
        createNewSession();
        Log.d(TAG, "Session cleared");
    }
    
    private void saveApiKey(String apiKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_API_KEY, apiKey).apply();
    }
    
    private String loadApiKey() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API_KEY, null);
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public String getCurrentSessionId() {
        return currentSessionId;
    }
    
    public List<ChatMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
    
    public void sendMessageWithAction(String message, AutoGLMCallback callback) {
        if (!isInitialized) {
            callback.onError(new IllegalStateException("AutoGLMManager未初始化"));
            return;
        }
        
        // 先尝试执行任务
        if (taskExecutor != null && taskExecutor.executeTask(message)) {
            // 任务执行成功，仍然发送消息获取AI确认
            String confirmMessage = "好的，我已经为您" + getActionDescription(message) + "。";
            callback.onSuccess(confirmMessage);
            
            // 同时记录到历史
            ChatMessage userMsg = new ChatMessage("user", message);
            ChatMessage assistantMsg = new ChatMessage("assistant", confirmMessage);
            messageHistory.add(userMsg);
            messageHistory.add(assistantMsg);
            manageHistorySize();
        } else {
            // 无法识别为操作指令，正常对话
            sendMessage(message, callback);
        }
    }
    
    private String getActionDescription(String message) {
        String normalized = message.toLowerCase();
        if (normalized.contains("词汇") || normalized.contains("单词")) {
            return "启动词汇训练";
        } else if (normalized.contains("真题") || normalized.contains("考试")) {
            return "打开真题练习";
        } else if (normalized.contains("计划")) {
            return "打开学习计划";
        } else if (normalized.contains("错题")) {
            return "打开错题本";
        } else if (normalized.contains("报告")) {
            return "打开学习报告";
        } else if (normalized.contains("每日一句")) {
            return "打开每日一句";
        } else if (normalized.contains("任务")) {
            return "打开今日任务";
        }
        return "执行操作";
    }
}
