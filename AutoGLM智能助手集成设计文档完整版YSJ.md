# AutoGLM智能助手集成设计文档完整版YSJ

## 📋 文档信息

**文档版本：** v2.0 完整版  
**创建日期：** 2025年12月19日  
**项目名称：** 英语学习助手 - AutoGLM智能助手集成设计文档  

---

## 1. 系统架构设计

### 1.1 整体架构（MVVM模式）

```
┌─────────────────────────────────────────┐
│         展示层 (Presentation)            │
│  Activities + Fragments + Adapters      │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│         ViewModel层                      │
│  LiveData + 业务逻辑                     │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│         业务层 (Business)                │
│  AutoGLMManager + Services              │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│         数据层 (Data)                    │
│  API + Database + Cache                 │
└─────────────────────────────────────────┘
```

### 1.2 核心模块

**AutoGLM核心模块：**
- AutoGLMManager - 单例管理器
- AutoGLMService - Retrofit接口
- NetworkManager - 网络管理
- CacheManager - 缓存管理

**UI模块：**
- AIAssistantActivity - AI助手主界面
- ChatAdapter - 消息列表适配器
- TaskManager - 任务管理器

**数据模块：**
- ChatMessage - 消息模型
- AutoGLMTask - 任务模型
- StudyAnalysis - 分析模型
- StudyData - 学习数据模型

---

## 2. 核心类设计

### 2.1 AutoGLMManager

```java
public class AutoGLMManager {
    // 单例
    private static volatile AutoGLMManager instance;
    
    // 配置
    private String apiKey;
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/";
    private String modelName = "autoglm-phone";
    
    // 服务
    private AutoGLMService service;
    private CacheManager cacheManager;
    
    // 会话
    private String sessionId;
    private List<ChatMessage> messageHistory;
    
    // 核心方法
    public static AutoGLMManager getInstance();
    public void initialize(Context context, String apiKey);
    public void sendMessage(String message, AutoGLMCallback callback);
    public void executeTask(String taskDescription, TaskCallback callback);
    public void analyzeStudyData(StudyData data, AnalysisCallback callback);
    public void generateStudyPlan(PlanRequest request, PlanCallback callback);
    public void createNewSession();
    public void clearSession();
}
```

### 2.2 AutoGLMService (Retrofit)

```java
public interface AutoGLMService {
    @POST("chat/completions")
    Call<ChatResponse> chat(@Body ChatRequest request);
    
    @POST("chat/completions")
    @Streaming
    Call<ResponseBody> chatStream(@Body ChatRequest request);
}
```

### 2.3 AIAssistantActivity

```java
public class AIAssistantActivity extends AppCompatActivity {
    // UI组件
    private RecyclerView chatRecyclerView;
    private EditText inputEditText;
    private ImageButton sendButton;
    private ImageButton voiceButton;
    
    // 数据
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private AutoGLMManager autoGLMManager;
    
    // 核心方法
    @Override
    protected void onCreate(Bundle savedInstanceState);
    private void sendMessage();
    private void handleResponse(ChatResponse response);
    private void executeQuickAction(String action);
    private void generateReport();
}
```

---

## 3. 数据模型设计

### 3.1 ChatMessage

```java
public class ChatMessage {
    private String id;              // 消息ID
    private String role;            // "user" 或 "assistant"
    private String content;         // 消息内容
    private long timestamp;         // 时间戳
    private MessageType type;       // TEXT, IMAGE, AUDIO
    private String metadata;        // JSON格式额外信息
}
```

### 3.2 AutoGLMTask

```java
public class AutoGLMTask {
    private String taskId;
    private String description;
    private TaskStatus status;      // PENDING, RUNNING, COMPLETED, FAILED
    private List<TaskStep> steps;
    private String result;
    private long startTime;
    private long endTime;
}
```

### 3.3 StudyAnalysis

```java
public class StudyAnalysis {
    private String analysisId;
    private String userId;
    private StudyData inputData;
    private String analysisContent;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> recommendations;
    private double overallScore;
    private long analysisTime;
}
```

---

## 4. 网络通信设计

### 4.1 API配置

```
基础URL: https://open.bigmodel.cn/api/paas/v4/
模型名称: autoglm-phone 或 GLM-4.5
认证方式: Authorization: Bearer YOUR_API_KEY
```

### 4.2 请求格式

```json
{
  "model": "autoglm-phone",
  "messages": [
    {"role": "system", "content": "系统提示词"},
    {"role": "user", "content": "用户消息"}
  ],
  "temperature": 0.7,
  "max_tokens": 2000
}
```

### 4.3 响应格式

```json
{
  "id": "chatcmpl-xxx",
  "choices": [{
    "message": {
      "role": "assistant",
      "content": "AI响应内容"
    },
    "finish_reason": "stop"
  }],
  "usage": {
    "prompt_tokens": 100,
    "completion_tokens": 200,
    "total_tokens": 300
  }
}
```

### 4.4 错误处理

- 401 Unauthorized → 提示API Key无效
- 429 Too Many Requests → 限流提示，稍后重试
- 500 Server Error → 服务器错误，稍后重试
- 网络超时 → 重试3次，使用缓存降级

---

## 5. 数据库设计

### 5.1 chat_messages表

```sql
CREATE TABLE chat_messages (
    id TEXT PRIMARY KEY,
    session_id TEXT NOT NULL,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    type TEXT DEFAULT 'TEXT',
    metadata TEXT
);
```

### 5.2 autoglm_tasks表

```sql
CREATE TABLE autoglm_tasks (
    task_id TEXT PRIMARY KEY,
    description TEXT NOT NULL,
    status TEXT NOT NULL,
    result TEXT,
    start_time INTEGER,
    end_time INTEGER,
    created_at INTEGER NOT NULL
);
```

### 5.3 study_analyses表

```sql
CREATE TABLE study_analyses (
    analysis_id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    analysis_content TEXT NOT NULL,
    overall_score REAL,
    analysis_time INTEGER NOT NULL
);
```

---

## 6. UI设计规范

### 6.1 主界面布局

```xml
<LinearLayout orientation="vertical">
    <Toolbar title="AI学习助手" />
    <RecyclerView id="chatRecyclerView" />
    <HorizontalScrollView>
        <LinearLayout id="quickActionsLayout" />
    </HorizontalScrollView>
    <LinearLayout orientation="horizontal">
        <EditText id="inputEditText" />
        <ImageButton id="voiceButton" />
        <ImageButton id="sendButton" />
    </LinearLayout>
</LinearLayout>
```

### 6.2 消息气泡样式

**AI消息：**
- 背景色：#F5F5F5
- 文字颜色：#333333
- 对齐：左对齐
- 圆角：16dp

**用户消息：**
- 背景色：#4CAF50
- 文字颜色：#FFFFFF
- 对齐：右对齐
- 圆角：16dp

---

## 7. 性能优化

### 7.1 网络优化
- 连接池复用
- GZIP压缩
- 请求重试（最多3次）
- 合理超时设置（30s连接，60s读写）

### 7.2 内存优化
- 及时释放大对象
- 使用弱引用
- RecyclerView ViewHolder复用
- 图片使用Glide加载

### 7.3 缓存策略
- 响应缓存（LRU，最大10MB）
- 会话缓存（最近50条消息）
- 缓存有效期24小时

---

## 8. 安全设计

### 8.1 API Key管理
- 加密存储（AES）
- 不在代码中硬编码
- 不在日志中打印

### 8.2 数据加密
- HTTPS通信
- 敏感数据本地加密
- 数据库加密（可选）

### 8.3 权限管理
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

## 9. 测试设计

### 9.1 单元测试
- AutoGLMManager测试
- 数据模型测试
- 工具类测试

### 9.2 集成测试
- API调用测试
- 数据库操作测试
- 缓存功能测试

### 9.3 UI测试
- Espresso界面测试
- 交互流程测试

---

## 10. 部署配置

### 10.1 Gradle依赖

```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // OkHttp
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    
    // Gson
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Room
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'
    
    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
}
```

### 10.2 混淆配置

```proguard
# AutoGLM
-keep class com.example.mybighomework.autoglm.** { *; }
-keep interface com.example.mybighomework.autoglm.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
```

---

**设计文档完成。**
