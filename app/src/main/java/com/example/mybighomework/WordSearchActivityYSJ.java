package com.example.mybighomework;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.WordSearchResultAdapterYSJ;
import com.example.mybighomework.database.entity.DictionaryWordEntity;
import com.example.mybighomework.viewmodel.WordSearchViewModelYSJ;

import java.util.Locale;

/**
 * 单词搜索Activity
 * 提供单词搜索和结果展示功能
 */
public class WordSearchActivityYSJ extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private WordSearchViewModelYSJ viewModel;
    private WordSearchResultAdapterYSJ adapter;
    
    private EditText etSearch;
    private ImageButton btnClear;
    private RecyclerView rvSearchResults;
    private View layoutEmpty;
    private View layoutHistory;
    private TextView tvEmptyHint;
    private ProgressBar progressBar;
    
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_search_ysj);
        
        initViews();
        initViewModel();
        initTTS();
        setupListeners();
        observeData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        etSearch = findViewById(R.id.et_search);
        btnClear = findViewById(R.id.btn_clear);
        rvSearchResults = findViewById(R.id.rv_search_results);
        layoutEmpty = findViewById(R.id.layout_empty);
        layoutHistory = findViewById(R.id.layout_history);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);
        progressBar = findViewById(R.id.progress_bar);
        
        // 设置RecyclerView
        adapter = new WordSearchResultAdapterYSJ();
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);
        
        // 初始显示空状态
        showEmptyState("输入单词开始搜索");
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(WordSearchViewModelYSJ.class);
        
        // 获取用户ID
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
        // 搜索框文本变化监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                if (s.length() == 0) {
                    showEmptyState("输入单词开始搜索");
                    adapter.submitList(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 实时搜索（防抖）
                etSearch.removeCallbacks(searchRunnable);
                if (s.length() > 0) {
                    etSearch.postDelayed(searchRunnable, 300);
                }
            }
        });
        
        // 搜索按钮点击（软键盘搜索）
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                hideKeyboard();
                return true;
            }
            return false;
        });
        
        // 清除按钮
        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
            showKeyboard();
        });
        
        // 列表项点击
        adapter.setOnItemClickListener(word -> {
            openWordDetail(word);
        });
        
        // 朗读按钮
        adapter.setOnSpeakClickListener(word -> {
            speakWord(word.getWord());
        });
        
        // 清除历史
        TextView tvClearHistory = findViewById(R.id.tv_clear_history);
        if (tvClearHistory != null) {
            tvClearHistory.setOnClickListener(v -> {
                viewModel.clearSearchHistory();
                Toast.makeText(this, "搜索历史已清除", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private final Runnable searchRunnable = () -> performSearch();

    private void performSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (!keyword.isEmpty()) {
            viewModel.searchWords(keyword);
        }
    }

    private void observeData() {
        // 搜索结果
        viewModel.getSearchResults().observe(this, results -> {
            if (results == null || results.isEmpty()) {
                String keyword = etSearch.getText().toString().trim();
                if (keyword.isEmpty()) {
                    showEmptyState("输入单词开始搜索");
                } else {
                    showEmptyState("未找到 \"" + keyword + "\" 相关单词");
                }
                adapter.submitList(null);
            } else {
                hideEmptyState();
                adapter.submitList(results);
            }
        });
        
        // 加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // 错误信息
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }

    private void showEmptyState(String hint) {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        tvEmptyHint.setText(hint);
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    private void openWordDetail(DictionaryWordEntity word) {
        Intent intent = new Intent(this, WordDetailActivityYSJ.class);
        intent.putExtra("word_id", word.getId());
        intent.putExtra("word", word.getWord());
        startActivity(intent);
    }

    private void speakWord(String word) {
        if (isTtsReady && word != null && !word.isEmpty()) {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_speak");
        } else {
            Toast.makeText(this, "语音功能暂不可用", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
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
