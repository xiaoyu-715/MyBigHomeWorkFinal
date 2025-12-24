# 设计文档：学习计划结构化升级

## 概述

本设计文档描述了将学习计划模块从"描述性文字"升级为"结构化可执行任务系统"的技术方案。核心改动包括：数据模型扩展、AI生成Prompt优化、新增计划详情页、任务执行与进度追踪功能。

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ PlanList    │  │ PlanDetail  │  │ PlanSelection       │  │
│  │ Activity    │  │ Activity    │  │ Dialog              │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                     ViewModel Layer                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              StudyPlanViewModel                      │    │
│  │  - 管理计划、阶段、任务数据                          │    │
│  │  - 处理进度计算逻辑                                  │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                    Repository Layer                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              StudyPlanRepository                     │    │
│  │  - 统一管理Plan/Phase/Task的CRUD                    │    │
│  │  - 处理数据关联和事务                               │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                              │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────────┐   │
│  │ StudyPlan │  │StudyPhase │  │     DailyTask         │   │
│  │   Entity  │  │  Entity   │  │      Entity           │   │
│  └───────────┘  └───────────┘  └───────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                   AppDatabase                        │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 数据流

```
AI生成 → JSON解析 → 预览Dialog → 保存到DB
                                      ↓
用户打开计划 → 加载Plan+Phases+Tasks → 显示详情页
                                      ↓
用户完成任务 → 更新Task状态 → 重算进度 → 更新UI
```

## 组件和接口

### 1. 数据实体

#### StudyPlanEntity（扩展）

```java
@Entity(tableName = "study_plans")
public class StudyPlanEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String title;           // 计划标题
    public String category;        // 分类
    public String summary;         // 简要描述（新增，替代原description）
    public String priority;        // 优先级
    public String startDate;       // 开始日期
    public String endDate;         // 结束日期
    public int dailyMinutes;       // 每日时长（分钟）
    public int totalDays;          // 总天数
    public int completedDays;      // 已完成天数
    public int progress;           // 总进度（0-100）
    public String status;          // 状态
    public int streakDays;         // 连续学习天数
    public long totalStudyTime;    // 累计学习时长（毫秒）
    public long createdAt;         // 创建时间
    public long updatedAt;         // 更新时间
    public boolean isAiGenerated;  // 是否AI生成
}
```

#### StudyPhaseEntity（新增）

```java
@Entity(tableName = "study_phases",
        foreignKeys = @ForeignKey(
            entity = StudyPlanEntity.class,
            parentColumns = "id",
            childColumns = "planId",
            onDelete = ForeignKey.CASCADE))
public class StudyPhaseEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int planId;             // 关联的计划ID
    public int phaseOrder;         // 阶段顺序（1, 2, 3...）
    public String phaseName;       // 阶段名称
    public String goal;            // 阶段目标
    public int durationDays;       // 持续天数
    public String taskTemplateJson;// 任务模板JSON
    public int completedDays;      // 已完成天数
    public int progress;           // 阶段进度（0-100）
    public String status;          // 状态：未开始/进行中/已完成
    public String startDate;       // 阶段开始日期
    public String endDate;         // 阶段结束日期
}
```

#### DailyTaskEntity（新增）

```java
@Entity(tableName = "daily_tasks",
        foreignKeys = {
            @ForeignKey(entity = StudyPlanEntity.class,
                parentColumns = "id", childColumns = "planId",
                onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = StudyPhaseEntity.class,
                parentColumns = "id", childColumns = "phaseId",
                onDelete = ForeignKey.CASCADE)
        })
public class DailyTaskEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int planId;             // 关联的计划ID
    public int phaseId;            // 关联的阶段ID
    public String date;            // 任务日期（yyyy-MM-dd）
    public String taskContent;     // 任务内容
    public int estimatedMinutes;   // 预计时长（分钟）
    public int actualMinutes;      // 实际时长（分钟）
    public boolean isCompleted;    // 是否完成
    public long completedAt;       // 完成时间戳
    public int taskOrder;          // 任务顺序
}
```

### 2. DAO接口

#### StudyPhaseDao

```java
@Dao
public interface StudyPhaseDao {
    @Insert
    long insert(StudyPhaseEntity phase);
    
    @Insert
    List<Long> insertAll(List<StudyPhaseEntity> phases);
    
    @Update
    void update(StudyPhaseEntity phase);
    
    @Delete
    void delete(StudyPhaseEntity phase);
    
    @Query("SELECT * FROM study_phases WHERE planId = :planId ORDER BY phaseOrder")
    List<StudyPhaseEntity> getPhasesByPlanId(int planId);
    
    @Query("SELECT * FROM study_phases WHERE planId = :planId AND status = '进行中' LIMIT 1")
    StudyPhaseEntity getCurrentPhase(int planId);
    
    @Query("UPDATE study_phases SET status = :status, progress = :progress WHERE id = :phaseId")
    void updatePhaseProgress(int phaseId, String status, int progress);
}
```

#### DailyTaskDao

```java
@Dao
public interface DailyTaskDao {
    @Insert
    long insert(DailyTaskEntity task);
    
    @Insert
    List<Long> insertAll(List<DailyTaskEntity> tasks);
    
    @Update
    void update(DailyTaskEntity task);
    
    @Delete
    void delete(DailyTaskEntity task);
    
    @Query("SELECT * FROM daily_tasks WHERE planId = :planId AND date = :date ORDER BY taskOrder")
    List<DailyTaskEntity> getTasksByDate(int planId, String date);
    
    @Query("SELECT * FROM daily_tasks WHERE phaseId = :phaseId ORDER BY date, taskOrder")
    List<DailyTaskEntity> getTasksByPhase(int phaseId);
    
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE planId = :planId AND date = :date AND isCompleted = 1")
    int getCompletedTaskCount(int planId, String date);
    
    @Query("SELECT COUNT(*) FROM daily_tasks WHERE planId = :planId AND date = :date")
    int getTotalTaskCount(int planId, String date);
    
    @Query("UPDATE daily_tasks SET isCompleted = :completed, completedAt = :completedAt, actualMinutes = :actualMinutes WHERE id = :taskId")
    void updateTaskCompletion(int taskId, boolean completed, long completedAt, int actualMinutes);
    
    @Query("SELECT EXISTS(SELECT 1 FROM daily_tasks WHERE planId = :planId AND date = :date)")
    boolean hasTasksForDate(int planId, String date);
}
```

### 3. AI生成Prompt优化

#### 结构化Prompt模板

```
你是一位专业的英语学习规划师。请根据用户需求生成结构化的学习计划。

【用户需求】
{conversationContext}

【输出要求】
请严格按照以下JSON格式返回（只返回JSON，不要其他内容）：

{
  "title": "计划标题（简洁明确）",
  "category": "词汇/语法/听力/阅读/写作/口语",
  "summary": "计划简介（50字以内）",
  "priority": "高/中/低",
  "totalDays": 总天数,
  "dailyMinutes": 每日学习分钟数,
  "phases": [
    {
      "phaseName": "阶段名称（如：基础巩固）",
      "goal": "阶段目标（30字以内）",
      "durationDays": 持续天数,
      "dailyTasks": [
        {
          "content": "任务内容",
          "minutes": 预计分钟数
        }
      ]
    }
  ]
}

【示例】
{
  "title": "英语四级听力突破计划",
  "category": "听力",
  "summary": "30天系统提升听力理解能力",
  "priority": "高",
  "totalDays": 30,
  "dailyMinutes": 45,
  "phases": [
    {
      "phaseName": "基础巩固",
      "goal": "熟悉英语语音语调，建立听力基础",
      "durationDays": 10,
      "dailyTasks": [
        {"content": "听力材料精听", "minutes": 20},
        {"content": "跟读模仿练习", "minutes": 15},
        {"content": "生词整理复习", "minutes": 10}
      ]
    },
    {
      "phaseName": "能力提升",
      "goal": "提高听力理解速度和准确率",
      "durationDays": 10,
      "dailyTasks": [
        {"content": "短对话听力练习", "minutes": 20},
        {"content": "长对话听力练习", "minutes": 20},
        {"content": "听力笔记训练", "minutes": 5}
      ]
    },
    {
      "phaseName": "冲刺强化",
      "goal": "模拟真实考试，查漏补缺",
      "durationDays": 10,
      "dailyTasks": [
        {"content": "模拟听力测试", "minutes": 30},
        {"content": "错题分析复盘", "minutes": 15}
      ]
    }
  ]
}
```

### 4. 核心业务逻辑

#### 进度计算算法

```java
public class ProgressCalculator {
    
    /**
     * 计算阶段进度
     * 基于已完成天数 / 总天数
     */
    public static int calculatePhaseProgress(StudyPhaseEntity phase, List<DailyTaskEntity> tasks) {
        if (phase.durationDays == 0) return 0;
        
        // 统计有任务完成的天数
        Set<String> completedDates = new HashSet<>();
        for (DailyTaskEntity task : tasks) {
            if (task.isCompleted) {
                completedDates.add(task.date);
            }
        }
        
        // 检查每天是否所有任务都完成
        Map<String, List<DailyTaskEntity>> tasksByDate = groupTasksByDate(tasks);
        int fullyCompletedDays = 0;
        for (Map.Entry<String, List<DailyTaskEntity>> entry : tasksByDate.entrySet()) {
            boolean allCompleted = entry.getValue().stream().allMatch(t -> t.isCompleted);
            if (allCompleted) {
                fullyCompletedDays++;
            }
        }
        
        return (fullyCompletedDays * 100) / phase.durationDays;
    }
    
    /**
     * 计算计划总进度
     * 基于所有阶段的加权平均
     */
    public static int calculatePlanProgress(List<StudyPhaseEntity> phases) {
        if (phases.isEmpty()) return 0;
        
        int totalDays = 0;
        int completedDays = 0;
        
        for (StudyPhaseEntity phase : phases) {
            totalDays += phase.durationDays;
            completedDays += (phase.progress * phase.durationDays) / 100;
        }
        
        return totalDays > 0 ? (completedDays * 100) / totalDays : 0;
    }
}
```

#### 任务自动生成逻辑

```java
public class TaskGenerator {
    
    /**
     * 为指定日期生成任务
     */
    public List<DailyTaskEntity> generateTasksForDate(
            StudyPlanEntity plan, 
            StudyPhaseEntity currentPhase, 
            String date) {
        
        List<DailyTaskEntity> tasks = new ArrayList<>();
        
        // 解析任务模板
        List<TaskTemplate> templates = parseTaskTemplates(currentPhase.taskTemplateJson);
        
        int order = 0;
        for (TaskTemplate template : templates) {
            DailyTaskEntity task = new DailyTaskEntity();
            task.planId = plan.id;
            task.phaseId = currentPhase.id;
            task.date = date;
            task.taskContent = template.content;
            task.estimatedMinutes = template.minutes;
            task.actualMinutes = 0;
            task.isCompleted = false;
            task.completedAt = 0;
            task.taskOrder = order++;
            tasks.add(task);
        }
        
        return tasks;
    }
}
```

## 数据模型

### ER图

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   StudyPlan     │       │   StudyPhase    │       │   DailyTask     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │──┐    │ id (PK)         │──┐    │ id (PK)         │
│ title           │  │    │ planId (FK)     │  │    │ planId (FK)     │
│ category        │  └───>│ phaseOrder      │  └───>│ phaseId (FK)    │
│ summary         │       │ phaseName       │       │ date            │
│ priority        │       │ goal            │       │ taskContent     │
│ startDate       │       │ durationDays    │       │ estimatedMinutes│
│ endDate         │       │ taskTemplateJson│       │ actualMinutes   │
│ dailyMinutes    │       │ completedDays   │       │ isCompleted     │
│ totalDays       │       │ progress        │       │ completedAt     │
│ completedDays   │       │ status          │       │ taskOrder       │
│ progress        │       │ startDate       │       └─────────────────┘
│ status          │       │ endDate         │
│ streakDays      │       └─────────────────┘
│ totalStudyTime  │
│ createdAt       │
│ updatedAt       │
│ isAiGenerated   │
└─────────────────┘

关系：
- StudyPlan 1:N StudyPhase
- StudyPhase 1:N DailyTask
- StudyPlan 1:N DailyTask
```

### JSON数据结构

#### AI返回的结构化计划

```json
{
  "title": "英语四级听力突破计划",
  "category": "听力",
  "summary": "30天系统提升听力理解能力",
  "priority": "高",
  "totalDays": 30,
  "dailyMinutes": 45,
  "phases": [
    {
      "phaseName": "基础巩固",
      "goal": "熟悉英语语音语调",
      "durationDays": 10,
      "dailyTasks": [
        {"content": "听力材料精听", "minutes": 20},
        {"content": "跟读模仿练习", "minutes": 15}
      ]
    }
  ]
}
```

#### 任务模板JSON（存储在phase中）

```json
[
  {"content": "听力材料精听", "minutes": 20},
  {"content": "跟读模仿练习", "minutes": 15}
]
```



## 正确性属性

*属性是系统在所有有效执行中应该保持为真的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### Property 1: JSON解析往返一致性
*对于任意*有效的结构化学习计划JSON，解析后再序列化应该产生语义等价的数据结构
**验证: Requirements 2.2**

### Property 2: 计划-阶段-任务关联完整性
*对于任意*保存的学习计划，查询其所有阶段和任务后，数据关联应该完整且正确
**验证: Requirements 1.3, 1.4, 2.5**

### Property 3: 今日任务查询正确性
*对于任意*计划和日期，查询返回的任务列表应该只包含该日期的任务，且按顺序排列
**验证: Requirements 3.2**

### Property 4: 任务完成状态切换正确性
*对于任意*任务，切换完成状态后，isCompleted字段应该取反，completedAt应该在完成时记录当前时间
**验证: Requirements 4.1, 4.2, 4.5**

### Property 5: 阶段进度计算正确性
*对于任意*阶段及其任务列表，阶段进度应该等于完全完成的天数除以总天数的百分比
**验证: Requirements 5.1, 5.5**

### Property 6: 计划总进度计算正确性
*对于任意*计划及其所有阶段，计划总进度应该等于各阶段进度的加权平均（按天数加权）
**验证: Requirements 5.2**

### Property 7: 阶段完成状态自动更新
*对于任意*阶段，当其所有天数的任务都完成时，阶段状态应该自动变为"已完成"
**验证: Requirements 5.3**

### Property 8: 计划完成状态自动更新
*对于任意*计划，当其所有阶段都完成时，计划状态应该自动变为"已完成"
**验证: Requirements 5.4**

### Property 9: 任务生成幂等性
*对于任意*计划和日期，多次调用任务生成不应该产生重复任务
**验证: Requirements 6.4**

### Property 10: 任务生成模板一致性
*对于任意*阶段，生成的每日任务应该与阶段的任务模板内容一致
**验证: Requirements 6.1, 6.3**

### Property 11: 连续学习天数计算正确性
*对于任意*学习记录序列，连续学习天数应该等于从今天往前连续有完成任务的天数
**验证: Requirements 9.1, 9.5**

### Property 12: 学习时长累计正确性
*对于任意*已完成任务集合，累计学习时长应该等于所有任务actualMinutes的总和
**验证: Requirements 9.2, 9.4**

## 错误处理

### AI生成错误
- JSON解析失败：显示"计划生成失败，请重试"，提供重试按钮
- 网络错误：显示"网络连接失败"，提供重试按钮
- 格式不完整：使用默认值填充缺失字段

### 数据库错误
- 保存失败：回滚事务，显示错误提示
- 查询失败：显示空状态，提供刷新按钮

### 业务逻辑错误
- 日期计算错误：使用当前日期作为默认值
- 进度计算溢出：限制在0-100范围内

## 测试策略

### 单元测试
- ProgressCalculator的进度计算方法
- TaskGenerator的任务生成方法
- JSON解析和序列化方法
- 日期计算工具方法

### 属性测试
使用JUnit + jqwik库进行属性测试：

1. **JSON解析测试**：生成随机合法JSON，验证解析-序列化往返一致性
2. **进度计算测试**：生成随机任务完成状态，验证进度计算公式
3. **任务生成测试**：生成随机模板，验证生成结果与模板一致
4. **状态转换测试**：生成随机状态序列，验证状态机正确性

### 集成测试
- 数据库CRUD操作
- 计划保存和加载完整流程
- 任务完成和进度更新流程

### UI测试
- 计划详情页显示正确
- 任务复选框交互正常
- 进度条更新正确
