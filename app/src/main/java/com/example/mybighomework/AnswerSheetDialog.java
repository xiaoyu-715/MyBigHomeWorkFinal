package com.example.mybighomework;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.flexbox.FlexboxLayout;

import java.util.Map;

/**
 * 答题卡对话框
 * 按题型分组显示所有题目的答题状态,支持快速跳转
 */
public class AnswerSheetDialog extends Dialog {

    private TextView tvAnsweredCount, tvUnansweredCount;
    private LinearLayout layoutQuestionGroups;
    private Button btnSubmitExam;
    
    private int totalQuestions;
    private int currentQuestionIndex;
    private Map<Integer, String> userAnswers;
    private OnQuestionSelectedListener listener;
    private OnSubmitExamListener submitListener;

    public interface OnQuestionSelectedListener {
        void onQuestionSelected(int questionIndex);
    }

    public interface OnSubmitExamListener {
        void onSubmitExam();
    }

    public AnswerSheetDialog(@NonNull Context context, int totalQuestions, 
                            int currentQuestionIndex, Map<Integer, String> userAnswers,
                            OnQuestionSelectedListener listener,
                            OnSubmitExamListener submitListener) {
        super(context);
        this.totalQuestions = totalQuestions;
        this.currentQuestionIndex = currentQuestionIndex;
        this.userAnswers = userAnswers;
        this.listener = listener;
        this.submitListener = submitListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_answer_sheet);

        // 设置对话框宽度为屏幕宽度的95%
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.95),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        initViews();
        createQuestionGroups();
        updateStatistics();
    }

    private void initViews() {
        tvAnsweredCount = findViewById(R.id.tv_answered_count);
        tvUnansweredCount = findViewById(R.id.tv_unanswered_count);
        layoutQuestionGroups = findViewById(R.id.layout_question_groups);
        btnSubmitExam = findViewById(R.id.btn_submit_exam);
        
        // 关闭按钮
        findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
        
        // 交卷按钮
        btnSubmitExam.setOnClickListener(v -> {
            if (submitListener != null) {
                submitListener.onSubmitExam();
            }
            dismiss();
        });
    }

    /**
     * 创建按题型分组的题号显示
     */
    private void createQuestionGroups() {
        layoutQuestionGroups.removeAllViews();
        
        // 定义题型分组
        QuestionGroup[] groups = {
            new QuestionGroup("Use of English", 0, 20),
            new QuestionGroup("Reading Comprehension", 20, 45),
            new QuestionGroup("Translation", 45, 46),
            new QuestionGroup("Writing", 46, 47)
        };
        
        for (QuestionGroup group : groups) {
            if (group.endIndex > totalQuestions) {
                group.endIndex = totalQuestions;
            }
            if (group.startIndex >= totalQuestions) {
                continue;
            }
            addQuestionGroup(group);
        }
    }

    /**
     * 添加一个题型分组
     */
    private void addQuestionGroup(QuestionGroup group) {
        // 题型标题
        TextView tvGroupTitle = new TextView(getContext());
        tvGroupTitle.setText(group.name);
        tvGroupTitle.setTextSize(16);
        tvGroupTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        tvGroupTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, dpToPx(12), 0, dpToPx(12));
        tvGroupTitle.setLayoutParams(titleParams);
        layoutQuestionGroups.addView(tvGroupTitle);
        
        // 题号网格容器（使用FlexboxLayout实现自动换行）
        FlexboxLayout questionNumbersContainer = new FlexboxLayout(getContext());
        questionNumbersContainer.setFlexWrap(com.google.android.flexbox.FlexWrap.WRAP);
        questionNumbersContainer.setJustifyContent(com.google.android.flexbox.JustifyContent.FLEX_START);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 0, 0, dpToPx(16));
        questionNumbersContainer.setLayoutParams(containerParams);
        
        // 添加题号按钮
        for (int i = group.startIndex; i < group.endIndex; i++) {
            final int questionIndex = i;
            TextView tvQuestionNumber = createQuestionNumberButton(questionIndex);
            questionNumbersContainer.addView(tvQuestionNumber);
        }
        
        layoutQuestionGroups.addView(questionNumbersContainer);
    }

    /**
     * 创建题号按钮
     */
    private TextView createQuestionNumberButton(int questionIndex) {
        TextView tvNumber = new TextView(getContext());
        tvNumber.setText(String.valueOf(questionIndex + 1));
        tvNumber.setTextSize(16);
        tvNumber.setGravity(Gravity.CENTER);
        tvNumber.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
            dpToPx(50),
            dpToPx(40)
        );
        params.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        tvNumber.setLayoutParams(params);
        
        // 设置样式
        boolean isAnswered = userAnswers.containsKey(questionIndex) && 
                            userAnswers.get(questionIndex) != null && 
                            !userAnswers.get(questionIndex).isEmpty();
        
        if (isAnswered) {
            tvNumber.setBackgroundResource(R.drawable.bg_question_number_selected);
            tvNumber.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        } else {
            tvNumber.setBackgroundResource(R.drawable.bg_question_number);
            tvNumber.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        }
        
        // 点击跳转
        tvNumber.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuestionSelected(questionIndex);
            }
            dismiss();
        });
        
        return tvNumber;
    }

    private void updateStatistics() {
        int answeredCount = userAnswers.size();
        int unansweredCount = totalQuestions - answeredCount;
        
        tvAnsweredCount.setText(String.valueOf(answeredCount));
        tvUnansweredCount.setText(String.valueOf(unansweredCount));
    }
    
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * 题型分组数据类
     */
    private static class QuestionGroup {
        String name;
        int startIndex;
        int endIndex;
        
        QuestionGroup(String name, int startIndex, int endIndex) {
            this.name = name;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
}

