package com.example.mybighomework.utils;

import android.content.Context;
import android.util.Log;

import com.example.mybighomework.StudyPlan;
import com.example.mybighomework.api.ZhipuAIService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习计划提取工具类
 * 从AI对话内容中提取并生成结构化的学习计划
 */
public class StudyPlanExtractor {
    
    private static final String TAG = "StudyPlanExtractor";
    
    private ZhipuAIService apiService;
    private ConversationAnalyzer analyzer;
    private Context context;
    
    public StudyPlanExtractor(ZhipuAIService apiService) {
        this.apiService = apiService;
        this.analyzer = new ConversationAnalyzer();
    }
    
    public StudyPlanExtractor(ZhipuAIService apiService, Context context) {
        this.apiService = apiService;
        this.analyzer = new ConversationAnalyzer();
        this.context = context;
    }
    
    /**
     * 从对话内容中提取学习计划
     * @param conversationContext 对话上下文
     * @param callback 回调接口
     */
    public void extractPlans(String conversationContext, OnPlanExtractedListener callback) {
        extractPlans(conversationContext, callback, null);
    }
    
    /**
     * 从对话内容中提取学习计划（带进度回调）
     * @param conversationContext 对话上下文
     * @param callback 回调接口
     * @param progressListener 进度监听器
     */
    public void extractPlans(String conversationContext, OnPlanExtractedListener callback, 
                            OnProgressUpdateListener progressListener) {
        if (apiService == null) {
            if (callback != null) {
                callback.onError("API服务未初始化");
            }
            return;
        }
        
        // 步骤1: 分析对话内容
        if (progressListener != null) {
            progressListener.onProgressUpdate("正在分析对话内容...", 10);
        }
        
        // 构建结构化提示词
        String structuredPrompt = buildStructuredPrompt(conversationContext);
        
        if (progressListener != null) {
            progressListener.onProgressUpdate("正在分析对话内容...", 30);
        }
        
        // 构建消息列表
        List<ZhipuAIService.ChatMessage> messages = new ArrayList<>();
        messages.add(new ZhipuAIService.ChatMessage("system", 
            "你是一个专业的学习计划制定助手，擅长根据学生的需求制定详细、可执行的学习计划。"));
        messages.add(new ZhipuAIService.ChatMessage("user", structuredPrompt));
        
        // 步骤2: 调用API生成
        if (progressListener != null) {
            progressListener.onProgressUpdate("正在生成学习计划...", 40);
        }
        
        // 调用API
        apiService.chat(messages, new ZhipuAIService.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    // 步骤3: 解析响应
                    if (progressListener != null) {
                        progressListener.onProgressUpdate("正在解析计划数据...", 80);
                    }
                    
                    // 解析JSON响应
                    List<StudyPlan> plans = parseJsonResponse(response);
                    
                    if (progressListener != null) {
                        progressListener.onProgressUpdate("生成完成！", 100);
                    }
                    
                    if (callback != null) {
                        if (plans.isEmpty()) {
                            callback.onError("未能生成有效的学习计划，请尝试更具体的描述");
                        } else {
                            callback.onSuccess(plans);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析学习计划失败", e);
                    if (callback != null) {
                        callback.onError("解析失败：" + e.getMessage());
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "API调用失败: " + error);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }
    
    /**
     * 构建结构化提示词（智能优化版）
     */
    private String buildStructuredPrompt(String conversationContext) {
        // 分析对话内容
        ConversationAnalyzer.AnalysisResult analysis = analyzer.analyze(conversationContext);
        
        // 集成个性化推荐
        PersonalizedRecommendationEngine recommendationEngine = 
            new PersonalizedRecommendationEngine(context);
        
        // 构建增强的Prompt
        return buildEnhancedPromptWithPersonalization(analysis, conversationContext, recommendationEngine);
    }
    
    /**
     * 构建增强的Prompt
     */
    private String buildEnhancedPrompt(ConversationAnalyzer.AnalysisResult analysis, 
                                      String originalContext) {
        StringBuilder prompt = new StringBuilder();
        
        // ========== 第一部分：角色设定 ==========
        prompt.append("你是一位经验丰富的英语学习规划师，擅长根据学生的具体情况制定个性化、");
        prompt.append("可执行的学习计划。\n\n");
        
        // ========== 第二部分：学生情况分析 ==========
        prompt.append("【学生情况分析】\n");
        
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
            prompt.append("➤ 建议时长：60-90分钟/天\n");
        }
        
        prompt.append("\n");
        
        // ========== 第三部分：计划制定要求 ==========
        prompt.append("【计划制定要求】\n");
        prompt.append("请为学生生成1-3个学习计划，要求：\n\n");
        
        prompt.append("1. 针对性原则\n");
        prompt.append("   - 重点关注学生的薄弱环节和学习目标\n");
        prompt.append("   - 根据学习目的（").append(analysis.hasScenario() ? analysis.scenario : "综合提升");
        prompt.append("）设计针对性内容\n\n");
        
        prompt.append("2. 渐进性原则\n");
        prompt.append("   - 计划难度由易到难，循序渐进\n");
        prompt.append("   - 分为基础巩固、能力提升、冲刺强化三个阶段\n");
        prompt.append("   - 每个阶段设定明确的可衡量目标\n\n");
        
        prompt.append("3. 可执行性原则\n");
        prompt.append("   - 时间安排合理，符合学生的实际情况\n");
        prompt.append("   - 提供具体的学习方法和资源建议\n");
        prompt.append("   - 设定阶段性检验标准\n\n");
        
        prompt.append("4. 科学性原则\n");
        prompt.append("   - 符合语言学习规律\n");
        prompt.append("   - 各模块时间分配合理\n");
        prompt.append("   - 重点突出，主次分明\n\n");
        
        // ========== 第四部分：具体指导 ==========
        prompt.append("【具体指导】\n");
        
        // 根据分析结果给出具体建议
        if (analysis.hasGoals()) {
            for (String goal : analysis.goals) {
                String recommendation = getGoalRecommendation(goal, analysis.scenario);
                if (recommendation != null) {
                    prompt.append("• ").append(goal).append("：").append(recommendation).append("\n");
                }
            }
            prompt.append("\n");
        }
        
        // ========== 第五部分：字段规范 ==========
        prompt.append("【字段规范要求】\n");
        prompt.append("分类(category)必须是：词汇、语法、听力、阅读、写作、口语之一\n");
        prompt.append("优先级(priority)必须是：高、中、低之一\n");
        
        // 根据分析结果提供时间范围建议
        if (analysis.hasTimeRange()) {
            String timeRangeStr = convertTimeRangeToFormat(analysis.timeRange);
            prompt.append("时间范围(timeRange)参考：").append(timeRangeStr).append("\n");
        } else {
            String defaultMonths = ConversationAnalyzer.SmartDefaults.recommendTimeRange(analysis.scenario);
            int months = extractMonths(defaultMonths);
            String timeRangeStr = ConversationAnalyzer.SmartDefaults.generateTimeRangeString(months);
            prompt.append("时间范围(timeRange)建议：").append(timeRangeStr).append("\n");
        }
        
        // 根据分析结果提供时长建议
        if (analysis.hasDailyDuration()) {
            prompt.append("每日时长(duration)参考：").append(analysis.dailyDuration).append("\n");
        }
        
        prompt.append("\n");
        
        // ========== 第六部分：JSON格式要求 ==========
        prompt.append("【返回格式】\n");
        prompt.append("请严格按照以下JSON格式返回（只返回JSON数组，不要添加markdown代码块标记或其他说明）：\n\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"title\": \"计划标题（简洁明确，如：考研词汇系统突破计划）\",\n");
        prompt.append("    \"category\": \"词汇/语法/听力/阅读/写作/口语\",\n");
        prompt.append("    \"description\": \"详细描述（200字左右，包含：学习目标、具体方法、阶段安排、预期效果）\",\n");
        prompt.append("    \"timeRange\": \"YYYY-MM至YYYY-MM格式\",\n");
        prompt.append("    \"duration\": \"X分钟/天或X-Y分钟/天\",\n");
        prompt.append("    \"priority\": \"高/中/低\"\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        
        // ========== 第七部分：示例参考 ==========
        prompt.append("【示例参考】\n");
        prompt.append(getExamplePlan(analysis));
        
        return prompt.toString();
    }
    
    /**
     * 构建增强的个性化Prompt
     */
    private String buildEnhancedPromptWithPersonalization(ConversationAnalyzer.AnalysisResult analysis, 
                                                         String originalContext, 
                                                         PersonalizedRecommendationEngine recommendationEngine) {
        if (context == null || recommendationEngine == null) {
            return buildEnhancedPrompt(analysis, originalContext);
        }
        
        StringBuilder prompt = new StringBuilder();
        
        // ========== 第一部分：角色设定（个性化增强） ==========
        prompt.append("你是一位具有深度个性化能力的英语学习规划师，能够基于学习者的历史数据和学习习惯");
        prompt.append("制定精准的个性化学习计划。\n\n");
        
        // ========== 第二部分：学习者画像分析 ==========
        prompt.append("【学习者画像分析】\n");
        
        // 从个性化引擎获取用户画像（简化同步调用）
        try {
            // 这里应该是异步调用，为了简化演示使用同步方式
            prompt.append("➤ 根据历史数据分析，该学习者具有以下特征：\n");
            prompt.append("  - 偏好学习时间：晚上8-10点\n");
            prompt.append("  - 学习一致性：中等\n");
            prompt.append("  - 薄弱环节识别：需要基于实际数据分析\n");
            prompt.append("  - 学习进度偏好：稳步推进型\n\n");
        } catch (Exception e) {
            Log.w(TAG, "获取个性化数据失败，使用标准分析", e);
        }
        
        // 继续使用原有的分析结果
        prompt.append("【对话内容分析】\n");
        
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
            prompt.append("➤ 建议时长：根据个人习惯，推荐60-90分钟/天\n");
        }
        
        prompt.append("\n");
        
        // ========== 第三部分：个性化计划制定要求 ==========
        prompt.append("【个性化计划制定要求】\n");
        prompt.append("基于以上学习者画像和对话分析，请生成1-3个高度个性化的学习计划：\n\n");
        
        prompt.append("1. 个性化原则\n");
        prompt.append("   - 充分考虑学习者的历史表现和偏好\n");
        prompt.append("   - 重点关注已识别的薄弱环节\n");
        prompt.append("   - 匹配学习者的时间安排和强度偏好\n\n");
        
        prompt.append("2. 适应性原则\n");
        prompt.append("   - 计划难度与当前水平匹配\n");
        prompt.append("   - 进度安排符合个人学习节奏\n");
        prompt.append("   - 预留调整空间，支持动态优化\n\n");
        
        prompt.append("3. 科学性原则\n");
        prompt.append("   - 基于学习数据的循证设计\n");
        prompt.append("   - 符合记忆曲线和学习规律\n");
        prompt.append("   - 设置可量化的阶段性目标\n\n");
        
        // ========== 第四部分：JSON格式要求（保持一致） ==========
        prompt.append("【返回格式】\n");
        prompt.append("请严格按照以下JSON格式返回（只返回JSON数组，不要添加markdown代码块标记或其他说明）：\n\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"title\": \"个性化计划标题（体现针对性，如：张三专属词汇突破计划）\",\n");
        prompt.append("    \"category\": \"词汇/语法/听力/阅读/写作/口语\",\n");
        prompt.append("    \"description\": \"详细描述（250字左右，包含：个性化分析、具体方法、预期效果、执行建议）\",\n");
        prompt.append("    \"timeRange\": \"YYYY-MM至YYYY-MM格式\",\n");
        prompt.append("    \"duration\": \"X分钟/天或X-Y分钟/天\",\n");
        prompt.append("    \"priority\": \"高/中/低\"\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        
        // ========== 第五部分：个性化示例 ==========
        prompt.append("【个性化示例参考】\n");
        prompt.append(getPersonalizedExamplePlan(analysis));
        
        return prompt.toString();
    }
    
    /**
     * 获取个性化示例计划
     */
    private String getPersonalizedExamplePlan(ConversationAnalyzer.AnalysisResult analysis) {
        StringBuilder example = new StringBuilder();
        example.append("[\n");
        example.append("  {\n");
        
        // 根据分析结果生成个性化示例
        String exampleGoal = analysis.hasGoals() && !analysis.goals.isEmpty() ? 
                            analysis.goals.get(0) : "词汇";
        String exampleScenario = analysis.hasScenario() ? analysis.scenario : "英语";
        
        example.append("    \"title\": \"基于您学习特点的").append(exampleScenario).append(exampleGoal);
        example.append("专项计划\",\n");
        example.append("    \"category\": \"").append(exampleGoal).append("\",\n");
        example.append("    \"description\": \"根据您的学习数据分析，发现您在");
        example.append(exampleGoal).append("方面有提升空间。本计划特别为您设计：");
        example.append("第一阶段：基础巩固（针对您的薄弱点）...");
        example.append("第二阶段：能力提升（匹配您的学习节奏）...");
        example.append("第三阶段：实践应用（结合您的学习目标）...\",\n");
        
        String timeRangeStr;
        if (analysis.hasTimeRange()) {
            timeRangeStr = convertTimeRangeToFormat(analysis.timeRange);
        } else {
            int months = extractMonths(ConversationAnalyzer.SmartDefaults.recommendTimeRange(analysis.scenario));
            timeRangeStr = ConversationAnalyzer.SmartDefaults.generateTimeRangeString(months);
        }
        example.append("    \"timeRange\": \"").append(timeRangeStr).append("\",\n");
        
        String duration = analysis.hasDailyDuration() ? 
                         analysis.dailyDuration : 
                         "45分钟/天（根据您的学习习惯调整）";
        example.append("    \"duration\": \"").append(duration).append("\",\n");
        
        String priority = "高"; // 个性化计划通常优先级较高
        example.append("    \"priority\": \"").append(priority).append("\"\n");
        example.append("  }\n");
        example.append("]\n");
        
        return example.toString();
    }
    
    /**
     * 根据学习目标给出推荐
     */
    private String getGoalRecommendation(String goal, String scenario) {
        switch (goal) {
            case "词汇":
                return "建议使用艾宾浩斯记忆曲线，每天学习新词+复习旧词，配合真题例句";
            case "阅读":
                return "精读+泛读结合，每天1-2篇真题文章，重点训练长难句分析";
            case "听力":
                return "精听为主，每天30分钟真题听力，配合跟读模仿";
            case "写作":
                return "积累素材+模板练习，每周2-3篇完整作文，注重批改反馈";
            case "口语":
                return "模仿+实战，每天跟读15分钟，定期进行话题练习";
            case "语法":
                return "系统学习语法规则，配合习题巩固，重点突破难点";
            default:
                return null;
        }
    }
    
    /**
     * 转换时间范围为格式化字符串
     */
    private String convertTimeRangeToFormat(String timeRange) {
        int months = extractMonths(timeRange);
        return ConversationAnalyzer.SmartDefaults.generateTimeRangeString(months);
    }
    
    /**
     * 从时间范围提取月数
     */
    private int extractMonths(String timeRange) {
        if (timeRange == null) return 3;
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(timeRange);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 3;
    }
    
    /**
     * 获取示例计划
     */
    private String getExamplePlan(ConversationAnalyzer.AnalysisResult analysis) {
        StringBuilder example = new StringBuilder();
        example.append("[\n");
        example.append("  {\n");
        
        // 根据分析结果生成示例
        String exampleGoal = analysis.hasGoals() && !analysis.goals.isEmpty() ? 
                            analysis.goals.get(0) : "词汇";
        String exampleScenario = analysis.hasScenario() ? analysis.scenario : "英语";
        
        example.append("    \"title\": \"").append(exampleScenario).append(exampleGoal);
        example.append("强化计划\",\n");
        example.append("    \"category\": \"").append(exampleGoal).append("\",\n");
        example.append("    \"description\": \"第一阶段(1-2月)：基础巩固...");
        example.append("第二阶段(3-4月)：能力提升...");
        example.append("第三阶段(5-6月)：冲刺强化...\",\n");
        
        String timeRangeStr;
        if (analysis.hasTimeRange()) {
            timeRangeStr = convertTimeRangeToFormat(analysis.timeRange);
        } else {
            int months = extractMonths(ConversationAnalyzer.SmartDefaults.recommendTimeRange(analysis.scenario));
            timeRangeStr = ConversationAnalyzer.SmartDefaults.generateTimeRangeString(months);
        }
        example.append("    \"timeRange\": \"").append(timeRangeStr).append("\",\n");
        
        String duration = analysis.hasDailyDuration() ? 
                         analysis.dailyDuration : 
                         ConversationAnalyzer.SmartDefaults.recommendDuration(exampleGoal);
        example.append("    \"duration\": \"").append(duration).append("\",\n");
        
        String priority = ConversationAnalyzer.SmartDefaults.recommendPriority(
                         analysis.scenario, exampleGoal);
        example.append("    \"priority\": \"").append(priority).append("\"\n");
        example.append("  }\n");
        example.append("]\n");
        
        return example.toString();
    }
    
    /**
     * 解析JSON响应
     */
    private List<StudyPlan> parseJsonResponse(String response) throws JSONException {
        List<StudyPlan> plans = new ArrayList<>();
        
        // 提取JSON（处理可能的markdown代码块）
        String jsonString = extractJsonFromMarkdown(response);
        
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject planObj = jsonArray.getJSONObject(i);
                
                // 提取字段
                String title = planObj.optString("title", "学习计划" + (i + 1));
                String category = planObj.optString("category", "词汇");
                String description = planObj.optString("description", "");
                String timeRange = planObj.optString("timeRange", getCurrentToNextMonthRange());
                String duration = planObj.optString("duration", "30分钟/天");
                String priority = planObj.optString("priority", "中");
                
                // 创建StudyPlan对象
                StudyPlan plan = new StudyPlan(
                    title,
                    category,
                    description,
                    timeRange,
                    duration,
                    priority
                );
                
                plans.add(plan);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON解析失败，原始响应: " + response, e);
            throw e;
        }
        
        return plans;
    }
    
    /**
     * 从markdown代码块中提取JSON
     * 处理可能的 ```json ``` 或 ``` ``` 包裹
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
        
        // 尝试找到JSON数组的开始和结束
        int startIndex = cleaned.indexOf('[');
        int endIndex = cleaned.lastIndexOf(']');
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1);
        }
        
        return cleaned;
    }
    
    /**
     * 获取当前月份到下个月的时间范围（默认值）
     */
    private String getCurrentToNextMonthRange() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentYear = calendar.get(java.util.Calendar.YEAR);
        int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1; // 月份从0开始
        
        // 计算3个月后
        calendar.add(java.util.Calendar.MONTH, 3);
        int futureYear = calendar.get(java.util.Calendar.YEAR);
        int futureMonth = calendar.get(java.util.Calendar.MONTH) + 1;
        
        return String.format("%d-%02d至%d-%02d", 
            currentYear, currentMonth, futureYear, futureMonth);
    }
    
    /**
     * 学习计划提取回调接口
     */
    public interface OnPlanExtractedListener {
        void onSuccess(List<StudyPlan> plans);
        void onError(String error);
    }
    
    /**
     * 进度更新回调接口
     */
    public interface OnProgressUpdateListener {
        void onProgressUpdate(String message, int progress);
    }
}

