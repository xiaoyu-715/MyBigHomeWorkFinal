package com.example.mybighomework.utils;

import android.util.Log;

import com.example.mybighomework.utils.StructuredPlanParser.TaskTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务模板验证器
 * 用于验证AI生成的任务模板的完整性和有效性
 * 避免生成空任务或无效任务
 * 
 * Requirements: 2.1, 2.2, 6.1
 */
public class TaskTemplateValidatorYSJ {
    
    private static final String TAG = "TaskTemplateValidator";
    
    // 任务时长限制（分钟）
    private static final int MIN_TASK_MINUTES = 5;
    private static final int MAX_TASK_MINUTES = 120;
    private static final int DEFAULT_TASK_MINUTES = 30;
    
    // 任务内容长度限制
    private static final int MIN_CONTENT_LENGTH = 2;
    private static final int MAX_CONTENT_LENGTH = 100;
    
    // 每日任务数量限制
    private static final int MIN_DAILY_TASKS = 1;
    private static final int MAX_DAILY_TASKS = 10;
    
    // 每日总时长限制（分钟）
    private static final int MIN_DAILY_MINUTES = 15;
    private static final int MAX_DAILY_MINUTES = 480; // 8小时
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        public boolean isValid;
        public String errorMessage;
        public List<String> warnings;
        public List<String> suggestions;
        
        public ValidationResult() {
            this.warnings = new ArrayList<>();
            this.suggestions = new ArrayList<>();
        }
        
        public static ValidationResult success() {
            ValidationResult result = new ValidationResult();
            result.isValid = true;
            return result;
        }
        
        public static ValidationResult error(String message) {
            ValidationResult result = new ValidationResult();
            result.isValid = false;
            result.errorMessage = message;
            return result;
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
        
        public void addSuggestion(String suggestion) {
            this.suggestions.add(suggestion);
        }
    }
    
    /**
     * 验证单个任务模板的有效性
     * 
     * @param template 任务模板
     * @return 验证结果
     */
    public static ValidationResult validateTaskTemplate(TaskTemplate template) {
        ValidationResult result = new ValidationResult();
        result.isValid = true;
        
        if (template == null) {
            return ValidationResult.error("任务模板为空");
        }
        
        // 1. 检查任务内容是否为空
        if (template.content == null || template.content.trim().isEmpty()) {
            return ValidationResult.error("任务内容为空");
        }
        
        // 2. 检查任务内容长度
        String content = template.content.trim();
        if (content.length() < MIN_CONTENT_LENGTH) {
            return ValidationResult.error("任务内容过短（少于" + MIN_CONTENT_LENGTH + "个字符）");
        }
        
        if (content.length() > MAX_CONTENT_LENGTH) {
            result.addWarning("任务内容过长（超过" + MAX_CONTENT_LENGTH + "个字符），建议简化");
        }
        
        // 3. 检查任务内容是否过于简单
        if (content.length() < 4 || !content.contains(" ") && content.length() < 6) {
            result.addWarning("任务描述可能过于简单，建议更详细");
        }
        
        // 4. 检查时长是否合理
        if (template.minutes < MIN_TASK_MINUTES) {
            result.addWarning("任务时长过短（少于" + MIN_TASK_MINUTES + "分钟），已调整为" + DEFAULT_TASK_MINUTES + "分钟");
            template.minutes = DEFAULT_TASK_MINUTES;
        }
        
        if (template.minutes > MAX_TASK_MINUTES) {
            result.addWarning("任务时长过长（超过" + MAX_TASK_MINUTES + "分钟），建议拆分为多个任务");
            result.addSuggestion("将长时间任务拆分为多个" + (MAX_TASK_MINUTES / 2) + "分钟的子任务");
        }
        
        // 5. 检查时长是否为5的倍数（更易于管理）
        if (template.minutes % 5 != 0) {
            result.addSuggestion("建议将任务时长调整为5的倍数，便于时间管理");
        }
        
        Log.d(TAG, "[模板验证] 任务模板验证完成: " + content + " (" + template.minutes + "分钟)");
        
        return result;
    }
    
    /**
     * 验证阶段的任务模板列表
     * 
     * @param templates 任务模板列表
     * @param phaseDurationDays 阶段持续天数
     * @return 验证结果
     */
    public static ValidationResult validatePhaseTaskTemplates(
            List<TaskTemplate> templates, int phaseDurationDays) {
        
        ValidationResult result = new ValidationResult();
        result.isValid = true;
        
        if (templates == null || templates.isEmpty()) {
            return ValidationResult.error("阶段任务模板列表为空");
        }
        
        // 1. 检查任务数量是否合理
        int taskCount = templates.size();
        if (taskCount < MIN_DAILY_TASKS) {
            return ValidationResult.error("每日任务数量过少（少于" + MIN_DAILY_TASKS + "个）");
        }
        
        if (taskCount > MAX_DAILY_TASKS) {
            result.addWarning("每日任务数量过多（超过" + MAX_DAILY_TASKS + "个），可能难以完成");
            result.addSuggestion("建议将任务数量控制在" + (MAX_DAILY_TASKS / 2) + "-" + MAX_DAILY_TASKS + "个之间");
        }
        
        // 2. 验证每个任务模板
        int validTaskCount = 0;
        for (int i = 0; i < templates.size(); i++) {
            TaskTemplate template = templates.get(i);
            ValidationResult taskResult = validateTaskTemplate(template);
            
            if (!taskResult.isValid) {
                result.addWarning("任务" + (i + 1) + "验证失败: " + taskResult.errorMessage);
            } else {
                validTaskCount++;
                // 合并警告和建议
                result.warnings.addAll(taskResult.warnings);
                result.suggestions.addAll(taskResult.suggestions);
            }
        }
        
        if (validTaskCount == 0) {
            return ValidationResult.error("没有有效的任务模板");
        }
        
        // 3. 检查总时长是否合理
        int totalMinutes = 0;
        for (TaskTemplate template : templates) {
            totalMinutes += template.minutes;
        }
        
        if (totalMinutes < MIN_DAILY_MINUTES) {
            result.addWarning("每日总时长过短（少于" + MIN_DAILY_MINUTES + "分钟）");
            result.addSuggestion("建议增加任务时长或任务数量");
        }
        
        if (totalMinutes > MAX_DAILY_MINUTES) {
            result.addWarning("每日总时长过长（超过" + (MAX_DAILY_MINUTES / 60) + "小时），可能难以完成");
            result.addSuggestion("建议减少任务时长或任务数量，或延长阶段天数");
        }
        
        // 4. 检查任务分布是否均衡
        if (taskCount > 1) {
            int avgMinutes = totalMinutes / taskCount;
            int maxDeviation = 0;
            
            for (TaskTemplate template : templates) {
                int deviation = Math.abs(template.minutes - avgMinutes);
                maxDeviation = Math.max(maxDeviation, deviation);
            }
            
            // 如果最大偏差超过平均值的50%，提示任务时长分布不均
            if (maxDeviation > avgMinutes * 0.5) {
                result.addSuggestion("任务时长分布不够均衡，建议调整为相近的时长");
            }
        }
        
        // 5. 检查阶段总工作量是否合理
        if (phaseDurationDays > 0) {
            int totalPhaseMinutes = totalMinutes * phaseDurationDays;
            int totalPhaseHours = totalPhaseMinutes / 60;
            
            if (totalPhaseHours < 5) {
                result.addWarning("阶段总工作量过少（少于5小时）");
            }
            
            if (totalPhaseHours > 100) {
                result.addWarning("阶段总工作量过大（超过100小时），可能难以完成");
                result.addSuggestion("建议延长阶段天数或减少每日任务量");
            }
        }
        
        Log.d(TAG, "[模板验证] 阶段任务模板验证完成: " + validTaskCount + "/" + taskCount + 
                   " 个有效任务，总时长: " + totalMinutes + "分钟");
        
        return result;
    }
    
    /**
     * 修复无效的任务模板
     * 尝试自动修复一些常见问题
     * 
     * @param template 任务模板
     * @return 修复后的任务模板
     */
    public static TaskTemplate fixTaskTemplate(TaskTemplate template) {
        if (template == null) {
            return new TaskTemplate("学习任务", DEFAULT_TASK_MINUTES);
        }
        
        // 修复空内容
        if (template.content == null || template.content.trim().isEmpty()) {
            template.content = "学习任务";
        }
        
        // 修复过短的时长
        if (template.minutes < MIN_TASK_MINUTES) {
            template.minutes = DEFAULT_TASK_MINUTES;
        }
        
        // 修复过长的时长
        if (template.minutes > MAX_TASK_MINUTES) {
            template.minutes = MAX_TASK_MINUTES;
        }
        
        // 调整为5的倍数
        if (template.minutes % 5 != 0) {
            template.minutes = ((template.minutes + 2) / 5) * 5;
        }
        
        return template;
    }
    
    /**
     * 批量修复任务模板列表
     * 
     * @param templates 任务模板列表
     * @return 修复后的任务模板列表
     */
    public static List<TaskTemplate> fixTaskTemplates(List<TaskTemplate> templates) {
        List<TaskTemplate> fixedTemplates = new ArrayList<>();
        
        if (templates == null || templates.isEmpty()) {
            // 返回默认任务模板
            fixedTemplates.add(new TaskTemplate("学习任务1", 30));
            fixedTemplates.add(new TaskTemplate("学习任务2", 30));
            return fixedTemplates;
        }
        
        for (TaskTemplate template : templates) {
            fixedTemplates.add(fixTaskTemplate(template));
        }
        
        return fixedTemplates;
    }
    
    /**
     * 生成验证报告
     * 
     * @param result 验证结果
     * @return 格式化的报告字符串
     */
    public static String generateValidationReport(ValidationResult result) {
        StringBuilder report = new StringBuilder();
        
        if (result.isValid) {
            report.append("✅ 验证通过\n");
        } else {
            report.append("❌ 验证失败: ").append(result.errorMessage).append("\n");
        }
        
        if (!result.warnings.isEmpty()) {
            report.append("\n⚠️ 警告:\n");
            for (String warning : result.warnings) {
                report.append("  • ").append(warning).append("\n");
            }
        }
        
        if (!result.suggestions.isEmpty()) {
            report.append("\n💡 建议:\n");
            for (String suggestion : result.suggestions) {
                report.append("  • ").append(suggestion).append("\n");
            }
        }
        
        return report.toString();
    }
}
