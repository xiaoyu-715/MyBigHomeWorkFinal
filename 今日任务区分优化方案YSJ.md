# 今日任务区分优化方案YSJ

## 📋 问题分析

### 当前情况

应用中有两个"今日任务"的概念：

1. **主页的今日任务（DailyTaskActivity）**
   - 显示所有活跃学习计划的今日任务合集
   - 用户从主页点击"今日任务"进入

2. **学习计划详情的今日任务（PlanDetailActivity）**
   - 显示单个学习计划的今日任务
   - 用户从学习计划列表点击某个计划进入

### 问题

用户可能会混淆这两种任务，不清楚它们的区别和关系。

---

## 💡 区分方案

### 方案设计

**核心思路：** 通过标题、说明文字和任务来源标识来明确区分

#### 1. 修改主页今日任务的标题和说明

**修改DailyTaskActivity：**

```
标题：今日任务（全部计划）
说明：这里显示您所有活跃学习计划的今日任务汇总
```

**如果有数据库任务：**
```
┌─────────────────────────────────────┐
│  今日任务（全部计划）                │
├─────────────────────────────────────┤
│  📅 2025年12月19日                  │
│  📊 今日进度：3/8                    │
│                                     │
│  💡 提示：这里汇总了所有学习计划的   │
│  今日任务，点击任务可查看所属计划    │
├─────────────────────────────────────┤
│  任务清单                            │
│                                     │
│  ☐ [六级备考] 词汇学习50个           │
│  ☐ [雅思准备] 听力训练30分钟         │
│  ☐ [六级备考] 阅读练习2篇            │
│  ...                                │
└─────────────────────────────────────┘
```

**如果没有数据库任务（使用默认任务）：**
```
┌─────────────────────────────────────┐
│  今日任务（默认）                    │
├─────────────────────────────────────┤
│  📅 2025年12月19日                  │
│  📊 今日进度：0/3                    │
│                                     │
│  💡 提示：您还没有创建学习计划，     │
│  这里显示的是默认任务。              │
│  建议使用AI助手生成个性化学习计划！  │
├─────────────────────────────────────┤
│  任务清单                            │
│                                     │
│  ☐ 词汇练习 - 完成20个单词学习       │
│  ☐ 模拟考试练习 - 完成20次答题       │
│  ☐ 每日一句练习 - 打开学习页面       │
└─────────────────────────────────────┘
```

#### 2. 在任务项中显示所属计划

**修改任务显示格式：**

```
数据库任务格式：
[计划名称] 任务内容 - 预计时长

示例：
☐ [六级备考] 词汇学习50个单词 - 预计30分钟
☐ [雅思准备] 听力训练 - 预计30分钟

默认任务格式：
任务标题 - 任务描述

示例：
☐ 词汇练习 - 完成20个单词学习
☐ 模拟考试练习 - 完成20次答题
```

#### 3. 添加任务来源提示

在DailyTaskActivity顶部添加提示卡片：

```xml
<androidx.cardview.widget.CardView
    android:id="@+id/cardTaskSource"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp"
    android:visibility="gone">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:background="#E3F2FD">
        
        <TextView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:text="💡"
            android:textSize="18sp" />
        
        <TextView
            android:id="@+id/tvTaskSourceHint"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="8dp"
            android:textSize="13sp"
            android:textColor="#1976D2" />
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

---

## 🔧 实施步骤

### 步骤1：修改DailyTaskActivity布局

添加任务来源提示卡片和修改标题。

### 步骤2：修改DailyTaskActivity逻辑

```java
private void loadTasksFromDatabase() {
    // ... 查询数据库任务
    
    runOnUiThread(() -> {
        if (!dbTasks.isEmpty()) {
            // 使用数据库任务
            useDatabase = true;
            showTaskSourceHint(true, dbTasks.size());
            loadTasksFromEntities(dbTasks);
        } else {
            // 使用默认任务
            useDatabase = false;
            showTaskSourceHint(false, 0);
            loadDefaultTasks();
        }
    });
}

private void showTaskSourceHint(boolean fromDatabase, int taskCount) {
    CardView cardTaskSource = findViewById(R.id.cardTaskSource);
    TextView tvTaskSourceHint = findViewById(R.id.tvTaskSourceHint);
    
    cardTaskSource.setVisibility(View.VISIBLE);
    
    if (fromDatabase) {
        tvTaskSourceHint.setText(
            "这里汇总了您所有学习计划的今日任务（共" + taskCount + "个）。" +
            "点击任务可查看详情。"
        );
    } else {
        tvTaskSourceHint.setText(
            "您还没有创建学习计划，这里显示的是默认任务。\n" +
            "建议使用AI助手生成个性化学习计划！"
        );
    }
}
```

### 步骤3：修改任务显示格式

在从数据库加载任务时，添加计划名称前缀：

```java
private void loadTasksFromEntities(List<DailyTaskEntity> entities) {
    taskList.clear();
    
    for (DailyTaskEntity entity : entities) {
        // 查询计划名称
        String planName = getPlanName(entity.getPlanId());
        
        // 构建任务标题（包含计划名称）
        String title = "[" + planName + "] " + entity.getTaskContent();
        String description = "预计" + entity.getEstimatedMinutes() + "分钟";
        
        DailyTask task = new DailyTask(
            title,
            description,
            "task_" + entity.getId(),
            entity.isCompleted()
        );
        task.setTaskId(entity.getId());
        task.setPlanId(entity.getPlanId());  // 新增字段
        taskList.add(task);
    }
}

private String getPlanName(int planId) {
    try {
        StudyPlanEntity plan = database.studyPlanDao().getPlanById(planId);
        return plan != null ? plan.getTitle() : "未知计划";
    } catch (Exception e) {
        return "计划" + planId;
    }
}
```

### 步骤4：添加任务点击跳转到计划详情

```java
private void handleTaskClick(DailyTask task, int position) {
    if (useDatabase && task.getPlanId() > 0) {
        // 数据库任务：跳转到计划详情
        Intent intent = new Intent(this, PlanDetailActivity.class);
        intent.putExtra(PlanDetailActivity.EXTRA_PLAN_ID, task.getPlanId());
        startActivity(intent);
    } else {
        // 默认任务：跳转到对应功能
        Intent intent = null;
        switch (task.getType()) {
            case "vocabulary":
                intent = new Intent(this, VocabularyActivity.class);
                break;
            case "exam_practice":
                intent = new Intent(this, MockExamActivity.class);
                break;
            case "daily_sentence":
                intent = new Intent(this, DailySentenceActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
```

---

## 📊 对比表

| 特性 | 主页今日任务 | 计划详情今日任务 |
|------|-------------|----------------|
| **入口** | 主页"今日任务"按钮 | 学习计划列表→某个计划 |
| **范围** | 所有活跃计划的任务 | 单个计划的任务 |
| **标题** | 今日任务（全部计划） | [计划名称]今日任务 |
| **任务格式** | [计划名] 任务内容 | 任务内容 |
| **点击行为** | 跳转到计划详情 | 执行任务 |
| **提示** | 显示任务来源说明 | 显示计划进度 |

---

## ✅ 优化效果

### 优化后的用户体验

**场景1：用户有多个学习计划**
- 主页今日任务：看到所有计划的任务汇总，一目了然
- 计划详情今日任务：专注于单个计划的任务

**场景2：用户没有学习计划**
- 主页今日任务：显示默认任务+提示创建计划
- 引导用户使用AI助手生成计划

**场景3：用户点击任务**
- 数据库任务：跳转到所属计划详情，查看完整信息
- 默认任务：直接跳转到功能页面

---

**方案设计完成，等待实施。**
