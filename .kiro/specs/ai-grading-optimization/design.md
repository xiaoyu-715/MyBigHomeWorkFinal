# 设计文档：智谱AI批改翻译和写作功能优化

## 概述

本设计文档描述了对真题考试模块中智谱AI批改功能的优化方案。主要包括：添加翻译题参考译文、拆分翻译和写作题结构、增加批改重试机制、优化评分Prompt等改进。

## 架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    ExamAnswerActivity                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ 翻译题显示   │  │ 写作题显示   │  │ 批改流程控制         │  │
│  │ (5道小题)   │  │ (Part A/B)  │  │                     │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
│         │                │                     │             │
│         └────────────────┼─────────────────────┘             │
│                          │                                   │
│                          ▼                                   │
│              ┌───────────────────────┐                       │
│              │   ZhipuAIService      │                       │
│              │  ┌─────────────────┐  │                       │
│              │  │ gradeTranslation│  │                       │
│              │  │ (带参考译文)    │  │                       │
│              │  ├─────────────────┤  │                       │
│              │  │ gradeWritingA   │  │                       │
│              │  │ (应用文评分)    │  │                       │
│              │  ├─────────────────┤  │                       │
│              │  │ gradeWritingB   │  │                       │
│              │  │ (图表作文评分)  │  │                       │
│              │  ├─────────────────┤  │                       │
│              │  │ 重试机制        │  │                       │
│              │  │ (最多2次)       │  │                       │
│              │  └─────────────────┘  │                       │
│              └───────────────────────┘                       │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │   ExamResultEntity    │
              │  ┌─────────────────┐  │
              │  │ translation1-5  │  │
              │  │ Score/Comment   │  │
              │  ├─────────────────┤  │
              │  │ writingAScore   │  │
              │  │ writingAComment │  │
              │  ├─────────────────┤  │
              │  │ writingBScore   │  │
              │  │ writingBComment │  │
              │  └─────────────────┘  │
              └───────────────────────┘
```

## 组件和接口

### 1. ExamQuestion类扩展

```java
private static class ExamQuestion {
    QuestionType type;
    String title;
    String passage;           // 英文原文
    String question;          // 题目要求
    String[] options;         // 选项（选择题用）
    int correctAnswer;        // 正确答案索引
    String explanation;       // 答案解析
    String referenceAnswer;   // 新增：参考答案（翻译题的标准译文）
    String writingType;       // 新增：写作类型（"PART_A" 或 "PART_B"）
}
```

### 2. ZhipuAIService接口扩展

```java
// 批改翻译（带参考译文和重试机制）
public void gradeTranslationWithRetry(
    String userTranslation, 
    String referenceTranslation,
    String originalText,
    int maxRetries,
    GradeCallback callback
);

// 批改写作Part A（应用文）
public void gradeWritingPartA(
    String essay, 
    String topic,
    int maxRetries,
    GradeCallback callback
);

// 批改写作Part B（图表作文）
public void gradeWritingPartB(
    String essay, 
    String topic,
    int maxRetries,
    GradeCallback callback
);
```

### 3. ExamResultEntity扩展

```java
// 翻译题分项得分（5道小题）
private float translation1Score;
private float translation2Score;
private float translation3Score;
private float translation4Score;
private float translation5Score;
private String translationComment;  // 整体评语

// 写作题分项得分
private float writingAScore;        // Part A得分（满分10分）
private String writingAComment;     // Part A评语
private float writingBScore;        // Part B得分（满分15分）
private String writingBComment;     // Part B评语
```

## 数据模型

### 翻译题数据结构（5道小题）

| 题号 | 英文原文 | 参考译文 | 满分 |
|------|----------|----------|------|
| 46 | Recent decades have seen science... | 近几十年来，科学已经形成了一种惯例... | 3分 |
| 47 | But by utilising the natural curiosity... | 但是，通过利用公众的天然好奇心... | 3分 |
| 48 | Scientists have employed a variety... | 科学家们采用了多种方式... | 3分 |
| 49 | These groups of people are part... | 这些人群是快速扩张的... | 3分 |
| 50 | They pool resources, collaborate... | 他们汇集资源、合作... | 3分 |

### 写作题数据结构

| 题号 | 类型 | 题目要求 | 满分 |
|------|------|----------|------|
| 51 | Part A | 邮件回复（约100词） | 10分 |
| 52 | Part B | 图表作文（160-200词） | 15分 |

## 正确性属性

*正确性属性是指在所有有效执行中都应该保持为真的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### Property 1: 翻译批改包含参考译文

*对于任意* 翻译题批改请求，发送给AI的prompt应该包含用户译文和参考译文两部分内容

**验证: 需求 1.2, 1.3**

### Property 2: 翻译题分数范围正确

*对于任意* 翻译小题的批改结果，分数应该在0-3分范围内，5道小题总分不超过15分

**验证: 需求 2.3, 2.4**

### Property 3: 写作题使用不同评分标准

*对于任意* 写作题批改请求，Part A和Part B应该使用不同的评分Prompt和分数上限

**验证: 需求 3.4, 5.2**

### Property 4: 批改重试机制正确执行

*对于任意* 批改请求失败的情况，系统应该最多重试2次，每次间隔2秒，最终失败时使用默认评分

**验证: 需求 4.1, 4.2, 4.3**

### Property 5: 答案切换时状态保持

*对于任意* 翻译小题切换操作，切换前的答案应该被保存，切换后应该恢复目标题目的已有答案

**验证: 需求 2.5**

## 错误处理

### 网络错误处理

1. **首次请求失败**: 等待2秒后自动重试
2. **第一次重试失败**: 再等待2秒后进行第二次重试
3. **所有重试失败**: 
   - 翻译题：每道小题给予默认2分（共10分）
   - 写作Part A：给予默认6分
   - 写作Part B：给予默认10分
   - 评语显示"AI批改失败，系统给予默认分数"

### JSON解析错误处理

1. 尝试提取```json代码块中的内容
2. 尝试查找第一个{和最后一个}之间的内容
3. 解析失败时使用默认评分

## 测试策略

### 单元测试

1. **ExamQuestion类测试**
   - 验证referenceAnswer字段正确存储和读取
   - 验证writingType字段正确区分Part A和Part B

2. **ZhipuAIService测试**
   - 验证翻译批改Prompt包含参考译文
   - 验证Part A和Part B使用不同的Prompt
   - 验证重试机制正确执行

3. **分数计算测试**
   - 验证翻译题每道小题分数在0-3范围内
   - 验证Part A分数在0-10范围内
   - 验证Part B分数在0-15范围内

### 属性测试

1. **Property 1测试**: 生成随机翻译内容，验证批改Prompt包含参考译文
2. **Property 2测试**: 生成随机批改结果，验证分数范围正确
3. **Property 3测试**: 分别调用Part A和Part B批改，验证Prompt不同
4. **Property 4测试**: 模拟网络失败，验证重试次数和间隔
5. **Property 5测试**: 模拟题目切换，验证答案保存和恢复
