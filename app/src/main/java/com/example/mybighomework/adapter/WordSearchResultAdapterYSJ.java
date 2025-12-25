package com.example.mybighomework.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.DictionaryWordEntity;

/**
 * 单词搜索结果列表适配器
 */
public class WordSearchResultAdapterYSJ extends ListAdapter<DictionaryWordEntity, WordSearchResultAdapterYSJ.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private OnSpeakClickListener onSpeakClickListener;

    public WordSearchResultAdapterYSJ() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<DictionaryWordEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DictionaryWordEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull DictionaryWordEntity oldItem,
                                               @NonNull DictionaryWordEntity newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull DictionaryWordEntity oldItem,
                                                  @NonNull DictionaryWordEntity newItem) {
                    return oldItem.getWord().equals(newItem.getWord()) &&
                           oldItem.getTranslation().equals(newItem.getTranslation());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_search_result_ysj, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DictionaryWordEntity word = getItem(position);
        holder.bind(word);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnSpeakClickListener(OnSpeakClickListener listener) {
        this.onSpeakClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWord;
        private final TextView tvPhonetic;
        private final TextView tvTranslation;
        private final TextView tvDifficulty;
        private final TextView tvFrequency;
        private final ImageButton btnSpeak;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvPhonetic = itemView.findViewById(R.id.tv_phonetic);
            tvTranslation = itemView.findViewById(R.id.tv_translation);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            btnSpeak = itemView.findViewById(R.id.btn_speak);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(getItem(position));
                }
            });

            btnSpeak.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onSpeakClickListener != null) {
                    onSpeakClickListener.onSpeakClick(getItem(position));
                }
            });
        }

        void bind(DictionaryWordEntity word) {
            tvWord.setText(word.getWord());
            
            // 显示音标（优先UK）
            String phonetic = word.getPhoneticUk();
            if (phonetic == null || phonetic.isEmpty()) {
                phonetic = word.getPhoneticUs();
            }
            if (phonetic != null && !phonetic.isEmpty()) {
                tvPhonetic.setText(phonetic);
                tvPhonetic.setVisibility(View.VISIBLE);
            } else {
                tvPhonetic.setVisibility(View.GONE);
            }
            
            // 翻译
            tvTranslation.setText(word.getTranslation());
            
            // 难度标签
            int difficulty = word.getDifficulty();
            String difficultyText = getDifficultyText(difficulty);
            tvDifficulty.setText(difficultyText);
            
            // 词频标签
            float frequency = word.getFrequency();
            if (frequency > 0.7f) {
                tvFrequency.setText("高频");
                tvFrequency.setVisibility(View.VISIBLE);
            } else if (frequency > 0.4f) {
                tvFrequency.setText("中频");
                tvFrequency.setVisibility(View.VISIBLE);
            } else {
                tvFrequency.setVisibility(View.GONE);
            }
        }

        private String getDifficultyText(int difficulty) {
            if (difficulty <= 2) return "初级";
            if (difficulty <= 4) return "中级";
            if (difficulty <= 6) return "高级";
            return "专业";
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DictionaryWordEntity word);
    }

    public interface OnSpeakClickListener {
        void onSpeakClick(DictionaryWordEntity word);
    }
}
