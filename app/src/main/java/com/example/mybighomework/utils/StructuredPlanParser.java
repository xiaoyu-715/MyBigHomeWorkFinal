package com.example.mybighomework.utils;

import android.util.Log;

import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.database.entity.StudyPhaseEntity;
import com.example.mybighomework.database.entity.StudyPlanEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 结构化学习计划解析器
 * 用于解析AI返回的JSON数据，提取阶段和任务信息
 * 
 * Requirements: 2.2
 */
public class StructuredPlanParser {
    
    private static final String TAG = "StructuredPlanParser";
    
    /**
     * 解析结果类
     * 包含解析后的计划、阶段和任务模板
     */
    public static class ParseResult {
        public StudyPlanEntity plan;
        public List<StudyPhaseEntity> phases;
        public List<List<TaskTemplate>> taskTemplates; // 每个阶段的任务模板列表
        public boolean success;
        public String errorMessage;
        
        public ParseResult() {
            this.phases = new ArrayList<>();
            this.taskTemplates = new ArrayList<>();
            this.success = false;
        }
        
        public static ParseResult success(StudyPlanEntity plan, 
                                         List<StudyPhaseEntity> phases,
                                         List<List<TaskTemplate>> taskTemplates) {
            ParseResult result = new ParseResult();
            result.plan = plan;
            result.phases = phases;
            result.taskTemplates = taskTemplates;
            result.success = true;
            return result;
        }
        
        public static ParseResult error(String message) {
            ParseResult result = new ParseResult();
            result.success = false;
            result.errorMessage = message;
            return result;
        }
    }

    
    /**
     * 任务模板类
     * 用于存储阶段中的每日任务模板
     */
    public static class TaskTemplate {
        public String content;
        public int minutes;
        public String actionType;       // 操作类型：daily_sentence, real_exam, mock_exam等
        public String completionType;   // 完成类型：count, simple
        public int completionTarget;    // 完成目标数量
        
        public TaskTemplate() {}
        
        public TaskTemplate(String content, int minutes) {
            this.content = content;
            this.minutes = minutes;
            this.actionType = "";
            this.completionType = "";
            this.completionTarget = 0;
        }
        
        public TaskTemplate(String content, int minutes, String actionType, 
                           String completionType, int completionTarget) {
            this.content = content;
            this.minutes = minutes;
            this.actionType = actionType;
            this.completionType = completionType;
            this.completionTarget = completionTarget;
        }
        
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("content", content);
            json.put("minutes", minutes);
            if (actionType != null && !actionType.isEmpty()) {
                json.put("actionType", actionType);
            }
            if (completionType != null && !completionType.isEmpty()) {
                json.put("completionType", completionType);
            }
            if (completionTarget > 0) {
                json.put("completionTarget", completionTarget);
            }
            return json;
        }
        
        public static TaskTemplate fromJson(JSONObject json) throws JSONException {
            TaskTemplate template = new TaskTemplate();
            template.content = json.optString("content", "学习任务");
            template.minutes = json.optInt("minutes", 15);
            template.actionType = json.optString("actionType", "");
            template.completionType = json.optString("completionType", "");
            template.completionTarget = json.optInt("completionTarget", 0);
            
            // 如果AI未返回actionType，则智能推断
            if (template.actionType == null || template.actionType.isEmpty()) {
                template.actionType = ActionTypeInferrer.inferActionType(template.content);
            }
            
            // 如果AI未返回completionType和completionTarget，则智能解析
            if (template.completionType == null || template.completionType.isEmpty() 
                || template.completionTarget <= 0) {
                CompletionConditionParser.CompletionCondition condition = 
                    CompletionConditionParser.parse(template.content);
                if (template.completionType == null || template.completionType.isEmpty()) {
                    template.completionType = condition.type;
                }
                if (template.completionTarget <= 0) {
                    template.completionTarget = condition.target;
                }
            }
            
            return template;
        }
        
        // Getter方法
        public String getContent() { return content; }
        public int getMinutes() { return minutes; }
        public String getActionType() { return actionType; }
        public String getCompletionType() { return completionType; }
        public int getCompletionTarget() { return completionTarget; }
    }
    
    /**
     * 解析AI返回的JSON响应
     * @param response AI返回的原始响应
     * @return 解析结果
     */
    public ParseResult parseResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return ParseResult.error("响应内容为空");
        }
        
        try {
            // 提取JSON（处理可能的markdown代码块）
            String jsonString = extractJsonFromMarkdown(response);
            Log.d(TAG, "提取的JSON: " + jsonString);
            
            JSONObject planJson = new JSONObject(jsonString);
            
            // 验证数据完整性
            String validationError = validatePlanData(planJson);
            if (validationError != null) {
                return ParseResult.error(validationError);
            }
            
            // 解析计划基本信息
            StudyPlanEntity plan = extractPlanEntity(planJson);
            
            // 解析阶段信息
            List<StudyPhaseEntity> phases = extractPhases(planJson);
            
            // 解析任务模板
            List<List<TaskTemplate>> taskTemplates = extractDailyTasks(planJson);
            
            // 验证并清洗任务模板
            for (int i = 0; i < taskTemplates.size(); i++) {
                List<TaskTemplate> templates = taskTemplates.get(i);
                TaskTemplateValidator.ValidationResult result = 
                    TaskTemplateValidator.validateAndCleanTemplates(templates, 1);
                
                if (result.isValid) {
                    taskTemplates.set(i, result.validTemplates);
                } else {
                    Log.w(TAG, "Phase " + i + " task templates validation failed: " + result.message);
                    // 如果验证失败，保留原列表（如果非空）或添加默认任务
                    if (templates.isEmpty()) {
                        templates.add(new TaskTemplate("根据阶段目标自主学习", 30));
                    }
                }
            }
            
            // 设置阶段的任务模板JSON
            for (int i = 0; i < phases.size() && i < taskTemplates.size(); i++) {
                StudyPhaseEntity phase = phases.get(i);
                List<TaskTemplate> templates = taskTemplates.get(i);
                phase.setTaskTemplateJson(taskTemplatesToJson(templates));
            }
            
            return ParseResult.success(plan, phases, taskTemplates);
            
        } catch (JSONException e) {
            Log.e(TAG, "JSON解析失败", e);
            return ParseResult.error("JSON解析失败: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "解析过程出错", e);
            return ParseResult.error("解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 从markdown代码块中提取JSON
     */
    private String extractJsonFromMarkdown(String response) {
        String cleaned = response.trim();
        
        // 移除markdown代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        cleaned = cleaned.trim();
        
        // 尝试找到JSON对象的开始和结束
        int startIndex = cleaned.indexOf('{');
        int endIndex = cleaned.lastIndexOf('}');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1);
        }
        
        return cleaned;
    }

    
    /**
     * 验证计划数据完整性
     * @param planJson 计划JSON对象
     * @return 错误信息，如果验证通过则返回null
     */
    public String validatePlanData(JSONObject planJson) {
        // 验证必需字段
        if (!planJson.has("title") || planJson.optString("title").isEmpty()) {
            return "缺少计划标题";
        }
        
        if (!planJson.has("phases")) {
            return "缺少阶段信息";
        }
        
        JSONArray phases = planJson.optJSONArray("phases");
        if (phases == null || phases.length() == 0) {
            return "阶段列表为空";
        }
        
        // 验证每个阶段
        for (int i = 0; i < phases.length(); i++) {
            JSONObject phase = phases.optJSONObject(i);
            if (phase == null) {
                return "阶段 " + (i + 1) + " 数据无效";
            }
            
            if (!phase.has("phaseName") || phase.optString("phaseName").isEmpty()) {
                return "阶段 " + (i + 1) + " 缺少名称";
            }
            
            if (!phase.has("dailyTasks")) {
                return "阶段 " + (i + 1) + " 缺少任务列表";
            }
            
            JSONArray tasks = phase.optJSONArray("dailyTasks");
            if (tasks == null || tasks.length() == 0) {
                return "阶段 " + (i + 1) + " 任务列表为空";
            }
        }
        
        return null; // 验证通过
    }
    
    /**
     * 提取计划实体
     */
    private StudyPlanEntity extractPlanEntity(JSONObject planJson) throws JSONException {
        StudyPlanEntity plan = new StudyPlanEntity();
        
        plan.setTitle(planJson.optString("title", "学习计划"));
        plan.setCategory(normalizeCategory(planJson.optString("category", "词汇")));
        plan.setSummary(planJson.optString("summary", ""));
        plan.setPriority(normalizePriority(planJson.optString("priority", "中")));
        plan.setTotalDays(planJson.optInt("totalDays", 30));
        plan.setDailyMinutes(planJson.optInt("dailyMinutes", 45));
        
        // 设置时间范围
        String timeRange = generateTimeRange(plan.getTotalDays());
        plan.setTimeRange(timeRange);
        
        // 设置每日时长描述
        plan.setDuration(plan.getDailyMinutes() + "分钟/天");
        
        // 设置初始状态
        plan.setStatus(StudyPlanEntity.STATUS_NOT_STARTED);
        plan.setProgress(0);
        plan.setCompletedDays(0);
        plan.setStreakDays(0);
        plan.setTotalStudyTime(0);
        plan.setAiGenerated(true);
        
        // 如果有description字段，也保存
        if (planJson.has("description")) {
            plan.setDescription(planJson.optString("description"));
        } else {
            plan.setDescription(plan.getSummary());
        }
        
        return plan;
    }
    
    /**
     * 提取阶段信息
     * @param planJson 计划JSON对象
     * @return 阶段实体列表
     */
    public List<StudyPhaseEntity> extractPhases(JSONObject planJson) throws JSONException {
        List<StudyPhaseEntity> phases = new ArrayList<>();
        
        JSONArray phasesArray = planJson.optJSONArray("phases");
        if (phasesArray == null) {
            return phases;
        }
        
        int totalDays = planJson.optInt("totalDays", 30);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        int currentDayOffset = 0;
        
        for (int i = 0; i < phasesArray.length(); i++) {
            JSONObject phaseJson = phasesArray.getJSONObject(i);
            
            StudyPhaseEntity phase = new StudyPhaseEntity();
            phase.setPhaseOrder(i + 1);
            phase.setPhaseName(phaseJson.optString("phaseName", "阶段" + (i + 1)));
            phase.setGoal(phaseJson.optString("goal", ""));
            phase.setDurationDays(phaseJson.optInt("durationDays", totalDays / phasesArray.length()));
            phase.setCompletedDays(0);
            phase.setProgress(0);
            
            // 设置阶段状态：第一个阶段为"进行中"，其他为"未开始"
            if (i == 0) {
                phase.setStatus(StudyPhaseEntity.STATUS_IN_PROGRESS);
            } else {
                phase.setStatus(StudyPhaseEntity.STATUS_NOT_STARTED);
            }
            
            // 计算阶段开始和结束日期
            Calendar startCal = (Calendar) calendar.clone();
            startCal.add(Calendar.DAY_OF_YEAR, currentDayOffset);
            phase.setStartDate(dateFormat.format(startCal.getTime()));
            
            Calendar endCal = (Calendar) startCal.clone();
            endCal.add(Calendar.DAY_OF_YEAR, phase.getDurationDays() - 1);
            phase.setEndDate(dateFormat.format(endCal.getTime()));
            
            currentDayOffset += phase.getDurationDays();
            
            phases.add(phase);
        }
        
        return phases;
    }

    
    /**
     * 提取每日任务模板
     * @param planJson 计划JSON对象
     * @return 每个阶段的任务模板列表
     */
    public List<List<TaskTemplate>> extractDailyTasks(JSONObject planJson) throws JSONException {
        List<List<TaskTemplate>> allTaskTemplates = new ArrayList<>();
        
        JSONArray phasesArray = planJson.optJSONArray("phases");
        if (phasesArray == null) {
            return allTaskTemplates;
        }
        
        for (int i = 0; i < phasesArray.length(); i++) {
            JSONObject phaseJson = phasesArray.getJSONObject(i);
            List<TaskTemplate> phaseTemplates = new ArrayList<>();
            
            JSONArray tasksArray = phaseJson.optJSONArray("dailyTasks");
            if (tasksArray != null) {
                for (int j = 0; j < tasksArray.length(); j++) {
                    JSONObject taskJson = tasksArray.getJSONObject(j);
                    TaskTemplate template = TaskTemplate.fromJson(taskJson);
                    phaseTemplates.add(template);
                }
            }
            
            allTaskTemplates.add(phaseTemplates);
        }
        
        return allTaskTemplates;
    }
    
    /**
     * 将任务模板列表转换为JSON字符串
     */
    private String taskTemplatesToJson(List<TaskTemplate> templates) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (TaskTemplate template : templates) {
                jsonArray.put(template.toJson());
            }
            return jsonArray.toString();
        } catch (JSONException e) {
            Log.e(TAG, "任务模板转JSON失败", e);
            return "[]";
        }
    }
    
    /**
     * 从JSON字符串解析任务模板列表
     */
    public static List<TaskTemplate> parseTaskTemplatesFromJson(String json) {
        List<TaskTemplate> templates = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return templates;
        }
        
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject taskJson = jsonArray.getJSONObject(i);
                templates.add(TaskTemplate.fromJson(taskJson));
            }
        } catch (JSONException e) {
            Log.e(TAG, "解析任务模板JSON失败", e);
        }
        
        return templates;
    }
    
    /**
     * 规范化分类
     */
    private String normalizeCategory(String category) {
        if (category == null) return "词汇";
        
        String[] validCategories = {"词汇", "语法", "听力", "阅读", "写作", "口语"};
        for (String valid : validCategories) {
            if (category.contains(valid)) {
                return valid;
            }
        }
        return "词汇";
    }
    
    /**
     * 规范化优先级
     */
    private String normalizePriority(String priority) {
        if (priority == null) return "中";
        
        if (priority.contains("高")) return "高";
        if (priority.contains("低")) return "低";
        return "中";
    }
    
    /**
     * 生成时间范围字符串
     */
    private String generateTimeRange(int totalDays) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        String startMonth = dateFormat.format(calendar.getTime());
        
        calendar.add(Calendar.DAY_OF_YEAR, totalDays);
        String endMonth = dateFormat.format(calendar.getTime());
        
        return startMonth + "至" + endMonth;
    }
    
    /**
     * 将解析结果转换回JSON（用于测试往返一致性）
     */
    public JSONObject toJson(ParseResult result) throws JSONException {
        if (!result.success || result.plan == null) {
            return null;
        }
        
        JSONObject json = new JSONObject();
        
        // 计划基本信息
        json.put("title", result.plan.getTitle());
        json.put("category", result.plan.getCategory());
        json.put("summary", result.plan.getSummary());
        json.put("priority", result.plan.getPriority());
        json.put("totalDays", result.plan.getTotalDays());
        json.put("dailyMinutes", result.plan.getDailyMinutes());
        
        // 阶段信息
        JSONArray phasesArray = new JSONArray();
        for (int i = 0; i < result.phases.size(); i++) {
            StudyPhaseEntity phase = result.phases.get(i);
            JSONObject phaseJson = new JSONObject();
            
            phaseJson.put("phaseName", phase.getPhaseName());
            phaseJson.put("goal", phase.getGoal());
            phaseJson.put("durationDays", phase.getDurationDays());
            
            // 任务模板
            JSONArray tasksArray = new JSONArray();
            if (i < result.taskTemplates.size()) {
                for (TaskTemplate template : result.taskTemplates.get(i)) {
                    tasksArray.put(template.toJson());
                }
            }
            phaseJson.put("dailyTasks", tasksArray);
            
            phasesArray.put(phaseJson);
        }
        json.put("phases", phasesArray);
        
        return json;
    }
}
