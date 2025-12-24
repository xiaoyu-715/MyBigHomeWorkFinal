# AI学习计划与今日任务集成设计文档YSJ

## 文档控制信息

### 基本信息

| 项目 | 内容 |
|------|------|
| 文档名称 | AI学习计划与今日任务集成设计文档 |
| 文档编号 | DES-2024-001-YSJ |
| 版本号 | v2.0 |
| 创建日期 | 2024-12-17 |
| 最后更新 | 2024-12-17 |
| 文档状态 | 待评审 |
| 密级 | 内部 |
| 关联需求文档 | REQ-2024-001-YSJ (需求文档v2.0) |

### 责任人

| 角色 | 姓名 | 职责 |
|------|------|------|
| 系统架构师 | 技术团队 | 系统架构设计 |
| 技术负责人 | 开发团队 | 详细设计与实现 |
| 数据库设计师 | 开发团队 | 数据库设计 |
| 代码审查员 | 技术团队 | 代码审查 |

### 变更历史

| 版本 | 日期 | 修改人 | 修改内容 | 审批人 |
|------|------|--------|----------|--------|
| v1.0 | 2024-12-17 | 开发团队 | 初始版本 | - |
| v2.0 | 2024-12-17 | 开发团队 | 添加类图、序列图、组件图、详细代码示例 | 待审批 |

---

## 一、设计概述

### 1.1 设计目标

基于需求文档，详细描述AI学习计划与今日任务集成系统的技术架构、模块设计、接口设计、数据库设计和实现细节。

### 1.2 设计原则

1. **单一职责原则**：每个类只负责一个功能领域
2. **MVVM架构**：遵循Android MVVM架构模式
3. **数据一致性**：使用事务保证数据完整性
4. **性能优先**：异步操作、缓存优化、索引优化

### 1.3 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Android SDK | API 24+ | 应用开发 |
| Java | 8+ | 编程语言 |
| Room | 2.5.0 | 数据库ORM |
| Gson | 2.10.1 | JSON解析 |
| LiveData | 2.5.0 | 数据观察 |

---

## 二、系统架构设计

### 2.1 整体架构

```
表现层（Presentation Layer）
├── AIChatActivity（AI聊天界面）
├── DailyTaskActivity（今日任务界面）
└── PlanSelectionDialog（计划选择对话框）

业务层（Business Layer）
├── StudyPlanViewModel（视图模型）
├── StudyPlanRepository（数据仓库）
├── StudyPlanExtractor（计划提取器）
└── TaskGenerationService（任务生成服务）

数据层（Data Layer）
├── StudyPlanDao（计划DAO）
├── StudyPhaseDao（阶段DAO）
├── DailyTaskDao（任务DAO）
└── AppDatabase（Room数据库）

外部服务层
└── ZhipuAIService（智谱AI服务）
```

### 2.2 数据流

```
用户对话 → AI生成 → JSON响应
    ↓
解析为StructuredPlanResult
    ↓
保存到数据库（事务）
    ├── StudyPlanEntity
    ├── StudyPhaseEntity
    └── DailyTaskEntity（模板）
    ↓
生成今日任务
    ↓
展示到UI
```

### 2.3 类图（Class Diagram）

#### 2.3.1 核心类关系图

```
+---------------------------+
|   AIChatActivity          |
+---------------------------+
| - planExtractor           |
| - studyPlanRepository     |
| - taskGenerationService   |
+---------------------------+
| + generateStudyPlan()     |
| + generateTodayTasks()    |
| + showSuccessDialog()     |
+---------------------------+
        |                   |
        | uses              | uses
        v                   v
+---------------------------+  +---------------------------+
|  StudyPlanExtractor       |  |  StudyPlanRepository      |
+---------------------------+  +---------------------------+
| - apiService              |  | - studyPlanDao            |
| - analyzer                |  | - studyPhaseDao           |
| - gson                    |  | - dailyTaskDao            |
+---------------------------+  | - database                |
| + extractStructuredPlan() |  +---------------------------+
| - buildPrompt()           |  | + savePlanWithStructure() |
| - parseResponse()         |  | + getActivePlans()        |
| - validateResult()        |  | + getTasksByDate()        |
| - calculateDates()        |  | + updateTask()            |
+---------------------------+  +---------------------------+
        |                              |
        | uses                         | uses
        v                              v
+---------------------------+  +---------------------------+
|   ZhipuAIService          |  |      AppDatabase          |
+---------------------------+  +---------------------------+
| - API_KEY                 |  | - INSTANCE                |
| - API_ENDPOINT            |  +---------------------------+
+---------------------------+  | + studyPlanDao()          |
| + chat()                  |  | + studyPhaseDao()         |
| + chatStream()            |  | + dailyTaskDao()          |
+---------------------------+  | + runInTransaction()      |
                               +---------------------------+

+---------------------------+
|  TaskGenerationService    |
+---------------------------+
| - dailyTaskDao            |
| - studyPhaseDao           |
| - executorService         |
+---------------------------+
| + ensureTodayTasksExist() |
| - getCurrentPhase()       |
| - getTaskTemplates()      |
| - isDuplicateTask()       |
| - calculateSimilarity()   |
+---------------------------+
        |
        | uses
        v
+---------------------------+
|   DailyTaskActivity       |
+---------------------------+
| - studyPlanRepository     |
| - taskGenerationService   |
| - adapter                 |
| - taskList                |
+---------------------------+
| + loadTodayTasks()        |
| + updateTaskStatus()      |
| + updateUI()              |
+---------------------------+
```

#### 2.3.2 数据模型类图

```
+---------------------------+
| StructuredPlanResult      |
+---------------------------+
| - title: String           |
| - description: String     |
| - category: String        |
| - difficulty: String      |
| - estimatedDays: int      |
| - startDate: Date         |
| - endDate: Date           |
| - totalTasks: int         |
| - phases: List<Phase>     |
+---------------------------+
| + getters/setters         |
+---------------------------+
        |
        | contains
        v
+---------------------------+
|         Phase             |
+---------------------------+
| - phaseName: String       |
| - phaseGoal: String       |
| - durationDays: int       |
| - phaseOrder: int         |
| - startDate: Date         |
| - endDate: Date           |
| - tasks: List<TaskTemplate>|
+---------------------------+
| + getters/setters         |
+---------------------------+
        |
        | contains
        v
+---------------------------+
|      TaskTemplate         |
+---------------------------+
| - taskContent: String     |
| - estimatedMinutes: int   |
| - taskType: String        |
| - dayOffset: int          |
| - taskOrder: int          |
+---------------------------+
| + getters/setters         |
+---------------------------+

+---------------------------+
|   StudyPlanEntity         |
+---------------------------+
| - id: int                 |
| - title: String           |
| - category: String        |
| - description: String     |
| - difficulty: String      |
| - startDate: String       |
| - endDate: String         |
| - progress: int           |
| - status: String          |
| - createdAt: long         |
+---------------------------+
        |
        | 1:N
        v
+---------------------------+
|   StudyPhaseEntity        |
+---------------------------+
| - id: int                 |
| - planId: int             |
| - phaseName: String       |
| - phaseGoal: String       |
| - durationDays: int       |
| - phaseOrder: int         |
| - startDate: String       |
| - endDate: String         |
+---------------------------+
        |
        | 1:N
        v
+---------------------------+
|   DailyTaskEntity         |
+---------------------------+
| - id: int                 |
| - planId: int             |
| - phaseId: int            |
| - date: String            |
| - taskContent: String     |
| - estimatedMinutes: int   |
| - actualMinutes: int      |
| - isCompleted: boolean    |
| - completedAt: long       |
| - taskOrder: int          |
| - taskType: String        |
| - source: String          |
| - planTitle: String       |
| - phaseName: String       |
+---------------------------+
```

### 2.4 序列图（Sequence Diagram）

#### 2.4.1 生成学习计划序列图

```
用户    AIChatActivity  StudyPlanExtractor  ZhipuAI  StudyPlanRepository  TaskGenerationService
 |           |                  |              |            |                      |
 |--点击生成-->|                  |              |            |                      |
 |           |                  |              |            |                      |
 |           |--extractPlan()-->|              |            |                      |
 |           |                  |              |            |                      |
 |           |                  |--buildPrompt()            |                      |
 |           |                  |              |            |                      |
 |           |                  |--chat()----->|            |                      |
 |           |                  |              |            |                      |
 |           |                  |<--JSON响应--|            |                      |
 |           |                  |              |            |                      |
 |           |                  |--parseJSON() |            |                      |
 |           |                  |              |            |                      |
 |           |                  |--validate()  |            |                      |
 |           |                  |              |            |                      |
 |           |<--StructuredPlan-|              |            |                      |
 |           |                  |              |            |                      |
 |           |--savePlan()------|--------------|----------->|                      |
 |           |                  |              |            |                      |
 |           |                  |              |      [事务开始]                  |
 |           |                  |              |            |                      |
 |           |                  |              |      [保存计划]                  |
 |           |                  |              |            |                      |
 |           |                  |              |      [保存阶段]                  |
 |           |                  |              |            |                      |
 |           |                  |              |      [保存任务模板]              |
 |           |                  |              |            |                      |
 |           |                  |              |      [事务提交]                  |
 |           |                  |              |            |                      |
 |           |<--planId---------|--------------|------------|                      |
 |           |                  |              |            |                      |
 |           |--generateTasks()-|--------------|------------|--------------------->|
 |           |                  |              |            |                      |
 |           |                  |              |            |            [检查幂等]  |
 |           |                  |              |            |                      |
 |           |                  |              |            |            [生成任务]  |
 |           |                  |              |            |                      |
 |           |<--任务列表----|--------------|------------|---------------------|
 |           |                  |              |            |                      |
 |<--成功对话框-|                  |              |            |                      |
 |           |                  |              |            |                      |
```

#### 2.4.2 完成任务序列图

```
用户    DailyTaskActivity  DailyTaskAdapter  StudyPlanRepository  Database
 |           |                  |                  |              |
 |--点击复选框->|                  |                  |              |
 |           |                  |                  |              |
 |           |<--onTaskComplete-|                  |              |
 |           |                  |                  |              |
 |           |--updateStatus()  |                  |              |
 |           |                  |                  |              |
 |           |--updateTask()----|----------------->|              |
 |           |                  |                  |              |
 |           |                  |                  |--update()-->|              |
 |           |                  |                  |              |
 |           |                  |                  |<--success---|              |
 |           |                  |                  |              |
 |           |<--onSuccess------|------------------|              |
 |           |                  |                  |              |
 |           |--notifyAdapter()->|                  |              |
 |           |                  |                  |              |
 |           |                  |--notifyChanged()->              |
 |           |                  |                  |              |
 |           |--updateProgress()|                  |              |
 |           |                  |                  |              |
 |<--UI更新---|                  |                  |              |
 |           |                  |                  |              |
```

### 2.5 组件图（Component Diagram）

```
+---------------------------------------------------------------+
|                     表现层组件                            |
|  +------------------+  +------------------+                   |
|  | AIChatActivity   |  | DailyTaskActivity|                   |
|  +------------------+  +------------------+                   |
|  | PlanSelectionDialog |                                      |
|  +------------------+                                          |
+---------------------------------------------------------------+
                        |
                        | 依赖
                        v
+---------------------------------------------------------------+
|                     业务层组件                            |
|  +------------------+  +------------------+                   |
|  |StudyPlanExtractor|  |StudyPlanRepository|                  |
|  +------------------+  +------------------+                   |
|  +------------------+  +------------------+                   |
|  |TaskGenerationSvc |  | StudyPlanViewModel|                  |
|  +------------------+  +------------------+                   |
+---------------------------------------------------------------+
                        |
                        | 依赖
                        v
+---------------------------------------------------------------+
|                     数据层组件                            |
|  +------------------+  +------------------+                   |
|  |  StudyPlanDao    |  |  StudyPhaseDao   |                   |
|  +------------------+  +------------------+                   |
|  +------------------+                                          |
|  |  DailyTaskDao    |                                          |
|  +------------------+                                          |
|  +------------------+                                          |
|  |   AppDatabase    |                                          |
|  +------------------+                                          |
+---------------------------------------------------------------+
                        |
                        | 依赖
                        v
+---------------------------------------------------------------+
|                   外部服务组件                          |
|  +------------------+                                          |
|  | ZhipuAIService   |                                          |
|  +------------------+                                          |
+---------------------------------------------------------------+
```

---

## 三、核心类设计

### 3.1 StructuredPlanResult（结构化计划结果）

**职责**：封装AI生成的完整学习计划结构

**关键字段**：
- `title`：计划标题
- `description`：计划描述
- `difficulty`：难度级别
- `estimatedDays`：预计天数
- `phases`：阶段列表

**内部类**：
- `Phase`：学习阶段
- `TaskTemplate`：任务模板

### 3.2 StudyPlanExtractor（学习计划提取器）

**职责**：从AI对话中提取结构化学习计划

**核心方法**：

```java
// 提取结构化学习计划
public void extractStructuredPlanEnhanced(
    String conversationContext,
    OnStructuredPlanExtractedListener callback,
    OnProgressUpdateListener progressListener)

// 构建增强的结构化Prompt
private String buildEnhancedStructuredPrompt(String context)

// 解析AI响应
private StructuredPlanResult parseEnhancedStructuredResponse(String response)

// 验证数据完整性
private boolean validateStructuredResult(StructuredPlanResult result)

// 计算日期
private void calculateDates(StructuredPlanResult result)
```

**Prompt设计**：
- 要求AI返回JSON格式
- 包含计划、阶段、任务的完整结构
- 指定任务类型和难度级别

### 3.3 StudyPlanRepository（学习计划仓库）

**职责**：管理学习计划、阶段、任务的数据操作

**核心方法**：

```java
// 保存完整的学习计划结构
public void savePlanWithFullStructure(
    StructuredPlanResult structuredResult,
    OnPlanSavedListener listener)

// 转换数据模型
private StudyPlanEntity convertToPlanEntity(StructuredPlanResult result)

// 保存阶段和任务模板
private void savePhasesAndTasks(long planId, StructuredPlanResult result)

// 保存任务模板
private void saveTaskTemplates(long planId, long phaseId, Phase phase, String planTitle)
```

**事务保证**：
- 使用`database.runInTransaction()`保证原子性
- 失败时自动回滚

### 3.4 TaskGenerationService（任务生成服务）

**职责**：根据学习计划生成今日任务

**核心方法**：

```java
// 确保今日任务存在（幂等）
public void ensureTodayTasksExistEnhanced(int planId, OnTasksGeneratedListener listener)

// 获取当前阶段
private StudyPhaseEntity getCurrentPhase(int planId, String date)

// 获取任务模板
private List<DailyTaskEntity> getTaskTemplatesForPhase(int phaseId)

// 检查任务重复
private boolean isDuplicateTask(String taskContent, List<DailyTaskEntity> existingTasks)

// 计算相似度
private double calculateSimilarity(String s1, String s2)
```

**去重算法**：
- 使用Levenshtein距离计算相似度
- 相似度>80%视为重复

### 3.5 AIChatActivity（AI聊天界面）

**职责**：处理AI对话和学习计划生成

**核心方法**：

```java
// 生成学习计划
private void generateStudyPlanFromMessage(int position)

// 生成今日任务
private void generateTodayTasksForPlan(int planId, StructuredPlanResult result)

// 显示成功对话框
private void showSuccessDialogWithTasks(String planTitle, int taskCount)
```

**交互流程**：
1. 用户点击"生成计划"
2. 显示进度对话框
3. 调用`StudyPlanExtractor`提取计划
4. 调用`StudyPlanRepository`保存计划
5. 调用`TaskGenerationService`生成今日任务
6. 显示成功对话框，可跳转到今日任务页面

### 3.6 DailyTaskActivity（今日任务界面）

**职责**：展示和管理今日任务

**核心方法**：

```java
// 加载今日任务
private void loadTodayTasks()

// 加载多个计划的任务
private void loadTasksForPlans(List<StudyPlanEntity> plans, String today)

// 更新任务状态
private void updateTaskStatus(DailyTaskEntity task, int position)

// 更新UI
private void updateUI()
```

**数据加载**：
- 查询所有活跃计划
- 对每个计划查询今日任务
- 合并到一个列表展示

---

## 四、数据库设计

### 4.1 表结构

#### daily_tasks（每日任务表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INTEGER | PRIMARY KEY | 任务ID |
| plan_id | INTEGER | FOREIGN KEY | 计划ID |
| phase_id | INTEGER | FOREIGN KEY | 阶段ID |
| date | TEXT | NOT NULL | 任务日期 |
| task_content | TEXT | NOT NULL | 任务内容 |
| estimated_minutes | INTEGER | | 预计时长 |
| is_completed | INTEGER | DEFAULT 0 | 是否完成 |
| task_type | TEXT | | 任务类型 |
| source | TEXT | DEFAULT 'ai_generated' | 来源 |
| plan_title | TEXT | | 计划标题 |
| phase_name | TEXT | | 阶段名称 |

#### 索引设计

```sql
CREATE INDEX idx_daily_tasks_plan_date ON daily_tasks(plan_id, date);
CREATE INDEX idx_daily_tasks_date ON daily_tasks(date);
CREATE INDEX idx_daily_tasks_phase ON daily_tasks(phase_id);
```

### 4.2 外键关系

```
study_plans (1) ─── (N) study_phases
study_plans (1) ─── (N) daily_tasks
study_phases (1) ─── (N) daily_tasks
```

---

## 五、接口设计

### 5.1 StudyPlanExtractor接口

```java
// 回调接口：结构化计划提取完成
public interface OnStructuredPlanExtractedListener {
    void onSuccess(StructuredPlanResult result);
    void onError(String error);
}

// 回调接口：进度更新
public interface OnProgressUpdateListener {
    void onProgressUpdate(int progress, String message);
}
```

### 5.2 StudyPlanRepository接口

```java
// 回调接口：计划保存完成
public interface OnPlanSavedListener {
    void onPlanSaved(long planId);
    void onError(Exception e);
}

// 回调接口：数据加载完成
public interface OnDataLoadListener<T> {
    void onDataLoaded(T data);
    void onLoadError(Exception e);
}
```

### 5.3 TaskGenerationService接口

```java
// 回调接口：任务生成完成
public interface OnTasksGeneratedListener {
    void onTasksGenerated(List<DailyTaskEntity> tasks, boolean isNewlyGenerated);
    void onError(Exception e);
}
```

---

## 六、异常处理

### 6.1 异常类型

| 异常类型 | 说明 | 处理方式 |
|---------|------|----------|
| NetworkException | 网络请求失败 | 提示用户，支持重试 |
| ParseException | JSON解析失败 | 提示用户重新生成 |
| ValidationException | 数据验证失败 | 提示用户重新生成 |
| DatabaseException | 数据库操作失败 | 回滚事务，提示用户 |

### 6.2 错误提示

- 网络错误："网络连接失败，请检查网络后重试"
- 解析错误："AI生成的计划格式错误，请重新生成"
- 验证错误："生成的计划数据不完整，请重试"
- 数据库错误："保存失败，请稍后重试"

---

## 七、性能优化

### 7.1 数据库优化

- 添加索引：`plan_id + date`、`date`、`phase_id`
- 使用事务：批量操作使用事务
- 异步操作：所有数据库操作在后台线程执行

### 7.2 UI优化

- 异步加载：使用ExecutorService异步加载数据
- 进度提示：显示加载进度对话框
- 缓存机制：缓存查询结果

### 7.3 网络优化

- 超时设置：设置合理的超时时间
- 重试机制：失败后自动重试（最多3次）
- 错误处理：友好的错误提示

---

## 八、测试策略

### 8.1 单元测试

- `StudyPlanExtractor`：测试计划提取和解析
- `TaskGenerationService`：测试任务生成和去重
- `StudyPlanRepository`：测试数据保存和查询

### 8.2 集成测试

- 测试完整流程：对话→生成→保存→展示
- 测试并发操作：多个计划同时生成任务
- 测试异常场景：网络失败、数据错误

### 8.3 性能测试

- 测试大数据量：100个任务加载时间<1秒
- 测试并发：多个计划同时操作
- 测试内存：监控内存使用情况

---

## 九、部署计划

### 9.1 开发阶段

| 阶段 | 任务 | 时间 |
|------|------|------|
| 1 | 数据模型扩展 | 1天 |
| 2 | 核心功能开发 | 3天 |
| 3 | 任务模块改造 | 2天 |
| 4 | 测试与优化 | 2天 |

### 9.2 验收标准

- [ ] AI生成的学习计划能正确保存
- [ ] 计划保存后自动生成今日任务
- [ ] 今日任务页面正确显示任务
- [ ] 任务完成状态能正确更新
- [ ] 无重复任务生成
- [ ] 性能测试通过

---

**文档结束**
