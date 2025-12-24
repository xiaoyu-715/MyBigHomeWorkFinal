package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.adapter.BookAdapter;
import com.example.mybighomework.database.entity.BookEntity;
import com.example.mybighomework.viewmodel.BookViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 单词书浏览Activity
 * 支持层级导航、搜索、学习进度显示
 */
public class VocabularyBookActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    // UI组件
    private ImageView btnBack, btnSearch;
    private TextView tvTitle;
    private LinearLayout layoutSearch;
    private EditText etSearch;
    private TextView btnCancelSearch;
    private LinearLayout layoutBreadcrumb;
    private TextView tvBreadcrumbRoot;
    private TextView tvBookCount, tvTotalWords;
    private RecyclerView rvBooks;
    private LinearLayout layoutLoading, layoutEmpty;
    private TextView tvEmptyMessage, tvEmptyHint;

    // ViewModel和适配器
    private BookViewModel viewModel;
    private BookAdapter adapter;

    // 导航状态
    private Stack<BreadcrumbItem> navigationStack = new Stack<>();
    private boolean isSearchMode = false;

    // 面包屑项
    private static class BreadcrumbItem {
        String id;
        String name;

        BreadcrumbItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_book);

        initViews();
        initViewModel();
        setupClickListeners();
        setupRecyclerView();
        loadRootBooks();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSearch = findViewById(R.id.btn_search);
        tvTitle = findViewById(R.id.tv_title);
        layoutSearch = findViewById(R.id.layout_search);
        etSearch = findViewById(R.id.et_search);
        btnCancelSearch = findViewById(R.id.btn_cancel_search);
        layoutBreadcrumb = findViewById(R.id.layout_breadcrumb);
        tvBreadcrumbRoot = findViewById(R.id.tv_breadcrumb_root);
        tvBookCount = findViewById(R.id.tv_book_count);
        tvTotalWords = findViewById(R.id.tv_total_words);
        rvBooks = findViewById(R.id.rv_books);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(BookViewModel.class);

        // 观察词书列表
        viewModel.getBooks().observe(this, books -> {
            hideLoading();
            if (books == null || books.isEmpty()) {
                showEmpty("暂无词书数据", "请等待数据导入完成");
            } else {
                hideEmpty();
                adapter.setBooks(books);
                updateStats(books);
            }
        });

        // 观察搜索结果
        viewModel.getSearchResults().observe(this, results -> {
            hideLoading();
            if (results == null || results.isEmpty()) {
                showEmpty("未找到匹配的词书", "请尝试其他关键词");
            } else {
                hideEmpty();
                adapter.setBooks(results);
                tvBookCount.setText("找到 " + results.size() + " 本词书");
            }
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });
    }


    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> onBackPressed());

        // 搜索按钮
        btnSearch.setOnClickListener(v -> toggleSearchMode(true));

        // 取消搜索
        btnCancelSearch.setOnClickListener(v -> toggleSearchMode(false));

        // 根目录点击
        tvBreadcrumbRoot.setOnClickListener(v -> navigateToRoot());

        // 搜索输入
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    viewModel.searchBooks(query);
                } else if (query.isEmpty() && isSearchMode) {
                    // 清空搜索时显示当前层级
                    loadCurrentLevel();
                }
            }
        });

        // 搜索键盘确认
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    viewModel.searchBooks(query);
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new BookAdapter(this);
        rvBooks.setLayoutManager(new LinearLayoutManager(this));
        rvBooks.setAdapter(adapter);
    }

    private void loadRootBooks() {
        showLoading();
        navigationStack.clear();
        updateBreadcrumb();
        viewModel.loadBooksByParentId("0");
    }

    private void loadCurrentLevel() {
        String parentId = navigationStack.isEmpty() ? "0" : navigationStack.peek().id;
        viewModel.loadBooksByParentId(parentId);
    }

    @Override
    public void onBookClick(BookEntity book) {
        if (book.getLevel() == 1) {
            // 分类，进入子级
            navigateToCategory(book);
        } else {
            // 词书，进入详情/学习
            openBookDetail(book);
        }
    }

    @Override
    public void onBookLongClick(BookEntity book) {
        // 长按显示词书信息
        showBookInfo(book);
    }

    private void navigateToCategory(BookEntity category) {
        showLoading();
        navigationStack.push(new BreadcrumbItem(category.getId(), category.getName()));
        updateBreadcrumb();
        viewModel.loadBooksByParentId(category.getId());
    }

    private void navigateToRoot() {
        if (isSearchMode) {
            toggleSearchMode(false);
        }
        loadRootBooks();
    }

    private void navigateToBreadcrumb(int index) {
        // 移除index之后的所有项
        while (navigationStack.size() > index + 1) {
            navigationStack.pop();
        }
        updateBreadcrumb();
        loadCurrentLevel();
    }

    private void updateBreadcrumb() {
        // 清除动态添加的面包屑项（保留根目录）
        int childCount = layoutBreadcrumb.getChildCount();
        if (childCount > 1) {
            layoutBreadcrumb.removeViews(1, childCount - 1);
        }

        // 添加导航路径
        for (int i = 0; i < navigationStack.size(); i++) {
            BreadcrumbItem item = navigationStack.get(i);
            final int index = i;

            // 添加分隔符
            TextView separator = new TextView(this);
            separator.setText(" > ");
            separator.setTextSize(14);
            separator.setTextColor(0xFF999999);
            layoutBreadcrumb.addView(separator);

            // 添加路径项
            TextView pathItem = new TextView(this);
            pathItem.setText(item.name);
            pathItem.setTextSize(14);
            pathItem.setPadding(0, 8, 0, 8);
            
            if (i == navigationStack.size() - 1) {
                // 当前位置，不可点击
                pathItem.setTextColor(0xFF333333);
            } else {
                // 可点击返回
                pathItem.setTextColor(0xFF2196F3);
                pathItem.setOnClickListener(v -> navigateToBreadcrumb(index));
            }
            
            layoutBreadcrumb.addView(pathItem);
        }

        // 更新标题
        if (navigationStack.isEmpty()) {
            tvTitle.setText("单词书");
        } else {
            tvTitle.setText(navigationStack.peek().name);
        }
    }

    private void toggleSearchMode(boolean enable) {
        isSearchMode = enable;
        if (enable) {
            layoutSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            // 显示软键盘
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            layoutSearch.setVisibility(View.GONE);
            etSearch.setText("");
            // 隐藏软键盘
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
            // 恢复当前层级显示
            loadCurrentLevel();
        }
    }

    private void openBookDetail(BookEntity book) {
        // TODO: 实现词书详情/学习页面
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("book_id", book.getId());
        intent.putExtra("book_name", book.getName());
        intent.putExtra("word_count", book.getItemNum());
        startActivity(intent);
    }

    private void showBookInfo(BookEntity book) {
        StringBuilder info = new StringBuilder();
        info.append("词书名称: ").append(book.getName()).append("\n");
        if (book.getFullName() != null && !book.getFullName().isEmpty()) {
            info.append("完整名称: ").append(book.getFullName()).append("\n");
        }
        info.append("单词数量: ").append(book.getItemNum()).append("\n");
        if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
            info.append("作者: ").append(book.getAuthor()).append("\n");
        }
        if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
            info.append("出版社: ").append(book.getPublisher()).append("\n");
        }
        if (book.getComment() != null && !book.getComment().isEmpty()) {
            info.append("描述: ").append(book.getComment());
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("词书信息")
            .setMessage(info.toString())
            .setPositiveButton("确定", null)
            .show();
    }

    private void updateStats(List<BookEntity> books) {
        int bookCount = 0;
        int totalWords = 0;
        int categoryCount = 0;

        for (BookEntity book : books) {
            if (book.getLevel() == 1) {
                categoryCount++;
            } else {
                bookCount++;
            }
            totalWords += book.getItemNum();
        }

        if (categoryCount > 0 && bookCount == 0) {
            tvBookCount.setText("共 " + categoryCount + " 个分类");
        } else if (bookCount > 0 && categoryCount == 0) {
            tvBookCount.setText("共 " + bookCount + " 本词书");
        } else {
            tvBookCount.setText(categoryCount + " 个分类, " + bookCount + " 本词书");
        }
        tvTotalWords.setText("总计 " + totalWords + " 个单词");
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvBooks.setVisibility(View.GONE);
    }

    private void hideLoading() {
        layoutLoading.setVisibility(View.GONE);
        rvBooks.setVisibility(View.VISIBLE);
    }

    private void showEmpty(String message, String hint) {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvBooks.setVisibility(View.GONE);
        tvEmptyMessage.setText(message);
        tvEmptyHint.setText(hint);
    }

    private void hideEmpty() {
        layoutEmpty.setVisibility(View.GONE);
        rvBooks.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (isSearchMode) {
            toggleSearchMode(false);
        } else if (!navigationStack.isEmpty()) {
            navigationStack.pop();
            updateBreadcrumb();
            loadCurrentLevel();
        } else {
            super.onBackPressed();
        }
    }
}
