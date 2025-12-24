# 词汇训练功能整合计划

## 目标
将所有与单词训练相关的功能整合到 `VocabularyActivity` 中,统一使用 `vocabulary_training` 作为任务类型。

## 当前状态分析

### BookLearningActivity (词书学习)
- **数据源**: 从词书数据库加载 `DictionaryWordEntity`
- **功能**: 支持学习模式和复习模式
- **进度追踪**: 使用 `vocabulary_training`
- **数据保存**: `WordLearningProgressEntity`
- **入口点**: `BookDetailActivity`

### VocabularyActivity (词汇训练)
- **数据源**: 固定词汇列表
- **功能**: 词汇选择题训练
- **进度追踪**: 使用 `vocabulary_training`
- **数据保存**: `VocabularyRecordEntity`, `StudyRecordEntity`
- **入口点**: `MainActivity`, `DailyTaskActivity`

## 整合方案

### 1. 扩展 VocabularyActivity 支持多种数据源

#### 1.1 添加数据源类型参数
```java
public static final String EXTRA_SOURCE_TYPE = "source_type";
public static final String SOURCE_TYPE_DEFAULT = "default";  // 固定词汇列表
public static final String SOURCE_TYPE_BOOK = "book";        // 词书学习
public static final String EXTRA_BOOK_ID = "book_id";
public static final String EXTRA_BOOK_NAME = "book_name";
public static final String EXTRA_MODE = "mode";              // learn/review
```

#### 1.2 修改数据加载逻辑
- 根据 `source_type` 选择不同的数据加载方式
- `SOURCE_TYPE_DEFAULT`: 使用原有的固定词汇列表
- `SOURCE_TYPE_BOOK`: 从词书数据库加载单词

#### 1.3 统一数据模型
- 扩展 `VocabularyItem` 类,支持 `DictionaryWordEntity` 的转换
- 或创建适配器模式,统一两种数据源

### 2. 整合学习进度保存

#### 2.1 保留两种进度保存方式
- 词书学习: 保存到 `WordLearningProgressEntity`
- 固定词汇: 保存到 `VocabularyRecordEntity`

#### 2.2 统一任务进度追踪
- 所有模式都使用 `TaskProgressTracker.recordProgress("vocabulary_training", 1)`

### 3. 更新所有入口点

#### 3.1 BookDetailActivity
```java
// 原来调用 BookLearningActivity
Intent intent = new Intent(this, BookLearningActivity.class);

// 改为调用 VocabularyActivity
Intent intent = new Intent(this, VocabularyActivity.class);
intent.putExtra(VocabularyActivity.EXTRA_SOURCE_TYPE, VocabularyActivity.SOURCE_TYPE_BOOK);
intent.putExtra(VocabularyActivity.EXTRA_BOOK_ID, bookId);
intent.putExtra(VocabularyActivity.EXTRA_BOOK_NAME, bookName);
intent.putExtra(VocabularyActivity.EXTRA_MODE, "learn");
```

#### 3.2 其他入口点
- `MainActivity`: 保持原有调用方式
- `DailyTaskActivity`: 保持原有调用方式
- `ActionTypeInferrer`: 确保 `vocabulary_training` 映射到 `VocabularyActivity`

### 4. 修复日期匹配问题

#### 4.1 问题分析
日志显示查询日期为 2025-12-24,但当前日期应该是 2025-12-25。可能的原因:
- 时区问题
- 任务创建时间与查询时间不一致

#### 4.2 解决方案
- 确保任务创建和查询使用相同的日期格式和时区
- 添加日志记录,追踪日期生成过程

### 5. 代码清理

#### 5.1 保留 BookLearningActivity (暂时)
- 标记为 `@Deprecated`
- 添加注释说明已迁移到 `VocabularyActivity`
- 后续版本可以删除

#### 5.2 更新文档
- 更新 README
- 更新开发文档

## 实施步骤

1. ✅ 创建整合计划文档
2. ⏳ 扩展 VocabularyActivity 支持词书数据源
3. ⏳ 更新 BookDetailActivity 的调用
4. ⏳ 测试词书学习功能
5. ⏳ 修复日期匹配问题
6. ⏳ 全面测试所有入口点
7. ⏳ 标记 BookLearningActivity 为废弃

## 注意事项

- 保持向后兼容性
- 确保所有现有功能正常工作
- 统一任务进度追踪逻辑
- 保留详细的日志记录,便于调试

## 预期效果

- 所有单词训练功能统一在 `VocabularyActivity` 中
- 任务进度追踪统一使用 `vocabulary_training`
- 代码结构更清晰,易于维护
- 用户体验一致
