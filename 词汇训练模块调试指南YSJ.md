# 词汇训练模块调试指南

## 问题: 词汇训练页面显示固定词汇而不是词书单词

### 现象
从词书详情页点击"开始学习"后,进入词汇训练页面,但显示的是固定词汇列表(如 function, abandon 等),而不是词书中的单词。

### 可能原因

#### 1. Intent 参数未正确传递
**检查方法**: 查看 logcat 日志
```
过滤: VocabularyActivity
查找日志:
- "initVocabularyData: sourceType=..."
- "加载词书数据: bookId=..."
- "加载固定词汇列表"
```

**预期日志**:
```
VocabularyActivity: initVocabularyData: sourceType=book, bookId=xxx
VocabularyActivity: 加载词书数据: bookId=xxx, mode=learn
VocabularyActivity: loadWordsFromBook 开始: bookId=xxx, mode=learn
VocabularyActivity: 开始选择单词...
VocabularyActivity: 选择了 20 个单词
```

**如果看到**:
```
VocabularyActivity: initVocabularyData: sourceType=default, bookId=null
VocabularyActivity: 加载固定词汇列表
```
说明 Intent 参数未传递或传递错误。

#### 2. BookDetailActivity 调用代码未生效
**检查方法**: 确认 BookDetailActivity 是否使用了最新代码

**正确的调用代码**:
```java
private void startLearning() {
    Intent intent = new Intent(this, VocabularyActivity.class);
    intent.putExtra(VocabularyActivity.EXTRA_SOURCE_TYPE, VocabularyActivity.SOURCE_TYPE_BOOK);
    intent.putExtra(VocabularyActivity.EXTRA_BOOK_ID, bookId);
    intent.putExtra(VocabularyActivity.EXTRA_BOOK_NAME, bookName);
    intent.putExtra(VocabularyActivity.EXTRA_MODE, "learn");
    startActivity(intent);
}
```

**检查点**:
- 是否使用了 `VocabularyActivity.EXTRA_SOURCE_TYPE`
- 是否传递了 `VocabularyActivity.SOURCE_TYPE_BOOK`
- bookId 是否有值

#### 3. 应用未重新安装
**解决方法**: 
- 卸载旧版本应用
- 重新编译安装
- 或使用 `./gradlew clean` 清理后重新编译

### 调试步骤

#### 步骤1: 添加日志查看参数
在 `BookDetailActivity.startLearning()` 方法开头添加:
```java
android.util.Log.d("BookDetailActivity", "startLearning: bookId=" + bookId + ", bookName=" + bookName);
```

#### 步骤2: 查看 VocabularyActivity 接收到的参数
已添加日志,查看 logcat:
```
VocabularyActivity: initVocabularyData: sourceType=?, bookId=?
```

#### 步骤3: 检查 Intent 传递
在 `VocabularyActivity.onCreate()` 中添加:
```java
android.util.Log.d("VocabularyActivity", "Intent extras: " + 
    intent.getExtras().keySet());
```

### 快速修复方案

如果确认是参数传递问题,可以尝试:

#### 方案1: 直接在 BookDetailActivity 中硬编码测试
```java
private void startLearning() {
    Intent intent = new Intent(this, VocabularyActivity.class);
    intent.putExtra("source_type", "book");  // 直接使用字符串
    intent.putExtra("book_id", bookId);
    intent.putExtra("book_name", bookName);
    intent.putExtra("mode", "learn");
    
    android.util.Log.d("BookDetailActivity", "传递参数: source_type=book, bookId=" + bookId);
    startActivity(intent);
}
```

#### 方案2: 在 VocabularyActivity 中添加调试日志
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_vocabulary);

    Intent intent = getIntent();
    
    // 调试日志
    android.util.Log.d("VocabularyActivity", "=== Intent 调试 ===");
    android.util.Log.d("VocabularyActivity", "所有 extras: " + intent.getExtras());
    android.util.Log.d("VocabularyActivity", "source_type: " + intent.getStringExtra("source_type"));
    android.util.Log.d("VocabularyActivity", "EXTRA_SOURCE_TYPE: " + intent.getStringExtra(EXTRA_SOURCE_TYPE));
    android.util.Log.d("VocabularyActivity", "book_id: " + intent.getStringExtra("book_id"));
    android.util.Log.d("VocabularyActivity", "EXTRA_BOOK_ID: " + intent.getStringExtra(EXTRA_BOOK_ID));
    
    sourceType = intent.getStringExtra(EXTRA_SOURCE_TYPE);
    // ... 其余代码
}
```

### 常见问题

#### Q: 为什么总是显示固定词汇?
A: 因为 `sourceType` 为 null 或不等于 "book",导致执行了 `loadDefaultVocabulary()`

#### Q: 如何确认参数传递成功?
A: 查看日志中的 sourceType 和 bookId 值

#### Q: 如果词书ID为空怎么办?
A: 检查 BookDetailActivity 中的 bookId 是否正确获取

### 下一步操作

1. **重新编译安装应用**
2. **从词书详情页点击"开始学习"**
3. **立即查看 logcat 日志**
4. **搜索 "VocabularyActivity" 和 "BookDetailActivity"**
5. **根据日志输出判断问题所在**

### 预期结果

如果一切正常,应该看到:
- 词书中的单词(不是 function, abandon 等固定词汇)
- 标题显示词书名称
- 单词来自选择的词书

### 紧急修复

如果问题紧急,可以临时在 `VocabularyActivity` 中添加:
```java
// 临时调试代码
if (bookId != null && !bookId.isEmpty()) {
    sourceType = SOURCE_TYPE_BOOK;
    android.util.Log.d("VocabularyActivity", "强制使用词书模式");
}
```
