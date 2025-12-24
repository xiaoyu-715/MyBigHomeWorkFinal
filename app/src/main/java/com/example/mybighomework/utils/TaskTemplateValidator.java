package com.example.mybighomework.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务模板验证器
 * 用于验证AI生成的任务模板的完整性和有效性
 * 
 * Requirements: Task 3
 */
public class TaskTemplateValidator {

    public static class ValidationResult {
        public boolean isValid;
        public String message;
        public List<StructuredPlanParser.TaskTemplate> validTemplates;

        public ValidationResult(boolean isValid, String message, List<StructuredPlanParser.TaskTemplate> validTemplates) {
            this.isValid = isValid;
            this.message = message;
            this.validTemplates = validTemplates;
        }

        public static ValidationResult success(List<StructuredPlanParser.TaskTemplate> templates) {
            return new ValidationResult(true, "验证通过", templates);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null);
        }
    }

    /**
     * 验证单个任务模板
     */
    public static boolean validateSingleTemplate(StructuredPlanParser.TaskTemplate template) {
        if (template == null) return false;
        
        // 1. 检查内容是否为空
        if (template.content == null || template.content.trim().isEmpty()) {
            return false;
        }
        
        // 2. 检查内容长度 (至少2个字符)
        if (template.content.trim().length() < 2) {
            return false;
        }
        
        // 3. 检查时长是否合理 (5-180分钟)
        if (template.minutes < 5 || template.minutes > 180) {
            return false;
        }
        
        return true;
    }

    /**
     * 验证并清洗任务模板列表
     * 过滤掉无效的模板，如果有效模板过少则返回失败
     * 
     * @param templates 原始模板列表
     * @param minTasks 最小任务数量
     * @return 验证结果
     */
    public static ValidationResult validateAndCleanTemplates(List<StructuredPlanParser.TaskTemplate> templates, int minTasks) {
        if (templates == null || templates.isEmpty()) {
            return ValidationResult.error("任务列表为空");
        }

        List<StructuredPlanParser.TaskTemplate> validTemplates = new ArrayList<>();
        
        for (StructuredPlanParser.TaskTemplate template : templates) {
            if (validateSingleTemplate(template)) {
                // 简单的内容去重
                boolean isDuplicate = false;
                for (StructuredPlanParser.TaskTemplate valid : validTemplates) {
                    if (valid.content.equals(template.content)) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                if (!isDuplicate) {
                    validTemplates.add(template);
                }
            }
        }

        if (validTemplates.size() < minTasks) {
            return ValidationResult.error("有效任务数量不足 (至少" + minTasks + "个，实际" + validTemplates.size() + "个)");
        }

        return ValidationResult.success(validTemplates);
    }

    /**
     * 验证阶段的任务分布
     * @param templates 阶段的所有任务模板
     * @param durationDays 阶段持续天数
     */
    public static ValidationResult validatePhaseDistribution(List<StructuredPlanParser.TaskTemplate> templates, int durationDays) {
        ValidationResult basicValidation = validateAndCleanTemplates(templates, 1);
        if (!basicValidation.isValid) {
            return basicValidation;
        }
        
        // 检查任务数量是否足够覆盖阶段持续时间
        // 假设每天至少1个任务，或者循环使用
        // 这里主要检查是否有足够的模板来支持多样性
        
        // 如果阶段很长（>7天），建议至少有3个任务模板
        if (durationDays > 7 && basicValidation.validTemplates.size() < 3) {
            return ValidationResult.error("阶段持续时间较长，建议增加任务种类");
        }
        
        return basicValidation;
    }
}
