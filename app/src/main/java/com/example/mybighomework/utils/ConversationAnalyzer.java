package com.example.mybighomework.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对话内容智能分析器
 * 从对话中提取学习目标、时间信息、水平等关键信息
 */
public class ConversationAnalyzer {
    
    private static final String TAG = "ConversationAnalyzer";
    
    // 正则表达式模式
    private static final Pattern TIME_RANGE_PATTERN = 
        Pattern.compile("(\\d+)\\s*(个月|月|周|天|年)");
    
    private static final Pattern DAILY_DURATION_PATTERN = 
        Pattern.compile("每天\\s*(\\d+\\.?\\d*)\\s*(小时|分钟|h|min|hour|minute)");
    
    private static final Pattern LEVEL_PATTERN = 
        Pattern.compile("(四级|六级|CET4|CET6|雅思|托福|考研|初级|中级|高级|基础|进阶)(\\d+)?分?");
    
    /**
     * 分析结果类
     */
    public static class AnalysisResult {
        public String scenario;           // 学习场景
        public List<String> goals;        // 学习目标
        public String timeRange;          // 时间范围
        public String dailyDuration;      // 每日时长
        public String currentLevel;       // 当前水平
        public List<String> weakPoints;   // 薄弱点
        
        public AnalysisResult() {
            this.goals = new ArrayList<>();
            this.weakPoints = new ArrayList<>();
        }
        
        public boolean hasScenario() { return scenario != null && !scenario.isEmpty(); }
        public boolean hasGoals() { return goals != null && !goals.isEmpty(); }
        public boolean hasTimeRange() { return timeRange != null && !timeRange.isEmpty(); }
        public boolean hasDailyDuration() { return dailyDuration != null && !dailyDuration.isEmpty(); }
        public boolean hasCurrentLevel() { return currentLevel != null && !currentLevel.isEmpty(); }
        public boolean hasWeakPoints() { return weakPoints != null && !weakPoints.isEmpty(); }
        
        @Override
        public String toString() {
            return "AnalysisResult{" +
                    "scenario='" + scenario + '\'' +
                    ", goals=" + goals +
                    ", timeRange='" + timeRange + '\'' +
                    ", dailyDuration='" + dailyDuration + '\'' +
                    ", currentLevel='" + currentLevel + '\'' +
                    ", weakPoints=" + weakPoints +
                    '}';
        }
    }
    
    /**
     * 分析对话内容
     */
    public AnalysisResult analyze(String conversation) {
        if (conversation == null || conversation.isEmpty()) {
            return new AnalysisResult();
        }
        
        AnalysisResult result = new AnalysisResult();
        
        // 提取学习场景
        result.scenario = extractScenario(conversation);
        
        // 提取学习目标
        result.goals = extractGoals(conversation);
        
        // 提取时间范围
        result.timeRange = extractTimeRange(conversation);
        
        // 提取每日时长
        result.dailyDuration = extractDailyDuration(conversation);
        
        // 提取当前水平
        result.currentLevel = extractLevel(conversation);
        
        // 提取薄弱点
        result.weakPoints = extractWeakPoints(conversation);
        
        Log.d(TAG, "分析结果: " + result);
        
        return result;
    }
    
    /**
     * 提取学习场景
     */
    private String extractScenario(String text) {
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("考研")) {
            return "考研";
        } else if (lowerText.contains("四级") || lowerText.contains("cet4") || lowerText.contains("cet-4")) {
            return "英语四级";
        } else if (lowerText.contains("六级") || lowerText.contains("cet6") || lowerText.contains("cet-6")) {
            return "英语六级";
        } else if (lowerText.contains("雅思") || lowerText.contains("ielts")) {
            return "雅思";
        } else if (lowerText.contains("托福") || lowerText.contains("toefl")) {
            return "托福";
        } else if (lowerText.contains("高考")) {
            return "高考英语";
        } else if (lowerText.contains("考博")) {
            return "考博英语";
        }
        
        return null;
    }
    
    /**
     * 提取学习目标
     */
    private List<String> extractGoals(String text) {
        List<String> goals = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        // 词汇相关
        if (lowerText.contains("词汇") || lowerText.contains("单词") || 
            lowerText.contains("背单词") || lowerText.contains("记单词")) {
            goals.add("词汇");
        }
        
        // 阅读相关
        if (lowerText.contains("阅读") || lowerText.contains("阅读理解")) {
            goals.add("阅读");
        }
        
        // 听力相关
        if (lowerText.contains("听力") || lowerText.contains("听说")) {
            goals.add("听力");
        }
        
        // 写作相关
        if (lowerText.contains("写作") || lowerText.contains("作文") || lowerText.contains("写文章")) {
            goals.add("写作");
        }
        
        // 口语相关
        if (lowerText.contains("口语") || lowerText.contains("说英语") || lowerText.contains("speaking")) {
            goals.add("口语");
        }
        
        // 语法相关
        if (lowerText.contains("语法") || lowerText.contains("grammar")) {
            goals.add("语法");
        }
        
        return goals;
    }
    
    /**
     * 提取时间范围
     */
    private String extractTimeRange(String text) {
        Matcher matcher = TIME_RANGE_PATTERN.matcher(text);
        
        if (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            // 转换为月份
            int months = 0;
            switch (unit) {
                case "年":
                    months = number * 12;
                    break;
                case "个月":
                case "月":
                    months = number;
                    break;
                case "周":
                    months = Math.max(1, number / 4);  // 4周约1个月
                    break;
                case "天":
                    months = Math.max(1, number / 30); // 30天约1个月
                    break;
            }
            
            if (months > 0) {
                return months + "个月";
            }
        }
        
        // 检查特殊表达
        if (text.contains("半年")) {
            return "6个月";
        } else if (text.contains("一年")) {
            return "12个月";
        } else if (text.contains("两年")) {
            return "24个月";
        }
        
        return null;
    }
    
    /**
     * 提取每日时长
     */
    private String extractDailyDuration(String text) {
        Matcher matcher = DAILY_DURATION_PATTERN.matcher(text);
        
        if (matcher.find()) {
            double number = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2);
            
            int minutes = 0;
            
            // 转换为分钟
            if (unit.equals("小时") || unit.equals("h") || unit.equals("hour")) {
                minutes = (int) (number * 60);
            } else if (unit.equals("分钟") || unit.equals("min") || unit.equals("minute")) {
                minutes = (int) number;
            }
            
            if (minutes > 0) {
                return minutes + "分钟/天";
            }
        }
        
        // 检查其他表达方式
        if (text.contains("一小时") || text.contains("1小时") || text.contains("1h")) {
            return "60分钟/天";
        } else if (text.contains("两小时") || text.contains("2小时") || text.contains("2h")) {
            return "120分钟/天";
        } else if (text.contains("半小时") || text.contains("0.5小时")) {
            return "30分钟/天";
        }
        
        return null;
    }
    
    /**
     * 提取当前水平
     */
    private String extractLevel(String text) {
        Matcher matcher = LEVEL_PATTERN.matcher(text);
        
        if (matcher.find()) {
            String level = matcher.group(1);
            String score = matcher.group(2);
            
            if (score != null) {
                return level + score + "分水平";
            } else {
                return level + "水平";
            }
        }
        
        // 检查词汇量描述
        if (text.contains("词汇量")) {
            Pattern vocabPattern = Pattern.compile("词汇量\\s*(\\d+)");
            Matcher vocabMatcher = vocabPattern.matcher(text);
            if (vocabMatcher.find()) {
                return "词汇量约" + vocabMatcher.group(1) + "个";
            }
        }
        
        // 检查基础描述
        if (text.contains("零基础") || text.contains("0基础")) {
            return "零基础";
        } else if (text.contains("有一定基础")) {
            return "有一定基础";
        }
        
        return null;
    }
    
    /**
     * 提取薄弱点
     */
    private List<String> extractWeakPoints(String text) {
        List<String> weakPoints = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        String[] weakKeywords = {
            "薄弱", "不好", "差", "不行", "弱", "需要提高", "需要加强", "困难"
        };
        
        // 检查词汇薄弱
        for (String keyword : weakKeywords) {
            if (lowerText.contains("词汇" + keyword) || 
                lowerText.contains("单词" + keyword)) {
                weakPoints.add("词汇薄弱");
                break;
            }
        }
        
        // 检查阅读薄弱
        for (String keyword : weakKeywords) {
            if (lowerText.contains("阅读" + keyword)) {
                weakPoints.add("阅读薄弱");
                break;
            }
        }
        
        // 检查听力薄弱
        for (String keyword : weakKeywords) {
            if (lowerText.contains("听力" + keyword)) {
                weakPoints.add("听力薄弱");
                break;
            }
        }
        
        // 检查写作薄弱
        for (String keyword : weakKeywords) {
            if (lowerText.contains("写作" + keyword) || 
                lowerText.contains("作文" + keyword)) {
                weakPoints.add("写作薄弱");
                break;
            }
        }
        
        // 检查口语薄弱
        for (String keyword : weakKeywords) {
            if (lowerText.contains("口语" + keyword)) {
                weakPoints.add("口语薄弱");
                break;
            }
        }
        
        // 检查语法薄弱
        for (String keyword : weakKeywords) {
            if (lowerText.contains("语法" + keyword)) {
                weakPoints.add("语法薄弱");
                break;
            }
        }
        
        return weakPoints;
    }
    
    /**
     * 获取智能默认值
     */
    public static class SmartDefaults {
        
        /**
         * 根据场景推荐时间范围
         */
        public static String recommendTimeRange(String scenario) {
            if (scenario == null) {
                return "3个月";
            }
            
            switch (scenario) {
                case "考研":
                    return "6个月";
                case "英语四级":
                case "英语六级":
                    return "3个月";
                case "雅思":
                case "托福":
                    return "4个月";
                case "高考英语":
                    return "12个月";
                default:
                    return "3个月";
            }
        }
        
        /**
         * 根据分类推荐每日时长
         */
        public static String recommendDuration(String category) {
            if (category == null) {
                return "60分钟/天";
            }
            
            switch (category) {
                case "词汇":
                    return "40-60分钟/天";
                case "阅读":
                    return "60-90分钟/天";
                case "听力":
                    return "30-60分钟/天";
                case "写作":
                    return "60-90分钟/天";
                case "口语":
                    return "30-45分钟/天";
                case "语法":
                    return "30-60分钟/天";
                default:
                    return "60分钟/天";
            }
        }
        
        /**
         * 根据场景和目标推荐优先级
         */
        public static String recommendPriority(String scenario, String goal) {
            // 考研的词汇、阅读为高优先级
            if ("考研".equals(scenario)) {
                if ("词汇".equals(goal) || "阅读".equals(goal)) {
                    return "高";
                }
            }
            
            // 四六级的词汇、听力为高优先级
            if ("英语四级".equals(scenario) || "英语六级".equals(scenario)) {
                if ("词汇".equals(goal) || "听力".equals(goal)) {
                    return "高";
                }
            }
            
            // 雅思托福的口语、写作为高优先级
            if ("雅思".equals(scenario) || "托福".equals(scenario)) {
                if ("口语".equals(goal) || "写作".equals(goal)) {
                    return "高";
                }
            }
            
            return "中";
        }
        
        /**
         * 生成默认时间范围字符串（格式：YYYY-MM至YYYY-MM）
         */
        public static String generateTimeRangeString(int months) {
            Calendar startCal = Calendar.getInstance();
            int startYear = startCal.get(Calendar.YEAR);
            int startMonth = startCal.get(Calendar.MONTH) + 1;
            
            Calendar endCal = Calendar.getInstance();
            endCal.add(Calendar.MONTH, months);
            int endYear = endCal.get(Calendar.YEAR);
            int endMonth = endCal.get(Calendar.MONTH) + 1;
            
            return String.format("%d-%02d至%d-%02d", 
                startYear, startMonth, endYear, endMonth);
        }
    }
}


