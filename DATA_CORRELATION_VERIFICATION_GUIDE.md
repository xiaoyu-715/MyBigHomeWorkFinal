# 数据关联验证指南

## 概述

本指南详细说明了如何验证你的英语学习应用中各种功能之间的数据关联性。通过系统性的验证，确保数据的一致性和完整性。

## 数据关联架构图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  VocabularyRecord│    │   QuestionEntity│    │  StudyRecord    │
│                 │    │                 │    │                 │
│ id (PK)         │◄───┤relatedVocabularyId│◄───┤vocabularyId (FK)│
│ word            │    │ id (PK)         │◄───┤questionId (FK)  │
│ meaning         │    │ questionText    │    │ studyType       │
│ ...             │    │ ...             │    │ ...             │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────┐
                                              │  ExamRecord     │
                                              │                 │
                                              │ examType        │
                                              │ totalQuestions  │
                                              │ correctAnswers  │
                                              │ ...             │
                                              └─────────────────┘
```

## 核心关联关系

### 1. 词汇与题目的关联

**关联方式：**
- `QuestionEntity.relatedVocabularyId` → `VocabularyRecordEntity.id`
- 通过关键词搜索自动关联

**验证方法：**
```java
// 1. 创建词汇记录
VocabularyRecordEntity vocabulary = new VocabularyRecordEntity();
vocabulary.setWord("abandon");
long vocabularyId = vocabularyDao.insert(vocabulary);

// 2. 创建关联题目
QuestionEntity question = new QuestionEntity();
question.setRelatedVocabularyId((int) vocabularyId);
long questionId = questionDao.insert(question);

// 3. 验证关联
QuestionEntity retrieved = questionDao.getQuestionById((int) questionId);
assertEquals(vocabularyId, (long) retrieved.getRelatedVocabularyId());
```

### 2. 学习记录的双重关联

**关联方式：**
- `StudyRecordEntity.questionId` → `QuestionEntity.id` (外键约束)
- `StudyRecordEntity.vocabularyId` → `VocabularyRecordEntity.id` (外键约束)

**验证方法：**
```java
// 创建学习记录时自动关联
StudyRecordEntity studyRecord = new StudyRecordEntity();
studyRecord.setQuestionId(questionId);
studyRecord.setVocabularyId(vocabularyId);
studyRecordDao.insertStudyRecord(studyRecord);

// 验证级联查询
List<StudyRecordEntity> questionRecords = 
    studyRecordDao.getStudyRecordsByQuestionId(questionId);
List<StudyRecordEntity> vocabularyRecords = 
    studyRecordDao.getStudyRecordsByVocabularyId(vocabularyId);
```

### 3. 考试记录的间接关联

**关联方式：**
- 通过 `examType` 字段关联
- 通过 `sessionId` 关联同一次考试的所有学习记录

**验证方法：**
```java
// 创建考试记录
ExamRecordEntity examRecord = new ExamRecordEntity();
examRecord.setExamType("CET4");

// 创建相关学习记录
String sessionId = "exam_session_" + System.currentTimeMillis();
StudyRecordEntity studyRecord = new StudyRecordEntity();
studyRecord.setExamType("CET4");
studyRecord.setSessionId(sessionId);

// 验证关联查询
List<StudyRecordEntity> examRecords = 
    studyRecordDao.getStudyRecordsByExamType("CET4");
List<StudyRecordEntity> sessionRecords = 
    studyRecordDao.getStudyRecordsBySessionId(sessionId);
```

## 验证测试用例

### 测试1：词汇题目关联验证
- **目的：** 验证词汇记录与题目之间的正确关联
- **步骤：** 创建词汇 → 创建关联题目 → 验证关联关系 → 测试反向查询

### 测试2：学习记录关联验证
- **目的：** 验证学习记录与词汇、题目的外键关联
- **步骤：** 创建词汇和题目 → 创建学习记录 → 验证外键关联 → 测试级联查询

### 测试3：考试记录关联验证
- **目的：** 验证考试记录与学习数据的间接关联
- **步骤：** 创建考试记录 → 创建相关学习记录 → 验证关联查询 → 检查统计一致性

### 测试4：数据联动服务验证
- **目的：** 验证DataLinkageService的完整数据关联功能
- **步骤：** 使用服务记录学习活动 → 验证自动关联 → 检查数据更新

### 测试5：数据完整性约束验证
- **目的：** 验证外键约束和级联删除
- **步骤：** 创建关联数据 → 删除父记录 → 验证级联删除效果

## 运行验证测试

### 1. 单元测试运行
```bash
# 运行所有数据关联测试
./gradlew test --tests DataCorrelationTest

# 运行特定测试
./gradlew test --tests DataCorrelationTest.testVocabularyQuestionCorrelation
```

### 2. 集成测试运行
```bash
# 运行Android集成测试
./gradlew connectedAndroidTest --tests DataCorrelationTest
```

## 手动验证步骤

### 1. 数据库查询验证
```sql
-- 查看词汇与题目的关联
SELECT v.word, q.questionText 
FROM vocabulary_records v 
JOIN questions q ON v.id = q.relatedVocabularyId;

-- 查看学习记录的完整关联
SELECT sr.*, v.word, q.questionText 
FROM study_records sr 
LEFT JOIN vocabulary_records v ON sr.vocabularyId = v.id 
LEFT JOIN questions q ON sr.questionId = q.id;

-- 查看考试统计与学习记录的一致性
SELECT 
    er.examType,
    er.correctAnswers as exam_correct,
    COUNT(CASE WHEN sr.isCorrect = 1 THEN 1 END) as record_correct
FROM exam_records er 
LEFT JOIN study_records sr ON er.examType = sr.examType 
GROUP BY er.id;
```

### 2. 应用内验证
1. **词汇学习验证：**
   - 学习词汇后检查相关题目是否出现
   - 答题后检查词汇掌握状态是否更新

2. **题目练习验证：**
   - 答题后检查学习记录是否正确创建
   - 检查词汇进度是否同步更新

3. **考试模式验证：**
   - 完成模拟考试后检查考试记录
   - 验证学习记录中的sessionId是否一致
   - 检查统计数据是否匹配

## 常见问题排查

### 1. 关联数据不一致
**症状：** 学习记录存在但关联的词汇或题目不存在
**排查：** 检查外键约束是否正确设置，验证数据插入顺序

### 2. 统计数据不匹配
**症状：** 考试记录的统计与实际学习记录不符
**排查：** 检查sessionId是否正确设置，验证统计计算逻辑

### 3. 级联删除失效
**症状：** 删除父记录后子记录仍然存在
**排查：** 检查外键约束的onDelete设置，确认Room配置正确

## 数据一致性维护

### 1. 事务处理
```java
@Transaction
public void recordStudyActivityWithTransaction(/* parameters */) {
    // 确保所有相关数据更新在同一事务中
    studyRecordDao.insertStudyRecord(studyRecord);
    vocabularyDao.updateVocabulary(vocabulary);
    questionDao.updateQuestion(question);
}
```

### 2. 数据验证
```java
public boolean validateDataConsistency() {
    // 验证所有学习记录都有有效的关联
    List<StudyRecordEntity> orphanRecords = 
        studyRecordDao.getOrphanStudyRecords();
    return orphanRecords.isEmpty();
}
```

### 3. 定期检查
- 定期运行数据一致性检查
- 监控外键约束违反情况
- 检查统计数据的准确性

## 性能优化建议

### 1. 索引优化
- 在外键字段上创建索引
- 为常用查询字段创建复合索引

### 2. 查询优化
- 使用JOIN查询减少数据库访问次数
- 合理使用分页查询大量数据

### 3. 缓存策略
- 缓存常用的关联查询结果
- 使用LiveData观察数据变化

通过以上验证方法和测试用例，你可以全面验证应用中各功能之间的数据关联性，确保数据的一致性和完整性。