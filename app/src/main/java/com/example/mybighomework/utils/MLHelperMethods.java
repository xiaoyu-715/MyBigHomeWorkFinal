package com.example.mybighomework.utils;

import android.util.Log;
import com.example.mybighomework.StudyPlan;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ML辅助方法类
 * 包含机器学习相关的辅助方法，从PersonalizedRecommendationEngine中分离出来
 */
public class MLHelperMethods {
    
    private static final String TAG = "MLHelperMethods";
    
    /**
     * 从用户画像中提取特征向量
     */
    public static double[] extractFeaturesFromProfile(PersonalizedRecommendationEngine.UserProfile profile) {
        return new double[] {
            profile.vocabularyMasteryLevel / 100.0,  // 归一化到[0,1]
            profile.averageExamScore / 100.0,        // 归一化到[0,1]
            Math.min(profile.dailyStudyMinutes / 120.0, 1.0), // 归一化，最大120分钟
            profile.consistencyScore,                 // 已经是[0,1]
            profile.motivationLevel / 100.0          // 归一化到[0,1]
        };
    }
    
    /**
     * 基于ML评分重新排序计划
     */
    public static List<StudyPlan> reorderPlansBasedOnMLScore(List<StudyPlan> plans, 
                                                           PersonalizedRecommendationEngine.UserProfile profile, 
                                                           double mlScore) {
        if (plans == null || plans.isEmpty()) {
            return plans;
        }
        
        try {
            // 简化的重排序逻辑
            if (mlScore > 0.8) {
                // 高分用户：推荐高强度计划
                return plans.stream()
                    .sorted((p1, p2) -> {
                        String priority1 = p1.getPriority();
                        String priority2 = p2.getPriority();
                        if ("高".equals(priority2) && !"高".equals(priority1)) return 1;
                        if ("高".equals(priority1) && !"高".equals(priority2)) return -1;
                        return 0;
                    })
                    .collect(Collectors.toList());
            } else if (mlScore > 0.6) {
                // 中等用户：平衡推荐
                return plans; // 保持原顺序
            } else {
                // 低分用户：推荐轻松计划
                return plans.stream()
                    .sorted((p1, p2) -> {
                        String priority1 = p1.getPriority();
                        String priority2 = p2.getPriority();
                        if ("低".equals(priority1) && !"低".equals(priority2)) return -1;
                        if ("低".equals(priority2) && !"低".equals(priority1)) return 1;
                        return 0;
                    })
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            Log.e(TAG, "重排序计划失败", e);
            return plans; // 返回原始列表
        }
    }
    
    /**
     * 记录性能指标
     */
    public static void recordPerformanceMetrics(Map<String, PersonalizedRecommendationEngine.PerformanceMetrics> performanceHistory,
                                              PersonalizedRecommendationEngine.RecommendationResult result, 
                                              long responseTime, 
                                              String variant) {
        try {
            PersonalizedRecommendationEngine.PerformanceMetrics metrics = 
                new PersonalizedRecommendationEngine.PerformanceMetrics();
            metrics.responseTime = responseTime;
            metrics.variant = variant;
            
            // 模拟性能指标（在实际应用中应该从用户反馈获取）
            metrics.accuracy = 0.85 + Math.random() * 0.1; // 85-95%
            metrics.userSatisfaction = 0.8 + Math.random() * 0.15; // 80-95%
            metrics.clickThroughRate = 0.6 + Math.random() * 0.2; // 60-80%
            metrics.conversionRate = 0.3 + Math.random() * 0.2; // 30-50%
            
            String metricsKey = System.currentTimeMillis() + "_" + variant;
            performanceHistory.put(metricsKey, metrics);
            
            // 限制历史记录数量
            if (performanceHistory.size() > 100) {
                cleanupOldMetrics(performanceHistory);
            }
            
            Log.d(TAG, String.format("性能指标已记录: 响应=%dms, 准确率=%.2f, 满意度=%.2f", 
                responseTime, metrics.accuracy, metrics.userSatisfaction));
                
        } catch (Exception e) {
            Log.e(TAG, "记录性能指标失败", e);
        }
    }
    
    /**
     * 清理旧的性能指标
     */
    public static void cleanupOldMetrics(Map<String, PersonalizedRecommendationEngine.PerformanceMetrics> performanceHistory) {
        long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L); // 7天前
        
        performanceHistory.entrySet().removeIf(entry -> 
            entry.getValue().timestamp < cutoffTime);
        
        Log.d(TAG, "旧性能指标已清理，剩余: " + performanceHistory.size() + " 条");
    }
    
    /**
     * 触发模型自我学习
     */
    public static void triggerModelSelfLearning(PersonalizedRecommendationEngine.SimpleMLModel model,
                                              PersonalizedRecommendationEngine.UserProfile profile,
                                              PersonalizedRecommendationEngine.RecommendationResult result,
                                              Map<String, PersonalizedRecommendationEngine.PerformanceMetrics> performanceHistory,
                                              int minSamples,
                                              double learningRate) {
        if (model == null) {
            return;
        }
        
        try {
            // 收集训练数据
            List<double[]> features = new ArrayList<>();
            List<Double> labels = new ArrayList<>();
            
            // 从历史性能数据中构建训练集
            for (PersonalizedRecommendationEngine.PerformanceMetrics metrics : performanceHistory.values()) {
                if (metrics.accuracy > 0 && metrics.userSatisfaction > 0) {
                    // 使用当前profile作为特征（简化实现）
                    features.add(extractFeaturesFromProfile(profile));
                    
                    // 使用综合评分作为标签
                    labels.add(metrics.getOverallScore());
                }
            }
            
            if (features.size() >= minSamples) {
                // 执行增量学习
                model.train(features, labels, learningRate);
                
                Log.d(TAG, "模型自我学习完成，训练样本: " + features.size());
            } else {
                Log.d(TAG, "训练样本不足，跳过自我学习");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "模型自我学习失败", e);
        }
    }
    
    /**
     * 计算ML增强的置信度
     */
    public static int calculateMLEnhancedConfidence(PersonalizedRecommendationEngine.SimpleMLModel model,
                                                  PersonalizedRecommendationEngine.UserProfile profile,
                                                  int baseConfidence) {
        if (model == null) {
            return baseConfidence;
        }
        
        try {
            double[] features = extractFeaturesFromProfile(profile);
            double mlPrediction = model.predict(features);
            
            // ML预测增强置信度
            double mlBonus = mlPrediction * 20; // 最多增加20分
            int enhancedConfidence = (int) Math.min(baseConfidence + mlBonus, 100);
            
            Log.d(TAG, String.format("置信度增强: 基础=%d, ML增强=%d (+%.1f)", 
                baseConfidence, enhancedConfidence, mlBonus));
            
            return enhancedConfidence;
            
        } catch (Exception e) {
            Log.e(TAG, "ML置信度增强失败", e);
            return baseConfidence;
        }
    }
    
    /**
     * 评估模型性能
     */
    public static ModelPerformanceReport evaluateModelPerformance(
            Map<String, PersonalizedRecommendationEngine.PerformanceMetrics> performanceHistory) {
        
        ModelPerformanceReport report = new ModelPerformanceReport();
        
        if (performanceHistory.isEmpty()) {
            report.averageAccuracy = 0.0;
            report.averageResponseTime = 0L;
            report.averageSatisfaction = 0.0;
            report.sampleCount = 0;
            return report;
        }
        
        double totalAccuracy = 0.0;
        long totalResponseTime = 0L;
        double totalSatisfaction = 0.0;
        double totalClickThrough = 0.0;
        double totalConversion = 0.0;
        
        int count = performanceHistory.size();
        
        for (PersonalizedRecommendationEngine.PerformanceMetrics metrics : performanceHistory.values()) {
            totalAccuracy += metrics.accuracy;
            totalResponseTime += metrics.responseTime;
            totalSatisfaction += metrics.userSatisfaction;
            totalClickThrough += metrics.clickThroughRate;
            totalConversion += metrics.conversionRate;
        }
        
        report.averageAccuracy = totalAccuracy / count;
        report.averageResponseTime = totalResponseTime / count;
        report.averageSatisfaction = totalSatisfaction / count;
        report.averageClickThroughRate = totalClickThrough / count;
        report.averageConversionRate = totalConversion / count;
        report.sampleCount = count;
        report.overallScore = (report.averageAccuracy + report.averageSatisfaction + 
                             report.averageClickThroughRate + report.averageConversionRate) / 4.0;
        
        return report;
    }
    
    /**
     * 模型性能报告
     */
    public static class ModelPerformanceReport {
        public double averageAccuracy;
        public long averageResponseTime;
        public double averageSatisfaction;
        public double averageClickThroughRate;
        public double averageConversionRate;
        public int sampleCount;
        public double overallScore;
        
        @Override
        public String toString() {
            return String.format("模型性能报告:\n" +
                "平均准确率: %.2f%%\n" +
                "平均响应时间: %dms\n" +
                "平均满意度: %.2f%%\n" +
                "平均点击率: %.2f%%\n" +
                "平均转换率: %.2f%%\n" +
                "样本数量: %d\n" +
                "综合评分: %.2f%%",
                averageAccuracy * 100,
                averageResponseTime,
                averageSatisfaction * 100,
                averageClickThroughRate * 100,
                averageConversionRate * 100,
                sampleCount,
                overallScore * 100);
        }
    }
}
