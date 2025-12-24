# AutoGLM 手机自动化功能集成方案

## 一、项目概述

### 1.1 OpenAuto/AndroidAutoGLM 功能分析

AndroidAutoGLM 是一个基于智谱AI视觉大模型的手机自动化操作应用，核心功能包括：

| 功能模块 | 描述 |
|---------|------|
| **屏幕识别** | 通过无障碍服务截取屏幕，发送给AI分析 |
| **手势模拟** | 点击、滑动、长按、双击等手势操作 |
| **应用启动** | 根据应用名称自动启动对应APP |
| **语音输入** | 使用Vosk离线语音识别输入指令 |
| **悬浮窗** | 实时显示AI状态和操作进度 |
| **文字输入** | 自动在输入框中输入文字 |

### 1.2 英语学习App现有AutoGLM模块

当前英语学习App已有基础的AutoGLM集成：
- 文本对话功能（使用glm-4模型）
- 简单的应用内跳转（TaskExecutor）
- AI学习助手界面

### 1.3 集成目标

将AndroidAutoGLM的**手机自动化操作能力**集成到英语学习App中，实现：
1. 语音指令控制学习流程
2. AI自动完成学习任务
3. 跨应用学习资源获取
4. 智能学习助手升级


---

## 二、技术架构对比

### 2.1 AndroidAutoGLM 架构

```
┌─────────────────────────────────────────────────────────────┐
│                    AndroidAutoGLM 架构                       │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │  ChatScreen │    │SettingsScreen│   │MarkdownViewer│    │
│  │  (Compose)  │    │  (Compose)  │    │  (Compose)  │     │
│  └──────┬──────┘    └─────────────┘    └─────────────┘     │
│         │                                                   │
│  ┌──────▼──────┐                                           │
│  │ChatViewModel│ ◄─── 管理对话状态、调用API、执行动作        │
│  └──────┬──────┘                                           │
│         │                                                   │
│  ┌──────▼──────────────────────────────────────────┐       │
│  │              AutoGLMService                      │       │
│  │  (AccessibilityService - 核心无障碍服务)         │       │
│  │  • takeScreenshot() - 截屏                       │       │
│  │  • performTap() - 点击                           │       │
│  │  • performSwipe() - 滑动                         │       │
│  │  • performLongPress() - 长按                     │       │
│  └──────┬──────────────────────────────────────────┘       │
│         │                                                   │
│  ┌──────▼──────┐    ┌─────────────┐    ┌─────────────┐     │
│  │ActionExecutor│    │ActionParser │    │  AppMapper  │     │
│  │ (执行动作)   │    │ (解析指令)  │    │ (应用映射)  │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
│                                                             │
│  ┌─────────────┐    ┌─────────────┐                        │
│  │ ModelClient │    │FloatingWindow│                       │
│  │ (API调用)   │    │ Controller  │                        │
│  └─────────────┘    └─────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 英语学习App现有架构

```
┌─────────────────────────────────────────────────────────────┐
│                  英语学习App AutoGLM模块                     │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐                                        │
│  │AIAssistantActivity│ ◄─── 对话界面 (传统View)             │
│  └────────┬────────┘                                        │
│           │                                                 │
│  ┌────────▼────────┐                                        │
│  │  AutoGLMManager │ ◄─── 管理API调用和会话                 │
│  └────────┬────────┘                                        │
│           │                                                 │
│  ┌────────▼────────┐    ┌─────────────┐                    │
│  │  AutoGLMService │    │ TaskExecutor │                    │
│  │  (Retrofit接口) │    │ (简单跳转)   │                    │
│  └─────────────────┘    └─────────────┘                    │
└─────────────────────────────────────────────────────────────┘
```


---

## 三、核心组件详解

### 3.1 AutoGLMService（无障碍服务）- 最核心组件

**文件位置**: `OpenAuto/AndroidAutoGLM/app/src/main/java/com/sidhu/androidautoglm/AutoGLMService.kt`

**核心能力**:

```kotlin
// 1. 截屏能力 (Android 11+)
suspend fun takeScreenshot(): Bitmap?

// 2. 点击操作
fun performTap(x: Float, y: Float): Boolean

// 3. 滑动操作
fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long): Boolean

// 4. 长按操作
fun performLongPress(x: Float, y: Float, duration: Long): Boolean

// 5. 全局操作
fun performGlobalBack(): Boolean
fun performGlobalHome(): Boolean
```

**关键依赖**:
- Android 11+ (API 30) 的 `takeScreenshot` API
- `AccessibilityService` 的 `dispatchGesture` API
- `TYPE_ACCESSIBILITY_OVERLAY` 悬浮窗权限

### 3.2 ActionParser（动作解析器）

**文件位置**: `OpenAuto/AndroidAutoGLM/app/src/main/java/com/sidhu/androidautoglm/action/ActionParser.kt`

**支持的动作类型**:

| 动作 | 格式 | 说明 |
|-----|------|------|
| Tap | `do(action="Tap", element=[x,y])` | 点击指定坐标 |
| DoubleTap | `do(action="Double Tap", element=[x,y])` | 双击 |
| LongPress | `do(action="Long Press", element=[x,y])` | 长按 |
| Swipe | `do(action="Swipe", start=[x1,y1], end=[x2,y2])` | 滑动 |
| Type | `do(action="Type", text="xxx")` | 输入文字 |
| Launch | `do(action="Launch", app="xxx")` | 启动应用 |
| Back | `do(action="Back")` | 返回 |
| Home | `do(action="Home")` | 回到桌面 |
| Wait | `do(action="Wait", duration="x seconds")` | 等待 |
| Finish | `finish(message="xxx")` | 完成任务 |

### 3.3 ModelClient（AI模型调用）

**文件位置**: `OpenAuto/AndroidAutoGLM/app/src/main/java/com/sidhu/androidautoglm/network/ModelClient.kt`

**关键配置**:
- 模型: `autoglm-phone` (智谱专用手机操作模型)
- API: `https://open.bigmodel.cn/api/paas/v4/chat/completions`
- 支持多模态输入（文本+图片）

### 3.4 FloatingWindowController（悬浮窗控制器）

**文件位置**: `OpenAuto/AndroidAutoGLM/app/src/main/java/com/sidhu/androidautoglm/FloatingWindowController.kt`

**功能**:
- 显示AI运行状态
- 支持拖动定位
- 截屏时自动隐藏
- 避让点击区域


---

## 四、集成方案

### 4.1 方案一：完整集成（推荐）

将AndroidAutoGLM的全部自动化能力集成到英语学习App中。

#### 4.1.1 需要新增的文件

```
app/src/main/java/com/example/mybighomework/autoglm/
├── accessibility/                    # 新增：无障碍服务模块
│   ├── AutomationAccessibilityService.java  # 核心无障碍服务
│   ├── GestureExecutor.java                 # 手势执行器
│   └── ScreenCaptureHelper.java             # 截屏辅助类
├── action/                           # 新增：动作模块
│   ├── Action.java                          # 动作基类
│   ├── ActionParser.java                    # 动作解析器
│   ├── ActionExecutorNew.java               # 动作执行器
│   └── AppMapper.java                       # 应用映射表
├── floating/                         # 新增：悬浮窗模块
│   ├── FloatingWindowManager.java           # 悬浮窗管理器
│   └── FloatingStatusView.java              # 状态视图
├── speech/                           # 新增：语音模块
│   ├── VoskModelManager.java                # Vosk模型管理
│   └── SpeechRecognizerHelper.java          # 语音识别辅助
├── network/                          # 修改：网络模块
│   └── MultimodalModelClient.java           # 多模态API客户端
└── ui/                               # 修改：UI模块
    └── AIAssistantActivity.java             # 升级AI助手界面
```

#### 4.1.2 AndroidManifest.xml 修改

```xml
<!-- 新增权限 -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" 
    tools:ignore="QueryAllPackagesPermission" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- 新增无障碍服务声明 -->
<service
    android:name=".autoglm.accessibility.AutomationAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/automation_accessibility_config" />
</service>
```

#### 4.1.3 无障碍服务配置文件

**res/xml/automation_accessibility_config.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault|flagIncludeNotImportantViews|flagRequestScreenshots"
    android:canPerformGestures="true"
    android:canRetrieveWindowContent="true"
    android:canTakeScreenshot="true"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100"
    android:settingsActivity=".SettingsActivity" />
```


### 4.2 方案二：轻量级集成

仅集成应用内自动化能力，不涉及跨应用操作。

#### 优点
- 不需要无障碍服务权限
- 实现简单，风险低
- 用户无需额外授权

#### 缺点
- 功能受限，只能在应用内操作
- 无法实现跨应用学习资源获取

#### 实现方式
- 升级现有TaskExecutor，增加更多应用内操作
- 使用普通AI模型（glm-4）而非autoglm-phone
- 通过Intent跳转实现功能导航

---

## 五、核心代码实现

### 5.1 无障碍服务实现（Java版本）

```java
// AutomationAccessibilityService.java
package com.example.mybighomework.autoglm.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

public class AutomationAccessibilityService extends AccessibilityService {
    
    private static AutomationAccessibilityService instance;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    
    public static AutomationAccessibilityService getInstance() {
        return instance;
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 监听屏幕事件
    }
    
    @Override
    public void onInterrupt() {
        // 服务中断处理
    }
    
    @Override
    public boolean onUnbind(android.content.Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }
    
    // 执行点击
    public boolean performTap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
        
        return dispatchGesture(builder.build(), null, null);
    }
    
    // 执行滑动
    public boolean performSwipe(float startX, float startY, float endX, float endY, long duration) {
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
        
        return dispatchGesture(builder.build(), null, null);
    }
    
    // 截屏 (Android 11+)
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void captureScreen(ScreenshotCallback callback) {
        takeScreenshot(Display.DEFAULT_DISPLAY, getMainExecutor(), 
            new TakeScreenshotCallback() {
                @Override
                public void onSuccess(ScreenshotResult result) {
                    Bitmap bitmap = Bitmap.wrapHardwareBuffer(
                        result.getHardwareBuffer(), 
                        result.getColorSpace()
                    );
                    callback.onScreenshot(bitmap.copy(Bitmap.Config.ARGB_8888, false));
                    result.getHardwareBuffer().close();
                }
                
                @Override
                public void onFailure(int errorCode) {
                    callback.onError(errorCode);
                }
            });
    }
    
    public interface ScreenshotCallback {
        void onScreenshot(Bitmap bitmap);
        void onError(int errorCode);
    }
    
    // 全局返回
    public boolean goBack() {
        return performGlobalAction(GLOBAL_ACTION_BACK);
    }
    
    // 回到桌面
    public boolean goHome() {
        return performGlobalAction(GLOBAL_ACTION_HOME);
    }
    
    // 获取屏幕尺寸
    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        return wm.getCurrentWindowMetrics().getBounds().width();
    }
    
    public int getScreenHeight() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        return wm.getCurrentWindowMetrics().getBounds().height();
    }
}
```


### 5.2 动作解析器实现

```java
// Action.java
package com.example.mybighomework.autoglm.action;

public abstract class Action {
    
    public static class Tap extends Action {
        public final int x, y;
        public Tap(int x, int y) { this.x = x; this.y = y; }
    }
    
    public static class DoubleTap extends Action {
        public final int x, y;
        public DoubleTap(int x, int y) { this.x = x; this.y = y; }
    }
    
    public static class LongPress extends Action {
        public final int x, y;
        public LongPress(int x, int y) { this.x = x; this.y = y; }
    }
    
    public static class Swipe extends Action {
        public final int startX, startY, endX, endY;
        public Swipe(int sx, int sy, int ex, int ey) {
            startX = sx; startY = sy; endX = ex; endY = ey;
        }
    }
    
    public static class Type extends Action {
        public final String text;
        public Type(String text) { this.text = text; }
    }
    
    public static class Launch extends Action {
        public final String appName;
        public Launch(String appName) { this.appName = appName; }
    }
    
    public static class Back extends Action {}
    public static class Home extends Action {}
    
    public static class Wait extends Action {
        public final long durationMs;
        public Wait(long ms) { this.durationMs = ms; }
    }
    
    public static class Finish extends Action {
        public final String message;
        public Finish(String msg) { this.message = msg; }
    }
    
    public static class Error extends Action {
        public final String reason;
        public Error(String reason) { this.reason = reason; }
    }
    
    public static class Unknown extends Action {}
}
```

```java
// ActionParser.java
package com.example.mybighomework.autoglm.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionParser {
    
    public static Action parse(String response, int screenWidth, int screenHeight) {
        String clean = response.trim();
        
        // 1. 匹配 finish(message="...")
        Pattern finishPattern = Pattern.compile(
            "finish\\s*\\(\\s*message\\s*=\\s*[\"'](.*?)[\"']\\s*\\)", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher finishMatcher = finishPattern.matcher(clean);
        if (finishMatcher.find()) {
            return new Action.Finish(finishMatcher.group(1));
        }
        
        // 2. 匹配 do(action="...", ...)
        Pattern doPattern = Pattern.compile(
            "do\\s*\\((.*)\\)", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher doMatcher = doPattern.matcher(clean);
        
        if (doMatcher.find()) {
            String args = doMatcher.group(1);
            String actionType = extractStringParam(args, "action");
            
            if (actionType == null) {
                return new Action.Error("缺少action类型");
            }
            
            switch (actionType.toLowerCase()) {
                case "tap":
                    int[] tapCoords = extractCoords(args, "element", screenWidth, screenHeight);
                    if (tapCoords != null) {
                        return new Action.Tap(tapCoords[0], tapCoords[1]);
                    }
                    break;
                    
                case "double tap":
                    int[] dtCoords = extractCoords(args, "element", screenWidth, screenHeight);
                    if (dtCoords != null) {
                        return new Action.DoubleTap(dtCoords[0], dtCoords[1]);
                    }
                    break;
                    
                case "long press":
                    int[] lpCoords = extractCoords(args, "element", screenWidth, screenHeight);
                    if (lpCoords != null) {
                        return new Action.LongPress(lpCoords[0], lpCoords[1]);
                    }
                    break;
                    
                case "swipe":
                    int[] start = extractCoords(args, "start", screenWidth, screenHeight);
                    int[] end = extractCoords(args, "end", screenWidth, screenHeight);
                    if (start != null && end != null) {
                        return new Action.Swipe(start[0], start[1], end[0], end[1]);
                    }
                    break;
                    
                case "type":
                    String text = extractStringParam(args, "text");
                    if (text != null) {
                        return new Action.Type(text);
                    }
                    break;
                    
                case "launch":
                    String app = extractStringParam(args, "app");
                    if (app != null) {
                        return new Action.Launch(app);
                    }
                    break;
                    
                case "back":
                    return new Action.Back();
                    
                case "home":
                    return new Action.Home();
                    
                case "wait":
                    String duration = extractStringParam(args, "duration");
                    if (duration != null) {
                        double seconds = parseSeconds(duration);
                        return new Action.Wait((long)(seconds * 1000));
                    }
                    return new Action.Wait(1000);
            }
        }
        
        // 3. 无法解析，作为完成消息
        return new Action.Finish(clean);
    }
    
    private static String extractStringParam(String args, String key) {
        Pattern p = Pattern.compile(key + "\\s*=\\s*[\"'](.*?)[\"']");
        Matcher m = p.matcher(args);
        return m.find() ? m.group(1) : null;
    }
    
    private static int[] extractCoords(String args, String key, int screenW, int screenH) {
        Pattern p = Pattern.compile(key + "\\s*=\\s*[\\[\\(]([\\d\\s,.]+)[\\]\\)]");
        Matcher m = p.matcher(args);
        if (m.find()) {
            String[] parts = m.group(1).split(",");
            if (parts.length >= 2) {
                float relX = Float.parseFloat(parts[0].trim());
                float relY = Float.parseFloat(parts[1].trim());
                int absX = (int)(relX / 1000f * screenW);
                int absY = (int)(relY / 1000f * screenH);
                return new int[]{absX, absY};
            }
        }
        return null;
    }
    
    private static double parseSeconds(String duration) {
        String num = duration.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(num);
        } catch (Exception e) {
            return 1.0;
        }
    }
}
```


### 5.3 多模态API客户端

```java
// MultimodalModelClient.java
package com.example.mybighomework.autoglm.network;

import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MultimodalModelClient {
    
    private static final String BASE_URL = "https://open.bigmodel.cn/api/paas/v4/";
    private static final String MODEL_NAME = "autoglm-phone";
    
    private final OkHttpClient client;
    private final String apiKey;
    
    // 系统提示词（来自AndroidAutoGLM）
    public static final String SYSTEM_PROMPT = 
        "你是一个智能体分析专家，可以根据操作历史和当前状态图执行一系列操作来完成任务。\n" +
        "你必须严格按照要求输出以下格式：\n" +
        "<think>{think}</think>\n" +
        "<answer>{action}</answer>\n\n" +
        "操作指令及其作用如下：\n" +
        "- do(action=\"Launch\", app=\"xxx\") - 启动目标app\n" +
        "- do(action=\"Tap\", element=[x,y]) - 点击操作\n" +
        "- do(action=\"Type\", text=\"xxx\") - 输入文本\n" +
        "- do(action=\"Swipe\", start=[x1,y1], end=[x2,y2]) - 滑动操作\n" +
        "- do(action=\"Long Press\", element=[x,y]) - 长按操作\n" +
        "- do(action=\"Double Tap\", element=[x,y]) - 双击操作\n" +
        "- do(action=\"Back\") - 返回上一页\n" +
        "- do(action=\"Home\") - 回到桌面\n" +
        "- do(action=\"Wait\", duration=\"x seconds\") - 等待\n" +
        "- finish(message=\"xxx\") - 完成任务";
    
    public MultimodalModelClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }
    
    public String sendRequest(List<Message> history, Bitmap screenshot) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL_NAME);
        requestBody.put("max_tokens", 3000);
        requestBody.put("temperature", 0.0);
        
        JSONArray messages = new JSONArray();
        for (Message msg : history) {
            messages.put(msg.toJson());
        }
        requestBody.put("messages", messages);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "chat/completions")
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(
                requestBody.toString(), 
                MediaType.parse("application/json")
            ))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JSONObject json = new JSONObject(response.body().string());
                return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            } else {
                throw new Exception("API调用失败: " + response.code());
            }
        }
    }
    
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }
    
    // 消息类
    public static class Message {
        public final String role;
        public final Object content;
        
        public Message(String role, String text) {
            this.role = role;
            this.content = text;
        }
        
        public Message(String role, String text, Bitmap image) {
            this.role = role;
            JSONArray contentArray = new JSONArray();
            try {
                JSONObject textObj = new JSONObject();
                textObj.put("type", "text");
                textObj.put("text", text);
                contentArray.put(textObj);
                
                if (image != null) {
                    JSONObject imageObj = new JSONObject();
                    imageObj.put("type", "image_url");
                    JSONObject urlObj = new JSONObject();
                    urlObj.put("url", "data:image/png;base64," + bitmapToBase64(image));
                    imageObj.put("image_url", urlObj);
                    contentArray.put(imageObj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.content = contentArray;
        }
        
        public JSONObject toJson() throws Exception {
            JSONObject obj = new JSONObject();
            obj.put("role", role);
            obj.put("content", content);
            return obj;
        }
    }
}
```


### 5.4 悬浮窗管理器

```java
// FloatingWindowManager.java
package com.example.mybighomework.autoglm.floating;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mybighomework.R;

public class FloatingWindowManager {
    
    private Context context;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    
    private TextView statusText;
    private ImageButton stopButton;
    
    private boolean isShowing = false;
    private OnStopClickListener stopListener;
    
    public interface OnStopClickListener {
        void onStopClick();
    }
    
    public FloatingWindowManager(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }
    
    public void show(OnStopClickListener listener) {
        if (isShowing) return;
        this.stopListener = listener;
        
        // 创建悬浮窗视图
        floatingView = LayoutInflater.from(context)
            .inflate(R.layout.floating_status_window, null);
        
        statusText = floatingView.findViewById(R.id.statusText);
        stopButton = floatingView.findViewById(R.id.stopButton);
        
        stopButton.setOnClickListener(v -> {
            if (stopListener != null) {
                stopListener.onStopClick();
            }
        });
        
        // 设置窗口参数
        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.x = 0;
        params.y = 100;
        
        // 添加拖动功能
        setupDragListener();
        
        windowManager.addView(floatingView, params);
        isShowing = true;
    }
    
    private void setupDragListener() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY - (int)(event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }
    
    public void updateStatus(String status) {
        if (statusText != null) {
            statusText.post(() -> statusText.setText(status));
        }
    }
    
    public void setTaskRunning(boolean running) {
        if (stopButton != null) {
            stopButton.post(() -> {
                stopButton.setImageResource(running 
                    ? R.drawable.ic_stop 
                    : R.drawable.ic_open);
            });
        }
    }
    
    public void hide() {
        if (!isShowing) return;
        
        try {
            windowManager.removeView(floatingView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isShowing = false;
    }
    
    public void setVisibility(boolean visible) {
        if (floatingView != null) {
            floatingView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}
```


---

## 六、英语学习场景应用

### 6.1 自动化学习任务示例

| 场景 | 用户指令 | AI执行流程 |
|-----|---------|-----------|
| 自动背单词 | "帮我背50个单词" | 启动词汇训练→自动翻页→记录进度 |
| 查找学习资料 | "在小红书搜索四级备考经验" | 启动小红书→搜索→筛选图文→返回总结 |
| 自动做题 | "帮我完成今天的真题练习" | 打开真题→选择题目→自动作答→提交 |
| 跨应用翻译 | "翻译微信里的这段英文" | 截屏微信→识别文字→调用翻译→显示结果 |
| 学习提醒 | "每天8点提醒我背单词" | 设置系统闹钟→关联学习任务 |

### 6.2 集成后的AI助手升级

```java
// 升级后的AIAssistantActivity核心逻辑
public class AIAssistantActivity extends AppCompatActivity {
    
    private AutomationAccessibilityService accessibilityService;
    private MultimodalModelClient modelClient;
    private FloatingWindowManager floatingWindow;
    private List<MultimodalModelClient.Message> conversationHistory;
    
    // 发送自动化任务
    private void sendAutomationTask(String userMessage) {
        // 1. 检查无障碍服务
        if (accessibilityService == null) {
            showAccessibilityDialog();
            return;
        }
        
        // 2. 显示悬浮窗
        floatingWindow.show(() -> stopTask());
        floatingWindow.updateStatus("正在思考...");
        
        // 3. 回到桌面，开始执行
        accessibilityService.goHome();
        
        // 4. 启动自动化循环
        new Thread(() -> executeAutomationLoop(userMessage)).start();
    }
    
    private void executeAutomationLoop(String task) {
        int maxSteps = 20;
        int step = 0;
        
        // 初始化对话历史
        conversationHistory.clear();
        conversationHistory.add(new MultimodalModelClient.Message(
            "system", 
            MultimodalModelClient.SYSTEM_PROMPT
        ));
        
        while (isRunning && step < maxSteps) {
            step++;
            
            try {
                // 1. 截屏
                floatingWindow.setVisibility(false);
                Thread.sleep(150);
                Bitmap screenshot = captureScreen();
                floatingWindow.setVisibility(true);
                
                if (screenshot == null) {
                    showError("截屏失败");
                    break;
                }
                
                // 2. 构建消息
                String prompt = step == 1 ? task : "继续执行任务";
                conversationHistory.add(new MultimodalModelClient.Message(
                    "user", prompt, screenshot
                ));
                
                // 3. 调用AI
                floatingWindow.updateStatus("AI思考中...");
                String response = modelClient.sendRequest(conversationHistory, screenshot);
                
                // 4. 解析动作
                Action action = ActionParser.parse(
                    response, 
                    accessibilityService.getScreenWidth(),
                    accessibilityService.getScreenHeight()
                );
                
                // 5. 执行动作
                floatingWindow.updateStatus(getActionDescription(action));
                boolean success = executeAction(action);
                
                // 6. 检查是否完成
                if (action instanceof Action.Finish) {
                    String message = ((Action.Finish) action).message;
                    runOnUiThread(() -> showTaskComplete(message));
                    break;
                }
                
                // 7. 记录历史
                conversationHistory.add(new MultimodalModelClient.Message(
                    "assistant", response
                ));
                
                // 8. 等待页面加载
                Thread.sleep(2000);
                
            } catch (Exception e) {
                showError("执行出错: " + e.getMessage());
                break;
            }
        }
        
        floatingWindow.setTaskRunning(false);
    }
    
    private boolean executeAction(Action action) {
        if (action instanceof Action.Tap) {
            Action.Tap tap = (Action.Tap) action;
            return accessibilityService.performTap(tap.x, tap.y);
        } else if (action instanceof Action.Swipe) {
            Action.Swipe swipe = (Action.Swipe) action;
            return accessibilityService.performSwipe(
                swipe.startX, swipe.startY, 
                swipe.endX, swipe.endY, 500
            );
        } else if (action instanceof Action.Back) {
            return accessibilityService.goBack();
        } else if (action instanceof Action.Home) {
            return accessibilityService.goHome();
        } else if (action instanceof Action.Launch) {
            String packageName = AppMapper.getPackageName(((Action.Launch) action).appName);
            return launchApp(packageName);
        }
        // ... 其他动作
        return false;
    }
}
```


---

## 七、依赖配置

### 7.1 build.gradle.kts 新增依赖

```kotlin
dependencies {
    // 现有依赖...
    
    // Vosk 离线语音识别 (可选)
    implementation("com.alphacephei:vosk-android:0.3.47")
    
    // OkHttp (如果尚未添加)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Compose (如果要使用Compose悬浮窗，可选)
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
}
```

### 7.2 assets 资源文件

如需离线语音识别，需要添加Vosk模型文件：
- `assets/model-cn.zip` - 中文语音模型
- `assets/model-en.zip` - 英文语音模型

---

## 八、实施步骤

### 阶段一：基础设施（1-2天）

| 任务 | 描述 | 优先级 |
|-----|------|-------|
| 1.1 | 创建无障碍服务类 `AutomationAccessibilityService` | P0 |
| 1.2 | 配置 AndroidManifest.xml 权限和服务声明 | P0 |
| 1.3 | 创建无障碍服务配置文件 | P0 |
| 1.4 | 实现权限检查和引导界面 | P0 |

### 阶段二：核心功能（2-3天）

| 任务 | 描述 | 优先级 |
|-----|------|-------|
| 2.1 | 实现 Action 类和 ActionParser | P0 |
| 2.2 | 实现 MultimodalModelClient | P0 |
| 2.3 | 实现 ActionExecutor | P0 |
| 2.4 | 实现 AppMapper（应用映射表） | P1 |

### 阶段三：UI组件（1-2天）

| 任务 | 描述 | 优先级 |
|-----|------|-------|
| 3.1 | 实现 FloatingWindowManager | P0 |
| 3.2 | 创建悬浮窗布局文件 | P0 |
| 3.3 | 升级 AIAssistantActivity | P0 |

### 阶段四：语音功能（可选，1-2天）

| 任务 | 描述 | 优先级 |
|-----|------|-------|
| 4.1 | 集成 Vosk 语音识别 | P2 |
| 4.2 | 实现语音输入UI | P2 |

### 阶段五：测试优化（1-2天）

| 任务 | 描述 | 优先级 |
|-----|------|-------|
| 5.1 | 功能测试 | P0 |
| 5.2 | 性能优化 | P1 |
| 5.3 | 异常处理完善 | P1 |

---

## 九、注意事项

### 9.1 系统要求
- **最低Android版本**: Android 11 (API 30) - 截屏功能依赖
- **推荐Android版本**: Android 12+ 

### 9.2 权限说明
用户需要手动开启以下权限：
1. **无障碍服务** - 核心功能依赖
2. **悬浮窗权限** - 显示状态窗口
3. **录音权限** - 语音输入（可选）

### 9.3 安全考虑
- API Key 应加密存储
- 敏感操作需用户确认
- 避免在支付等敏感场景自动操作

### 9.4 用户体验
- 首次使用需引导开启权限
- 提供清晰的状态反馈
- 支持随时停止任务

---

## 十、总结

本方案将 AndroidAutoGLM 的手机自动化能力完整集成到英语学习App中，主要包括：

1. **无障碍服务** - 实现屏幕截取和手势模拟
2. **AI视觉理解** - 使用 autoglm-phone 模型分析屏幕
3. **动作执行** - 解析AI指令并执行对应操作
4. **悬浮窗反馈** - 实时显示任务状态
5. **语音输入** - 支持语音指令（可选）

集成后，用户可以通过自然语言指令让AI自动完成学习任务，大幅提升学习效率。

