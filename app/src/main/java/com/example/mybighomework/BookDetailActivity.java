package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.WordPreviewAdapter;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.BookEntity;
import com.example.mybighomework.database.entity.DictionaryWordEntity;
import com.example.mybighomework.database.repository.BookRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词书详情页面
 * 显示词书信息、学习进度、单词预览
 */
public class BookDetailActivity extends AppCompatActivity {

    // UI组件
    private ImageView btnBack;
    private TextView tvTitle, tvBookName, tvBookInfo, tvWordCount;
    private TextView tvLearnedCount, tvMasteredCount, tvReviewCount;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private RecyclerView rvWordPreview;
    private Button btnStartLearn, btnReview, btnViewAll;
    private LinearLayout layoutLoading;
    private ScrollView layoutContent;

    // 数据
    private String bookId;
    private String bookName;
    private int wordCount;
    private BookRepository repository;
    private ExecutorService executor;
    private WordPreviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // 获取传入参数
        bookId = getIntent().getStringExtra("book_id");
        bookName = getIntent().getStringExtra("book_name");
        wordCount = getIntent().getIntExtra("word_count", 0);

        if (bookId == null) {
            Toast.makeText(this, "词书ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initData();
        setupClickListeners();
        loadBookDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        tvBookName = findViewById(R.id.tv_book_name);
        tvBookInfo = findViewById(R.id.tv_book_info);
        tvWordCount = findViewById(R.id.tv_word_count);
        tvLearnedCount = findViewById(R.id.tv_learned_count);
        tvMasteredCount = findViewById(R.id.tv_mastered_count);
        tvReviewCount = findViewById(R.id.tv_review_count);
        progressBar = findViewById(R.id.progress_bar);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        rvWordPreview = findViewById(R.id.rv_word_preview);
        btnStartLearn = findViewById(R.id.btn_start_learn);
        btnReview = findViewById(R.id.btn_review);
        btnViewAll = findViewById(R.id.btn_view_all);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutContent = findViewById(R.id.layout_content);

        // 设置标题
        tvTitle.setText(bookName != null ? bookName : "词书详情");
    }

    private void initData() {
        AppDatabase database = AppDatabase.getInstance(this);
        repository = new BookRepository(database);
        executor = Executors.newSingleThreadExecutor();
        
        adapter = new WordPreviewAdapter();
        rvWordPreview.setLayoutManager(new LinearLayoutManager(this));
        rvWordPreview.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnStartLearn.setOnClickListener(v -> startLearning());

        btnReview.setOnClickListener(v -> startReview());

        btnViewAll.setOnClickListener(v -> viewAllWords());
    }

    private void loadBookDetail() {
        showLoading();
        
        executor.execute(() -> {
            try {
                // 加载词书详情
                BookEntity book = repository.getBookByIdSync(bookId);
                
                // 加载单词预览（前20个）
                List<DictionaryWordEntity> words = repository.getWordsForBookSync(bookId);
                List<DictionaryWordEntity> previewWords = words.size() > 20 
                    ? words.subList(0, 20) : words;

                // 加载学习进度
                // TODO: 从进度表获取实际进度
                int learnedCount = 0;
                int masteredCount = 0;
                int reviewCount = 0;

                final BookEntity finalBook = book;
                final List<DictionaryWordEntity> finalPreviewWords = previewWords;
                final int finalLearnedCount = learnedCount;
                final int finalMasteredCount = masteredCount;
                final int finalReviewCount = reviewCount;
                final int totalWords = words.size();

                runOnUiThread(() -> {
                    hideLoading();
                    updateUI(finalBook, finalPreviewWords, totalWords, 
                            finalLearnedCount, finalMasteredCount, finalReviewCount);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUI(BookEntity book, List<DictionaryWordEntity> previewWords, 
                         int totalWords, int learnedCount, int masteredCount, int reviewCount) {
        if (book != null) {
            tvBookName.setText(book.getName());
            
            // 构建词书信息
            StringBuilder info = new StringBuilder();
            if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                info.append("作者: ").append(book.getAuthor());
            }
            if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
                if (info.length() > 0) info.append(" | ");
                info.append(book.getPublisher());
            }
            if (book.getComment() != null && !book.getComment().isEmpty()) {
                if (info.length() > 0) info.append("\n");
                info.append(book.getComment());
            }
            tvBookInfo.setText(info.length() > 0 ? info.toString() : "暂无描述");
        }

        // 更新统计
        tvWordCount.setText(String.valueOf(totalWords));
        tvLearnedCount.setText(String.valueOf(learnedCount));
        tvMasteredCount.setText(String.valueOf(masteredCount));
        tvReviewCount.setText(String.valueOf(reviewCount));

        // 更新进度
        int percent = totalWords > 0 ? (learnedCount * 100 / totalWords) : 0;
        progressBar.setProgress(percent);
        tvProgressPercent.setText(percent + "%");

        // 更新单词预览
        adapter.setWords(previewWords);

        // 更新按钮状态
        if (reviewCount > 0) {
            btnReview.setText("复习 (" + reviewCount + ")");
            btnReview.setEnabled(true);
        } else {
            btnReview.setText("暂无复习");
            btnReview.setEnabled(false);
        }
    }

    private void startLearning() {
        android.util.Log.d("BookDetailActivity", "startLearning: bookId=" + bookId + ", bookName=" + bookName);
        Intent intent = new Intent(this, VocabularyActivity.class);
        intent.putExtra(VocabularyActivity.EXTRA_SOURCE_TYPE, VocabularyActivity.SOURCE_TYPE_BOOK);
        intent.putExtra(VocabularyActivity.EXTRA_BOOK_ID, bookId);
        intent.putExtra(VocabularyActivity.EXTRA_BOOK_NAME, bookName);
        intent.putExtra(VocabularyActivity.EXTRA_MODE, "learn");
        android.util.Log.d("BookDetailActivity", "传递参数: EXTRA_SOURCE_TYPE=" + VocabularyActivity.SOURCE_TYPE_BOOK);
        startActivity(intent);
    }

    private void startReview() {
        Intent intent = new Intent(this, VocabularyActivity.class);
        intent.putExtra(VocabularyActivity.EXTRA_SOURCE_TYPE, VocabularyActivity.SOURCE_TYPE_BOOK);
        intent.putExtra(VocabularyActivity.EXTRA_BOOK_ID, bookId);
        intent.putExtra(VocabularyActivity.EXTRA_BOOK_NAME, bookName);
        intent.putExtra(VocabularyActivity.EXTRA_MODE, "review");
        startActivity(intent);
    }

    private void viewAllWords() {
        // TODO: 跳转到单词列表页面
        Toast.makeText(this, "查看全部单词功能开发中", Toast.LENGTH_SHORT).show();
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);
    }

    private void hideLoading() {
        layoutLoading.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
