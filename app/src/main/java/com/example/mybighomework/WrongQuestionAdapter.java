package com.example.mybighomework;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.example.mybighomework.database.entity.WrongQuestionEntity;

public class WrongQuestionAdapter extends RecyclerView.Adapter<WrongQuestionAdapter.ViewHolder> {
    
    private final Context context;
    private final List<WrongQuestionEntity> wrongQuestions;
    private final OnItemActionListener listener;
    
    public interface OnItemActionListener {
        void onShowExplanation(WrongQuestionEntity question);
        void onMarkMastered(WrongQuestionEntity question);
        void onRemove(WrongQuestionEntity question);
    }

    public WrongQuestionAdapter(Context context, List<WrongQuestionEntity> wrongQuestions, OnItemActionListener listener) {
        this.context = context;
        this.wrongQuestions = wrongQuestions;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wrong_question, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WrongQuestionEntity question = wrongQuestions.get(position);
        
        holder.tvQuestionNumber.setText("第" + (position + 1) + "题");
        holder.tvCategory.setText(question.getCategory());
        holder.tvWrongCount.setText("错误 " + question.getWrongCount() + " 次");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        holder.tvWrongDate.setText(sdf.format(question.getWrongTime()));
        
        holder.tvQuestionContent.setText(question.getQuestionText());
        
        String[] options = question.getOptions();
        TextView[] optionTextViews = {holder.tvOptionA, holder.tvOptionB, holder.tvOptionC, holder.tvOptionD};
        
        if (options == null || options.length == 0) {
            for (TextView optionView : optionTextViews) {
                optionView.setVisibility(View.GONE);
            }
        } else {
            for (int i = 0; i < optionTextViews.length; i++) {
                if (i < options.length) {
                    optionTextViews[i].setText((char)('A' + i) + ". " + options[i]);
                    optionTextViews[i].setVisibility(View.VISIBLE);
                    
                    // Reset background
                    optionTextViews[i].setBackgroundResource(R.drawable.bg_option_normal);
                    optionTextViews[i].setTextColor(ContextCompat.getColor(context, R.color.text_primary));

                    if (i == question.getCorrectAnswerIndex()) {
                        optionTextViews[i].setBackgroundResource(R.drawable.bg_option_correct);
                        optionTextViews[i].setTextColor(ContextCompat.getColor(context, R.color.success));
                    } else if (i == question.getUserAnswerIndex()) {
                        optionTextViews[i].setBackgroundResource(R.drawable.bg_option_wrong);
                        optionTextViews[i].setTextColor(ContextCompat.getColor(context, R.color.error));
                    }

                } else {
                    optionTextViews[i].setVisibility(View.GONE);
                }
            }
        }

        holder.tvExplanation.setText(question.getExplanation());
        holder.layoutExplanation.setVisibility(View.GONE);

        if (question.isMastered()) {
            holder.ivMasteredIcon.setVisibility(View.VISIBLE);
            holder.tvMasteredStatus.setVisibility(View.VISIBLE);
            holder.btnMarkMastered.setVisibility(View.GONE);
        } else {
            holder.ivMasteredIcon.setVisibility(View.GONE);
            holder.tvMasteredStatus.setVisibility(View.GONE);
            holder.btnMarkMastered.setVisibility(View.VISIBLE);
        }

        holder.btnShowExplanation.setOnClickListener(v -> {
            boolean isVisible = holder.layoutExplanation.getVisibility() == View.VISIBLE;
            holder.layoutExplanation.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.btnShowExplanation.setText(isVisible ? "查看解析" : "隐藏解析");
            if (listener != null) {
                listener.onShowExplanation(question);
            }
        });

        holder.btnMarkMastered.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarkMastered(question);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(question);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return wrongQuestions.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber, tvCategory, tvWrongDate, tvWrongCount;
        TextView tvQuestionContent;
        LinearLayout layoutOptions;
        TextView tvOptionA, tvOptionB, tvOptionC, tvOptionD;
        LinearLayout layoutExplanation;
        TextView tvExplanation;
        Button btnShowExplanation, btnMarkMastered, btnRemove;
        ImageView ivMasteredIcon;
        TextView tvMasteredStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvWrongDate = itemView.findViewById(R.id.tv_wrong_date);
            tvWrongCount = itemView.findViewById(R.id.tv_wrong_count);
            tvQuestionContent = itemView.findViewById(R.id.tv_question_content);
            layoutOptions = itemView.findViewById(R.id.layout_options);
            tvOptionA = itemView.findViewById(R.id.tv_option_a);
            tvOptionB = itemView.findViewById(R.id.tv_option_b);
            tvOptionC = itemView.findViewById(R.id.tv_option_c);
            tvOptionD = itemView.findViewById(R.id.tv_option_d);
            layoutExplanation = itemView.findViewById(R.id.layout_explanation);
            tvExplanation = itemView.findViewById(R.id.tv_explanation);
            btnShowExplanation = itemView.findViewById(R.id.btn_show_explanation);
            btnMarkMastered = itemView.findViewById(R.id.btn_mark_mastered);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            ivMasteredIcon = itemView.findViewById(R.id.iv_mastered_icon);
            tvMasteredStatus = itemView.findViewById(R.id.tv_mastered_status);
        }
    }
}