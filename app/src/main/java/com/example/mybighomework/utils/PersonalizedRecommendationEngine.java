package com.example.mybighomework.utils;

import android.content.Context;
import android.util.Log;

import com.example.mybighomework.StudyPlan;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;
import com.example.mybighomework.database.entity.ExamRecordEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * 个性化推荐引擎
 * 基于用户历史学习数据，提供智能化的学习计划推荐
 */
public class PersonalizedRecommendationEngine {
    
    private static final String TAG = "PersonalizationEngine";
    
    private Context context;
    private AppDatabase database;
    private ExecutorService executorService;
    
    // 推荐权重配置 (动态调整)
    private volatile double vocabularyWeight = 0.3;
    private volatile double examWeight = 0.4;
    private volatile double studyTimeWeight = 0.2;
    private volatile double recentActivityWeight = 0.1;
    
    // 机器学习相关配置
    private static final int MIN_SAMPLES_FOR_ML = 10; // 启用ML所需的最小样本数
    private static final double LEARNING_RATE = 0.01; // 学习率
    private static final int MAX_ITERATIONS = 100; // 最大迭代次数
    
    // A/B测试配置
    private static final Map<String, Double> AB_TEST_VARIANTS = new HashMap<String, Double>() {{
        put("variant_a", 1.0);  // 标准权重
        put("variant_b", 1.2);  // 增强版权重
        put("variant_c", 0.8);  // 轻量版权重
    }};
    
    // 性能指标跟踪
    private final Map<String, PerformanceMetrics> performanceHistory = new ConcurrentHashMap<>();
    
    // 缓存机制
    private static final long CACHE_EXPIRY_TIME = 30 * 60 * 1000L; // 30分钟
    private final Map<String, CacheEntry<UserProfile>> profileCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<RecommendationResult>> recommendationCache = new ConcurrentHashMap<>();
    
    /**
     * 缓存条目
     */
    private static class CacheEntry<T> {
        public final T data;
        public final long timestamp;
        
        public CacheEntry(T data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME;
        }
    }
    
    /**
     * 性能指标类
     */
    public static class PerformanceMetrics {
        public long timestamp;
        public double accuracy;          // 推荐准确率
        public double userSatisfaction;  // 用户满意度
        public long responseTime;        // 响应时间
        public double clickThroughRate;  // 点击率
        public double conversionRate;    // 转换率
        public String variant;           // A/B测试变体
        
        public PerformanceMetrics() {
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * 计算综合评分
         */
        public double getOverallScore() {
            return (accuracy * 0.3 + userSatisfaction * 0.3 + 
                   clickThroughRate * 0.2 + conversionRate * 0.2);
        }
    }
    
    /**
     * 机器学习模型 (简化的线性回归)
     */
    public static class SimpleMLModel {
        private double[] weights;
        private double bias;
        private int featureCount;
        
        public SimpleMLModel(int featureCount) {
            this.featureCount = featureCount;
            this.weights = new double[featureCount];
            this.bias = 0.0;
            
            // 随机初始化权重
            for (int i = 0; i < featureCount; i++) {
                weights[i] = Math.random() * 0.1 - 0.05; // [-0.05, 0.05]
            }
        }
        
        /**
         * 预测
         */
        public double predict(double[] features) {
            if (features.length != featureCount) {
                throw new IllegalArgumentException("特征数量不匹配");
            }
            
            double sum = bias;
            for (int i = 0; i < featureCount; i++) {
                sum += weights[i] * features[i];
            }
            
            return sigmoid(sum);
        }
        
        /**
         * 训练模型 (梯度下降)
         */
        public void train(List<double[]> features, List<Double> labels, double learningRate) {
            for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
                double totalLoss = 0.0;
                
                for (int i = 0; i < features.size(); i++) {
                    double[] x = features.get(i);
                    double y = labels.get(i);
                    double prediction = predict(x);
                    double error = prediction - y;
                    
                    totalLoss += error * error;
                    
                    // 更新权重
                    for (int j = 0; j < featureCount; j++) {
                        weights[j] -= learningRate * error * prediction * (1 - prediction) * x[j];
                    }
                    
                    // 更新偏置
                    bias -= learningRate * error * prediction * (1 - prediction);
                }
                
                // 早停条件
                if (totalLoss / features.size() < 0.01) {
                    Log.d(TAG, "模型训练收敛，迭代次数: " + iteration);
                    break;
                }
            }
        }
        
        private double sigmoid(double x) {
            return 1.0 / (1.0 + Math.exp(-x));
        }
    }
    
    // ML模型实例
    private SimpleMLModel recommendationModel;
    private boolean isMLEnabled = false;
    
    public PersonalizedRecommendationEngine(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        
        // 初始化ML模型
        initializeMLModel();
        
        // 加载历史性能数据
        loadPerformanceHistory();
    }
    
    /**
     * 初始化机器学习模型
     */
    private void initializeMLModel() {
        try {
            // 特征维度：词汇掌握度、平均成绩、学习时长、一致性分数、动机水平
            this.recommendationModel = new SimpleMLModel(5);
            
            // 检查是否有足够的历史数据来启用ML
            checkMLEligibility();
            
            Log.d(TAG, "机器学习模型初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "初始化ML模型失败", e);
            this.isMLEnabled = false;
        }
    }
    
    /**
     * 检查ML启用条件
     */
    private void checkMLEligibility() {
        int sampleCount = performanceHistory.size();
        this.isMLEnabled = sampleCount >= MIN_SAMPLES_FOR_ML;
        
        Log.d(TAG, "ML启用状态: " + isMLEnabled + " (样本数: " + sampleCount + ")");
    }
    
    /**
     * 加载历史性能数据
     */
    private void loadPerformanceHistory() {
        // 简化实现：从SharedPreferences加载
        // 在真实应用中应该从数据库加载
        Log.d(TAG, "历史性能数据加载完成");
    }
    
    /**
     * 用户学习画像
     */
    public static class UserProfile {
        public int vocabularyMasteryLevel;    // 词汇掌握水平 (0-100)
        public double averageExamScore;       // 平均考试成绩
        public int dailyStudyMinutes;         // 日均学习时长
        public List<String> weakCategories;  // 薄弱类别
        public List<String> strongCategories; // 强势类别
        public String preferredStudyTime;     // 偏好学习时间段
        public int continuousStudyDays;       // 连续学习天数
        public String currentLevel;           // 当前英语水平
        
        // 新增智能分析字段
        public double learningEfficiency;     // 学习效率 (0-1)
        public String learningStyle;          // 学习风格 (视觉型/听觉型/动觉型)
        public int motivationLevel;           // 学习动机强度 (0-100)
        public double consistencyScore;       // 学习一致性 (0-1)
        public long lastActiveTime;           // 最后活跃时间
        public Map<String, Double> categoryProgress; // 各类别进度
        public List<String> achievedGoals;    // 已达成目标
        public String targetExam;             // 目标考试
        public int studyStreak;               // 连续学习天数
        public double retentionRate;          // 知识保持率
        
        public UserProfile() {
            this.weakCategories = new ArrayList<>();
            this.strongCategories = new ArrayList<>();
            this.categoryProgress = new HashMap<>();
            this.achievedGoals = new ArrayList<>();
            this.learningEfficiency = 0.7; // 默认效率
            this.learningStyle = "综合型";
            this.motivationLevel = 75; // 默认动机
            this.consistencyScore = 0.6; // 默认一致性
        }
        
        /**
         * 获取学习者类型
         */
        public String getLearnerType() {
            if (consistencyScore > 0.8 && dailyStudyMinutes > 60) {
                return "勤奋型学习者";
            } else if (learningEfficiency > 0.8) {
                return "高效型学习者";
            } else if (continuousStudyDays > 14) {
                return "坚持型学习者";
            } else if (motivationLevel > 80) {
                return "积极型学习者";
            } else {
                return "普通学习者";
            }
        }
        
        /**
         * 获取推荐学习强度
         */
        public String getRecommendedIntensity() {
            double intensity = (learningEfficiency + consistencyScore + motivationLevel / 100.0) / 3;
            if (intensity > 0.8) {
                return "高强度";
            } else if (intensity > 0.6) {
                return "中等强度";
            } else {
                return "轻度强度";
            }
        }
        
        @Override
        public String toString() {
            return "UserProfile{" +
                    "vocabularyLevel=" + vocabularyMasteryLevel +
                    ", avgScore=" + averageExamScore +
                    ", dailyMinutes=" + dailyStudyMinutes +
                    ", weakAreas=" + weakCategories +
                    ", strongAreas=" + strongCategories +
                    ", level='" + currentLevel + '\'' +
                    '}';
        }
    }
    
    /**
     * 个性化推荐结果
     */
    public static class RecommendationResult {
        public List<StudyPlan> recommendedPlans;
        public UserProfile userProfile;
        public String recommendationReason;
        public int confidenceScore; // 推荐置信度 (0-100)
        
        public RecommendationResult() {
            this.recommendedPlans = new ArrayList<>();
        }
    }
    
    /**
     * 生成个性化推荐 (带缓存优化)
     */
    public void generateRecommendations(OnRecommendationListener listener) {
        String cacheKey = "user_recommendations";
        
        // 检查缓存
        CacheEntry<RecommendationResult> cachedResult = recommendationCache.get(cacheKey);
        if (cachedResult != null && !cachedResult.isExpired()) {
            Log.d(TAG, "使用缓存的推荐结果");
            if (listener != null) {
                listener.onRecommendationGenerated(cachedResult.data);
            }
            return;
        }
        
        executorService.execute(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // 1. 构建用户画像 (带缓存)
                UserProfile userProfile = buildUserProfileWithCache();
                
                // 2. 应用A/B测试权重调整
                String variant = applyABTestVariant();
                adjustWeightsForVariant(variant);
                
                // 3. 基于画像生成推荐计划 (ML增强)
                List<StudyPlan> recommendedPlans = generateMLEnhancedRecommendations(userProfile);
                
                // 4. 计算推荐置信度 (ML增强)
                int confidenceScore = calculateMLEnhancedConfidence(userProfile);
                
                // 5. 生成推荐理由
                String reason = generateRecommendationReason(userProfile);
                
                RecommendationResult result = new RecommendationResult();
                result.userProfile = userProfile;
                result.recommendedPlans = recommendedPlans;
                result.confidenceScore = confidenceScore;
                result.recommendationReason = reason;
                
                // 6. 记录性能指标
                long responseTime = System.currentTimeMillis() - startTime;
                recordPerformanceMetrics(result, responseTime, variant);
                
                // 7. 触发模型自我学习
                if (isMLEnabled) {
                    triggerModelSelfLearning(userProfile, result);
                }
                
                // 缓存结果
                recommendationCache.put(cacheKey, new CacheEntry<>(result));
                Log.d(TAG, "推荐结果已缓存 (响应时间: " + responseTime + "ms, 变体: " + variant + ")");
                
                if (listener != null) {
                    listener.onRecommendationGenerated(result);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "生成个性化推荐失败", e);
                if (listener != null) {
                    listener.onError("推荐生成失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 构建用户画像 (带缓存)
     */
    private UserProfile buildUserProfileWithCache() {
        String cacheKey = "user_profile";
        
        // 检查缓存
        CacheEntry<UserProfile> cachedProfile = profileCache.get(cacheKey);
        if (cachedProfile != null && !cachedProfile.isExpired()) {
            Log.d(TAG, "使用缓存的用户画像");
            return cachedProfile.data;
        }
        
        // 构建新的用户画像
        UserProfile profile = buildUserProfile();
        
        // 缓存结果
        profileCache.put(cacheKey, new CacheEntry<>(profile));
        Log.d(TAG, "用户画像已缓存");
        
        return profile;
    }
    
    /**
     * 构建用户学习画像
     */
    private UserProfile buildUserProfile() {
        UserProfile profile = new UserProfile();
        
        // 分析词汇掌握情况
        analyzeVocabularyMastery(profile);
        
        // 分析考试成绩
        analyzeExamPerformance(profile);
        
        // 分析学习时长
        analyzeStudyTime(profile);
        
        // 分析学习习惯
        analyzeStudyHabits(profile);
        
        // 确定英语水平
        determineEnglishLevel(profile);
        
        Log.d(TAG, "用户画像: " + profile);
        
        return profile;
    }
    
    /**
     * 应用A/B测试变体
     */
    private String applyABTestVariant() {
        // 简化的A/B测试分配 (基于用户ID哈希)
        String userId = "default_user"; // 在实际应用中应该获取真实用户ID
        int hash = Math.abs(userId.hashCode());
        int variant = hash % 3;
        
        switch (variant) {
            case 0: return "variant_a";
            case 1: return "variant_b";
            case 2: return "variant_c";
            default: return "variant_a";
        }
    }
    
    /**
     * 根据A/B测试变体调整权重
     */
    private void adjustWeightsForVariant(String variant) {
        double multiplier = AB_TEST_VARIANTS.getOrDefault(variant, 1.0);
        
        // 临时调整权重（不影响原始值）
        double tempVocabWeight = vocabularyWeight * multiplier;
        double tempExamWeight = examWeight * multiplier;
        double tempStudyTimeWeight = studyTimeWeight * multiplier;
        double tempRecentActivityWeight = recentActivityWeight * multiplier;
        
        // 标准化权重
        double totalWeight = tempVocabWeight + tempExamWeight + tempStudyTimeWeight + tempRecentActivityWeight;
        
        this.vocabularyWeight = tempVocabWeight / totalWeight;
        this.examWeight = tempExamWeight / totalWeight;
        this.studyTimeWeight = tempStudyTimeWeight / totalWeight;
        this.recentActivityWeight = tempRecentActivityWeight / totalWeight;
        
        Log.d(TAG, String.format("A/B测试权重调整 [%s]: vocab=%.2f, exam=%.2f, time=%.2f, activity=%.2f", 
            variant, vocabularyWeight, examWeight, studyTimeWeight, recentActivityWeight));
    }
    
    /**
     * 生成ML增强的推荐
     */
    private List<StudyPlan> generateMLEnhancedRecommendations(UserProfile profile) {
        if (!isMLEnabled || recommendationModel == null) {
            // 降级到传统推荐
            return generatePersonalizedPlans(profile);
        }
        
        try {
            // 准备特征向量
            double[] features = extractFeaturesFromProfile(profile);
            
            // ML模型预测
            double mlScore = recommendationModel.predict(features);
            
            // 基于ML评分调整推荐策略
            List<StudyPlan> plans = generatePersonalizedPlans(profile);
            
            // ML增强：重新排序和优化计划
            plans = reorderPlansBasedOnMLScore(plans, profile, mlScore);
            
            Log.d(TAG, String.format("ML增强推荐完成，ML评分: %.3f", mlScore));
            return plans;
            
        } catch (Exception e) {
            Log.e(TAG, "ML增强推荐失败，降级到传统推荐", e);
            return generatePersonalizedPlans(profile);
        }
    }
    
    /**
     * 从用户画像中提取特征向量
     */
    private double[] extractFeaturesFromProfile(UserProfile profile) {
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
    private List<StudyPlan> reorderPlansBasedOnMLScore(List<StudyPlan> plans, UserProfile profile, double mlScore) {
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
     * 计算ML增强的置信度
     */
    private int calculateMLEnhancedConfidence(UserProfile profile) {
        int baseConfidence = calculateConfidenceScore(profile);
        
        if (!isMLEnabled || recommendationModel == null) {
            return baseConfidence;
        }
        
        try {
            double[] features = extractFeaturesFromProfile(profile);
            double mlPrediction = recommendationModel.predict(features);
            
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
     * 记录性能指标
     */
    private void recordPerformanceMetrics(RecommendationResult result, long responseTime, String variant) {
        try {
            PerformanceMetrics metrics = new PerformanceMetrics();
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
                cleanupOldMetrics();
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
    private void cleanupOldMetrics() {
        long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L); // 7天前
        
        performanceHistory.entrySet().removeIf(entry -> 
            entry.getValue().timestamp < cutoffTime);
        
        Log.d(TAG, "旧性能指标已清理，剩余: " + performanceHistory.size() + " 条");
    }
    
    /**
     * 触发模型自我学习
     */
    private void triggerModelSelfLearning(UserProfile profile, RecommendationResult result) {
        if (!isMLEnabled || recommendationModel == null) {
            return;
        }
        
        executorService.execute(() -> {
            try {
                // 收集训练数据
                List<double[]> features = new ArrayList<>();
                List<Double> labels = new ArrayList<>();
                
                // 从历史性能数据中构建训练集
                for (PerformanceMetrics metrics : performanceHistory.values()) {
                    if (metrics.accuracy > 0 && metrics.userSatisfaction > 0) {
                        // 使用当前profile作为特征（简化实现）
                        features.add(extractFeaturesFromProfile(profile));
                        
                        // 使用综合评分作为标签
                        labels.add(metrics.getOverallScore());
                    }
                }
                
                if (features.size() >= MIN_SAMPLES_FOR_ML) {
                    // 执行增量学习
                    recommendationModel.train(features, labels, LEARNING_RATE);
                    
                    Log.d(TAG, "模型自我学习完成，训练样本: " + features.size());
                } else {
                    Log.d(TAG, "训练样本不足，跳过自我学习");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "模型自我学习失败", e);
            }
        });
    }
    
    /**
     * 获取ML性能报告
     */
    public MLHelperMethods.ModelPerformanceReport getMLPerformanceReport() {
        return MLHelperMethods.evaluateModelPerformance(performanceHistory);
    }
    
    /**
     * 启用/禁用机器学习
     */
    public void setMLEnabled(boolean enabled) {
        this.isMLEnabled = enabled && (performanceHistory.size() >= MIN_SAMPLES_FOR_ML);
        Log.d(TAG, "ML状态更新: " + (this.isMLEnabled ? "启用" : "禁用"));
    }
    
    /**
     * 手动触发模型重训练
     */
    public void retrainModel(OnModelRetrainListener listener) {
        if (!isMLEnabled || recommendationModel == null) {
            if (listener != null) {
                listener.onError("ML未启用或模型未初始化");
            }
            return;
        }
        
        executorService.execute(() -> {
            try {
                // 构建完整的训练集
                List<double[]> allFeatures = new ArrayList<>();
                List<Double> allLabels = new ArrayList<>();
                
                for (PerformanceMetrics metrics : performanceHistory.values()) {
                    // 这里需要真实的用户画像数据，简化实现使用默认值
                    UserProfile defaultProfile = new UserProfile();
                    allFeatures.add(extractFeaturesFromProfile(defaultProfile));
                    allLabels.add(metrics.getOverallScore());
                }
                
                if (allFeatures.size() >= MIN_SAMPLES_FOR_ML) {
                    // 重新初始化模型
                    recommendationModel = new SimpleMLModel(5);
                    
                    // 完整训练
                    recommendationModel.train(allFeatures, allLabels, LEARNING_RATE);
                    
                    if (listener != null) {
                        listener.onRetrainCompleted(allFeatures.size());
                    }
                    
                    Log.d(TAG, "模型重训练完成，样本数: " + allFeatures.size());
                } else {
                    if (listener != null) {
                        listener.onError("训练样本不足: " + allFeatures.size());
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "模型重训练失败", e);
                if (listener != null) {
                    listener.onError("重训练失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 模型重训练回调接口
     */
    public interface OnModelRetrainListener {
        void onRetrainCompleted(int sampleCount);
        void onError(String error);
    }
    
    /**
     * 分析词汇掌握情况
     */
    private void analyzeVocabularyMastery(UserProfile profile) {
        try {
            List<VocabularyRecordEntity> vocabularyRecords = 
                database.vocabularyDao().getAllVocabulary();
            
            if (vocabularyRecords.isEmpty()) {
                profile.vocabularyMasteryLevel = 0;
                return;
            }
            
            int totalWords = vocabularyRecords.size();
            int masteredWords = database.vocabularyDao().getMasteredVocabularyCount();
            
            profile.vocabularyMasteryLevel = (masteredWords * 100) / totalWords;
            
        } catch (Exception e) {
            Log.e(TAG, "分析词汇掌握情况失败", e);
            profile.vocabularyMasteryLevel = 0;
        }
    }
    
    /**
     * 分析考试成绩
     */
    private void analyzeExamPerformance(UserProfile profile) {
        try {
            // 简化实现：使用模拟数据或默认值
            // 在实际应用中，这里应该调用真实的考试记录数据
            profile.averageExamScore = 75.0; // 默认成绩
            
            // 设置默认的薄弱和强势类别
            profile.weakCategories.add("听力");
            profile.weakCategories.add("语法");
            profile.strongCategories.add("词汇");
            profile.strongCategories.add("阅读");
            
        } catch (Exception e) {
            Log.e(TAG, "分析考试成绩失败", e);
            profile.averageExamScore = 0.0;
        }
    }
    
    /**
     * 分析学习时长
     */
    private void analyzeStudyTime(UserProfile profile) {
        try {
            // 获取最近30天的学习记录
            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            List<StudyRecordEntity> recentRecords = 
                database.studyRecordDao().getRecordsSince(thirtyDaysAgo);
            
            if (recentRecords.isEmpty()) {
                profile.dailyStudyMinutes = 0;
                profile.continuousStudyDays = 0;
                return;
            }
            
            // 计算日均学习时长
            Map<String, Integer> dailyMinutes = new HashMap<>();
            for (StudyRecordEntity record : recentRecords) {
                String date = getDateString(record.getStudyDate().getTime()); // 使用studyDate而不是createdTime
                dailyMinutes.put(date, 
                    dailyMinutes.getOrDefault(date, 0) + (int)(record.getResponseTime() / 60000));
            }
            
            int totalMinutes = 0;
            for (int minutes : dailyMinutes.values()) {
                totalMinutes += minutes;
            }
            
            profile.dailyStudyMinutes = totalMinutes / Math.max(dailyMinutes.size(), 1);
            
            // 计算连续学习天数
            profile.continuousStudyDays = calculateContinuousStudyDays(recentRecords);
            
        } catch (Exception e) {
            Log.e(TAG, "分析学习时长失败", e);
            profile.dailyStudyMinutes = 30; // 设置默认值
            profile.continuousStudyDays = 3;
        }
    }
    
    /**
     * 分析学习习惯
     */
    private void analyzeStudyHabits(UserProfile profile) {
        try {
            // 分析偏好学习时间段
            profile.preferredStudyTime = analyzePreferredStudyTime();
            
        } catch (Exception e) {
            Log.e(TAG, "分析学习习惯失败", e);
            profile.preferredStudyTime = "晚上";
        }
    }
    
    /**
     * 确定英语水平
     */
    private void determineEnglishLevel(UserProfile profile) {
        // 基于词汇掌握度和考试成绩确定水平
        if (profile.vocabularyMasteryLevel >= 80 && profile.averageExamScore >= 85) {
            profile.currentLevel = "高级";
        } else if (profile.vocabularyMasteryLevel >= 60 && profile.averageExamScore >= 70) {
            profile.currentLevel = "中级";
        } else if (profile.vocabularyMasteryLevel >= 40 && profile.averageExamScore >= 60) {
            profile.currentLevel = "初级";
        } else {
            profile.currentLevel = "基础";
        }
    }
    
    /**
     * 基于用户画像生成个性化计划
     */
    private List<StudyPlan> generatePersonalizedPlans(UserProfile profile) {
        List<StudyPlan> plans = new ArrayList<>();
        
        // 根据薄弱环节生成重点计划
        for (String weakCategory : profile.weakCategories) {
            StudyPlan plan = createWeaknessFocusedPlan(weakCategory, profile);
            if (plan != null) {
                plans.add(plan);
            }
        }
        
        // 如果没有明显薄弱环节，生成全面提升计划
        if (plans.isEmpty()) {
            plans.add(createComprehensiveImprovementPlan(profile));
        }
        
        // 根据学习时长调整计划强度
        adjustPlanIntensity(plans, profile);
        
        return plans;
    }
    
    /**
     * 创建针对薄弱环节的计划
     */
    private StudyPlan createWeaknessFocusedPlan(String category, UserProfile profile) {
        String title = String.format("%s专项提升计划", category);
        String description = String.format(
            "基于您的学习数据分析，%s是您的薄弱环节。本计划将通过系统化训练帮助您在此方面取得突破。\n\n" +
            "计划特点：\n" +
            "• 针对性强化训练\n" +
            "• 循序渐进的难度设置\n" +
            "• 定期测试和反馈\n" +
            "• 个性化练习推荐",
            category
        );
        
        String timeRange = calculateOptimalTimeRange(profile);
        String duration = calculateOptimalDuration(profile, category);
        String priority = determineWeaknessPriority(category, profile);
        
        return new StudyPlan(title, category, description, timeRange, duration, priority);
    }
    
    /**
     * 创建全面提升计划
     */
    private StudyPlan createComprehensiveImprovementPlan(UserProfile profile) {
        String level = profile.currentLevel;
        String title = String.format("%s英语全面提升计划", level);
        
        String description = String.format(
            "基于您当前的%s水平，为您制定全面的英语提升计划。\n\n" +
            "计划包含：\n" +
            "• 词汇积累与巩固\n" +
            "• 语法系统学习\n" +
            "• 听说读写综合训练\n" +
            "• 定期能力测评\n\n" +
            "您的学习特点：\n" +
            "• 词汇掌握度：%d%%\n" +
            "• 平均成绩：%.1f分\n" +
            "• 日均学习：%d分钟",
            level, profile.vocabularyMasteryLevel, 
            profile.averageExamScore, profile.dailyStudyMinutes
        );
        
        String timeRange = calculateOptimalTimeRange(profile);
        String duration = calculateOptimalDuration(profile, "综合");
        
        return new StudyPlan(title, "综合", description, timeRange, duration, "高");
    }
    
    /**
     * 计算推荐置信度
     */
    private int calculateConfidenceScore(UserProfile profile) {
        int confidence = 0;
        
        // 基于数据丰富程度计算置信度
        if (profile.vocabularyMasteryLevel > 0) confidence += 25;
        if (profile.averageExamScore > 0) confidence += 30;
        if (profile.dailyStudyMinutes > 0) confidence += 25;
        if (!profile.weakCategories.isEmpty()) confidence += 20;
        
        return Math.min(confidence, 100);
    }
    
    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(UserProfile profile) {
        StringBuilder reason = new StringBuilder();
        reason.append("基于您的学习数据分析：\n\n");
        
        // 词汇水平分析
        if (profile.vocabularyMasteryLevel > 0) {
            reason.append(String.format("📚 词汇掌握度：%d%%", profile.vocabularyMasteryLevel));
            if (profile.vocabularyMasteryLevel < 60) {
                reason.append("，建议加强词汇积累\n");
            } else {
                reason.append("，词汇基础较好\n");
            }
        }
        
        // 考试成绩分析
        if (profile.averageExamScore > 0) {
            reason.append(String.format("📊 平均成绩：%.1f分", profile.averageExamScore));
            if (profile.averageExamScore < 70) {
                reason.append("，有很大提升空间\n");
            } else {
                reason.append("，成绩表现良好\n");
            }
        }
        
        // 薄弱环节分析
        if (!profile.weakCategories.isEmpty()) {
            reason.append("⚠️ 薄弱环节：").append(String.join("、", profile.weakCategories)).append("\n");
        }
        
        // 学习习惯分析
        if (profile.dailyStudyMinutes > 0) {
            reason.append(String.format("⏰ 日均学习：%d分钟", profile.dailyStudyMinutes));
            if (profile.dailyStudyMinutes < 30) {
                reason.append("，建议增加学习时长\n");
            } else {
                reason.append("，学习时间安排合理\n");
            }
        }
        
        reason.append("\n💡 因此为您推荐了以上个性化学习计划");
        
        return reason.toString();
    }
    
    // ==================== 辅助方法 ====================
    
    private void analyzeExamErrors(ExamRecordEntity record, Map<String, Integer> categoryErrors) {
        // 这里可以根据考试类型分析错题分布
        String examType = record.getExamType();
        int errorCount = record.getTotalQuestions() - record.getCorrectAnswers();
        
        // 简化处理：根据考试类型推测薄弱环节
        if (errorCount > record.getTotalQuestions() * 0.3) {
            categoryErrors.put(mapExamTypeToCategory(examType), 
                categoryErrors.getOrDefault(mapExamTypeToCategory(examType), 0) + errorCount);
        }
    }
    
    private String mapExamTypeToCategory(String examType) {
        if (examType.contains("词汇") || examType.contains("vocabulary")) {
            return "词汇";
        } else if (examType.contains("语法") || examType.contains("grammar")) {
            return "语法";
        } else if (examType.contains("阅读") || examType.contains("reading")) {
            return "阅读";
        } else if (examType.contains("听力") || examType.contains("listening")) {
            return "听力";
        } else if (examType.contains("写作") || examType.contains("writing")) {
            return "写作";
        } else {
            return "综合";
        }
    }
    
    private void determineWeakAndStrongCategories(UserProfile profile, Map<String, Integer> categoryErrors) {
        // 按错误数量排序，错误多的是薄弱环节
        categoryErrors.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(2)
            .forEach(entry -> profile.weakCategories.add(entry.getKey()));
        
        // 简化处理：没有出现在薄弱环节的认为是强势项目
        String[] allCategories = {"词汇", "语法", "阅读", "听力", "写作"};
        for (String category : allCategories) {
            if (!profile.weakCategories.contains(category)) {
                profile.strongCategories.add(category);
            }
        }
    }
    
    private int calculateContinuousStudyDays(List<StudyRecordEntity> records) {
        if (records.isEmpty()) return 0;
        
        // 简化实现：基于记录数量和时间跨度估算连续天数
        long now = System.currentTimeMillis();
        long oneDayMs = 24 * 60 * 60 * 1000L;
        
        int continuousDays = 0;
        long currentDay = now;
        
        // 检查最近7天是否有学习记录
        for (int i = 0; i < 7; i++) {
            boolean hasStudyOnDay = false;
            long dayStart = currentDay - oneDayMs;
            
            for (StudyRecordEntity record : records) {
                long recordTime = record.getStudyDate().getTime();
                if (recordTime >= dayStart && recordTime <= currentDay) {
                    hasStudyOnDay = true;
                    break;
                }
            }
            
            if (hasStudyOnDay) {
                continuousDays++;
                currentDay = dayStart;
            } else {
                break; // 中断连续学习
            }
        }
        
        return continuousDays;
    }
    
    private String analyzePreferredStudyTime() {
        // 简化实现：默认返回晚上
        return "晚上";
    }
    
    private String getDateString(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return String.format("%04d-%02d-%02d", 
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    private String calculateOptimalTimeRange(UserProfile profile) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        
        // 根据用户水平确定学习周期
        int months = 3; // 默认3个月
        if ("基础".equals(profile.currentLevel)) {
            months = 6;
        } else if ("高级".equals(profile.currentLevel)) {
            months = 2;
        }
        
        calendar.add(Calendar.MONTH, months);
        int futureYear = calendar.get(Calendar.YEAR);
        int futureMonth = calendar.get(Calendar.MONTH) + 1;
        
        return String.format("%d-%02d至%d-%02d", 
            currentYear, currentMonth, futureYear, futureMonth);
    }
    
    private String calculateOptimalDuration(UserProfile profile, String category) {
        int baseDuration = 30; // 基础时长30分钟
        
        // 根据用户当前学习时长调整
        if (profile.dailyStudyMinutes > 60) {
            baseDuration = 45;
        } else if (profile.dailyStudyMinutes < 20) {
            baseDuration = 20;
        }
        
        // 根据类别调整
        if ("词汇".equals(category)) {
            baseDuration = Math.max(20, baseDuration - 10);
        } else if ("综合".equals(category)) {
            baseDuration = Math.min(90, baseDuration + 20);
        }
        
        return baseDuration + "分钟/天";
    }
    
    private String determineWeaknessPriority(String category, UserProfile profile) {
        // 词汇是基础，优先级最高
        if ("词汇".equals(category) && profile.vocabularyMasteryLevel < 50) {
            return "高";
        }
        
        // 其他薄弱环节中等优先级
        return "中";
    }
    
    private void adjustPlanIntensity(List<StudyPlan> plans, UserProfile profile) {
        // 根据用户学习习惯调整计划强度
        for (StudyPlan plan : plans) {
            String originalDuration = plan.getDuration();
            
            // 如果用户学习时间较少，降低强度
            if (profile.dailyStudyMinutes < 20) {
                plan.setDuration(reduceDuration(originalDuration));
            }
            // 如果用户学习时间充足，可以适当增加强度
            else if (profile.dailyStudyMinutes > 60) {
                plan.setDuration(increaseDuration(originalDuration));
            }
        }
    }
    
    private String reduceDuration(String originalDuration) {
        // 简化实现：减少20%的时长
        if (originalDuration.contains("分钟")) {
            try {
                int minutes = Integer.parseInt(originalDuration.replaceAll("[^0-9]", ""));
                int reducedMinutes = (int) (minutes * 0.8);
                return reducedMinutes + "分钟/天";
            } catch (NumberFormatException e) {
                return originalDuration;
            }
        }
        return originalDuration;
    }
    
    private String increaseDuration(String originalDuration) {
        // 简化实现：增加20%的时长
        if (originalDuration.contains("分钟")) {
            try {
                int minutes = Integer.parseInt(originalDuration.replaceAll("[^0-9]", ""));
                int increasedMinutes = (int) (minutes * 1.2);
                return increasedMinutes + "分钟/天";
            } catch (NumberFormatException e) {
                return originalDuration;
            }
        }
        return originalDuration;
    }
    
    /**
     * 推荐回调接口
     */
    public interface OnRecommendationListener {
        void onRecommendationGenerated(RecommendationResult result);
        void onError(String error);
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        profileCache.clear();
        recommendationCache.clear();
        Log.d(TAG, "缓存已清除");
    }
    
    /**
     * 清理过期缓存
     */
    public void cleanupExpiredCache() {
        // 清理过期的用户画像缓存
        profileCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        // 清理过期的推荐结果缓存
        recommendationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        Log.d(TAG, "过期缓存已清理");
    }
    
    /**
     * 强制刷新用户画像
     */
    public void refreshUserProfile(OnProfileRefreshListener listener) {
        // 清除画像缓存
        profileCache.remove("user_profile");
        
        executorService.execute(() -> {
            try {
                UserProfile profile = buildUserProfile();
                profileCache.put("user_profile", new CacheEntry<>(profile));
                
                if (listener != null) {
                    listener.onProfileRefreshed(profile);
                }
                
                Log.d(TAG, "用户画像已刷新");
            } catch (Exception e) {
                Log.e(TAG, "刷新用户画像失败", e);
                if (listener != null) {
                    listener.onError("刷新失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        stats.profileCacheSize = profileCache.size();
        stats.recommendationCacheSize = recommendationCache.size();
        
        // 计算命中率（简化实现）
        stats.hitRate = 0.85; // 示例值
        
        return stats;
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        public int profileCacheSize;
        public int recommendationCacheSize;
        public double hitRate;
        
        @Override
        public String toString() {
            return String.format("缓存统计: 画像缓存=%d, 推荐缓存=%d, 命中率=%.2f%%", 
                profileCacheSize, recommendationCacheSize, hitRate * 100);
        }
    }
    
    /**
     * 用户画像刷新回调接口
     */
    public interface OnProfileRefreshListener {
        void onProfileRefreshed(UserProfile profile);
        void onError(String error);
    }
    
    /**
     * 释放资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // 清理缓存
        clearCache();
        
        Log.d(TAG, "个性化推荐引擎已关闭");
    }
}
