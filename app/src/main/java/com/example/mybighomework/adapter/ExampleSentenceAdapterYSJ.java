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
import com.example.mybighomework.database.entity.ExampleSentenceEntity;

/**
 * 例句列表适配器
 */
public class ExampleSentenceAdapterYSJ extends ListAdapter<ExampleSentenceEntity, ExampleSentenceAdapterYSJ.ViewHolder> {

    private OnSpeakClickListener onSpeakClickListener;

    public ExampleSentenceAdapterYSJ() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ExampleSentenceEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ExampleSentenceEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull ExampleSentenceEntity oldItem,
                                               @NonNull ExampleSentenceEntity newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ExampleSentenceEntity oldItem,
                                                  @NonNull ExampleSentenceEntity newItem) {
                    return oldItem.getEnglishSentence().equals(newItem.getEnglishSentence());
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_example_sentence_ysj, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExampleSentenceEntity sentence = getItem(position);
        holder.bind(sentence);
    }

    public void setOnSpeakClickListener(OnSpeakClickListener listener) {
        this.onSpeakClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEnglish;
        private final TextView tvChinese;
        private final TextView tvSource;
        private final TextView tvDifficulty;
        private final ImageButton btnSpeak;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglish = itemView.findViewById(R.id.tv_english);
            tvChinese = itemView.findViewById(R.id.tv_chinese);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
            btnSpeak = itemView.findViewById(R.id.btn_speak);

            btnSpeak.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onSpeakClickListener != null) {
                    onSpeakClickListener.onSpeakClick(getItem(position));
                }
            });
        }

        void bind(ExampleSentenceEntity sentence) {
            tvEnglish.setText(sentence.getEnglishSentence());
            tvChinese.setText(sentence.getChineseSentence());
            
            // 来源
            String source = sentence.getSource();
            if (source != null && !source.isEmpty()) {
                tvSource.setText("来源：" + source);
                tvSource.setVisibility(View.VISIBLE);
            } else {
                tvSource.setVisibility(View.GONE);
            }
            
            // 难度标签
            int difficulty = sentence.getDifficulty();
            tvDifficulty.setText(getDifficultyText(difficulty));
        }

        private String getDifficultyText(int difficulty) {
            if (difficulty <= 3) return "简单";
            if (difficulty <= 6) return "中等";
            return "困难";
        }
    }

    public interface OnSpeakClickListener {
        void onSpeakClick(ExampleSentenceEntity sentence);
    }
}
