package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddStudyPlanActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etPlanTitle, etPlanDescription, etTimeRange, etDuration;
    private Spinner spinnerCategory, spinnerPriority;
    private Button btnSavePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_study_plan);

        initViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etPlanTitle = findViewById(R.id.et_plan_title);
        etPlanDescription = findViewById(R.id.et_plan_description);
        etTimeRange = findViewById(R.id.et_time_range);
        etDuration = findViewById(R.id.et_duration);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerPriority = findViewById(R.id.spinner_priority);
        btnSavePlan = findViewById(R.id.btn_save_plan);
    }

    private void setupSpinners() {
        // 设置分类选择器
        String[] categories = {"词汇训练", "数学训练", "编程", "历史", "体育", "其他"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // 设置优先级选择器
        String[] priorities = {"高", "中", "低"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSavePlan.setOnClickListener(v -> savePlan());
    }

    private void savePlan() {
        String title = etPlanTitle.getText().toString().trim();
        String description = etPlanDescription.getText().toString().trim();
        String timeRange = etTimeRange.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String priority = spinnerPriority.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty() || timeRange.isEmpty() || duration.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建新的学习计划
        StudyPlan newPlan = new StudyPlan(title, category, description, timeRange, 
            duration, 0, priority, "未开始", false);

        // 返回结果
        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_plan_title", title);
        resultIntent.putExtra("new_plan_category", category);
        resultIntent.putExtra("new_plan_description", description);
        resultIntent.putExtra("new_plan_time_range", timeRange);
        resultIntent.putExtra("new_plan_duration", duration);
        resultIntent.putExtra("new_plan_priority", priority);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "学习计划已添加", Toast.LENGTH_SHORT).show();
        finish();
    }
}