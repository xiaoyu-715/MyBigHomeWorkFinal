package com.example.mybighomework.utils;

import android.content.Context;
import android.util.Log;

import com.example.mybighomework.StudyPlan;

import java.util.List;

/**
 * 学习计划优化功能集成器
 * 统一管理和协调各个优化组件
 */
public class StudyPlanOptimizationIntegrator {
    
    private static final String TAG = "StudyPlanOptimizer";
    
    private Context context;
    private PersonalizedRecommendationEngine recommendationEngine;
    private StudyPlanTemplateManager templateManager;
    private StudyPlanTracker tracker;
    private SmartReminderManager reminderManager;
    
    public StudyPlanOptimizationIntegrator(Context context) {
        this.context = context;
        initializeComponents();
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
            
            Log.d(TAG, "学习计划优化系统初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "初始化优化系统失败", e);
        }
    }
    
    /**
     * 获取个性化推荐
     */
    public void getPersonalizedRecommendations(OnOptimizationResultListener listener) {
        if (recommendationEngine == null) {
            if (listener != null) {
                listener.onError("推荐引擎未初始化");
            }
            return;
        }
        
        recommendationEngine.generateRecommendations(new PersonalizedRecommendationEngine.OnRecommendationListener() {
            @Override
            public void onRecommendationGenerated(PersonalizedRecommendationEngine.RecommendationResult result) {
                if (listener != null) {
                    listener.onPersonalizedRecommendation(result);
                }
            }
            
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError("个性化推荐失败: " + error);
                }
            }
        });
    }
    
    /**
     * 获取所有可用模板
     */
    public List<StudyPlanTemplateManager.StudyPlanTemplate> getAllTemplates() {
        if (templateManager == null) {
            Log.e(TAG, "模板管理器未初始化");
            return null;
        }
        
        return templateManager.getAllTemplates();
    }
    
    /**
     * 应用模板
     */
    public List<StudyPlan> applyTemplate(String templateId) {
        if (templateManager == null) {
            Log.e(TAG, "模板管理器未初始化");
            return null;
        }
        
        StudyPlanTemplateManager.StudyPlanTemplate template = 
            templateManager.getTemplateById(templateId);
        
        if (template != null) {
            return templateManager.applyTemplate(template);
        }
        
        return null;
    }
    
    /**
     * 获取进度统计
     */
    public void getProgressStats(OnProgressStatsListener listener) {
        if (tracker == null) {
            if (listener != null) {
                listener.onError("进度跟踪器未初始化");
            }
            return;
        }
        
        tracker.getProgressStats(new StudyPlanTracker.OnProgressStatsListener() {
            @Override
            public void onStatsCalculated(StudyPlanTracker.ProgressStats stats) {
                if (listener != null) {
                    listener.onProgressStats(stats);
                }
            }
            
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError("进度统计失败: " + error);
                }
            }
        });
    }
    
    /**
     * 启用智能提醒
     */
    public void enableSmartReminders() {
        if (reminderManager == null) {
            Log.e(TAG, "提醒管理器未初始化");
            return;
        }
        
        SmartReminderManager.ReminderConfig config = new SmartReminderManager.ReminderConfig();
        config.enabled = true;
        config.adaptiveEnabled = true;
        config.frequency = SmartReminderManager.ReminderFrequency.SMART;
        
        reminderManager.enableSmartReminder(config);
        Log.d(TAG, "智能提醒已启用");
    }
    
    /**
     * 禁用智能提醒
     */
    public void disableSmartReminders() {
        if (reminderManager != null) {
            reminderManager.cancelAllReminders();
            Log.d(TAG, "智能提醒已禁用");
        }
    }
    
    /**
     * 分析学习习惯
     */
    public void analyzeStudyHabits(OnStudyHabitsListener listener) {
        if (reminderManager == null) {
            if (listener != null) {
                listener.onError("提醒管理器未初始化");
            }
            return;
        }
        
        reminderManager.analyzeStudyHabits(new SmartReminderManager.OnHabitsAnalyzedListener() {
            @Override
            public void onHabitsAnalyzed(SmartReminderManager.StudyHabits habits) {
                if (listener != null) {
                    listener.onHabitsAnalyzed(habits);
                }
            }
            
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError("习惯分析失败: " + error);
                }
            }
        });
    }
    
    /**
     * 更新计划进度
     */
    public void updatePlanProgress(int planId, int progress) {
        if (tracker != null) {
            tracker.updatePlanProgress(planId, progress, new StudyPlanTracker.OnProgressUpdateListener() {
                @Override
                public void onProgressUpdated(int planId, int newProgress) {
                    Log.d(TAG, "计划 " + planId + " 进度已更新为 " + newProgress + "%");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "更新进度失败: " + error);
                }
            });
        }
    }
    
    /**
     * 记录学习活动
     */
    public void recordStudyActivity(int planId, String activityType, int durationMinutes) {
        if (tracker != null) {
            tracker.recordStudyActivity(planId, activityType, durationMinutes);
            Log.d(TAG, "学习活动已记录: " + activityType + " " + durationMinutes + "分钟");
        }
    }
    
    /**
     * 获取系统状态报告
     */
    public SystemStatusReport getSystemStatus() {
        SystemStatusReport report = new SystemStatusReport();
        
        report.recommendationEngineStatus = (recommendationEngine != null) ? "正常" : "未初始化";
        report.templateManagerStatus = (templateManager != null) ? "正常" : "未初始化";
        report.trackerStatus = (tracker != null) ? "正常" : "未初始化";
        report.reminderManagerStatus = (reminderManager != null) ? "正常" : "未初始化";
        
        if (templateManager != null) {
            report.totalTemplates = templateManager.getAllTemplates().size();
        }
        
        return report;
    }
    
    /**
     * 释放所有资源
     */
    public void shutdown() {
        try {
            if (recommendationEngine != null) {
                recommendationEngine.shutdown();
            }
            if (tracker != null) {
                tracker.shutdown();
            }
            if (reminderManager != null) {
                reminderManager.shutdown();
            }
            
            Log.d(TAG, "学习计划优化系统已关闭");
        } catch (Exception e) {
            Log.e(TAG, "关闭优化系统时发生错误", e);
        }
    }
    
    // ==================== 回调接口 ====================
    
    public interface OnOptimizationResultListener {
        void onPersonalizedRecommendation(PersonalizedRecommendationEngine.RecommendationResult result);
        void onError(String error);
    }
    
    public interface OnProgressStatsListener {
        void onProgressStats(StudyPlanTracker.ProgressStats stats);
        void onError(String error);
    }
    
    public interface OnStudyHabitsListener {
        void onHabitsAnalyzed(SmartReminderManager.StudyHabits habits);
        void onError(String error);
    }
    
    /**
     * 系统状态报告
     */
    public static class SystemStatusReport {
        public String recommendationEngineStatus;
        public String templateManagerStatus;
        public String trackerStatus;
        public String reminderManagerStatus;
        public int totalTemplates;
        
        @Override
        public String toString() {
            return "SystemStatusReport{" +
                    "推荐引擎='" + recommendationEngineStatus + '\'' +
                    ", 模板管理='" + templateManagerStatus + '\'' +
                    ", 进度跟踪='" + trackerStatus + '\'' +
                    ", 智能提醒='" + reminderManagerStatus + '\'' +
                    ", 模板数量=" + totalTemplates +
                    '}';
        }
    }
    
    /**
     * 检查系统健康状态
     */
    public boolean isSystemHealthy() {
        return recommendationEngine != null && 
               templateManager != null && 
               tracker != null && 
               reminderManager != null;
    }
    
    /**
     * 获取版本信息
     */
    public String getVersionInfo() {
        return "学习计划优化系统 v2.0\n" +
               "- 个性化推荐引擎\n" +
               "- 智能模板系统\n" +
               "- 进度跟踪系统\n" +
               "- 智能提醒系统\n" +
               "- AI对话分析优化";
    }
}
