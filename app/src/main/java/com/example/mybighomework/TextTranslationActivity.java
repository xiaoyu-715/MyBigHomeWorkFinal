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
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.TranslationHistoryDao;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

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
    private EditText etInputText;
    private Button btnTranslate;
    private CardView cardTranslationResult;
    private RecyclerView rvHistory;
    private FloatingActionButton fabVoiceInput;
    private ProgressBar progressBar;

    // 数据相关
    private TranslationHistoryDao historyDao;
    private TranslationHistoryAdapter historyAdapter;

    // ML Kit 翻译器
    private Translator translator;
    private String sourceLanguage = TranslateLanguage.ENGLISH;
    private String targetLanguage = TranslateLanguage.CHINESE;
    private boolean isModelDownloading = false;

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
        initTranslator();
        setupClickListeners();
        loadHistory();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSwitchLanguage = findViewById(R.id.btn_switch_language);
        btnClearInput = findViewById(R.id.btn_clear_input);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        btnCopyResult = findViewById(R.id.btn_copy_result);
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
    }

    private void initTranslator() {
        downloadTranslationModel();
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

        fabVoiceInput.setOnClickListener(v -> requestVoiceInput());

        // 历史记录点击事件
        historyAdapter.setOnItemClickListener(history -> {
            etInputText.setText(history.getSourceText());
            tvTranslationResult.setText(history.getTranslatedText());
            cardTranslationResult.setVisibility(View.VISIBLE);
        });
    }

    private void downloadTranslationModel() {
        if (isModelDownloading) {
            return;
        }

        isModelDownloading = true;
        showProgress(true);

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build();

        if (translator != null) {
            translator.close();
        }
        translator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        isModelDownloading = false;
                        showProgress(false);
                        Log.d(TAG, "翻译模型下载成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isModelDownloading = false;
                        showProgress(false);
                        Log.e(TAG, "翻译模型下载失败", e);
                        Toast.makeText(TextTranslationActivity.this,
                                "翻译模型下载失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void switchLanguage() {
        // 切换源语言和目标语言
        String temp = sourceLanguage;
        sourceLanguage = targetLanguage;
        targetLanguage = temp;

        updateLanguageDisplay();
        downloadTranslationModel();

        // 清空输入和结果
        etInputText.setText("");
        cardTranslationResult.setVisibility(View.GONE);
    }

    private void updateLanguageDisplay() {
        String sourceLangText = TranslateLanguage.ENGLISH.equals(sourceLanguage) ? "英文" : "中文";
        String targetLangText = TranslateLanguage.ENGLISH.equals(targetLanguage) ? "英文" : "中文";
        
        tvSourceLanguage.setText(sourceLangText);
        tvTargetLanguage.setText(targetLangText);
    }

    private void performTranslation() {
        String inputText = etInputText.getText().toString().trim();
        
        if (TextUtils.isEmpty(inputText)) {
            Toast.makeText(this, "请输入要翻译的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (translator == null || isModelDownloading) {
            Toast.makeText(this, "翻译模型正在下载中，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }

        // 预处理输入文本，优化翻译质量
        String processedText = TranslationTextProcessor.preprocessText(inputText);

        showProgress(true);

        translator.translate(processedText)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String translatedText) {
                        showProgress(false);
                        // 格式化翻译结果
                        String formattedTranslation = TranslationTextProcessor.formatTranslationResult(translatedText);
                        displayTranslationResult(formattedTranslation);
                        saveToHistory(processedText, formattedTranslation);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showProgress(false);
                        Log.e(TAG, "翻译失败", e);
                        Toast.makeText(TextTranslationActivity.this,
                                "翻译失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                historyDao.insert(history);
                
                runOnUiThread(() -> loadHistory());
            } catch (Exception e) {
                Log.e(TAG, "保存历史记录失败", e);
            }
        }).start();
    }

    private void loadHistory() {
        new Thread(() -> {
            try {
                List<TranslationHistoryEntity> historyList = historyDao.getRecent(50);
                runOnUiThread(() -> {
                    historyAdapter.setHistoryList(historyList);
                    tvEmptyHistory.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
                    rvHistory.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);
                });
            } catch (Exception e) {
                Log.e(TAG, "加载历史记录失败", e);
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
                historyDao.deleteAll();
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
                String languageCode = TranslateLanguage.ENGLISH.equals(sourceLanguage)
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
            String languageCode = TranslateLanguage.ENGLISH.equals(sourceLanguage)
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
        if (translator != null) {
            translator.close();
            translator = null;
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

