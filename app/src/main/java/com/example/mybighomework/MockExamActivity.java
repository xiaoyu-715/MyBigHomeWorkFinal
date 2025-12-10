package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.WrongQuestionEntity;
import com.example.mybighomework.database.entity.ExamRecordEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import com.example.mybighomework.repository.WrongQuestionRepository;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.repository.StudyRecordRepository;

import com.example.mybighomework.utils.ModuleStatisticsManager;
import com.example.mybighomework.utils.TaskCompletionManager;

public class MockExamActivity extends AppCompatActivity {

    // UI组件
    private ImageView btnBack;
    private TextView tvProgress, tvScore, tvQuestion, tvResult, tvTimeRemaining;
    private ImageView ivResult;
    private ProgressBar progressBar;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private Button btnNext, btnRestart, btnFinish, btnSubmit;
    private LinearLayout layoutOptions;
    private CardView layoutResult;
    private LinearLayout navHome, navReport, navProfile, navMore;
    private TextView tvExamType, tvExamTitle;

    // 考试数据
    private List<MockQuestion> mockQuestions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalQuestions = 30; // 模拟考试题目数量
    private boolean isAnswered = false;
    private String examType = "模拟考试";
    private CountDownTimer examTimer;
    private long timeLeftInMillis = 90 * 60 * 1000; // 90分钟
    private boolean isExamFinished = false;
    private long examStartTime = 0; // 考试开始时间
    
    private WrongQuestionRepository wrongQuestionRepository;
    private ExamRecordRepository examRecordRepository;
    private UserSettingsRepository userSettingsRepository;
    private StudyRecordRepository studyRecordRepository;

    // 模拟考试题目类
    private static class MockQuestion {
        String question;
        String[] options;
        int correctAnswer;
        String explanation;
        String category; // 题目类型：词汇、语法、阅读等

        MockQuestion(String question, String[] options, int correctAnswer, String explanation, String category) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.category = category;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_mock_exam);
            
            initDatabase();
            initViews();
            initMockExamData();
            setupClickListeners();
            setupBackPressedCallback();
            startExamTimer();
            showCurrentQuestion();
        } catch (Exception e) {
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }
    
    private void initDatabase() {
        AppDatabase database = AppDatabase.getInstance(this);
        wrongQuestionRepository = new WrongQuestionRepository(database.wrongQuestionDao());
        examRecordRepository = new ExamRecordRepository(database.examDao());
        userSettingsRepository = new UserSettingsRepository(this);
        studyRecordRepository = new StudyRecordRepository(database.studyRecordDao());
        
        // 记录考试开始时间
        examStartTime = System.currentTimeMillis();
    }
    
    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (examTimer != null) {
                    examTimer.cancel();
                }
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initViews() {
        try {
            // 顶部控件
            btnBack = findViewById(R.id.btn_back);
            tvProgress = findViewById(R.id.tv_progress);
            tvScore = findViewById(R.id.tv_score);
            progressBar = findViewById(R.id.progress_bar);
            tvExamType = findViewById(R.id.tv_exam_type);
            tvExamTitle = findViewById(R.id.tv_exam_title);
            tvTimeRemaining = findViewById(R.id.tv_time_remaining);

            // 题目显示区域
            tvQuestion = findViewById(R.id.tv_question);

            // 选项按钮
            btnOptionA = findViewById(R.id.btn_option_a);
            btnOptionB = findViewById(R.id.btn_option_b);
            btnOptionC = findViewById(R.id.btn_option_c);
            btnOptionD = findViewById(R.id.btn_option_d);

            // 结果显示区域
            layoutOptions = findViewById(R.id.layout_options);
            layoutResult = findViewById(R.id.layout_result);
            ivResult = findViewById(R.id.iv_result);
            tvResult = findViewById(R.id.tv_result);
            btnNext = findViewById(R.id.btn_next);

            // 底部操作按钮
            btnRestart = findViewById(R.id.btn_restart);
            btnFinish = findViewById(R.id.btn_finish);
            btnSubmit = findViewById(R.id.btn_submit);

            // 底部导航
            navHome = findViewById(R.id.nav_home);
            navReport = findViewById(R.id.nav_report);
            navProfile = findViewById(R.id.nav_profile);
            navMore = findViewById(R.id.nav_more);
            
            // 检查关键组件是否为null
            if (btnBack == null || tvProgress == null || tvQuestion == null || 
                btnOptionA == null || btnOptionB == null || btnOptionC == null || btnOptionD == null ||
                layoutOptions == null || layoutResult == null || tvTimeRemaining == null) {
                Toast.makeText(this, "布局初始化失败", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "初始化视图时发生错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initMockExamData() {
        mockQuestions = new ArrayList<>();
        
        // 词汇题
        mockQuestions.add(new MockQuestion(
            "The company's new policy will have a significant _______ on employee productivity.",
            new String[]{"impact", "compact", "contact", "contract"},
            0,
            "impact意为影响，符合句意：公司的新政策将对员工生产力产生重大影响。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "She was _______ to find that her application had been accepted.",
            new String[]{"delighted", "delayed", "deleted", "delivered"},
            0,
            "delighted意为高兴的，符合句意：她很高兴发现自己的申请被接受了。",
            "词汇"
        ));

        // 语法题
        mockQuestions.add(new MockQuestion(
            "If I _______ more time, I would have finished the project yesterday.",
            new String[]{"had had", "have had", "had", "have"},
            0,
            "这是虚拟语气的用法，表示与过去事实相反的假设，条件句用过去完成时。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The book _______ by millions of people around the world.",
            new String[]{"has been read", "has read", "was reading", "reads"},
            0,
            "这里需要现在完成时的被动语态，表示书被全世界数百万人阅读。",
            "语法"
        ));

        // 阅读理解题
        mockQuestions.add(new MockQuestion(
            "According to the passage, what is the main advantage of renewable energy?",
            new String[]{"It's environmentally friendly", "It's cheaper", "It's more reliable", "It's easier to install"},
            0,
            "根据文章内容，可再生能源的主要优势是环保。",
            "阅读"
        ));

        // 添加更多四六级词汇题
        mockQuestions.add(new MockQuestion(
            "The professor's lecture was so _______ that many students fell asleep.",
            new String[]{"boring", "interested", "exciting", "fascinating"},
            0,
            "boring意为无聊的，符合句意：教授的讲座如此无聊以至于很多学生睡着了。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The company needs to _______ its marketing strategy to attract younger consumers.",
            new String[]{"adjust", "object", "reject", "project"},
            0,
            "adjust意为调整，符合句意：公司需要调整营销策略以吸引年轻消费者。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The research team will _______ the experiment next month.",
            new String[]{"conduct", "confuse", "conclude", "construct"},
            0,
            "conduct意为进行、实施，符合句意：研究团队将在下个月进行实验。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The new law will _______ stricter regulations on environmental protection.",
            new String[]{"impose", "compose", "suppose", "propose"},
            0,
            "impose意为实施、强加，符合句意：新法律将实施更严格的环保法规。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "Students must _______ their essays before the deadline.",
            new String[]{"submit", "admit", "permit", "commit"},
            0,
            "submit意为提交，符合句意：学生必须在截止日期前提交论文。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The manager decided to _______ the meeting until next week.",
            new String[]{"postpone", "propose", "suppose", "dispose"},
            0,
            "postpone意为推迟，符合句意：经理决定将会议推迟到下周。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The medicine should help _______ your pain.",
            new String[]{"relieve", "believe", "achieve", "receive"},
            0,
            "relieve意为缓解，符合句意：这种药应该能帮助缓解你的疼痛。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The company aims to _______ its profits by 20% this year.",
            new String[]{"increase", "decrease", "cease", "release"},
            0,
            "increase意为增加，符合句意：公司的目标是今年利润增长20%。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The teacher asked students to _______ their opinions during the discussion.",
            new String[]{"express", "impress", "suppress", "compress"},
            0,
            "express意为表达，符合句意：老师要求学生在讨论中表达自己的观点。",
            "词汇"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The government will _______ new measures to combat climate change.",
            new String[]{"implement", "compliment", "supplement", "experiment"},
            0,
            "implement意为实施，符合句意：政府将实施新措施来应对气候变化。",
            "词汇"
        ));
        
        // 语法题
        mockQuestions.add(new MockQuestion(
            "By the time you arrive, we _______ waiting for more than an hour.",
            new String[]{"will have been", "will be", "have been", "had been"},
            0,
            "将来完成进行时，表示到将来某时已经进行了一段时间的动作。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "_______ carefully, the essay would have received a higher grade.",
            new String[]{"Had it been written", "If it was written", "Was it written", "It was written"},
            0,
            "虚拟语气的倒装形式，表示与过去事实相反的假设。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The news _______ everyone in the office yesterday.",
            new String[]{"surprised", "was surprised", "surprising", "was surprising"},
            0,
            "surprise作及物动词，主动形式表示'使惊讶'，主语是news。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "Not until he failed the exam _______ how important study was.",
            new String[]{"did he realize", "he realized", "he did realize", "realized he"},
            0,
            "not until位于句首时，主句需要部分倒装。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The problem is _______ difficult that nobody can solve it.",
            new String[]{"so", "such", "very", "too"},
            0,
            "so...that结构，so修饰形容词difficult。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "I would rather you _______ home now.",
            new String[]{"went", "go", "will go", "have gone"},
            0,
            "would rather后接宾语从句时，从句用虚拟语气，表示现在或将来用过去时。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "It is high time that we _______ action to protect the environment.",
            new String[]{"took", "take", "will take", "have taken"},
            0,
            "It is high time that结构中，从句用虚拟语气，动词用过去时。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "_______ the bad weather, the flight was cancelled.",
            new String[]{"Because of", "Because", "Although", "Despite"},
            0,
            "because of后接名词或名词短语，because后接句子。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The house _______ last year is now for sale.",
            new String[]{"built", "building", "to build", "builds"},
            0,
            "过去分词作后置定语，表示被动和完成。",
            "语法"
        ));
        
        mockQuestions.add(new MockQuestion(
            "_______ more attention, the trees could have grown better.",
            new String[]{"Given", "Giving", "To give", "Give"},
            0,
            "过去分词作条件状语，表示被动关系。",
            "语法"
        ));
        
        // 阅读理解题
        mockQuestions.add(new MockQuestion(
            "What is the main idea of the passage about environmental protection?",
            new String[]{"Individual actions are important", "Government policies are useless", "Technology solves all problems", "Money is the only solution"},
            0,
            "文章主旨是强调个人行动对环境保护的重要性。",
            "阅读"
        ));
        
        mockQuestions.add(new MockQuestion(
            "According to the passage, what is the best way to learn a foreign language?",
            new String[]{"Practice speaking regularly", "Only read textbooks", "Avoid making mistakes", "Study grammar rules only"},
            0,
            "文章指出学习外语的最佳方法是定期练习口语。",
            "阅读"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The author's attitude toward social media can be described as _______.",
            new String[]{"cautiously optimistic", "completely negative", "extremely positive", "totally indifferent"},
            0,
            "作者对社交媒体持谨慎乐观的态度。",
            "阅读"
        ));
        
        mockQuestions.add(new MockQuestion(
            "Which of the following is NOT mentioned as a benefit of exercise?",
            new String[]{"Improving intelligence instantly", "Reducing stress", "Improving sleep quality", "Strengthening immune system"},
            0,
            "立即提高智力不是文中提到的运动益处。",
            "阅读"
        ));
        
        mockQuestions.add(new MockQuestion(
            "What can be inferred about the future of renewable energy?",
            new String[]{"It will become more affordable", "It will disappear soon", "It's too expensive to develop", "Nobody is interested in it"},
            0,
            "可以推断出可再生能源的未来会变得更加经济实惠。",
            "阅读"
        ));
        
        mockQuestions.add(new MockQuestion(
            "The word 'substantial' in paragraph 3 is closest in meaning to _______.",
            new String[]{"significant", "small", "trivial", "temporary"},
            0,
            "substantial意为'大量的、重要的'，与significant意思最接近。",
            "阅读"
        ));

        // 打乱顺序
        Collections.shuffle(mockQuestions);
        totalQuestions = Math.min(mockQuestions.size(), 30);
    }

    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> {
            if (examTimer != null) {
                examTimer.cancel();
            }
            finish();
        });

        // 选项按钮
        btnOptionA.setOnClickListener(v -> selectOption(0));
        btnOptionB.setOnClickListener(v -> selectOption(1));
        btnOptionC.setOnClickListener(v -> selectOption(2));
        btnOptionD.setOnClickListener(v -> selectOption(3));

        // 下一题按钮
        btnNext.setOnClickListener(v -> nextQuestion());

        // 提交考试按钮
        btnSubmit.setOnClickListener(v -> submitExam());

        // 重新开始按钮
        btnRestart.setOnClickListener(v -> restartExam());

        // 完成考试按钮
        btnFinish.setOnClickListener(v -> finishExam());

        // 底部导航点击事件
        navHome.setOnClickListener(v -> {
            if (examTimer != null) {
                examTimer.cancel();
            }
            Intent intent = new Intent(MockExamActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        navReport.setOnClickListener(v -> {
            if (examTimer != null) {
                examTimer.cancel();
            }
            Intent intent = new Intent(MockExamActivity.this, ReportActivity.class);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            if (examTimer != null) {
                examTimer.cancel();
            }
            Intent intent = new Intent(MockExamActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        navMore.setOnClickListener(v -> {
            if (examTimer != null) {
                examTimer.cancel();
            }
            Intent intent = new Intent(MockExamActivity.this, MoreActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void startExamTimer() {
        examTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateTimerDisplay();
                Toast.makeText(MockExamActivity.this, "考试时间到！自动提交", Toast.LENGTH_LONG).show();
                submitExam();
            }
        }.start();
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimeRemaining.setText("剩余时间: " + timeFormatted);
        
        // 时间不足时改变颜色
        if (timeLeftInMillis < 10 * 60 * 1000) { // 少于10分钟
            tvTimeRemaining.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    private void showCurrentQuestion() {
        if (mockQuestions == null || mockQuestions.isEmpty()) {
            Toast.makeText(this, "题目数据加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (currentQuestionIndex >= totalQuestions || currentQuestionIndex >= mockQuestions.size()) {
            showFinalResult();
            return;
        }

        MockQuestion currentQuestion = mockQuestions.get(currentQuestionIndex);
        
        if (currentQuestion == null || currentQuestion.question == null || currentQuestion.options == null) {
            Toast.makeText(this, "题目数据异常", Toast.LENGTH_SHORT).show();
            nextQuestion();
            return;
        }
        
        // 更新进度
        tvProgress.setText((currentQuestionIndex + 1) + "/" + totalQuestions);
        progressBar.setProgress((currentQuestionIndex + 1) * 100 / totalQuestions);
        tvScore.setText("得分: " + score);
        tvExamType.setText(examType);
        tvExamTitle.setText("四级模拟考试");

        // 显示题目
        tvQuestion.setText("第" + (currentQuestionIndex + 1) + "题 [" + currentQuestion.category + "]：" + currentQuestion.question);

        // 设置选项
        if (currentQuestion.options.length >= 4) {
            btnOptionA.setText("A. " + currentQuestion.options[0]);
            btnOptionB.setText("B. " + currentQuestion.options[1]);
            btnOptionC.setText("C. " + currentQuestion.options[2]);
            btnOptionD.setText("D. " + currentQuestion.options[3]);
        } else {
            Toast.makeText(this, "选项数据不完整", Toast.LENGTH_SHORT).show();
            nextQuestion();
            return;
        }

        // 重置按钮状态
        resetOptionButtons();

        // 显示选项，隐藏结果
        layoutOptions.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        
        isAnswered = false;
    }

    private void selectOption(int selectedOption) {
        if (isAnswered || isExamFinished) return;
        
        if (mockQuestions == null || currentQuestionIndex >= mockQuestions.size()) {
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
            return;
        }

        isAnswered = true;
        MockQuestion currentQuestion = mockQuestions.get(currentQuestionIndex);
        
        if (currentQuestion == null || currentQuestion.options == null) {
            Toast.makeText(this, "题目数据异常", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedOption < 0 || selectedOption >= currentQuestion.options.length) {
            Toast.makeText(this, "选项索引异常", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查答案
        boolean isCorrect = selectedOption == currentQuestion.correctAnswer;
        
        if (isCorrect) {
            score += 3; // 模拟考试每题3分
            ivResult.setImageResource(R.drawable.ic_check);
            tvResult.setText("正确！\n" + (currentQuestion.explanation != null ? currentQuestion.explanation : ""));
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            
            // 答对题目时增加模拟考试模块统计数
            ModuleStatisticsManager.getInstance(this).incrementMockExamCorrectCount();
        } else {
            ivResult.setImageResource(R.drawable.ic_close);
            String correctAnswerText = "";
            if (currentQuestion.correctAnswer >= 0 && currentQuestion.correctAnswer < currentQuestion.options.length) {
                correctAnswerText = currentQuestion.options[currentQuestion.correctAnswer];
            }
            tvResult.setText("错误！正确答案是: " + correctAnswerText + 
                           "\n" + (currentQuestion.explanation != null ? currentQuestion.explanation : ""));
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            
            saveWrongQuestion(currentQuestion, selectedOption);
        }

        // 高亮正确答案和用户选择
        highlightAnswers(selectedOption, currentQuestion.correctAnswer);

        // 显示结果区域
        layoutResult.setVisibility(View.VISIBLE);
        
        // 更新得分显示
        tvScore.setText("得分: " + score);
        
        // 【任务完成跟踪】每答一题，累计计数，达到20题自动完成任务
        TaskCompletionManager.getInstance(this).incrementExamAnswerCount();
    }

    private void saveWrongQuestion(MockQuestion item, int userAnswerIndex) {
        WrongQuestionEntity wrongQuestion = new WrongQuestionEntity();
        wrongQuestion.setQuestionText(item.question);
        wrongQuestion.setOptions(item.options);
        wrongQuestion.setCorrectAnswerIndex(item.correctAnswer);
        wrongQuestion.setUserAnswerIndex(userAnswerIndex);
        wrongQuestion.setExplanation(item.explanation);
        wrongQuestion.setCategory("模拟考试");
        wrongQuestion.setSource("MockExamActivity");
        wrongQuestion.setWrongTime(new Date());
        wrongQuestionRepository.addWrongQuestion(wrongQuestion);
    }

    private void highlightAnswers(int selectedOption, int correctOption) {
        Button[] buttons = {btnOptionA, btnOptionB, btnOptionC, btnOptionD};
        
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == null) continue;
            
            if (i == correctOption) {
                // 正确答案的样式
                buttons[i].setBackgroundResource(R.drawable.btn_correct_background);
            } else if (i == selectedOption) {
                // 用户选择的错误答案的样式
                buttons[i].setBackgroundResource(R.drawable.btn_error_background);
            } else {
                // 其他未选择的错误答案，恢复默认样式并禁用
                buttons[i].setBackgroundResource(R.drawable.btn_default_background);
            }
            // 禁用所有按钮，防止重复选择
            buttons[i].setEnabled(false);
        }
    }

    private void resetOptionButtons() {
        Button[] buttons = {btnOptionA, btnOptionB, btnOptionC, btnOptionD};
        
        for (Button button : buttons) {
            if (button != null) {
                button.setBackgroundResource(R.drawable.btn_default_background);
                button.setEnabled(true);
            }
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showCurrentQuestion();
    }

    private void submitExam() {
        if (examTimer != null) {
            examTimer.cancel();
        }
        isExamFinished = true;
        showFinalResult();
    }

    private void restartExam() {
        currentQuestionIndex = 0;
        score = 0;
        isAnswered = false;
        isExamFinished = false;
        timeLeftInMillis = 90 * 60 * 1000;
        
        if (examTimer != null) {
            examTimer.cancel();
        }
        
        if (mockQuestions != null && !mockQuestions.isEmpty()) {
            Collections.shuffle(mockQuestions);
        }
        
        startExamTimer();
        showCurrentQuestion();
        Toast.makeText(this, "重新开始模拟考试", Toast.LENGTH_SHORT).show();
    }

    private void finishExam() {
        submitExam();
    }

    private void showFinalResult() {
        if (examTimer != null) {
            examTimer.cancel();
        }
        
        isExamFinished = true;
        String message = "模拟考试完成！\n总得分: " + score + "/" + (totalQuestions * 3) + "\n";
        
        double percentage = (double) score / (totalQuestions * 3) * 100;
        if (percentage >= 85) {
            message += "优秀！超过四级标准！";
        } else if (percentage >= 70) {
            message += "良好！达到四级水平！";
        } else if (percentage >= 60) {
            message += "及格！继续努力！";
        } else {
            message += "需要加强练习！";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // 保存考试记录到数据库（等待完成后再关闭）
        saveExamRecordAndFinish();
    }
    
    /**
     * 保存考试记录到数据库
     */
    private void saveExamRecord() {
        new Thread(() -> {
            try {
                ExamRecordEntity examRecord = new ExamRecordEntity();
                examRecord.setExamType("四级"); // 默认为四级
                examRecord.setExamMode("模拟考试");
                examRecord.setTotalQuestions(totalQuestions);
                
                // 计算正确和错误数量
                int correctAnswers = score / 3; // 每题3分
                int wrongAnswers = totalQuestions - correctAnswers;
                
                examRecord.setCorrectAnswers(correctAnswers);
                examRecord.setWrongAnswers(wrongAnswers);
                examRecord.setScore(score);
                
                // 计算考试用时
                long duration = (90 * 60 * 1000) - timeLeftInMillis;
                examRecord.setDuration(duration);
                
                examRecord.setExamTime(examStartTime);
                
                // 保存到数据库
                examRecordRepository.addExamRecord(examRecord);
                
                // 【统一时间记录】记录模拟考试时长到用户设置
                userSettingsRepository.recordStudyTime(duration, "mock_exam");
                
                // 【图表数据】同时创建学习记录用于图表显示
                StudyRecordEntity studyRecord = new StudyRecordEntity();
                studyRecord.setStudyType("mock_exam");
                studyRecord.setQuestionId(null);
                studyRecord.setVocabularyId(null);
                studyRecord.setCorrect(correctAnswers > wrongAnswers);
                studyRecord.setResponseTime(duration);
                studyRecord.setScore(score);
                studyRecord.setStudyDate(new java.util.Date()); // 显式设置学习日期
                studyRecord.setNotes("模拟考试 - 得分:" + score);
                studyRecordRepository.addStudyRecord(studyRecord);
                
                runOnUiThread(() -> {
                    Toast.makeText(MockExamActivity.this, "考试成绩已保存", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MockExamActivity.this, "保存成绩失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    /**
     * 保存考试记录到数据库并在完成后关闭页面
     */
    private void saveExamRecordAndFinish() {
        new Thread(() -> {
            try {
                ExamRecordEntity examRecord = new ExamRecordEntity();
                examRecord.setExamType("四级"); // 默认为四级
                examRecord.setExamMode("模拟考试");
                examRecord.setTotalQuestions(totalQuestions);
                
                // 计算正确和错误数量
                int correctAnswers = score / 3; // 每题3分
                int wrongAnswers = totalQuestions - correctAnswers;
                
                examRecord.setCorrectAnswers(correctAnswers);
                examRecord.setWrongAnswers(wrongAnswers);
                examRecord.setScore(score);
                
                // 计算考试用时
                long duration = (90 * 60 * 1000) - timeLeftInMillis;
                examRecord.setDuration(duration);
                
                examRecord.setExamTime(examStartTime);
                
                // 保存到数据库
                examRecordRepository.addExamRecord(examRecord);
                
                // 【统一时间记录】记录模拟考试时长到用户设置
                userSettingsRepository.recordStudyTime(duration, "mock_exam");
                
                // 【图表数据】同时创建学习记录用于图表显示
                StudyRecordEntity studyRecord = new StudyRecordEntity();
                studyRecord.setStudyType("mock_exam");
                studyRecord.setQuestionId(null);
                studyRecord.setVocabularyId(null);
                studyRecord.setCorrect(correctAnswers > wrongAnswers);
                studyRecord.setResponseTime(duration);
                studyRecord.setScore(score);
                studyRecord.setStudyDate(new java.util.Date()); // 显式设置学习日期
                studyRecord.setNotes("模拟考试 - 得分:" + score);
                studyRecordRepository.addStudyRecord(studyRecord);
                
                runOnUiThread(() -> {
                    Toast.makeText(MockExamActivity.this, "考试成绩已保存", Toast.LENGTH_SHORT).show();
                    // 数据保存完成后再关闭页面
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MockExamActivity.this, "保存成绩失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // 即使出错也关闭页面
                    finish();
                });
            }
        }).start();
    }
    
    /**
     * 更新学习连续天数
     * 注意：现在学习连续天数已经在recordStudyTime中自动更新，这个方法保留用于兼容性
     */
    private void updateStudyStreak() {
        new Thread(() -> {
            try {
                userSettingsRepository.updateStudyStreak();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (examTimer != null) {
            examTimer.cancel();
        }
    }
}