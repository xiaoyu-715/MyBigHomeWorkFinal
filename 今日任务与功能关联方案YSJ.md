# 今日任务与功能关联方案YSJ

## 📋 需求分析

### 用户需求

AI生成的今日任务（如"每日学习新词汇 20分钟"）需要：
1. **与应用功能关联** - 任务对应具体的应用功能
2. **自动更新进度** - 用户完成功能后，任务自动标记为完成

### 当前问题

AI生成的任务内容是自由文本（如"每日学习新词汇"），无法直接与应用功能（VocabularyActivity）关联。

---

## 💡 解决方案

### 方案设计

**核心思路：** 为DailyTaskEntity添加actionType字段，建立任务与功能的映射关系。

```
DailyTaskEntity
├── taskContent: "每日学习新词汇"（显示文本）
├── actionType: "vocabulary_training"（功能标识）
├── estimatedMinutes: 20
└── isCompleted: false

用户完成VocabularyActivity
    ↓
检查今日是否有actionType="vocabulary_training"的任务
    ↓
如果有，自动标记为完成
    ↓
更新DailyTaskEntity.isCompleted = true
```

---

## 🔧 技术实现

### 步骤1：扩展DailyTaskEntity

```java
@Entity(tableName = "daily_tasks")
public class DailyTaskEntity {
    // ... 现有字段
    
    /** 操作类型（用于关联应用功能） */
    private String actionType;  // 新增字段
    
    // vocabulary_training - 词汇训练
    // exam_practice - 真题练习
    // daily_sentence - 每日一句
    // grammar_review - 语法复习
    // writing_practice - 写作练习
    // listening_practice - 听力练习
}
```

### 步骤2：智能任务内容解析

在TaskGenerationService或StructuredPlanParser中，根据任务内容智能识别actionType：

```java
private String detectActionType(String taskContent) {
    String content = taskContent.toLowerCase();
    
    if (content.contains("词汇") || content.contains("单词")) {
        return "vocabulary_training";
    } else if (content.contains("真题") || content.contains("考试")) {
        return "exam_practice";
    } else if (content.contains("每日一句")) {
        return "daily_sentence";
    } else if (content.contains("语法")) {
        return "grammar_review";
    } else if (content.contains("写作")) {
        return "writing_practice";
    } else if (content.contains("听力")) {
        return "listening_practice";
    }
    
    return "general"; // 通用任务
}
```

### 步骤3：功能完成时自动标记任务

在VocabularyActivity等功能页面的onDestroy或完成逻辑中：

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    
    // 标记今日任务为完成
    markTodayTaskAsCompleted("vocabulary_training");
}

private void markTodayTaskAsCompleted(String actionType) {
    new Thread(() -> {
        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
            
            AppDatabase database = AppDatabase.getInstance(this);
            DailyTaskDao taskDao = database.dailyTaskDao();
            
            // 查找今日该类型的任务
            List<DailyTaskEntity> tasks = taskDao.getTasksByActionType(actionType, today);
            
            if (tasks != null && !tasks.isEmpty()) {
                for (DailyTaskEntity task : tasks) {
                    if (!task.isCompleted()) {
                        task.setCompleted(true);
                        task.setCompletedAt(System.currentTimeMillis());
                        taskDao.update(task);
                        
                        Log.d("TaskCompletion", "自动标记任务完成: " + task.getTaskContent());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("TaskCompletion", "标记任务失败", e);
        }
    }).start();
}
```

### 步骤4：在DailyTaskDao中添加查询方法

```java
@Dao
public interface DailyTaskDao {
    // ... 现有方法
    
    /**
     * 根据操作类型和日期查询任务
     */
    @Query("SELECT * FROM daily_tasks WHERE actionType = :actionType AND date = :date")
    List<DailyTaskEntity> getTasksByActionType(String actionType, String date);
}
```

---

## 📋 实施步骤

### P0 - 立即实现

1. [ ] 在DailyTaskEntity中添加actionType字段
2. [ ] 在DailyTaskDao中添加getTasksByActionType查询方法
3. [ ] 在StructuredPlanParser中实现智能actionType检测
4. [ ] 创建TaskCompletionHelper工具类
5. [ ] 在VocabularyActivity中集成自动标记功能

### P1 - 后续实现

6. [ ] 在其他功能页面集成自动标记
7. [ ] 优化actionType检测算法
8. [ ] 添加任务完成通知

---

## 🎯 预期效果

**用户体验：**

```
1. AI生成学习计划
   - 计划包含："每日学习新词汇 20分钟"

2. 系统创建今日任务
   - taskContent: "每日学习新词汇"
   - actionType: "vocabulary_training"
   - isCompleted: false

3. 用户打开词汇训练
   - 学习20个单词

4. 用户退出词汇训练
   - 系统自动检测今日有"vocabulary_training"任务
   - 自动标记任务为完成
   - isCompleted: true ✅

5. 用户返回今日任务页面
   - 看到"每日学习新词汇"已完成 ✅
   - 进度自动更新：1/3
```

---

**方案设计完成，建议立即实施。**
