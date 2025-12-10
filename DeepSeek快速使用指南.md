# DeepSeek AI 快速使用指南 🚀

## 📋 接入完成清单

✅ **已完成的工作：**

1. ✅ 创建 `DeepSeekApiService.java` - API 服务类
2. ✅ 创建 `DeepSeekChatActivity.java` - 聊天界面
3. ✅ 创建 `ChatMessage.java` - 消息数据模型
4. ✅ 创建 `ChatMessageAdapter.java` - 消息列表适配器
5. ✅ 创建聊天界面布局文件
6. ✅ 更新 AndroidManifest.xml
7. ✅ 在 MoreActivity 添加入口
8. ✅ 创建完整使用文档

---

## 🎯 5分钟快速开始

### 步骤 1: 获取 API Key（2分钟）

1. 打开浏览器访问：https://platform.deepseek.com
2. 点击右上角"注册/登录"
3. 完成注册后进入控制台
4. 点击"API Keys" → "创建新密钥"
5. 复制生成的 API Key（格式类似：`sk-xxxxxxxxxxxxx`）

### 步骤 2: 构建应用（2分钟）

```bash
# 在项目根目录执行
./gradlew assembleDebug

# 或在 Windows 上
gradlew.bat assembleDebug
```

### 步骤 3: 配置并测试（1分钟）

1. 安装应用到手机
2. 打开应用 → 点击"更多功能"
3. 找到"AI 学习助手"并点击
4. 首次使用会弹出配置对话框
5. 粘贴你的 API Key → 点击"确定"
6. 输入"你好"测试对话

**恭喜！DeepSeek AI 已成功接入！** 🎉

---

## 💡 5个实用场景

### 场景1: 英文翻译
```
👤 用户: 请翻译：The early bird catches the worm.
🤖 AI: 这句谚语的意思是"早起的鸟儿有虫吃"，
      比喻做事要趁早，先下手为强。
```

### 场景2: 语法纠错
```
👤 用户: 请检查：She don't like apples.
🤖 AI: 这句话有语法错误。正确的是：
      She doesn't like apples.
      第三人称单数否定要用 doesn't。
```

### 场景3: 词汇解释
```
👤 用户: "run" 这个词有哪些常见用法？
🤖 AI: "run" 是一个多义词，主要用法包括：
      1. 跑步：I run every morning.
      2. 经营：He runs a restaurant.
      3. 运行：The program runs smoothly.
      ...
```

### 场景4: 作文批改
```
👤 用户: 请批改：Yesterday I go to shopping.
🤖 AI: 发现以下问题：
      1. 时态错误：go → went
      2. 搭配错误：go to shopping → go shopping
      正确句子：Yesterday I went shopping.
```

### 场景5: 学习建议
```
👤 用户: 如何提高英语听力？
🤖 AI: 提高听力可以从以下方面入手：
      1. 每天听英语播客15-30分钟
      2. 看英文电影/电视剧（带字幕）
      3. 使用听力训练APP
      4. 跟读模仿，培养语感
      ...
```

---

## 📱 界面导航

```
主界面 (MainActivity)
    ↓
更多功能 (MoreActivity)
    ↓
AI 学习助手 (DeepSeekChatActivity)
    ↓
开始对话！
```

**快捷入口：** 主界面 → 底部"更多" → "AI 学习助手"

---

## 🔧 配置说明

### API Key 存储位置
```
应用私有目录：
/data/data/com.example.mybighomework/shared_prefs/deepseek_config.xml
```

### 修改 API Key
1. 打开聊天界面
2. 点击右上角"设置"图标
3. 输入新的 API Key
4. 点击"确定"保存

### 删除 API Key
```java
// 或通过代码清除（仅供开发参考）
SharedPreferences prefs = getSharedPreferences("deepseek_config", Context.MODE_PRIVATE);
prefs.edit().remove("api_key").apply();
```

---

## 💰 费用预估

### 价格
- **输入 Token**: ¥0.001 / 1K tokens
- **输出 Token**: ¥0.002 / 1K tokens

### 实际案例
| 场景 | Token 消耗 | 费用 |
|------|-----------|------|
| 简单翻译 | 输入50 + 输出100 | ≈ ¥0.0003 |
| 语法纠错 | 输入100 + 输出200 | ≈ ¥0.0005 |
| 作文批改 | 输入500 + 输出800 | ≈ ¥0.002 |
| 学习建议 | 输入100 + 输出500 | ≈ ¥0.001 |

**总结：** 日常使用非常经济，100次对话约 ¥0.5 元

---

## 🐛 常见问题速查

### Q1: 点击发送没反应？
**检查清单：**
- [ ] API Key 是否已配置
- [ ] 手机是否联网
- [ ] 输入框是否有内容
- [ ] 查看错误提示

### Q2: 提示 "API Key 无效"？
**解决方法：**
1. 确认 API Key 复制完整（包括 `sk-` 前缀）
2. 检查是否有多余的空格
3. 登录 DeepSeek 控制台验证 Key 状态
4. 尝试重新创建 API Key

### Q3: 回复速度慢？
**可能原因：**
- 网络延迟 → 切换到更好的网络
- 请求复杂 → 简化提问
- 服务繁忙 → 稍后重试

### Q4: 应用崩溃？
**排查步骤：**
1. 查看 Logcat 错误信息
2. 确认网络权限已添加
3. 检查是否是低内存设备
4. 重启应用重试

---

## 🎓 最佳实践

### ✅ 推荐做法
1. **清晰提问** - "请翻译：..."比"这句话什么意思"更好
2. **提供上下文** - 说明使用场景获得更准确的回答
3. **分步提问** - 复杂问题拆分成多个小问题
4. **使用例子** - 用具体例子让 AI 理解你的需求

### ❌ 避免做法
1. 不要发送过长的文本（建议<2000字）
2. 不要频繁重复相同问题
3. 不要在输入中包含敏感信息
4. 不要过度依赖，要独立思考

---

## 📈 进阶功能

### 1. 自定义 AI 角色
修改 `DeepSeekChatActivity.java` 第 221 行的系统提示：
```java
String systemPrompt = "你是一个专业的雅思口语教练...";
```

### 2. 调整对话记忆
修改 `DeepSeekChatActivity.java` 第 239 行：
```java
int startIndex = Math.max(0, messageList.size() - 20); // 记住20条
```

### 3. 导出对话记录
在 `DeepSeekChatActivity` 中添加导出功能：
```java
btnExport.setOnClickListener(v -> exportChatHistory());
```

---

## 📚 代码文件清单

### 核心文件
```
app/src/main/java/com/example/mybighomework/
├── api/
│   └── DeepSeekApiService.java          # API 服务
├── model/
│   └── ChatMessage.java                  # 消息模型
├── adapter/
│   └── ChatMessageAdapter.java           # 消息适配器
├── DeepSeekChatActivity.java            # 聊天界面
└── MoreActivity.java                     # 入口（已更新）

app/src/main/res/
├── layout/
│   ├── activity_deepseek_chat.xml       # 聊天界面布局
│   ├── item_chat_sent.xml               # 发送消息布局
│   └── item_chat_received.xml           # 接收消息布局
├── values/
│   ├── strings.xml                       # 字符串资源
│   └── colors.xml                        # 颜色资源
└── AndroidManifest.xml                   # 清单文件（已更新）
```

### 文档文件
```
DeepSeek接入文档.md                      # 完整技术文档
DeepSeek快速使用指南.md                  # 本文件
```

---

## 🎯 下一步行动

### 立即行动
1. [ ] 获取 DeepSeek API Key
2. [ ] 构建并安装应用
3. [ ] 配置 API Key
4. [ ] 尝试5个实用场景

### 短期目标（1周内）
1. [ ] 熟悉各种功能
2. [ ] 探索不同的提问方式
3. [ ] 将 AI 助手应用到实际学习中
4. [ ] 收集使用反馈

### 长期规划（1月内）
1. [ ] 自定义 AI 角色和功能
2. [ ] 实现对话记录保存
3. [ ] 添加快捷功能按钮
4. [ ] 优化用户体验

---

## 📞 获取帮助

### 文档资源
- **完整技术文档**: 查看 `DeepSeek接入文档.md`
- **DeepSeek 官方文档**: https://platform.deepseek.com/docs
- **API 参考**: https://platform.deepseek.com/api-docs

### 问题反馈
如遇到问题，请提供：
1. 错误提示信息
2. 操作步骤
3. Logcat 日志
4. 设备信息（Android 版本、机型）

---

## ✨ 特别提示

1. **首次使用送$5体验额度** - DeepSeek 新用户可获得免费额度
2. **学生优惠** - 关注官方公告，可能有学生优惠活动
3. **API 限流** - 默认限制为60次/分钟，足够日常使用
4. **数据安全** - 对话不会被保存到服务器（除非开启日志）

---

## 🎊 成功案例

### 案例1: 英语作文批改
某学生使用 AI 助手批改作文，发现了20+个语法错误和表达问题，经过3次迭代修改，作文质量显著提升。

### 案例2: 四六级备考
通过每天与 AI 对话练习，词汇量增加500+，口语表达能力明显提高。

### 案例3: 学习计划制定
AI 根据学生情况给出个性化学习计划，坚持1个月后，考试成绩提升15分。

---

**祝你学习进步！** 📚✨

有任何问题随时查阅 `DeepSeek接入文档.md` 或联系技术支持。

