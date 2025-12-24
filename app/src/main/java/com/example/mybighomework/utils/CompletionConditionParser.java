package com.example.mybighomework.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 完成条件解析器
 * 从任务内容中解析完成条件（类型和目标值）
 */
public class CompletionConditionParser {
    
    // 数量型模式：X个、X道、X套、X句、X次
    private static final Pattern COUNT_PATTERN = Pattern.compile("(\\d+)\\s*(个|道|套|句|次|篇)");
    
    // 时长型模式：X分钟
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*分钟");
    
    /**
     * 从任务内容解析完成条件
     * @param taskContent 任务内容
     * @return 完成条件
     */
    public static CompletionCondition parse(String taskContent) {
        if (taskContent == null || taskContent.isEmpty()) {
            return new CompletionCondition("simple", 1);
        }
        
        // 尝试解析数量
        Matcher countMatcher = COUNT_PATTERN.matcher(taskContent);
        if (countMatcher.find()) {
            try {
                int target = Integer.parseInt(countMatcher.group(1));
                if (target > 0) {
                    return new CompletionCondition("count", target);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        
        // 尝试解析时长
        Matcher durationMatcher = DURATION_PATTERN.matcher(taskContent);
        if (durationMatcher.find()) {
            try {
                int target = Integer.parseInt(durationMatcher.group(1));
                if (target > 0) {
                    return new CompletionCondition("duration", target);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        
        // 默认简单型
        return new CompletionCondition("simple", 1);
    }
    
    /**
     * 完成条件类
     */
    public static class CompletionCondition {
        public final String type;   // count/duration/simple
        public final int target;    // 目标值
        
        public CompletionCondition(String type, int target) {
            this.type = type;
            this.target = target;
        }
        
        @Override
        public String toString() {
            return "CompletionCondition{type='" + type + "', target=" + target + "}";
        }
    }
}
