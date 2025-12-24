# 实现计划

## 阶段一：数据模型和基础设施

- [x] 1. 创建新的数据实体





  - [x] 1.1 创建StudyPhaseEntity实体类


    - 定义字段：id, planId, phaseOrder, phaseName, goal, durationDays, taskTemplateJson, completedDays, progress, status, startDate, endDate
    - 添加Room注解和外键约束
    - _Requirements: 1.1, 1.3_

  - [x] 1.2 创建DailyTaskEntity实体类


    - 定义字段：id, planId, phaseId, date, taskContent, estimatedMinutes, actualMinutes, isCompleted, completedAt, taskOrder
    - 添加Room注解和外键约束
    - _Requirements: 1.2, 1.4_

  - [x] 1.3 扩展StudyPlanEntity实体


    - 新增字段：summary, totalDays, completedDays, streakDays, totalStudyTime, isAiGenerated
    - 保持与现有字段的兼容性
    - _Requirements: 1.1_

  - [ ]* 1.4 编写属性测试：数据关联完整性
    - **Property 2: 计划-阶段-任务关联完整性**
    - **Validates: Requirements 1.3, 1.4, 2.5**

- [x] 2. 创建DAO接口





  - [x] 2.1 创建StudyPhaseDao接口


    - 实现insert, insertAll, update, delete方法
    - 实现getPhasesByPlanId, getCurrentPhase, updatePhaseProgress查询方法
    - _Requirements: 1.3_


  - [x] 2.2 创建DailyTaskDao接口

    - 实现insert, insertAll, update, delete方法
    - 实现getTasksByDate, getTasksByPhase, getCompletedTaskCount, getTotalTaskCount查询方法
    - 实现updateTaskCompletion, hasTasksForDate方法
    - _Requirements: 1.4, 3.2_

  - [ ]* 2.3 编写属性测试：今日任务查询正确性
    - **Property 3: 今日任务查询正确性**
    - **Validates: Requirements 3.2**

- [x] 3. 更新数据库配置





  - [x] 3.1 更新AppDatabase类


    - 添加StudyPhaseEntity和DailyTaskEntity到entities数组
    - 添加studyPhaseDao()和dailyTaskDao()抽象方法
    - 增加数据库版本号并添加迁移脚本
    - _Requirements: 1.5_

- [x] 4. Checkpoint - 确保数据层编译通过





  - 确保所有测试通过，如有问题请询问用户。

## 阶段二：核心业务逻辑

- [x] 5. 创建进度计算工具类
+






  - [x] 5.1 创建ProgressCalculator类





    - 实现calculatePhaseProgress方法：计算阶段进度
    - 实现calculatePlanProgress方法：计算计划总进度
    - 实现groupTasksByDate辅助方法
    - _Requirements: 5.1, 5.2, 5.5_

  - [ ]* 5.2 编写属性测试：阶段进度计算正确性
    - **Property 5: 阶段进度计算正确性**
    - **Validates: Requirements 5.1, 5.5**

  - [ ]* 5.3 编写属性测试：计划总进度计算正确性
    - **Property 6: 计划总进度计算正确性**
    - **Validates: Requirements 5.2**

- [x] 6. 创建任务生成器






  - [x] 6.1 创建TaskGenerator类

    - 实现generateTasksForDate方法：根据模板生成指定日期的任务
    - 实现parseTaskTemplates方法：解析JSON任务模板
    - 实现shouldGenerateTasks方法：检查是否需要生成任务
    - _Requirements: 6.1, 6.2, 6.3_

  - [ ]* 6.2 编写属性测试：任务生成幂等性
    - **Property 9: 任务生成幂等性**
    - **Validates: Requirements 6.4**

  - [ ]* 6.3 编写属性测试：任务生成模板一致性
    - **Property 10: 任务生成模板一致性**
    - **Validates: Requirements 6.1, 6.3**

- [x] 7. 创建状态管理器





  - [x] 7.1 创建PlanStatusManager类


    - 实现updatePhaseStatus方法：根据任务完成情况更新阶段状态
    - 实现updatePlanStatus方法：根据阶段完成情况更新计划状态
    - 实现checkAndAdvancePhase方法：检查是否需要进入下一阶段
    - _Requirements: 5.3, 5.4_

  - [ ]* 7.2 编写属性测试：阶段完成状态自动更新
    - **Property 7: 阶段完成状态自动更新**
    - **Validates: Requirements 5.3**

  - [ ]* 7.3 编写属性测试：计划完成状态自动更新
    - **Property 8: 计划完成状态自动更新**
    - **Validates: Requirements 5.4**

- [x] 8. 创建学习统计工具类






  - [x] 8.1 创建StudyStatisticsHelper类

    - 实现calculateStreakDays方法：计算连续学习天数
    - 实现calculateTotalStudyTime方法：计算累计学习时长
    - 实现calculateWeeklyStudyTime方法：计算本周学习时长
    - 实现getCompletedTasksCount方法：获取已完成任务数
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ]* 8.2 编写属性测试：连续学习天数计算正确性
    - **Property 11: 连续学习天数计算正确性**
    - **Validates: Requirements 9.1, 9.5**

  - [ ]* 8.3 编写属性测试：学习时长累计正确性
    - **Property 12: 学习时长累计正确性**
    - **Validates: Requirements 9.2, 9.4**

- [x] 9. Checkpoint - 确保业务逻辑测试通过





  - 确保所有测试通过，如有问题请询问用户。

## 阶段三：AI生成优化

- [x] 10. 优化AI生成Prompt





  - [x] 10.1 创建StructuredPlanPromptBuilder类


    - 实现buildPrompt方法：构建结构化计划生成的Prompt
    - 包含JSON格式要求和示例
    - _Requirements: 2.1_


  - [x] 10.2 创建StructuredPlanParser类

    - 实现parseResponse方法：解析AI返回的JSON
    - 实现extractPhases方法：提取阶段信息
    - 实现extractDailyTasks方法：提取任务模板
    - 实现validatePlanData方法：验证数据完整性
    - _Requirements: 2.2_

  - [ ]* 10.3 编写属性测试：JSON解析往返一致性
    - **Property 1: JSON解析往返一致性**
    - **Validates: Requirements 2.2**

- [x] 11. 更新StudyPlanExtractor





  - [x] 11.1 修改extractPlans方法


    - 使用StructuredPlanPromptBuilder构建Prompt
    - 使用StructuredPlanParser解析响应
    - 返回包含阶段和任务的完整计划数据
    - _Requirements: 2.1, 2.2_

- [x] 12. 更新Repository层






  - [x] 12.1 扩展StudyPlanRepository

    - 实现savePlanWithPhasesAndTasks方法：事务保存计划、阶段、任务
    - 实现getPlanWithDetails方法：获取计划及其所有阶段和任务
    - 实现updateTaskCompletion方法：更新任务完成状态并触发进度重算
    - _Requirements: 2.5, 4.2, 4.4_

  - [ ]* 12.2 编写属性测试：任务完成状态切换正确性
    - **Property 4: 任务完成状态切换正确性**
    - **Validates: Requirements 4.1, 4.2, 4.5**

- [x] 13. Checkpoint - 确保AI生成流程正常





  - 确保所有测试通过，如有问题请询问用户。

## 阶段四：UI界面实现

- [x] 14. 创建计划详情页





  - [x] 14.1 创建activity_plan_detail.xml布局


    - 顶部：计划标题和返回按钮
    - 概览卡片：总进度、连续学习天数、本周时长
    - 今日任务区域：任务列表和完成状态
    - 阶段进度区域：阶段列表和状态指示
    - 底部：完成今日学习按钮
    - _Requirements: 3.1, 3.2, 3.3, 8.1, 8.2, 8.3_


  - [x] 14.2 创建item_daily_task.xml布局

    - 复选框、任务内容、预计时长
    - 简洁清新的卡片样式
    - _Requirements: 8.4_

  - [x] 14.3 创建item_phase_progress.xml布局


    - 阶段名称、状态图标、进度条
    - 不同状态的视觉区分
    - _Requirements: 8.4_

- [x] 15. 创建详情页Activity和适配器





  - [x] 15.1 创建PlanDetailActivity类


    - 加载计划详情数据
    - 显示今日任务列表
    - 显示阶段进度列表
    - 处理任务完成交互
    - _Requirements: 3.1, 3.2, 3.3, 4.1_


  - [x] 15.2 创建DailyTaskAdapter类

    - 显示任务列表
    - 处理复选框点击事件
    - 更新任务完成状态
    - _Requirements: 4.1, 4.2_

  - [x] 15.3 创建PhaseProgressAdapter类


    - 显示阶段列表
    - 根据状态显示不同图标和颜色
    - _Requirements: 3.3_

- [x] 16. 更新计划预览对话框





  - [x] 16.1 修改PlanSelectionDialog


    - 显示阶段预览信息
    - 显示每个阶段的任务模板
    - 支持展开/收起阶段详情
    - _Requirements: 2.4_

  - [x] 16.2 创建item_phase_preview.xml布局


    - 阶段名称、目标、天数
    - 任务模板列表
    - _Requirements: 2.4_

- [x] 17. Checkpoint - 确保详情页功能正常





  - 确保所有测试通过，如有问题请询问用户。

## 阶段五：计划列表优化

- [x] 18. 优化计划列表卡片





  - [x] 18.1 更新item_study_plan.xml布局


    - 添加当前阶段显示区域
    - 添加今日任务完成情况显示
    - 添加剩余天数显示
    - 优化整体视觉效果
    - _Requirements: 7.1, 7.2, 7.3, 8.1, 8.2_

  - [x] 18.2 更新StudyPlanAdapter类


    - 加载并显示当前阶段信息
    - 加载并显示今日任务完成情况
    - 计算并显示剩余天数
    - 点击跳转到详情页
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 19. 更新ViewModel





  - [x] 19.1 扩展StudyPlanViewModel


    - 添加获取计划详情的方法
    - 添加获取今日任务的方法
    - 添加更新任务状态的方法
    - 添加刷新统计数据的方法
    - _Requirements: 3.1, 4.1, 9.1, 9.2, 9.3_

- [x] 20. 实现任务自动生成





  - [x] 20.1 创建TaskGenerationService类


    - 在打开计划时检查并生成今日任务
    - 处理阶段切换时的任务生成
    - 避免重复生成
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 21. Final Checkpoint - 确保所有功能正常





  - 确保所有测试通过，如有问题请询问用户。
