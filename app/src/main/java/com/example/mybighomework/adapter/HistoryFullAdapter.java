package com.example.mybighomework.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;
import com.example.mybighomework.utils.TimeFormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 增强版历史记录适配器
 * 支持分页加载和滑动删除功能
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 5.3
 */
public class HistoryFullAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    
    private final List<TranslationHistoryEntity> historyList = new ArrayList<>();
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    
    /**
     * 点击监听器接口
     */
    public interface OnItemClickListener {
        void onItemClick(TranslationHistoryEntity history, int position);
    }
    
    /**
     * 长按监听器接口
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(TranslationHistoryEntity history, int position);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        // 如果正在加载且是最后一个位置，显示加载项
        if (isLoading && position == historyList.size()) {
            return VIEW_TYPE_LOADING;
        }
        return VIEW_TYPE_ITEM;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_LOADING) {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_history_full, parent, false);
            return new HistoryViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HistoryViewHolder) {
            TranslationHistoryEntity history = historyList.get(position);
            ((HistoryViewHolder) holder).bind(history, position);
        }
        // LoadingViewHolder不需要绑定数据
    }
    
    @Override
    public int getItemCount() {
        // 如果正在加载，额外显示一个加载项
        return historyList.size() + (isLoading ? 1 : 0);
    }
    
    /**
     * 添加更多数据到列表末尾
     * @param items 要添加的数据列表
     */
    public void addItems(List<TranslationHistoryEntity> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        int startPosition = historyList.size();
        historyList.addAll(items);
        notifyItemRangeInserted(startPosition, items.size());
    }
    
    /**
     * 设置数据列表（替换现有数据）
     * @param items 新的数据列表
     */
    public void setItems(List<TranslationHistoryEntity> items) {
        historyList.clear();
        if (items != null) {
            historyList.addAll(items);
        }
        notifyDataSetChanged();
    }
    
    /**
     * 移除指定位置的数据
     * @param position 要移除的位置
     */
    public void removeItem(int position) {
        if (position >= 0 && position < historyList.size()) {
            historyList.remove(position);
            notifyItemRemoved(position);
            // 更新后续项的位置
            notifyItemRangeChanged(position, historyList.size() - position);
        }
    }
    
    /**
     * 获取指定位置的数据
     * @param position 位置
     * @return 历史记录实体，如果位置无效则返回null
     */
    public TranslationHistoryEntity getItem(int position) {
        if (position >= 0 && position < historyList.size()) {
            return historyList.get(position);
        }
        return null;
    }
    
    /**
     * 设置加载状态
     * @param loading 是否正在加载
     */
    public void setLoading(boolean loading) {
        if (this.isLoading != loading) {
            this.isLoading = loading;
            if (loading) {
                notifyItemInserted(historyList.size());
            } else {
                notifyItemRemoved(historyList.size());
            }
        }
    }
    
    /**
     * 设置是否还有更多数据
     * @param hasMore 是否还有更多数据
     */
    public void setHasMoreData(boolean hasMore) {
        this.hasMoreData = hasMore;
    }
    
    /**
     * 检查是否还有更多数据
     * @return 是否还有更多数据
     */
    public boolean hasMoreData() {
        return hasMoreData;
    }
    
    /**
     * 检查是否正在加载
     * @return 是否正在加载
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * 清空所有数据
     */
    public void clearItems() {
        int size = historyList.size();
        historyList.clear();
        notifyItemRangeRemoved(0, size);
    }
    
    /**
     * 获取数据列表大小
     * @return 数据列表大小
     */
    public int getDataSize() {
        return historyList.size();
    }

    /**
     * 历史记录项ViewHolder
     */
    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLanguageTag;
        private final TextView tvSourceText;
        private final TextView tvTranslatedText;
        private final TextView tvTimestamp;
        
        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLanguageTag = itemView.findViewById(R.id.tv_language_tag);
            tvSourceText = itemView.findViewById(R.id.tv_source_text);
            tvTranslatedText = itemView.findViewById(R.id.tv_translated_text);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(historyList.get(position), position);
                }
            });
            
            // 设置长按事件
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(historyList.get(position), position);
                    return true;
                }
                return false;
            });
        }
        
        void bind(TranslationHistoryEntity history, int position) {
            // 设置语言标签
            String languageTag = getLanguageTag(history.getSourceLanguage(), history.getTargetLanguage());
            tvLanguageTag.setText(languageTag);
            
            // 设置源文本
            tvSourceText.setText(history.getSourceText());
            
            // 设置翻译文本
            tvTranslatedText.setText(history.getTranslatedText());
            
            // 设置时间戳
            if (tvTimestamp != null) {
                String formattedTime = TimeFormatUtils.formatTimestamp(history.getTimestamp());
                tvTimestamp.setText(formattedTime);
            }
        }
        
        private String getLanguageTag(String sourceLang, String targetLang) {
            String source = "en".equals(sourceLang) ? "英" : "中";
            String target = "en".equals(targetLang) ? "英" : "中";
            return source + " → " + target;
        }
    }
    
    /**
     * 加载中ViewHolder
     */
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        private final ProgressBar progressBar;
        
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
