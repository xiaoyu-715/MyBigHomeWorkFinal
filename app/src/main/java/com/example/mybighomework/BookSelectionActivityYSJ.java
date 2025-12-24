package com.example.mybighomework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.BookSelectionAdapterYSJ;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.BookEntity;
import com.example.mybighomework.database.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词书选择界面
 * 用于词汇训练时选择学习的词书
 */
public class BookSelectionActivityYSJ extends AppCompatActivity {
    
    private static final String PREFS_NAME = "VocabularyTraining";
    private static final String KEY_LAST_BOOK_ID = "last_book_id";
    private static final String KEY_LAST_BOOK_NAME = "last_book_name";
    
    private ImageView btnBack;
    private TextView tvTitle;
    private RecyclerView rvBooks;
    private BookSelectionAdapterYSJ adapter;
    
    private BookRepository repository;
    private ExecutorService executor;
    private List<BookEntity> bookList = new ArrayList<>();
    private String categoryId;
    private String categoryName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_selection);
        
        categoryId = getIntent().getStringExtra("category_id");
        categoryName = getIntent().getStringExtra("category_name");
        
        initViews();
        initData();
        setupClickListeners();
        loadBooks();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        rvBooks = findViewById(R.id.rv_books);
        
        if (categoryName != null && !categoryName.isEmpty()) {
            tvTitle.setText(categoryName);
        } else {
            tvTitle.setText("选择词书");
        }
        
        rvBooks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookSelectionAdapterYSJ(this, bookList, this::onBookSelected);
        rvBooks.setAdapter(adapter);
    }
    
    private void initData() {
        AppDatabase database = AppDatabase.getInstance(this);
        repository = new BookRepository(database);
        executor = Executors.newSingleThreadExecutor();
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
    
    private void loadBooks() {
        executor.execute(() -> {
            try {
                List<BookEntity> books;
                if (categoryId != null && !categoryId.isEmpty()) {
                    books = repository.getChildBooksSync(categoryId);
                } else {
                    books = repository.getAllLearnableBooksSync();
                }
                
                runOnUiThread(() -> {
                    if (books != null && !books.isEmpty()) {
                        bookList.clear();
                        bookList.addAll(books);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "暂无可用词书", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载词书失败: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void onBookSelected(BookEntity book) {
        saveLastSelectedBook(book.getId(), book.getName());
        
        Intent intent = new Intent(this, VocabularyActivity.class);
        intent.putExtra(VocabularyActivity.EXTRA_SOURCE_TYPE, VocabularyActivity.SOURCE_TYPE_BOOK);
        intent.putExtra(VocabularyActivity.EXTRA_BOOK_ID, book.getId());
        intent.putExtra(VocabularyActivity.EXTRA_BOOK_NAME, book.getName());
        intent.putExtra(VocabularyActivity.EXTRA_MODE, "learn");
        startActivity(intent);
    }
    
    private void saveLastSelectedBook(String bookId, String bookName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_LAST_BOOK_ID, bookId)
            .putString(KEY_LAST_BOOK_NAME, bookName)
            .apply();
    }
    
    public static String getLastSelectedBookId(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_LAST_BOOK_ID, null);
    }
    
    public static String getLastSelectedBookName(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_LAST_BOOK_NAME, null);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
