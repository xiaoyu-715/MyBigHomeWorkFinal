package com.example.mybighomework.utils;

import android.content.Context;
import android.util.Log;

/**
 * 结构化学习计划Prompt构建器
 * 用于构建生成结构化学习计划的AI提示词
 * 
 * Requirements: 2.1
 */
public class StructuredPlanPromptBuilder {
    
    private static final String TAG = "StructuredPlanPromptBuilder";
    
    private Context context;
    private ConversationAnalyzer analyzer;
    
    public StructuredPlanPromptBuilder() {
        this.analyzer = new ConversationAnalyzer();
    }
    
    public StructuredPlanPromptBuilder(Context context) {
        this.context = context;
        this.analyzer = new ConversationAnalyzer();
    }
    
    /**
     * 构建结构化计划生成的Prompt
     * @param conversationContext 对话上下文
     * @return 构建好的Prompt字符串
     */
    public String buildPrompt(String conversationContext) {
        // 分析对话内容
        ConversationAnalyzer.AnalysisResult analysis = analyzer.analyze(conversationContext);
        
        StringBuilder prompt = new StringBuilder();
        
        // ========== 第一部分：角色设定 ==========
        prompt.append("你是一位专业的英语学习规划师。请根据用户需求生成结构化的学习计划。\n\n");
        
        // ========== 第二部分：用户需求分析 ==========
        prompt.append("【用户需求】\n");
        appendUserRequirements(prompt, analysis, conversationContext);
        prompt.append("\n");
        
        // ========== 第三部分：输出要求 ==========
        prompt.append("【输出要求】\n");
        prompt.append("请严格按照以下JSON格式返回（只返回JSON，不要其他内容）：\n\n");
        appendJsonFormat(prompt);
        prompt.append("\n");
        
        // ========== 第四部分：功能约束 ==========
        prompt.append("【功能约束】\n");
        appendFunctionConstraints(prompt);
        prompt.append("\n");
        
        // ========== 第五部分：示例 ==========
        prompt.append("【示例】\n");
        appendExample(prompt, analysis);
        
        return prompt.toString();
    }

    
    /**
     * 添加用户需求分析部分
     */
    private void appendUserRequirements(StringBuilder prompt, 
                                        ConversationAnalyzer.AnalysisResult analysis,
                                        String conversationContext) {
        // 学习场景
        if (analysis.hasScenario()) {
            prompt.append("➤ 学习目的：").append(analysis.scenario).append("\n");
        } else {
            prompt.append("➤ 学习目的：英语能力提升\n");
        }
        
        // 学习目标
        if (analysis.hasGoals()) {
            prompt.append("➤ 重点提升：").append(String.join("、", analysis.goals)).append("\n");
        }
        
        // 当前水平
        if (analysis.hasCurrentLevel()) {
            prompt.append("➤ 当前水平：").append(analysis.currentLevel).append("\n");
        }
        
        // 薄弱点
        if (analysis.hasWeakPoints()) {
            prompt.append("➤ 薄弱环节：").append(String.join("、", analysis.weakPoints)).append("\n");
        }
        
        // 时间安排
        if (analysis.hasTimeRange()) {
            prompt.append("➤ 学习周期：").append(analysis.timeRange).append("\n");
        } else {
            String defaultTime = ConversationAnalyzer.SmartDefaults.recommendTimeRange(analysis.scenario);
            prompt.append("➤ 建议周期：").append(defaultTime).append("\n");
        }
        
        // 每日时长
        if (analysis.hasDailyDuration()) {
            prompt.append("➤ 每日时长：").append(analysis.dailyDuration).append("\n");
        } else {
            prompt.append("➤ 建议时长：45-60分钟/天\n");
        }
        
        // 原始对话内容（简化）
        if (conversationContext != null && !conversationContext.isEmpty()) {
            String simplified = simplifyContext(conversationContext);
            if (!simplified.isEmpty()) {
                prompt.append("➤ 用户描述：").append(simplified).append("\n");
            }
        }
    }
    
    /**
     * 简化对话上下文
     */
    private String simplifyContext(String context) {
        if (context == null) return "";
        // 限制长度，避免Prompt过长
        String simplified = context.trim();
        if (simplified.length() > 200) {
            simplified = simplified.substring(0, 200) + "...";
        }
        return simplified;
    }
    
    /**
     * 添加JSON格式说明
     */
    private void appendJsonFormat(StringBuilder prompt) {
        prompt.append("{\n");
        prompt.append("  \"title\": \"计划标题（简洁明确）\",\n");
        prompt.append("  \"category\": \"词汇/语法/听力/阅读/写作/口语\",\n");
        prompt.append("  \"summary\": \"计划简介（50字以内）\",\n");
        prompt.append("  \"priority\": \"高/中/低\",\n");
        prompt.append("  \"totalDays\": 总天数,\n");
        prompt.append("  \"dailyMinutes\": 每日学习分钟数,\n");
        prompt.append("  \"phases\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"phaseName\": \"阶段名称（如：基础巩固）\",\n");
        prompt.append("      \"goal\": \"阶段目标（30字以内）\",\n");
        prompt.append("      \"durationDays\": 持续天数,\n");
        prompt.append("      \"dailyTasks\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"content\": \"任务内容（具体可量化，如：学习20个新单词）\",\n");
        prompt.append("          \"minutes\": 预计分钟数,\n");
        prompt.append("          \"actionType\": \"操作类型（见功能约束）\",\n");
        prompt.append("          \"completionType\": \"完成类型（count/simple）\",\n");
        prompt.append("          \"completionTarget\": 完成目标数量\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
    }
    
    /**
     * 添加功能约束说明
     * 确保AI生成的任务对应应用中实际存在的功能
     */
    private void appendFunctionConstraints(StringBuilder prompt) {
        prompt.append("任务必须对应以下6种应用功能之一：\n\n");
        
        prompt.append("1. 每日一句 (daily_sentence)\n");
        prompt.append("   - 完成类型：simple（进入页面即完成）\n");
        prompt.append("   - 示例：\"学习今日一句\"、\"每日一句学习\"\n");
        prompt.append("   - 优先级：最高（每日必学）\n\n");
        
        prompt.append("2. 真题练习 (real_exam)\n");
        prompt.append("   - 完成类型：count（按套数计）\n");
        prompt.append("   - 示例：\"完成1套真题\"、\"真题练习2套\"\n");
        prompt.append("   - 优先级：高\n\n");
        
        prompt.append("3. 模拟考试 (mock_exam)\n");
        prompt.append("   - 完成类型：count（按题数计）\n");
        prompt.append("   - 示例：\"模拟考试练习20题\"、\"完成模拟测试\"\n");
        prompt.append("   - 优先级：中高\n\n");
        
        prompt.append("4. 错题练习 (wrong_question_practice)\n");
        prompt.append("   - 完成类型：count（按题数计）\n");
        prompt.append("   - 示例：\"复习10道错题\"、\"错题巩固练习\"\n");
        prompt.append("   - 优先级：中\n\n");
        
        prompt.append("5. 词汇训练 (vocabulary_training)\n");
        prompt.append("   - 完成类型：count（按单词数计）\n");
        prompt.append("   - 示例：\"学习20个新单词\"、\"词汇训练30个\"\n");
        prompt.append("   - 优先级：中\n\n");
        
        prompt.append("6. 翻译练习 (translation_practice)\n");
        prompt.append("   - 完成类型：count（按次数计）\n");
        prompt.append("   - 示例：\"翻译练习5次\"、\"完成3段翻译\"\n");
        prompt.append("   - 优先级：低\n\n");
        
        prompt.append("【重要规则】\n");
        prompt.append("- 每日任务必须包含\"每日一句\"作为第一个任务\n");
        prompt.append("- 任务内容必须具体可量化（如\"学习20个单词\"而非\"词汇学习\"）\n");
        prompt.append("- 禁止生成应用不支持的任务类型（如听力、口语、写作等）\n");
        prompt.append("- completionTarget必须是正整数\n");
    }

    
    /**
     * 添加示例
     */
    private void appendExample(StringBuilder prompt, ConversationAnalyzer.AnalysisResult analysis) {
        // 根据分析结果生成相关示例
        String category = getRecommendedCategory(analysis);
        String scenario = analysis.hasScenario() ? analysis.scenario : "英语四级";
        int totalDays = getRecommendedTotalDays(analysis);
        int dailyMinutes = getRecommendedDailyMinutes(analysis);
        
        prompt.append("{\n");
        prompt.append("  \"title\": \"").append(scenario).append(category).append("突破计划\",\n");
        prompt.append("  \"category\": \"").append(category).append("\",\n");
        prompt.append("  \"summary\": \"").append(totalDays).append("天系统提升").append(category).append("能力\",\n");
        prompt.append("  \"priority\": \"高\",\n");
        prompt.append("  \"totalDays\": ").append(totalDays).append(",\n");
        prompt.append("  \"dailyMinutes\": ").append(dailyMinutes).append(",\n");
        prompt.append("  \"phases\": [\n");
        
        // 阶段1：基础巩固
        int phase1Days = totalDays / 3;
        prompt.append("    {\n");
        prompt.append("      \"phaseName\": \"基础巩固\",\n");
        prompt.append("      \"goal\": \"").append(getPhase1Goal(category)).append("\",\n");
        prompt.append("      \"durationDays\": ").append(phase1Days).append(",\n");
        prompt.append("      \"dailyTasks\": [\n");
        appendPhase1Tasks(prompt, category, dailyMinutes);
        prompt.append("      ]\n");
        prompt.append("    },\n");
        
        // 阶段2：能力提升
        int phase2Days = totalDays / 3;
        prompt.append("    {\n");
        prompt.append("      \"phaseName\": \"能力提升\",\n");
        prompt.append("      \"goal\": \"").append(getPhase2Goal(category)).append("\",\n");
        prompt.append("      \"durationDays\": ").append(phase2Days).append(",\n");
        prompt.append("      \"dailyTasks\": [\n");
        appendPhase2Tasks(prompt, category, dailyMinutes);
        prompt.append("      ]\n");
        prompt.append("    },\n");
        
        // 阶段3：冲刺强化
        int phase3Days = totalDays - phase1Days - phase2Days;
        prompt.append("    {\n");
        prompt.append("      \"phaseName\": \"冲刺强化\",\n");
        prompt.append("      \"goal\": \"").append(getPhase3Goal(category)).append("\",\n");
        prompt.append("      \"durationDays\": ").append(phase3Days).append(",\n");
        prompt.append("      \"dailyTasks\": [\n");
        appendPhase3Tasks(prompt, category, dailyMinutes);
        prompt.append("      ]\n");
        prompt.append("    }\n");
        
        prompt.append("  ]\n");
        prompt.append("}\n");
    }
    
    /**
     * 获取推荐的分类
     */
    private String getRecommendedCategory(ConversationAnalyzer.AnalysisResult analysis) {
        if (analysis.hasGoals() && !analysis.goals.isEmpty()) {
            return analysis.goals.get(0);
        }
        return "听力";
    }
    
    /**
     * 获取推荐的总天数
     */
    private int getRecommendedTotalDays(ConversationAnalyzer.AnalysisResult analysis) {
        if (analysis.hasTimeRange()) {
            int months = extractMonths(analysis.timeRange);
            return months * 30;
        }
        return 30; // 默认30天
    }
    
    /**
     * 获取推荐的每日分钟数
     */
    private int getRecommendedDailyMinutes(ConversationAnalyzer.AnalysisResult analysis) {
        if (analysis.hasDailyDuration()) {
            return extractMinutes(analysis.dailyDuration);
        }
        return 45; // 默认45分钟
    }
    
    /**
     * 从时间范围提取月数
     */
    private int extractMonths(String timeRange) {
        if (timeRange == null) return 1;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(timeRange);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }
    
    /**
     * 从时长描述提取分钟数
     */
    private int extractMinutes(String duration) {
        if (duration == null) return 45;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(duration);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 45;
    }

    
    // ========== 阶段目标生成方法 ==========
    
    private String getPhase1Goal(String category) {
        switch (category) {
            case "词汇":
                return "掌握核心词汇，建立词汇基础";
            case "听力":
                return "熟悉英语语音语调，建立听力基础";
            case "阅读":
                return "提高阅读速度，掌握基本技巧";
            case "写作":
                return "积累写作素材，掌握基本句型";
            case "口语":
                return "纠正发音，建立口语自信";
            case "语法":
                return "梳理语法体系，掌握基础语法";
            default:
                return "打好基础，建立学习习惯";
        }
    }
    
    private String getPhase2Goal(String category) {
        switch (category) {
            case "词汇":
                return "扩展词汇量，掌握词汇用法";
            case "听力":
                return "提高听力理解速度和准确率";
            case "阅读":
                return "攻克长难句，提升理解深度";
            case "写作":
                return "练习各类题型，提升表达能力";
            case "口语":
                return "提高流利度，丰富表达方式";
            case "语法":
                return "攻克难点语法，灵活运用";
            default:
                return "强化训练，提升能力";
        }
    }
    
    private String getPhase3Goal(String category) {
        switch (category) {
            case "词汇":
                return "查漏补缺，巩固记忆";
            case "听力":
                return "模拟真实考试，查漏补缺";
            case "阅读":
                return "限时训练，提升应试能力";
            case "写作":
                return "模拟考试，完善写作技巧";
            case "口语":
                return "模拟实战，提升应变能力";
            case "语法":
                return "综合练习，查漏补缺";
            default:
                return "冲刺强化，全面提升";
        }
    }
    
    // ========== 阶段任务生成方法 ==========
    
    private void appendPhase1Tasks(StringBuilder prompt, String category, int dailyMinutes) {
        switch (category) {
            case "词汇":
                prompt.append("        {\"content\": \"核心词汇学习\", \"minutes\": ").append(dailyMinutes * 2 / 3).append("},\n");
                prompt.append("        {\"content\": \"词汇复习巩固\", \"minutes\": ").append(dailyMinutes / 3).append("}\n");
                break;
            case "听力":
                prompt.append("        {\"content\": \"听力材料精听\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"跟读模仿练习\", \"minutes\": ").append(dailyMinutes / 3).append("},\n");
                prompt.append("        {\"content\": \"生词整理复习\", \"minutes\": ").append(dailyMinutes / 6).append("}\n");
                break;
            case "阅读":
                prompt.append("        {\"content\": \"精读文章练习\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"词汇积累\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            case "写作":
                prompt.append("        {\"content\": \"范文学习分析\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"句型积累练习\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            case "口语":
                prompt.append("        {\"content\": \"发音练习\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"跟读模仿\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            case "语法":
                prompt.append("        {\"content\": \"语法知识学习\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"语法练习题\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            default:
                prompt.append("        {\"content\": \"基础学习\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"练习巩固\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
        }
    }
    
    private void appendPhase2Tasks(StringBuilder prompt, String category, int dailyMinutes) {
        switch (category) {
            case "词汇":
                prompt.append("        {\"content\": \"进阶词汇学习\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"词汇应用练习\", \"minutes\": ").append(dailyMinutes / 3).append("},\n");
                prompt.append("        {\"content\": \"复习巩固\", \"minutes\": ").append(dailyMinutes / 6).append("}\n");
                break;
            case "听力":
                prompt.append("        {\"content\": \"短对话听力练习\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"长对话听力练习\", \"minutes\": ").append(dailyMinutes / 3).append("},\n");
                prompt.append("        {\"content\": \"听力笔记训练\", \"minutes\": ").append(dailyMinutes / 6).append("}\n");
                break;
            case "阅读":
                prompt.append("        {\"content\": \"长难句分析\", \"minutes\": ").append(dailyMinutes / 3).append("},\n");
                prompt.append("        {\"content\": \"阅读理解练习\", \"minutes\": ").append(dailyMinutes * 2 / 3).append("}\n");
                break;
            case "写作":
                prompt.append("        {\"content\": \"写作练习\", \"minutes\": ").append(dailyMinutes * 2 / 3).append("},\n");
                prompt.append("        {\"content\": \"范文对比分析\", \"minutes\": ").append(dailyMinutes / 3).append("}\n");
                break;
            case "口语":
                prompt.append("        {\"content\": \"话题练习\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"表达训练\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            case "语法":
                prompt.append("        {\"content\": \"难点语法攻克\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"综合练习\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            default:
                prompt.append("        {\"content\": \"能力提升训练\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"实战练习\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
        }
    }
    
    private void appendPhase3Tasks(StringBuilder prompt, String category, int dailyMinutes) {
        switch (category) {
            case "词汇":
                prompt.append("        {\"content\": \"词汇综合测试\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"错题复习\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            case "听力":
                prompt.append("        {\"content\": \"模拟听力测试\", \"minutes\": ").append(dailyMinutes * 2 / 3).append("},\n");
                prompt.append("        {\"content\": \"错题分析复盘\", \"minutes\": ").append(dailyMinutes / 3).append("}\n");
                break;
            case "阅读":
                prompt.append("        {\"content\": \"限时阅读测试\", \"minutes\": ").append(dailyMinutes * 2 / 3).append("},\n");
                prompt.append("        {\"content\": \"错题分析\", \"minutes\": ").append(dailyMinutes / 3).append("}\n");
                break;
            case "写作":
                prompt.append("        {\"content\": \"模拟写作\", \"minutes\": ").append(dailyMinutes * 2 / 3).append("},\n");
                prompt.append("        {\"content\": \"批改复盘\", \"minutes\": ").append(dailyMinutes / 3).append("}\n");
                break;
            case "口语":
                prompt.append("        {\"content\": \"模拟口语测试\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"复盘改进\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            case "语法":
                prompt.append("        {\"content\": \"语法综合测试\", \"minutes\": ").append(dailyMinutes / 2).append("},\n");
                prompt.append("        {\"content\": \"查漏补缺\", \"minutes\": ").append(dailyMinutes / 2).append("}\n");
                break;
            default:
                prompt.append("        {\"content\": \"模拟测试\", \"minutes\": ").append(dailyMinutes * 2 / 3).append("},\n");
                prompt.append("        {\"content\": \"复盘总结\", \"minutes\": ").append(dailyMinutes / 3).append("}\n");
        }
    }
}
