# 需求文档：智谱AI批改翻译和写作功能优化

## 简介

本需求文档描述了对真题考试模块中智谱AI批改翻译和写作功能的优化改进。当前实现存在参考译文缺失、题目结构不合理、API Key检查缺失等问题，需要进行全面优化以提升批改准确性和用户体验。

## 术语表

- **ZhipuAIService**: 智谱AI API服务类，负责调用AI接口进行批改
- **ExamAnswerActivity**: 考试答题页面，包含翻译和写作题的答题与批改逻辑
- **ExamQuestion**: 考试题目数据类，存储题目内容、选项、答案等信息
- **GradeResult**: 批改结果类，包含分数和评语
- **参考译文**: 翻译题的标准答案，用于AI对比评分

## 需求

### 需求1：添加翻译题参考译文

**用户故事:** 作为考生，我希望AI批改翻译题时能参考标准译文，以便获得更准确的评分和反馈。

#### 验收标准

1. WHEN 定义翻译题目时 THE ExamQuestion类 SHALL 包含referenceAnswer字段用于存储参考译文
2. WHEN AI批改翻译题时 THE ZhipuAIService SHALL 将用户译文与参考译文一起发送给AI进行对比评分
3. WHEN 翻译题有参考译文时 THE 批改Prompt SHALL 包含完整的参考译文内容
4. THE 2025年考研英语二翻译题 SHALL 包含5个划线句子的标准中文译文

### 需求2：优化翻译题结构

**用户故事:** 作为考生，我希望翻译题能按照真实考研格式拆分为5道小题，以便分别作答和获得每题的评分。

#### 验收标准

1. WHEN 初始化考试数据时 THE 系统 SHALL 将翻译题拆分为5道独立的小题（46-50题）
2. WHEN 显示翻译题时 THE 系统 SHALL 分别显示每道小题的英文原文
3. WHEN 批改翻译题时 THE 系统 SHALL 分别批改每道小题并给出单独评分
4. THE 每道翻译小题 SHALL 满分为3分，5道共15分
5. WHEN 用户切换翻译小题时 THE 系统 SHALL 保存当前小题答案并恢复目标小题的已有答案

### 需求3：优化写作题结构

**用户故事:** 作为考生，我希望写作题能分为Part A和Part B两部分，以便分别作答和获得各部分的评分。

#### 验收标准

1. WHEN 初始化考试数据时 THE 系统 SHALL 将写作题拆分为Part A（应用文）和Part B（图表作文）两道题
2. THE Part A写作题 SHALL 满分为10分
3. THE Part B写作题 SHALL 满分为15分
4. WHEN 批改写作题时 THE 系统 SHALL 分别批改Part A和Part B并给出单独评分和评语
5. WHEN 显示成绩时 THE ExamResultActivity SHALL 分别显示Part A和Part B的得分和评语

### 需求4：增加批改重试机制

**用户故事:** 作为用户，我希望网络请求失败时系统能自动重试，以便提高批改成功率。

#### 验收标准

1. WHEN AI批改请求失败时 THE ZhipuAIService SHALL 自动重试最多2次
2. WHEN 重试时 THE 系统 SHALL 在每次重试前等待2秒
3. WHEN 所有重试都失败后 THE 系统 SHALL 使用默认评分并提示用户
4. WHEN 批改过程中 THE 进度对话框 SHALL 显示当前批改状态（包括重试信息）

### 需求5：优化评分维度和Prompt

**用户故事:** 作为考生，我希望获得更专业、更详细的批改反馈，以便了解自己的不足之处。

#### 验收标准

1. WHEN 批改翻译题时 THE Prompt SHALL 包含准确性、流畅性、用词三个维度的评分标准
2. WHEN 批改写作题时 THE Prompt SHALL 根据Part A或Part B使用不同的评分标准
3. THE Part A评分标准 SHALL 包含格式规范、内容完整、语言表达三个维度
4. THE Part B评分标准 SHALL 包含内容切题、结构清晰、语法正确、词汇丰富四个维度
5. WHEN 返回评语时 THE AI SHALL 提供具体的改进建议
