package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.BookCategoryAdapterYSJ;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.BookEntity;
import com.example.mybighomework.database.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词书分类导航界面
 * 显示词书的顶级分类,用户选择分类后进入该分类的词书列表
 */
public class BookCategoryActivityYSJ extends AppCompatActivity {
    
    private ImageView btnBack;
    private TextView tvTitle;
    private RecyclerView rvCategories;
    private BookCategoryAdapterYSJ adapter;
    
    private BookRepository repository;
    private ExecutorService executor;
    private List<BookEntity> categoryList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_category);
        
        initViews();
        initData();
        setupClickListeners();
        loadCategories();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        rvCategories = findViewById(R.id.rv_categories);
        
        tvTitle.setText("选择分类");
        
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new BookCategoryAdapterYSJ(this, categoryList, this::onCategorySelected);
        rvCategories.setAdapter(adapter);
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
    
    private void loadCategories() {
        executor.execute(() -> {
            try {
                List<BookEntity> categories = repository.getTopLevelBooksSync();
                
                runOnUiThread(() -> {
                    if (categories != null && !categories.isEmpty()) {
                        categoryList.clear();
                        categoryList.addAll(categories);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "暂无分类", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载分类失败: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void onCategorySelected(BookEntity category) {
        Intent intent = new Intent(this, BookSelectionActivityYSJ.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
