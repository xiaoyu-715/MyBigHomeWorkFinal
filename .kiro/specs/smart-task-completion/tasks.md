# 任务清单：智能任务完成识别系统

## 任务概览

| 阶段 | 任务数 | 预计工时 | 说明 |
|-----|-------|---------|------|
| 阶段1：数据层 | 3 | 2h | 数据库字段扩展和DAO更新 |
| 阶段2：核心组件 | 4 | 4h | 创建核心业务逻辑组件 |
| 阶段3：模块集成 | 6 | 3h | 各功能模块集成TaskProgressTracker |
| 阶段4：AI集成 | 2 | 2h | AI Prompt和Parser增强 |
| 阶段5：UI优化 | 2 | 2h | 任务列表显示和交互优化 |
| 阶段6：清理和测试 | 2 | 1h | 废弃代码清理和测试 |

**总计：19个任务，预计14小时**

---

## 阶段1：数据层（优先级：P0）

### 任务 1.1：扩展 DailyTaskEntity 字段

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/database/entity/DailyTaskEntity.java`

**修改内容**：
```java
// 新增字段
@ColumnInfo(name = "completion_type")
private String completionType;  // count/duration/simple

@ColumnInfo(name = "completion_target")
private int completionTarget;   // 完成目标值

@ColumnInfo(name = "current_progress")
private int currentProgress;    // 当前进度

// 新增 getter/setter
public String getCompletionType() { return completionType; }
public void setCompletionType(String completionType) { this.completionType = completionType; }

public int getCompletionTarget() { return completionTarget; }
public void setCompletionTarget(int completionTarget) { this.completionTarget = completionTarget; }

public int getCurrentProgress() { return currentProgress; }
public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }
```

**验收标准**：
- 编译通过
- 字段有默认值（completionType="simple", completionTarget=1, currentProgress=0）

---

### 任务 1.2：添加数据库迁移

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/database/AppDatabase.java`

**修改内容**：
```java
// 增加数据库版本号
@Database(entities = {...}, version = X+1)

// 添加迁移
static final Migration MIGRATION_X_X1 = new Migration(X, X+1) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE daily_tasks ADD COLUMN completion_type TEXT DEFAULT 'simple'");
        database.execSQL("ALTER TABLE daily_tasks ADD COLUMN completion_target INTEGER DEFAULT 1");
        database.execSQL("ALTER TABLE daily_tasks ADD COLUMN current_progress INTEGER DEFAULT 0");
    }
};

// 在 getInstance() 中添加迁移
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_X_X1)
    .build();
```

**验收标准**：
- 应用升级后数据库迁移成功
- 旧数据保留，新字段有默认值

---

### 任务 1.3：扩展 DailyTaskDao 查询方法

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/database/dao/DailyTaskDao.java`

**修改内容**：
```java
// 根据 actionType 和日期查询任务
@Query("SELECT * FROM daily_tasks WHERE action_type = :actionType AND date = :date")
List<DailyTaskEntity> getTasksByActionType(String actionType, String date);

// 查询今日未完成的指定类型任务
@Query("SELECT * FROM daily_tasks WHERE action_type = :actionType AND date = :date AND completed = 0")
List<DailyTaskEntity> getUncompletedTasksByActionType(String actionType, String date);

// 更新任务进度
@Query("UPDATE daily_tasks SET current_progress = :progress WHERE id = :taskId")
void updateProgress(int taskId, int progress);

// 标记任务完成
@Query("UPDATE daily_tasks SET completed = 1, completed_at = :completedAt, current_progress = :progress WHERE id = :taskId")
void markCompleted(int taskId, int progress, long completedAt);
```

**验收标准**：
- 编译通过
- 查询方法能正确返回结果

---

## 阶段2：核心组件（优先级：P0）

### 任务 2.1：创建 TaskProgressTracker

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/utils/TaskProgressTracker.java`（新建）

**实现内容**：
```java
public class TaskProgressTracker {
    private static TaskProgressTracker instance;
    private Context context;
    private DailyTaskDao taskDao;
    
    // 单例获取
    public static synchronized TaskProgressTracker getInstance(Context context);
    
    // 记录进度（用于 count 类型）
    public void recordProgress(String actionType, int increment);
    
    // 标记简单型任务完成（用于 simple 类型）
    public void markSimpleTaskCompleted(String actionType);
    
    // 获取进度（用于UI显示）
    public void getProgress(String actionType, ProgressCallback callback);
    
    // 回调接口
    public interface ProgressCallback {
        void onResult(int currentProgress, int targetProgress);
    }
}
```

**验收标准**：
- 编译通过
- recordProgress 能正确累计进度
- 达到目标时自动标记任务完成

---

### 任务 2.2：创建 ActionTypeInferrer

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/utils/ActionTypeInferrer.java`（新建）

**实现内容**：
```java
public class ActionTypeInferrer {
    // 按优先级排序的关键词映射
    private static final LinkedHashMap<String, List<String>> ACTION_KEYWORDS;
    
    // 根据任务内容推断操作类型
    public static String inferActionType(String taskContent);
    
    // 获取操作类型对应的Activity类
    public static Class<?> getTargetActivity(String actionType);
    
    // 获取操作类型的描述
    public static String getActionDescription(String actionType);
    
    // 获取默认完成类型
    public static String getDefaultCompletionType(String actionType);
}
```

**验收标准**：
- "学习20个单词" → vocabulary_training
- "完成1套真题" → real_exam
- "每日一句" → daily_sentence
- 优先级正确（每日一句 > 真题 > 模拟考试 > 错题 > 词汇 > 翻译）

---

### 任务 2.3：创建 CompletionConditionParser

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/utils/CompletionConditionParser.java`（新建）

**实现内容**：
```java
public class CompletionConditionParser {
    // 从任务内容解析完成条件
    public static CompletionCondition parse(String taskContent);
    
    // 完成条件类
    public static class CompletionCondition {
        public String type;   // count/duration/simple
        public int target;    // 目标值
    }
}
```

**验收标准**：
- "学习20个单词" → type=count, target=20
- "练习15分钟" → type=duration, target=15
- "学习今日一句" → type=simple, target=1

---

### 任务 2.4：更新 TaskGenerator 集成推断逻辑

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/plan/TaskGenerator.java`

**修改内容**：
```java
public List<DailyTaskEntity> generateTasksForDate(...) {
    for (TaskTemplate template : templates) {
        DailyTaskEntity task = new DailyTaskEntity();
        // ... 现有字段设置 ...
        
        // 设置 actionType（优先使用模板值，否则推断）
        String actionType = template.getActionType();
        if (actionType == null || actionType.isEmpty()) {
            actionType = ActionTypeInferrer.inferActionType(template.getContent());
        }
        task.setActionType(actionType);
        
        // 设置完成条件（优先使用模板值，否则解析）
        String completionType = template.getCompletionType();
        int completionTarget = template.getCompletionTarget();
        if (completionType == null || completionType.isEmpty()) {
            CompletionCondition condition = CompletionConditionParser.parse(template.getContent());
            completionType = condition.type;
            completionTarget = condition.target;
        }
        task.setCompletionType(completionType);
        task.setCompletionTarget(completionTarget);
        task.setCurrentProgress(0);
        
        tasks.add(task);
    }
}
```

**验收标准**：
- 新生成的任务包含正确的 actionType、completionType、completionTarget

---

## 阶段3：模块集成（优先级：P1）

### 任务 3.1：集成 VocabularyActivity

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/VocabularyActivity.java`

**修改内容**：
```java
private void selectOption(int optionIndex) {
    // ... 原有答题逻辑 ...
    
    // 替换旧代码：TaskCompletionManager.getInstance(this).incrementVocabularyCount();
    // 新代码：
    TaskProgressTracker.getInstance(this).recordProgress("vocabulary_training", 1);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    // 删除：TaskCompletionHelper.markTaskAsCompleted(this, "vocabulary_training");
    // ... 其他清理代码 ...
}
```

**验收标准**：
- 每答一题进度+1
- 达到目标自动完成任务

---

### 任务 3.2：集成 MockExamActivity

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/MockExamActivity.java`

**修改内容**：
```java
private void selectOption(int selectedOption) {
    // ... 原有答题逻辑 ...
    
    // 替换旧代码：TaskCompletionManager.getInstance(this).incrementExamAnswerCount();
    // 新代码：
    TaskProgressTracker.getInstance(this).recordProgress("mock_exam", 1);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    // 删除：TaskCompletionHelper.markTaskAsCompleted(this, "exam_practice");
    // ... 其他清理代码 ...
}
```

**验收标准**：
- 每答一题进度+1
- actionType 使用 "mock_exam"（不是 "exam_practice"）

---

### 任务 3.3：集成 DailySentenceActivity

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/DailySentenceActivity.java`

**修改内容**：
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ... 原有初始化 ...
    
    // 替换旧代码：TaskCompletionManager.getInstance(this).markDailySentenceCompleted();
    // 新代码：
    TaskProgressTracker.getInstance(this).markSimpleTaskCompleted("daily_sentence");
}
```

**验收标准**：
- 进入页面自动完成任务
- 数据库中任务状态正确更新

---

### 任务 3.4：集成 ExamAnswerActivity

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/ExamAnswerActivity.java`

**修改内容**：
```java
private void submitExam() {
    // ... 原有提交逻辑 ...
    
    // 新增：记录完成一套真题
    TaskProgressTracker.getInstance(this).recordProgress("real_exam", 1);
}
```

**验收标准**：
- 提交真题后进度+1
- 中途退出不计入进度

---

### 任务 3.5：集成 WrongQuestionPracticeActivity

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/WrongQuestionPracticeActivity.java`

**修改内容**：
```java
private void selectOption(int selectedOption) {
    // ... 原有答题逻辑 ...
    
    // 新增：记录进度
    TaskProgressTracker.getInstance(this).recordProgress("wrong_question_practice", 1);
}
```

**验收标准**：
- 每答一题进度+1
- 达到目标自动完成任务

---

### 任务 3.6：集成 TextTranslationActivity

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/TextTranslationActivity.java`

**修改内容**：
```java
private void saveToHistory(String sourceText, String translatedText) {
    // ... 原有保存逻辑 ...
    
    // 新增：记录进度
    TaskProgressTracker.getInstance(this).recordProgress("translation_practice", 1);
}
```

**验收标准**：
- 每完成一次翻译进度+1
- 达到目标自动完成任务

---

## 阶段4：AI集成（优先级：P1）

### 任务 4.1：增强 StructuredPlanPromptBuilder

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/plan/StructuredPlanPromptBuilder.java`

**修改内容**：
1. 添加 `appendFunctionConstraints()` 方法，说明支持的6种任务类型
2. 修改 `appendJsonFormat()` 方法，要求返回 actionType、completionType、completionTarget 字段
3. 添加禁止生成的任务类型说明

**验收标准**：
- AI 生成的任务包含正确的字段
- 不会生成应用不支持的任务类型

---

### 任务 4.2：增强 StructuredPlanParser

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/plan/StructuredPlanParser.java`

**修改内容**：
```java
public static class TaskTemplate {
    public String content;
    public int minutes;
    public String actionType;        // 新增
    public String completionType;    // 新增
    public int completionTarget;     // 新增
    
    public static TaskTemplate fromJson(JSONObject json) {
        // 解析新字段
        template.actionType = json.optString("actionType", "");
        template.completionType = json.optString("completionType", "");
        template.completionTarget = json.optInt("completionTarget", 0);
        
        // AI未返回时智能推断
        if (template.actionType.isEmpty()) {
            template.actionType = ActionTypeInferrer.inferActionType(template.content);
        }
        if (template.completionType.isEmpty()) {
            CompletionCondition condition = CompletionConditionParser.parse(template.content);
            template.completionType = condition.type;
            template.completionTarget = condition.target;
        }
    }
}
```

**验收标准**：
- 正确解析 AI 返回的字段
- AI 未返回时能智能推断

---

## 阶段5：UI优化（优先级：P2）

### 任务 5.1：优化 DailyTaskActivity 任务点击跳转

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/DailyTaskActivity.java`

**修改内容**：
```java
private void handleTaskClick(DailyTask task) {
    String actionType = task.getActionType();
    
    if (actionType != null && !actionType.isEmpty()) {
        Class<?> targetActivity = ActionTypeInferrer.getTargetActivity(actionType);
        if (targetActivity != null) {
            String description = ActionTypeInferrer.getActionDescription(actionType);
            Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, targetActivity));
            return;
        }
    }
    
    // 兜底处理
    // ...
}
```

**验收标准**：
- 点击任务跳转到正确的功能页面
- 显示正确的提示文字

---

### 任务 5.2：优化任务列表进度显示

- [x] 完成

**文件**：`app/src/main/java/com/example/mybighomework/adapter/DailyTaskAdapter.java`

**修改内容**：
```java
@Override
public void onBindViewHolder(ViewHolder holder, int position) {
    DailyTask task = tasks.get(position);
    
    // 显示进度
    String progressText = formatProgress(task);
    holder.tvProgress.setText(progressText);
    
    // 根据完成状态设置样式
    if (task.isCompleted()) {
        holder.itemView.setAlpha(0.6f);
        holder.ivStatus.setImageResource(R.drawable.ic_check_circle);
    } else {
        holder.itemView.setAlpha(1.0f);
        holder.ivStatus.setImageResource(R.drawable.ic_circle_outline);
    }
}

private String formatProgress(DailyTask task) {
    String type = task.getCompletionType();
    int current = task.getCurrentProgress();
    int target = task.getCompletionTarget();
    
    if ("count".equals(type)) {
        return String.format("已完成 %d/%d", current, target);
    } else if ("duration".equals(type)) {
        return String.format("已练习 %d/%d 分钟", current, target);
    } else {
        return task.isCompleted() ? "已完成" : "未完成";
    }
}
```

**验收标准**：
- 数量型任务显示 "已完成 X/Y"
- 时长型任务显示 "已练习 X/Y 分钟"
- 简单型任务显示 "已完成" 或 "未完成"

---

## 阶段6：清理和测试（优先级：P2）

### 任务 6.1：废弃旧代码

- [x] 完成

**修改内容**：

1. **TaskCompletionManager.java**：添加 @Deprecated 注解，保留但不再使用
```java
@Deprecated
public class TaskCompletionManager {
    // 保留代码，添加注释说明已废弃
}
```

2. **各 Activity 中的旧调用**：确保已全部替换为 TaskProgressTracker

**验收标准**：
- 旧代码标记为废弃
- 应用正常运行，无旧代码调用

---

### 任务 6.2：功能测试

- [x] 完成

**测试用例**：

| 测试场景 | 操作 | 预期结果 |
|---------|------|---------|
| 词汇训练进度 | 答10道题 | 进度显示 10/20（假设目标20） |
| 词汇训练完成 | 答20道题 | 任务自动标记完成 |
| 模拟考试进度 | 答5道题 | 进度显示 5/20 |
| 真题练习完成 | 提交1套真题 | 任务自动标记完成 |
| 真题中途退出 | 不提交退出 | 进度不变 |
| 每日一句完成 | 进入页面 | 任务自动标记完成 |
| 错题练习进度 | 答3道错题 | 进度显示 3/10 |
| 翻译练习进度 | 翻译2次 | 进度显示 2/5 |
| 任务点击跳转 | 点击词汇任务 | 跳转到 VocabularyActivity |
| 返回刷新 | 从功能页返回 | 任务列表状态更新 |

**验收标准**：
- 所有测试用例通过
- 无崩溃或数据不一致问题

---

## 依赖关系

```
阶段1（数据层）
    │
    ▼
阶段2（核心组件）
    │
    ├──────────────┬──────────────┐
    ▼              ▼              ▼
阶段3          阶段4          阶段5
(模块集成)     (AI集成)       (UI优化)
    │              │              │
    └──────────────┴──────────────┘
                   │
                   ▼
              阶段6（清理测试）
```

---

## 风险和注意事项

1. **数据库迁移**：确保迁移脚本正确，避免数据丢失
2. **向后兼容**：旧任务（无新字段）需要能正常显示和使用
3. **线程安全**：TaskProgressTracker 的数据库操作需在后台线程执行
4. **性能**：避免频繁的数据库查询，考虑缓存今日任务

