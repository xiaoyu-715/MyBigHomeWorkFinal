package com.example.mybighomework;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import com.example.mybighomework.database.AppDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybighomework.adapter.ChatMessageAdapter;
import com.example.mybighomework.api.ZhipuAIService;
import com.example.mybighomework.database.entity.DailyTaskEntity;
import com.example.mybighomework.dialog.PlanSelectionDialog;
import com.example.mybighomework.model.ChatMessage;
import com.example.mybighomework.repository.StudyPlanRepository;
import com.example.mybighomework.service.TaskGenerationService;
import com.example.mybighomework.utils.StudyPlanExtractor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AI 学习助手聊天界面
 * 提供与 智谱AI（glm-4-flash免费模型）的对话功能
 * 
 * 功能：
 * 1. AI 对话 - 与 智谱AI 进行智能对话
 * 2. 英语学习助手 - 可用于翻译、语法纠错、作文批改等
 * 3. 学习建议 - 获取个性化学习建议
 * 4. 问答解惑 - 解答英语学习相关问题
 */
public class AIChatActivity extends AppCompatActivity {
    
    private static final String TAG = "AIChatActivity";
    
    // 智谱AI API Key（glm-4-flash免费模型）
    private static final String ZHIPU_API_KEY = "e1b0c0c6ee7942908b11119e8fca3efa.w86kmtMVZLXo1vjE";
    
    // UI 组件
    private RecyclerView rvMessages;
    private EditText etInput;
    private ImageButton btnSend, btnBack, btnSettings, btnGeneratePlan;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private LinearLayout layoutInput;

    // 进度对话框
    private androidx.appcompat.app.AlertDialog progressDialog;
    
    // 适配器和数据
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList;
    
    // 智谱AI API 服务
    private ZhipuAIService apiService;
    
    // 主线程 Handler
    private Handler mainHandler;
    
    // 当前 AI 回复的消息（用于流式更新）
    private ChatMessage currentAiMessage;
    private StringBuilder currentMessageBuilder;  // 使用StringBuilder优化字符串拼接
    private long lastUpdateTime = 0;  // 上次更新时间，用于节流
    private static final long UPDATE_INTERVAL = 100;  // 更新间隔（毫秒）
    private Runnable updateRunnable;  // 延迟更新的Runnable
    
    // 学习计划相关
    private StudyPlanRepository studyPlanRepository;
    private StudyPlanExtractor planExtractor;
    private TaskGenerationService taskGenerationService;
    private int regenerateCount = 0;  // 重新生成次数计数
    private static final int MAX_REGENERATE_COUNT = 3;  // 最大重新生成次数
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置状态栏透明
        setupStatusBar();
        
        setContentView(R.layout.activity_ai_chat);
        
        // 设置软键盘弹出时的布局调整
        setupKeyboardHandling();
        
        initViews();
        initData();
        setupListeners();
        
        // 显示欢迎消息
        showWelcomeMessage();
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        rvMessages = findViewById(R.id.rv_messages);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
        btnSettings = findViewById(R.id.btn_settings);
        btnGeneratePlan = findViewById(R.id.btn_generate_plan);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        layoutInput = findViewById(R.id.layout_input);
        
        // 设置 RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        
        // 添加键盘监听，当键盘弹出时自动滚动到底部
        rvMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                     int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom && messageList.size() > 0) {
                    rvMessages.postDelayed(() -> {
                        if (messageList.size() > 0) {
                            rvMessages.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }, 100);
                }
            }
        });
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化消息列表
        messageList = new ArrayList<>();
        adapter = new ChatMessageAdapter(this, messageList);
        rvMessages.setAdapter(adapter);
        
        // 初始化 Handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 初始化学习计划仓库
        AppDatabase database = AppDatabase.getInstance(this);
        studyPlanRepository = new StudyPlanRepository(
            this.getApplication(),
            database.studyPlanDao(),
            database.studyPhaseDao(),
            database.dailyTaskDao()
        );
        
        // 初始化智谱AI服务（使用内置API Key）
        apiService = new ZhipuAIService(ZHIPU_API_KEY);
        planExtractor = new StudyPlanExtractor(apiService, this);
        
        // 初始化任务生成服务
        taskGenerationService = new TaskGenerationService(this);
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
        
        // 设置按钮（显示关于信息）
        btnSettings.setOnClickListener(v -> showAboutDialog());
        
        // 手动生成学习计划按钮
        btnGeneratePlan.setOnClickListener(v -> {
            if (messageList.isEmpty()) {
                Toast.makeText(this, "请先与AI助手进行对话", Toast.LENGTH_SHORT).show();
                return;
            }
            generateStudyPlanFromMessage(-1);
        });
        
        // 发送按钮
        btnSend.setOnClickListener(v -> sendMessage());
        
        // 输入框回车发送
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
        
        // 生成学习计划按钮点击监听
        adapter.setOnGeneratePlanClickListener(position -> {
            generateStudyPlanFromMessage(position);
        });
    }
    
    /**
     * 设置状态栏透明
     */
    private void setupStatusBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
    
    /**
     * 设置键盘处理
     */
    private void setupKeyboardHandling() {
        // 设置窗口软输入模式为 adjustResize
        getWindow().setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        );
        
        // 使用 WindowInsetsCompat 监听键盘变化
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            
            // 设置输入框底部边距为键盘高度
            if (layoutInput != null) {
                layoutInput.setPadding(
                    layoutInput.getPaddingLeft(),
                    layoutInput.getPaddingTop(),
                    layoutInput.getPaddingRight(),
                    imeInsets.bottom > 0 ? imeInsets.bottom : (int)(8 * getResources().getDisplayMetrics().density)
                );
            }
            
            // 键盘弹出时滚动到底部
            if (imeInsets.bottom > 0 && messageList != null && messageList.size() > 0) {
                rvMessages.postDelayed(() -> {
                    rvMessages.smoothScrollToPosition(messageList.size() - 1);
                }, 100);
            }
            
            return windowInsets;
        });
        
        // 备用方案：使用 ViewTreeObserver
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousKeyboardHeight = 0;
            
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keyboardHeight = screenHeight - r.bottom;
                
                if (Math.abs(keyboardHeight - previousKeyboardHeight) > 100) {
                    previousKeyboardHeight = keyboardHeight;
                    
                    if (keyboardHeight > 200 && messageList != null && messageList.size() > 0) {
                        rvMessages.postDelayed(() -> {
                            rvMessages.smoothScrollToPosition(messageList.size() - 1);
                        }, 150);
                    }
                }
            }
        });
    }
    
    /**
     * 显示欢迎消息
     */
    private void showWelcomeMessage() {
        String welcomeText = "👋 你好！我是你的英语学习 AI 助手。\n\n" +
                "我可以帮你：\n" +
                "• 翻译英文句子或文章\n" +
                "• 纠正语法错误\n" +
                "• 批改英语作文\n" +
                "• 解释词汇用法\n" +
                "• 提供学习建议\n" +
                "• 解答英语相关问题\n\n" +
                "请问有什么可以帮到你的吗？";
        
        ChatMessage welcomeMessage = new ChatMessage(
                ChatMessage.TYPE_RECEIVED,
                welcomeText,
                System.currentTimeMillis()
        );
        
        messageList.add(welcomeMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        updateEmptyView();
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String input = etInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (apiService == null) {
            Toast.makeText(this, "AI服务初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 清空输入框
        etInput.setText("");
        
        // 添加用户消息
        ChatMessage userMessage = new ChatMessage(
                ChatMessage.TYPE_SENT,
                input,
                System.currentTimeMillis()
        );
        messageList.add(userMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.smoothScrollToPosition(messageList.size() - 1);
        updateEmptyView();
        
        // 显示加载状态
        showLoading(true);
        
        // 构建消息历史
        List<ZhipuAIService.ChatMessage> apiMessages = buildApiMessages();
        
        // 发送请求（使用流式输出）
        apiService.chatStream(apiMessages, new ZhipuAIService.StreamCallback() {
            @Override
            public void onChunk(String chunk) {
                mainHandler.post(() -> {
                    if (currentAiMessage == null) {
                        // 创建新的 AI 消息
                        currentMessageBuilder = new StringBuilder();
                        currentMessageBuilder.append(chunk);
                        currentAiMessage = new ChatMessage(
                                ChatMessage.TYPE_RECEIVED,
                                chunk,
                                System.currentTimeMillis()
                        );
                        messageList.add(currentAiMessage);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        // 首次添加时滚动到最新消息
                        rvMessages.scrollToPosition(messageList.size() - 1);
                        updateEmptyView();
                    } else {
                        // 追加内容到StringBuilder
                        currentMessageBuilder.append(chunk);
                        
                        // 节流更新UI，避免频繁刷新
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                            // 立即更新
                            currentAiMessage.setContent(currentMessageBuilder.toString());
                            adapter.notifyItemChanged(messageList.size() - 1, "payload");
                            lastUpdateTime = currentTime;
                            
                            // 只在用户已经滚动到底部时才自动滚动
                            if (isScrolledToBottom()) {
                                rvMessages.scrollToPosition(messageList.size() - 1);
                            }
                        } else {
                            // 取消之前的延迟更新
                            if (updateRunnable != null) {
                                mainHandler.removeCallbacks(updateRunnable);
                            }
                            
                            // 设置延迟更新，确保最后的内容也能显示
                            updateRunnable = () -> {
                                if (currentAiMessage != null && currentMessageBuilder != null) {
                                    currentAiMessage.setContent(currentMessageBuilder.toString());
                                    adapter.notifyItemChanged(messageList.size() - 1, "payload");
                                    lastUpdateTime = System.currentTimeMillis();
                                }
                            };
                            mainHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
                        }
                    }
                });
            }
            
            @Override
            public void onComplete() {
                mainHandler.post(() -> {
                    showLoading(false);
                    
                    // 确保最后的内容更新
                    if (currentAiMessage != null && currentMessageBuilder != null) {
                        currentAiMessage.setContent(currentMessageBuilder.toString());
                        adapter.notifyItemChanged(messageList.size() - 1);
                        
                        // 智能检测：如果AI回复包含学习建议，自动显示生成按钮
                        if (isStudyAdviceMessage(currentAiMessage.getContent())) {
                            currentAiMessage.setShowGeneratePlanButton(true);
                            adapter.notifyItemChanged(messageList.size() - 1);
                        }
                    }
                    
                    // 清理资源
                    currentAiMessage = null;
                    currentMessageBuilder = null;
                    lastUpdateTime = 0;
                    if (updateRunnable != null) {
                        mainHandler.removeCallbacks(updateRunnable);
                        updateRunnable = null;
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    showLoading(false);
                    
                    // 清理资源
                    currentAiMessage = null;
                    currentMessageBuilder = null;
                    lastUpdateTime = 0;
                    if (updateRunnable != null) {
                        mainHandler.removeCallbacks(updateRunnable);
                        updateRunnable = null;
                    }
                    
                    Toast.makeText(AIChatActivity.this, 
                            "发送失败: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 构建 API 消息列表
     */
    private List<ZhipuAIService.ChatMessage> buildApiMessages() {
        List<ZhipuAIService.ChatMessage> apiMessages = new ArrayList<>();
        
        // 添加系统提示（定义 AI 角色）
        String systemPrompt = "你是一个专业的英语学习助手，擅长帮助学生提高英语水平。" +
                "你可以进行翻译、语法纠错、作文批改、词汇解释等。" +
                "请用简洁、友好的方式回答问题。";
        apiMessages.add(new ZhipuAIService.ChatMessage("system", systemPrompt));
        
        // 添加历史消息（最近10条）
        int startIndex = Math.max(0, messageList.size() - 10);
        for (int i = startIndex; i < messageList.size(); i++) {
            ChatMessage msg = messageList.get(i);
            String role = msg.getType() == ChatMessage.TYPE_SENT ? "user" : "assistant";
            apiMessages.add(new ZhipuAIService.ChatMessage(role, msg.getContent()));
        }
        
        return apiMessages;
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        // 不显示加载圆圈，但仍禁用输入
        progressBar.setVisibility(View.GONE);
        btnSend.setEnabled(!show);
        etInput.setEnabled(!show);
    }
    
    /**
     * 更新空状态视图
     */
    private void updateEmptyView() {
        tvEmpty.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
    }
    
    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("关于 AI 学习助手")
            .setMessage("本助手由智谱AI（glm-4-flash）提供支持\n\n" +
                       "功能特点：\n" +
                       "• 英语翻译与纠错\n" +
                       "• 作文批改\n" +
                       "• 学习计划生成\n" +
                       "• 智能问答\n\n" +
                       "免费使用，无需配置")
            .setPositiveButton("确定", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
        if (studyPlanRepository != null) {
            studyPlanRepository.shutdown();
        }
        dismissProgressDialog();
    }

    // ==================== 学习计划生成功能 ====================
    
    /**
     * 从消息生成学习计划
     */
    private void generateStudyPlanFromMessage(int position) {
        if (planExtractor == null) {
            Toast.makeText(this, "AI服务未初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 获取对话上下文
        String context = getConversationContext();
        
        // 显示进度对话框
        showProgressDialog();
        
        // 调用提取器生成结构化学习计划（带阶段和任务）
        planExtractor.extractStructuredPlan(context, 
            new StudyPlanExtractor.OnStructuredPlanExtractedListener() {
                @Override
                public void onSuccess(StudyPlanExtractor.StructuredPlanResult result) {
                    mainHandler.post(() -> {
                        dismissProgressDialog();
                        regenerateCount = 0;
                        // 保存结构化计划（包含阶段）
                        saveStructuredPlan(result);
                    });
                }
                
                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        dismissProgressDialog();
                        Toast.makeText(AIChatActivity.this, 
                            getString(R.string.generation_failed) + ": " + error, 
                            Toast.LENGTH_LONG).show();
                    });
                }
            },
            new StudyPlanExtractor.OnProgressUpdateListener() {
                @Override
                public void onProgressUpdate(String message, int progress) {
                    mainHandler.post(() -> {
                        updateProgressDialog(message, progress);
                    });
                }
            });
    }
    
    /**
     * 获取对话上下文（最近5轮对话，即10条消息）
     */
    private String getConversationContext() {
        StringBuilder context = new StringBuilder();
        
        // 获取最近10条消息（5轮对话）
        int start = Math.max(0, messageList.size() - 10);
        for (int i = start; i < messageList.size(); i++) {
            ChatMessage msg = messageList.get(i);
            String role = msg.getType() == ChatMessage.TYPE_SENT ? "用户" : "AI助手";
            context.append(role).append(": ").append(msg.getContent()).append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * 显示学习计划选择对话框
     */
    private void showPlanSelectionDialog(List<StudyPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            Toast.makeText(this, "未能生成有效的学习计划", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 添加调试日志
        Log.d(TAG, "准备显示学习计划对话框，计划数量：" + plans.size());
        
        PlanSelectionDialog dialog = PlanSelectionDialog.newInstance(new ArrayList<>(plans));
        
        // 设置计划选择监听器
        dialog.setOnPlansSelectedListener(selectedPlans -> {
            Log.d(TAG, "用户选择了 " + selectedPlans.size() + " 个计划");
            saveSelectedPlans(selectedPlans);
        });
        
        // 设置重新生成监听器
        dialog.setOnRegenerateClickListener(() -> {
            Log.d(TAG, "用户请求重新生成计划");
            handleRegeneratePlans();
        });
        
        // 确保对话框显示
        dialog.setCancelable(false); // 防止点击外部关闭
        dialog.show(getSupportFragmentManager(), "PlanSelectionDialog");
        
        // 显示提示
        Toast.makeText(this, "AI已生成 " + plans.size() + " 个学习计划，请选择保存", 
            Toast.LENGTH_LONG).show();
    }
    
    /**
     * 保存选中的学习计划
     */
    private void saveSelectedPlans(List<StudyPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return;
        }
        
        final int totalCount = plans.size();
        final int[] savedCount = {0};
        final int[] failedCount = {0};
        final List<Long> savedPlanIds = new ArrayList<>();
        
        // 显示保存进度对话框
        showSavingProgressDialog(totalCount);
        
        for (StudyPlan plan : plans) {
            // 添加AI生成标识和时间戳
            enrichPlanWithMetadata(plan);
            
            studyPlanRepository.addStudyPlanAsync(plan, 
                new StudyPlanRepository.OnPlanSavedListener() {
                    @Override
                    public void onPlanSaved(long id) {
                        savedCount[0]++;
                        savedPlanIds.add(id);
                        updateSavingProgress(savedCount[0], totalCount);
                        checkSaveCompleteAndGenerateTasks(savedCount[0], failedCount[0], totalCount, savedPlanIds);
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "保存学习计划失败", e);
                        failedCount[0]++;
                        checkSaveCompleteAndGenerateTasks(savedCount[0], failedCount[0], totalCount, savedPlanIds);
                    }
                });
        }
    }
    
    /**
     * 检查保存是否完成，并为保存成功的计划生成今日任务
     */
    private void checkSaveCompleteAndGenerateTasks(int savedCount, int failedCount, int totalCount, List<Long> savedPlanIds) {
        if (savedCount + failedCount == totalCount) {
            // 全部完成
            if (savedCount > 0 && !savedPlanIds.isEmpty()) {
                // 更新进度提示
                updateProgressDialog("正在生成今日任务...", 90);
                
                // 为所有保存成功的计划生成今日任务
                generateTodayTasksForPlans(savedPlanIds, savedCount, failedCount);
            } else {
                dismissProgressDialog();
                new AlertDialog.Builder(this)
                    .setTitle("保存失败")
                    .setMessage("无法保存学习计划，请检查网络连接后重试")
                    .setPositiveButton("确定", null)
                    .show();
            }
        }
    }
    
    /**
     * 为保存的计划生成今日任务
     */
    private void generateTodayTasksForPlans(List<Long> planIds, int savedCount, int failedCount) {
        final int[] tasksGeneratedCount = {0};
        final int[] tasksFailedCount = {0};
        final int totalPlans = planIds.size();
        
        for (Long planId : planIds) {
            taskGenerationService.ensureTodayTasksExist(planId.intValue(), 
                new TaskGenerationService.OnTasksGeneratedListener() {
                    @Override
                    public void onTasksGenerated(List<DailyTaskEntity> tasks, boolean isNewlyGenerated) {
                        if (isNewlyGenerated && !tasks.isEmpty()) {
                            tasksGeneratedCount[0]++;
                            Log.d(TAG, "为计划 " + planId + " 生成了 " + tasks.size() + " 个今日任务");
                        }
                        checkTaskGenerationComplete(tasksGeneratedCount[0], tasksFailedCount[0], 
                            totalPlans, savedCount, failedCount);
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        tasksFailedCount[0]++;
                        Log.e(TAG, "为计划 " + planId + " 生成今日任务失败", e);
                        checkTaskGenerationComplete(tasksGeneratedCount[0], tasksFailedCount[0], 
                            totalPlans, savedCount, failedCount);
                    }
                });
        }
    }
    
    /**
     * 检查任务生成是否完成
     */
    private void checkTaskGenerationComplete(int tasksGeneratedCount, int tasksFailedCount, 
                                             int totalPlans, int savedCount, int failedCount) {
        if (tasksGeneratedCount + tasksFailedCount == totalPlans) {
            dismissProgressDialog();
            showSuccessDialogWithTaskInfo(savedCount, failedCount, tasksGeneratedCount);
        }
    }
    
    /**
     * 显示成功对话框（带任务生成信息）
     */
    private void showSuccessDialogWithTaskInfo(int savedCount, int failedCount, int tasksGeneratedCount) {
        StringBuilder message = new StringBuilder();
        
        if (failedCount == 0) {
            message.append(String.format("✅ 成功保存%d个AI学习计划\n", savedCount));
        } else {
            message.append(String.format("✅ 成功保存%d个计划（%d个失败）\n", savedCount, failedCount));
        }
        
        if (tasksGeneratedCount > 0) {
            message.append(String.format("📋 已为%d个计划生成今日任务\n\n", tasksGeneratedCount));
            message.append("您可以立即开始学习，或稍后在计划详情中查看任务。");
        } else {
            message.append("\n计划已添加到您的学习计划列表中，您可以随时查看和调整。");
        }
        
        new AlertDialog.Builder(this)
            .setTitle("🎉 AI学习计划已生成")
            .setMessage(message.toString())
            .setPositiveButton("立即查看", (dialog, which) -> {
                // 添加标识，表示是从AI生成跳转过去的
                Intent intent = new Intent(this, StudyPlanActivity.class);
                intent.putExtra("from_ai_generation", true);
                intent.putExtra("generated_count", savedCount);
                startActivity(intent);
                
                // 添加过渡动画
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            })
            .setNegativeButton("稍后查看", null)
            .setCancelable(false)
            .show();
    }
    
    /**
     * 检测消息是否包含学习建议
     */
    private boolean isStudyAdviceMessage(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        String[] keywords = {"建议", "计划", "学习", "步骤", "阶段", "目标", "练习", 
                            "复习", "掌握", "提高", "强化", "备考", "方法"};
        
        String lowerContent = content.toLowerCase();
        int matchCount = 0;
        
        for (String keyword : keywords) {
            if (lowerContent.contains(keyword)) {
                matchCount++;
            }
        }
        
        // 如果包含3个或以上关键词，认为是学习建议
        return matchCount >= 3;
    }

    // ==================== 进度对话框管理 ====================
    
    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }
        
        View progressView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(progressView);
        builder.setCancelable(false);
        
        progressDialog = builder.create();
        
        // 设置取消按钮
        android.widget.Button btnCancel = progressView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            dismissProgressDialog();
            Toast.makeText(this, "已取消生成", Toast.LENGTH_SHORT).show();
        });
        
        progressDialog.show();
    }
    
    /**
     * 更新进度对话框
     */
    private void updateProgressDialog(String message, int progress) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        
        android.widget.ProgressBar progressBar = progressDialog.findViewById(R.id.progress_bar);
        android.widget.TextView tvMessage = progressDialog.findViewById(R.id.tv_progress_message);
        android.widget.TextView tvPercent = progressDialog.findViewById(R.id.tv_progress_percent);
        
        // 更新进度条
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        
        // 更新文字
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
        
        if (tvPercent != null) {
            tvPercent.setText(progress + "%");
        }
        
        // 更新步骤指示器
        updateStepIndicators(progress);
    }
    
    /**
     * 更新步骤指示器
     */
    private void updateStepIndicators(int progress) {
        if (progressDialog == null) return;
        
        View step1 = progressDialog.findViewById(R.id.step1_indicator);
        View step2 = progressDialog.findViewById(R.id.step2_indicator);
        View step3 = progressDialog.findViewById(R.id.step3_indicator);
        
        // 根据进度更新步骤状态
        if (step1 != null) {
            if (progress >= 10) {
                step1.setBackgroundResource(R.drawable.bg_gradient_primary);
            } else {
                step1.setBackgroundColor(getColor(R.color.separator));
            }
        }
        
        if (step2 != null) {
            if (progress >= 40) {
                step2.setBackgroundResource(R.drawable.bg_gradient_primary);
            } else {
                step2.setBackgroundColor(getColor(R.color.separator));
            }
        }
        
        if (step3 != null) {
            if (progress >= 80) {
                step3.setBackgroundResource(R.drawable.bg_gradient_primary);
            } else {
                step3.setBackgroundColor(getColor(R.color.separator));
            }
        }
    }
    
    /**
     * 关闭进度对话框
     */
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    /**
     * 检查RecyclerView是否已滚动到底部
     */
    private boolean isScrolledToBottom() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvMessages.getLayoutManager();
        if (layoutManager != null) {
            int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
            return lastVisiblePosition >= messageList.size() - 2;  // 允许一定的容差
        }
        return false;
    }
    
    /**
     * 处理重新生成学习计划
     */
    private void handleRegeneratePlans() {
        // 检查重新生成次数
        if (regenerateCount >= MAX_REGENERATE_COUNT) {
            Toast.makeText(this, R.string.regenerate_limit_reached, Toast.LENGTH_LONG).show();
            return;
        }
        
        regenerateCount++;
        Toast.makeText(this, getString(R.string.regenerating) + " (第" + regenerateCount + "次)", 
                      Toast.LENGTH_SHORT).show();
        
        // 重新生成
        generateStudyPlanFromMessage(-1);
    }
    
    /**
     * 保存结构化学习计划（包含阶段）
     */
    private void saveStructuredPlan(StudyPlanExtractor.StructuredPlanResult result) {
        if (result == null || result.plan == null) {
            Toast.makeText(this, "计划数据为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "开始保存结构化计划: " + result.plan.getTitle());
        Log.d(TAG, "阶段数量: " + (result.phases != null ? result.phases.size() : 0));
        
        showSavingProgressDialog(1);
        
        // 保存结构化计划（包含阶段）
        // 注意：taskTemplates需要转换为DailyTaskEntity列表
        List<DailyTaskEntity> tasks = convertTaskTemplatesToEntities(result.taskTemplates);
        
        Log.d(TAG, "调用 studyPlanRepository.savePlanWithPhasesAndTasks");
        
        studyPlanRepository.savePlanWithPhasesAndTasks(
            result.plan,
            result.phases,
            tasks,
            new StudyPlanRepository.OnPlanSavedListener() {
                @Override
                public void onPlanSaved(long id) {
                    Log.d(TAG, "计划保存成功回调, ID: " + id);
                    mainHandler.post(() -> {
                        dismissProgressDialog();
                        
                        // 显示成功对话框
                        new AlertDialog.Builder(AIChatActivity.this)
                            .setTitle("🎉 AI学习计划已生成")
                            .setMessage(
                                "✅ 成功保存学习计划\n" +
                                "📋 已为计划创建" + result.phases.size() + "个学习阶段\n\n" +
                                "您可以立即开始学习，或稍后在计划详情中查看任务。"
                            )
                            .setPositiveButton("立即查看", (dialog, which) -> {
                                Intent intent = new Intent(AIChatActivity.this, StudyPlanActivity.class);
                                intent.putExtra("from_ai_generation", true);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            })
                            .setNegativeButton("稍后查看", null)
                            .setCancelable(false)
                            .show();
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "计划保存失败回调", e);
                    mainHandler.post(() -> {
                        dismissProgressDialog();
                        Log.e(TAG, "保存结构化计划失败", e);
                        Toast.makeText(AIChatActivity.this, 
                            "保存失败: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        );
    }
    
    /**
     * 将任务模板转换为DailyTaskEntity列表
     */
    private List<DailyTaskEntity> convertTaskTemplatesToEntities(List<List<com.example.mybighomework.utils.StructuredPlanParser.TaskTemplate>> taskTemplates) {
        // 简化实现：返回null，让TaskGenerationService自动生成任务
        // 完整实现需要将TaskTemplate转换为DailyTaskEntity
        return null;
    }
    
    /**
     * 为学习计划添加元数据
     */
    private void enrichPlanWithMetadata(StudyPlan plan) {
        // 添加AI生成标识
        String currentDescription = plan.getDescription();
        if (!currentDescription.contains("🤖")) {
            plan.setDescription("🤖 AI生成 | " + currentDescription);
        }
        
        // 添加生成时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        plan.setDescription(plan.getDescription() + "\n\n生成时间：" + timestamp);
        
        // 设置状态为未开始
        if (plan.getStatus() == null || plan.getStatus().isEmpty()) {
            plan.setStatus("未开始");
        }
        
        // 设置初始进度
        plan.setProgress(0);
    }
    
    /**
     * 显示保存进度对话框
     */
    private void showSavingProgressDialog(int totalCount) {
        androidx.appcompat.app.AlertDialog.Builder builder = 
            new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("保存学习计划");
        builder.setMessage(String.format("正在保存 0/%d 个计划...", totalCount));
        
        progressDialog = builder.create();
        progressDialog.show();
    }
    
    /**
     * 更新保存进度
     */
    private void updateSavingProgress(int savedCount, int totalCount) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(String.format("正在保存 %d/%d 个计划...", 
                savedCount, totalCount));
        }
    }
}
