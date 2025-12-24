# 需求文档：AutoGLM 手机自动化功能集成

## 简介

将 AndroidAutoGLM 的手机自动化操作能力集成到英语学习App中，使AI助手能够通过屏幕识别和手势模拟自动完成学习任务，实现真正的"手机自动驾驶"学习体验。

## 术语表

- **Accessibility_Service**: Android无障碍服务，提供屏幕截取和手势模拟能力
- **AutoGLM_Phone_Model**: 智谱AI专用手机操作视觉大模型
- **Action**: AI返回的操作指令（点击、滑动、输入等）
- **Floating_Window**: 悬浮窗，显示AI运行状态
- **Gesture_Executor**: 手势执行器，将Action转换为实际屏幕操作
- **Screen_Capture**: 屏幕截取，获取当前屏幕图像供AI分析

## 需求

### 需求1：无障碍服务基础设施

**用户故事:** 作为开发者，我希望应用具备无障碍服务能力，以便AI能够截取屏幕和模拟手势操作。

#### 验收标准

1. WHEN 用户首次启动自动化功能 THEN THE System SHALL 检测无障碍服务是否已开启
2. WHEN 无障碍服务未开启 THEN THE System SHALL 显示引导界面并跳转到系统设置
3. WHEN 无障碍服务已开启 THEN THE Accessibility_Service SHALL 能够获取屏幕内容
4. THE Accessibility_Service SHALL 支持截取当前屏幕为Bitmap图像
5. THE Accessibility_Service SHALL 支持在指定坐标执行点击操作
6. THE Accessibility_Service SHALL 支持从起点到终点执行滑动操作
7. THE Accessibility_Service SHALL 支持在指定坐标执行长按操作
8. THE Accessibility_Service SHALL 支持执行系统返回和回到桌面操作


### 需求2：动作解析与执行

**用户故事:** 作为系统，我希望能够解析AI返回的操作指令并执行对应的屏幕操作，以便完成自动化任务。

#### 验收标准

1. WHEN AI返回包含 `do(action="Tap", element=[x,y])` 的响应 THEN THE Action_Parser SHALL 解析为点击动作
2. WHEN AI返回包含 `do(action="Swipe", start=[x1,y1], end=[x2,y2])` 的响应 THEN THE Action_Parser SHALL 解析为滑动动作
3. WHEN AI返回包含 `do(action="Type", text="xxx")` 的响应 THEN THE Action_Parser SHALL 解析为输入动作
4. WHEN AI返回包含 `do(action="Launch", app="xxx")` 的响应 THEN THE Action_Parser SHALL 解析为启动应用动作
5. WHEN AI返回包含 `do(action="Back")` 的响应 THEN THE Action_Parser SHALL 解析为返回动作
6. WHEN AI返回包含 `do(action="Home")` 的响应 THEN THE Action_Parser SHALL 解析为回到桌面动作
7. WHEN AI返回包含 `do(action="Long Press", element=[x,y])` 的响应 THEN THE Action_Parser SHALL 解析为长按动作
8. WHEN AI返回包含 `do(action="Wait", duration="x seconds")` 的响应 THEN THE Action_Parser SHALL 解析为等待动作
9. WHEN AI返回包含 `finish(message="xxx")` 的响应 THEN THE Action_Parser SHALL 解析为任务完成动作
10. WHEN 解析到的坐标为相对坐标(0-999) THEN THE Action_Parser SHALL 将其转换为屏幕绝对坐标
11. THE Gesture_Executor SHALL 根据解析后的Action执行对应的屏幕操作

### 需求3：多模态AI调用

**用户故事:** 作为系统，我希望能够将屏幕截图和用户指令发送给AI模型，以便获取下一步操作指令。

#### 验收标准

1. THE Multimodal_Client SHALL 使用智谱AI的 `autoglm-phone` 模型
2. THE Multimodal_Client SHALL 支持发送包含文本和图片的多模态消息
3. WHEN 发送请求时 THE Multimodal_Client SHALL 将Bitmap图像转换为Base64编码
4. THE Multimodal_Client SHALL 维护对话历史以保持上下文
5. WHEN 对话历史过长时 THE Multimodal_Client SHALL 移除历史消息中的图片以节省Token
6. THE Multimodal_Client SHALL 在系统提示词中包含所有支持的操作指令说明
7. IF API调用失败 THEN THE Multimodal_Client SHALL 返回错误信息


### 需求4：悬浮窗状态显示

**用户故事:** 作为用户，我希望在AI执行任务时能看到实时状态，以便了解当前进度和操作。

#### 验收标准

1. WHEN 自动化任务开始执行 THEN THE Floating_Window SHALL 显示在屏幕上
2. THE Floating_Window SHALL 显示当前AI状态（思考中、执行中、已完成、出错）
3. THE Floating_Window SHALL 显示当前正在执行的操作描述
4. THE Floating_Window SHALL 提供停止按钮允许用户随时终止任务
5. WHEN 用户点击停止按钮 THEN THE System SHALL 立即停止当前任务
6. THE Floating_Window SHALL 支持拖动改变位置
7. WHEN 执行截屏操作时 THE Floating_Window SHALL 临时隐藏以避免被截入
8. WHEN 任务完成或停止 THEN THE Floating_Window SHALL 显示返回应用按钮
9. IF 悬浮窗权限未开启 THEN THE System SHALL 引导用户开启权限

### 需求5：应用映射

**用户故事:** 作为系统，我希望能够根据应用名称找到对应的包名，以便启动指定应用。

#### 验收标准

1. THE App_Mapper SHALL 维护常用应用名称到包名的映射表
2. THE App_Mapper SHALL 支持中文应用名称（如"微信"、"抖音"）
3. THE App_Mapper SHALL 支持英文应用名称（如"WeChat"、"YouTube"）
4. THE App_Mapper SHALL 支持大小写不敏感的匹配
5. WHEN 应用名称无法匹配 THEN THE App_Mapper SHALL 返回null


### 需求6：自动化任务执行流程

**用户故事:** 作为用户，我希望通过自然语言指令让AI自动完成学习任务，以便提高学习效率。

#### 验收标准

1. WHEN 用户发送自动化指令 THEN THE System SHALL 检查所有必要权限
2. WHEN 权限检查通过 THEN THE System SHALL 显示悬浮窗并回到桌面
3. THE System SHALL 执行循环：截屏→发送AI→解析动作→执行动作
4. WHEN 执行动作后 THE System SHALL 等待页面加载再进行下一轮
5. THE System SHALL 限制最大执行步数（默认20步）防止无限循环
6. WHEN AI返回finish动作 THEN THE System SHALL 结束任务并显示完成消息
7. IF 执行过程中出错 THEN THE System SHALL 停止任务并显示错误信息
8. WHEN 用户点击停止 THEN THE System SHALL 立即终止执行循环

### 需求7：AI助手界面升级

**用户故事:** 作为用户，我希望在AI助手界面中能够选择普通对话或自动化任务模式，以便根据需要使用不同功能。

#### 验收标准

1. THE AI_Assistant_Activity SHALL 提供模式切换（对话模式/自动化模式）
2. WHEN 选择自动化模式 THEN THE System SHALL 显示自动化功能说明
3. THE AI_Assistant_Activity SHALL 显示权限状态（无障碍服务、悬浮窗）
4. WHEN 权限未开启 THEN THE System SHALL 提供快捷开启入口
5. THE AI_Assistant_Activity SHALL 在自动化模式下显示示例指令
6. WHEN 任务执行中 THE AI_Assistant_Activity SHALL 显示执行状态


### 需求8：文字输入功能

**用户故事:** 作为系统，我希望能够在输入框中自动输入文字，以便完成搜索、发消息等任务。

#### 验收标准

1. WHEN 执行Type动作 THEN THE Gesture_Executor SHALL 查找当前可编辑的输入框
2. WHEN 找到输入框 THEN THE Gesture_Executor SHALL 使用AccessibilityNodeInfo设置文本
3. IF 未找到可编辑输入框 THEN THE Gesture_Executor SHALL 返回执行失败
4. THE Gesture_Executor SHALL 在输入文本后等待短暂时间确保输入完成

### 需求9：手势动画反馈

**用户故事:** 作为用户，我希望在AI执行点击或滑动时能看到视觉反馈，以便了解AI正在操作的位置。

#### 验收标准

1. WHEN 执行点击操作 THEN THE System SHALL 在点击位置显示圆形动画
2. WHEN 执行滑动操作 THEN THE System SHALL 显示从起点到终点的轨迹动画
3. THE 动画 SHALL 使用半透明黄色以便清晰可见
4. THE 动画 SHALL 在操作完成后自动消失

### 需求10：错误处理与恢复

**用户故事:** 作为系统，我希望能够妥善处理各种错误情况，以便提供稳定的用户体验。

#### 验收标准

1. IF 截屏失败 THEN THE System SHALL 停止任务并提示用户
2. IF API调用失败 THEN THE System SHALL 停止任务并显示错误原因
3. IF 动作执行失败 THEN THE System SHALL 通知AI并尝试继续
4. IF 无障碍服务断开 THEN THE System SHALL 停止任务并提示重新开启
5. IF 达到最大步数限制 THEN THE System SHALL 停止任务并提示用户

