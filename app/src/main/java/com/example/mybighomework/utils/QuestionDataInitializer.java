package com.example.mybighomework.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import com.example.mybighomework.database.entity.QuestionEntity;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;
import com.example.mybighomework.repository.QuestionRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目数据初始化工具
 * 将预设的四六级题目导入数据库
 */
public class QuestionDataInitializer {
    
    private static final String PREFS_NAME = "QuestionDataPrefs";
    private static final String KEY_DATA_INITIALIZED = "data_initialized";
    private static final String KEY_DATA_VERSION = "data_version";
    private static final int CURRENT_DATA_VERSION = 2; // 数据版本号，修改题目时增加
    
    /**
     * 初始化所有题目数据
     */
    public static void initializeIfNeeded(Application application) {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean initialized = prefs.getBoolean(KEY_DATA_INITIALIZED, false);
        int dataVersion = prefs.getInt(KEY_DATA_VERSION, 0);
        
        // 如果未初始化或版本更新，则重新初始化
        if (!initialized || dataVersion < CURRENT_DATA_VERSION) {
            initializeAllData(application, prefs);
        }
    }
    
    /**
     * 强制重新初始化数据（用于更新题库）
     */
    public static void forceReinitialize(Application application) {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initializeAllData(application, prefs);
    }
    
    private static void initializeAllData(Application application, SharedPreferences prefs) {
        // 初始化词汇题
        initializeVocabularyQuestions(application);
        
        // 初始化真题
        initializeExamPracticeQuestions(application);
        
        // 初始化模拟题
        initializeMockExamQuestions(application);
        
        // 初始化词汇记录
        initializeVocabularyRecords(application);
        
        // 标记为已初始化
        prefs.edit()
            .putBoolean(KEY_DATA_INITIALIZED, true)
            .putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION)
            .apply();
    }
    
    /**
     * 初始化词汇训练题目
     */
    private static void initializeVocabularyQuestions(Application application) {
        QuestionRepository repository = new QuestionRepository(application);
        List<QuestionEntity> questions = new ArrayList<>();
        
        // 添加四六级词汇题（对应VocabularyActivity中的数据）
        questions.add(createVocabularyQuestion(
            "abandon", "[əˈbændən]", "v. 放弃；抛弃",
            new String[]{"放弃；抛弃", "获得；得到", "继续；坚持", "开始；启动"}, 0));
            
        questions.add(createVocabularyQuestion(
            "ability", "[əˈbɪləti]", "n. 能力；才能",
            new String[]{"困难；障碍", "能力；才能", "疾病；不适", "敌意；反对"}, 1));
            
        questions.add(createVocabularyQuestion(
            "achieve", "[əˈtʃiːv]", "v. 实现；达到",
            new String[]{"失败；落败", "放弃；舍弃", "实现；达到", "忽视；忽略"}, 2));
            
        questions.add(createVocabularyQuestion(
            "advantage", "[ədˈvɑːntɪdʒ]", "n. 优势；有利条件",
            new String[]{"缺点；劣势", "困难；障碍", "危险；风险", "优势；有利条件"}, 3));
            
        questions.add(createVocabularyQuestion(
            "analyze", "[ˈænəlaɪz]", "v. 分析；解析",
            new String[]{"分析；解析", "综合；合成", "忽略；无视", "混淆；搞乱"}, 0));
            
        questions.add(createVocabularyQuestion(
            "approach", "[əˈprəʊtʃ]", "v./n. 接近；方法",
            new String[]{"离开；远离", "接近；方法", "拒绝；否认", "破坏；损坏"}, 1));
            
        questions.add(createVocabularyQuestion(
            "appropriate", "[əˈprəʊpriət]", "adj. 适当的；恰当的",
            new String[]{"错误的；不对的", "危险的；冒险的", "适当的；恰当的", "困难的；艰难的"}, 2));
            
        questions.add(createVocabularyQuestion(
            "benefit", "[ˈbenɪfɪt]", "n./v. 利益；好处；有益于",
            new String[]{"损失；伤害", "困难；障碍", "危险；风险", "利益；好处；有益于"}, 3));
            
        questions.add(createVocabularyQuestion(
            "challenge", "[ˈtʃælɪndʒ]", "n./v. 挑战",
            new String[]{"挑战", "帮助；援助", "简化；使简单", "避免；回避"}, 0));
            
        questions.add(createVocabularyQuestion(
            "contribute", "[kənˈtrɪbjuːt]", "v. 贡献；捐助",
            new String[]{"破坏；损坏", "贡献；捐助", "拒绝；否认", "减少；降低"}, 1));
            
        questions.add(createVocabularyQuestion(
            "demonstrate", "[ˈdemənstreɪt]", "v. 证明；演示",
            new String[]{"隐藏；掩盖", "混淆；搞乱", "证明；演示", "拒绝；否认"}, 2));
            
        questions.add(createVocabularyQuestion(
            "enhance", "[ɪnˈhɑːns]", "v. 提高；增强",
            new String[]{"减少；降低", "忽略；忽视", "破坏；损害", "提高；增强"}, 3));
            
        questions.add(createVocabularyQuestion(
            "establish", "[ɪˈstæblɪʃ]", "v. 建立；确立",
            new String[]{"建立；确立", "破坏；摧毁", "放弃；舍弃", "拒绝；否认"}, 0));
            
        questions.add(createVocabularyQuestion(
            "function", "[ˈfʌŋkʃn]", "n./v. 功能；运作",
            new String[]{"失败；故障", "功能；运作", "危险；风险", "困难；障碍"}, 1));
            
        questions.add(createVocabularyQuestion(
            "generate", "[ˈdʒenəreɪt]", "v. 产生；引起",
            new String[]{"破坏；损坏", "消除；清除", "产生；引起", "减少；降低"}, 2));
        
        // 批量插入
        repository.insertQuestions(questions, null);
    }
    
    /**
     * 创建词汇题目
     */
    private static QuestionEntity createVocabularyQuestion(String word, String phonetic, 
                                                          String meaning, String[] options, 
                                                          int correctAnswer) {
        QuestionEntity question = new QuestionEntity();
        question.setQuestionText(word + " " + phonetic);
        question.setOptions(options);
        question.setCorrectAnswer(correctAnswer);
        question.setExplanation(meaning);
        question.setCategory("词汇");
        question.setExamType("四六级");
        question.setDifficulty("medium");
        question.setSource("词汇训练");
        return question;
    }
    
    /**
     * 初始化真题练习题目
     */
    private static void initializeExamPracticeQuestions(Application application) {
        QuestionRepository repository = new QuestionRepository(application);
        List<QuestionEntity> questions = new ArrayList<>();
        
        // 添加真题数据
        questions.add(createExamQuestion(
            "The company has decided to _______ its operations to include online sales.",
            new String[]{"expand", "contract", "maintain", "eliminate"},
            0,
            "expand意为扩展，符合句意：公司决定扩展业务以包括在线销售。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "Despite the heavy rain, the outdoor concert was not _______.",
            new String[]{"postponed", "promoted", "prevented", "predicted"},
            0,
            "postponed意为推迟，符合句意：尽管下大雨，户外音乐会没有被推迟。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The new policy will _______ all employees regardless of their position.",
            new String[]{"affect", "effect", "infect", "defect"},
            0,
            "affect作动词意为影响，符合句意：新政策将影响所有员工，无论他们的职位如何。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "She has a _______ for languages and can speak five fluently.",
            new String[]{"talent", "habit", "hobby", "interest"},
            0,
            "talent意为天赋，符合句意：她有语言天赋，能流利地说五种语言。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The research team needs to _______ more data before drawing conclusions.",
            new String[]{"collect", "connect", "correct", "contact"},
            0,
            "collect意为收集，符合句意：研究团队需要收集更多数据才能得出结论。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The meeting has been _______ until next Friday due to scheduling conflicts.",
            new String[]{"delayed", "delivered", "deleted", "declared"},
            0,
            "delayed意为延迟，符合句意：由于时间安排冲突，会议被延迟到下周五。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "Students are required to _______ their assignments by the deadline.",
            new String[]{"submit", "admit", "permit", "commit"},
            0,
            "submit意为提交，符合句意：学生需要在截止日期前提交作业。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The new software will help _______ the efficiency of our work processes.",
            new String[]{"improve", "approve", "remove", "move"},
            0,
            "improve意为改善，符合句意：新软件将帮助提高我们工作流程的效率。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The company's profits have _______ significantly over the past year.",
            new String[]{"increased", "decreased", "remained", "disappeared"},
            0,
            "increased意为增加，符合句意：公司利润在过去一年中显著增加。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "Please _______ me if you need any assistance with the project.",
            new String[]{"contact", "contract", "construct", "conduct"},
            0,
            "contact意为联系，符合句意：如果你在项目中需要任何帮助，请联系我。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The weather forecast _______ rain for the weekend.",
            new String[]{"predicts", "prevents", "protects", "provides"},
            0,
            "predicts意为预测，符合句意：天气预报预测周末会下雨。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The manager decided to _______ the project deadline by one week.",
            new String[]{"extend", "intend", "attend", "defend"},
            0,
            "extend意为延长，符合句意：经理决定将项目截止日期延长一周。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The students were asked to _______ their opinions on the new curriculum.",
            new String[]{"express", "impress", "suppress", "compress"},
            0,
            "express意为表达，符合句意：学生们被要求表达对新课程的意见。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The company will _______ a new product line next month.",
            new String[]{"launch", "lunch", "branch", "ranch"},
            0,
            "launch意为推出，符合句意：公司将在下个月推出新的产品线。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The team needs to _______ their strategy for the upcoming competition.",
            new String[]{"revise", "advise", "devise", "supervise"},
            0,
            "revise意为修订，符合句意：团队需要修订即将到来的比赛策略。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The professor will _______ the lecture due to technical difficulties.",
            new String[]{"postpone", "propose", "suppose", "compose"},
            0,
            "postpone意为推迟，符合句意：由于技术困难，教授将推迟讲座。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The government plans to _______ new regulations for environmental protection.",
            new String[]{"implement", "complement", "supplement", "experiment"},
            0,
            "implement意为实施，符合句意：政府计划实施新的环保法规。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The research findings will _______ to our understanding of the disease.",
            new String[]{"contribute", "distribute", "attribute", "substitute"},
            0,
            "contribute意为贡献，符合句意：研究发现将有助于我们对疾病的理解。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The company decided to _______ its headquarters to a larger building.",
            new String[]{"relocate", "allocate", "locate", "dislocate"},
            0,
            "relocate意为搬迁，符合句意：公司决定将总部搬迁到更大的建筑。",
            "词汇", "真题练习"));
            
        questions.add(createExamQuestion(
            "The new employee needs time to _______ to the company culture.",
            new String[]{"adapt", "adopt", "adept", "accept"},
            0,
            "adapt意为适应，符合句意：新员工需要时间适应公司文化。",
            "词汇", "真题练习"));
        
        repository.insertQuestions(questions, null);
    }
    
    /**
     * 初始化模拟考试题目
     */
    private static void initializeMockExamQuestions(Application application) {
        QuestionRepository repository = new QuestionRepository(application);
        List<QuestionEntity> questions = new ArrayList<>();
        
        // 模拟考试词汇题
        questions.add(createExamQuestion(
            "The company's new policy will have a significant _______ on employee productivity.",
            new String[]{"impact", "compact", "contact", "contract"},
            0,
            "impact意为影响，符合句意：公司的新政策将对员工生产力产生重大影响。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "She was _______ to find that her application had been accepted.",
            new String[]{"delighted", "delayed", "deleted", "delivered"},
            0,
            "delighted意为高兴的，符合句意：她很高兴发现自己的申请被接受了。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The professor's lecture was so _______ that many students fell asleep.",
            new String[]{"boring", "interested", "exciting", "fascinating"},
            0,
            "boring意为无聊的，符合句意：教授的讲座如此无聊以至于很多学生睡着了。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The company needs to _______ its marketing strategy to attract younger consumers.",
            new String[]{"adjust", "object", "reject", "project"},
            0,
            "adjust意为调整，符合句意：公司需要调整营销策略以吸引年轻消费者。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The research team will _______ the experiment next month.",
            new String[]{"conduct", "confuse", "conclude", "construct"},
            0,
            "conduct意为进行、实施，符合句意：研究团队将在下个月进行实验。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The new law will _______ stricter regulations on environmental protection.",
            new String[]{"impose", "compose", "suppose", "propose"},
            0,
            "impose意为实施、强加，符合句意：新法律将实施更严格的环保法规。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "Students must _______ their essays before the deadline.",
            new String[]{"submit", "admit", "permit", "commit"},
            0,
            "submit意为提交，符合句意：学生必须在截止日期前提交论文。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The manager decided to _______ the meeting until next week.",
            new String[]{"postpone", "propose", "suppose", "dispose"},
            0,
            "postpone意为推迟，符合句意：经理决定将会议推迟到下周。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The medicine should help _______ your pain.",
            new String[]{"relieve", "believe", "achieve", "receive"},
            0,
            "relieve意为缓解，符合句意：这种药应该能帮助缓解你的疼痛。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The company aims to _______ its profits by 20% this year.",
            new String[]{"increase", "decrease", "cease", "release"},
            0,
            "increase意为增加，符合句意：公司的目标是今年利润增长20%。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The teacher asked students to _______ their opinions during the discussion.",
            new String[]{"express", "impress", "suppress", "compress"},
            0,
            "express意为表达，符合句意：老师要求学生在讨论中表达自己的观点。",
            "词汇", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The government will _______ new measures to combat climate change.",
            new String[]{"implement", "compliment", "supplement", "experiment"},
            0,
            "implement意为实施，符合句意：政府将实施新措施来应对气候变化。",
            "词汇", "模拟考试"));
            
        // 语法题
        questions.add(createExamQuestion(
            "If I _______ more time, I would have finished the project yesterday.",
            new String[]{"had had", "have had", "had", "have"},
            0,
            "这是虚拟语气的用法，表示与过去事实相反的假设，条件句用过去完成时。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The book _______ by millions of people around the world.",
            new String[]{"has been read", "has read", "was reading", "reads"},
            0,
            "这里需要现在完成时的被动语态，表示书被全世界数百万人阅读。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "By the time you arrive, we _______ waiting for more than an hour.",
            new String[]{"will have been", "will be", "have been", "had been"},
            0,
            "将来完成进行时，表示到将来某时已经进行了一段时间的动作。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "_______ carefully, the essay would have received a higher grade.",
            new String[]{"Had it been written", "If it was written", "Was it written", "It was written"},
            0,
            "虚拟语气的倒装形式，表示与过去事实相反的假设。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The news _______ everyone in the office yesterday.",
            new String[]{"surprised", "was surprised", "surprising", "was surprising"},
            0,
            "surprise作及物动词，主动形式表示'使惊讶'，主语是news。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "Not until he failed the exam _______ how important study was.",
            new String[]{"did he realize", "he realized", "he did realize", "realized he"},
            0,
            "not until位于句首时，主句需要部分倒装。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The problem is _______ difficult that nobody can solve it.",
            new String[]{"so", "such", "very", "too"},
            0,
            "so...that结构，so修饰形容词difficult。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "I would rather you _______ home now.",
            new String[]{"went", "go", "will go", "have gone"},
            0,
            "would rather后接宾语从句时，从句用虚拟语气，表示现在或将来用过去时。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "It is high time that we _______ action to protect the environment.",
            new String[]{"took", "take", "will take", "have taken"},
            0,
            "It is high time that结构中，从句用虚拟语气，动词用过去时。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "_______ the bad weather, the flight was cancelled.",
            new String[]{"Because of", "Because", "Although", "Despite"},
            0,
            "because of后接名词或名词短语，because后接句子。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The house _______ last year is now for sale.",
            new String[]{"built", "building", "to build", "builds"},
            0,
            "过去分词作后置定语，表示被动和完成。",
            "语法", "模拟考试"));
            
        questions.add(createExamQuestion(
            "_______ more attention, the trees could have grown better.",
            new String[]{"Given", "Giving", "To give", "Give"},
            0,
            "过去分词作条件状语，表示被动关系。",
            "语法", "模拟考试"));
            
        // 阅读理解题
        questions.add(createExamQuestion(
            "According to the passage, what is the main advantage of renewable energy?",
            new String[]{"It's environmentally friendly", "It's cheaper", "It's more reliable", "It's easier to install"},
            0,
            "根据文章内容，可再生能源的主要优势是环保。",
            "阅读", "模拟考试"));
            
        questions.add(createExamQuestion(
            "What is the main idea of the passage about environmental protection?",
            new String[]{"Individual actions are important", "Government policies are useless", "Technology solves all problems", "Money is the only solution"},
            0,
            "文章主旨是强调个人行动对环境保护的重要性。",
            "阅读", "模拟考试"));
            
        questions.add(createExamQuestion(
            "According to the passage, what is the best way to learn a foreign language?",
            new String[]{"Practice speaking regularly", "Only read textbooks", "Avoid making mistakes", "Study grammar rules only"},
            0,
            "文章指出学习外语的最佳方法是定期练习口语。",
            "阅读", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The author's attitude toward social media can be described as _______.",
            new String[]{"cautiously optimistic", "completely negative", "extremely positive", "totally indifferent"},
            0,
            "作者对社交媒体持谨慎乐观的态度。",
            "阅读", "模拟考试"));
            
        questions.add(createExamQuestion(
            "Which of the following is NOT mentioned as a benefit of exercise?",
            new String[]{"Improving intelligence instantly", "Reducing stress", "Improving sleep quality", "Strengthening immune system"},
            0,
            "立即提高智力不是文中提到的运动益处。",
            "阅读", "模拟考试"));
            
        questions.add(createExamQuestion(
            "What can be inferred about the future of renewable energy?",
            new String[]{"It will become more affordable", "It will disappear soon", "It's too expensive to develop", "Nobody is interested in it"},
            0,
            "可以推断出可再生能源的未来会变得更加经济实惠。",
            "阅读", "模拟考试"));
            
        questions.add(createExamQuestion(
            "The word 'substantial' in paragraph 3 is closest in meaning to _______.",
            new String[]{"significant", "small", "trivial", "temporary"},
            0,
            "substantial意为'大量的、重要的'，与significant意思最接近。",
            "阅读", "模拟考试"));
        
        repository.insertQuestions(questions, null);
    }
    
    /**
     * 创建考试题目
     */
    private static QuestionEntity createExamQuestion(String questionText, String[] options, 
                                                    int correctAnswer, String explanation,
                                                    String category, String source) {
        QuestionEntity question = new QuestionEntity();
        question.setQuestionText(questionText);
        question.setOptions(options);
        question.setCorrectAnswer(correctAnswer);
        question.setExplanation(explanation);
        question.setCategory(category);
        question.setExamType("四六级");
        question.setDifficulty("medium");
        question.setSource(source);
        return question;
    }
    
    /**
     * 初始化词汇记录
     */
    private static void initializeVocabularyRecords(Application application) {
        VocabularyRecordRepository repository = new VocabularyRecordRepository(application);
        List<VocabularyRecordEntity> records = new ArrayList<>();
        
        // 添加词汇记录（与词汇训练题目对应）
        String[][] vocabularyData = {
            {"abandon", "v. 放弃；抛弃", "[əˈbændən]", "They had to abandon their car in the snow.", "CET4"},
            {"ability", "n. 能力；才能", "[əˈbɪləti]", "She has the ability to speak five languages fluently.", "CET4"},
            {"achieve", "v. 实现；达到", "[əˈtʃiːv]", "He achieved his goal of becoming a doctor.", "CET4"},
            {"advantage", "n. 优势；有利条件", "[ədˈvɑːntɪdʒ]", "One advantage of living in the city is easy access to public transportation.", "CET4"},
            {"analyze", "v. 分析；解析", "[ˈænəlaɪz]", "Scientists analyze the data to find patterns.", "CET4"},
            {"approach", "v./n. 接近；方法", "[əˈprəʊtʃ]", "We need a new approach to solve this problem.", "CET4"},
            {"appropriate", "adj. 适当的；恰当的", "[əˈprəʊpriət]", "Jeans are not appropriate for a formal occasion.", "CET4"},
            {"benefit", "n./v. 利益；好处；有益于", "[ˈbenɪfɪt]", "Regular exercise has many health benefits.", "CET4"},
            {"challenge", "n./v. 挑战", "[ˈtʃælɪndʒ]", "Learning a new language is always a challenge.", "CET4"},
            {"contribute", "v. 贡献；捐助", "[kənˈtrɪbjuːt]", "Everyone can contribute to environmental protection.", "CET4"},
            {"demonstrate", "v. 证明；演示", "[ˈdemənstreɪt]", "The teacher demonstrated how to use the equipment.", "CET6"},
            {"enhance", "v. 提高；增强", "[ɪnˈhɑːns]", "Music can enhance your mood and reduce stress.", "CET6"},
            {"establish", "v. 建立；确立", "[ɪˈstæblɪʃ]", "The company was established in 1990.", "CET4"},
            {"function", "n./v. 功能；运作", "[ˈfʌŋkʃn]", "The heart's function is to pump blood through the body.", "CET4"},
            {"generate", "v. 产生；引起", "[ˈdʒenəreɪt]", "Wind turbines generate clean electricity.", "CET6"}
        };
        
        for (String[] vocab : vocabularyData) {
            VocabularyRecordEntity record = new VocabularyRecordEntity();
            record.setWord(vocab[0]);
            record.setMeaning(vocab[1]);
            record.setPronunciation(vocab[2]);
            record.setExample(vocab[3]);
            record.setLevel(vocab[4]);
            record.setDifficulty("中等");
            records.add(record);
        }
        
        repository.addVocabularyRecords(records);
    }
}

