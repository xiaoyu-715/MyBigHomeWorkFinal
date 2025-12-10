package com.example.mybighomework;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DailySentenceHistoryAdapter extends RecyclerView.Adapter<DailySentenceHistoryAdapter.ViewHolder> {

    private List<DailySentence> historyList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(DailySentence sentence);
    }

    public DailySentenceHistoryAdapter(List<DailySentence> historyList) {
        this.historyList = historyList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_sentence_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailySentence sentence = historyList.get(position);
        
        holder.tvDate.setText(sentence.getDate());
        holder.tvEnglishText.setText(sentence.getEnglishText());
        holder.tvChineseText.setText(sentence.getChineseText());
        holder.tvAuthor.setText("—— " + sentence.getAuthor());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(sentence);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvEnglishText, tvChineseText, tvAuthor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvEnglishText = itemView.findViewById(R.id.tv_english_text);
            tvChineseText = itemView.findViewById(R.id.tv_chinese_text);
            tvAuthor = itemView.findViewById(R.id.tv_author);
        }
    }
}