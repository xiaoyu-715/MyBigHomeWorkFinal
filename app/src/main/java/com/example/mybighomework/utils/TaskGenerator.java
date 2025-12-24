package com.example.mybighomework.utils;

import android.util.Log;

import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务生成器
 * 根据阶段的任务模板生成每日具体任务
 * 
 * Requirements: 6.1, 6.2, 6.3
 */
public class TaskGenerator {
    
    private static final String TAG = "TaskGenerator";
    
    /**
     * 任务模板数据类
     * 用于存储从JSON解析出的任务模板信息
     */
    public static class TaskTemplate {
        private final String content;
        private final int minutes;
        private final String actionType; // 用于关联应用功能，实现自动完成
        private final String completionType; // 完成类型：count/duration/simple
        private final int completionTarget; // 完成目标值
        
        public TaskTemplate(String content, int minutes) {
            this(content, minutes, null, null, 0);
        }
        
        public TaskTemplate(String content, int minutes, String actionType) {
            this(content, minutes, actionType, null, 0);
        }
        
        public TaskTemplate(String content, int minutes, String actionType, 
                           String completionType, int completionTarget) {
            this.content = content;
            this.minutes = minutes;
            this.actionType = actionType;
            this.completionType = completionType;
            this.completionTarget = completionTarget;
        }
        
        public String getContent() {
            return content;
        }
        
        public int getMinutes() {
            return minutes;
        }
        
        public String getActionType() {
            return actionType;
        }
        
        public String getCompletionType() {
            return completionType;
        }
        
        public int getCompletionTarget() {
            return completionTarget;
        }
        
        @Override
        public String toString() {
            return "TaskTemplate{content='" + content + "', minutes=" + minutes + 
                   ", actionType='" + actionType + "', completionType='" + completionType +
                   "', completionTarget=" + completionTarget + "}";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TaskTemplate that = (TaskTemplate) obj;
            return minutes == that.minutes && 
                   (content != null ? content.equals(that.content) : that.content == null);
        }
        
        @Override
        public int hashCode() {
            int result = content != null ? content.hashCode() : 0;
            result = 31 * result + minutes;
            return result;
        }
    }
    
    /**
     * 为指定日期生成任务
     * 根据当前阶段的任务模板生成具体的每日任务
     * 
     * @param plan 学习计划
     * @param currentPhase 当前阶段
     * @param date 任务日期（yyyy-MM-dd格式）
     * @return 生成的任务列表，如果模板为空或解析失败则返回空列表
     * 
     * Requirements: 6.1, 6.3
     */
    public List<DailyTaskEntity> generateTasksForDate(
            StudyPlanEntity plan,
            StudyPhaseEntity currentPhase,
            String date) {
        
        List<DailyTaskEntity> tasks = new ArrayList<>();
        
        // 参数校验
        if (plan == null || currentPhase == null || date == null || date.isEmpty()) {
            Log.w(TAG, "generateTasksForDate: 参数无效");
            return tasks;
        }
        
        // 解析任务模板
        List<TaskTemplate> templates = parseTaskTemplates(currentPhase.getTaskTemplateJson());
        
        if (templates.isEmpty()) {
            Log.w(TAG, "generateTasksForDate: 任务模板为空，planId=" + plan.getId() 
                    + ", phaseId=" + currentPhase.getId());
            return tasks;
        }
        
        // 根据模板生成任务
        int order = 0;
        for (TaskTemplate template : templates) {
            DailyTaskEntity task = new DailyTaskEntity();
            task.setPlanId(plan.getId());
            task.setPhaseId(currentPhase.getId());
            task.setDate(date);
            task.setTaskContent(template.getContent());
            task.setEstimatedMinutes(template.getMinutes());
            task.setActualMinutes(0);
            task.setCompleted(false);
            task.setCompletedAt(0);
            task.setTaskOrder(order++);
            
            // 设置actionType（优先使用模板值，否则智能推断）
            String actionType = template.getActionType();
            if (actionType == null || actionType.isEmpty()) {
                actionType = ActionTypeInferrer.inferActionType(template.getContent());
            }
            task.setActionType(actionType);
            
            // 设置完成条件（优先使用模板值，否则解析）
            String completionType = template.getCompletionType();
            int completionTarget = template.getCompletionTarget();
            
            if (completionType == null || completionType.isEmpty() || completionTarget <= 0) {
                // 从任务内容解析完成条件
                CompletionConditionParser.CompletionCondition condition = 
                    CompletionConditionParser.parse(template.getContent());
                completionType = condition.type;
                completionTarget = condition.target;
            }
            
            task.setCompletionType(completionType);
            task.setCompletionTarget(completionTarget);
            task.setCurrentProgress(0);
            
            tasks.add(task);
        }
        
        Log.d(TAG, "generateTasksForDate: 生成了 " + tasks.size() + " 个任务，日期=" + date);
        return tasks;
    }

    
    /**
     * 解析JSON任务模板
     * 将阶段中存储的JSON格式任务模板解析为TaskTemplate列表
     * 
     * JSON格式示例：
     * [
     *   {"content": "听力材料精听", "minutes": 20},
     *   {"content": "跟读模仿练习", "minutes": 15}
     * ]
     * 
     * @param taskTemplateJson JSON格式的任务模板字符串
     * @return 解析后的任务模板列表，如果解析失败则返回空列表
     * 
     * Requirements: 6.1
     */
    public List<TaskTemplate> parseTaskTemplates(String taskTemplateJson) {
        List<TaskTemplate> templates = new ArrayList<>();
        
        // 空值检查
        if (taskTemplateJson == null || taskTemplateJson.trim().isEmpty()) {
            Log.w(TAG, "parseTaskTemplates: 任务模板JSON为空");
            return templates;
        }
        
        try {
            JSONArray jsonArray = new JSONArray(taskTemplateJson);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject taskObj = jsonArray.getJSONObject(i);
                
                // 获取任务内容，支持"content"和"taskContent"两种字段名
                String content = taskObj.optString("content", "");
                if (content.isEmpty()) {
                    content = taskObj.optString("taskContent", "");
                }
                
                // 获取预计时长，支持"minutes"和"estimatedMinutes"两种字段名
                int minutes = taskObj.optInt("minutes", 0);
                if (minutes == 0) {
                    minutes = taskObj.optInt("estimatedMinutes", 30); // 默认30分钟
                }
                
                // 获取actionType（用于自动完成功能）
                String actionType = taskObj.optString("actionType", "");
                
                // 获取完成条件字段
                String completionType = taskObj.optString("completionType", "");
                int completionTarget = taskObj.optInt("completionTarget", 0);
                
                // 跳过无效的任务模板
                if (content.isEmpty()) {
                    Log.w(TAG, "parseTaskTemplates: 跳过无效任务模板，索引=" + i);
                    continue;
                }
                
                templates.add(new TaskTemplate(content, minutes, actionType, completionType, completionTarget));
            }
            
            Log.d(TAG, "parseTaskTemplates: 成功解析 " + templates.size() + " 个任务模板");
            
        } catch (JSONException e) {
            Log.e(TAG, "parseTaskTemplates: JSON解析失败", e);
        }
        
        return templates;
    }
    
    /**
     * 检查是否需要为指定日期生成任务
     * 用于避免重复生成任务
     * 
     * @param plan 学习计划
     * @param currentPhase 当前阶段
     * @param date 日期（yyyy-MM-dd格式）
     * @param hasExistingTasks 该日期是否已有任务（由调用方通过DAO查询）
     * @return true表示需要生成任务，false表示不需要
     * 
     * Requirements: 6.2, 6.4
     */
    public boolean shouldGenerateTasks(
            StudyPlanEntity plan,
            StudyPhaseEntity currentPhase,
            String date,
            boolean hasExistingTasks) {
        
        // 参数校验
        if (plan == null || currentPhase == null || date == null || date.isEmpty()) {
            Log.w(TAG, "shouldGenerateTasks: 参数无效");
            return false;
        }
        
        // 如果已有任务，不需要重复生成（幂等性保证）
        if (hasExistingTasks) {
            Log.d(TAG, "shouldGenerateTasks: 日期 " + date + " 已有任务，跳过生成");
            return false;
        }
        
        // 检查计划状态：已暂停或已完成的计划不生成新任务
        if (plan.isPaused()) {
            Log.d(TAG, "shouldGenerateTasks: 计划已暂停，不生成任务");
            return false;
        }
        
        if (plan.isCompleted()) {
            Log.d(TAG, "shouldGenerateTasks: 计划已完成，不生成任务");
            return false;
        }
        
        // 检查阶段状态：已完成的阶段不生成新任务
        if (currentPhase.isCompleted()) {
            Log.d(TAG, "shouldGenerateTasks: 阶段已完成，不生成任务");
            return false;
        }
        
        // 检查任务模板是否存在
        String templateJson = currentPhase.getTaskTemplateJson();
        if (templateJson == null || templateJson.trim().isEmpty()) {
            Log.w(TAG, "shouldGenerateTasks: 阶段没有任务模板");
            return false;
        }
        
        // 检查日期是否在阶段的有效范围内
        if (!isDateInPhaseRange(currentPhase, date)) {
            Log.d(TAG, "shouldGenerateTasks: 日期 " + date + " 不在阶段有效范围内");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查日期是否在阶段的有效范围内
     * 
     * @param phase 学习阶段
     * @param date 日期（yyyy-MM-dd格式）
     * @return true表示日期在范围内
     */
    private boolean isDateInPhaseRange(StudyPhaseEntity phase, String date) {
        String startDate = phase.getStartDate();
        String endDate = phase.getEndDate();
        
        // 如果阶段没有设置日期范围，默认允许
        if (startDate == null || startDate.isEmpty() || 
            endDate == null || endDate.isEmpty()) {
            return true;
        }
        
        // 字符串比较（yyyy-MM-dd格式可以直接比较）
        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
    }
    
    /**
     * 将任务模板列表序列化为JSON字符串
     * 用于保存任务模板到数据库
     * 
     * @param templates 任务模板列表
     * @return JSON格式字符串
     */
    public String serializeTaskTemplates(List<TaskTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return "[]";
        }
        
        JSONArray jsonArray = new JSONArray();
        
        for (TaskTemplate template : templates) {
            try {
                JSONObject taskObj = new JSONObject();
                taskObj.put("content", template.getContent());
                taskObj.put("minutes", template.getMinutes());
                if (template.getActionType() != null && !template.getActionType().isEmpty()) {
                    taskObj.put("actionType", template.getActionType());
                }
                if (template.getCompletionType() != null && !template.getCompletionType().isEmpty()) {
                    taskObj.put("completionType", template.getCompletionType());
                }
                if (template.getCompletionTarget() > 0) {
                    taskObj.put("completionTarget", template.getCompletionTarget());
                }
                jsonArray.put(taskObj);
            } catch (JSONException e) {
                Log.e(TAG, "serializeTaskTemplates: 序列化失败", e);
            }
        }
        
        return jsonArray.toString();
    }
    
    /**
     * 根据任务内容智能推断actionType
     * 用于自动完成功能的关联
     * 
     * @param taskContent 任务内容
     * @return 推断的actionType，如果无法推断则返回null
     * @deprecated 使用 {@link ActionTypeInferrer#inferActionType(String)} 代替
     */
    @Deprecated
    private String inferActionType(String taskContent) {
        // 委托给 ActionTypeInferrer
        return ActionTypeInferrer.inferActionType(taskContent);
    }
    
    /**
     * 为阶段的多个日期批量生成任务
     * 
     * @param plan 学习计划
     * @param phase 学习阶段
     * @param dates 日期列表
     * @param existingTaskDates 已有任务的日期集合（用于避免重复）
     * @return 生成的所有任务列表
     */
    public List<DailyTaskEntity> generateTasksForDates(
            StudyPlanEntity plan,
            StudyPhaseEntity phase,
            List<String> dates,
            java.util.Set<String> existingTaskDates) {
        
        List<DailyTaskEntity> allTasks = new ArrayList<>();
        
        if (dates == null || dates.isEmpty()) {
            return allTasks;
        }
        
        for (String date : dates) {
            boolean hasExisting = existingTaskDates != null && existingTaskDates.contains(date);
            
            if (shouldGenerateTasks(plan, phase, date, hasExisting)) {
                List<DailyTaskEntity> tasks = generateTasksForDate(plan, phase, date);
                allTasks.addAll(tasks);
            }
        }
        
        Log.d(TAG, "generateTasksForDates: 共生成 " + allTasks.size() + " 个任务");
        return allTasks;
    }
}
