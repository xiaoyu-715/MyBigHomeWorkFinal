# AutoGLM智能助手集成设计文档YSJ

## 📋 文档信息

**文档版本：** v1.0  
**创建日期：** 2025年12月19日  
**项目名称：** 英语学习助手 - AutoGLM智能助手集成设计文档  

---

## 1. 系统架构设计

### 1.1 整体架构

采用MVVM架构模式，分为四层：

```
展示层 → 业务逻辑层 → 数据访问层 → 外部服务层
```

**核心模块：**
- AutoGLM核心模块 - API调用、会话管理
- AI助手模块 - 对话交互、快捷操作
- 任务管理模块 - 任务执行、进度跟踪
- 数据分析模块 - 学习数据分析、报告生成

---

## 2. 核心类设计

### 2.1 AutoGLMManager (核心管理器)

**职责：** API调用、会话管理、任务调度

**主要方法：**
```java
public class AutoGLMManager {
    // 单例获取
    public static AutoGLMManager getInstance();
    
    // 初始化
    public void initialize(Context context, String apiKey);
    
    // 消息发送
    public void sendMessage(String message, AutoGLMCallback callback);
    
    // 任务执行
    public void executeTask(String taskDescription, TaskCallback callback);
    
    // 数据分析
    public void analyzeStudyData(StudyData data, AnalysisCallback callback);
    
    // 计划生成
    public void generateStudyPlan(PlanRequest request, PlanCallback callback);
    
    // 会话管理
    public void createNewSession();
    public void clearSession();
}
```

### 2.2 AutoGLMService (Retrofit接口)

**职责：** 定义API接口

```java
public interface AutoGLMService {
    @POST("chat/completions")
    Call<ChatResponse> chat(@Body ChatRequest request);
}
```

### 2.3 AIAssistantActivity (AI助手界面)

**职责：** 对话交互界面

**主要组件：**
- RecyclerView - 聊天消息列表
- EditText - 消息输入框
- Button - 发送、语音、附件按钮
- LinearLayout - 快捷操作区域

**主要方法：**
```java
public class AIAssistantActivity extends AppCompatActivity {
    private void sendMessage();
    private void executeQuickAction(String action);
    private void generateReport();
    private void showAnalysisResult(StudyAnalysis analysis);
}
```

---

## 3. 数据模型设计

### 3.1 ChatMessage (聊天消息)

```java
public class ChatMessage {
    private String id;              // 消息ID
    private String role;            // "user" 或 "assistant"
    private String content;         // 消息内容
    private long timestamp;         // 时间戳
    private MessageType type;       // TEXT, IMAGE, AUDIO
    private String metadata;        // 额外信息(JSON)
}
```

### 3.2 AutoGLMTask (任务对象)

```java
public class AutoGLMTask {
    private String taskId;          // 任务ID
    private String description;     // 任务描述
    private TaskStatus status;      // PENDING, RUNNING, COMPLETED, FAILED
    private List<TaskStep> steps;   // 任务步骤
    private String result;          // 执行结果
    private long startTime;         // 开始时间
    private long endTime;           // 结束时间
}
```

### 3.3 StudyAnalysis (学习分析)

```java
public class StudyAnalysis {
    private String analysisId;              // 分析ID
    private String userId;                  // 用户ID
    private StudyData inputData;            // 输入数据
    private String analysisContent;         // 分析内容
    private List<String> strengths;         // 优势
    private List<String> weaknesses;        // 劣势
    private List<String> recommendations;   // 建议
    private double overallScore;            // 总体评分
    private long analysisTime;              // 分析时间
}
```

### 3.4 StudyData (学习数据)

```java
public class StudyData {
    private String userId;              // 用户ID
    private int studyDays;              // 学习天数
    private int totalStudyTime;         // 总学习时长(分钟)
    private int vocabularyMastered;     // 掌握词汇数
    private double accuracyRate;        // 正确率
    private int wrongQuestionCount;     // 错题数量
}
```

---

## 4. 网络通信设计

### 4.1 API配置

```java
// 基础URL
baseUrl = "https://open.bigmodel.cn/api/paas/v4/"

// 模型名称
modelName = "autoglm-phone" 或 "GLM-4.5"

// 认证方式
Authorization: Bearer YOUR_API_KEY
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
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "AI响应内容"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 100,
    "completion_tokens": 200,
    "total_tokens": 300
  }
}
```

### 4.4 错误处理

```java
// 网络错误
- 连接超时 → 重试3次
- 网络不可用 → 使用缓存数据

// API错误
- 401 Unauthorized → 提示API Key无效
- 429 Too Many Requests → 限流提示
- 500 Server Error → 稍后重试
```

---

## 5. 数据库设计

### 5.1 chat_messages 表

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

### 5.2 autoglm_tasks 表

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

### 5.3 study_analyses 表

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

### 6.1 AI助手主界面布局

```xml
<LinearLayout orientation="vertical">
    <!-- 标题栏 -->
    <Toolbar title="AI学习助手" />
    
    <!-- 聊天区域 -->
    <RecyclerView id="chatRecyclerView" />
    
    <!-- 快捷操作 -->
    <HorizontalScrollView>
        <LinearLayout id="quickActionsLayout" />
    </HorizontalScrollView>
    
    <!-- 输入区域 -->
    <LinearLayout orientation="horizontal">
        <EditText id="inputEditText" hint="输入消息..." />
        <ImageButton id="voiceButton" />
        <ImageButton id="attachButton" />
        <ImageButton id="sendButton" />
    </LinearLayout>
</LinearLayout>
```

### 6.2 聊天气泡样式

**用户消息：**
- 背景色：#4CAF50 (绿色)
- 文字颜色：#FFFFFF
- 对齐方式：右对齐
- 圆角：16dp

**AI消息：**
- 背景色：#F5F5F5 (浅灰)
- 文字颜色：#333333
- 对齐方式：左对齐
- 圆角：16dp

### 6.3 快捷操作按钮

```xml
<Button
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:text="开始学习"
    android:drawableStart="@drawable/ic_play"
    android:layout_margin="8dp" />
```

---

## 7. 缓存策略

### 7.1 响应缓存

```java
// 缓存策略
- 缓存常见问题的响应
- 缓存有效期：24小时
- 缓存大小限制：10MB
- 使用LRU算法管理缓存
```

### 7.2 会话缓存

```java
// 会话历史缓存
- 保存最近50条消息
- 应用重启后恢复会话
- 支持多会话管理
```

---

## 8. 性能优化

### 8.1 网络优化

- 使用连接池复用连接
- 启用GZIP压缩
- 实现请求重试机制
- 设置合理的超时时间

### 8.2 内存优化

- 及时释放大对象
- 使用弱引用避免内存泄漏
- 图片使用Glide加载
- RecyclerView使用ViewHolder

### 8.3 UI优化

- 异步加载数据
- 使用LiveData更新UI
- 避免在主线程执行耗时操作
- 使用DiffUtil优化列表更新

---

## 9. 安全设计

### 9.1 API Key管理

```java
// 加密存储
SharedPreferences prefs = getEncryptedSharedPreferences();
prefs.edit().putString("api_key", encryptedKey).apply();

// 不在代码中硬编码
// 不在日志中打印
```

### 9.2 数据加密

```java
// 敏感数据加密
- 使用AES加密用户数据
- HTTPS通信加密
- 本地数据库加密(SQLCipher)
```

### 9.3 权限控制

```xml
<!-- 必需权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 可选权限 -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

## 10. 测试设计

### 10.1 单元测试

```java
// AutoGLMManager测试
@Test
public void testSendMessage() {
    // 测试消息发送功能
}

@Test
public void testSessionManagement() {
    // 测试会话管理
}
```

### 10.2 集成测试

```java
// API集成测试
@Test
public void testAPICall() {
    // 测试API调用
}

// 数据库集成测试
@Test
public void testDatabaseOperations() {
    // 测试数据库操作
}
```

### 10.3 UI测试

```java
// Espresso UI测试
@Test
public void testChatInterface() {
    // 测试聊天界面交互
}
```

---

## 11. 部署配置

### 11.1 Gradle配置

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

### 11.2 混淆配置

```proguard
# AutoGLM相关
-keep class com.example.mybighomework.autoglm.** { *; }
-keep interface com.example.mybighomework.autoglm.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
```

---

## 12. 监控与日志

### 12.1 日志记录

```java
// 使用统一的日志管理
LogManager.d("AutoGLM", "发送消息: " + message);
LogManager.e("AutoGLM", "API调用失败", exception);
```

### 12.2 性能监控

```java
// 监控API响应时间
long startTime = System.currentTimeMillis();
// ... API调用
long duration = System.currentTimeMillis() - startTime;
PerformanceMonitor.recordAPICall(duration);
```

---

**设计文档编制完成。**
