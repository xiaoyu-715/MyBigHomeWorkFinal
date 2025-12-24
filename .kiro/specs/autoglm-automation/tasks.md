# 实现计划：AutoGLM 手机自动化功能集成

## 概述

本实现计划将 AndroidAutoGLM 的手机自动化能力集成到英语学习App中。采用分阶段实现策略，从底层无障碍服务开始，逐步构建动作解析、API调用、悬浮窗等模块，最终完成整体集成。

## 任务列表

- [x] 1. 无障碍服务基础设施
  - [x] 1.1 创建 AutomationAccessibilityService 类
    - 继承 AccessibilityService
    - 实现单例模式
    - 实现 onAccessibilityEvent 和 onInterrupt 方法
    - 实现 getScreenWidth/getScreenHeight 方法
    - _需求: 1.3, 1.4_

  - [x] 1.2 实现截屏功能
    - 使用 takeScreenshot API (Android 11+)
    - 实现 ScreenshotCallback 回调接口
    - 处理截屏失败情况
    - _需求: 1.4_

  - [x] 1.3 实现手势操作
    - 实现 performTap 点击操作
    - 实现 performSwipe 滑动操作
    - 实现 performLongPress 长按操作
    - 使用 GestureDescription 构建手势
    - _需求: 1.5, 1.6, 1.7_

  - [x] 1.4 实现全局操作
    - 实现 goBack 返回操作
    - 实现 goHome 回到桌面操作
    - 使用 performGlobalAction API
    - _需求: 1.8_

  - [x] 1.5 实现文本输入辅助
    - 实现 findEditableNode 查找可编辑节点
    - 使用 AccessibilityNodeInfo 遍历节点树
    - _需求: 8.1, 8.2_

  - [x] 1.6 配置无障碍服务
    - 创建 res/xml/accessibility_service_config.xml
    - 在 AndroidManifest.xml 中注册服务
    - 配置 canTakeScreenshot 和 canPerformGestures 能力
    - _需求: 1.3_

- [x] 2. 动作模块
  - [x] 2.1 创建 Action 类层次结构
    - 创建抽象基类 Action
    - 创建 Tap, DoubleTap, LongPress 子类
    - 创建 Swipe 子类
    - 创建 Type, Launch, Back, Home 子类
    - 创建 Wait, Finish, Error 子类
    - _需求: 2.1-2.9_

  - [x] 2.2 实现 ActionParser 解析器
    - 实现 parse 方法解析AI响应
    - 支持 `do(action="...", ...)` 格式解析
    - 支持 `finish(message="...")` 格式解析
    - 实现坐标从相对值(0-999)到绝对像素的转换
    - 实现 parseResponseParts 分离思考和动作部分
    - _需求: 2.1-2.10_

  - [ ]* 2.3 编写 ActionParser 属性测试
    - **Property 1: 动作解析往返一致性**
    - **Property 2: 坐标转换正确性**
    - **验证: 需求 2.1-2.10**

  - [x] 2.4 实现 AppMapper 应用映射器
    - 创建应用名称到包名的映射表
    - 支持中文应用名称（微信、抖音等）
    - 支持英文应用名称（WeChat、YouTube等）
    - 实现大小写不敏感匹配
    - _需求: 5.1-5.5_

  - [ ]* 2.5 编写 AppMapper 属性测试
    - **Property 4: 应用映射大小写不敏感**
    - **Property 5: 未知应用返回null**
    - **验证: 需求 5.4, 5.5**

  - [x] 2.6 实现 ActionExecutor 动作执行器
    - 注入 AutomationAccessibilityService 依赖
    - 实现 execute 方法分发不同动作类型
    - 实现 launchApp 启动应用
    - 实现 typeText 输入文本
    - 处理执行失败情况
    - _需求: 2.11, 8.1-8.4_

- [x] 3. 检查点 - 确保动作模块测试通过
  - 运行所有单元测试和属性测试
  - 如有问题请向用户确认

- [x] 4. 网络模块
  - [x] 4.1 实现 MultimodalModelClient
    - 配置智谱AI API端点和模型名称
    - 实现 Message 内部类（支持文本和图片）
    - 实现 bitmapToBase64 图片编码方法
    - 实现 sendRequest 发送多模态请求
    - 维护对话历史列表
    - 实现历史消息图片清理逻辑
    - _需求: 3.1-3.7_

  - [ ]* 4.2 编写 MultimodalModelClient 属性测试
    - **Property 3: Base64编码往返一致性**
    - **Property 8: 对话历史图片清理**
    - **验证: 需求 3.3, 3.5**

  - [x] 4.3 定义系统提示词
    - 包含所有支持的操作指令说明
    - 包含响应格式要求
    - 参考 AndroidAutoGLM 的提示词设计
    - _需求: 3.6_

- [x] 5. 悬浮窗模块
  - [x] 5.1 创建悬浮窗布局
    - 创建 layout/layout_floating_window.xml
    - 包含状态文本显示区域
    - 包含停止按钮
    - 包含返回应用按钮
    - 设计可拖动的布局结构
    - _需求: 4.2, 4.3, 4.4, 4.8_

  - [x] 5.2 实现 FloatingWindowManager
    - 实现 show/hide 方法
    - 实现 updateStatus 更新状态文本
    - 实现 setTaskRunning 控制按钮显示
    - 实现 setVisibility 控制可见性（截屏时隐藏）
    - 实现拖动功能
    - 实现 OnStopClickListener 回调
    - _需求: 4.1-4.8_

  - [ ]* 5.3 编写 FloatingWindowManager 属性测试
    - **Property 6: 停止任务立即生效**
    - **Property 7: 截屏时悬浮窗隐藏**
    - **验证: 需求 4.5, 4.7**

- [x] 6. 检查点 - 确保悬浮窗模块测试通过
  - 运行所有单元测试和属性测试
  - 如有问题请向用户确认

- [x] 7. 核心集成
  - [x] 7.1 实现 AutomationManager
    - 实现单例模式
    - 实现 initialize 初始化方法
    - 实现 checkPermissions 权限检查
    - 实现 startTask 启动任务
    - 实现 stopTask 停止任务
    - 实现 isRunning 状态查询
    - 实现 AutomationCallback 回调接口
    - _需求: 6.1-6.8_

  - [x] 7.2 实现自动化执行循环
    - 实现截屏→发送AI→解析动作→执行动作循环
    - 实现最大步数限制（20步）
    - 实现等待页面加载逻辑
    - 处理finish动作结束任务
    - 处理各种错误情况
    - _需求: 6.3-6.7_

  - [x] 7.3 实现权限检查和引导
    - 检查无障碍服务是否开启
    - 检查悬浮窗权限是否开启
    - 实现跳转系统设置的引导
    - _需求: 1.1, 1.2, 4.9_

- [x] 8. UI集成
  - [x] 8.1 升级 AIAssistantActivity
    - 添加模式切换（对话模式/自动化模式）
    - 显示权限状态指示器
    - 添加权限快捷开启入口
    - 显示自动化功能说明
    - 显示示例指令
    - 显示任务执行状态
    - _需求: 7.1-7.6_

  - [x] 8.2 创建权限引导对话框
    - 创建无障碍服务引导对话框
    - 创建悬浮窗权限引导对话框
    - 实现跳转系统设置功能
    - _需求: 1.2, 4.9_

  - [x] 8.3 实现手势动画反馈
    - 实现点击位置圆形动画
    - 实现滑动轨迹动画
    - 使用半透明黄色样式
    - 动画完成后自动消失
    - _需求: 9.1-9.4_

- [x] 9. 错误处理完善
  - [x] 9.1 实现错误处理逻辑
    - 处理截屏失败
    - 处理API调用失败
    - 处理动作执行失败
    - 处理无障碍服务断开
    - 处理达到最大步数限制
    - _需求: 10.1-10.5_

- [x] 10. 最终检查点 - 确保所有测试通过 ✅ 已完成
  - [x] 运行所有单元测试和属性测试 - 全部通过
  - [x] 进行手动集成测试 - 用户已在真机测试
  - [x] 修复问题：添加本应用到AppMapper映射表
    - 添加 "考研英语备考" → "com.example.mybighomework"
    - 添加 "考研英语" → "com.example.mybighomework"
    - 添加 "英语学习" → "com.example.mybighomework"
    - 添加 "英语备考" → "com.example.mybighomework"
    - 添加 "MyBigHomeWork" → "com.example.mybighomework"

## 备注

- 标记 `*` 的任务为可选测试任务，可跳过以加快MVP开发
- 每个任务都引用了具体的需求条款以确保可追溯性
- 检查点任务用于阶段性验证，确保增量开发的正确性
- 属性测试验证系统的正确性属性
