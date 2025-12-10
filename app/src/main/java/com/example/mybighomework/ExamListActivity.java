package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class ExamListActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnDownload;
    private TextView btnCet4, btnCet6;
    private LinearLayout examListContainer;
    private LinearLayout navHome, navReport, navProfile, navMore;

    private boolean isCet4Selected = true; // 默认选择四级

    // 模拟考试数据
    private static class ExamPaperItem {
        String title;
        String year;
        String type;
        double difficulty;
        String status;

        ExamPaperItem(String title, String year, String type, double difficulty, String status) {
            this.title = title;
            this.year = year;
            this.type = type;
            this.difficulty = difficulty;
            this.status = status;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_list);

        android.util.Log.d("ExamListActivity", "ExamListActivity启动完成");

        initViews();
        setupClickListeners();
        loadExamPapers();

        android.util.Log.d("ExamListActivity", "试卷列表初始化完成，总试卷数：" + examListContainer.getChildCount());
    }


    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnDownload = findViewById(R.id.btn_download);
        btnCet4 = findViewById(R.id.btn_cet4);
        btnCet6 = findViewById(R.id.btn_cet6);
        examListContainer = findViewById(R.id.exam_list_container);

        // 底部导航栏
        navHome = findViewById(R.id.nav_home);
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);
    }

    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 下载按钮
        btnDownload.setOnClickListener(v -> {
            Toast.makeText(this, "下载功能开发中...", Toast.LENGTH_SHORT).show();
        });

        // 四级六级选择
        btnCet4.setOnClickListener(v -> {
            if (!isCet4Selected) {
                selectCet4();
                loadExamPapers();
            }
        });

        btnCet6.setOnClickListener(v -> {
            if (isCet4Selected) {
                selectCet6();
                loadExamPapers();
            }
        });

        // 底部导航栏
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        navReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportActivity.class);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        navMore.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoreActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void selectCet4() {
        isCet4Selected = true;
        btnCet4.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_exam_type_selected));
        btnCet4.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnCet6.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_exam_type_unselected));
        btnCet6.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
    }

    private void selectCet6() {
        isCet4Selected = false;
        btnCet6.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_exam_type_selected));
        btnCet6.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnCet4.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_exam_type_unselected));
        btnCet4.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
    }

    private void loadExamPapers() {
        examListContainer.removeAllViews();

        // 模拟考试数据 - 实际项目中应该从数据库或服务器获取
        if (isCet4Selected) {
            addExamPaper("2025年考研英语（二）试题（网友回忆版）", "2025", "考研英语二", 4.9, "未完成");
            addExamPaper("2024年考研英语（二）试题", "2024", "考研英语二", 4.8, "");
            addExamPaper("2023年考研英语（二）试题", "2023", "考研英语二", 4.2, "");
            addExamPaper("2022年考研英语（二）试题", "2022", "考研英语二", 4.8, "");
            addExamPaper("2021年考研英语（二）试题", "2021", "考研英语二", 4.6, "");
            addExamPaper("2020年考研英语（二）试题", "2020", "考研英语二", 5.0, "");
        } else {
            addExamPaper("2025年考研英语（一）试题（网友回忆版）", "2025", "考研英语一", 4.9, "未完成");
            addExamPaper("2024年考研英语（一）试题", "2024", "考研英语一", 4.8, "");
            addExamPaper("2023年考研英语（一）试题", "2023", "考研英语一", 4.2, "");
            addExamPaper("2022年考研英语（一）试题", "2022", "考研英语一", 4.8, "");
            addExamPaper("2021年考研英语（一）试题", "2021", "考研英语一", 4.6, "");
            addExamPaper("2020年考研英语（一）试题", "2020", "考研英语一", 5.0, "");
        }
    }

    private void addExamPaper(String title, String year, String type, double difficulty, String status) {
        View itemView = getLayoutInflater().inflate(R.layout.item_exam_paper, examListContainer, false);

        TextView tvPaperName = itemView.findViewById(R.id.tv_paper_name);
        TextView tvPaperYear = itemView.findViewById(R.id.tv_paper_year);
        TextView tvExamType = itemView.findViewById(R.id.tv_exam_type);

        tvPaperName.setText(title);
        tvPaperYear.setText(year);
        tvExamType.setText(type);

        // 确保CardView可点击
        androidx.cardview.widget.CardView cardView = (androidx.cardview.widget.CardView) itemView;
        cardView.setClickable(true);

        // 点击事件 - 跳转到考试答题页面
        // 绑定到最外层的CardView而不是内部的LinearLayout
        cardView.setOnClickListener(v -> {
            android.util.Log.d("ExamListActivity", "点击试卷: " + title + ", 跳转到答题页面");
            Intent intent = new Intent(this, ExamAnswerActivity.class);
            intent.putExtra("exam_title", title);
            intent.putExtra("exam_year", year);
            intent.putExtra("exam_type", type);
            intent.putExtra("difficulty", difficulty);

            try {
                startActivity(intent);
                android.util.Log.d("ExamListActivity", "成功跳转到答题页面");
            } catch (Exception e) {
                android.util.Log.e("ExamListActivity", "跳转失败", e);
                Toast.makeText(this, "跳转失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        examListContainer.addView(itemView);
        android.util.Log.d("ExamListActivity", "添加试卷: " + title + ", 当前总试卷数: " + examListContainer.getChildCount());
    }
}
