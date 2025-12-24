package com.example.mybighomework.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.adapter.HistoryFullAdapter;
import com.example.mybighomework.adapter.SwipeToDeleteCallback;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;
import com.example.mybighomework.dialog.HistoryDetailDialog;
import com.example.mybighomework.repository.TranslationHistoryRepository;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 历史记录底部弹出面板Fragment
 * 提供完整的历史记录浏览、搜索和删除功能
 * 
 * Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 6.1
 */
public class HistoryBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "HistoryBottomSheet";
    
    // UI组件
    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private ProgressBar progressLoading;
    private EditText etSearch;
    private ImageButton btnClearSearch;
    private ImageButton btnClose;
    
    // 数据相关
    private HistoryFullAdapter adapter;
    private TranslationHistoryRepository repository;
    private ExecutorService executor;
    private Handler mainHandler;
    
    // 分页状态
    private int currentPage = 0;
    private boolean isLoading = false;
    private String currentSearchKeyword = "";
    
    // 搜索防抖
    private static final long SEARCH_DEBOUNCE_DELAY = 300;
    private Runnable searchRunnable;
    
    // 回调接口
    private OnHistorySelectedListener historySelectedListener;
    
    /**
     * 历史记录选择监听器接口
     */
    public interface OnHistorySelectedListener {
        void onHistorySelected(TranslationHistoryEntity history);
    }
    
    public void setOnHistorySelectedListener(OnHistorySelectedListener listener) {
        this.historySelectedListener = listener;
    }
    
    public static HistoryBottomSheetFragment newInstance() {
        return new HistoryBottomSheetFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_bottom_sheet, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initRepository();
        setupRecyclerView();
        setupSearchView();
        setupClickListeners();
        
        // 加载初始数据
        loadHistory(true);
    }

    
    @Override
    public void onStart() {
        super.onStart();
        // 设置BottomSheet展开高度为屏幕的90%
        View bottomSheet = getDialog() != null ? 
                getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet) : null;
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            behavior.setPeekHeight((int) (screenHeight * 0.9));
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    
    private void initViews(View view) {
        rvHistory = view.findViewById(R.id.rv_history);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        progressLoading = view.findViewById(R.id.progress_loading);
        etSearch = view.findViewById(R.id.et_search);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        btnClose = view.findViewById(R.id.btn_close);
    }
    
    private void initRepository() {
        if (getContext() != null) {
            AppDatabase database = AppDatabase.getInstance(getContext());
            repository = new TranslationHistoryRepository(database.translationHistoryDao());
        }
    }
    
    private void setupRecyclerView() {
        adapter = new HistoryFullAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setAdapter(adapter);
        
        // 设置点击监听 - 显示详情对话框
        adapter.setOnItemClickListener((history, position) -> {
            showHistoryDetailDialog(history, position);
        });
        
        // 设置滑动删除
        setupSwipeToDelete();
        
        // 设置滚动监听，实现分页加载
        rvHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 0) { // 向下滚动
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null) {
                        int visibleItemCount = lm.getChildCount();
                        int totalItemCount = lm.getItemCount();
                        int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();
                        
                        // 当滚动到接近底部时加载更多
                        if (!isLoading && adapter.hasMoreData()) {
                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                                    && firstVisibleItemPosition >= 0) {
                                loadMoreHistory();
                            }
                        }
                    }
                }
            }
        });
    }
    
    private void setupSwipeToDelete() {
        if (getContext() == null) return;
        
        SwipeToDeleteCallback swipeCallback = new SwipeToDeleteCallback(getContext(), position -> {
            TranslationHistoryEntity history = adapter.getItem(position);
            if (history != null) {
                deleteHistoryItem(history, position);
            }
        });
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(rvHistory);
    }
    
    private void setupSearchView() {
        // 搜索文本变化监听（带防抖）
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 显示/隐藏清除按钮
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // 防抖搜索
                if (searchRunnable != null) {
                    mainHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    String keyword = s.toString().trim();
                    if (!keyword.equals(currentSearchKeyword)) {
                        currentSearchKeyword = keyword;
                        searchHistory(keyword);
                    }
                };
                mainHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
            }
        });
        
        // 搜索按钮点击（软键盘搜索按钮）
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText().toString().trim();
                currentSearchKeyword = keyword;
                searchHistory(keyword);
                return true;
            }
            return false;
        });
        
        // 清除搜索按钮
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentSearchKeyword = "";
            loadHistory(true);
        });
    }
    
    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> dismiss());
    }

    
    /**
     * 加载历史记录
     * @param refresh 是否刷新（从第一页开始）
     */
    private void loadHistory(boolean refresh) {
        if (isLoading || repository == null) return;
        
        if (refresh) {
            currentPage = 0;
            adapter.clearItems();
            adapter.setHasMoreData(true);
        }
        
        isLoading = true;
        
        // 首次加载显示中央加载指示器
        if (refresh && adapter.getDataSize() == 0) {
            progressLoading.setVisibility(View.VISIBLE);
        } else {
            adapter.setLoading(true);
        }
        
        executor.execute(() -> {
            try {
                List<TranslationHistoryEntity> historyList;
                boolean hasMore;
                
                if (TextUtils.isEmpty(currentSearchKeyword)) {
                    historyList = repository.getHistoryPage(currentPage);
                    hasMore = repository.hasMoreData(currentPage);
                } else {
                    historyList = repository.searchHistory(currentSearchKeyword, currentPage);
                    hasMore = repository.hasMoreSearchData(currentSearchKeyword, currentPage);
                }
                
                mainHandler.post(() -> {
                    isLoading = false;
                    progressLoading.setVisibility(View.GONE);
                    adapter.setLoading(false);
                    
                    if (historyList != null && !historyList.isEmpty()) {
                        adapter.addItems(historyList);
                        currentPage++;
                    }
                    
                    adapter.setHasMoreData(hasMore);
                    updateEmptyState();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    isLoading = false;
                    progressLoading.setVisibility(View.GONE);
                    adapter.setLoading(false);
                    
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "加载历史记录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    /**
     * 加载更多历史记录
     */
    private void loadMoreHistory() {
        loadHistory(false);
    }
    
    /**
     * 搜索历史记录
     * @param keyword 搜索关键词
     */
    private void searchHistory(String keyword) {
        currentPage = 0;
        adapter.clearItems();
        adapter.setHasMoreData(true);
        loadHistory(true);
    }
    
    /**
     * 删除历史记录项
     * @param history 要删除的历史记录
     * @param position 列表位置
     */
    private void deleteHistoryItem(TranslationHistoryEntity history, int position) {
        if (repository == null) return;
        
        // 先从UI移除
        adapter.removeItem(position);
        
        // 后台删除数据库记录
        executor.execute(() -> {
            try {
                repository.deleteHistory(history.getId());
                mainHandler.post(this::updateEmptyState);
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                    }
                    // 删除失败，重新加载数据
                    loadHistory(true);
                });
            }
        });
    }
    
    /**
     * 更新空状态显示
     */
    private void updateEmptyState() {
        boolean isEmpty = adapter.getDataSize() == 0;
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        // 根据是否有搜索关键词显示不同的空状态提示
        if (isEmpty) {
            if (TextUtils.isEmpty(currentSearchKeyword)) {
                tvEmptyMessage.setText("暂无翻译历史");
            } else {
                tvEmptyMessage.setText("未找到匹配的翻译记录");
            }
        }
    }
    
    /**
     * 刷新历史记录列表
     * 可供外部调用，用于在添加新记录后刷新
     */
    public void refreshHistory() {
        currentSearchKeyword = "";
        etSearch.setText("");
        loadHistory(true);
    }
    
    /**
     * 显示历史记录详情对话框
     * @param history 历史记录实体
     * @param position 列表位置
     */
    private void showHistoryDetailDialog(TranslationHistoryEntity history, int position) {
        if (getParentFragmentManager() == null) return;
        
        HistoryDetailDialog dialog = HistoryDetailDialog.newInstance(history);
        dialog.setOnHistoryActionListener(new HistoryDetailDialog.OnHistoryActionListener() {
            @Override
            public void onUseHistory(TranslationHistoryEntity selectedHistory) {
                // 通知外部监听器使用此翻译
                if (historySelectedListener != null) {
                    historySelectedListener.onHistorySelected(selectedHistory);
                }
                dismiss();
            }

            @Override
            public void onDeleteHistory(int historyId) {
                // 删除历史记录
                deleteHistoryItem(history, position);
            }
        });
        dialog.show(getParentFragmentManager(), "HistoryDetailDialog");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除搜索防抖回调
        if (searchRunnable != null) {
            mainHandler.removeCallbacks(searchRunnable);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 关闭线程池
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
