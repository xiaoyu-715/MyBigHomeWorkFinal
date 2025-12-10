package com.example.mybighomework.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mybighomework.StudyPlan;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 增强版学习计划优化器
 * 集成所有优化功能并提供高级管理能力
 */
public class EnhancedStudyPlanOptimizer {
    
    private static final String TAG = "EnhancedOptimizer";
    private static final String PREF_NAME = "enhanced_optimizer_prefs";
    
    private Context context;
    private PersonalizedRecommendationEngine recommendationEngine;
    private StudyPlanTemplateManager templateManager;
    private StudyPlanTracker tracker;
    private SmartReminderManager reminderManager;
    private SharedPreferences preferences;
    private ScheduledExecutorService scheduledExecutor;
    
    // 优化器配置
    private boolean autoCleanupEnabled = true;
    private boolean adaptiveLearningEnabled = true;
    private boolean performanceMonitoringEnabled = true;
    
    public EnhancedStudyPlanOptimizer(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.scheduledExecutor = Executors.newScheduledThreadPool(2);
        
        initializeComponents();
        setupAutomaticTasks();
    }
    
    /**
     * 初始化所有组件
     */
    private void initializeComponents() {
        try {
            this.recommendationEngine = new PersonalizedRecommendationEngine(context);
            this.templateManager = new StudyPlanTemplateManager(context);
            this.tracker = new StudyPlanTracker(context);
            this.reminderManager = new SmartReminderManager(context);
            
            Log.d(TAG, "增强版优化器初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "初始化增强版优化器失败", e);
        }
    }
    
    /**
     * 设置自动任务
     */
    private void setupAutomaticTasks() {
        if (autoCleanupEnabled) {
            // 每小时清理过期缓存
            scheduledExecutor.scheduleAtFixedRate(() -> {
                if (recommendationEngine != null) {
                    recommendationEngine.cleanupExpiredCache();
                }
            }, 1, 1, TimeUnit.HOURS);
        }
        
        if (performanceMonitoringEnabled) {
            // 每30分钟记录性能统计
            scheduledExecutor.scheduleAtFixedRate(() -> {
                logPerformanceStats();
            }, 30, 30, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 智能推荐生成 (增强版)
     */
    public void generateSmartRecommendations(OnSmartRecommendationListener listener) {
        if (recommendationEngine == null) {
            if (listener != null) {
                listener.onError("推荐引擎未初始化");
            }
            return;
        }
        
        Log.d(TAG, "开始生成智能推荐");
        
        recommendationEngine.generateRecommendations(new PersonalizedRecommendationEngine.OnRecommendationListener() {
            @Override
            public void onRecommendationGenerated(PersonalizedRecommendationEngine.RecommendationResult result) {
                // 增强推荐结果
                SmartRecommendationResult enhancedResult = enhanceRecommendationResult(result);
                
                if (listener != null) {
                    listener.onSmartRecommendation(enhancedResult);
                }
                
                // 记录推荐事件
                recordRecommendationEvent(enhancedResult);
                
                Log.d(TAG, "智能推荐生成完成，置信度: " + result.confidenceScore + "%");
            }
            
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError("智能推荐失败: " + error);
                }
            }
        });
    }
    
    /**
     * 增强推荐结果
     */
    private SmartRecommendationResult enhanceRecommendationResult(PersonalizedRecommendationEngine.RecommendationResult result) {
        SmartRecommendationResult enhanced = new SmartRecommendationResult();
        enhanced.originalResult = result;
        enhanced.userProfile = result.userProfile;
        enhanced.recommendedPlans = result.recommendedPlans;
        enhanced.confidenceScore = result.confidenceScore;
        enhanced.recommendationReason = result.recommendationReason;
        
        // 添加学习者类型分析
        enhanced.learnerType = result.userProfile.getLearnerType();
        enhanced.recommendedIntensity = result.userProfile.getRecommendedIntensity();
        
        // 添加模板匹配建议
        enhanced.suggestedTemplates = findMatchingTemplates(result.userProfile);
        
        // 添加学习路径建议
        enhanced.learningPath = generateLearningPath(result.userProfile);
        
        // 添加时间安排建议
        enhanced.timeSchedule = generateOptimalSchedule(result.userProfile);
        
        return enhanced;
    }
    
    /**
     * 查找匹配的模板
     */
    private List<StudyPlanTemplateManager.StudyPlanTemplate> findMatchingTemplates(PersonalizedRecommendationEngine.UserProfile profile) {
        if (templateManager == null) return null;
        
        List<StudyPlanTemplateManager.StudyPlanTemplate> allTemplates = templateManager.getAllTemplates();
        
        // 根据用户画像筛选合适的模板
        return allTemplates.stream()
            .filter(template -> isTemplateSuitableForProfile(template, profile))
            .limit(3) // 最多推荐3个模板
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 判断模板是否适合用户画像
     */
    private boolean isTemplateSuitableForProfile(StudyPlanTemplateManager.StudyPlanTemplate template, 
                                               PersonalizedRecommendationEngine.UserProfile profile) {
        // 简化的匹配逻辑
        if (profile.currentLevel != null) {
            if (profile.currentLevel.contains("基础") && template.getName().contains("基础")) {
                return true;
            }
            if (profile.currentLevel.contains("高级") && template.getName().contains("高级")) {
                return true;
            }
        }
        
        // 根据目标考试匹配
        if (profile.targetExam != null) {
            return template.getName().toLowerCase().contains(profile.targetExam.toLowerCase());
        }
        
        return true; // 默认匹配
    }
    
    /**
     * 生成学习路径
     */
    private String generateLearningPath(PersonalizedRecommendationEngine.UserProfile profile) {
        StringBuilder path = new StringBuilder();
        path.append("🛤️ 推荐学习路径：\n\n");
        
        // 基于薄弱环节制定路径
        if (!profile.weakCategories.isEmpty()) {
            path.append("1️⃣ 薄弱环节突破阶段\n");
            path.append("   重点：").append(String.join("、", profile.weakCategories)).append("\n");
            path.append("   预计时长：4-6周\n\n");
        }
        
        // 综合提升阶段
        path.append("2️⃣ 综合能力提升阶段\n");
        path.append("   重点：全面发展各项技能\n");
        path.append("   预计时长：8-12周\n\n");
        
        // 强化巩固阶段
        path.append("3️⃣ 强化巩固阶段\n");
        path.append("   重点：查漏补缺，冲刺提高\n");
        path.append("   预计时长：2-4周");
        
        return path.toString();
    }
    
    /**
     * 生成最优时间安排
     */
    private String generateOptimalSchedule(PersonalizedRecommendationEngine.UserProfile profile) {
        StringBuilder schedule = new StringBuilder();
        schedule.append("⏰ 最优时间安排：\n\n");
        
        // 基于用户偏好时间
        String preferredTime = profile.preferredStudyTime != null ? profile.preferredStudyTime : "晚上";
        schedule.append("🕐 最佳学习时间：").append(preferredTime).append("\n");
        
        // 基于学习强度
        String intensity = profile.getRecommendedIntensity();
        int dailyMinutes = profile.dailyStudyMinutes > 0 ? profile.dailyStudyMinutes : 45;
        
        schedule.append("📊 建议学习强度：").append(intensity).append("\n");
        schedule.append("⏱️ 每日学习时长：").append(dailyMinutes).append("分钟\n");
        
        // 学习频率建议
        if (profile.consistencyScore > 0.8) {
            schedule.append("📅 学习频率：每天（您的一致性很好！）\n");
        } else if (profile.consistencyScore > 0.6) {
            schedule.append("📅 学习频率：每周5-6天\n");
        } else {
            schedule.append("📅 学习频率：每周3-4天（循序渐进）\n");
        }
        
        return schedule.toString();
    }
    
    /**
     * 记录推荐事件
     */
    private void recordRecommendationEvent(SmartRecommendationResult result) {
        long timestamp = System.currentTimeMillis();
        
        preferences.edit()
            .putLong("last_recommendation_time", timestamp)
            .putInt("last_confidence_score", result.confidenceScore)
            .putString("last_learner_type", result.learnerType)
            .apply();
        
        Log.d(TAG, "推荐事件已记录");
    }
    
    /**
     * 记录性能统计
     */
    private void logPerformanceStats() {
        try {
            if (recommendationEngine != null) {
                PersonalizedRecommendationEngine.CacheStats cacheStats = 
                    recommendationEngine.getCacheStats();
                Log.d(TAG, "性能统计: " + cacheStats.toString());
            }
            
            // 记录内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
            long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
            long usedMemory = totalMemory - freeMemory;
            
            Log.d(TAG, String.format("内存使用: %dMB/%dMB (%.1f%%)", 
                usedMemory, totalMemory, (usedMemory * 100.0) / totalMemory));
                
        } catch (Exception e) {
            Log.e(TAG, "记录性能统计失败", e);
        }
    }
    
    /**
     * 获取系统健康报告
     */
    public SystemHealthReport getHealthReport() {
        SystemHealthReport report = new SystemHealthReport();
        report.timestamp = System.currentTimeMillis();
        
        // 组件状态
        report.recommendationEngineHealth = (recommendationEngine != null) ? "健康" : "异常";
        report.templateManagerHealth = (templateManager != null) ? "健康" : "异常";
        report.trackerHealth = (tracker != null) ? "健康" : "异常";
        report.reminderManagerHealth = (reminderManager != null) ? "健康" : "异常";
        
        // 缓存状态
        if (recommendationEngine != null) {
            PersonalizedRecommendationEngine.CacheStats stats = recommendationEngine.getCacheStats();
            report.cacheHitRate = stats.hitRate;
            report.cacheSize = stats.profileCacheSize + stats.recommendationCacheSize;
        }
        
        // 上次推荐时间
        report.lastRecommendationTime = preferences.getLong("last_recommendation_time", 0);
        
        // 计算整体健康分数
        int healthyComponents = 0;
        if (recommendationEngine != null) healthyComponents++;
        if (templateManager != null) healthyComponents++;
        if (tracker != null) healthyComponents++;
        if (reminderManager != null) healthyComponents++;
        
        report.overallHealthScore = (healthyComponents * 100) / 4;
        
        return report;
    }
    
    /**
     * 智能推荐结果 (增强版)
     */
    public static class SmartRecommendationResult {
        public PersonalizedRecommendationEngine.RecommendationResult originalResult;
        public PersonalizedRecommendationEngine.UserProfile userProfile;
        public List<StudyPlan> recommendedPlans;
        public String recommendationReason;
        public int confidenceScore;
        
        // 增强字段
        public String learnerType;
        public String recommendedIntensity;
        public List<StudyPlanTemplateManager.StudyPlanTemplate> suggestedTemplates;
        public String learningPath;
        public String timeSchedule;
    }
    
    /**
     * 系统健康报告
     */
    public static class SystemHealthReport {
        public long timestamp;
        public String recommendationEngineHealth;
        public String templateManagerHealth;
        public String trackerHealth;
        public String reminderManagerHealth;
        public double cacheHitRate;
        public int cacheSize;
        public long lastRecommendationTime;
        public int overallHealthScore;
        
        @Override
        public String toString() {
            return String.format("系统健康报告 [%s]\n" +
                "推荐引擎: %s\n" +
                "模板管理: %s\n" +
                "进度跟踪: %s\n" +
                "智能提醒: %s\n" +
                "缓存命中率: %.1f%%\n" +
                "整体健康分数: %d/100",
                new java.util.Date(timestamp),
                recommendationEngineHealth, templateManagerHealth,
                trackerHealth, reminderManagerHealth,
                cacheHitRate * 100, overallHealthScore);
        }
    }
    
    /**
     * 智能推荐监听器
     */
    public interface OnSmartRecommendationListener {
        void onSmartRecommendation(SmartRecommendationResult result);
        void onError(String error);
    }
    
    /**
     * 强制刷新所有缓存和数据
     */
    public void forceRefreshAll(OnRefreshCompleteListener listener) {
        Log.d(TAG, "开始强制刷新所有数据");
        
        scheduledExecutor.execute(() -> {
            try {
                // 清除所有缓存
                if (recommendationEngine != null) {
                    recommendationEngine.clearCache();
                }
                
                // 重新分析用户画像
                if (recommendationEngine != null) {
                    recommendationEngine.refreshUserProfile(new PersonalizedRecommendationEngine.OnProfileRefreshListener() {
                        @Override
                        public void onProfileRefreshed(PersonalizedRecommendationEngine.UserProfile profile) {
                            if (listener != null) {
                                listener.onRefreshComplete("数据刷新完成");
                            }
                        }
                        
                        @Override
                        public void onError(String error) {
                            if (listener != null) {
                                listener.onRefreshError("刷新失败: " + error);
                            }
                        }
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "强制刷新失败", e);
                if (listener != null) {
                    listener.onRefreshError("刷新异常: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 刷新完成监听器
     */
    public interface OnRefreshCompleteListener {
        void onRefreshComplete(String message);
        void onRefreshError(String error);
    }
    
    /**
     * 关闭优化器
     */
    public void shutdown() {
        try {
            // 关闭定时任务
            if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
                scheduledExecutor.shutdown();
            }
            
            // 关闭各个组件
            if (recommendationEngine != null) {
                recommendationEngine.shutdown();
            }
            if (tracker != null) {
                tracker.shutdown();
            }
            if (reminderManager != null) {
                reminderManager.shutdown();
            }
            
            Log.d(TAG, "增强版优化器已关闭");
        } catch (Exception e) {
            Log.e(TAG, "关闭优化器时发生错误", e);
        }
    }
}
