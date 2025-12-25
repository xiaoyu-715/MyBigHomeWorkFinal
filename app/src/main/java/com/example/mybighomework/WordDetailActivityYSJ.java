package com.example.mybighomework;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.ExampleSentenceAdapterYSJ;
import com.example.mybighomework.database.entity.DictionaryWordEntity;
import com.example.mybighomework.viewmodel.WordSearchViewModelYSJ;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * 单词详情Activity
 * 展示单词的详细信息和例句
 */
public class WordDetailActivityYSJ extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private WordSearchViewModelYSJ viewModel;
    private ExampleSentenceAdapterYSJ exampleAdapter;
    
    private TextView tvWord;
    private TextView tvPhoneticUk;
    private TextView tvPhoneticUs;
    private TextView tvTranslation;
    private ImageButton btnSpeakUk;
    private ImageButton btnSpeakUs;
    private ImageButton btnCollect;
    private RecyclerView rvExamples;
    private TextView tvNoExamples;
    private MaterialButton btnAddToVocabulary;
    private MaterialButton btnCopy;
    
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    
    private String wordId;
    private String currentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail_ysj);
        
        wordId = getIntent().getStringExtra("word_id");
        currentWord = getIntent().getStringExtra("word");
        
        initViews();
        initViewModel();
        initTTS();
        setupListeners();
        observeData();
        
        // 加载单词详情
        if (currentWord != null && !currentWord.isEmpty()) {
            viewModel.getWordDetail(currentWord);
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("单词详情");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        tvWord = findViewById(R.id.tv_word);
        tvPhoneticUk = findViewById(R.id.tv_phonetic_uk);
        tvPhoneticUs = findViewById(R.id.tv_phonetic_us);
        tvTranslation = findViewById(R.id.tv_translation);
        btnSpeakUk = findViewById(R.id.btn_speak_uk);
        btnSpeakUs = findViewById(R.id.btn_speak_us);
        btnCollect = findViewById(R.id.btn_collect);
        rvExamples = findViewById(R.id.rv_examples);
        tvNoExamples = findViewById(R.id.tv_no_examples);
        btnAddToVocabulary = findViewById(R.id.btn_add_to_vocabulary);
        btnCopy = findViewById(R.id.btn_copy);
        
        // 设置例句RecyclerView
        exampleAdapter = new ExampleSentenceAdapterYSJ();
        rvExamples.setLayoutManager(new LinearLayoutManager(this));
        rvExamples.setAdapter(exampleAdapter);
        rvExamples.setNestedScrollingEnabled(false);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(WordSearchViewModelYSJ.class);
        
        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("current_user_id", "default");
        viewModel.setCurrentUserId(userId);
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA && 
                         result != TextToSpeech.LANG_NOT_SUPPORTED);
        }
    }

    private void setupListeners() {
        // 英式发音
        btnSpeakUk.setOnClickListener(v -> {
            if (isTtsReady) {
                textToSpeech.setLanguage(Locale.UK);
                speakWord(currentWord);
            }
        });
        
        // 美式发音
        btnSpeakUs.setOnClickListener(v -> {
            if (isTtsReady) {
                textToSpeech.setLanguage(Locale.US);
                speakWord(currentWord);
            }
        });
        
        // 收藏按钮
        btnCollect.setOnClickListener(v -> {
            viewModel.toggleCollection();
        });
        
        // 加入生词本
        btnAddToVocabulary.setOnClickListener(v -> {
            viewModel.collectWordWithNote(null);
            Toast.makeText(this, "已加入生词本", Toast.LENGTH_SHORT).show();
        });
        
        // 复制
        btnCopy.setOnClickListener(v -> {
            copyToClipboard();
        });
        
        // 例句朗读
        exampleAdapter.setOnSpeakClickListener(sentence -> {
            if (isTtsReady) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.speak(sentence.getEnglishSentence(), 
                    TextToSpeech.QUEUE_FLUSH, null, "sentence_speak");
            }
        });
    }

    private void observeData() {
        // 单词详情
        viewModel.getSelectedWord().observe(this, word -> {
            if (word != null) {
                displayWordDetails(word);
            }
        });
        
        // 例句
        viewModel.getCurrentExamples().observe(this, examples -> {
            if (examples == null || examples.isEmpty()) {
                rvExamples.setVisibility(View.GONE);
                tvNoExamples.setVisibility(View.VISIBLE);
            } else {
                rvExamples.setVisibility(View.VISIBLE);
                tvNoExamples.setVisibility(View.GONE);
                exampleAdapter.submitList(examples);
            }
        });
        
        // 收藏状态
        viewModel.getIsCollected().observe(this, isCollected -> {
            updateCollectButton(isCollected);
        });
        
        // 错误信息
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }

    private void displayWordDetails(DictionaryWordEntity word) {
        currentWord = word.getWord();
        tvWord.setText(word.getWord());
        
        // 音标
        String phoneticUk = word.getPhoneticUk();
        String phoneticUs = word.getPhoneticUs();
        
        tvPhoneticUk.setText(phoneticUk != null ? phoneticUk : "");
        tvPhoneticUs.setText(phoneticUs != null ? phoneticUs : "");
        
        // 翻译
        tvTranslation.setText(word.getTranslation());
    }

    private void updateCollectButton(boolean isCollected) {
        if (isCollected) {
            btnCollect.setImageResource(R.drawable.ic_favorite);
            btnAddToVocabulary.setText("已在生词本");
            btnAddToVocabulary.setEnabled(false);
        } else {
            btnCollect.setImageResource(R.drawable.ic_favorite_border);
            btnAddToVocabulary.setText("加入生词本");
            btnAddToVocabulary.setEnabled(true);
        }
    }

    private void speakWord(String word) {
        if (isTtsReady && word != null && !word.isEmpty()) {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_speak");
        } else {
            Toast.makeText(this, "语音功能暂不可用", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard() {
        DictionaryWordEntity word = viewModel.getSelectedWord().getValue();
        if (word != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(word.getWord()).append("\n");
            if (word.getPhoneticUk() != null) {
                sb.append("UK: ").append(word.getPhoneticUk()).append("\n");
            }
            if (word.getPhoneticUs() != null) {
                sb.append("US: ").append(word.getPhoneticUs()).append("\n");
            }
            sb.append(word.getTranslation());
            
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("word", sb.toString());
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
