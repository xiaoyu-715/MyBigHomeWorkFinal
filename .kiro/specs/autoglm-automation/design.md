# 设计文档：AutoGLM 手机自动化功能集成

## 概述

本设计文档描述如何将 AndroidAutoGLM 的手机自动化能力集成到英语学习App中。核心是通过Android无障碍服务实现屏幕截取和手势模拟，结合智谱AI的autoglm-phone多模态模型实现智能操作决策。

## 架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        英语学习App                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐     ┌──────────────────┐                  │
│  │ AIAssistantActivity│     │  其他Activity    │                  │
│  │  (升级版AI助手)   │     │                  │                  │
│  └────────┬─────────┘     └──────────────────┘                  │
│           │                                                      │
│  ┌────────▼─────────┐                                           │
│  │AutomationManager │ ◄─── 自动化任务管理器（新增）              │
│  │  - 任务状态管理   │                                           │
│  │  - 执行循环控制   │                                           │
│  └────────┬─────────┘                                           │
│           │                                                      │
│  ┌────────┴────────────────────────────────────┐                │
│  │                                              │                │
│  ▼                                              ▼                │
│  ┌──────────────────┐     ┌──────────────────┐                  │
│  │MultimodalClient  │     │ ActionExecutor   │                  │
│  │  - API调用       │     │  - 动作执行      │                  │
│  │  - 历史管理      │     │  - 手势模拟      │                  │
│  └──────────────────┘     └────────┬─────────┘                  │
│                                     │                            │
│  ┌──────────────────┐     ┌────────▼─────────┐                  │
│  │  ActionParser    │     │AccessibilityService│                │
│  │  - 指令解析      │     │  - 截屏          │                  │
│  │  - 坐标转换      │     │  - 手势派发      │                  │
│  └──────────────────┘     └──────────────────┘                  │
│                                                                  │
│  ┌──────────────────┐     ┌──────────────────┐                  │
│  │FloatingWindowMgr │     │   AppMapper      │                  │
│  │  - 状态显示      │     │  - 应用映射      │                  │
│  │  - 用户交互      │     │                  │                  │
│  └──────────────────┘     └──────────────────┘                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```


### 执行流程时序图

```
用户          AIAssistant    AutomationMgr   MultimodalClient   AccessibilityService   FloatingWindow
 │                │                │                │                    │                   │
 │──发送指令────►│                │                │                    │                   │
 │                │──启动任务────►│                │                    │                   │
 │                │                │──────────────────────────────────►│显示悬浮窗         │
 │                │                │                │                    │                   │
 │                │                │──────────────────────────────────►│回到桌面           │
 │                │                │                │                    │                   │
 │                │                │◄─────────────────────────────────│                   │
 │                │                │                │                    │                   │
 │                │                │ ┌─────────────────────────────────────────────────────┐
 │                │                │ │ 循环执行（最多20步）                                  │
 │                │                │ │                                                      │
 │                │                │ │──隐藏悬浮窗──────────────────────────────────────►│
 │                │                │ │──截屏────────────────────────►│                   │
 │                │                │ │◄─────────Bitmap───────────────│                   │
 │                │                │ │──显示悬浮窗──────────────────────────────────────►│
 │                │                │ │                                                      │
 │                │                │ │──发送(文本+图片)──►│                               │
 │                │                │ │◄────AI响应────────│                               │
 │                │                │ │                                                      │
 │                │                │ │──解析动作                                           │
 │                │                │ │──更新状态────────────────────────────────────────►│
 │                │                │ │                                                      │
 │                │                │ │──执行手势──────────────────────►│                   │
 │                │                │ │◄─────执行结果─────────────────│                   │
 │                │                │ │                                                      │
 │                │                │ │──等待2秒                                            │
 │                │                │ └─────────────────────────────────────────────────────┘
 │                │                │                │                    │                   │
 │                │◄──任务完成────│                │                    │                   │
 │◄──显示结果────│                │                │                    │                   │
```

## 组件和接口

### 1. AutomationAccessibilityService（无障碍服务）

**职责**: 提供屏幕截取和手势模拟的底层能力

**接口定义**:
```java
public class AutomationAccessibilityService extends AccessibilityService {
    // 单例访问
    public static AutomationAccessibilityService getInstance();
    
    // 截屏（Android 11+）
    public void captureScreen(ScreenshotCallback callback);
    
    // 点击操作
    public boolean performTap(float x, float y);
    
    // 滑动操作
    public boolean performSwipe(float startX, float startY, 
                                float endX, float endY, long duration);
    
    // 长按操作
    public boolean performLongPress(float x, float y, long duration);
    
    // 全局操作
    public boolean goBack();
    public boolean goHome();
    
    // 屏幕尺寸
    public int getScreenWidth();
    public int getScreenHeight();
    
    // 查找可编辑节点
    public AccessibilityNodeInfo findEditableNode();
    
    // 回调接口
    public interface ScreenshotCallback {
        void onScreenshot(Bitmap bitmap);
        void onError(int errorCode);
    }
}
```


### 2. Action（动作类型）

**职责**: 定义所有支持的操作类型

**类结构**:
```java
public abstract class Action {
    public static class Tap extends Action {
        public final int x, y;
    }
    
    public static class DoubleTap extends Action {
        public final int x, y;
    }
    
    public static class LongPress extends Action {
        public final int x, y;
    }
    
    public static class Swipe extends Action {
        public final int startX, startY, endX, endY;
    }
    
    public static class Type extends Action {
        public final String text;
    }
    
    public static class Launch extends Action {
        public final String appName;
    }
    
    public static class Back extends Action {}
    
    public static class Home extends Action {}
    
    public static class Wait extends Action {
        public final long durationMs;
    }
    
    public static class Finish extends Action {
        public final String message;
    }
    
    public static class Error extends Action {
        public final String reason;
    }
}
```

### 3. ActionParser（动作解析器）

**职责**: 解析AI返回的文本，提取操作指令

**接口定义**:
```java
public class ActionParser {
    // 解析AI响应为Action对象
    public static Action parse(String response, int screenWidth, int screenHeight);
    
    // 解析响应中的思考和动作部分
    public static Pair<String, String> parseResponseParts(String content);
}
```

**解析规则**:
1. 优先匹配 `finish(message="...")` 格式
2. 其次匹配 `do(action="...", ...)` 格式
3. 坐标从相对值(0-999)转换为绝对像素值
4. 无法解析时作为Finish消息处理

### 4. ActionExecutor（动作执行器）

**职责**: 执行解析后的Action

**接口定义**:
```java
public class ActionExecutor {
    public ActionExecutor(AutomationAccessibilityService service);
    
    // 执行动作，返回是否成功
    public boolean execute(Action action);
    
    // 启动应用
    private boolean launchApp(String packageName);
    
    // 输入文本
    private boolean typeText(String text);
}
```


### 5. MultimodalModelClient（多模态API客户端）

**职责**: 与智谱AI autoglm-phone模型通信

**接口定义**:
```java
public class MultimodalModelClient {
    public static final String SYSTEM_PROMPT = "..."; // 系统提示词
    
    public MultimodalModelClient(String apiKey);
    
    // 发送请求
    public String sendRequest(List<Message> history, Bitmap screenshot) 
        throws Exception;
    
    // 图片转Base64
    public static String bitmapToBase64(Bitmap bitmap);
    
    // 消息类
    public static class Message {
        public Message(String role, String text);
        public Message(String role, String text, Bitmap image);
    }
}
```

**API配置**:
- 端点: `https://open.bigmodel.cn/api/paas/v4/chat/completions`
- 模型: `autoglm-phone`
- 超时: 60秒
- max_tokens: 3000

### 6. FloatingWindowManager（悬浮窗管理器）

**职责**: 管理悬浮窗的显示和交互

**接口定义**:
```java
public class FloatingWindowManager {
    public FloatingWindowManager(Context context);
    
    // 显示悬浮窗
    public void show(OnStopClickListener listener);
    
    // 隐藏悬浮窗
    public void hide();
    
    // 更新状态文本
    public void updateStatus(String status);
    
    // 设置任务运行状态
    public void setTaskRunning(boolean running);
    
    // 设置可见性（截屏时隐藏）
    public void setVisibility(boolean visible);
    
    // 停止回调
    public interface OnStopClickListener {
        void onStopClick();
    }
}
```

### 7. AppMapper（应用映射器）

**职责**: 将应用名称映射到包名

**接口定义**:
```java
public class AppMapper {
    // 获取包名
    public static String getPackageName(String appName);
}
```

**映射表示例**:
- "微信" → "com.tencent.mm"
- "抖音" → "com.ss.android.ugc.aweme"
- "设置" → "com.android.settings"
- "Chrome" → "com.android.chrome"


### 8. AutomationManager（自动化任务管理器）

**职责**: 协调整个自动化任务的执行流程

**接口定义**:
```java
public class AutomationManager {
    private static AutomationManager instance;
    
    public static AutomationManager getInstance();
    
    // 初始化
    public void initialize(Context context, String apiKey);
    
    // 启动自动化任务
    public void startTask(String userCommand, AutomationCallback callback);
    
    // 停止任务
    public void stopTask();
    
    // 检查权限
    public boolean checkPermissions(Context context);
    
    // 任务状态
    public boolean isRunning();
    
    // 回调接口
    public interface AutomationCallback {
        void onStatusUpdate(String status);
        void onActionExecuted(Action action, boolean success);
        void onTaskComplete(String message);
        void onError(String error);
    }
}
```

## 数据模型

### 对话消息结构

```json
{
  "role": "user",
  "content": [
    {
      "type": "text",
      "text": "打开抖音搜索英语学习"
    },
    {
      "type": "image_url",
      "image_url": {
        "url": "data:image/png;base64,..."
      }
    }
  ]
}
```

### AI响应格式

```
<think>
当前在桌面，需要先启动抖音应用
</think>
<answer>
do(action="Launch", app="抖音")
</answer>
```

或新格式：
```
当前在桌面，需要先启动抖音应用
do(action="Launch", app="抖音")
```


## 正确性属性

*正确性属性是系统在所有有效执行中应保持为真的特征或行为。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### Property 1: 动作解析往返一致性

*对于任意* 有效的动作字符串，解析后的Action对象应包含原始字符串中的所有关键信息（动作类型、坐标、文本等）。

**验证: 需求 2.1-2.9**

### Property 2: 坐标转换正确性

*对于任意* 相对坐标(0-999)和屏幕尺寸，转换后的绝对坐标应满足：`absX = relX / 1000 * screenWidth` 且 `absY = relY / 1000 * screenHeight`。

**验证: 需求 2.10**

### Property 3: Base64编码往返一致性

*对于任意* 有效的Bitmap图像，编码为Base64后再解码应得到像素相同的图像。

**验证: 需求 3.3**

### Property 4: 应用映射大小写不敏感

*对于任意* 已映射的应用名称，其大小写变体（如"WeChat"、"wechat"、"WECHAT"）应映射到相同的包名。

**验证: 需求 5.4**

### Property 5: 未知应用返回null

*对于任意* 不在映射表中的应用名称，getPackageName应返回null。

**验证: 需求 5.5**

### Property 6: 停止任务立即生效

*对于任意* 正在运行的任务，调用stopTask后isRunning应立即返回false。

**验证: 需求 4.5**

### Property 7: 截屏时悬浮窗隐藏

*对于任意* 截屏操作，执行前悬浮窗应不可见，执行后应恢复可见。

**验证: 需求 4.7**

### Property 8: 对话历史图片清理

*对于任意* 对话历史，当添加新消息后，之前消息中的图片内容应被移除，仅保留文本。

**验证: 需求 3.5**


## 错误处理

### 权限错误

| 错误场景 | 处理方式 |
|---------|---------|
| 无障碍服务未开启 | 显示引导对话框，跳转系统设置 |
| 悬浮窗权限未开启 | 显示引导对话框，跳转权限设置 |
| 无障碍服务断开 | 停止任务，提示用户重新开启 |

### 执行错误

| 错误场景 | 处理方式 |
|---------|---------|
| 截屏失败 | 停止任务，显示错误提示 |
| API调用失败 | 停止任务，显示错误原因 |
| 动作执行失败 | 通知AI，尝试继续执行 |
| 应用启动失败 | 通知AI，尝试其他方式 |
| 输入框未找到 | 通知AI，尝试点击后重试 |

### 边界情况

| 场景 | 处理方式 |
|-----|---------|
| 达到最大步数(20) | 停止任务，提示用户 |
| 用户手动停止 | 立即终止循环，显示停止状态 |
| 网络超时 | 重试一次，失败则停止 |

## 测试策略

### 单元测试

- ActionParser: 测试各种动作格式的解析
- AppMapper: 测试应用名称映射
- 坐标转换: 测试相对坐标到绝对坐标的转换
- Base64编码: 测试图片编码解码

### 属性测试

- 动作解析的完整性
- 坐标转换的数学正确性
- 应用映射的大小写不敏感性

### 集成测试

- 无障碍服务的手势执行
- API调用的请求响应
- 完整任务执行流程

### 手动测试

- UI交互体验
- 悬浮窗拖动
- 权限引导流程

