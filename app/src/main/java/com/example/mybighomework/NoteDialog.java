package com.example.mybighomework;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mybighomework.database.entity.QuestionNoteEntity;

/**
 * 笔记对话框
 * 用于添加、编辑和查看题目笔记
 */
public class NoteDialog extends Dialog {

    private TextView tvTitle;
    private EditText etNote;
    private Button btnSave, btnDelete, btnCancel;

    private String examTitle;
    private int questionIndex;
    private String existingNote;
    private OnNoteActionListener listener;

    public interface OnNoteActionListener {
        void onNoteSaved(String noteContent);
        void onNoteDeleted();
    }

    public NoteDialog(@NonNull Context context, String examTitle, int questionIndex, 
                     String existingNote, OnNoteActionListener listener) {
        super(context);
        this.examTitle = examTitle;
        this.questionIndex = questionIndex;
        this.existingNote = existingNote;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_note);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_note_title);
        etNote = findViewById(R.id.et_note);
        btnSave = findViewById(R.id.btn_save_note);
        btnDelete = findViewById(R.id.btn_delete_note);
        btnCancel = findViewById(R.id.btn_cancel_note);

        // 设置标题
        tvTitle.setText("第 " + (questionIndex + 1) + " 题笔记");

        // 如果有现有笔记，显示出来
        if (!TextUtils.isEmpty(existingNote)) {
            etNote.setText(existingNote);
            etNote.setSelection(existingNote.length()); // 光标移到末尾
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // 保存按钮
        btnSave.setOnClickListener(v -> {
            String noteContent = etNote.getText().toString().trim();
            if (TextUtils.isEmpty(noteContent)) {
                Toast.makeText(getContext(), "笔记内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onNoteSaved(noteContent);
            }
            dismiss();
        });

        // 删除按钮
        btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteDeleted();
            }
            dismiss();
        });

        // 取消按钮
        btnCancel.setOnClickListener(v -> dismiss());
    }
}


