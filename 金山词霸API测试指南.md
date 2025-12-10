# 金山词霸API测试指南

## 📋 测试目标

验证每日一句功能已成功集成金山词霸API，能够正常获取和显示数据。

---

## 🧪 测试环境

- **Android版本**: Android 7.0 (API 24) 及以上
- **网络要求**: 需要联网
- **测试工具**: Logcat 日志查看

---

## 📝 测试步骤

### 步骤1: 清除旧数据（可选）

如果想测试首次加载，需要清除应用数据：

1. 打开手机设置
2. 找到应用管理
3. 找到"MyBigHomeWork"应用
4. 点击"存储"
5. 点击"清除数据"

### 步骤2: 启动应用并打开Logcat

```bash
# 方法1: 在Android Studio中
1. 点击底部 "Logcat" 标签
2. 在过滤器中输入: DailySentenceRepo

# 方法2: 使用命令行
adb logcat | grep DailySentenceRepo
```

### 步骤3: 打开每日一句页面

1. 启动应用
2. 点击主页的"每日一句"功能
3. 观察页面加载情况

### 步骤4: 观察日志输出

#### 成功场景1: 首次加载（从API获取）

```
D/DailySentenceRepo: 本地没有今日句子，从API获取...
D/DailySentenceRepo: 开始从金山词霸API获取数据...
D/DailySentenceRepo: API请求成功: IcibaResponse{sid='3802', content='...', note='...', translation='...', dateline='2025-10-09'}
D/DailySentenceRepo: 成功保存到数据库，ID: 1
```

#### 成功场景2: 再次打开（从本地获取）

```
D/DailySentenceRepo: 从本地数据库获取今日句子: Life is like riding a bicycle...
```

#### 失败场景: 网络异常

```
E/DailySentenceRepo: API请求异常: Unable to resolve host "open.iciba.com"
D/DailySentenceRepo: 使用默认数据，ID: 1
```

### 步骤5: 验证界面显示

检查以下内容：

- [ ] 显示英文句子
- [ ] 显示中文翻译
- [ ] 显示作者/来源
- [ ] 显示当前日期
- [ ] 词汇解析区域（可能为空）

### 步骤6: 验证数据库

使用数据库查看工具检查：

```bash
# 方法1: 使用Android Studio的Database Inspector
1. View -> Tool Windows -> App Inspection
2. 选择 Database Inspector
3. 查看 daily_sentences 表

# 方法2: 使用adb命令
adb shell
run-as com.example.mybighomework
cd databases
sqlite3 english_learning_db
SELECT * FROM daily_sentences WHERE date = date('now');
```

检查字段：
- [ ] `englishText` - 有英文内容
- [ ] `chineseText` - 有中文翻译
- [ ] `author` - 有作者信息
- [ ] `date` - 是今天的日期
- [ ] `audioUrl` - 有音频URL（http://news.iciba.com/...）
- [ ] `imageUrl` - 有图片URL（http://cdn.iciba.com/...）
- [ ] `sid` - 有句子ID

---

## 🔍 详细验证

### 1. API响应验证

#### 查看完整API响应

在 `RetrofitClient.java` 中已配置日志拦截器，可以看到完整HTTP请求和响应：

```
D/OkHttp: --> GET http://open.iciba.com/dsapi/
D/OkHttp: <-- 200 OK http://open.iciba.com/dsapi/ (234ms)
D/OkHttp: Content-Type: application/json
D/OkHttp: {"sid":"3802","tts":"http://...","content":"...","note":"..."}
```

#### 验证点

- [ ] HTTP状态码为 200
- [ ] 响应格式为JSON
- [ ] 包含必需字段: content, note, translation
- [ ] 响应时间 < 5秒

### 2. 数据转换验证

检查API数据是否正确转换为数据库实体：

```java
// API响应
content: "Life is like riding a bicycle..."
note: "生活就像骑自行车..."
translation: "《给爱因斯坦的信》"

// 数据库实体
englishText: "Life is like riding a bicycle..."
chineseText: "生活就像骑自行车..."
author: "《给爱因斯坦的信》"
```

### 3. 缓存机制验证

#### 测试场景A: 同一天多次打开

1. 打开每日一句页面（应该从API获取）
2. 返回主页
3. 再次打开每日一句页面（应该从本地获取）

**预期结果**: 
- 第一次有网络请求日志
- 第二次只有本地查询日志

#### 测试场景B: 第二天打开

1. 修改系统时间到明天
2. 打开每日一句页面

**预期结果**: 
- 重新从API获取新数据
- 数据库有两条记录

### 4. 降级策略验证

#### 测试场景C: 断网情况

1. 清除应用数据
2. 关闭网络（飞行模式）
3. 打开每日一句页面

**预期结果**:
- 显示默认句子
- 日志显示: "API请求异常"
- 日志显示: "使用默认数据"

---

## 📊 测试检查表

### 功能测试

- [ ] 首次打开能获取API数据
- [ ] 显示英文句子
- [ ] 显示中文翻译
- [ ] 显示作者/来源
- [ ] 再次打开使用本地缓存
- [ ] 日期显示正确
- [ ] 收藏功能正常

### 数据测试

- [ ] API数据正确保存到数据库
- [ ] audioUrl字段有值
- [ ] imageUrl字段有值
- [ ] sid字段有值
- [ ] 历史记录正常显示
- [ ] 数据库版本升级成功（9→10）

### 异常测试

- [ ] 网络断开时使用默认数据
- [ ] API失败时有错误日志
- [ ] 应用不会崩溃
- [ ] 用户体验流畅

### 性能测试

- [ ] API响应时间 < 5秒
- [ ] 页面加载流畅
- [ ] 无明显卡顿
- [ ] 内存使用正常

---

## 🐛 常见问题排查

### 问题1: API请求失败

**症状**: 日志显示 "API请求异常: Unable to resolve host"

**原因**: 
- 网络未连接
- DNS解析失败
- 防火墙拦截

**解决方案**:
1. 检查网络连接
2. 尝试使用移动网络
3. 检查网络安全配置

### 问题2: 明文流量被拦截

**症状**: 日志显示 "Cleartext HTTP traffic not permitted"

**原因**: Android 9以上系统默认禁止HTTP

**解决方案**: 已配置 `network_security_config.xml`，确保：
```xml
<application android:usesCleartextTraffic="true">
```

### 问题3: 数据库迁移失败

**症状**: 应用崩溃，提示数据库版本不匹配

**原因**: 数据库版本升级但迁移脚本有误

**解决方案**:
1. 卸载应用重新安装
2. 或清除应用数据

### 问题4: 显示默认数据而不是API数据

**症状**: 总是显示固定的"种一棵树"句子

**原因**: 
- API请求失败
- 数据转换错误
- 本地已有数据

**解决方案**:
1. 查看Logcat确认原因
2. 清除应用数据重试
3. 检查网络连接

---

## 📈 性能监控

### 关键指标

| 指标 | 期望值 | 监控方法 |
|-----|-------|---------|
| API响应时间 | < 3秒 | Logcat时间戳 |
| 数据库查询时间 | < 100ms | 代码计时 |
| 内存占用 | < 50MB | Android Profiler |
| 网络流量 | < 100KB | Network Profiler |

### 监控命令

```bash
# 查看网络请求
adb shell dumpsys network_stats

# 查看应用内存
adb shell dumpsys meminfo com.example.mybighomework

# 查看CPU使用
adb shell top -m 10 | grep mybighomework
```

---

## 🎯 测试用例

### 用例1: 正常流程

**前置条件**: 清除应用数据，连接网络

**操作步骤**:
1. 启动应用
2. 点击"每日一句"
3. 等待加载

**预期结果**:
- 显示来自API的英文句子和中文翻译
- 日期为今天
- 有作者信息

### 用例2: 缓存测试

**前置条件**: 已成功加载过今天的数据

**操作步骤**:
1. 返回主页
2. 关闭网络
3. 再次进入"每日一句"

**预期结果**:
- 依然能看到今天的句子
- 无网络请求
- 加载速度快

### 用例3: 降级测试

**前置条件**: 清除应用数据，断开网络

**操作步骤**:
1. 启动应用
2. 点击"每日一句"
3. 等待加载

**预期结果**:
- 显示默认句子（种一棵树...）
- 日志有降级提示
- 应用不崩溃

### 用例4: 历史记录

**前置条件**: 已有多天的数据

**操作步骤**:
1. 进入"每日一句"
2. 查看历史记录部分
3. 点击"查看全部历史"

**预期结果**:
- 显示多条历史记录
- 按日期降序排列
- 包含API获取的数据

---

## ✅ 测试结论

完成以上测试后，填写以下检查表：

### 基础功能
- [ ] API集成成功
- [ ] 数据显示正常
- [ ] 缓存机制生效
- [ ] 降级策略有效

### 数据完整性
- [ ] 所有字段都有值
- [ ] 数据格式正确
- [ ] 数据库保存成功
- [ ] 新字段已添加

### 用户体验
- [ ] 加载速度快
- [ ] 无明显bug
- [ ] 异常有提示
- [ ] 界面友好

### 技术指标
- [ ] 响应时间达标
- [ ] 内存占用正常
- [ ] 无内存泄漏
- [ ] 日志清晰

---

**测试通过即可发布使用！** 🎉

如有问题，请参考日志和错误信息进行排查。

