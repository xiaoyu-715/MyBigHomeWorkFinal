# 金山词霸API快速使用指南

## 🚀 5分钟快速开始

### 1. 同步项目

打开项目后，点击 Gradle 同步按钮，或运行：

```bash
./gradlew clean build
```

### 2. 运行应用

1. 连接手机或启动模拟器
2. 点击 Run 按钮（绿色三角形）
3. 等待应用安装完成

### 3. 测试功能

1. 打开应用
2. 点击主页的**"每日一句"**功能
3. 观察是否显示来自金山词霸的英文句子

---

## 📱 功能说明

### 自动获取数据

应用会自动判断：
- ✅ 如果本地有今天的数据 → 直接显示
- ✅ 如果本地没有今天的数据 → 从API获取
- ✅ 如果API失败 → 使用默认数据

### 数据缓存

- 每天只请求一次API
- 数据自动保存到本地数据库
- 支持离线查看历史记录

---

## 🔍 查看日志

### Android Studio中查看

1. 点击底部的 **Logcat** 标签
2. 在搜索框输入：`DailySentenceRepo`
3. 观察日志输出

### 关键日志

```
✅ 成功从API获取：
D/DailySentenceRepo: API请求成功: IcibaResponse{...}
D/DailySentenceRepo: 成功保存到数据库，ID: 1

✅ 从本地获取：
D/DailySentenceRepo: 从本地数据库获取今日句子: ...

❌ API失败（使用降级）：
E/DailySentenceRepo: API请求异常: ...
D/DailySentenceRepo: 使用默认数据，ID: 1
```

---

## 📊 数据库查看

### 使用Database Inspector

1. 菜单：**View → Tool Windows → App Inspection**
2. 选择 **Database Inspector**
3. 查看 `daily_sentences` 表
4. 检查字段：
   - `englishText` - 英文句子
   - `chineseText` - 中文翻译
   - `audioUrl` - 音频链接
   - `imageUrl` - 图片链接
   - `date` - 日期

---

## 🎯 API信息

### 金山词霸API

- **地址**: http://open.iciba.com/dsapi/
- **方法**: GET
- **无需认证**: 完全免费
- **返回格式**: JSON

### 示例响应

```json
{
  "content": "Life is like riding a bicycle.",
  "note": "生活就像骑自行车。",
  "translation": "《给爱因斯坦的信》",
  "tts": "http://news.iciba.com/admin/tts/2021-01-20-day.mp3",
  "picture": "http://cdn.iciba.com/news/word/2021-01-20.jpg"
}
```

---

## 🔧 代码示例

### 获取今日一句

```java
DailySentenceRepository repository = new DailySentenceRepository(context);

repository.getTodaySentence(sentence -> {
    // 在主线程更新UI
    runOnUiThread(() -> {
        tvEnglish.setText(sentence.getEnglishText());
        tvChinese.setText(sentence.getChineseText());
        tvAuthor.setText(sentence.getAuthor());
    });
});
```

### 强制从API获取

```java
repository.fetchTodaySentenceFromApi(sentence -> {
    // 总是从API获取最新数据
});
```

---

## ⚠️ 常见问题

### Q1: 总是显示默认数据？

**原因**: API请求失败

**解决**:
1. 检查网络连接
2. 查看Logcat日志
3. 确认网络权限已配置

### Q2: 应用崩溃？

**原因**: 数据库版本不匹配

**解决**:
1. 卸载应用
2. 重新安装
3. 或清除应用数据

### Q3: 明文流量被拦截？

**原因**: Android 9+默认禁止HTTP

**解决**: 已在`AndroidManifest.xml`中配置：
```xml
android:usesCleartextTraffic="true"
```

---

## 📁 核心文件

| 文件 | 说明 |
|------|------|
| `model/IcibaResponse.java` | API数据模型 |
| `api/DailySentenceApiService.java` | API接口定义 |
| `network/RetrofitClient.java` | 网络客户端 |
| `repository/DailySentenceRepository.java` | 业务逻辑 |
| `database/entity/DailySentenceEntity.java` | 数据库实体 |

---

## 🎉 成功标志

如果看到以下情况，说明集成成功：

1. ✅ 应用能正常打开每日一句页面
2. ✅ 显示的句子每天不同
3. ✅ Logcat有"API请求成功"日志
4. ✅ 数据库中有audioUrl和imageUrl字段
5. ✅ 历史记录中有来自API的数据

---

## 🚀 下一步

### 可选增强功能

1. **音频播放**: 使用MediaPlayer播放句子朗读
2. **图片显示**: 使用Glide加载配图
3. **分享功能**: 分享句子到社交平台
4. **收藏功能**: 收藏喜欢的句子
5. **定时推送**: 每天定时推送新句子

---

## 📞 技术支持

- 查看完整文档：`金山词霸API集成完成总结.md`
- 查看测试指南：`金山词霸API测试指南.md`
- 查看日志排查问题

---

**现在就可以使用了！** 🎊

