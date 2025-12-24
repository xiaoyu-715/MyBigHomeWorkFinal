package com.example.mybighomework.utils;

import com.example.mybighomework.DailySentenceActivity;
import com.example.mybighomework.ExamListActivity;
import com.example.mybighomework.MockExamActivity;
import com.example.mybighomework.TextTranslationActivity;
import com.example.mybighomework.VocabularyActivity;
import com.example.mybighomework.WrongQuestionPracticeActivity;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作类型推断器
 * 根据任务内容推断对应的操作类型（actionType）
 */
public class ActionTypeInferrer {
    
    // 按优先级排序的关键词映射
    private static final LinkedHashMap<String, List<String>> ACTION_KEYWORDS = new LinkedHashMap<>();
    
    static {
        // 优先级1：每日一句（最高优先级）
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
     * @param taskContent 任务内容
     * @return 操作类型，无法推断时返回null
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
        
        // 模糊匹配：考试/测试 → mock_exam（默认）
        if ((taskContent.contains("考试") || taskContent.contains("测试")) 
            && !taskContent.contains("真题") && !taskContent.contains("模拟")) {
            return "mock_exam";
        }
        
        // 模糊匹配：练习题/做题 → mock_exam
        if ((taskContent.contains("练习题") || taskContent.contains("做题"))
            && !taskContent.contains("真题") && !taskContent.contains("模拟") && !taskContent.contains("错题")) {
            return "mock_exam";
        }
        
        return null;
    }
    
    /**
     * 获取操作类型对应的Activity类
     * @param actionType 操作类型
     * @return Activity类，未知类型返回null
     */
    public static Class<?> getTargetActivity(String actionType) {
        if (actionType == null) {
            return null;
        }
        
        switch (actionType) {
            case "vocabulary_training":
                return VocabularyActivity.class;
            case "mock_exam":
                return MockExamActivity.class;
            case "real_exam":
                return ExamListActivity.class;
            case "daily_sentence":
                return DailySentenceActivity.class;
            case "wrong_question_practice":
                return WrongQuestionPracticeActivity.class;
            case "translation_practice":
                return TextTranslationActivity.class;
            default:
                return null;
        }
    }
    
    /**
     * 获取操作类型的用户友好描述
     * @param actionType 操作类型
     * @return 描述文字
     */
    public static String getActionDescription(String actionType) {
        if (actionType == null) {
            return "查看计划详情";
        }
        
        switch (actionType) {
            case "vocabulary_training":
                return "进入词汇训练";
            case "mock_exam":
                return "进入模拟考试";
            case "real_exam":
                return "进入真题练习";
            case "daily_sentence":
                return "进入每日一句";
            case "wrong_question_practice":
                return "进入错题练习";
            case "translation_practice":
                return "进入翻译练习";
            default:
                return "查看计划详情";
        }
    }
    
    /**
     * 获取操作类型的默认完成类型
     * @param actionType 操作类型
     * @return completionType (count/duration/simple)
     */
    public static String getDefaultCompletionType(String actionType) {
        if ("daily_sentence".equals(actionType)) {
            return "simple";
        }
        return "count";
    }
    
    /**
     * 获取操作类型的默认完成目标
     * @param actionType 操作类型
     * @return 默认目标值
     */
    public static int getDefaultCompletionTarget(String actionType) {
        if (actionType == null) {
            return 1;
        }
        
        switch (actionType) {
            case "vocabulary_training":
                return 20; // 默认20个单词
            case "mock_exam":
                return 20; // 默认20道题
            case "real_exam":
                return 1;  // 默认1套真题
            case "daily_sentence":
                return 1;  // 进入即完成
            case "wrong_question_practice":
                return 10; // 默认10道错题
            case "translation_practice":
                return 5;  // 默认5个翻译
            default:
                return 1;
        }
    }
}
