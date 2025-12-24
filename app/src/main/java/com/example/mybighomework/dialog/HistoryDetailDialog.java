package com.example.mybighomework.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mybighomework.R;
import com.example.mybighomework.database.entity.TranslationHistoryEntity;
import com.example.mybighomework.utils.TimeFormatUtils;
import com.google.android.material.button.MaterialButton;

/**
 * 历史记录详情对话框
 * 显示完整的源文本和翻译文本，提供复制和删除操作
 * 
 * Requirements: 7.3
 */
public class HistoryDetailDialog extends DialogFragment {

    private static final String ARG_HISTORY_ID = "history_id";
    private static final String ARG_SOURCE_TEXT = "source_text";
    private static final String ARG_TRANSLATED_TEXT = "translated_text";
    private static final String ARG_SOURCE_LANGUAGE = "source_language";
    private static final String ARG_TARGET_LANGUAGE = "target_language";
    private static final String ARG_TIMESTAMP = "timestamp";

    // UI组件
    private ImageButton btnClose;
    private TextView tvTimestamp;
    private TextView tvSourceLanguage;
    private TextView tvSourceText;
    private ImageButton btnCopySource;
    private TextView tvTargetLanguage;
    private TextView tvTranslatedText;
    private ImageButton btnCopyTranslated;
    private MaterialButton btnDelete;
    private MaterialButton btnUse;

    // 数据
    private int historyId;
    private String sourceText;
    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    private long timestamp;

    // 回调接口
    private OnHistoryActionListener actionListener;

    /**
     * 历史记录操作监听器接口
     */
    public interface OnHistoryActionListener {
        /**
         * 当用户选择使用此翻译时调用
         */
        void onUseHistory(TranslationHistoryEntity history);

        /**
         * 当用户选择删除此记录时调用
         */
        void onDeleteHistory(int historyId);
    }

    /**
     * 创建新实例
     */
    public static HistoryDetailDialog newInstance(TranslationHistoryEntity history) {
        HistoryDetailDialog dialog = new HistoryDetailDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_HISTORY_ID, history.getId());
        args.putString(ARG_SOURCE_TEXT, history.getSourceText());
        args.putString(ARG_TRANSLATED_TEXT, history.getTranslatedText());
        args.putString(ARG_SOURCE_LANGUAGE, history.getSourceLanguage());
        args.putString(ARG_TARGET_LANGUAGE, history.getTargetLanguage());
        args.putLong(ARG_TIMESTAMP, history.getTimestamp());
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnHistoryActionListener(OnHistoryActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_MyBigHomeWork_Dialog);
        
        if (getArguments() != null) {
            historyId = getArguments().getInt(ARG_HISTORY_ID);
            sourceText = getArguments().getString(ARG_SOURCE_TEXT, "");
            translatedText = getArguments().getString(ARG_TRANSLATED_TEXT, "");
            sourceLanguage = getArguments().getString(ARG_SOURCE_LANGUAGE, "zh");
            targetLanguage = getArguments().getString(ARG_TARGET_LANGUAGE, "en");
            timestamp = getArguments().getLong(ARG_TIMESTAMP, System.currentTimeMillis());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_history_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        bindData();
        setupClickListeners();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置对话框宽度
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initViews(View view) {
        btnClose = view.findViewById(R.id.btn_close);
        tvTimestamp = view.findViewById(R.id.tv_timestamp);
        tvSourceLanguage = view.findViewById(R.id.tv_source_language);
        tvSourceText = view.findViewById(R.id.tv_source_text);
        btnCopySource = view.findViewById(R.id.btn_copy_source);
        tvTargetLanguage = view.findViewById(R.id.tv_target_language);
        tvTranslatedText = view.findViewById(R.id.tv_translated_text);
        btnCopyTranslated = view.findViewById(R.id.btn_copy_translated);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnUse = view.findViewById(R.id.btn_use);
    }

    private void bindData() {
        // 设置时间戳
        tvTimestamp.setText(TimeFormatUtils.formatTimestamp(timestamp));
        
        // 设置语言标签
        tvSourceLanguage.setText(getLanguageDisplayName(sourceLanguage));
        tvTargetLanguage.setText(getLanguageDisplayName(targetLanguage));
        
        // 设置文本内容
        tvSourceText.setText(sourceText);
        tvTranslatedText.setText(translatedText);
    }

    private void setupClickListeners() {
        // 关闭按钮
        btnClose.setOnClickListener(v -> dismiss());

        // 复制源文本
        btnCopySource.setOnClickListener(v -> {
            copyToClipboard(sourceText, "源文本");
        });

        // 复制翻译文本
        btnCopyTranslated.setOnClickListener(v -> {
            copyToClipboard(translatedText, "翻译文本");
        });

        // 删除按钮
        btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteHistory(historyId);
            }
            dismiss();
        });

        // 使用按钮
        btnUse.setOnClickListener(v -> {
            if (actionListener != null) {
                TranslationHistoryEntity history = createHistoryEntity();
                actionListener.onUseHistory(history);
            }
            dismiss();
        });
    }

    /**
     * 复制文本到剪贴板
     */
    private void copyToClipboard(String text, String label) {
        if (getContext() == null) return;
        
        ClipboardManager clipboard = (ClipboardManager) 
                getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "已复制" + label, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取语言显示名称
     */
    private String getLanguageDisplayName(String languageCode) {
        if (languageCode == null) return "未知";
        
        switch (languageCode.toLowerCase()) {
            case "zh":
            case "zh-cn":
            case "chinese":
                return "中文";
            case "en":
            case "english":
                return "英文";
            case "ja":
            case "japanese":
                return "日文";
            case "ko":
            case "korean":
                return "韩文";
            case "fr":
            case "french":
                return "法文";
            case "de":
            case "german":
                return "德文";
            case "es":
            case "spanish":
                return "西班牙文";
            case "ru":
            case "russian":
                return "俄文";
            default:
                return languageCode.toUpperCase();
        }
    }

    /**
     * 创建历史记录实体对象
     */
    private TranslationHistoryEntity createHistoryEntity() {
        TranslationHistoryEntity history = new TranslationHistoryEntity();
        history.setId(historyId);
        history.setSourceText(sourceText);
        history.setTranslatedText(translatedText);
        history.setSourceLanguage(sourceLanguage);
        history.setTargetLanguage(targetLanguage);
        history.setTimestamp(timestamp);
        return history;
    }
}
