package com.example.mybighomework.autoglm.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mybighomework.AIChatActivity;
import com.example.mybighomework.ExamListActivity;
import com.example.mybighomework.MainActivity;
import com.example.mybighomework.ReportActivity;
import com.example.mybighomework.StudyPlanActivity;
import com.example.mybighomework.VocabularyActivity;
import com.example.mybighomework.WrongQuestionActivity;
import com.example.mybighomework.api.ZhipuAIService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用内自动化管理器
 * 只在当前应用内执行自动化操作，不跳转到其他应用
 */
public class InAppAutomationManager {
    
    private static final String TAG = "InAppAutomationManager";
    
    // 单例
    private static volatile InAppAutomationManager instance;
    
    private Context context;
    private ZhipuAIService aiService;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    private boolean isRunning = false;
    private int currentStep = 0;
    private static final int MAX_STEPS = 10;
    
    private AutomationCallback callback;
    
    /**
     * 应用内页面定义
     */
    public enum AppPage {
        HOME("主页", "显示学习进度、今日任务、功能入口", MainActivity.class),
        VOCABULARY("词汇训练", "单词学习和测试", VocabularyActivity.class),
        EXAM("真题练习", "历年真题练习", ExamListActivity.class),
        WRONG_QUESTIONS("错题本", "查看和复习错题", WrongQuestionActivity.class),
        STUDY_PLAN("学习计划", "查看和管理学习计划", StudyPlanActivity.class),
        REPORT("学习报告", "查看学习统计和报告", ReportActivity.class),
        AI_CHAT("AI助手", "与AI对话获取学习建议", AIChatActivity.class);
        
        public final String name;
        public final String description;
        public final Class<?> activityClass;
        
        AppPage(String name, String description, Class<?> activityClass) {
            this.name = name;
            this.description = description;
            this.activityClass = activityClass;
        }
    }
    
    /**
     * 应用内动作类型
     */
    public enum ActionType {
        NAVIGATE,       // 导航到页面
        START_LEARNING, // 开始学习
        VIEW_REPORT,    // 查看报告
        REVIEW_WRONG,   // 复习错题
        GENERATE_PLAN,  // 生成学习计划
        COMPLETE        // 完成
    }
    
    /**
     * 动作结果
     */
    public static class ActionResult {
        public ActionType type;
        public AppPage targetPage;
        public String message;
        public boolean success;
        
        public ActionResult(ActionType type, String message) {
            this.type = type;
            this.message = message;
            this.success = true;
        }
        
        public ActionResult(ActionType type, AppPage targetPage, String message) {
            this.type = type;
            this.targetPage = targetPage;
            this.message = message;
            this.success = true;
        }
    }
    
    public interface AutomationCallback {
        void onStatusUpdate(String status);
        void onActionExecuted(ActionResult action);
        void onTaskComplete(String message);
        void onError(String error);
    }
    
    private InAppAutomationManager() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static InAppAutomationManager getInstance() {
        if (instance == null) {
            synchronized (InAppAutomationManager.class) {
                if (instance == null) {
                    instance = new InAppAutomationManager();
                }
            }
        }
        return instance;
    }
    
    public void initialize(Context context, String apiKey) {
        this.context = context.getApplicationContext();
        this.aiService = new ZhipuAIService(apiKey);
    }
    
    /**
     * 执行应用内自动化指令
     * 所有指令都优先通过AI理解后再执行
     */
    public void executeCommand(String command, AutomationCallback callback) {
        if (isRunning) {
            callback.onError("任务正在执行中");
            return;
        }
        
        this.callback = callback;
        this.isRunning = true;
        this.currentStep = 0;
        
        executorService.execute(() -> {
            try {
                notifyStatus("正在理解您的指令...");
                
                // 优先使用AI理解用户指令
                ActionResult action = askAIForAction(command);
                
                // 如果AI无法理解，再尝试本地关键词匹配作为备用
                if (action == null) {
                    action = parseCommand(command);
                }
                
                if (action != null) {
                    executeAction(action);
                } else {
                    notifyError("抱歉，我无法理解您的指令: " + command + "\n\n您可以尝试说：\n• 打开词汇训练\n• 查看学习报告\n• 复习错题");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "执行指令失败", e);
                notifyError("执行失败: " + e.getMessage());
            } finally {
                isRunning = false;
            }
        });
    }
    
    /**
     * 解析用户指令
     */
    private ActionResult parseCommand(String command) {
        String lowerCommand = command.toLowerCase();
        
        // 导航指令
        if (containsAny(lowerCommand, "打开", "进入", "去", "跳转")) {
            if (containsAny(lowerCommand, "词汇", "单词", "背单词")) {
                return new ActionResult(ActionType.NAVIGATE, AppPage.VOCABULARY, "正在打开词汇训练...");
            }
            if (containsAny(lowerCommand, "真题", "练习", "考试", "试卷")) {
                return new ActionResult(ActionType.NAVIGATE, AppPage.EXAM, "正在打开真题练习...");
            }
            if (containsAny(lowerCommand, "错题", "复习")) {
                return new ActionResult(ActionType.NAVIGATE, AppPage.WRONG_QUESTIONS, "正在打开错题本...");
            }
            if (containsAny(lowerCommand, "计划", "学习计划")) {
                return new ActionResult(ActionType.NAVIGATE, AppPage.STUDY_PLAN, "正在打开学习计划...");
            }
            if (containsAny(lowerCommand, "报告", "统计", "进度")) {
                return new ActionResult(ActionType.NAVIGATE, AppPage.REPORT, "正在打开学习报告...");
            }
            if (containsAny(lowerCommand, "ai", "助手", "对话", "聊天")) {
                return new ActionResult(ActionType.NAVIGATE, AppPage.AI_CHAT, "正在打开AI助手...");
            }
            if (containsAny(lowerCommand, "主页", "首页", "home")) {
                return new ActionResult(ActionType.NAVIGATE, AppPage.HOME, "正在返回主页...");
            }
        }
        
        // 学习指令
        if (containsAny(lowerCommand, "开始学习", "学习", "背单词")) {
            return new ActionResult(ActionType.START_LEARNING, AppPage.VOCABULARY, "正在开始词汇学习...");
        }
        
        // 复习指令
        if (containsAny(lowerCommand, "复习错题", "看错题")) {
            return new ActionResult(ActionType.REVIEW_WRONG, AppPage.WRONG_QUESTIONS, "正在打开错题复习...");
        }
        
        // 报告指令
        if (containsAny(lowerCommand, "查看报告", "学习情况", "学习进度")) {
            return new ActionResult(ActionType.VIEW_REPORT, AppPage.REPORT, "正在查看学习报告...");
        }
        
        return null;
    }
    
    /**
     * 使用AI理解指令
     */
    private ActionResult askAIForAction(String command) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = "用户指令: " + command + "\n\n请分析用户想要执行什么操作，返回对应的页面名称。只返回页面名称，不要其他内容。";
            
            List<ZhipuAIService.ChatMessage> messages = new ArrayList<>();
            messages.add(new ZhipuAIService.ChatMessage("system", systemPrompt));
            messages.add(new ZhipuAIService.ChatMessage("user", userPrompt));
            
            // 使用同步方式等待AI响应
            final String[] responseHolder = new String[1];
            final Object lock = new Object();
            
            aiService.chat(messages, new ZhipuAIService.ChatCallback() {
                @Override
                public void onSuccess(String response) {
                    synchronized (lock) {
                        responseHolder[0] = response;
                        lock.notify();
                    }
                }
                
                @Override
                public void onError(String error) {
                    synchronized (lock) {
                        responseHolder[0] = null;
                        lock.notify();
                    }
                }
            });
            
            // 等待响应（最多5秒）
            synchronized (lock) {
                lock.wait(5000);
            }
            
            if (responseHolder[0] != null) {
                return parseAIResponse(responseHolder[0], command);
            }
        } catch (Exception e) {
            Log.e(TAG, "AI解析失败", e);
        }
        
        return null;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个英语学习应用的智能导航助手。你的任务是理解用户的意图，并判断用户想要访问哪个功能页面。\n\n");
        sb.append("应用包含以下功能页面：\n");
        
        for (AppPage page : AppPage.values()) {
            sb.append("- ").append(page.name).append(": ").append(page.description).append("\n");
        }
        
        sb.append("\n请仔细分析用户的指令，理解其真实意图：\n");
        sb.append("- 如果用户想学习单词、背单词、词汇测试等，返回：词汇训练\n");
        sb.append("- 如果用户想做题、练习、考试、真题等，返回：真题练习\n");
        sb.append("- 如果用户想看错题、复习错误、纠错等，返回：错题本\n");
        sb.append("- 如果用户想制定计划、查看计划、学习安排等，返回：学习计划\n");
        sb.append("- 如果用户想看进度、统计、报告、学习情况等，返回：学习报告\n");
        sb.append("- 如果用户想聊天、问问题、获取建议等，返回：AI助手\n");
        sb.append("- 如果用户想回主页、首页等，返回：主页\n");
        sb.append("\n只返回页面名称，不要返回其他内容。如果完全无法判断用户意图，返回：无法理解");
        
        return sb.toString();
    }
    
    /**
     * 解析AI响应
     */
    private ActionResult parseAIResponse(String response, String originalCommand) {
        String trimmed = response.trim();
        
        for (AppPage page : AppPage.values()) {
            if (trimmed.contains(page.name)) {
                return new ActionResult(ActionType.NAVIGATE, page, "正在打开" + page.name + "...");
            }
        }
        
        if (trimmed.contains("无法理解")) {
            return null;
        }
        
        // 尝试模糊匹配
        if (containsAny(trimmed, "词汇", "单词")) {
            return new ActionResult(ActionType.NAVIGATE, AppPage.VOCABULARY, "正在打开词汇训练...");
        }
        if (containsAny(trimmed, "真题", "练习")) {
            return new ActionResult(ActionType.NAVIGATE, AppPage.EXAM, "正在打开真题练习...");
        }
        
        return null;
    }
    
    /**
     * 执行动作
     */
    private void executeAction(ActionResult action) {
        currentStep++;
        notifyStatus(action.message);
        
        switch (action.type) {
            case NAVIGATE:
                navigateToPage(action.targetPage);
                break;
            case START_LEARNING:
                navigateToPage(AppPage.VOCABULARY);
                break;
            case REVIEW_WRONG:
                navigateToPage(AppPage.WRONG_QUESTIONS);
                break;
            case VIEW_REPORT:
                navigateToPage(AppPage.REPORT);
                break;
            default:
                break;
        }
        
        notifyActionExecuted(action);
        notifyComplete("操作完成: " + action.message);
    }
    
    /**
     * 导航到指定页面
     */
    private void navigateToPage(AppPage page) {
        if (page == null || page.activityClass == null) return;
        
        mainHandler.post(() -> {
            try {
                Intent intent = new Intent(context, page.activityClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "导航失败", e);
            }
        });
    }
    
    // 辅助方法
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private void notifyStatus(String status) {
        if (callback != null) {
            mainHandler.post(() -> callback.onStatusUpdate(status));
        }
    }
    
    private void notifyActionExecuted(ActionResult action) {
        if (callback != null) {
            mainHandler.post(() -> callback.onActionExecuted(action));
        }
    }
    
    private void notifyComplete(String message) {
        if (callback != null) {
            mainHandler.post(() -> callback.onTaskComplete(message));
        }
    }
    
    private void notifyError(String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
    
    public int getCurrentStep() {
        return currentStep;
    }
    
    public int getMaxSteps() {
        return MAX_STEPS;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void stopTask() {
        isRunning = false;
    }
}
