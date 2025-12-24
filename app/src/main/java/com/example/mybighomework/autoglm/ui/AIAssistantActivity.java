package com.example.mybighomework.autoglm.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import com.example.mybighomework.R;
import com.example.mybighomework.autoglm.manager.InAppAutomationManager;
import com.example.mybighomework.autoglm.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能自动化Activity
 * 提供应用内自动化操作功能
 */
public class AIAssistantActivity extends AppCompatActivity {
    
    // UI组件
    private RecyclerView chatRecyclerView;
    private EditText inputEditText;
    private ImageButton sendButton;
    private View inputContainer;
    
    // 任务状态
    private CardView taskStatusLayout;
    private ProgressBar taskProgressBar;
    private TextView tvTaskStatus;
    private TextView tvTaskStep;
    private TextView tvTaskDetail;
    
    // 示例指令
    private Chip btnExample1;
    private Chip btnExample2;
    private Chip btnExample3;
    private Chip btnExample4;
    private Chip btnExample5;
    
    // 数据
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private InAppAutomationManager automationManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置键盘调整模式
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        setContentView(R.layout.activity_ai_assistant);
        
        initializeViews();
        initializeData();
        setupListeners();
        setupKeyboardListener();
    }
    
    /**
     * 初始化视图组件
     */
    private void initializeViews() {
        // 工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        
        // 日志列表
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // 输入区域
        inputEditText = findViewById(R.id.inputEditText);
        sendButton = findViewById(R.id.sendButton);
        inputContainer = findViewById(R.id.inputContainer);
        
        // 任务状态
        taskStatusLayout = findViewById(R.id.taskStatusLayout);
        taskProgressBar = findViewById(R.id.taskProgressBar);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);
        tvTaskStep = findViewById(R.id.tvTaskStep);
        tvTaskDetail = findViewById(R.id.tvTaskDetail);
        
        // 示例指令
        btnExample1 = findViewById(R.id.btnExample1);
        btnExample2 = findViewById(R.id.btnExample2);
        btnExample3 = findViewById(R.id.btnExample3);
        btnExample4 = findViewById(R.id.btnExample4);
        btnExample5 = findViewById(R.id.btnExample5);
    }
    
    /**
     * 初始化数据
     */
    private void initializeData() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setAdapter(chatAdapter);
        
        // 初始化应用内自动化管理器
        automationManager = InAppAutomationManager.getInstance();
        automationManager.initialize(this, "e1b0c0c6ee7942908b11119e8fca3efa.w86kmtMVZLXo1vjE");
        
        // 添加欢迎消息
        addWelcomeMessage();
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 发送按钮
        sendButton.setOnClickListener(v -> executeCommand());
        
        // 输入框文本变化
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.length() > 0);
                sendButton.setAlpha(s.length() > 0 ? 1.0f : 0.5f);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 示例指令按钮
        btnExample1.setOnClickListener(v -> {
            inputEditText.setText(btnExample1.getText());
            inputEditText.setSelection(inputEditText.getText().length());
        });
        
        btnExample2.setOnClickListener(v -> {
            inputEditText.setText(btnExample2.getText());
            inputEditText.setSelection(inputEditText.getText().length());
        });
        
        if (btnExample3 != null) {
            btnExample3.setOnClickListener(v -> {
                inputEditText.setText(btnExample3.getText());
                inputEditText.setSelection(inputEditText.getText().length());
            });
        }
        
        if (btnExample4 != null) {
            btnExample4.setOnClickListener(v -> {
                inputEditText.setText(btnExample4.getText());
                inputEditText.setSelection(inputEditText.getText().length());
            });
        }
        
        if (btnExample5 != null) {
            btnExample5.setOnClickListener(v -> {
                inputEditText.setText(btnExample5.getText());
                inputEditText.setSelection(inputEditText.getText().length());
            });
        }
    }
    
    /**
     * 设置键盘监听器
     */
    private void setupKeyboardListener() {
        final View rootView = findViewById(R.id.rootLayout);
        
        // 使用 WindowInsetsCompat 监听键盘变化
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            
            if (inputContainer != null) {
                inputContainer.setPadding(
                    inputContainer.getPaddingLeft(),
                    inputContainer.getPaddingTop(),
                    inputContainer.getPaddingRight(),
                    imeInsets.bottom > 0 ? imeInsets.bottom : (int)(12 * getResources().getDisplayMetrics().density)
                );
            }
            
            if (imeInsets.bottom > 0) {
                chatRecyclerView.postDelayed(this::scrollToBottom, 100);
            }
            
            return windowInsets;
        });
        
        // 备用方案
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
                    if (keyboardHeight > 200) {
                        chatRecyclerView.postDelayed(() -> scrollToBottom(), 150);
                    }
                }
            }
        });
    }
    
    /**
     * 添加欢迎消息
     */
    private void addWelcomeMessage() {
        ChatMessage welcome = new ChatMessage("system",
                "👋 欢迎使用智能导航！\n\n" +
                "我可以帮你快速跳转到应用内的各个功能：\n" +
                "• 词汇训练 - 背单词、词汇测试\n" +
                "• 真题练习 - 历年真题练习\n" +
                "• 错题本 - 查看和复习错题\n" +
                "• 学习计划 - 管理学习计划\n" +
                "• 学习报告 - 查看学习统计\n" +
                "• AI助手 - 获取学习建议\n\n" +
                "试试说：\"打开词汇训练\" 或 \"我要背单词\"");
        messages.add(welcome);
        chatAdapter.notifyItemInserted(messages.size() - 1);
    }
    
    /**
     * 执行自动化指令
     */
    private void executeCommand() {
        String command = inputEditText.getText().toString().trim();
        if (command.isEmpty()) return;
        
        // 清空输入框
        inputEditText.setText("");
        
        // 添加用户指令到日志
        ChatMessage userMessage = new ChatMessage("user", command);
        messages.add(userMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // 显示任务状态
        showTaskStatus(true);
        updateTaskStatus("处理中...", "正在理解指令...", 0);
        
        // 执行应用内自动化
        automationManager.executeCommand(command, new InAppAutomationManager.AutomationCallback() {
            @Override
            public void onStatusUpdate(String status) {
                runOnUiThread(() -> {
                    tvTaskDetail.setText(status);
                    addSystemMessage("📍 " + status);
                });
            }
            
            @Override
            public void onActionExecuted(InAppAutomationManager.ActionResult action) {
                runOnUiThread(() -> {
                    int step = automationManager.getCurrentStep();
                    updateTaskStatus("执行中...", action.message, step);
                });
            }
            
            @Override
            public void onTaskComplete(String message) {
                runOnUiThread(() -> {
                    showTaskStatus(false);
                    addSystemMessage("✅ " + message);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showTaskStatus(false);
                    addSystemMessage("❌ " + error);
                    Toast.makeText(AIAssistantActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * 显示/隐藏任务状态
     */
    private void showTaskStatus(boolean show) {
        if (taskStatusLayout != null) {
            taskStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String status, String detail, int step) {
        if (tvTaskStatus != null) tvTaskStatus.setText(status);
        if (tvTaskDetail != null) tvTaskDetail.setText(detail);
        if (tvTaskStep != null) tvTaskStep.setText(String.format("步骤: %d/%d", step, automationManager.getMaxSteps()));
    }
    
    /**
     * 添加系统消息
     */
    private void addSystemMessage(String message) {
        ChatMessage systemMessage = new ChatMessage("system", message);
        messages.add(systemMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }
    
    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        if (messages.size() > 0) {
            chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
