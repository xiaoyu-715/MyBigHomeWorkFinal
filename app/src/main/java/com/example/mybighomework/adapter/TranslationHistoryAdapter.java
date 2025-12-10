package com.example.mybighomework.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻译历史记录适配器
 */
public class TranslationHistoryAdapter extends RecyclerView.Adapter<TranslationHistoryAdapter.ViewHolder> {
    
    private List<TranslationHistoryEntity> historyList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(TranslationHistoryEntity history);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setHistoryList(List<TranslationHistoryEntity> historyList) {
        this.historyList = historyList != null ? historyList : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_translation_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TranslationHistoryEntity history = historyList.get(position);
        holder.bind(history);
    }
    
    @Override
    public int getItemCount() {
        return historyList.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLanguageTag;
        TextView tvSourceText;
        TextView tvTranslatedText;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLanguageTag = itemView.findViewById(R.id.tv_language_tag);
            tvSourceText = itemView.findViewById(R.id.tv_source_text);
            tvTranslatedText = itemView.findViewById(R.id.tv_translated_text);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(historyList.get(position));
                }
            });
        }
        
        void bind(TranslationHistoryEntity history) {
            // 设置语言标签
            String languageTag = getLanguageTag(history.getSourceLanguage(), history.getTargetLanguage());
            tvLanguageTag.setText(languageTag);
            
            // 设置源文本和翻译文本
            tvSourceText.setText(history.getSourceText());
            tvTranslatedText.setText(history.getTranslatedText());
        }
        
        private String getLanguageTag(String sourceLang, String targetLang) {
            String source = "en".equals(sourceLang) ? "英" : "中";
            String target = "en".equals(targetLang) ? "英" : "中";
            return source + target;
        }
    }
}

