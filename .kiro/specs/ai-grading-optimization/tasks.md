# 实现计划：智谱AI批改翻译和写作功能优化

## 概述

本实现计划将智谱AI批改功能的优化设计转化为具体的编码任务，按照增量开发的方式逐步实现各项功能。

## 任务

- [x] 1. 扩展ExamQuestion类支持参考答案
  - [x] 1.1 在ExamQuestion类中添加referenceAnswer字段
    - 添加String类型的referenceAnswer成员变量
    - 更新构造函数支持referenceAnswer参数
    - _需求: 1.1_
  - [x] 1.2 在ExamQuestion类中添加writingType字段
    - 添加String类型的writingType成员变量（"PART_A"或"PART_B"）
    - 更新构造函数支持writingType参数
    - _需求: 3.1_

- [x] 2. 重构翻译题数据结构
  - [x] 2.1 将翻译题拆分为5道独立小题
    - 创建5个独立的ExamQuestion对象（题号46-50）
    - 每道题包含独立的英文原文段落
    - _需求: 2.1, 2.2_
  - [x] 2.2 添加2025年考研英语二翻译题参考译文
    - 为每道翻译小题添加标准中文译文
    - 确保译文准确、专业
    - _需求: 1.4_

- [x] 3. 重构写作题数据结构
  - [x] 3.1 将写作题拆分为Part A和Part B两道题
    - 创建Part A题目（题号51，应用文）
    - 创建Part B题目（题号52，图表作文）
    - 设置writingType字段区分类型
    - _需求: 3.1_

- [x] 4. 优化ZhipuAIService批改功能
  - [x] 4.1 实现带重试机制的批改方法
    - 添加gradeWithRetry通用方法
    - 实现最多2次重试，每次间隔2秒
    - 所有重试失败后返回默认评分
    - _需求: 4.1, 4.2, 4.3_
  - [x] 4.2 优化翻译题批改Prompt
    - 修改gradeTranslation方法接收参考译文参数
    - 更新Prompt包含英文原文、参考译文、用户译文
    - 调整评分标准为满分3分
    - _需求: 1.2, 1.3, 5.1_
  - [x] 4.3 实现Part A写作批改方法
    - 创建gradeWritingPartA方法
    - 设计应用文专用评分Prompt（格式规范、内容完整、语言表达）
    - 满分设置为10分
    - _需求: 3.2, 5.3_
  - [x] 4.4 实现Part B写作批改方法
    - 创建gradeWritingPartB方法
    - 设计图表作文专用评分Prompt（内容切题、结构清晰、语法正确、词汇丰富）
    - 满分设置为15分
    - _需求: 3.3, 5.4_

- [x] 5. 更新ExamAnswerActivity批改流程
  - [x] 5.1 更新翻译题显示逻辑
    - 修改showTranslationQuestion方法支持多道翻译小题
    - 实现翻译小题切换时的答案保存和恢复
    - _需求: 2.2, 2.5_
  - [x] 5.2 更新写作题显示逻辑
    - 修改showWritingQuestion方法区分Part A和Part B
    - 实现写作题切换时的答案保存和恢复
    - _需求: 3.1_
  - [x] 5.3 重构gradeTranslationAndWriting方法
    - 分别批改5道翻译小题
    - 分别批改Part A和Part B写作题
    - 更新进度对话框显示批改状态
    - _需求: 2.3, 3.4, 4.4_
  - [x] 5.4 更新finishGrading方法计算总分
    - 计算翻译题总分（5道小题之和，满分15分）
    - 计算写作题总分（Part A + Part B，满分25分）
    - 更新总分计算公式
    - _需求: 2.4, 3.2, 3.3_

- [x] 6. 更新ExamResultEntity和数据库
  - [ ] 6.1 扩展ExamResultEntity字段
    - 添加translation1-5Score字段
    - 添加writingAScore、writingAComment字段
    - 添加writingBScore、writingBComment字段
    - _需求: 3.5_
  - [ ] 6.2 更新数据库迁移
    - 创建数据库迁移脚本添加新字段
    - 更新数据库版本号
    - _需求: 3.5_

- [ ] 7. 更新ExamResultActivity成绩显示
  - [ ] 7.1 更新布局文件显示分项得分
    - 添加翻译题分项得分显示区域
    - 添加Part A和Part B分别显示区域
    - _需求: 3.5_
  - [ ] 7.2 更新displayExamResult方法
    - 显示翻译题各小题得分
    - 分别显示Part A和Part B得分和评语
    - _需求: 3.5_

- [ ] 8. 检查点 - 确保所有测试通过
  - 运行应用验证翻译题拆分正确
  - 验证写作题Part A和Part B分别显示
  - 验证AI批改功能正常工作
  - 验证成绩页面正确显示分项得分
  - 如有问题请询问用户

## 备注

- 任务按照依赖关系排序，需要按顺序执行
- 每个任务完成后应进行基本功能验证
- 检查点任务用于确保阶段性成果的正确性
