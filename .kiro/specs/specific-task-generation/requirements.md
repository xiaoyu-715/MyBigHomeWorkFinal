# 需求文档：AI学习计划任务具体化

## 简介

本需求文档描述了将AI生成的学习计划任务从"时间导向"升级为"目标导向"的功能需求。通过让AI生成更加具体、可量化的学习任务，用户可以清晰地知道每个任务的具体目标（如"完成20个单词训练"），而不仅仅是时间要求（如"学习20分钟"）。

## 术语表

- **DailyTaskEntity（每日任务实体）**: 数据库中存储的每日任务记录，包含任务内容、预计时长、完成状态等字段
- **TaskTemplate（任务模板）**: 阶段中定义的每日任务模板，用于生成具体的每日任务
- **目标导向任务**: 以具体可量化目标为完成标准的任务（如：完成20个单词训练）
- **时间导向任务**: 以时间为完成标准的任务（如：学习20分钟）
- **任务类型（taskType）**: 任务的主分类，对应应用的功能模块
- **任务子类型（examSubType）**: 真题练习任务的细分类型，对应具体题型
- **目标数量（targetCount）**: 任务需要完成的具体数量目标
- **操作类型（actionType）**: DailyTaskEntity中用于关联应用功能的字段
- **TaskCompletionManager**: 现有的任务完成管理器，负责跟踪词汇训练和模拟考试的进度
- **StructuredPlanPromptBuilder**: 现有的AI Prompt构建器，用于生成学习计划的提示词

## 现有功能模块分析

通过代码分析，本应用现有以下可关联的学习功能模块：

| 模块名称 | Activity | 功能描述 | 现有跟踪机制 | 可量化指标 |
|---------|----------|---------|-------------|-----------|
| 词汇训练 | VocabularyActivity | 单词选择题测试（四选一） | TaskCompletionManager.incrementVocabularyCount() | 答题数量（默认目标20题） |
| 模拟考试 | MockExamActivity | 四六级模拟考试（词汇、语法、阅读题） | TaskCompletionManager.incrementExamAnswerCount() | 答题数量（默认目标20题） |
| 真题练习 | ExamAnswerActivity | 考研英语真题练习（完形、阅读、翻译、写作） | 无现有跟踪 | 按题型分类的答题数量 |
| 错题练习 | WrongQuestionPracticeActivity | 错题复习练习 | 无现有跟踪 | 复习错题数量 |
| 每日一句 | DailySentenceActivity | 每日英语句子学习 | TaskCompletionManager.markDailySentenceCompleted() | 打开页面即完成 |

### 现有实现特点

#### 1. 词汇训练模块（VocabularyActivity）
- 每次答题调用 `TaskCompletionManager.incrementVocabularyCount()`
- 达到20题自动标记任务完成
- 支持发音播放、错题记录
- **可量化目标**：答题数量

#### 2. 模拟考试模块（MockExamActivity）
- 四六级模拟考试，30题，每题3分，90分钟时限
- 每次答题调用 `TaskCompletionManager.incrementExamAnswerCount()`
- 达到20题自动标记任务完成
- 包含词汇题、语法题、阅读理解题三类
- **可量化目标**：答题数量

#### 3. 真题练习模块（ExamAnswerActivity）
- 考研英语真题练习，3小时时限
- 题型结构：
  | 题型 | 题目数量 | 分值 | 选项类型 |
  |-----|---------|------|---------|
  | 完形填空 | 20题 | 每题0.5分，共10分 | 四选一 |
  | 阅读理解Text 1 | 5题 | 每题2分，共10分 | 四选一 |
  | 阅读理解Text 2 | 5题 | 每题2分，共10分 | 四选一 |
  | 阅读理解Text 3 | 5题 | 每题2分，共10分 | 四选一 |
  | 阅读理解Text 4 | 5题 | 每题2分，共10分 | 四选一 |
  | 新题型（标题匹配） | 5题 | 每题2分，共10分 | 七选五/八选五 |
  | 翻译 | 5段 | 共10分 | 文本输入 |
  | 写作Part A | 1篇 | 10分 | 文本输入（约100词） |
  | 写作Part B | 1篇 | 20分 | 文本输入（160-200词） |
- 支持智谱AI批改翻译和写作
- **暂无与任务系统的集成**
- **可量化目标**：按题型分别计数

#### 4. 错题练习模块（WrongQuestionPracticeActivity）
- 支持顺序/随机练习模式
- 答对自动标记为掌握
- 暂无与任务系统的集成
- **可量化目标**：复习错题数量

#### 5. 每日一句模块（DailySentenceActivity）
- 打开页面即标记完成
- 支持收藏、分享、音频播放
- **可量化目标**：打开页面即完成（布尔值）

### 现有数据结构分析

#### DailyTaskEntity 现有字段
```java
- id: int                    // 主键
- planId: int                // 关联的计划ID
- phaseId: int               // 关联的阶段ID
- date: String               // 任务日期（yyyy-MM-dd）
- taskContent: String        // 任务内容
- estimatedMinutes: int      // 预计时长（分钟）
- actualMinutes: int         // 实际时长（分钟）
- isCompleted: boolean       // 是否完成
- completedAt: long          // 完成时间戳
- taskOrder: int             // 任务顺序
- actionType: String         // 操作类型（用于关联应用功能）
```

#### 需要新增的字段
```java
- taskType: String           // 任务类型（vocabulary/mock_exam/exam_cloze/...）
- examSubType: String        // 真题练习子类型（cloze/reading_text1/...）
- targetCount: int           // 目标数量
- currentCount: int          // 当前完成数量
```

## 需求列表

### 需求1：任务内容具体化

**用户故事**: 作为用户，我希望AI生成的学习任务包含具体的学习目标，以便我清楚知道每个任务要完成什么。

#### 验收标准

1. WHEN AI生成词汇类任务 THEN 系统 SHALL 指定具体的答题数量（如：完成20道词汇训练题）
2. WHEN AI生成模拟考试任务 THEN 系统 SHALL 指定具体的答题数量（如：完成30道模拟考试题）
3. WHEN AI生成真题练习-完形填空任务 THEN 系统 SHALL 指定具体的题目数量（如：完成10道完形填空）
4. WHEN AI生成真题练习-阅读理解任务 THEN 系统 SHALL 指定具体的篇章（如：完成阅读理解Text 1的5道题）
5. WHEN AI生成真题练习-新题型任务 THEN 系统 SHALL 指定完成新题型5道标题匹配题
6. WHEN AI生成真题练习-翻译任务 THEN 系统 SHALL 指定完成翻译练习（5段英译中）
7. WHEN AI生成真题练习-写作任务 THEN 系统 SHALL 指定具体的写作部分（如：完成写作Part A邮件或Part B图表作文）
8. WHEN AI生成错题复习任务 THEN 系统 SHALL 指定具体的错题数量（如：复习15道错题）
9. WHEN AI生成每日一句任务 THEN 系统 SHALL 指定学习每日一句（打开页面即完成）
10. WHEN 任务无法关联具体模块 THEN 系统 SHALL 使用时间作为完成标准（如：学习30分钟）

### 需求2：任务类型分类

**用户故事**: 作为用户，我希望任务有明确的类型分类，以便我快速识别任务性质。

#### 验收标准

1. WHEN 创建任务实体 THEN 系统 SHALL 包含任务类型字段（taskType），支持以下值：
   - `vocabulary`: 词汇训练
   - `mock_exam`: 模拟考试
   - `exam_cloze`: 真题练习-完形填空
   - `exam_reading`: 真题练习-阅读理解
   - `exam_new_type`: 真题练习-新题型
   - `exam_translation`: 真题练习-翻译
   - `exam_writing`: 真题练习-写作
   - `wrong_question`: 错题练习
   - `daily_sentence`: 每日一句
   - `general`: 通用任务

2. WHEN 创建任务实体 THEN 系统 SHALL 包含目标数量字段（targetCount）和当前数量字段（currentCount）

3. WHEN 创建真题练习任务 THEN 系统 SHALL 包含题型子类型字段（examSubType），支持以下值：
   - `cloze`: 完形填空（20题）
   - `reading_text1`: 阅读理解Text 1（5题）
   - `reading_text2`: 阅读理解Text 2（5题）
   - `reading_text3`: 阅读理解Text 3（5题）
   - `reading_text4`: 阅读理解Text 4（5题）
   - `new_type`: 新题型-标题匹配（5题）
   - `translation`: 翻译（5段）
   - `writing_a`: 写作Part A-邮件（1篇）
   - `writing_b`: 写作Part B-图表作文（1篇）

4. WHEN 显示任务列表 THEN 系统 SHALL 根据任务类型显示对应的图标和描述

5. WHEN AI生成任务 THEN 系统 SHALL 自动识别并设置正确的任务类型、子类型和目标数量

6. WHEN 任务类型无法识别 THEN 系统 SHALL 默认设置为general类型，使用时间作为完成标准

### 需求3：任务与功能模块关联

**用户故事**: 作为用户，我希望点击任务能够直接跳转到对应的学习功能，以便快速开始学习。

#### 验收标准

1. WHEN 词汇类任务（taskType=vocabulary）被点击 THEN 系统 SHALL 跳转到VocabularyActivity词汇训练模块

2. WHEN 模拟考试任务（taskType=mock_exam）被点击 THEN 系统 SHALL 跳转到MockExamActivity模拟考试模块

3. WHEN 真题练习任务（taskType以exam_开头）被点击 THEN 系统 SHALL 跳转到ExamListActivity真题列表页面，并传递examSubType参数提示用户选择对应题型

4. WHEN 错题复习任务（taskType=wrong_question）被点击 THEN 系统 SHALL 跳转到WrongQuestionPracticeActivity错题练习模块

5. WHEN 每日一句任务（taskType=daily_sentence）被点击 THEN 系统 SHALL 跳转到DailySentenceActivity每日一句模块

6. WHEN 通用类型任务（taskType=general）被点击 THEN 系统 SHALL 显示任务详情弹窗而不跳转

### 需求4：任务目标量化显示

**用户故事**: 作为用户，我希望在任务列表中清晰看到每个任务的具体目标，以便了解学习量。

#### 验收标准

1. WHEN 显示任务卡片 THEN 系统 SHALL 展示具体的目标描述，格式如下：
   - 词汇训练：「完成20道词汇题」
   - 模拟考试：「完成30道模拟考试题」
   - 完形填空：「完成完形填空10题」
   - 阅读理解：「完成阅读理解Text 1」
   - 新题型：「完成新题型5题」
   - 翻译：「完成翻译练习」
   - 写作：「完成写作Part A」或「完成写作Part B」
   - 错题练习：「复习15道错题」
   - 每日一句：「学习每日一句」

2. WHEN 显示任务卡片 THEN 系统 SHALL 展示当前进度（如：已完成12/20）

3. WHEN 显示真题练习任务 THEN 系统 SHALL 展示具体题型名称和进度

4. WHEN 任务有关联模块 THEN 系统 SHALL 显示"点击开始学习"的提示按钮

5. WHEN 任务完成（currentCount >= targetCount） THEN 系统 SHALL 显示完成标识（绿色勾选图标）

6. WHEN 任务部分完成 THEN 系统 SHALL 显示进度条和百分比

### 需求5：AI Prompt优化

**用户故事**: 作为开发者，我希望优化AI生成学习计划的Prompt，以便生成更具体的任务内容。

#### 验收标准

1. WHEN 构建AI Prompt THEN 系统 SHALL 在StructuredPlanPromptBuilder中明确要求生成具体可量化的任务目标

2. WHEN 构建AI Prompt THEN 系统 SHALL 提供任务类型和目标格式的示例JSON，包含以下字段：
   ```json
   {
     "content": "任务描述",
     "minutes": 预计分钟数,
     "taskType": "任务类型",
     "examSubType": "真题子类型（可选）",
     "targetCount": 目标数量
   }
   ```

3. WHEN 构建AI Prompt THEN 系统 SHALL 说明可用的任务类型及其对应的目标数量建议：
   - vocabulary: 建议10-30题
   - mock_exam: 建议20-30题
   - exam_cloze: 建议5-20题
   - exam_reading: 建议1篇（5题）
   - exam_new_type: 建议5题
   - exam_translation: 建议1-5段
   - exam_writing: 建议1篇
   - wrong_question: 建议10-20题
   - daily_sentence: 固定1次

4. WHEN 构建AI Prompt THEN 系统 SHALL 说明真题练习的题型结构和分值分布

5. WHEN 解析AI响应 THEN 系统 SHALL 正确提取taskType、examSubType、targetCount等字段

6. WHEN AI响应缺少taskType字段 THEN 系统 SHALL 根据任务内容关键词智能推断任务类型

7. WHEN AI响应格式不符 THEN 系统 SHALL 使用默认值进行兜底处理（taskType=general, targetCount=0）

### 需求6：与现有TaskCompletionManager集成

**用户故事**: 作为用户，我希望任务可以通过实际完成学习目标来自动标记完成。

#### 验收标准

1. WHEN 用户在词汇模块完成答题 THEN 系统 SHALL 通过TaskCompletionManager更新对应任务的currentCount

2. WHEN 用户在模拟考试模块完成答题 THEN 系统 SHALL 通过TaskCompletionManager更新对应任务的currentCount

3. WHEN 用户在真题练习模块完成指定题型的答题 THEN 系统 SHALL 通过新增的跟踪机制更新对应任务的currentCount

4. WHEN 用户打开每日一句页面 THEN 系统 SHALL 自动将对应任务的currentCount设为1，标记任务完成

5. WHEN 任务的currentCount达到targetCount THEN 系统 SHALL 自动将isCompleted设为true

6. WHEN 用户手动勾选任务 THEN 系统 SHALL 仍然支持手动完成方式，将currentCount设为targetCount

### 需求7：真题练习任务跟踪机制

**用户故事**: 作为用户，我希望真题练习的各题型进度能够被独立跟踪，以便精确了解学习进度。

#### 验收标准

1. WHEN 用户在ExamAnswerActivity完成完形填空题 THEN 系统 SHALL 调用新增的incrementExamClozeCount()方法记录答题数量

2. WHEN 用户在ExamAnswerActivity完成阅读理解题 THEN 系统 SHALL 调用新增的incrementExamReadingCount(textNumber)方法记录答题数量，textNumber为1-4

3. WHEN 用户在ExamAnswerActivity完成新题型题 THEN 系统 SHALL 调用新增的incrementExamNewTypeCount()方法记录答题数量

4. WHEN 用户在ExamAnswerActivity提交翻译答案 THEN 系统 SHALL 调用新增的markExamTranslationCompleted()方法记录翻译完成状态

5. WHEN 用户在ExamAnswerActivity提交写作答案 THEN 系统 SHALL 调用新增的markExamWritingCompleted(partType)方法记录写作完成状态，partType为"a"或"b"

6. WHEN 真题练习任务的目标题型完成 THEN 系统 SHALL 自动标记对应任务为完成

7. WHEN 用户切换到新的一天 THEN 系统 SHALL 重置所有真题练习的进度计数

### 需求8：数据库迁移

**用户故事**: 作为开发者，我希望数据库能够平滑升级以支持新的任务字段。

#### 验收标准

1. WHEN 应用升级 THEN 系统 SHALL 执行数据库迁移，为daily_tasks表添加新字段：
   - taskType: TEXT DEFAULT 'general'
   - examSubType: TEXT DEFAULT NULL
   - targetCount: INTEGER DEFAULT 0
   - currentCount: INTEGER DEFAULT 0

2. WHEN 迁移现有数据 THEN 系统 SHALL 根据actionType字段推断taskType：
   - actionType='vocabulary' → taskType='vocabulary'
   - actionType='exam_practice' → taskType='mock_exam'
   - actionType='daily_sentence' → taskType='daily_sentence'
   - 其他 → taskType='general'

3. WHEN 迁移完成 THEN 系统 SHALL 保持现有任务数据的完整性

### 需求9：任务类型智能识别

**用户故事**: 作为用户，我希望系统能够智能识别任务内容并自动分类。

#### 验收标准

1. WHEN 任务内容包含"词汇"、"单词"、"背单词"等关键词 THEN 系统 SHALL 自动设置taskType为vocabulary

2. WHEN 任务内容包含"模拟考试"、"模拟测试"、"四级"、"六级"等关键词 THEN 系统 SHALL 自动设置taskType为mock_exam

3. WHEN 任务内容包含"完形填空"、"完形"等关键词 THEN 系统 SHALL 自动设置taskType为exam_cloze

4. WHEN 任务内容包含"阅读理解"、"阅读"、"Text"等关键词 THEN 系统 SHALL 自动设置taskType为exam_reading，并尝试识别Text编号

5. WHEN 任务内容包含"新题型"、"标题匹配"、"七选五"等关键词 THEN 系统 SHALL 自动设置taskType为exam_new_type

6. WHEN 任务内容包含"翻译"、"英译中"等关键词 THEN 系统 SHALL 自动设置taskType为exam_translation

7. WHEN 任务内容包含"写作"、"作文"、"邮件"、"图表"等关键词 THEN 系统 SHALL 自动设置taskType为exam_writing，并尝试识别Part A或Part B

8. WHEN 任务内容包含"错题"、"复习错题"等关键词 THEN 系统 SHALL 自动设置taskType为wrong_question

9. WHEN 任务内容包含"每日一句"、"每日句子"等关键词 THEN 系统 SHALL 自动设置taskType为daily_sentence

10. WHEN 无法识别任务类型 THEN 系统 SHALL 默认设置taskType为general

