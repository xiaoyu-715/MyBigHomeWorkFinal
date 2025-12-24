package com.example.mybighomework;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.TranslationHistoryAdapter;
import com.example.mybighomework.api.ZhipuAIService;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.TranslationHistoryDao;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;
import com.example.mybighomework.dialog.HistoryDetailDialog;
import com.example.mybighomework.fragment.HistoryBottomSheetFragment;
import com.example.mybighomework.repository.TranslationHistoryRepository;
import com.example.mybighomework.translation.ZhipuTranslationService;
import com.example.mybighomework.utils.TaskProgressTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * 文本输入翻译Activity
 */
public class TextTranslationActivity extends AppCompatActivity {

    private static final String TAG = "TextTranslation";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // UI组件
    private ImageButton btnBack, btnSwitchLanguage, btnClearInput, btnClearHistory, btnCopyResult;
    private TextView tvSourceLanguage, tvTargetLanguage, tvEmptyHistory, tvTranslationResult;
    private TextView btnViewAllHistory;
    private EditText etInputText;
    private Button btnTranslate;
    private CardView cardTranslationResult;
    private RecyclerView rvHistory;
    private FloatingActionButton fabVoiceInput;
    private ProgressBar progressBar;

    // 数据相关
    private TranslationHistoryDao historyDao;
    private TranslationHistoryRepository historyRepository;
    private TranslationHistoryAdapter historyAdapter;
    
    // 主页面显示的历史记录数量限制
    private static final int MAIN_PAGE_HISTORY_LIMIT = 5;

    // 智谱AI翻译服务
    private static final String ZHIPU_API_KEY = "e1b0c0c6ee7942908b11119e8fca3efa.w86kmtMVZLXo1vjE";
    private ZhipuTranslationService translationService;
    
    // 语言代码常量
    private static final String LANG_ENGLISH = "en";
    private static final String LANG_CHINESE = "zh";
    
    private String sourceLanguage = LANG_ENGLISH;
    private String targetLanguage = LANG_CHINESE;

    // 语音识别
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_translation);

        // 设置全屏显示，让内容延伸到状态栏
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        initViews();
        initDatabase();
        initTranslationService();
        setupClickListeners();
        loadHistory();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSwitchLanguage = findViewById(R.id.btn_switch_language);
        btnClearInput = findViewById(R.id.btn_clear_input);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        btnCopyResult = findViewById(R.id.btn_copy_result);
        btnViewAllHistory = findViewById(R.id.btn_view_all_history);
        tvSourceLanguage = findViewById(R.id.tv_source_language);
        tvTargetLanguage = findViewById(R.id.tv_target_language);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        tvTranslationResult = findViewById(R.id.tv_translation_result);
        etInputText = findViewById(R.id.et_input_text);
        btnTranslate = findViewById(R.id.btn_translate);
        cardTranslationResult = findViewById(R.id.card_translation_result);
        rvHistory = findViewById(R.id.rv_history);
        fabVoiceInput = findViewById(R.id.fab_voice_input);
        progressBar = findViewById(R.id.progress_bar);

        // 设置历史记录RecyclerView
        historyAdapter = new TranslationHistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);

        updateLanguageDisplay();
    }

    private void initDatabase() {
        AppDatabase database = AppDatabase.getInstance(this);
        historyDao = database.translationHistoryDao();
        historyRepository = new TranslationHistoryRepository(historyDao);
    }

    private void initTranslationService() {
        ZhipuAIService aiService = new ZhipuAIService(ZHIPU_API_KEY);
        translationService = new ZhipuTranslationService(aiService);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSwitchLanguage.setOnClickListener(v -> switchLanguage());

        btnClearInput.setOnClickListener(v -> {
            etInputText.setText("");
            cardTranslationResult.setVisibility(View.GONE);
        });

        btnTranslate.setOnClickListener(v -> performTranslation());

        btnCopyResult.setOnClickListener(v -> copyTranslationResult());

        btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());

        // 查看全部历史记录按钮
        btnViewAllHistory.setOnClickListener(v -> showHistoryBottomSheet());

        fabVoiceInput.setOnClickListener(v -> requestVoiceInput());

        // 历史记录点击事件 - 显示详情对话框
        historyAdapter.setOnItemClickListener(history -> {
            showHistoryDetailDialog(history);
        });
    }

    private void switchLanguage() {
        // 切换源语言和目标语言
        String temp = sourceLanguage;
        sourceLanguage = targetLanguage;
        targetLanguage = temp;

        updateLanguageDisplay();

        // 清空输入和结果
        etInputText.setText("");
        cardTranslationResult.setVisibility(View.GONE);
    }

    private void updateLanguageDisplay() {
        String sourceLangText = LANG_ENGLISH.equals(sourceLanguage) ? "英文" : "中文";
        String targetLangText = LANG_ENGLISH.equals(targetLanguage) ? "英文" : "中文";
        
        tvSourceLanguage.setText(sourceLangText);
        tvTargetLanguage.setText(targetLangText);
    }

    private void performTranslation() {
        String inputText = etInputText.getText().toString().trim();
        
        if (TextUtils.isEmpty(inputText)) {
            Toast.makeText(this, "请输入要翻译的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (translationService == null) {
            Toast.makeText(this, "翻译服务未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        // 预处理输入文本，优化翻译质量
        String processedText = TranslationTextProcessor.preprocessText(inputText);

        showProgress(true);

        translationService.translate(processedText, sourceLanguage, targetLanguage, 
                new ZhipuTranslationService.TranslationCallback() {
                    @Override
                    public void onSuccess(String translatedText) {
                        runOnUiThread(() -> {
                            showProgress(false);
                            // 格式化翻译结果
                            String formattedTranslation = TranslationTextProcessor.formatTranslationResult(translatedText);
                            displayTranslationResult(formattedTranslation);
                            saveToHistory(processedText, formattedTranslation);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showProgress(false);
                            Log.e(TAG, "翻译失败: " + error);
                            Toast.makeText(TextTranslationActivity.this,
                                    error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void displayTranslationResult(String translatedText) {
        tvTranslationResult.setText(translatedText);
        cardTranslationResult.setVisibility(View.VISIBLE);
    }

    private void saveToHistory(String sourceText, String translatedText) {
        new Thread(() -> {
            try {
                TranslationHistoryEntity history = new TranslationHistoryEntity(
                        sourceText, translatedText, sourceLanguage, targetLanguage);
                // 使用Repository插入，会自动清理超出限制的旧记录
                historyRepository.insertHistory(history);
                
                // 【智能任务完成跟踪】每完成一次翻译，累计计数，达到目标自动完成任务
                TaskProgressTracker.getInstance(TextTranslationActivity.this).recordProgress("translation_practice", 1);
                
                runOnUiThread(() -> loadHistory());
            } catch (Exception e) {
                Log.e(TAG, "保存历史记录失败", e);
            }
        }).start();
    }

    private void loadHistory() {
        new Thread(() -> {
            try {
                // 主页面只显示最近的几条记录
                List<TranslationHistoryEntity> historyList = historyRepository.getRecentHistory(MAIN_PAGE_HISTORY_LIMIT);
                int totalCount = historyRepository.getTotalCount();
                
                runOnUiThread(() -> {
                    historyAdapter.setHistoryList(historyList);
                    tvEmptyHistory.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
                    rvHistory.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);
                    // 根据总数量决定是否显示"查看全部"按钮
                    btnViewAllHistory.setVisibility(totalCount > MAIN_PAGE_HISTORY_LIMIT ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "加载历史记录失败", e);
            }
        }).start();
    }
    
    /**
     * 显示历史记录底部弹出面板
     */
    private void showHistoryBottomSheet() {
        HistoryBottomSheetFragment bottomSheet = HistoryBottomSheetFragment.newInstance();
        bottomSheet.setOnHistorySelectedListener(history -> {
            // 恢复语言方向
            sourceLanguage = history.getSourceLanguage();
            targetLanguage = history.getTargetLanguage();
            updateLanguageDisplay();
            
            // 填充输入和结果
            etInputText.setText(history.getSourceText());
            tvTranslationResult.setText(history.getTranslatedText());
            cardTranslationResult.setVisibility(View.VISIBLE);
        });
        bottomSheet.show(getSupportFragmentManager(), "HistoryBottomSheet");
    }
    
    /**
     * 显示历史记录详情对话框
     * @param history 历史记录实体
     */
    private void showHistoryDetailDialog(TranslationHistoryEntity history) {
        HistoryDetailDialog dialog = HistoryDetailDialog.newInstance(history);
        dialog.setOnHistoryActionListener(new HistoryDetailDialog.OnHistoryActionListener() {
            @Override
            public void onUseHistory(TranslationHistoryEntity selectedHistory) {
                // 恢复语言方向
                sourceLanguage = selectedHistory.getSourceLanguage();
                targetLanguage = selectedHistory.getTargetLanguage();
                updateLanguageDisplay();
                
                // 填充输入和结果
                etInputText.setText(selectedHistory.getSourceText());
                tvTranslationResult.setText(selectedHistory.getTranslatedText());
                cardTranslationResult.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDeleteHistory(int historyId) {
                // 删除历史记录
                deleteHistoryById(historyId);
            }
        });
        dialog.show(getSupportFragmentManager(), "HistoryDetailDialog");
    }
    
    /**
     * 根据ID删除历史记录
     * @param historyId 历史记录ID
     */
    private void deleteHistoryById(int historyId) {
        new Thread(() -> {
            try {
                historyRepository.deleteHistory(historyId);
                runOnUiThread(() -> {
                    loadHistory();
                    Toast.makeText(TextTranslationActivity.this,
                            "已删除", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "删除历史记录失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(TextTranslationActivity.this,
                            "删除失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void copyTranslationResult() {
        String translatedText = tvTranslationResult.getText().toString();
        if (!TextUtils.isEmpty(translatedText)) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("translation", translatedText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        }
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清空历史记录")
                .setMessage("确定要清空所有翻译历史记录吗？")
                .setPositiveButton("确定", (dialog, which) -> clearHistory())
                .setNegativeButton("取消", null)
                .show();
    }

    private void clearHistory() {
        new Thread(() -> {
            try {
                historyRepository.deleteAllHistory();
                runOnUiThread(() -> {
                    loadHistory();
                    Toast.makeText(TextTranslationActivity.this,
                            "历史记录已清空", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "清空历史记录失败", e);
            }
        }).start();
    }

    private void requestVoiceInput() {
        // 检查录音权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startVoiceRecognition();
        }
    }

    private void startVoiceRecognition() {
        if (isListening) {
            stopVoiceRecognition();
            return;
        }

        // 尝试多种语音识别方式
        try {
            // 方式1：使用 SpeechRecognizer
            if (speechRecognizer == null) {
                try {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                    speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        isListening = true;
                        runOnUiThread(() -> {
                            Toast.makeText(TextTranslationActivity.this,
                                "请开始说话...", Toast.LENGTH_SHORT).show();
                            fabVoiceInput.setImageResource(R.drawable.ic_mic_recording);
                        });
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        Log.d(TAG, "开始说话");
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                        // 音量变化
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                        Log.d(TAG, "结束说话");
                    }

                    @Override
                    public void onError(int error) {
                        isListening = false;
                        runOnUiThread(() -> {
                            fabVoiceInput.setImageResource(R.drawable.ic_mic);
                            String errorMessage = getErrorMessage(error);
                            Toast.makeText(TextTranslationActivity.this,
                                errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResults(Bundle results) {
                        isListening = false;
                        ArrayList<String> matches = results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            String recognizedText = matches.get(0);
                            runOnUiThread(() -> {
                                fabVoiceInput.setImageResource(R.drawable.ic_mic);
                                etInputText.setText(recognizedText);
                                // 自动触发翻译
                                performTranslation();
                            });
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        // 部分结果
                    }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
                    });

                    // 设置识别参数
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                // 根据当前源语言设置识别语言
                String languageCode = LANG_ENGLISH.equals(sourceLanguage)
                        ? "en-US" : "zh-CN";
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode);
                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

                    // 开始识别
                    speechRecognizer.startListening(intent);
                    return; // 成功启动，返回

                } catch (Exception e) {
                    Log.e(TAG, "SpeechRecognizer 设置监听器失败", e);
                    speechRecognizer = null;
                    throw e; // 重新抛出异常，让外层try-catch处理
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "SpeechRecognizer 创建失败，尝试其他方式", e);
            speechRecognizer = null;
        }

        // 方式2：尝试使用Intent启动语音识别
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

            // 根据当前源语言设置识别语言
            String languageCode = LANG_ENGLISH.equals(sourceLanguage)
                    ? "en-US" : "zh-CN";
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请开始说话...");

            // 启动外部语音识别应用
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
            return;
        } catch (Exception e) {
            Log.e(TAG, "Intent语音识别失败", e);
        }

        // 如果都失败了，才显示不支持提示
        Toast.makeText(this, "您的设备不支持语音识别功能", Toast.LENGTH_LONG).show();
    }

    private void stopVoiceRecognition() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            fabVoiceInput.setImageResource(R.drawable.ic_mic);
        }
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "音频错误";
            case SpeechRecognizer.ERROR_CLIENT:
                return "客户端错误";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "权限不足";
            case SpeechRecognizer.ERROR_NETWORK:
                return "网络错误";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "网络超时";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "没有识别到语音，请重试";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "识别服务忙";
            case SpeechRecognizer.ERROR_SERVER:
                return "服务器错误";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "没有语音输入";
            default:
                return "识别失败，请重试";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予后，启动语音识别
                startVoiceRecognition();
            } else {
                Toast.makeText(this, "需要录音权限才能使用语音输入", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭翻译服务，释放线程池资源
        if (translationService != null) {
            translationService.shutdown();
            translationService = null;
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停时停止语音识别
        stopVoiceRecognition();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    String recognizedText = results.get(0);
                    etInputText.setText(recognizedText);
                    // 自动触发翻译
                    performTranslation();
                }
            }
        }
    }
}

