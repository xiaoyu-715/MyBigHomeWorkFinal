package com.example.mybighomework.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.DictionaryWordEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 单词预览适配器
 * 用于词书详情页显示单词列表预览
 */
public class WordPreviewAdapter extends RecyclerView.Adapter<WordPreviewAdapter.WordViewHolder> {

    private List<DictionaryWordEntity> words = new ArrayList<>();

    public void setWords(List<DictionaryWordEntity> words) {
        this.words = words != null ? words : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_preview, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        DictionaryWordEntity word = words.get(position);
        holder.bind(word, position + 1);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        private TextView tvIndex, tvWord, tvPhonetic, tvTranslation;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_index);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvPhonetic = itemView.findViewById(R.id.tv_phonetic);
            tvTranslation = itemView.findViewById(R.id.tv_translation);
        }

        void bind(DictionaryWordEntity word, int index) {
            tvIndex.setText(String.valueOf(index));
            tvWord.setText(word.getWord());
            
            // 显示音标（优先美式）
            String phonetic = word.getPhoneticUs();
            if (phonetic == null || phonetic.isEmpty()) {
                phonetic = word.getPhoneticUk();
            }
            if (phonetic != null && !phonetic.isEmpty()) {
                tvPhonetic.setVisibility(View.VISIBLE);
                tvPhonetic.setText(phonetic);
            } else {
                tvPhonetic.setVisibility(View.GONE);
            }

            // 显示翻译
            String translation = word.getTranslation();
            if (translation != null && !translation.isEmpty()) {
                tvTranslation.setText(translation);
            } else {
                tvTranslation.setText("暂无释义");
            }
        }
    }
}
