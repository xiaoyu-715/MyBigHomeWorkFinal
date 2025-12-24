package com.example.mybighomework.autoglm.manager;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.mybighomework.VocabularyActivity;
import com.example.mybighomework.ExamListActivity;
import com.example.mybighomework.StudyPlanActivity;
import com.example.mybighomework.WrongQuestionActivity;
import com.example.mybighomework.ReportActivity;
import com.example.mybighomework.DailySentenceActivity;
import com.example.mybighomework.DailyTaskActivity;

public class TaskExecutor {
    
    private Context context;
    
    public TaskExecutor(Context context) {
        this.context = context;
    }
    
    public boolean executeTask(String intent) {
        String normalizedIntent = intent.toLowerCase().trim();
        
        // 词汇训练相关
        if (containsAny(normalizedIntent, "词汇", "单词", "背单词", "词汇训练", "学单词")) {
            startVocabularyTraining();
            return true;
        }
        
        // 真题练习相关
        if (containsAny(normalizedIntent, "真题", "考试", "做题", "练习题", "试卷")) {
            startExamPractice();
            return true;
        }
        
        // 学习计划相关
        if (containsAny(normalizedIntent, "学习计划", "计划", "规划", "安排")) {
            viewStudyPlan();
            return true;
        }
        
        // 错题本相关
        if (containsAny(normalizedIntent, "错题", "错题本", "复习错题", "错题复习")) {
            reviewWrongQuestions();
            return true;
        }
        
        // 学习报告相关
        if (containsAny(normalizedIntent, "学习报告", "报告", "学习数据", "学习情况", "进度")) {
            viewReport();
            return true;
        }
        
        // 每日一句相关
        if (containsAny(normalizedIntent, "每日一句", "每日", "一句话", "句子")) {
            viewDailySentence();
            return true;
        }
        
        // 今日任务相关
        if (containsAny(normalizedIntent, "今日任务", "今天任务", "任务", "待办")) {
            viewDailyTasks();
            return true;
        }
        
        return false;
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private void startVocabularyTraining() {
        Intent intent = new Intent(context, VocabularyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        showToast("正在启动词汇训练...");
    }
    
    private void startExamPractice() {
        Intent intent = new Intent(context, ExamListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        showToast("正在打开真题练习...");
    }
    
    private void viewStudyPlan() {
        Intent intent = new Intent(context, StudyPlanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        showToast("正在打开学习计划...");
    }
    
    private void reviewWrongQuestions() {
        Intent intent = new Intent(context, WrongQuestionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        showToast("正在打开错题本...");
    }
    
    private void viewReport() {
        Intent intent = new Intent(context, ReportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        showToast("正在打开学习报告...");
    }
    
    private void viewDailySentence() {
        Intent intent = new Intent(context, DailySentenceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        showToast("正在打开每日一句...");
    }
    
    private void viewDailyTasks() {
        Intent intent = new Intent(context, DailyTaskActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        showToast("正在打开今日任务...");
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
