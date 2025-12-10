package com.example.mybighomework.utils;

import com.example.mybighomework.database.entity.StudyRecordEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 图表数据处理工具类
 * 负责处理学习数据并转换为图表所需的格式
 */
public class ChartDataProcessor {

    /**
     * 处理每日学习数据（最近N天）
     * @param records 学习记录列表
     * @param days 天数
     * @return 每日学习题目数量的映射
     */
    public static Map<String, Integer> processDailyStudyData(List<StudyRecordEntity> records, int days) {
        Map<String, Integer> dailyCount = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        // 初始化最近N天的数据
        for (int i = days - 1; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = dateFormat.format(calendar.getTime());
            dailyCount.put(dateKey, 0);
        }
        
        // 统计每天的学习题目数量
        for (StudyRecordEntity record : records) {
            if (record.getStudyDate() != null) {
                String dateKey = dateFormat.format(record.getStudyDate());
                if (dailyCount.containsKey(dateKey)) {
                    dailyCount.put(dateKey, dailyCount.get(dateKey) + 1);
                }
            }
        }
        
        return dailyCount;
    }

    /**
     * 处理每周学习数据（最近N周）
     * @param records 学习记录列表
     * @param weeks 周数
     * @return 每周学习题目数量的映射
     */
    public static Map<String, Integer> processWeeklyStudyData(List<StudyRecordEntity> records, int weeks) {
        Map<String, Integer> weeklyCount = new HashMap<>();
        SimpleDateFormat weekFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        // 初始化最近N周的数据
        for (int i = weeks - 1; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.WEEK_OF_YEAR, -i);
            // 设置为周一
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            String weekKey = "第" + (weeks - i) + "周";
            weeklyCount.put(weekKey, 0);
        }
        
        // 统计每周的学习题目数量
        Calendar recordCalendar = Calendar.getInstance();
        for (StudyRecordEntity record : records) {
            if (record.getStudyDate() != null) {
                recordCalendar.setTime(record.getStudyDate());
                
                // 计算该记录属于哪一周
                for (int i = weeks - 1; i >= 0; i--) {
                    calendar.setTime(new Date());
                    calendar.add(Calendar.WEEK_OF_YEAR, -i);
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    Date weekStart = calendar.getTime();
                    
                    calendar.add(Calendar.DAY_OF_YEAR, 6);
                    Date weekEnd = calendar.getTime();
                    
                    if (record.getStudyDate().compareTo(weekStart) >= 0 && 
                        record.getStudyDate().compareTo(weekEnd) <= 0) {
                        String weekKey = "第" + (weeks - i) + "周";
                        weeklyCount.put(weekKey, weeklyCount.get(weekKey) + 1);
                        break;
                    }
                }
            }
        }
        
        return weeklyCount;
    }

    /**
     * 按学习类型统计数据
     * @param records 学习记录列表
     * @return 各类型学习数量的映射
     */
    public static Map<String, Integer> processStudyTypeData(List<StudyRecordEntity> records) {
        Map<String, Integer> typeCount = new HashMap<>();
        
        // 初始化各类型计数
        typeCount.put("词汇训练", 0);
        typeCount.put("真题练习", 0);
        typeCount.put("模拟考试", 0);
        typeCount.put("错题复习", 0);
        
        // 统计各类型的学习数量
        for (StudyRecordEntity record : records) {
            String studyType = record.getStudyType();
            if (studyType != null) {
                switch (studyType) {
                    case "vocabulary":
                        typeCount.put("词汇训练", typeCount.get("词汇训练") + 1);
                        break;
                    case "exam_practice":
                        typeCount.put("真题练习", typeCount.get("真题练习") + 1);
                        break;
                    case "mock_exam":
                        typeCount.put("模拟考试", typeCount.get("模拟考试") + 1);
                        break;
                    case "wrong_question":
                        typeCount.put("错题复习", typeCount.get("错题复习") + 1);
                        break;
                }
            }
        }
        
        return typeCount;
    }

    /**
     * 按正确率统计数据
     * @param records 学习记录列表
     * @return 正确和错误的数量
     */
    public static Map<String, Integer> processAccuracyData(List<StudyRecordEntity> records) {
        Map<String, Integer> accuracyCount = new HashMap<>();
        int correctCount = 0;
        int wrongCount = 0;
        
        for (StudyRecordEntity record : records) {
            if (record.isCorrect()) {
                correctCount++;
            } else {
                wrongCount++;
            }
        }
        
        accuracyCount.put("答对", correctCount);
        accuracyCount.put("答错", wrongCount);
        
        return accuracyCount;
    }

    /**
     * 按难度统计数据
     * @param records 学习记录列表
     * @return 各难度级别的数量
     */
    public static Map<String, Integer> processDifficultyData(List<StudyRecordEntity> records) {
        Map<String, Integer> difficultyCount = new HashMap<>();
        
        // 初始化难度级别计数
        difficultyCount.put("简单", 0);
        difficultyCount.put("中等", 0);
        difficultyCount.put("困难", 0);
        
        // 统计各难度级别的学习数量
        for (StudyRecordEntity record : records) {
            String difficulty = record.getDifficulty();
            if (difficulty != null) {
                switch (difficulty.toLowerCase()) {
                    case "easy":
                    case "简单":
                        difficultyCount.put("简单", difficultyCount.get("简单") + 1);
                        break;
                    case "medium":
                    case "中等":
                        difficultyCount.put("中等", difficultyCount.get("中等") + 1);
                        break;
                    case "hard":
                    case "困难":
                        difficultyCount.put("困难", difficultyCount.get("困难") + 1);
                        break;
                }
            }
        }
        
        return difficultyCount;
    }

    /**
     * 计算学习趋势（是否在进步）
     * @param records 学习记录列表
     * @param days 分析的天数
     * @return 趋势分析结果
     */
    public static StudyTrend calculateStudyTrend(List<StudyRecordEntity> records, int days) {
        if (records.isEmpty() || days < 2) {
            return new StudyTrend(0, "数据不足");
        }
        
        Map<String, Integer> dailyData = processDailyStudyData(records, days);
        List<Integer> values = new ArrayList<>(dailyData.values());
        
        // 计算前半段和后半段的平均值
        int midPoint = values.size() / 2;
        double firstHalfAvg = values.subList(0, midPoint).stream()
                .mapToInt(Integer::intValue).average().orElse(0);
        double secondHalfAvg = values.subList(midPoint, values.size()).stream()
                .mapToInt(Integer::intValue).average().orElse(0);
        
        double trendValue = secondHalfAvg - firstHalfAvg;
        String trendDescription;
        
        if (trendValue > 1) {
            trendDescription = "学习量显著增加，保持良好势头！";
        } else if (trendValue > 0) {
            trendDescription = "学习量稳步增长，继续努力！";
        } else if (trendValue > -1) {
            trendDescription = "学习量基本稳定，可以尝试增加练习量。";
        } else {
            trendDescription = "学习量有所下降，建议调整学习计划。";
        }
        
        return new StudyTrend(trendValue, trendDescription);
    }

    /**
     * 学习趋势数据类
     */
    public static class StudyTrend {
        private final double trendValue;
        private final String description;
        
        public StudyTrend(double trendValue, String description) {
            this.trendValue = trendValue;
            this.description = description;
        }
        
        public double getTrendValue() {
            return trendValue;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isPositive() {
            return trendValue > 0;
        }
    }

    /**
     * 获取日期标签数组
     * @param days 天数
     * @return 日期标签数组
     */
    public static String[] getDailyLabels(int days) {
        String[] labels = new String[days];
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        
        for (int i = days - 1; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            labels[days - 1 - i] = dateFormat.format(calendar.getTime());
        }
        
        return labels;
    }

    /**
     * 获取周标签数组
     * @param weeks 周数
     * @return 周标签数组
     */
    public static String[] getWeeklyLabels(int weeks) {
        String[] labels = new String[weeks];
        for (int i = 0; i < weeks; i++) {
            labels[i] = "第" + (i + 1) + "周";
        }
        return labels;
    }
}
