# 设计文档：智能任务完成识别系统

## 概述

本设计文档描述智能任务完成识别系统的技术实现方案。系统能够根据任务内容智能推断操作类型，追踪用户学习进度，并在达到目标后自动标记任务完成。

## 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer                                │
│  DailyTaskActivity │ VocabularyActivity │ MockExamActivity   │
│  ExamAnswerActivity │ WrongQuestionPracticeActivity │ ...    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Business Logic Layer                        │
│  TaskProgressTracker │ ActionTypeInferrer │ CompletionParser │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   AI Integration Layer                       │
│      StructuredPlanPromptBuilder │ StructuredPlanParser      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Access Layer                         │
│              DailyTaskDao │ DailyTaskEntity                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 数据模型

### DailyTaskEntity 字段

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | int | 主键 |
| planId | int | 所属计划ID |
| phaseId | int | 所属阶段ID |
| date | String | 任务日期 (yyyy-MM-dd) |
| taskContent | String | 任务内容 |
| actionType | String | 操作类型 |
| completionType | String | 完成类型：count/duration/simple |
| completionTarget | int | 完成目标值 |
| currentProgress | int | 当前进度 |
| estimatedMinutes | int | 预计时长（分钟） |
| actualMinutes | int | 实际时长（分钟） |
| completed | boolean | 是否已完成 |
| completedAt | long | 完成时间戳 |
| taskOrder | int | 任务顺序 |

### 操作类型映射表

| actionType | 目标Activity | completionType | 计数方式 |
|------------|-------------|----------------|----------|
| daily_sentence | DailySentenceActivity | simple | 进入即完成 |
| real_exam | ExamListActivity | count | 每提交一套+1 |
| mock_exam | MockExamActivity | count | 每答一题+1 |
| wrong_question_practice | WrongQuestionPracticeActivity | count | 每答一题+1 |
| vocabulary_training | VocabularyActivity | count | 每答一题+1 |
| translation_practice | TextTranslationActivity | count | 每翻译一次+1 |

---

## 核心组件设计

### 1. TaskProgressTracker（任务进度追踪器）

统一的任务进度追踪器，替代现有的 TaskCompletionManager。

```java
public class TaskProgressTracker {
    
    private static TaskProgressTracker instance;
    private Context context;
    private DailyTaskDao taskDao;
    
    public static synchronized TaskProgressTracker getInstance(Context context) {
        if (instance == null) {
            instance = new TaskProgressTracker(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 记录进度并检查是否完成
     * @param actionType 操作类型
     * @param increment 增量（通常为1）
     */
    public void recordProgress(String actionType, int increment) {
        new Thread(() -> {
            String today = getTodayDate();
            List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
            
            for (DailyTaskEntity task : tasks) {
                if (task.isCompleted()) continue;
                
                // 更新进度
                int newProgress = task.getCurrentProgress() + increment;
                task.setCurrentProgress(newProgress);
                
                // 检查是否达到目标
                if (newProgress >= task.getCompletionTarget()) {
                    task.setCompleted(true);
                    task.setCompletedAt(System.currentTimeMillis());
                }
                
                taskDao.update(task);
            }
        }).start();
    }
    
    /**
     * 标记简单型任务完成（进入页面即完成）
     * @param actionType 操作类型
     */
    public void markSimpleTaskCompleted(String actionType) {
        new Thread(() -> {
            String today = getTodayDate();
            List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
            
            for (DailyTaskEntity task : tasks) {
                if (!task.isCompleted() && "simple".equals(task.getCompletionType())) {
                    task.setCompleted(true);
                    task.setCompletedAt(System.currentTimeMillis());
                    task.setCurrentProgress(1);
                    taskDao.update(task);
                }
            }
        }).start();
    }
    
    /**
     * 获取今日指定类型任务的进度
     */
    public void getProgress(String actionType, ProgressCallback callback) {
        new Thread(() -> {
            String today = getTodayDate();
            List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
            
            int totalProgress = 0;
            int totalTarget = 0;
            for (DailyTaskEntity task : tasks) {
                totalProgress += task.getCurrentProgress();
                totalTarget += task.getCompletionTarget();
            }
            
            callback.onResult(totalProgress, totalTarget);
        }).start();
    }
    
    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
    
    public interface ProgressCallback {
        void onResult(int currentProgress, int targetProgress);
    }
}
```

### 2. ActionTypeInferrer（操作类型推断器）

```java
public class ActionTypeInferrer {
    
    // 按优先级排序的关键词映射
    private static final LinkedHashMap<String, List<String>> ACTION_KEYWORDS = new LinkedHashMap<>();
    
    static {
        // 优先级1：每日一句
        ACTION_KEYWORDS.put("daily_sentence", 
            Arrays.asList("每日一句", "今日一句", "句子跟读", "跟读练习"));
        
        // 优先级2：真题练习
        ACTION_KEYWORDS.put("real_exam", 
            Arrays.asList("真题", "考研真题", "真题套卷", "历年真题"));
        
        // 优先级3：模拟考试
        ACTION_KEYWORDS.put("mock_exam", 
            Arrays.asList("模拟考试", "模拟题", "四级模拟", "六级模拟"));
        
        // 优先级4：错题练习
        ACTION_KEYWORDS.put("wrong_question_practice", 
            Arrays.asList("错题", "错题复习", "错题巩固", "错题本"));
        
        // 优先级5：词汇训练
        ACTION_KEYWORDS.put("vocabulary_training", 
            Arrays.asList("词汇", "单词", "背单词", "记单词", "新词", "生词"));
        
        // 优先级6：翻译练习
        ACTION_KEYWORDS.put("translation_practice", 
            Arrays.asList("翻译", "中英互译", "英译中", "中译英", "翻译练习"));
    }
    
    /**
     * 根据任务内容推断操作类型
     */
    public static String inferActionType(String taskContent) {
        if (taskContent == null || taskContent.isEmpty()) {
            return null;
        }
        
        // 按优先级顺序匹配
        for (Map.Entry<String, List<String>> entry : ACTION_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (taskContent.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        
        // 模糊匹配：考试/测试 → mock_exam
        if ((taskContent.contains("考试") || taskContent.contains("测试")) 
            && !taskContent.contains("真题") && !taskContent.contains("模拟")) {
            return "mock_exam";
        }
        
        return null;
    }
    
    /**
     * 获取操作类型对应的Activity类
     */
    public static Class<?> getTargetActivity(String actionType) {
        switch (actionType) {
            case "vocabulary_training": return VocabularyActivity.class;
            case "mock_exam": return MockExamActivity.class;
            case "real_exam": return ExamListActivity.class;
            case "daily_sentence": return DailySentenceActivity.class;
            case "wrong_question_practice": return WrongQuestionPracticeActivity.class;
            case "translation_practice": return TextTranslationActivity.class;
            default: return null;
        }
    }
    
    /**
     * 获取操作类型的描述
     */
    public static String getActionDescription(String actionType) {
        switch (actionType) {
            case "vocabulary_training": return "进入词汇训练";
            case "mock_exam": return "进入模拟考试";
            case "real_exam": return "进入真题练习";
            case "daily_sentence": return "进入每日一句";
            case "wrong_question_practice": return "进入错题练习";
            case "translation_practice": return "进入翻译练习";
            default: return "查看计划详情";
        }
    }
    
    /**
     * 获取操作类型的默认完成类型
     */
    public static String getDefaultCompletionType(String actionType) {
        if ("daily_sentence".equals(actionType)) {
            return "simple";
        }
        return "count";
    }
}
```

### 3. CompletionConditionParser（完成条件解析器）

```java
public class CompletionConditionParser {
    
    /**
     * 从任务内容解析完成条件
     */
    public static CompletionCondition parse(String taskContent) {
        if (taskContent == null || taskContent.isEmpty()) {
            return new CompletionCondition("simple", 1);
        }
        
        // 解析数量：X个、X道、X套、X句、X次
        Pattern countPattern = Pattern.compile("(\\d+)\\s*(个|道|套|句|次)");
        Matcher countMatcher = countPattern.matcher(taskContent);
        if (countMatcher.find()) {
            int target = Integer.parseInt(countMatcher.group(1));
            return new CompletionCondition("count", target);
        }
        
        // 解析时长：X分钟
        Pattern durationPattern = Pattern.compile("(\\d+)\\s*分钟");
        Matcher durationMatcher = durationPattern.matcher(taskContent);
        if (durationMatcher.find()) {
            int target = Integer.parseInt(durationMatcher.group(1));
            return new CompletionCondition("duration", target);
        }
        
        // 默认简单型
        return new CompletionCondition("simple", 1);
    }
    
    public static class CompletionCondition {
        public String type;
        public int target;
        
        public CompletionCondition(String type, int target) {
            this.type = type;
            this.target = target;
        }
    }
}
```

---

## 各模块集成设计

### VocabularyActivity

```java
private void selectOption(int optionIndex) {
    // ... 原有答题逻辑 ...
    
    // 记录进度
    TaskProgressTracker.getInstance(this).recordProgress("vocabulary_training", 1);
}
```

### MockExamActivity

```java
private void selectOption(int selectedOption) {
    // ... 原有答题逻辑 ...
    
    // 记录进度
    TaskProgressTracker.getInstance(this).recordProgress("mock_exam", 1);
}

// 删除 onDestroy 中的 TaskCompletionHelper 调用
```

### ExamAnswerActivity

```java
private void submitExam() {
    // ... 原有提交逻辑 ...
    
    // 记录完成一套真题
    TaskProgressTracker.getInstance(this).recordProgress("real_exam", 1);
}
```

### DailySentenceActivity

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ... 原有初始化 ...
    
    // 标记简单型任务完成
    TaskProgressTracker.getInstance(this).markSimpleTaskCompleted("daily_sentence");
}
```

### WrongQuestionPracticeActivity

```java
private void selectOption(int selectedOption) {
    // ... 原有答题逻辑 ...
    
    // 记录进度
    TaskProgressTracker.getInstance(this).recordProgress("wrong_question_practice", 1);
}
```

### TextTranslationActivity

```java
private void saveToHistory(String sourceText, String translatedText) {
    // ... 原有保存逻辑 ...
    
    // 记录进度
    TaskProgressTracker.getInstance(this).recordProgress("translation_practice", 1);
}
```

---

## AI集成设计

### StructuredPlanPromptBuilder 增强

```java
private void appendFunctionConstraints(StringBuilder prompt) {
    prompt.append("【重要约束】你只能生成以下类型的任务：\n\n");
    
    prompt.append("1. 每日一句（actionType: daily_sentence, completionType: simple）\n");
    prompt.append("   示例：学习今日一句\n\n");
    
    prompt.append("2. 真题练习（actionType: real_exam, completionType: count）\n");
    prompt.append("   示例：完成1套真题\n\n");
    
    prompt.append("3. 模拟考试（actionType: mock_exam, completionType: count）\n");
    prompt.append("   示例：完成20道模拟题\n\n");
    
    prompt.append("4. 错题练习（actionType: wrong_question_practice, completionType: count）\n");
    prompt.append("   示例：复习10道错题\n\n");
    
    prompt.append("5. 词汇训练（actionType: vocabulary_training, completionType: count）\n");
    prompt.append("   示例：学习20个新单词\n\n");
    
    prompt.append("6. 翻译练习（actionType: translation_practice, completionType: count）\n");
    prompt.append("   示例：完成5个翻译练习\n\n");
    
    prompt.append("【禁止生成】\n");
    prompt.append("- 观看英语电影、阅读英文原著（需要外部资源）\n");
    prompt.append("- 与外国人对话、参加英语角（需要线下完成）\n");
    prompt.append("- 写作批改、口语评测、听力精听（应用不支持）\n\n");
}

private void appendJsonFormat(StringBuilder prompt) {
    prompt.append("任务JSON格式：\n");
    prompt.append("{\n");
    prompt.append("  \"content\": \"具体任务描述（必须包含数量）\",\n");
    prompt.append("  \"minutes\": 预计分钟数,\n");
    prompt.append("  \"actionType\": \"操作类型\",\n");
    prompt.append("  \"completionType\": \"count/duration/simple\",\n");
    prompt.append("  \"completionTarget\": 完成目标数值\n");
    prompt.append("}\n");
}
```

### StructuredPlanParser 增强

```java
public static class TaskTemplate {
    public String content;
    public int minutes;
    public String actionType;
    public String completionType;
    public int completionTarget;
    
    public static TaskTemplate fromJson(JSONObject json) {
        TaskTemplate template = new TaskTemplate();
        template.content = json.optString("content", "学习任务");
        template.minutes = json.optInt("minutes", 15);
        template.actionType = json.optString("actionType", "");
        template.completionType = json.optString("completionType", "");
        template.completionTarget = json.optInt("completionTarget", 0);
        
        // 如果AI未返回，智能推断
        if (template.actionType.isEmpty()) {
            template.actionType = ActionTypeInferrer.inferActionType(template.content);
        }
        if (template.completionType.isEmpty() || template.completionTarget == 0) {
            CompletionConditionParser.CompletionCondition condition = 
                CompletionConditionParser.parse(template.content);
            template.completionType = condition.type;
            template.completionTarget = condition.target;
        }
        
        return template;
    }
}
```

---

## DailyTaskActivity 增强

### 任务点击跳转

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
    
    // 兜底：跳转到计划详情页
    if (task.getPlanId() > 0) {
        Intent intent = new Intent(this, PlanDetailActivity.class);
        intent.putExtra("plan_id", task.getPlanId());
        startActivity(intent);
    }
}
```

### 进度显示

```java
private String formatProgress(DailyTask task) {
    String type = task.getCompletionType();
    int current = task.getCurrentProgress();
    int target = task.getCompletionTarget();
    
    if ("count".equals(type)) {
        return String.format("已完成 %d/%d", current, target);
    } else if ("duration".equals(type)) {
        return String.format("已练习 %d/%d 分钟", current, target);
    } else {
        return task.isCompleted() ? "✓ 已完成" : "○ 未完成";
    }
}
```

---

## 数据库迁移

```java
// Room Migration
static final Migration MIGRATION_X_Y = new Migration(X, Y) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE daily_tasks ADD COLUMN completionType TEXT DEFAULT 'simple'");
        database.execSQL("ALTER TABLE daily_tasks ADD COLUMN completionTarget INTEGER DEFAULT 1");
        database.execSQL("ALTER TABLE daily_tasks ADD COLUMN currentProgress INTEGER DEFAULT 0");
    }
};
```

---

## 废弃组件

以下组件将被废弃，由 TaskProgressTracker 统一替代：

| 废弃组件 | 替代方案 |
|---------|---------|
| TaskCompletionManager.incrementVocabularyCount() | TaskProgressTracker.recordProgress("vocabulary_training", 1) |
| TaskCompletionManager.incrementExamAnswerCount() | TaskProgressTracker.recordProgress("mock_exam", 1) |
| TaskCompletionManager.markDailySentenceCompleted() | TaskProgressTracker.markSimpleTaskCompleted("daily_sentence") |
| TaskCompletionHelper.markTaskAsCompleted() | TaskProgressTracker 内部自动处理 |

---

## 测试策略

### 单元测试

| 测试项 | 测试内容 |
|-------|---------|
| ActionTypeInferrer | 各种关键词的匹配正确性、优先级顺序 |
| CompletionConditionParser | 数量/时长/简单型的解析正确性 |
| TaskProgressTracker | 进度累计、完成判断、数据库更新 |

### 集成测试

| 测试项 | 测试内容 |
|-------|---------|
| 词汇训练流程 | 答题 → 进度更新 → 达标完成 |
| 真题练习流程 | 提交 → 进度更新 → 达标完成 |
| 每日一句流程 | 进入页面 → 自动完成 |
| 任务列表刷新 | 返回页面 → 状态更新 |

