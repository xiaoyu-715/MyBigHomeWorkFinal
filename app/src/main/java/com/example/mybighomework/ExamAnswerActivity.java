package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.ExamAnswerEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import com.example.mybighomework.database.entity.ExamResultEntity;
import com.example.mybighomework.repository.ExamAnswerRepository;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.repository.StudyRecordRepository;
import com.example.mybighomework.repository.QuestionNoteRepository;
import com.example.mybighomework.database.repository.ExamResultRepository;
import com.example.mybighomework.api.ZhipuAIService;
import com.example.mybighomework.utils.TaskProgressTracker;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamAnswerActivity extends AppCompatActivity {

    // UI组件
    private ImageView btnBack;
    private TextView tvExamTitle, tvTimer, tvProgress, tvSection, tvQuestionType;
    private TextView tvPassageTitle, tvPassage, tvQuestion, tvResult;
    private TextView tvBottomQuestion; // 底部选项窗口中的题目问题
    private ImageView ivResult;
    private ProgressBar progressBar;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private Button btnOptionE, btnOptionF, btnOptionG, btnOptionH; // 新题型额外选项
    private Button btnPrevious, btnNext;
    private EditText etTranslation, etWriting;
    private LinearLayout layoutOptions, layoutTranslation, layoutWriting;
    private androidx.cardview.widget.CardView layoutResult;
    
    // 新增功能按钮
    private ImageView btnSelectQuestionType, btnNote, btnAnswerSheet;
    
    // BottomSheet相关
    private LinearLayout bottomSheetOptions;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private LinearLayout layoutQuestionNumbers;
    private ScrollView scrollPassage;

    // 考试数据
    private List<ExamQuestion> questions;
    private int currentQuestionIndex = 0;
    private CountDownTimer examTimer;
    private long timeLeftInMillis = 3 * 60 * 60 * 1000; // 3小时考试时间（考研英语）
    private String examTitle, examYear, examType;
    private double difficulty;

    private ExamAnswerRepository examAnswerRepository;
    private UserSettingsRepository userSettingsRepository; // 用于记录学习时长
    private StudyRecordRepository studyRecordRepository; // 用于图表数据显示
    private QuestionNoteRepository questionNoteRepository; // 题目笔记仓库
    private ExamResultRepository examResultRepository; // 考试成绩仓库
    private ZhipuAIService zhipuAIService; // 智谱AI服务
    private Map<Integer, String> userAnswers; // 存储用户答案
    private int[] currentRange = null; // 当前题号范围缓存
    private long examStartTime; // 考试开始时间（毫秒）
    private ProgressDialog gradingDialog; // 批改进度对话框
    private Handler mainHandler; // 主线程Handler

    // 题目类型枚举
    private enum QuestionType {
        CLOZE_TEST,      // 完形填空
        READING_COMPREHENSION, // 阅读理解
        TRANSLATION,     // 翻译
        WRITING          // 写作
    }

    // 考试题目类
    private static class ExamQuestion {
        QuestionType type;
        String title;
        String passage; // 文章内容
        String question; // 题目内容
        String[] options; // 选项
        int correctAnswer; // 正确答案索引
        String explanation; // 答案解析
        String referenceAnswer; // 参考答案（翻译题的标准译文）
        String writingType; // 写作类型（"PART_A" 或 "PART_B"）

        // 原有构造函数（兼容现有代码）
        ExamQuestion(QuestionType type, String title, String passage, String question,
                    String[] options, int correctAnswer, String explanation) {
            this.type = type;
            this.title = title;
            this.passage = passage;
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.referenceAnswer = null;
            this.writingType = null;
        }
        
        // 扩展构造函数（支持参考答案和写作类型）
        ExamQuestion(QuestionType type, String title, String passage, String question,
                    String[] options, int correctAnswer, String explanation,
                    String referenceAnswer, String writingType) {
            this.type = type;
            this.title = title;
            this.passage = passage;
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.referenceAnswer = referenceAnswer;
            this.writingType = writingType;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            android.util.Log.d("ExamAnswerActivity", "开始初始化ExamAnswerActivity");

            setContentView(R.layout.activity_exam_answer);
            android.util.Log.d("ExamAnswerActivity", "布局文件加载完成");

            // 获取考试信息
            Intent intent = getIntent();
            if (intent != null) {
                examTitle = intent.getStringExtra("exam_title");
                examYear = intent.getStringExtra("exam_year");
                examType = intent.getStringExtra("exam_type");
                difficulty = intent.getDoubleExtra("difficulty", 4.0);

                android.util.Log.d("ExamAnswerActivity", "接收到的考试信息: " + examTitle + ", " + examYear + ", " + examType + ", " + difficulty);
            } else {
                android.util.Log.e("ExamAnswerActivity", "Intent为空");
                Toast.makeText(this, "启动参数错误", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            android.util.Log.d("ExamAnswerActivity", "开始初始化数据库");
            initDatabase();

            android.util.Log.d("ExamAnswerActivity", "开始初始化视图");
            initViews();

            android.util.Log.d("ExamAnswerActivity", "开始初始化考试数据");
            initExamData();

            android.util.Log.d("ExamAnswerActivity", "初始化用户答案存储");
            userAnswers = new HashMap<>();
            
            android.util.Log.d("ExamAnswerActivity", "初始化题号切换器");
            initQuestionNumbers();

            android.util.Log.d("ExamAnswerActivity", "开始设置点击监听器");
            setupClickListeners();

            android.util.Log.d("ExamAnswerActivity", "开始启动考试计时器");
            startExamTimer();

            android.util.Log.d("ExamAnswerActivity", "开始显示第一题");
            showCurrentQuestion();

            android.util.Log.d("ExamAnswerActivity", "ExamAnswerActivity初始化完成");
        } catch (Exception e) {
            android.util.Log.e("ExamAnswerActivity", "Activity初始化失败", e);
            Toast.makeText(this, "页面加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void initDatabase() {
        AppDatabase database = AppDatabase.getInstance(this);
        examAnswerRepository = new ExamAnswerRepository(database.examAnswerDao());
        userSettingsRepository = new UserSettingsRepository(this);
        studyRecordRepository = new StudyRecordRepository(database.studyRecordDao());
        questionNoteRepository = new QuestionNoteRepository(database.questionNoteDao());
        examResultRepository = new ExamResultRepository(this);
        
        // 初始化智谱AI服务
        SharedPreferences prefs = getSharedPreferences("zhipuai_config", MODE_PRIVATE);
        String apiKey = prefs.getString("api_key", "");
        zhipuAIService = new ZhipuAIService(apiKey);
        
        // 初始化主线程Handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 记录考试开始时间
        examStartTime = System.currentTimeMillis();
    }

    private void initViews() {
        try {
            // 顶部控件
            btnBack = findViewById(R.id.btn_back);
            tvExamTitle = findViewById(R.id.tv_exam_title);
            tvTimer = findViewById(R.id.tv_timer);
            tvProgress = findViewById(R.id.tv_progress);
            progressBar = findViewById(R.id.progress_bar);
            tvSection = findViewById(R.id.tv_section);

            // 题目内容
            tvQuestionType = findViewById(R.id.tv_question_type);
            tvPassageTitle = findViewById(R.id.tv_passage_title);
            tvPassage = findViewById(R.id.tv_passage);
            tvQuestion = findViewById(R.id.tv_question);

            // 选项按钮
            btnOptionA = findViewById(R.id.btn_option_a);
            btnOptionB = findViewById(R.id.btn_option_b);
            btnOptionC = findViewById(R.id.btn_option_c);
            btnOptionD = findViewById(R.id.btn_option_d);
            btnOptionE = findViewById(R.id.btn_option_e);
            btnOptionF = findViewById(R.id.btn_option_f);
            btnOptionG = findViewById(R.id.btn_option_g);
            btnOptionH = findViewById(R.id.btn_option_h);

            // 输入区域
            etTranslation = findViewById(R.id.et_translation);
            etWriting = findViewById(R.id.et_writing);

            // 布局容器
            layoutOptions = findViewById(R.id.layout_options);
            layoutTranslation = findViewById(R.id.layout_translation);
            layoutWriting = findViewById(R.id.layout_writing);
            layoutResult = findViewById(R.id.layout_result);

            // 结果显示
            ivResult = findViewById(R.id.iv_result);
            tvResult = findViewById(R.id.tv_result);

            // 底部按钮
            btnPrevious = findViewById(R.id.btn_previous);
            btnNext = findViewById(R.id.btn_next);
            
            // 新增功能按钮
            btnSelectQuestionType = findViewById(R.id.btn_select_question_type);
            btnNote = findViewById(R.id.btn_note);
            btnAnswerSheet = findViewById(R.id.btn_answer_sheet);

            // BottomSheet相关
            bottomSheetOptions = findViewById(R.id.bottom_sheet_options);
            layoutQuestionNumbers = findViewById(R.id.layout_question_numbers);
            scrollPassage = findViewById(R.id.scroll_passage);
            tvBottomQuestion = findViewById(R.id.tv_bottom_question);
            
            // 初始化BottomSheet行为
            if (bottomSheetOptions != null) {
                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetOptions);
                bottomSheetBehavior.setPeekHeight(360);  // 默认显示高度（增加了题目问题显示区域）
                bottomSheetBehavior.setHideable(false);  // 不允许完全隐藏
            }

            // 设置考试标题
            if (tvExamTitle != null && examTitle != null) {
                tvExamTitle.setText(examTitle);
            }

            android.util.Log.d("ExamAnswerActivity", "视图初始化完成");
        } catch (Exception e) {
            android.util.Log.e("ExamAnswerActivity", "视图初始化失败", e);
            Toast.makeText(this, "视图初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            throw e;
        }
    }

    private void initExamData() {
        try {
            android.util.Log.d("ExamAnswerActivity", "开始初始化考试数据");
            questions = new ArrayList<>();

        // 完形填空题目 (20题，每题0.5分，共10分) - 2025年考研英语二真题
        String clozePassage = "There are many understandable reasons why you might find it difficult to ask for help when you need it. Psychologists have been interested in this __1__ for decades, not least because people's widespread __2__ to ask for help has led to some high-profile failures.\n\n" +
            "Asking for help takes __3__. It involves communicating a need on your part — there's something you can't do. __4__, you're broadcasting your own weakness, which can be __5__. You might have __6__ about losing control of whatever it is you are asking for help with. __7__ someone starts to help, perhaps they will take over, or get a credit for your early efforts. Yet another __8__ that you might be worried about is being a nuisance or __9__ the person you go to for help.\n\n" +
            "If you struggle with low self-esteem, you might find it especially difficult to __10__ for help because you have the added worry of the other person __11__ your request. You might see such refusals as implying something __12__ about the status of your relationship with them. To __13__ these difficulties, try to remind yourself that everyone needs help sometimes. Nobody knows everything and can do everything all by themselves. And while you might __14__ coming across as incompetent, there's actually research that shows that advice-seekers are __15__ as more competent, not less.\n\n" +
            "Perhaps most encouraging of all is a paper from 2022 by researchers at Stanford University, in California, that involved a mix of contrived help-seeking interactions and asking people to __16__ times they'd sought help in the past. The findings showed that help-seekers generally underestimate how __17__ other people will be to help and how good it will make the help-giver feel (for most people, having the chance to help someone is highly __18__).\n\n" +
            "So bear all this in mind the next time you need to ask for help. __19__, take care over who you ask and when you ask them. And if someone can't help right now, avoid taking it personally. They might just be too __20__, or they might not feel confident about their ability to help.";
        
        // 第1题：question
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第1题",
            clozePassage,
            "1. Psychologists have been interested in this ____ for decades",
            new String[]{"A. illusion", "B. discussion", "C. tradition", "D. question"},
            3, // 答案是D
            "答案：D. question。心理学家对这个\"问题\"感兴趣了几十年。"
        ));
        
        // 第2题：reluctance
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第2题",
            clozePassage,
            "2. people's widespread ____ to ask for help",
            new String[]{"A. reluctance", "B. ambition", "C. tendency", "D. enthusiasm"},
            0, // 答案是A
            "答案：A. reluctance。人们普遍不愿意寻求帮助。"
        ));
        
        // 第3题：courage
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第3题",
            clozePassage,
            "3. Asking for help takes ____",
            new String[]{"A. attention", "B. talent", "C. courage", "D. patience"},
            2, // 答案是C
            "答案：C. courage。寻求帮助需要勇气。"
        ));
        
        // 第4题：In other words
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第4题",
            clozePassage,
            "4. ____, you're broadcasting your own weakness",
            new String[]{"A. At any time", "B. In other words", "C. By all means", "D. On the contrary"},
            1, // 答案是B
            "答案：B. In other words。换句话说，你在展示自己的弱点。"
        ));
        
        // 第5题：uncomfortable
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第5题",
            clozePassage,
            "5. broadcasting your own weakness, which can be ____",
            new String[]{"A. unrealistic", "B. deceptive", "C. tiresome", "D. uncomfortable"},
            3, // 答案是D
            "答案：D. uncomfortable。展示弱点会让人感到不舒服。"
        ));
        
        // 第6题：concerns
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第6题",
            clozePassage,
            "6. You might have ____ about losing control",
            new String[]{"A. doubts", "B. concerns", "C. suggestions", "D. secrets"},
            1, // 答案是B
            "答案：B. concerns。你可能担心失去控制。"
        ));
        
        // 第7题：Once
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第7题",
            clozePassage,
            "7. ____ someone starts to help",
            new String[]{"A. Once", "B. Unless", "C. Although", "D. Before"},
            0, // 答案是A
            "答案：A. Once。一旦有人开始帮忙。"
        ));
        
        // 第8题：factor
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第8题",
            clozePassage,
            "8. Yet another ____ that you might be worried about",
            new String[]{"A. theory", "B. choice", "C. factor", "D. context"},
            2, // 答案是C
            "答案：C. factor。另一个你可能担心的因素。"
        ));
        
        // 第9题：inconveniencing
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第9题",
            clozePassage,
            "9. being a nuisance or ____ the person",
            new String[]{"A. overpraising", "B. outperforming", "C. reassessing", "D. inconveniencing"},
            3, // 答案是D
            "答案：D. inconveniencing。成为麻烦或给别人带来不便。"
        ));
        
        // 第10题：reach out
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第10题",
            clozePassage,
            "10. difficult to ____ for help",
            new String[]{"A. reach out", "B. settle down", "C. turn over", "D. look back"},
            0, // 答案是A
            "答案：A. reach out。难以伸手求助。"
        ));
        
        // 第11题：declining
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第11题",
            clozePassage,
            "11. the other person ____ your request",
            new String[]{"A. declining", "B. considering", "C. criticizing", "D. evaluating"},
            0, // 答案是A
            "答案：A. declining。担心别人拒绝你的请求。"
        ));
        
        // 第12题：negative
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第12题",
            clozePassage,
            "12. implying something ____ about the status",
            new String[]{"A. unnecessary", "B. negative", "C. strange", "D. impractical"},
            1, // 答案是B
            "答案：B. negative。暗示关系的负面状态。"
        ));
        
        // 第13题：overcome
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第13题",
            clozePassage,
            "13. To ____ these difficulties",
            new String[]{"A. explain", "B. identify", "C. predict", "D. overcome"},
            3, // 答案是D
            "答案：D. overcome。克服这些困难。"
        ));
        
        // 第14题：fear
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第14题",
            clozePassage,
            "14. you might ____ coming across as incompetent",
            new String[]{"A. deny", "B. forget", "C. miss", "D. fear"},
            3, // 答案是D
            "答案：D. fear。你可能害怕显得无能。"
        ));
        
        // 第15题：perceived
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第15题",
            clozePassage,
            "15. advice-seekers are ____ as more competent",
            new String[]{"A. disguised", "B. perceived", "C. followed", "D. introduced"},
            1, // 答案是B
            "答案：B. perceived。寻求建议的人被认为更有能力。"
        ));
        
        // 第16题：recall
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第16题",
            clozePassage,
            "16. asking people to ____ times they'd sought help",
            new String[]{"A. recall", "B. classify", "C. analyse", "D. compare"},
            0, // 答案是A
            "答案：A. recall。让人们回忆他们寻求帮助的时候。"
        ));
        
        // 第17题：willing
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第17题",
            clozePassage,
            "17. how ____ other people will be to help",
            new String[]{"A. brave", "B. disapproving", "C. willing", "D. hesitant"},
            2, // 答案是C
            "答案：C. willing。低估了别人愿意帮助的程度。"
        ));
        
        // 第18题：rewarding
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第18题",
            clozePassage,
            "18. having the chance to help someone is highly ____",
            new String[]{"A. relaxing", "B. surprising", "C. rewarding", "D. demanding"},
            2, // 答案是C
            "答案：C. rewarding。帮助别人是非常有意义的。"
        ));
        
        // 第19题：Thus
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第19题",
            clozePassage,
            "19. So bear all this in mind the next time you need to ask for help. ____",
            new String[]{"A. Thus", "B. Also", "C. Finally", "D. Instead"},
            0, // 答案是A
            "答案：A. Thus。因此，要小心选择求助对象。"
        ));
        
        // 第20题：polite
        questions.add(new ExamQuestion(
            QuestionType.CLOZE_TEST,
            "完形填空 - 第20题",
            clozePassage,
            "20. They might just be too ____",
            new String[]{"A. polite", "B. proud", "C. busy", "D. lazy"},
            2, // 答案是C
            "答案：C. busy。他们可能只是太忙了。"
        ));

        // ========== 阅读理解 Part A - Text 1: 美国小费文化 (21-25题) ==========
        String text1Passage = "U.S. customers historically tipped people they assumed were earning most of their income via tips, such as restaurant servers earning less than the minimum wage. In the early 2010s, a wide range of businesses started processing purchases with iPads and other digital payment systems. These systems often prompted customers to tip for services that were not previously tipped.\n\n" +
            "Today's tip requests are often not connected to the salary and service norms that used to determine when and how people tip. Customers in the past nearly always paid tips after receiving a service, such as at the conclusion of a restaurant meal, after getting a haircut or once a pizza was delivered. That timing could reward high-quality service and give workers an incentive to provide it.\n\n" +
            "It's becoming more common for tips to be requested beforehand. And new tipping technology may even automatically add tips.\n\n" +
            "The prevalence of digital payment devices has made it easier to ask customers for a tip. That helps explain why tip requests are creeping into new kinds of services. Customers now routinely see menus of suggested default options — often well above 20% of what they owe. The amounts have risen from 10% or less in the 1950s to 15% around the year 2000 to 20% or higher today. This increase is sometimes called tipflation — the expectation of ever-higher tip amounts.\n\n" +
            "Tipping has always been a vital source of income for workers in historically tipped services, like restaurants, where the tipped minimum wage can be as low as US $2.13 an hour. Tip creep and tipflation are now further supplementing the income of many low-wage service workers.\n\n" +
            "Notably, tipping primarily benefits some of these workers, such as waiters, but not others, such as cooks and dishwashers. To ensure that all employees were paid fair wages, some restaurants banned tipping and increased prices, but this movement towards no-tipping services has largely fizzled out.\n\n" +
            "So to increase employee wages without raising prices, more employers are succumbing to temptations of tip creep and tipflation. However, many customers are frustrated because they feel they are being asked for too high of a tip too often. And, as our research emphasizes, tipping now seems to be more coercive, less generous, and often completely dissociated from service quality.";
        
        // Text 1 - 第21题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 1 - 第21题",
            text1Passage,
            "21. According to Paragraph 1, the practice of tipping in the U.S. ____",
            new String[]{"was regarded as a sign of generosity", "was considered essential for waiters", "was a way of rewarding diligence", "was optional in most businesses"},
            2, // C
            "答案：C。第一段提到美国顾客历史上给那些主要靠小费收入的人小费，这是一种奖励勤劳的方式。"
        ));
        
        // Text 1 - 第22题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 1 - 第22题",
            text1Passage,
            "22. Compared with tips in the past, today's tips ____",
            new String[]{"are paid much less frequently", "are less often requested in advance", "have less to do with service quality", "contribute less to workers' income"},
            2, // C
            "答案：C。文章最后一段提到，现在的小费与服务质量完全脱节（completely dissociated from service quality）。"
        ));
        
        // Text 1 - 第23题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 1 - 第23题",
            text1Passage,
            "23. Tip requests are creeping into new kinds of services as a result of ____",
            new String[]{"the advancement of technology", "the desire for income increase", "the diversification of business", "the emergence of tipflation"},
            0, // A
            "答案：A。第四段提到数字支付设备的普及（digital payment devices）使得更容易要求顾客给小费。"
        ));
        
        // Text 1 - 第24题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 1 - 第24题",
            text1Passage,
            "24. The movement toward no-tipping services was intended to ____",
            new String[]{"promote consumption", "enrich income sources", "maintain reasonable prices", "guarantee income fairness"},
            3, // D
            "答案：D。第六段提到，为了确保所有员工获得公平工资（paid fair wages），一些餐厅禁止小费。"
        ));
        
        // Text 1 - 第25题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 1 - 第25题",
            text1Passage,
            "25. It can be learned from the last paragraph that tipping ____",
            new String[]{"is becoming a burden for customers", "helps encourage quality service", "is vital to business development", "reflects the need to reduce prices"},
            0, // A
            "答案：A。最后一段提到顾客感到沮丧，因为被要求给太高的小费太频繁（too high of a tip too often）。"
        ));

        // ========== 阅读理解 Part A - Text 2: 英国NHS改革 (26-30题) ==========
        String text2Passage = "When it was established, the National Health Service (NHS) was visionary: offering high-quality, timely care to meet the dominant needs of the population it served. Nearly 75 years on, with the UK facing very different health challenges, it is clear that the model is out of date.\n\n" +
            "From life expectancy to cancer and infant mortality rates, we are lagging behind many of our peers. With more than 6.8 million on waitlists, healthcare is becoming increasingly inaccessible for those who cannot opt to pay for private treatment; and the cost of providing healthcare is increasingly squeezing our investment in other public services. As demand for healthcare continues to grow, pressures on the workforce — which is already near breaking point — will only become more acute.\n\n" +
            "Many of the answers to the crisis in health and care are well rehearsed. We need to be much better at reducing and diverting demand on health services, rather than simply managing it. Much more needs to be invested in communities and primary care to reduce our reliance on hospitals. And capacity in social care needs to be greater, to support the growing number of people living with long-term conditions.\n\n" +
            "Yet despite two decades of strategies and a number of major health reforms, we have failed to make meaningful progress on any of these aims. That is why the Reform think tank is launching a new programme of work entitled \"Reimagining health\", supported by ten former health ministers. Together, we are calling for a much more open and honest conversation about the future of health in the UK, and an \"urgent rethink\" of the hospital-centric model we retain.\n\n" +
            "This must begin with the question of how we maximise the health of the nation, rather than \"fix\" the NHS. It is estimated, for example, that healthcare accounts for only about 20% of health outcomes. Much more important are the places we live, work and socialise — yet there is no clear cross-government strategy for improving these social determinants of health. Worse, when policies like the national obesity strategy are scrapped, taxpayers are left with the hefty price tag of treating the illnesses, like diabetes, that result.\n\n" +
            "Reform wants to ask how power and resources should be distributed in our health system. What health functions should remain at the centre, and what should be given to local leaders, often responsible for services that create health, and with a much better understanding of the needs of their populations?";
        
        // Text 2 - 第26题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 2 - 第26题",
            text2Passage,
            "26. According to the first two paragraphs, the NHS ____",
            new String[]{"is troubled by funding deficiencies", "can hardly satisfy people's needs", "can barely retain its current employees", "is rivalled by private medical services"},
            1, // B
            "答案：B。第二段提到，有680万人在等待名单上，医疗服务越来越难以获得，说明NHS难以满足人们的需求。"
        ));
        
        // Text 2 - 第27题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 2 - 第27题",
            text2Passage,
            "27. One answer to the crisis in health and care is to ____",
            new String[]{"boost the efficiency of hospitals", "lighten the burden on social care", "increase resources for primary care", "reduce the pressure on communities"},
            2, // C
            "答案：C。第三段提到需要在社区和初级医疗（primary care）方面投入更多资金，以减少对医院的依赖。"
        ));
        
        // Text 2 - 第28题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 2 - 第28题",
            text2Passage,
            "28. \"Reimagining health\" is aimed to ____",
            new String[]{"reinforce hospital management", "readjust healthcare regulations", "restructure the health system", "resume suspended health reforms"},
            2, // C
            "答案：C。第四段提到\"Reimagining health\"计划要对以医院为中心的模式进行紧急反思，即重组医疗体系。"
        ));
        
        // Text 2 - 第29题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 2 - 第29题",
            text2Passage,
            "29. To maximise the nation's health, the author suggests ____",
            new String[]{"introducing relevant taxation policies", "paying due attention to social factors", "reevaluating major health outcomes", "enhancing the quality of healthcare"},
            1, // B
            "答案：B。第五段提到，健康结果中只有20%来自医疗，更重要的是人们生活、工作和社交的地方，即社会因素。"
        ));
        
        // Text 2 - 第30题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 2 - 第30题",
            text2Passage,
            "30. It can be inferred that local leaders should ____",
            new String[]{"exercise their power more reasonably", "develop a stronger sense of responsibility", "play a bigger role in the health system", "understand people's health needs better"},
            2, // C
            "答案：C。最后一段提到应该把权力和资源分配给地方领导者，让他们在医疗体系中发挥更大作用。"
        ));

        // ========== 阅读理解 Part A - Text 3: 印度HAPs计划 (31-35题) ==========
        String text3Passage = "Heat action plans, or HAPs, have been proliferating in India in the past few years. In general, an HAP spells out when and how officials should issue heat warnings and alert hospitals and other institutions. Nagpur's plan, for instance, calls for hospitals to set aside \"cold wards\" in the summer for treating heatstroke patients, and advises builders to give construction laborers a break from work on very hot days.\n\n" +
            "But implementation of existing HAPs has been uneven, according to a report from the center for Policy Research. Many lack adequate funding, it found. And their triggering thresholds often are not customized to the local climate. In some areas, high daytime temperatures alone might serve as an adequate trigger for alerts. But in other places, nighttime temperatures or humidity might be as important a gauge of risk as daytime highs.\n\n" +
            "Mumbai's April heat stroke deaths highlighted the need for more nuanced and localized warnings, researchers say. That day's high temperature of roughly 36°C was 1°C shy of the heat wave alert threshold for coastal cities set by national meteorological authorities. But the effects of the heat were amplified by humidity — an often neglected factor in heat alert systems — and the lack of shade at the late-morning outdoor ceremony.\n\n" +
            "To help improve HAPs, urban planner Kotharkar's team is working on a model plan that outlines best practices and could be adapted to local conditions. Among other things, she says, all cities should create a vulnerability map to help focus responses on the populations most at risk.\n\n" +
            "Such mapping doesn't need to be complex, Kotharkar says. \"A useful map can be created by looking at even a few key parameters.\" For example, neighborhoods with a large elderly population or informal dwellings that cope poorly with heat could get special warnings or be bolstered with cooling centers. The Nagpur project has already created a risk and vulnerability map, which enabled Kotharkar to tell officials which neighborhoods to focus on in the event of a heat wave this summer.\n\n" +
            "HAPs shouldn't just include short-term emergency responses, researchers say, but also recommend medium- to long-term measures that could make communities cooler. In Nagpur, for example, Kotharkar's team has been able to advise city officials about where to plant trees to provide shade. HAPs could also guide efforts to retrofit homes or modify building regulations. \"Reducing deaths in an emergency is a good target to have, but it's the lowest target,\" says Climate researcher Chandni Singh.";
        
        // Text 3 - 第31题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 3 - 第31题",
            text3Passage,
            "31. According to Paragraph 1, Nagpur's plan proposes measures to ____",
            new String[]{"tackle extreme weather", "ensure construction quality", "monitor emergency warnings", "address excessive workloads"},
            0, // A
            "答案：A。第一段提到Nagpur计划要求医院设立\"冷病房\"治疗中暑病人，建议建筑工人在高温天气休息，都是应对极端天气的措施。"
        ));
        
        // Text 3 - 第32题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 3 - 第32题",
            text3Passage,
            "32. One problem with existing HAPs is that they ____",
            new String[]{"prove too costly to be implemented", "lack localized alert-issuing criteria", "give delayed responses to heat waves", "keep hospitals under great pressure"},
            1, // B
            "答案：B。第二段提到现有HAPs的触发阈值往往没有根据当地气候定制（not customized to the local climate），缺乏本地化的预警标准。"
        ));
        
        // Text 3 - 第33题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 3 - 第33题",
            text3Passage,
            "33. Mumbai's case shows that India's heat alert systems need to ____",
            new String[]{"include other factors besides temperature", "take subtle weather changes into account", "prioritize potentially disastrous heat waves", "draw further support from local authorities"},
            0, // A
            "答案：A。第三段提到孟买案例显示，热浪预警系统中湿度是一个经常被忽视的因素，需要包含温度以外的其他因素。"
        ));
        
        // Text 3 - 第34题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 3 - 第34题",
            text3Passage,
            "34. Kotharkar holds that a vulnerability map can help ____",
            new String[]{"prevent the harm of high humidity", "target areas needing special attention", "expand the Nagpur project's coverage", "make relief plans for heat-stricken people"},
            1, // B
            "答案：B。第四、五段提到脆弱性地图可以帮助关注最危险的人群（populations most at risk），即针对需要特别关注的区域。"
        ));
        
        // Text 3 - 第35题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 3 - 第35题",
            text3Passage,
            "35. According to the last paragraph, researchers believe that HAPs should ____",
            new String[]{"focus more on heatstroke treatment", "invite wider public participation", "apply for more government grants", "serve a broader range of purposes"},
            3, // D
            "答案：D。最后一段提到HAPs不应只包括短期应急响应，还应包括中长期措施（medium- to long-term measures），服务更广泛的目的。"
        ));

        // ========== 阅读理解 Part A - Text 4: 城市欲望小径 (36-40题) ==========
        String text4Passage = "Navigating beyond the organised pavements and parks of our urban spaces, desire paths are the unofficial footprints of a community, revealing the unspoken preferences, shared shortcuts and collective choices of humans. Often appearing as trodden dirt tracks through otherwise neat green spaces, these routes of collective disobedience cut corners, bisect lawns and cross hills, representing the natural capability of people (and animals) to go from point A to point B most effectively.\n\n" +
            "Urban planners interpret desire paths as more than just convenient shortcuts; they offer valuable insights into the dynamics between planning and behaviour. Ohio State University allowed its students to navigate the Oval, a lawn in the centre of campus, freely, then proceeded to pave the desire paths, creating a web of effective routes students had established.\n\n" +
            "Yet, reluctance persists among other planners to integrate desire paths into formal plans, citing concerns about safety, environmental impact, or primarily, aesthetics. A Reddit webpage devoted to the phenomenon, boasting nearly 50,000 members, showcases images of local desire paths adorned with signs instructing pedestrians to adhere to designated walkways, underscoring the rebellious nature inherent in these human-made tracks. This clash highlights an ongoing struggle between the organic, user-driven evolution of public spaces and the desire for a visually curated and controlled urban environment.\n\n" +
            "The Wickquasgeek Trail is an example of a historical desire path, created by Native Americans to cross the forests of Manhattan and move between settlements quickly. This trail, when Dutch colonists arrived, was widened and made into one of the main trade roads across the island, known at the time as de Heere Straat, or Gentlemen's Street. Following the British assumption of control in New York, the street was renamed Broadway. Notably, Broadway stands out as one of the few areas in NYC that defies the grid-based system applied to the rest of the city, cutting a diagonal across parts of the city.\n\n" +
            "In online spaces, desire paths have sparked a fascination that can approach obsession, with the Reddit page serving as a hub. Contributors offer a wide array of stories, from little-known new shortcuts to long-established alternate routes.\n\n" +
            "Animal desire paths, such as ducks forging trails through frozen ponds or dogs carving direct routes in gardens, highlight the adaptability of these trails in both human and animal experiences. As desire paths criss-cross through both physical and virtual landscapes, they stand as a proof of the collective insistence on forging unconventional routes and embracing the spirit of communal choice.";
        
        // Text 4 - 第36题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 4 - 第36题",
            text4Passage,
            "36. According to Paragraph 1, desire paths are a result of ____",
            new String[]{"the curiosity to explore surrounding hills", "the necessity to preserve green spaces", "the tendency to pursue convenience", "the wish to find comfort in solitude"},
            2, // C
            "答案：C。第一段提到欲望小径代表人们从A点到B点最有效地移动的自然能力（go from point A to point B most effectively），体现了追求便利的倾向。"
        ));
        
        // Text 4 - 第37题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 4 - 第37题",
            text4Passage,
            "37. It can be inferred that Ohio State University ____",
            new String[]{"intends to improve its desire paths", "leads in the research on desire paths", "guides the creation of its desire paths", "takes a positive view of desire paths"},
            3, // D
            "答案：D。第二段提到俄亥俄州立大学允许学生自由穿越草坪，然后将欲望小径铺设成正式道路，说明持积极态度。"
        ));
        
        // Text 4 - 第38题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 4 - 第38题",
            text4Passage,
            "38. The images on the Reddit webpage reflect ____",
            new String[]{"conflicting opinions on the use of desire paths", "the call to upgrade the designing of public spaces", "the demand for proper planning of desire paths", "growing concerns over the loss of public spaces"},
            0, // A
            "答案：A。第三段提到Reddit网页展示的图片显示，有标志要求行人遵守指定人行道，这凸显了对欲望小径使用的矛盾意见。"
        ));
        
        // Text 4 - 第39题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 4 - 第39题",
            text4Passage,
            "39. The example of the Wickquasgeek Trail illustrates ____",
            new String[]{"the growth of New York City", "the Dutch origin of desire paths", "the importance of urban planning", "the recognition of desire paths"},
            3, // D
            "答案：D。第四段提到Wickquasgeek Trail这条欲望小径后来被拓宽并成为主要贸易道路（百老汇），体现了对欲望小径的认可。"
        ));
        
        // Text 4 - 第40题
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "阅读理解 Text 4 - 第40题",
            text4Passage,
            "40. It can be learned from the last paragraph that desire paths ____",
            new String[]{"reveal humans' deep respect for nature", "are crucial to humans' mental wellbeing", "are a human imitation of animal behavior", "show a shared trait in humans and animals"},
            3, // D
            "答案：D。最后一段提到动物欲望小径（如鸭子、狗）的例子，强调这些小径在人类和动物经历中的适应性，体现了人类和动物的共同特征。"
        ));

        // 新题型题目 (5题，每题2分，共10分) - 标题匹配
        String newTypePassage = "Five Steps to Suggesting a Change at Work That'll Actually Get Taken Seriously\n\nEveryone wants to be that person — the one who looks at the same information as everyone else, but who sees a fresh, innovative solution. However, it takes more than simply having a good idea. How you share it is as important as the suggestion itself.\n\nWhy? Because writing a new script — literally or figuratively — means that other team members will have to adapt to something new. So whether you're suggesting a (seemingly) benign change like streamlining outdated protocol, or a bigger change like adding an hour to each workday so people can leave early on Fridays, you're asking others to reimagine their workflow or schedule. Not to mention, if the process you're scrapping is one someone else suggested, there's the possibility of hurt feelings.\n\nTo gain buy-in on an innovative, new idea, follow these steps:\n\n41. ____\nGreat ideas don't stand alone. In other words, you can't mention your suggestion once and expect it to be adopted. To see a change, you'll need to champion your plan and sell its merits. In addition, you need to be willing to stand up to scrutiny and criticism and be prepared to explain your innovation in different ways for various audiences.\n\n42. ____\nSometimes it makes sense to go to your boss first. But other times, it's useful to build a coalition among your co-workers or other stakeholders. When it works, it works great — because you're ready for your stubborn supervisor's pushback with answers like, \"Actually, I connected with a few people in our tech department to discuss how much time these kinds of website updates would take, and they suggested they have the bandwidth.\"\n\n43. ____\nOne of the biggest barriers to gaining buy-in occurs when the owner of an idea is viewed as argumentative, defensive, or close-minded. Because, let's be honest: No one likes a know-it-all. So, if people disagree with you, don't be indignant. Instead, listen to their concerns fully, try to understand their perspective, and include their concerns (and possible remedies) in future discussions.\n\nSo, instead of saying, \"Martha, our current slogan is confusing and should be updated,\" you could try, \"Martha raises a great point that our current slogan has a long history for our stakeholders, but I wonder if we might be able to brainstorm a tagline that could build on that — and be clearer for new customers.\"\n\n44. ____\nNew ideas are the grandchildren of old ones. In other words, don't throw old solutions under the bus to make your improvement stand out. Remember that in light of whatever the problem the old system solved — or, maybe, has failed to solve in recent memory — it was a great idea at the time. Appreciating the older contributions as you suggest future innovations helps bolster the credibility of your idea.\n\n45. ____\nWhen pitching a new idea, it's important to use the language of abundance instead of the language of deficit. Instead of saying what is wrong, broken, or suboptimal, talk about what is right, fixable, or ideal. For example, try, \"I can see lots of applications for this new approach\" rather than, \"This innovation is the only way.\" Be optimistic but realistic, and you will stand out.\n\nA. Stay positive\nB. Respect the past\nC. Use channels\nD. Give it time\nE. Invite resistance\nF. Be a salesman\nG. Be humble";
        
        // 第41题：Be a salesman
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "新题型（标题匹配）- 第41题",
            newTypePassage,
            "41. Great ideas don't stand alone. In other words, you can't mention your suggestion once and expect it to be adopted. To see a change, you'll need to champion your plan and sell its merits...",
            new String[]{"A. Stay positive", "B. Respect the past", "C. Use channels", "D. Give it time", "E. Invite resistance", "F. Be a salesman", "G. Be humble"},
            5, // F. Be a salesman
            "答案：F. Be a salesman（做推销员）。本段强调需要不断推广你的想法，准备好应对批评，向不同受众解释创新。"
        ));
        
        // 第42题：Use channels
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "新题型（标题匹配）- 第42题",
            newTypePassage,
            "42. Sometimes it makes sense to go to your boss first. But other times, it's useful to build a coalition among your co-workers or other stakeholders...",
            new String[]{"A. Stay positive", "B. Respect the past", "C. Use channels", "D. Give it time", "E. Invite resistance", "F. Be a salesman", "G. Be humble"},
            2, // C. Use channels
            "答案：C. Use channels（利用渠道）。本段讲述如何通过建立同事联盟来获得支持，这是一种利用不同渠道的策略。"
        ));
        
        // 第43题：Be humble
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "新题型（标题匹配）- 第43题",
            newTypePassage,
            "43. One of the biggest barriers to gaining buy-in occurs when the owner of an idea is viewed as argumentative, defensive, or close-minded. Because, let's be honest: No one likes a know-it-all...",
            new String[]{"A. Stay positive", "B. Respect the past", "C. Use channels", "D. Give it time", "E. Invite resistance", "F. Be a salesman", "G. Be humble"},
            6, // G. Be humble
            "答案：G. Be humble（保持谦虚）。本段强调不要表现得像万事通，要倾听他人意见，理解他人观点。"
        ));
        
        // 第44题：Respect the past
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "新题型（标题匹配）- 第44题",
            newTypePassage,
            "44. New ideas are the grandchildren of old ones. In other words, don't throw old solutions under the bus to make your improvement stand out...",
            new String[]{"A. Stay positive", "B. Respect the past", "C. Use channels", "D. Give it time", "E. Invite resistance", "F. Be a salesman", "G. Be humble"},
            1, // B. Respect the past
            "答案：B. Respect the past（尊重过去）。本段强调新想法源于旧想法，要认可过去的贡献，不要贬低旧方案。"
        ));
        
        // 第45题：Stay positive
        questions.add(new ExamQuestion(
            QuestionType.READING_COMPREHENSION,
            "新题型（标题匹配）- 第45题",
            newTypePassage,
            "45. When pitching a new idea, it's important to use the language of abundance instead of the language of deficit. Instead of saying what is wrong, broken, or suboptimal, talk about what is right, fixable, or ideal...",
            new String[]{"A. Stay positive", "B. Respect the past", "C. Use channels", "D. Give it time", "E. Invite resistance", "F. Be a salesman", "G. Be humble"},
            0, // A. Stay positive
            "答案：A. Stay positive（保持积极）。本段强调使用积极正面的语言，谈论什么是对的而不是错的。"
        ));

        // ========== 翻译题目 (5道小题，每题3分，共15分) - 2025年考研英语二真题 ==========
        
        // 第46题：翻译第一段
        questions.add(new ExamQuestion(
            QuestionType.TRANSLATION,
            "翻译 - 第46题",
            "Recent decades have seen science move into a convention where engagement in the subject can only be done through institutions, such as a university.",
            "46. 请将以下英文翻译成中文：\n\nRecent decades have seen science move into a convention where engagement in the subject can only be done through institutions, such as a university.",
            null,
            -1,
            "翻译第一段：关于科学参与的惯例。",
            "近几十年来，科学已经形成了一种惯例，即只有通过大学等机构才能参与科学研究。",
            null
        ));
        
        // 第47题：翻译第二段
        questions.add(new ExamQuestion(
            QuestionType.TRANSLATION,
            "翻译 - 第47题",
            "But by utilising the natural curiosity of the general public it is possible to overcome many of these challenges by engaging non-scientists directly in the research process.",
            "47. 请将以下英文翻译成中文：\n\nBut by utilising the natural curiosity of the general public it is possible to overcome many of these challenges by engaging non-scientists directly in the research process.",
            null,
            -1,
            "翻译第二段：关于利用公众好奇心。",
            "但是，通过利用公众的天然好奇心，让非科学家直接参与研究过程，就有可能克服许多这样的挑战。",
            null
        ));
        
        // 第48题：翻译第三段
        questions.add(new ExamQuestion(
            QuestionType.TRANSLATION,
            "翻译 - 第48题",
            "Scientists have employed a variety of ways to engage the general public in their research, such as making data analysis into an online game or sample collection into a smartphone application.",
            "48. 请将以下英文翻译成中文：\n\nScientists have employed a variety of ways to engage the general public in their research, such as making data analysis into an online game or sample collection into a smartphone application.",
            null,
            -1,
            "翻译第三段：关于科学家吸引公众参与的方式。",
            "科学家们采用了多种方式让公众参与他们的研究，例如将数据分析变成在线游戏，或将样本收集变成智能手机应用程序。",
            null
        ));
        
        // 第49题：翻译第四段
        questions.add(new ExamQuestion(
            QuestionType.TRANSLATION,
            "翻译 - 第49题",
            "These groups of people are part of a rapidly expanding biotechnological social movement of citizen scientists and professional scientists seeking to take discovery out of institutions and put it into the hands of anyone with the enthusiasm.",
            "49. 请将以下英文翻译成中文：\n\nThese groups of people are part of a rapidly expanding biotechnological social movement of citizen scientists and professional scientists seeking to take discovery out of institutions and put it into the hands of anyone with the enthusiasm.",
            null,
            -1,
            "翻译第四段：关于公民科学家运动。",
            "这些人群是快速扩张的生物技术社会运动的一部分，由公民科学家和专业科学家组成，他们致力于将科学发现从机构中解放出来，交到任何有热情的人手中。",
            null
        ));
        
        // 第50题：翻译第五段
        questions.add(new ExamQuestion(
            QuestionType.TRANSLATION,
            "翻译 - 第50题",
            "They pool resources, collaborate, think outside the box, and find solutions and ways around obstacles to explore science for the sake of science without the traditional boundaries of working inside a formal setting.",
            "50. 请将以下英文翻译成中文：\n\nThey pool resources, collaborate, think outside the box, and find solutions and ways around obstacles to explore science for the sake of science without the traditional boundaries of working inside a formal setting.",
            null,
            -1,
            "翻译第五段：关于他们的合作方式。",
            "他们汇集资源、相互合作、跳出思维定式，寻找解决方案和绕过障碍的方法，为了科学而探索科学，不受在正式环境中工作的传统界限的限制。",
            null
        ));

        // ========== 写作题目 (Part A 10分，Part B 15分，共25分) ==========
        
        // 第51题：Part A 应用文写作
        questions.add(new ExamQuestion(
            QuestionType.WRITING,
            "写作 Part A - 第51题",
            "",
            "51. Directions:\nRead the following email from your classmate Paul and write him a reply.\n\nDear Li Ming,\n\nI was really excited to hear that you'd invite some young craftsmen to demonstrate their innovative craft-making on campus. May I know more about what they'll show? Also, I'd like to help with your preparation work. Please let me know what I can do.\n\nYours,\nPaul\n\nWrite your answer in about 100 words on the ANSWER SHEET.\nDo not use your own name in your email; use \"Li Ming\" instead. (10 points)",
            null,
            -1,
            "Part A：邮件回复，约100词，满分10分。",
            null,
            "PART_A"
        ));
        
        // 第52题：Part B 图表作文
        questions.add(new ExamQuestion(
            QuestionType.WRITING,
            "写作 Part B - 第52题",
            "",
            "52. Directions: Write an essay of 160-200 words based on the following table. In your essay, you should:\n1) describe the table briefly,\n2) explain its intended meaning, and\n3) give your comments.\n\n【表格：近年来全国居民平均每百户年末主要耐用消费品拥有量】\n年份\t空调(台)\t洗衣机(台)\t电冰箱(台)\n2014\t75.2\t83.7\t85.5\n2017\t96.1\t91.7\t95.3\n2020\t117.7\t96.7\t101.8\n2023\t145.9\t98.2\t103.4\n\n(15 points)",
            null,
            -1,
            "Part B：图表作文，160-200词，满分15分。",
            null,
            "PART_B"
        ));
        
            android.util.Log.d("ExamAnswerActivity", "考试数据初始化完成，总题数：" + questions.size());
        } catch (Exception e) {
            android.util.Log.e("ExamAnswerActivity", "考试数据初始化失败", e);
            Toast.makeText(this, "考试数据加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 选项按钮
        btnOptionA.setOnClickListener(v -> selectOption(0));
        btnOptionB.setOnClickListener(v -> selectOption(1));
        btnOptionC.setOnClickListener(v -> selectOption(2));
        btnOptionD.setOnClickListener(v -> selectOption(3));
        btnOptionE.setOnClickListener(v -> selectOption(4));
        btnOptionF.setOnClickListener(v -> selectOption(5));
        btnOptionG.setOnClickListener(v -> selectOption(6));
        btnOptionH.setOnClickListener(v -> selectOption(7));

        // 上一题下一题按钮
        btnPrevious.setOnClickListener(v -> previousQuestion());
        btnNext.setOnClickListener(v -> nextQuestion());
        
        // 选择题型按钮
        btnSelectQuestionType.setOnClickListener(v -> showQuestionTypeMenu());
        
        // 笔记按钮
        btnNote.setOnClickListener(v -> showNoteDialog());
        
        // 答题卡按钮
        btnAnswerSheet.setOnClickListener(v -> showAnswerSheet());
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
                Toast.makeText(ExamAnswerActivity.this, "考试时间到！自动提交", Toast.LENGTH_LONG).show();
                submitExam();
            }
        }.start();
    }

    private void updateTimerDisplay() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        tvTimer.setText(timeFormatted);

        // 时间不足时改变颜色
        if (timeLeftInMillis < 45 * 60 * 1000) { // 少于45分钟（考研英语考试时间提醒）
            tvTimer.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    private void showCurrentQuestion() {
        if (questions == null || questions.isEmpty() || currentQuestionIndex >= questions.size()) {
            showFinalResult();
            return;
        }

        ExamQuestion currentQuestion = questions.get(currentQuestionIndex);

        // 更新进度
        tvProgress.setText((currentQuestionIndex + 1) + "/" + questions.size());
        progressBar.setProgress((currentQuestionIndex + 1) * 100 / questions.size());

        // 更新题目编号显示
        if (currentQuestionIndex < 20) {
            tvSection.setText("完形填空");
        } else if (currentQuestionIndex < 25) {
            tvSection.setText("阅读理解A");
        } else if (currentQuestionIndex < 30) {
            tvSection.setText("阅读理解B");
        } else if (currentQuestionIndex < 35) {
            tvSection.setText("阅读理解C");
        } else if (currentQuestionIndex < 40) {
            tvSection.setText("阅读理解D");
        } else if (currentQuestionIndex < 45) {
            tvSection.setText("新题型");
        } else if (currentQuestionIndex < 50) {
            tvSection.setText("翻译");
        } else {
            tvSection.setText("写作");
        }

        // 更新题目类型
        tvQuestionType.setText(currentQuestion.title);

        // 显示题目内容
        tvQuestion.setText(currentQuestion.question);

        // 根据题目类型显示不同内容
        switch (currentQuestion.type) {
            case CLOZE_TEST:
                showClozeTestQuestion(currentQuestion);
                break;
            case READING_COMPREHENSION:
                showReadingComprehensionQuestion(currentQuestion);
                break;
            case TRANSLATION:
                showTranslationQuestion(currentQuestion);
                break;
            case WRITING:
                showWritingQuestion(currentQuestion);
                break;
        }

        // 隐藏结果区域
        if (layoutResult != null) {
        layoutResult.setVisibility(View.GONE);
        }

        // 更新按钮状态
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        
        // 重新初始化题号切换器（切换题目范围时需要重建）
        initQuestionNumbers();
    }

    private void showClozeTestQuestion(ExamQuestion question) {
        layoutOptions.setVisibility(View.VISIBLE);
        layoutTranslation.setVisibility(View.GONE);
        layoutWriting.setVisibility(View.GONE);

        // 隐藏底部题目问题（完形填空不需要）
        if (tvBottomQuestion != null) {
            tvBottomQuestion.setVisibility(View.GONE);
        }

        // 完形填空只显示4个选项
        btnOptionA.setVisibility(View.VISIBLE);
        btnOptionB.setVisibility(View.VISIBLE);
        btnOptionC.setVisibility(View.VISIBLE);
        btnOptionD.setVisibility(View.VISIBLE);
        btnOptionE.setVisibility(View.GONE);
        btnOptionF.setVisibility(View.GONE);
        btnOptionG.setVisibility(View.GONE);
        btnOptionH.setVisibility(View.GONE);

        // 显示文章内容（包含题目编号）
        if (question.passage != null && !question.passage.isEmpty()) {
            tvPassage.setVisibility(View.VISIBLE);
            tvPassage.setText(question.passage);
        } else {
            tvPassage.setVisibility(View.GONE);
        }

        // 显示选项供用户选择答案
        if (question.options != null && question.options.length >= 4) {
            btnOptionA.setText(question.options[0]);
            btnOptionB.setText(question.options[1]);
            btnOptionC.setText(question.options[2]);
            btnOptionD.setText(question.options[3]);
        }
        
        // 恢复之前选择的答案状态
        restoreSelectedOption();
    }

    private void showReadingComprehensionQuestion(ExamQuestion question) {
        layoutOptions.setVisibility(View.GONE);
        layoutTranslation.setVisibility(View.GONE);
        layoutWriting.setVisibility(View.GONE);

        // 显示文章内容
        if (question.passage != null && !question.passage.isEmpty()) {
            tvPassage.setVisibility(View.VISIBLE);
            tvPassage.setText(question.passage);
        } else {
            tvPassage.setVisibility(View.GONE);
        }

        // 如果有选项，显示选项
        if (question.options != null && question.options.length > 0) {
            layoutOptions.setVisibility(View.VISIBLE);
            
            // 显示底部题目问题
            if (tvBottomQuestion != null && question.question != null && !question.question.isEmpty()) {
                tvBottomQuestion.setVisibility(View.VISIBLE);
                tvBottomQuestion.setText(question.question);
            }
            
            // 新题型题目有7-8个选项（标题匹配/段落选项）
            if (question.options.length >= 7) {
                // 显示对应数量的选项按钮
                btnOptionA.setVisibility(View.VISIBLE);
                btnOptionB.setVisibility(View.VISIBLE);
                btnOptionC.setVisibility(View.VISIBLE);
                btnOptionD.setVisibility(View.VISIBLE);
                btnOptionE.setVisibility(View.VISIBLE);
                btnOptionF.setVisibility(View.VISIBLE);
                btnOptionG.setVisibility(View.VISIBLE);
                btnOptionH.setVisibility(question.options.length >= 8 ? View.VISIBLE : View.GONE);
                
                btnOptionA.setText(question.options[0]);
                btnOptionB.setText(question.options[1]);
                btnOptionC.setText(question.options[2]);
                btnOptionD.setText(question.options[3]);
                btnOptionE.setText(question.options[4]);
                btnOptionF.setText(question.options[5]);
                btnOptionG.setText(question.options[6]);
                if (question.options.length >= 8) {
                    btnOptionH.setText(question.options[7]);
                }
            } else if (question.options.length >= 4) {
                // 阅读理解选项：显示4个选项，添加字母前缀 "A 选项文本"
                btnOptionA.setVisibility(View.VISIBLE);
                btnOptionB.setVisibility(View.VISIBLE);
                btnOptionC.setVisibility(View.VISIBLE);
                btnOptionD.setVisibility(View.VISIBLE);
                btnOptionE.setVisibility(View.GONE);
                btnOptionF.setVisibility(View.GONE);
                btnOptionG.setVisibility(View.GONE);
                btnOptionH.setVisibility(View.GONE);
                
                btnOptionA.setText("A " + question.options[0]);
                btnOptionB.setText("B " + question.options[1]);
                btnOptionC.setText("C " + question.options[2]);
                btnOptionD.setText("D " + question.options[3]);
            }
            
            // 恢复之前选择的答案状态
            restoreSelectedOption();
        } else {
            layoutOptions.setVisibility(View.GONE);
            if (tvBottomQuestion != null) {
                tvBottomQuestion.setVisibility(View.GONE);
            }
        }
    }

    private void showTranslationQuestion(ExamQuestion question) {
        layoutOptions.setVisibility(View.GONE);
        layoutTranslation.setVisibility(View.VISIBLE);
        layoutWriting.setVisibility(View.GONE);

        // 显示题号和英文原文
        if (question.passage != null && !question.passage.isEmpty()) {
            tvPassage.setVisibility(View.VISIBLE);
            // 显示题目标题和英文原文
            String displayText = question.title + "\n\n" +
                    "【英文原文】\n" + question.passage + "\n\n" +
                    "请将上述英文翻译成中文：";
            tvPassage.setText(displayText);
        } else if (question.question != null && !question.question.isEmpty()) {
            tvPassage.setVisibility(View.VISIBLE);
            tvPassage.setText(question.question);
        } else {
            tvPassage.setVisibility(View.GONE);
        }
        
        // 隐藏底部题目问题
        if (tvBottomQuestion != null) {
            tvBottomQuestion.setVisibility(View.GONE);
        }

        // 设置简洁的输入提示
        etTranslation.setHint("请在此输入中文翻译...");
        
        // 恢复之前输入的翻译内容
        String savedAnswer = userAnswers.get(currentQuestionIndex);
        if (savedAnswer != null && savedAnswer.startsWith("翻译：")) {
            etTranslation.setText(savedAnswer.substring(3));
        } else if (savedAnswer != null && !savedAnswer.isEmpty()) {
            etTranslation.setText(savedAnswer);
        } else {
            etTranslation.setText("");
        }
    }

    private void showWritingQuestion(ExamQuestion question) {
        layoutOptions.setVisibility(View.GONE);
        layoutTranslation.setVisibility(View.GONE);
        layoutWriting.setVisibility(View.VISIBLE);

        // 显示写作题目要求
        if (question.question != null && !question.question.isEmpty()) {
            tvPassage.setVisibility(View.VISIBLE);
            // 根据写作类型显示不同的标题
            String typeLabel = "";
            if ("PART_A".equals(question.writingType)) {
                typeLabel = "【应用文写作 - 满分10分】\n\n";
            } else if ("PART_B".equals(question.writingType)) {
                typeLabel = "【大作文 - 满分15分】\n\n";
            }
            tvPassage.setText(typeLabel + question.question);
        } else {
            tvPassage.setVisibility(View.GONE);
        }
        
        // 隐藏底部题目问题
        if (tvBottomQuestion != null) {
            tvBottomQuestion.setVisibility(View.GONE);
        }

        // 设置写作提示
        if ("PART_A".equals(question.writingType)) {
            etWriting.setHint("请在此输入应用文（约100词）...");
        } else {
            etWriting.setHint("请在此输入作文（160-200词）...");
        }
        
        // 恢复之前输入的写作内容
        String savedAnswer = userAnswers.get(currentQuestionIndex);
        if (savedAnswer != null && savedAnswer.startsWith("作文：")) {
            etWriting.setText(savedAnswer.substring(3));
        } else if (savedAnswer != null && !savedAnswer.isEmpty()) {
            etWriting.setText(savedAnswer);
        } else {
            etWriting.setText("");
        }
    }

    private void selectOption(int optionIndex) {
        android.util.Log.d("ExamAnswerActivity", "选择选项: 题号=" + (currentQuestionIndex + 1) + ", 选项索引=" + optionIndex + ", 选项=" + (char)('A' + optionIndex));
        
        ExamQuestion currentQuestion = questions.get(currentQuestionIndex);

        if (currentQuestion.type == QuestionType.TRANSLATION || currentQuestion.type == QuestionType.WRITING) {
            return; // 非选择题不处理
        }

        // 对于新题型题目，答案格式特殊
        if (currentQuestion.type == QuestionType.READING_COMPREHENSION && currentQuestion.title.contains("新题型")) {
            highlightSelectedOption(optionIndex);  // 高亮选中选项
            String answer = String.valueOf((char)('A' + optionIndex));  // 统一格式：只保存字母
            saveUserAnswer(currentQuestionIndex, answer);
            return;
        }

        // 高亮选中选项
        highlightSelectedOption(optionIndex);

        // 直接保存答案，不显示结果、不自动跳题
        String answer = String.valueOf((char)('A' + optionIndex));
        saveUserAnswer(currentQuestionIndex, answer);
    }

    private void highlightSelectedOption(int selectedOption) {
        Button[] buttons = {btnOptionA, btnOptionB, btnOptionC, btnOptionD, 
                          btnOptionE, btnOptionF, btnOptionG, btnOptionH};

        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getVisibility() == View.VISIBLE) {
            if (i == selectedOption) {
                buttons[i].setBackground(ContextCompat.getDrawable(this, R.drawable.btn_option_selected));
            } else {
                buttons[i].setBackground(ContextCompat.getDrawable(this, R.drawable.btn_option_default));
                }
            }
        }
    }

    private void resetOptionButtons() {
        Button[] buttons = {btnOptionA, btnOptionB, btnOptionC, btnOptionD,
                          btnOptionE, btnOptionF, btnOptionG, btnOptionH};

        for (Button button : buttons) {
            if (button.getVisibility() == View.VISIBLE) {
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_option_default));
            button.setEnabled(true);
            }
        }
    }

    private void showAnswerResult(ExamQuestion question, int selectedOption) {
        // 对于完形填空题目，不显示结果，直接保存答案（只保存选项字母）
        if (question.type == QuestionType.CLOZE_TEST) {
            String answer = String.valueOf((char)('A' + selectedOption));  // 只保存"A"/"B"/"C"/"D"
            android.util.Log.d("ExamAnswerActivity", "完形填空保存答案: 题号=" + (currentQuestionIndex + 1) + ", 选项=" + answer);
            saveUserAnswer(currentQuestionIndex, answer);
            return;
        }

        boolean isCorrect = selectedOption == question.correctAnswer;

        if (isCorrect) {
            ivResult.setImageResource(R.drawable.ic_check);
            tvResult.setText("正确！" + (question.explanation != null ? "\n" + question.explanation : ""));
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            ivResult.setImageResource(R.drawable.ic_close);
            String correctAnswerText = question.options != null && question.correctAnswer < question.options.length ?
                question.options[question.correctAnswer] : "正确答案";
            tvResult.setText("错误！正确答案是: " + correctAnswerText +
                           (question.explanation != null ? "\n" + question.explanation : ""));
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        layoutResult.setVisibility(View.VISIBLE);

        // 保存用户答案
        saveUserAnswer(currentQuestionIndex, String.valueOf((char)('A' + selectedOption)));
    }

    private void saveUserAnswer(int questionIndex, String answer) {
        android.util.Log.d("ExamAnswerActivity", "保存答案: 题号=" + (questionIndex + 1) + ", questionIndex=" + questionIndex + ", answer=" + answer);
        
        userAnswers.put(questionIndex, answer);

        // 保存到数据库
        ExamAnswerEntity answerEntity = new ExamAnswerEntity();
        answerEntity.setQuestionIndex(questionIndex);
        answerEntity.setAnswer(answer);
        answerEntity.setExamTitle(examTitle != null ? examTitle : "未知考试");
        answerEntity.setAnswerTime(new Date());

        examAnswerRepository.addAnswer(answerEntity);
        
        android.util.Log.d("ExamAnswerActivity", "答案已保存到数据库和内存");
        
        // 更新题号显示（显示勾选标记）
        updateQuestionNumberHighlight();
    }

    private void nextQuestion() {
        // 在切换题目前保存当前题目的文本答案
        saveCurrentTextAnswer();
        
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            showCurrentQuestion();
        } else {
            submitExam();
        }
    }

    private void previousQuestion() {
        // 在切换题目前保存当前题目的文本答案
        saveCurrentTextAnswer();
        
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            showCurrentQuestion();
        }
    }

    private String getSectionName(QuestionType type) {
        switch (type) {
            case CLOZE_TEST: return "完形";
            case READING_COMPREHENSION: return "阅读";
            case TRANSLATION: return "翻译";
            case WRITING: return "写作";
            default: return "未知";
        }
    }

    private void submitExam() {
        if (examTimer != null) {
            examTimer.cancel();
        }

        // 保存写作和翻译答案
        saveTextAnswers();

        // 显示批改进度对话框
        showGradingDialog();

        // 后台线程执行批改
        new Thread(() -> {
            try {
                // 1. 批改选择题
                android.util.Log.d("ExamAnswerActivity", "开始批改选择题");
                ExamResultEntity result = gradeExam();
                
                // 2. AI批改翻译和写作（异步执行）
                android.util.Log.d("ExamAnswerActivity", "开始AI批改翻译和写作");
                gradeTranslationAndWriting(result);
                
                // 3. 【统一时间记录】记录真题练习时长到用户设置（同时更新学习连续天数）
                long duration = System.currentTimeMillis() - examStartTime;
                userSettingsRepository.recordStudyTime(duration, "real_exam");
                
                // 【图表数据】同时创建学习记录用于图表显示
                StudyRecordEntity studyRecord = new StudyRecordEntity();
                studyRecord.setStudyType("real_exam");
                studyRecord.setQuestionId(null);
                studyRecord.setVocabularyId(null);
                studyRecord.setCorrect(true); // 真题练习默认为完成
                studyRecord.setResponseTime(duration);
                studyRecord.setScore((int) result.getTotalScore()); // 真题练习现在有分数了
                studyRecord.setStudyDate(new java.util.Date()); // 显式设置学习日期
                studyRecord.setNotes("真题练习 - " + examTitle + " " + examYear + " - 得分:" + result.getTotalScore());
                studyRecordRepository.addStudyRecord(studyRecord);
                
                // 【智能任务完成跟踪】提交真题后记录完成一套真题
                TaskProgressTracker.getInstance(ExamAnswerActivity.this).recordProgress("real_exam", 1);
                
                // 注意：gradeTranslationAndWriting是异步的，它会在完成后调用finishGrading
                // finishGrading会保存成绩并跳转到成绩详情页
                
            } catch (Exception e) {
                android.util.Log.e("ExamAnswerActivity", "批改过程出错", e);
                dismissGradingDialog();
                mainHandler.post(() -> {
                    Toast.makeText(ExamAnswerActivity.this, "批改失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    /**
     * 保存当前题目的文本答案（翻译或写作）
     */
    private void saveCurrentTextAnswer() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        ExamQuestion currentQuestion = questions.get(currentQuestionIndex);
        
        // 保存翻译答案
        if (currentQuestion.type == QuestionType.TRANSLATION && 
            layoutTranslation.getVisibility() == View.VISIBLE && 
            etTranslation != null) {
            String translation = etTranslation.getText().toString().trim();
            if (!TextUtils.isEmpty(translation)) {
                saveUserAnswer(currentQuestionIndex, "翻译：" + translation);
            }
        }

        // 保存写作答案
        if (currentQuestion.type == QuestionType.WRITING && 
            layoutWriting.getVisibility() == View.VISIBLE && 
            etWriting != null) {
            String writing = etWriting.getText().toString().trim();
            if (!TextUtils.isEmpty(writing)) {
                saveUserAnswer(currentQuestionIndex, "作文：" + writing);
            }
        }
    }

    private void saveTextAnswers() {
        // 保存所有翻译和写作答案（在提交考试时调用）
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion question = questions.get(i);
            
            // 保存翻译答案
            if (question.type == QuestionType.TRANSLATION && etTranslation != null) {
                String translation = etTranslation.getText().toString().trim();
                if (!TextUtils.isEmpty(translation)) {
                    saveUserAnswer(i, "翻译：" + translation);
                }
            }

            // 保存写作答案
            if (question.type == QuestionType.WRITING && etWriting != null) {
                String writing = etWriting.getText().toString().trim();
                if (!TextUtils.isEmpty(writing)) {
                    saveUserAnswer(i, "作文：" + writing);
                }
            }
        }
    }

    /**
     * 恢复之前选择的答案状态
     */
    private void restoreSelectedOption() {
        // 先重置所有按钮
        resetOptionButtons();
        
        // 安全检查：确保userAnswers已初始化
        if (userAnswers == null) {
            return;
        }
        
        // 检查当前题目是否已经选择过答案
        String savedAnswer = userAnswers.get(currentQuestionIndex);
        android.util.Log.d("ExamAnswerActivity", "恢复答案状态: 题号=" + (currentQuestionIndex + 1) + ", savedAnswer=" + savedAnswer);
        
        if (savedAnswer != null && !savedAnswer.isEmpty()) {
            // 解析保存的答案，提取选项索引
            int selectedIndex = -1;
            
            // 精确匹配单个字符（匹配 A-H）
            if (savedAnswer.equals("A")) {
                selectedIndex = 0;
            } else if (savedAnswer.equals("B")) {
                selectedIndex = 1;
            } else if (savedAnswer.equals("C")) {
                selectedIndex = 2;
            } else if (savedAnswer.equals("D")) {
                selectedIndex = 3;
            } else if (savedAnswer.equals("E")) {
                selectedIndex = 4;
            } else if (savedAnswer.equals("F")) {
                selectedIndex = 5;
            } else if (savedAnswer.equals("G")) {
                selectedIndex = 6;
            } else if (savedAnswer.equals("H")) {
                selectedIndex = 7;
            }
            
            android.util.Log.d("ExamAnswerActivity", "解析选项索引: savedAnswer=" + savedAnswer + ", selectedIndex=" + selectedIndex);
            
            // 高亮之前选择的选项
            if (selectedIndex >= 0 && selectedIndex < 8) {
                highlightSelectedOption(selectedIndex);
                android.util.Log.d("ExamAnswerActivity", "已恢复选项高亮: 题号=" + (currentQuestionIndex + 1) + ", 选项=" + (char)('A' + selectedIndex));
            }
        }
    }
    
    /**
     * 根据当前题目索引获取题号显示范围
     * @return int数组，[起始索引, 结束索引]（左闭右开区间）
     * 
     * 题目结构（共52题）：
     * - 完形填空: 1-20题 (索引0-19)
     * - 阅读理解 Text 1: 21-25题 (索引20-24)
     * - 阅读理解 Text 2: 26-30题 (索引25-29)
     * - 阅读理解 Text 3: 31-35题 (索引30-34)
     * - 阅读理解 Text 4: 36-40题 (索引35-39)
     * - 新题型: 41-45题 (索引40-44)
     * - 翻译: 46-50题 (索引45-49)
     * - 写作 Part A: 51题 (索引50)
     * - 写作 Part B: 52题 (索引51)
     */
    private int[] getQuestionNumberRange() {
        if (currentQuestionIndex < 20) {
            return new int[]{0, 20}; // 完形填空 1-20题
        } else if (currentQuestionIndex < 25) {
            return new int[]{20, 25}; // 阅读理解 Text 1: 21-25题
        } else if (currentQuestionIndex < 30) {
            return new int[]{25, 30}; // 阅读理解 Text 2: 26-30题
        } else if (currentQuestionIndex < 35) {
            return new int[]{30, 35}; // 阅读理解 Text 3: 31-35题
        } else if (currentQuestionIndex < 40) {
            return new int[]{35, 40}; // 阅读理解 Text 4: 36-40题
        } else if (currentQuestionIndex < 45) {
            return new int[]{40, 45}; // 新题型: 41-45题
        } else if (currentQuestionIndex < 50) {
            return new int[]{45, 50}; // 翻译: 46-50题
        } else {
            return new int[]{50, 52}; // 写作: 51-52题
        }
    }
    
    /**
     * 初始化题号切换器
     */
    private void initQuestionNumbers() {
        android.util.Log.d("ExamAnswerActivity", "开始初始化题号切换器");
        
        if (layoutQuestionNumbers == null) {
            android.util.Log.e("ExamAnswerActivity", "layoutQuestionNumbers为null");
            return;
        }
        
        if (questions == null || questions.isEmpty()) {
            android.util.Log.e("ExamAnswerActivity", "questions为空");
            return;
        }
        
        // 根据当前题目类型动态获取题号范围
        int[] range = getQuestionNumberRange();
        
        // 检查范围是否改变，如果没变则只更新高亮，不重建
        if (currentRange != null && currentRange[0] == range[0] && currentRange[1] == range[1]) {
            android.util.Log.d("ExamAnswerActivity", "题号范围未变化，只更新高亮");
            updateQuestionNumberHighlight();
            return;
        }
        
        // 范围改变了，需要重建题号按钮
        currentRange = range;
        layoutQuestionNumbers.removeAllViews();
        
        int startIndex = range[0];
        int endIndex = Math.min(range[1], questions.size());
        int questionCount = endIndex - startIndex;
        android.util.Log.d("ExamAnswerActivity", "题号范围改变，将创建" + questionCount + "个题号按钮 (从" + (startIndex + 1) + "到" + endIndex + ")");
        
        for (int i = 0; i < questionCount; i++) {
            final int questionIndex = startIndex + i;
            
            TextView tvNumber = new TextView(this);
            tvNumber.setText(String.valueOf(questionIndex + 1));
            tvNumber.setTextSize(16);
            tvNumber.setPadding(32, 16, 32, 16);
            tvNumber.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
            tvNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_question_number));
            tvNumber.setGravity(android.view.Gravity.CENTER);
            
            // 设置布局参数 - 增大间距
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(12, 8, 12, 8);
            tvNumber.setLayoutParams(params);
            
            // 点击跳转到对应题目
            tvNumber.setOnClickListener(v -> jumpToQuestion(questionIndex));
            
            layoutQuestionNumbers.addView(tvNumber);
        }
        
        android.util.Log.d("ExamAnswerActivity", "题号按钮创建完成，共" + layoutQuestionNumbers.getChildCount() + "个");
        
        // 高亮当前题号
        updateQuestionNumberHighlight();
    }
    
    /**
     * 跳转到指定题目
     */
    private void jumpToQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            // 在跳转前保存当前题目的文本答案
            saveCurrentTextAnswer();
            
            currentQuestionIndex = index;
            showCurrentQuestion();
        }
    }
    
    /**
     * 更新题号高亮
     */
    private void updateQuestionNumberHighlight() {
        if (layoutQuestionNumbers == null) {
            return;
        }
        
        // 获取当前题号范围
        int[] range = getQuestionNumberRange();
        int startIndex = range[0];
        
        for (int i = 0; i < layoutQuestionNumbers.getChildCount(); i++) {
            TextView tvNumber = (TextView) layoutQuestionNumbers.getChildAt(i);
            
            // 计算实际的题目索引
            int questionIndex = startIndex + i;
            
            // 检查该题是否已答
            boolean isAnswered = userAnswers.containsKey(questionIndex) && userAnswers.get(questionIndex) != null && !userAnswers.get(questionIndex).isEmpty();
            
            if (questionIndex == currentQuestionIndex) {
                // 当前题号 - 蓝色高亮显示
                tvNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_question_number_selected));
                tvNumber.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                tvNumber.setTextSize(16);
                tvNumber.setTypeface(null, android.graphics.Typeface.BOLD);
            } else if (isAnswered) {
                // 已答题 - 显示勾选标记
                tvNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_question_number));
                tvNumber.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
                tvNumber.setText(String.valueOf(questionIndex + 1) + "✓");
                tvNumber.setTextSize(15);
                tvNumber.setTypeface(null, android.graphics.Typeface.NORMAL);
            } else {
                // 未答题 - 默认样式
                tvNumber.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_question_number));
                tvNumber.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
                tvNumber.setText(String.valueOf(questionIndex + 1));
                tvNumber.setTextSize(16);
                tvNumber.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void showFinalResult() {
        String message = "考试完成！\n";
        message += "考试名称：" + (examTitle != null ? examTitle : "未知考试") + "\n";
        message += "总题数：" + questions.size() + "\n";
        message += "已答题数：" + userAnswers.size() + "\n";
        message += "考试时间：3小时";

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // 返回上一页
        finish();
    }

    /**
     * 显示题型选择菜单
     */
    private void showQuestionTypeMenu() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("选择题型");
        
        String[] questionTypes = {
            "完形填空 (1-20题)",
            "阅读理解 Text 1 (21-25题)",
            "阅读理解 Text 2 (26-30题)",
            "阅读理解 Text 3 (31-35题)",
            "阅读理解 Text 4 (36-40题)",
            "新题型 (41-45题)",
            "翻译 (46题)",
            "写作 (47题)"
        };
        
        int[] startIndexes = {0, 20, 25, 30, 35, 40, 45, 46};
        
        builder.setItems(questionTypes, (dialog, which) -> {
            int targetIndex = startIndexes[which];
            if (targetIndex < questions.size()) {
                // 保存当前题目答案
                saveCurrentTextAnswer();
                // 跳转到目标题目
                jumpToQuestion(targetIndex);
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 显示笔记对话框
     */
    private void showNoteDialog() {
        // 首先获取当前题目的现有笔记
        questionNoteRepository.getNote(examTitle, currentQuestionIndex, new QuestionNoteRepository.NoteQueryCallback() {
            @Override
            public void onResult(com.example.mybighomework.database.entity.QuestionNoteEntity note) {
                String existingNote = (note != null) ? note.getNoteContent() : null;
                
                // 显示笔记对话框
                NoteDialog dialog = new NoteDialog(
                    ExamAnswerActivity.this,
                    examTitle,
                    currentQuestionIndex,
                    existingNote,
                    new NoteDialog.OnNoteActionListener() {
                        @Override
                        public void onNoteSaved(String noteContent) {
                            // 保存笔记到数据库
                            questionNoteRepository.saveNote(
                                examTitle, 
                                currentQuestionIndex, 
                                noteContent,
                                new QuestionNoteRepository.NoteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(ExamAnswerActivity.this, 
                                            "笔记已保存", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(ExamAnswerActivity.this, 
                                            "保存笔记失败: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }
                            );
                        }

                        @Override
                        public void onNoteDeleted() {
                            // 删除笔记
                            questionNoteRepository.deleteNote(
                                examTitle, 
                                currentQuestionIndex,
                                new QuestionNoteRepository.NoteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(ExamAnswerActivity.this, 
                                            "笔记已删除", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(ExamAnswerActivity.this, 
                                            "删除笔记失败: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }
                            );
                        }
                    }
                );
                
                android.view.Window window = dialog.getWindow();
                if (window != null) {
                    window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
                
                dialog.show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ExamAnswerActivity.this, 
                    "加载笔记失败: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 显示答题卡
     */
    private void showAnswerSheet() {
        AnswerSheetDialog dialog = new AnswerSheetDialog(
            this,
            questions.size(),
            currentQuestionIndex,
            userAnswers,
            questionIndex -> {
                // 跳转到指定题目
                jumpToQuestion(questionIndex);
            },
            () -> {
                // 交卷
                submitExam();
            }
        );
        
        dialog.show();
    }

    /**
     * 显示批改进度对话框
     */
    private void showGradingDialog() {
        mainHandler.post(() -> {
            gradingDialog = new ProgressDialog(this);
            gradingDialog.setMessage("正在批改试卷，请稍候...");
            gradingDialog.setCancelable(false);
            gradingDialog.show();
        });
    }
    
    /**
     * 更新批改进度对话框
     */
    private void updateGradingDialog(String message) {
        mainHandler.post(() -> {
            if (gradingDialog != null && gradingDialog.isShowing()) {
                gradingDialog.setMessage(message);
            }
        });
    }
    
    /**
     * 关闭批改进度对话框
     */
    private void dismissGradingDialog() {
        mainHandler.post(() -> {
            if (gradingDialog != null && gradingDialog.isShowing()) {
                gradingDialog.dismiss();
            }
        });
    }
    
    /**
     * 批改选择题
     * @return 考试成绩实体
     */
    private ExamResultEntity gradeExam() {
        ExamResultEntity result = new ExamResultEntity();
        result.setExamTitle(examTitle);
        result.setExamYear(examYear);
        result.setExamDate(new Date());
        result.setExamDuration(System.currentTimeMillis() - examStartTime);
        
        // 初始化统计变量
        int clozeCorrect = 0, clozeTotal = 0;
        int readingCorrect = 0, readingTotal = 0;
        int newTypeCorrect = 0, newTypeTotal = 0;
        int totalCorrect = 0, totalWrong = 0;
        
        // 用于存储每题的答题详情
        JSONArray answerDetailsArray = new JSONArray();
        
        // 遍历所有题目批改
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion question = questions.get(i);
            String userAnswer = userAnswers.get(i);
            
            // 跳过翻译和写作题（非选择题）
            if (question.type == QuestionType.TRANSLATION || question.type == QuestionType.WRITING) {
                continue;
            }
            
            // 解析用户答案
            int userAnswerIndex = -1;
            if (userAnswer != null && userAnswer.length() == 1) {
                char answerChar = userAnswer.charAt(0);
                if (answerChar >= 'A' && answerChar <= 'H') {
                    userAnswerIndex = answerChar - 'A';
                }
            }
            
            // 判断正误
            boolean isCorrect = (userAnswerIndex == question.correctAnswer);
            
            // 按题型分类统计
            if (question.type == QuestionType.CLOZE_TEST) {
                clozeTotal++;
                if (isCorrect) clozeCorrect++;
            } else if (question.type == QuestionType.READING_COMPREHENSION) {
                // 判断是否为新题型
                if (question.title.contains("新题型")) {
                    newTypeTotal++;
                    if (isCorrect) newTypeCorrect++;
                } else {
                    readingTotal++;
                    if (isCorrect) readingCorrect++;
                }
            }
            
            if (isCorrect) {
                totalCorrect++;
            } else {
                totalWrong++;
            }
            
            // 记录每题的详细答题情况
            try {
                JSONObject questionDetail = new JSONObject();
                questionDetail.put("questionIndex", i);
                questionDetail.put("questionType", question.type.toString());
                questionDetail.put("questionTitle", question.title);
                questionDetail.put("userAnswer", userAnswer != null ? userAnswer : "未作答");
                questionDetail.put("correctAnswer", String.valueOf((char)('A' + question.correctAnswer)));
                questionDetail.put("isCorrect", isCorrect);
                questionDetail.put("explanation", question.explanation);
                answerDetailsArray.put(questionDetail);
            } catch (Exception e) {
                android.util.Log.e("ExamAnswerActivity", "记录答题详情失败", e);
            }
        }
        
        // 计算各部分得分
        float clozeScore = clozeCorrect * 0.5f;  // 完形填空每题0.5分
        float readingScore = readingCorrect * 1.0f;  // 阅读理解每题1分
        float newTypeScore = newTypeCorrect * 2.0f;  // 新题型每题2分
        
        // 设置成绩
        result.setClozeScore(clozeScore);
        result.setReadingScore(readingScore);
        result.setNewTypeScore(newTypeScore);
        result.setClozeCorrect(clozeCorrect);
        result.setClozeTotal(clozeTotal);
        result.setReadingCorrect(readingCorrect);
        result.setReadingTotal(readingTotal);
        result.setNewTypeCorrect(newTypeCorrect);
        result.setNewTypeTotal(newTypeTotal);
        result.setCorrectAnswers(totalCorrect);
        result.setWrongAnswers(totalWrong);
        result.setTotalQuestions(clozeTotal + readingTotal + newTypeTotal);
        
        // 计算正确率
        int totalQuestions = clozeTotal + readingTotal + newTypeTotal;
        if (totalQuestions > 0) {
            result.setAccuracy((float) totalCorrect / totalQuestions);
        }
        
        // 保存答题详情
        result.setAnswerDetails(answerDetailsArray.toString());
        
        android.util.Log.d("ExamAnswerActivity", "批改完成 - 完形:" + clozeCorrect + "/" + clozeTotal + 
            ", 阅读:" + readingCorrect + "/" + readingTotal + 
            ", 新题型:" + newTypeCorrect + "/" + newTypeTotal);
        
        return result;
    }
    
    // 用于存储翻译题批改结果的临时变量
    private float[] translationScores = new float[5];
    private StringBuilder translationComments = new StringBuilder();
    private int translationGradedCount = 0;
    
    // 用于存储写作题批改结果的临时变量
    private float writingAScoreTemp = 0;
    private String writingACommentTemp = "";
    private float writingBScoreTemp = 0;
    private String writingBCommentTemp = "";
    
    /**
     * AI批改翻译和写作（重构版本）
     * 支持分别批改5道翻译题和2道写作题
     */
    private void gradeTranslationAndWriting(ExamResultEntity result) {
        // 重置临时变量
        translationScores = new float[5];
        translationComments = new StringBuilder();
        translationGradedCount = 0;
        writingAScoreTemp = 0;
        writingACommentTemp = "";
        writingBScoreTemp = 0;
        writingBCommentTemp = "";
        
        // 收集所有翻译题和写作题
        java.util.List<Integer> translationIndices = new java.util.ArrayList<>();
        java.util.List<Integer> writingAIndices = new java.util.ArrayList<>();
        java.util.List<Integer> writingBIndices = new java.util.ArrayList<>();
        
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion question = questions.get(i);
            if (question.type == QuestionType.TRANSLATION) {
                translationIndices.add(i);
            } else if (question.type == QuestionType.WRITING) {
                if ("PART_A".equals(question.writingType)) {
                    writingAIndices.add(i);
                } else if ("PART_B".equals(question.writingType)) {
                    writingBIndices.add(i);
                }
            }
        }
        
        android.util.Log.d("ExamAnswerActivity", "找到翻译题: " + translationIndices.size() + 
            ", Part A写作: " + writingAIndices.size() + ", Part B写作: " + writingBIndices.size());
        
        // 开始批改翻译题
        if (!translationIndices.isEmpty()) {
            gradeTranslationsSequentially(result, translationIndices, 0, writingAIndices, writingBIndices);
        } else {
            // 没有翻译题，直接批改写作
            result.setTranslationScore(0.0f);
            result.setTranslationComment("未作答");
            gradeWritingPartAInternal(result, writingAIndices, writingBIndices);
        }
    }
    
    /**
     * 顺序批改翻译题
     */
    private void gradeTranslationsSequentially(ExamResultEntity result, 
            java.util.List<Integer> translationIndices, int currentIndex,
            java.util.List<Integer> writingAIndices, java.util.List<Integer> writingBIndices) {
        
        if (currentIndex >= translationIndices.size()) {
            // 所有翻译题批改完成，计算总分
            float totalTranslationScore = 0;
            for (float score : translationScores) {
                totalTranslationScore += score;
            }
            result.setTranslationScore(totalTranslationScore);
            result.setTranslationComment(translationComments.toString().trim());
            
            android.util.Log.d("ExamAnswerActivity", "翻译题批改完成 - 总分:" + totalTranslationScore);
            
            // 继续批改写作Part A
            gradeWritingPartAInternal(result, writingAIndices, writingBIndices);
            return;
        }
        
        int questionIndex = translationIndices.get(currentIndex);
        ExamQuestion question = questions.get(questionIndex);
        String answer = userAnswers.get(questionIndex);
        
        // 提取用户答案
        String userTranslation = "";
        if (answer != null) {
            if (answer.startsWith("翻译：")) {
                userTranslation = answer.substring(3);
            } else {
                userTranslation = answer;
            }
        }
        
        int questionNumber = 46 + currentIndex; // 翻译题从46题开始
        updateGradingDialog("正在批改翻译第" + questionNumber + "题 (" + (currentIndex + 1) + "/5)...");
        
        if (userTranslation.trim().isEmpty()) {
            // 未作答
            translationScores[currentIndex] = 0;
            translationComments.append("第").append(questionNumber).append("题：未作答\n");
            // 继续下一题
            gradeTranslationsSequentially(result, translationIndices, currentIndex + 1, writingAIndices, writingBIndices);
        } else {
            // 调用AI批改
            String referenceAnswer = question.referenceAnswer != null ? question.referenceAnswer : "";
            String originalText = question.passage != null ? question.passage : "";
            
            final int idx = currentIndex;
            final int qNum = questionNumber;
            
            zhipuAIService.gradeTranslationWithReference(userTranslation, referenceAnswer, originalText, 3.0f,
                new ZhipuAIService.GradeCallback() {
                    @Override
                    public void onSuccess(ZhipuAIService.GradeResult gradeResult) {
                        translationScores[idx] = gradeResult.getScore();
                        translationComments.append("第").append(qNum).append("题(").append(String.format("%.1f", gradeResult.getScore())).append("分)：")
                            .append(gradeResult.getComment()).append("\n");
                        android.util.Log.d("ExamAnswerActivity", "翻译第" + qNum + "题批改完成 - 得分:" + gradeResult.getScore());
                        // 继续下一题
                        gradeTranslationsSequentially(result, translationIndices, idx + 1, writingAIndices, writingBIndices);
                    }
                    
                    @Override
                    public void onError(String error) {
                        android.util.Log.e("ExamAnswerActivity", "翻译第" + qNum + "题批改失败: " + error);
                        // 批改失败给默认分数
                        translationScores[idx] = 2.0f;
                        translationComments.append("第").append(qNum).append("题(2.0分)：AI批改失败，给予默认分数\n");
                        // 继续下一题
                        gradeTranslationsSequentially(result, translationIndices, idx + 1, writingAIndices, writingBIndices);
                    }
                });
        }
    }
    
    /**
     * 批改写作Part A
     */
    private void gradeWritingPartAInternal(ExamResultEntity result, 
            java.util.List<Integer> writingAIndices, java.util.List<Integer> writingBIndices) {
        
        if (writingAIndices.isEmpty()) {
            // 没有Part A写作题
            result.setWritingScore(0.0f);
            result.setWritingComment("未作答");
            // 继续批改Part B
            gradeWritingPartBInternal(result, writingBIndices);
            return;
        }
        
        int questionIndex = writingAIndices.get(0);
        ExamQuestion question = questions.get(questionIndex);
        String answer = userAnswers.get(questionIndex);
        
        // 提取用户答案
        String userEssay = "";
        if (answer != null) {
            if (answer.startsWith("作文：")) {
                userEssay = answer.substring(3);
            } else {
                userEssay = answer;
            }
        }
        
        updateGradingDialog("正在批改写作Part A...");
        
        if (userEssay.trim().isEmpty()) {
            // 未作答
            writingAScoreTemp = 0;
            writingACommentTemp = "Part A：未作答";
            // 继续批改Part B
            gradeWritingPartBInternal(result, writingBIndices);
        } else {
            String topic = question.question != null ? question.question : "";
            
            zhipuAIService.gradeWritingPartA(userEssay, topic, new ZhipuAIService.GradeCallback() {
                @Override
                public void onSuccess(ZhipuAIService.GradeResult gradeResult) {
                    writingAScoreTemp = gradeResult.getScore();
                    writingACommentTemp = "Part A(" + String.format("%.1f", gradeResult.getScore()) + "分)：" + gradeResult.getComment();
                    android.util.Log.d("ExamAnswerActivity", "写作Part A批改完成 - 得分:" + gradeResult.getScore());
                    // 继续批改Part B
                    gradeWritingPartBInternal(result, writingBIndices);
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("ExamAnswerActivity", "写作Part A批改失败: " + error);
                    // 批改失败给默认分数
                    writingAScoreTemp = 6.0f;
                    writingACommentTemp = "Part A(6.0分)：AI批改失败，给予默认分数";
                    // 继续批改Part B
                    gradeWritingPartBInternal(result, writingBIndices);
                }
            });
        }
    }
    
    /**
     * 批改写作Part B
     */
    private void gradeWritingPartBInternal(ExamResultEntity result, java.util.List<Integer> writingBIndices) {
        
        if (writingBIndices.isEmpty()) {
            // 没有Part B写作题，完成批改
            float totalWritingScore = writingAScoreTemp + writingBScoreTemp;
            result.setWritingScore(totalWritingScore);
            String comment = writingACommentTemp;
            if (!writingBCommentTemp.isEmpty()) {
                comment += "\n" + writingBCommentTemp;
            }
            result.setWritingComment(comment);
            finishGrading(result);
            return;
        }
        
        int questionIndex = writingBIndices.get(0);
        ExamQuestion question = questions.get(questionIndex);
        String answer = userAnswers.get(questionIndex);
        
        // 提取用户答案
        String userEssay = "";
        if (answer != null) {
            if (answer.startsWith("作文：")) {
                userEssay = answer.substring(3);
            } else {
                userEssay = answer;
            }
        }
        
        updateGradingDialog("正在批改写作Part B...");
        
        if (userEssay.trim().isEmpty()) {
            // 未作答
            writingBScoreTemp = 0;
            writingBCommentTemp = "Part B：未作答";
            // 完成批改
            float totalWritingScore = writingAScoreTemp + writingBScoreTemp;
            result.setWritingScore(totalWritingScore);
            String comment = writingACommentTemp;
            if (!writingBCommentTemp.isEmpty()) {
                comment += "\n" + writingBCommentTemp;
            }
            result.setWritingComment(comment);
            finishGrading(result);
        } else {
            String topic = question.question != null ? question.question : "";
            
            zhipuAIService.gradeWritingPartB(userEssay, topic, new ZhipuAIService.GradeCallback() {
                @Override
                public void onSuccess(ZhipuAIService.GradeResult gradeResult) {
                    writingBScoreTemp = gradeResult.getScore();
                    writingBCommentTemp = "Part B(" + String.format("%.1f", gradeResult.getScore()) + "分)：" + gradeResult.getComment();
                    android.util.Log.d("ExamAnswerActivity", "写作Part B批改完成 - 得分:" + gradeResult.getScore());
                    
                    // 完成批改
                    float totalWritingScore = writingAScoreTemp + writingBScoreTemp;
                    result.setWritingScore(totalWritingScore);
                    String comment = writingACommentTemp;
                    if (!writingBCommentTemp.isEmpty()) {
                        comment += "\n" + writingBCommentTemp;
                    }
                    result.setWritingComment(comment);
                    finishGrading(result);
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("ExamAnswerActivity", "写作Part B批改失败: " + error);
                    // 批改失败给默认分数
                    writingBScoreTemp = 10.0f;
                    writingBCommentTemp = "Part B(10.0分)：AI批改失败，给予默认分数";
                    
                    // 完成批改
                    float totalWritingScore = writingAScoreTemp + writingBScoreTemp;
                    result.setWritingScore(totalWritingScore);
                    String comment = writingACommentTemp;
                    if (!writingBCommentTemp.isEmpty()) {
                        comment += "\n" + writingBCommentTemp;
                    }
                    result.setWritingComment(comment);
                    finishGrading(result);
                }
            });
        }
    }
    
    /**
     * 批改写作（旧方法，保持兼容）
     */
    private void gradeWritingInternal(ExamResultEntity result, String writingAnswer, String writingTopic) {
        if (writingAnswer != null && !writingAnswer.trim().isEmpty()) {
            updateGradingDialog("正在批改写作题...");
            
            zhipuAIService.gradeWriting(writingAnswer, writingTopic, new ZhipuAIService.GradeCallback() {
                @Override
                public void onSuccess(ZhipuAIService.GradeResult gradeResult) {
                    result.setWritingScore(gradeResult.getScore());
                    result.setWritingComment(gradeResult.getComment());
                    android.util.Log.d("ExamAnswerActivity", "写作批改完成 - 得分:" + gradeResult.getScore() + ", 评语:" + gradeResult.getComment());
                    
                    // 计算总分并保存成绩
                    finishGrading(result);
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("ExamAnswerActivity", "写作批改失败: " + error);
                    // 批改失败给默认分数
                    result.setWritingScore(10.0f);
                    result.setWritingComment("AI批改失败，系统给予默认分数。");
                    
                    // 计算总分并保存成绩
                    finishGrading(result);
                }
            });
        } else {
            // 未作答写作题
            result.setWritingScore(0.0f);
            result.setWritingComment("未作答");
            
            // 计算总分并保存成绩
            finishGrading(result);
        }
    }
    
    /**
     * 完成批改，计算总分并保存
     */
    private void finishGrading(ExamResultEntity result) {
        // 计算总分
        float totalScore = result.getClozeScore() + result.getReadingScore() + 
                          result.getNewTypeScore() + result.getTranslationScore() + 
                          result.getWritingScore();
        result.setTotalScore(totalScore);
        
        // 评定等级
        String grade;
        if (totalScore >= 60) {
            grade = "优秀";
        } else if (totalScore >= 45) {
            grade = "良好";
        } else if (totalScore >= 30) {
            grade = "及格";
        } else {
            grade = "不及格";
        }
        result.setGrade(grade);
        
        android.util.Log.d("ExamAnswerActivity", "批改完成 - 总分:" + totalScore + ", 等级:" + grade);
        
        // 保存成绩到数据库
        examResultRepository.addResult(result, new ExamResultRepository.ResultCallback() {
            @Override
            public void onSuccess(ExamResultEntity savedResult) {
                android.util.Log.d("ExamAnswerActivity", "成绩已保存到数据库，ID:" + savedResult.getId());
                
                // 关闭批改对话框
                dismissGradingDialog();
                
                // 跳转到成绩详情页
                mainHandler.post(() -> {
                    Intent intent = new Intent(ExamAnswerActivity.this, ExamResultActivity.class);
                    intent.putExtra("result_id", savedResult.getId());
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                android.util.Log.e("ExamAnswerActivity", "保存成绩失败: " + error);
                dismissGradingDialog();
                mainHandler.post(() -> {
                    Toast.makeText(ExamAnswerActivity.this, "保存成绩失败: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (examTimer != null) {
            examTimer.cancel();
        }
        if (zhipuAIService != null) {
            zhipuAIService.shutdown();
        }
    }
}
