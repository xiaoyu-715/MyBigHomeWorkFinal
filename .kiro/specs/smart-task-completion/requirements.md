# 需求文档：智能任务完成识别系统

## 简介

本功能旨在让系统能够根据学习计划生成的任务内容，智能推断出用户需要执行的具体操作，并在用户完成相应操作后自动标记任务为已完成。该功能与AI学习助手生成学习计划功能深度集成，确保AI生成的任务模板也包含操作类型信息。

## 术语表

| 术语 | 说明 |
|-----|------|
| Task（任务） | 学习计划中每日生成的具体学习任务 |
| ActionType（操作类型） | 任务关联的应用功能标识符，用于自动完成和智能跳转 |
| CompletionType（完成类型） | 任务的完成判定方式：count（数量型）、duration（时长型）、simple（简单型） |
| CompletionTarget（完成目标） | 任务需要达到的具体数值（如20个单词、15分钟） |
| CurrentProgress（当前进度） | 用户已完成的数值（如已学习5个单词） |
| TaskProgressTracker | 统一的任务进度追踪器，负责追踪进度并自动完成任务 |

## 应用功能模块

| 功能名称 | Activity | actionType | completionType | 计数方式 |
|---------|----------|------------|----------------|----------|
| 每日一句 | DailySentenceActivity | daily_sentence | simple | 进入页面即完成 |
| 真题练习 | ExamListActivity → ExamAnswerActivity | real_exam | count | 每提交一套+1 |
| 模拟考试 | MockExamActivity | mock_exam | count | 每答一题+1 |
| 错题练习 | WrongQuestionPracticeActivity | wrong_question_practice | count | 每答一题+1 |
| 词汇训练 | VocabularyActivity | vocabulary_training | count | 每答一题+1 |
| 翻译练习 | TextTranslationActivity | translation_practice | count | 每翻译一次+1 |

**优先级说明**：当任务内容同时匹配多个关键词时，按上表顺序（从上到下）选择。

---

## 需求

### 需求 1：智能操作类型推断

**用户故事**：作为用户，我希望系统能根据任务内容自动识别我需要做什么操作。

#### 验收标准

| 编号 | 条件 | 结果 |
|-----|------|------|
| 1.1 | 任务包含"每日一句"、"今日一句"、"句子跟读" | actionType = "daily_sentence" |
| 1.2 | 任务包含"真题"、"考研真题"、"历年真题" | actionType = "real_exam" |
| 1.3 | 任务包含"模拟考试"、"模拟题"、"四级模拟"、"六级模拟" | actionType = "mock_exam" |
| 1.4 | 任务包含"错题"、"错题复习"、"错题巩固" | actionType = "wrong_question_practice" |
| 1.5 | 任务包含"词汇"、"单词"、"背单词"、"记单词" | actionType = "vocabulary_training" |
| 1.6 | 任务包含"翻译"、"中英互译"、"英译中"、"中译英" | actionType = "translation_practice" |
| 1.7 | 任务包含"考试"、"测试"但不含"真题"或"模拟" | actionType = "mock_exam"（默认） |
| 1.8 | 任务无法匹配任何关键词 | actionType = null，保持手动完成模式 |

### 需求 2：任务完成条件智能识别

**用户故事**：作为用户，我希望系统能根据任务内容判断完成条件，而不是进入页面就算完成。

#### 验收标准

| 编号 | 条件 | 结果 |
|-----|------|------|
| 2.1 | 任务包含"X个单词"、"X个词汇" | completionType=count, completionTarget=X |
| 2.2 | 任务包含"X道题"、"X道模拟题"、"X道错题" | completionType=count, completionTarget=X |
| 2.3 | 任务包含"X套真题"、"X套模拟题" | completionType=count, completionTarget=X |
| 2.4 | 任务包含"X个翻译"、"X句翻译" | completionType=count, completionTarget=X |
| 2.5 | 任务包含"X分钟" | completionType=duration, completionTarget=X |
| 2.6 | 任务无法解析具体数量或时长 | completionType=simple, completionTarget=1 |

### 需求 3：基于进度的任务自动完成

**用户故事**：作为用户，我希望完成学习活动达到目标后，系统能自动标记任务完成。

#### 验收标准

**3.1 词汇训练（VocabularyActivity）**
- 用户每答完一题，currentProgress +1
- currentProgress >= completionTarget 时，自动标记任务完成

**3.2 模拟考试（MockExamActivity）**
- 用户每答完一题，currentProgress +1
- currentProgress >= completionTarget 时，自动标记任务完成

**3.3 真题练习（ExamAnswerActivity）**
- 用户调用 submitExam() 提交答案时，currentProgress +1
- 中途退出不计入进度
- currentProgress >= completionTarget 时，自动标记任务完成

**3.4 每日一句（DailySentenceActivity）**
- 用户打开页面（onCreate）时，自动标记任务完成

**3.5 错题练习（WrongQuestionPracticeActivity）**
- 用户每答完一题，currentProgress +1
- currentProgress >= completionTarget 时，自动标记任务完成

**3.6 翻译练习（TextTranslationActivity）**
- 用户每完成一次翻译（saveToHistory），currentProgress +1
- currentProgress >= completionTarget 时，自动标记任务完成

**3.7 通用规则**
- 任务完成时记录 completedAt 时间戳
- 退出页面但未达目标时保存 currentProgress，下次继续累计

### 需求 4：任务完成状态和进度显示

**用户故事**：作为用户，我希望在今日任务页面能看到任务完成状态和进度。

#### 验收标准

| 编号 | 条件 | 显示内容 |
|-----|------|---------|
| 4.1 | completionType=count | "已完成 X/Y"（如"已学习 5/20 个单词"） |
| 4.2 | completionType=duration | "已练习 X/Y 分钟" |
| 4.3 | completionType=simple | 只显示完成状态（✓ 或 ○） |
| 4.4 | 用户从功能页面返回 | 刷新任务列表状态 |

### 需求 5：操作类型与功能页面映射

**用户故事**：作为用户，我希望点击任务后能直接跳转到正确的学习页面。

#### 验收标准

| actionType | 目标Activity | 提示文字 |
|------------|-------------|---------|
| vocabulary_training | VocabularyActivity | "进入词汇训练" |
| mock_exam | MockExamActivity | "进入模拟考试" |
| real_exam | ExamListActivity | "进入真题练习" |
| daily_sentence | DailySentenceActivity | "进入每日一句" |
| wrong_question_practice | WrongQuestionPracticeActivity | "进入错题练习" |
| translation_practice | TextTranslationActivity | "进入翻译练习" |
| null 或未知 | PlanDetailActivity | "查看计划详情" |

### 需求 6：已有任务的兼容处理

**用户故事**：作为用户，我希望之前创建的任务也能支持自动完成功能。

#### 验收标准

| 编号 | 条件 | 结果 |
|-----|------|------|
| 6.1 | 加载任务时 actionType 为空 | 根据任务内容推断（按需求1规则） |
| 6.2 | 加载任务时 completionType 为空 | 根据任务内容解析（按需求2规则） |
| 6.3 | 推断成功 | 更新数据库中的任务记录 |
| 6.4 | 无法推断 | 保持手动完成模式 |

### 需求 7：AI生成计划包含完成条件

**用户故事**：作为用户，我希望AI生成的任务自动包含操作类型和完成条件。

#### 验收标准

| 编号 | 要求 |
|-----|------|
| 7.1 | AI Prompt 要求返回 actionType 字段 |
| 7.2 | AI Prompt 要求返回 completionType 和 completionTarget 字段 |
| 7.3 | 解析器保存这些字段到数据库 |
| 7.4 | AI未返回时，根据任务内容智能推断 |

### 需求 8：AI生成具体可量化的任务

**用户故事**：作为用户，我希望AI生成的任务是具体的、可量化的。

#### 验收标准

| 任务类型 | 示例 | completionType | completionTarget | actionType |
|---------|------|----------------|------------------|------------|
| 词汇学习 | "学习20个新单词" | count | 20 | vocabulary_training |
| 模拟考试 | "完成20道模拟题" | count | 20 | mock_exam |
| 真题练习 | "完成1套真题" | count | 1 | real_exam |
| 每日一句 | "学习今日一句" | simple | 1 | daily_sentence |
| 错题复习 | "复习10道错题" | count | 10 | wrong_question_practice |
| 翻译练习 | "完成5个翻译" | count | 5 | translation_practice |

### 需求 9：AI生成任务必须与应用功能对应

**用户故事**：作为用户，我希望AI生成的任务都是我能在应用内完成的。

#### 验收标准

| 编号 | 要求 |
|-----|------|
| 9.1 | AI Prompt 明确告知只能生成应用支持的任务类型 |
| 9.2 | 只允许6种功能类型的任务（见应用功能模块表） |
| 9.3 | 不支持的 actionType 映射到最接近的功能 |
| 9.4 | 禁止生成：需要外部资源的任务、需要线下完成的任务、应用不支持的功能 |

### 需求 10：统一任务完成机制

**用户故事**：作为开发者，我希望有一个统一的任务完成机制，避免多套系统导致数据不一致。

#### 验收标准

| 编号 | 要求 |
|-----|------|
| 10.1 | 创建统一的 TaskProgressTracker 类 |
| 10.2 | 废弃 TaskCompletionManager 中的硬编码目标值 |
| 10.3 | 所有模块通过 TaskProgressTracker 更新进度 |
| 10.4 | TaskProgressTracker 根据任务的 completionTarget 动态判断完成 |
| 10.5 | 进度数据同时更新 SharedPreferences（快速读取）和数据库（持久化） |

---

## 现有实现状态

| 模块 | 现有实现 | 问题 | 需要修改 |
|------|---------|------|---------|
| VocabularyActivity | ✅ TaskCompletionManager + TaskCompletionHelper | 目标值硬编码20 | 改用 TaskProgressTracker |
| MockExamActivity | ✅ TaskCompletionManager + TaskCompletionHelper | actionType用"exam_practice"，目标值硬编码20 | 改为"mock_exam"，改用 TaskProgressTracker |
| DailySentenceActivity | ⚠️ 只有 TaskCompletionManager | 未更新数据库 | 添加 TaskCompletionHelper 调用 |
| ExamAnswerActivity | ❌ 无实现 | - | 新增 TaskProgressTracker 集成 |
| WrongQuestionPracticeActivity | ❌ 无实现 | - | 新增 TaskProgressTracker 集成 |
| TextTranslationActivity | ❌ 无实现 | - | 新增 TaskProgressTracker 集成 |

---

## 数据库字段扩展

DailyTaskEntity 需要新增以下字段：

| 字段名 | 类型 | 说明 |
|-------|------|------|
| completionType | String | 完成类型：count/duration/simple |
| completionTarget | int | 完成目标值 |
| currentProgress | int | 当前进度 |

