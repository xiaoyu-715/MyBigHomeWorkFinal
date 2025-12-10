package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.WrongQuestionEntity;
import com.example.mybighomework.repository.WrongQuestionRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 答案解析Activity
 * 显示考试完成后的答案解析和成绩统计
 */
public class ExamAnalysisActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvExamTitle, tvTotalScore, tvCorrectCount, tvWrongCount, tvUnansweredCount;
    private TextView tvSectionScore, tvAccuracyRate, tvTimeUsed, tvRanking;
    private Button btnAddToWrongBook, btnViewWrongBook, btnRetry;
    private RecyclerView recyclerAnalysis;
    private AnalysisAdapter analysisAdapter;
    
    private String examType;
    private int totalQuestions;
    private Map<Integer, String> userAnswers;
    private List<AnalysisItem> analysisList;
    private long timeUsed;
    
    private WrongQuestionRepository wrongQuestionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_analysis);

        getIntentData();
        initRepository();
        initViews();
        calculateResults();
        setupRecyclerView();
        setupClickListeners();
    }
    
    private void initRepository() {
        AppDatabase database = AppDatabase.getInstance(this);
        wrongQuestionRepository = new WrongQuestionRepository(database.wrongQuestionDao());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        examType = intent.getStringExtra("exam_type");
        totalQuestions = intent.getIntExtra("total_questions", 52);
        userAnswers = (Map<Integer, String>) intent.getSerializableExtra("user_answers");
        timeUsed = intent.getLongExtra("time_used", 0);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvExamTitle = findViewById(R.id.tv_exam_title);
        tvTotalScore = findViewById(R.id.tv_total_score);
        tvCorrectCount = findViewById(R.id.tv_correct_count);
        tvWrongCount = findViewById(R.id.tv_wrong_count);
        tvUnansweredCount = findViewById(R.id.tv_unanswered_count);
        tvSectionScore = findViewById(R.id.tv_section_score);
        tvAccuracyRate = findViewById(R.id.tv_accuracy_rate);
        tvTimeUsed = findViewById(R.id.tv_time_used);
        tvRanking = findViewById(R.id.tv_ranking);
        btnAddToWrongBook = findViewById(R.id.btn_add_to_wrong_book);
        btnViewWrongBook = findViewById(R.id.btn_view_wrong_book);
        btnRetry = findViewById(R.id.btn_retry);
        recyclerAnalysis = findViewById(R.id.recycler_analysis);

        tvExamTitle.setText(examType != null ? examType : "考研英语");
    }

    private void calculateResults() {
        analysisList = new ArrayList<>();
        
        // 这里应该根据实际的题目数据和用户答案计算结果
        // 现在使用模拟数据
        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;
        
        // 完形填空 (1-20题)
        for (int i = 1; i <= 20; i++) {
            String userAnswer = userAnswers != null ? userAnswers.get(i) : null;
            String correctAnswer = getCorrectAnswer(i);
            boolean isCorrect = correctAnswer.equals(userAnswer);
            
            if (userAnswer == null) {
                unansweredCount++;
            } else if (isCorrect) {
                correctCount++;
            } else {
                wrongCount++;
            }
            
            AnalysisItem item = new AnalysisItem();
            item.questionNumber = i;
            item.sectionName = "完形填空";
            item.userAnswer = userAnswer != null ? userAnswer : "未答";
            item.correctAnswer = correctAnswer;
            item.isCorrect = isCorrect;
            item.explanation = getExplanation(i);
            analysisList.add(item);
        }
        
        // 阅读理解 (21-50题)
        for (int i = 21; i <= 50; i++) {
            String userAnswer = userAnswers != null ? userAnswers.get(i) : null;
            String correctAnswer = getCorrectAnswer(i);
            boolean isCorrect = correctAnswer.equals(userAnswer);
            
            if (userAnswer == null) {
                unansweredCount++;
            } else if (isCorrect) {
                correctCount++;
            } else {
                wrongCount++;
            }
            
            AnalysisItem item = new AnalysisItem();
            item.questionNumber = i;
            item.sectionName = getSectionName(i);
            item.userAnswer = userAnswer != null ? userAnswer : "未答";
            item.correctAnswer = correctAnswer;
            item.isCorrect = isCorrect;
            item.explanation = getExplanation(i);
            analysisList.add(item);
        }
        
        // 写作 (51-52题)
        for (int i = 51; i <= 52; i++) {
            String userAnswer = userAnswers != null ? userAnswers.get(i) : null;
            
            if (userAnswer == null) {
                unansweredCount++;
            }
            
            AnalysisItem item = new AnalysisItem();
            item.questionNumber = i;
            item.sectionName = i == 51 ? "应用文" : "图表作文";
            item.userAnswer = userAnswer != null ? "已作答" : "未答";
            item.correctAnswer = "主观题";
            item.isCorrect = userAnswer != null;
            item.explanation = "写作题为主观题，请参考范文";
            analysisList.add(item);
        }
        
        // 更新统计数据
        tvCorrectCount.setText(String.valueOf(correctCount));
        tvWrongCount.setText(String.valueOf(wrongCount));
        tvUnansweredCount.setText(String.valueOf(unansweredCount));
        
        // 计算总分 (简化计算)
        int totalScore = calculateTotalScore(correctCount);
        tvTotalScore.setText(totalScore + "分");
        
        // 计算正确率
        int answeredCount = correctCount + wrongCount;
        double accuracyRate = answeredCount > 0 ? (correctCount * 100.0 / answeredCount) : 0;
        tvAccuracyRate.setText(String.format("%.1f%%", accuracyRate));
        
        // 显示用时
        int hours = (int) (timeUsed / 1000) / 3600;
        int minutes = (int) ((timeUsed / 1000) % 3600) / 60;
        tvTimeUsed.setText(String.format("%d小时%d分钟", hours, minutes));
        
        // 显示排名 (模拟数据)
        int ranking = 100 - (int)(accuracyRate * 0.8);
        tvRanking.setText(String.format("超过 %d%% 的考生", Math.max(10, ranking)));
        
        // 显示各部分得分
        updateSectionScore(correctCount, wrongCount);
    }

    private int calculateTotalScore(int correctCount) {
        // 简化的评分规则
        // 完形填空: 20题×0.5分=10分
        // 阅读理解: 30题×2分=60分
        // 翻译: 10分
        // 写作: 30分
        // 这里简化为每题2分
        return correctCount * 2;
    }

    private void updateSectionScore(int correctCount, int wrongCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("完形填空: 7/10  ");
        sb.append("阅读理解: 32/40\n");
        sb.append("翻译: 6/10  ");
        sb.append("写作: 28/30");
        tvSectionScore.setText(sb.toString());
    }

    private String getCorrectAnswer(int questionNumber) {
        // 这里应该从题目数据中获取正确答案
        // 现在返回模拟数据
        if (questionNumber == 1) return "B";
        if (questionNumber == 21) return "C";
        return "A";
    }

    private String getExplanation(int questionNumber) {
        // 这里应该从题目数据中获取解析
        // 现在返回模拟解析
        if (questionNumber == 1) {
            return "【解析】本题考查固定搭配。be prone to 表示'易于遭受，倾向于'，符合语境。" +
                   "这个地区容易发生地震和海啸。其他选项：relevant 相关的；available 可用的；alien 陌生的，均不符合。";
        }
        if (questionNumber == 21) {
            return "【解析】本题考查细节理解。根据第二段'a \"rehearsal room\" approach to teaching Shakespeare'可知，" +
                   "这种方法要求学生扮演莎士比亚戏剧中的角色。C选项'play the roles in Shakespeare'正确。";
        }
        return "【解析】详细解析请参考解析手册。";
    }

    private String getSectionName(int questionNumber) {
        if (questionNumber >= 21 && questionNumber <= 25) return "阅读理解Text 1";
        if (questionNumber >= 26 && questionNumber <= 30) return "阅读理解Text 2";
        if (questionNumber >= 31 && questionNumber <= 35) return "阅读理解Text 3";
        if (questionNumber >= 36 && questionNumber <= 40) return "阅读理解Text 4";
        if (questionNumber >= 41 && questionNumber <= 45) return "新题型";
        if (questionNumber >= 46 && questionNumber <= 50) return "翻译";
        return "其他";
    }

    private void setupRecyclerView() {
        analysisAdapter = new AnalysisAdapter(analysisList);
        recyclerAnalysis.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnalysis.setAdapter(analysisAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // 加入错题本
        btnAddToWrongBook.setOnClickListener(v -> addWrongQuestionsToBook());
        
        // 查看错题本
        btnViewWrongBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, WrongQuestionActivity.class);
            startActivity(intent);
        });
        
        // 重新答题
        btnRetry.setOnClickListener(v -> {
            // 关闭当前页面，用户可以重新开始考试
            finish();
        });
    }
    
    /**
     * 将错题添加到错题本
     */
    private void addWrongQuestionsToBook() {
        new Thread(() -> {
            int addedCount = 0;
            
            for (AnalysisItem item : analysisList) {
                // 只添加答错的题目
                if (!item.isCorrect && !item.userAnswer.equals("未答")) {
                    WrongQuestionEntity wrongQuestion = new WrongQuestionEntity();
                    
                    // 设置题目内容
                    wrongQuestion.setQuestionText("第" + item.questionNumber + "题 - " + item.sectionName);
                    
                    // 设置选项（模拟数据，实际应从题目数据中获取）
                    String[] options = {"Option A", "Option B", "Option C", "Option D"};
                    wrongQuestion.setOptions(options);
                    
                    // 设置答案索引
                    wrongQuestion.setUserAnswerIndex(item.userAnswer.charAt(0) - 'A');
                    wrongQuestion.setCorrectAnswerIndex(item.correctAnswer.charAt(0) - 'A');
                    
                    // 设置解析和分类
                    wrongQuestion.setExplanation(item.explanation);
                    wrongQuestion.setCategory("真题练习");
                    wrongQuestion.setSource(examType != null ? examType : "考研英语");
                    
                    // 设置时间和计数
                    wrongQuestion.setWrongTime(new Date());
                    wrongQuestion.setWrongCount(1);
                    wrongQuestion.setMastered(false);
                    
                    // 保存到数据库
                    wrongQuestionRepository.addWrongQuestionSync(wrongQuestion);
                    addedCount++;
                }
            }
            
            final int finalCount = addedCount;
            runOnUiThread(() -> {
                if (finalCount > 0) {
                    Toast.makeText(this, "已添加 " + finalCount + " 道错题到错题本", 
                                 Toast.LENGTH_SHORT).show();
                    btnAddToWrongBook.setEnabled(false);
                    btnAddToWrongBook.setText("已加入错题本");
                } else {
                    Toast.makeText(this, "没有错题需要添加", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * 解析项数据类
     */
    private static class AnalysisItem {
        int questionNumber;
        String sectionName;
        String userAnswer;
        String correctAnswer;
        boolean isCorrect;
        String explanation;
    }

    /**
     * 解析列表适配器
     */
    private class AnalysisAdapter extends RecyclerView.Adapter<AnalysisAdapter.ViewHolder> {
        private List<AnalysisItem> items;
        private String currentExpandedSection = null;

        AnalysisAdapter(List<AnalysisItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exam_analysis, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AnalysisItem item = items.get(position);
            
            // 显示题号和section
            holder.tvQuestionNumber.setText("第" + item.questionNumber + "题");
            holder.tvSectionName.setText(item.sectionName);
            
            // 显示答案对比
            holder.tvUserAnswer.setText("你的答案: " + item.userAnswer);
            holder.tvCorrectAnswer.setText("正确答案: " + item.correctAnswer);
            
            // 设置正确/错误状态
            if (item.userAnswer.equals("未答")) {
                holder.ivStatus.setImageResource(R.drawable.ic_question_mark);
                holder.tvUserAnswer.setTextColor(ContextCompat.getColor(
                    ExamAnalysisActivity.this, R.color.text_secondary));
            } else if (item.isCorrect) {
                holder.ivStatus.setImageResource(R.drawable.ic_check_circle);
                holder.tvUserAnswer.setTextColor(ContextCompat.getColor(
                    ExamAnalysisActivity.this, android.R.color.holo_green_dark));
            } else {
                holder.ivStatus.setImageResource(R.drawable.ic_close_circle);
                holder.tvUserAnswer.setTextColor(ContextCompat.getColor(
                    ExamAnalysisActivity.this, android.R.color.holo_red_dark));
            }
            
            // 显示解析
            holder.tvExplanation.setText(item.explanation);
            
            // 点击展开/收起解析
            boolean isExpanded = item.sectionName.equals(currentExpandedSection);
            holder.layoutExplanation.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.tvToggleExplanation.setText(isExpanded ? "收起解析 ▲" : "查看解析 ▼");
            
            holder.tvToggleExplanation.setOnClickListener(v -> {
                if (isExpanded) {
                    currentExpandedSection = null;
                } else {
                    currentExpandedSection = item.sectionName;
                }
                notifyDataSetChanged();
            });
            
            // 整个item点击也可以展开/收起
            holder.itemView.setOnClickListener(v -> {
                if (isExpanded) {
                    currentExpandedSection = null;
                } else {
                    currentExpandedSection = item.sectionName;
                }
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivStatus;
            TextView tvQuestionNumber, tvSectionName;
            TextView tvUserAnswer, tvCorrectAnswer;
            TextView tvExplanation, tvToggleExplanation;
            LinearLayout layoutExplanation;

            ViewHolder(View itemView) {
                super(itemView);
                ivStatus = itemView.findViewById(R.id.iv_status);
                tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
                tvSectionName = itemView.findViewById(R.id.tv_section_name);
                tvUserAnswer = itemView.findViewById(R.id.tv_user_answer);
                tvCorrectAnswer = itemView.findViewById(R.id.tv_correct_answer);
                tvExplanation = itemView.findViewById(R.id.tv_explanation);
                tvToggleExplanation = itemView.findViewById(R.id.tv_toggle_explanation);
                layoutExplanation = itemView.findViewById(R.id.layout_explanation);
            }
        }
    }
}

