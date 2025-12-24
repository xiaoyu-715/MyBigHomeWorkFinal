package com.example.mybighomework;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.VocabularyRecordEntity;
import com.example.mybighomework.database.entity.StudyRecordEntity;
import com.example.mybighomework.database.entity.WrongQuestionEntity;
import com.example.mybighomework.repository.VocabularyRecordRepository;
import com.example.mybighomework.repository.StudyRecordRepository;
import com.example.mybighomework.repository.WrongQuestionRepository;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.utils.ModuleStatisticsManager;
import com.example.mybighomework.utils.TaskCompletionManager;
import com.example.mybighomework.utils.TaskCompletionHelper;
import com.example.mybighomework.utils.TaskProgressTracker;
import java.util.Date;

public class VocabularyActivity extends AppCompatActivity {

    // UI组件
    private ImageView btnBack;
    private TextView tvProgress, tvScore, tvWord, tvPhonetic, tvMeaning, tvResult;
    private ImageView btnPlay, ivResult;
    private ProgressBar progressBar;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private Button btnNext, btnRestart, btnFinish;
    private LinearLayout layoutOptions, layoutResult;
    private LinearLayout navHome, navReport, navProfile, navMore;

    // 游戏数据
    private List<VocabularyItem> vocabularyList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalQuestions = 10;
    private boolean isAnswered = false;
    
    // 数据库相关
    private VocabularyRecordRepository vocabularyRecordRepository;
    private StudyRecordRepository studyRecordRepository;
    private WrongQuestionRepository wrongQuestionRepository;
    private UserSettingsRepository userSettingsRepository;
    private ExecutorService executorService;
    
    // 训练统计数据
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private long trainingStartTime;
    
    // 音频播放相关
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    // 词汇数据类
    private static class VocabularyItem {
        String word;
        String phonetic;
        String meaning;
        String[] options;
        int correctAnswer;

        VocabularyItem(String word, String phonetic, String meaning, String[] options, int correctAnswer) {
            this.word = word;
            this.phonetic = phonetic;
            this.meaning = meaning;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        initDatabase();
        initViews();
        initVocabularyData();
        setupClickListeners();
        showCurrentQuestion();
        
        // 记录训练开始时间
        trainingStartTime = System.currentTimeMillis();
    }
    
    private void initDatabase() {
        AppDatabase database = AppDatabase.getInstance(this);
        vocabularyRecordRepository = new VocabularyRecordRepository(database.vocabularyDao());
        studyRecordRepository = new StudyRecordRepository(database.studyRecordDao());
        wrongQuestionRepository = new WrongQuestionRepository(database.wrongQuestionDao());
        userSettingsRepository = new UserSettingsRepository(this);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 注意：任务完成现在由TaskProgressTracker在每次答题时自动追踪
        // 不再需要在onDestroy中手动标记任务完成
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // 释放MediaPlayer资源
        releaseMediaPlayer();
    }
    
    /**
     * 处理返回操作 - 保存已训练的数据
     */
    private void handleBackPressed() {
        if (currentQuestionIndex > 0) {
            // 用户至少做了一道题，保存训练记录
            saveTrainingRecord();
        }
        finish();
    }

    private void initViews() {
        // 顶部控件
        btnBack = findViewById(R.id.btn_back);
        tvProgress = findViewById(R.id.tv_progress);
        tvScore = findViewById(R.id.tv_score);
        progressBar = findViewById(R.id.progress_bar);

        // 单词显示区域
        tvWord = findViewById(R.id.tv_word);
        tvPhonetic = findViewById(R.id.tv_phonetic);
        tvMeaning = findViewById(R.id.tv_meaning);
        btnPlay = findViewById(R.id.btn_play);

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

        // 底部导航
        navHome = findViewById(R.id.nav_home);
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);
    }
    

    private void initVocabularyData() {
        vocabularyList = new ArrayList<>();
        
        // 添加四六级词汇数据
        vocabularyList.add(new VocabularyItem("abandon", "[əˈbændən]", "v. 放弃；抛弃", 
            new String[]{"放弃；抛弃", "获得；得到", "继续；坚持", "开始；启动"}, 0));
        vocabularyList.add(new VocabularyItem("ability", "[əˈbɪləti]", "n. 能力；才能", 
            new String[]{"困难；障碍", "能力；才能", "疾病；不适", "敌意；反对"}, 1));
        vocabularyList.add(new VocabularyItem("achieve", "[əˈtʃiːv]", "v. 实现；达到", 
            new String[]{"失败；落败", "放弃；舍弃", "实现；达到", "忽视；忽略"}, 2));
        vocabularyList.add(new VocabularyItem("advantage", "[ədˈvɑːntɪdʒ]", "n. 优势；有利条件", 
            new String[]{"缺点；劣势", "困难；障碍", "危险；风险", "优势；有利条件"}, 3));
        vocabularyList.add(new VocabularyItem("analyze", "[ˈænəlaɪz]", "v. 分析；解析", 
            new String[]{"分析；解析", "综合；合成", "忽略；无视", "混淆；搞乱"}, 0));
        vocabularyList.add(new VocabularyItem("approach", "[əˈprəʊtʃ]", "v./n. 接近；方法", 
            new String[]{"离开；远离", "接近；方法", "拒绝；否认", "破坏；损坏"}, 1));
        vocabularyList.add(new VocabularyItem("appropriate", "[əˈprəʊpriət]", "adj. 适当的；恰当的", 
            new String[]{"错误的；不对的", "危险的；冒险的", "适当的；恰当的", "困难的；艰难的"}, 2));
        vocabularyList.add(new VocabularyItem("benefit", "[ˈbenɪfɪt]", "n./v. 利益；好处；有益于", 
            new String[]{"损失；伤害", "困难；障碍", "危险；风险", "利益；好处；有益于"}, 3));
        vocabularyList.add(new VocabularyItem("challenge", "[ˈtʃælɪndʒ]", "n./v. 挑战", 
            new String[]{"挑战", "帮助；援助", "简化；使简单", "避免；回避"}, 0));
        vocabularyList.add(new VocabularyItem("contribute", "[kənˈtrɪbjuːt]", "v. 贡献；捐助", 
            new String[]{"破坏；损坏", "贡献；捐助", "拒绝；否认", "减少；降低"}, 1));
        vocabularyList.add(new VocabularyItem("demonstrate", "[ˈdemənstreɪt]", "v. 证明；演示", 
            new String[]{"隐藏；掩盖", "混淆；搞乱", "证明；演示", "拒绝；否认"}, 2));
        vocabularyList.add(new VocabularyItem("enhance", "[ɪnˈhɑːns]", "v. 提高；增强", 
            new String[]{"减少；降低", "忽略；忽视", "破坏；损害", "提高；增强"}, 3));
        vocabularyList.add(new VocabularyItem("establish", "[ɪˈstæblɪʃ]", "v. 建立；确立", 
            new String[]{"建立；确立", "破坏；摧毁", "放弃；舍弃", "拒绝；否认"}, 0));
        vocabularyList.add(new VocabularyItem("function", "[ˈfʌŋkʃn]", "n./v. 功能；运作", 
            new String[]{"失败；故障", "功能；运作", "危险；风险", "困难；障碍"}, 1));
        vocabularyList.add(new VocabularyItem("generate", "[ˈdʒenəreɪt]", "v. 产生；引起", 
            new String[]{"破坏；损坏", "消除；清除", "产生；引起", "减少；降低"}, 2));

        // 打乱顺序
        Collections.shuffle(vocabularyList);
        totalQuestions = Math.min(vocabularyList.size(), 10);
    }

    private void setupClickListeners() {
        // 返回按钮 - 保存训练数据后退出
        btnBack.setOnClickListener(v -> handleBackPressed());

        // 播放按钮
        btnPlay.setOnClickListener(v -> playWordPronunciation());

        // 选项按钮
        btnOptionA.setOnClickListener(v -> selectOption(0));
        btnOptionB.setOnClickListener(v -> selectOption(1));
        btnOptionC.setOnClickListener(v -> selectOption(2));
        btnOptionD.setOnClickListener(v -> selectOption(3));

        // 下一题按钮
        btnNext.setOnClickListener(v -> nextQuestion());

        // 重新开始按钮
        btnRestart.setOnClickListener(v -> restartGame());

        // 完成训练按钮
        btnFinish.setOnClickListener(v -> finishTraining());

        // 底部导航点击事件
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(VocabularyActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        navReport.setOnClickListener(v -> {
            Intent intent = new Intent(VocabularyActivity.this, ReportActivity.class);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(VocabularyActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        navMore.setOnClickListener(v -> {
            Intent intent = new Intent(VocabularyActivity.this, MoreActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * 播放单词发音 - 使用Google免费TTS
     */
    private void playWordPronunciation() {
        if (isPlaying) {
            Toast.makeText(this, "正在播放中，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String word = tvWord.getText().toString().trim();
        if (word.isEmpty()) {
            Toast.makeText(this, "没有可播放的单词", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // 释放之前的MediaPlayer
            releaseMediaPlayer();
            
            // 创建新的MediaPlayer
            mediaPlayer = new MediaPlayer();
            
            // 有道词典发音API（国内可用，免费）
            String encodedWord = URLEncoder.encode(word, "UTF-8");
            // type: 0=英式发音, 1=美式发音, 2=英式发音
            String url = "https://dict.youdao.com/dictvoice?audio=" + encodedWord + "&type=1";
            
            mediaPlayer.setDataSource(url);
            
            // 设置准备监听
            mediaPlayer.setOnPreparedListener(mp -> {
                isPlaying = true;
                btnPlay.setEnabled(false);
                btnPlay.setAlpha(0.5f);
                mp.start();
            });
            
            // 设置播放完成监听
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlay.setEnabled(true);
                btnPlay.setAlpha(1.0f);
                releaseMediaPlayer();
            });
            
            // 设置错误监听
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPlaying = false;
                btnPlay.setEnabled(true);
                btnPlay.setAlpha(1.0f);
                Toast.makeText(VocabularyActivity.this, "播放失败，请检查网络", Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                return true;
            });
            
            // 异步准备
            mediaPlayer.prepareAsync();
            
        } catch (Exception e) {
            isPlaying = false;
            btnPlay.setEnabled(true);
            btnPlay.setAlpha(1.0f);
            Toast.makeText(this, "播放出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    /**
     * 释放MediaPlayer资源
     */
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mediaPlayer = null;
            }
        }
    }
    
    private void showCurrentQuestion() {
        if (currentQuestionIndex >= totalQuestions) {
            showFinalResult();
            return;
        }

        VocabularyItem currentItem = vocabularyList.get(currentQuestionIndex);
        
        // 更新进度
        tvProgress.setText((currentQuestionIndex + 1) + "/" + totalQuestions);
        progressBar.setProgress((currentQuestionIndex + 1) * 100 / totalQuestions);
        tvScore.setText("得分: " + score);

        // 显示单词信息
        tvWord.setText(currentItem.word);
        tvPhonetic.setText(currentItem.phonetic);
        tvMeaning.setVisibility(View.GONE);

        // 设置选项
        btnOptionA.setText("A. " + currentItem.options[0]);
        btnOptionB.setText("B. " + currentItem.options[1]);
        btnOptionC.setText("C. " + currentItem.options[2]);
        btnOptionD.setText("D. " + currentItem.options[3]);

        // 重置按钮状态
        resetOptionButtons();

        // 显示选项，隐藏结果
        layoutOptions.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        
        isAnswered = false;
    }

    private void selectOption(int selectedOption) {
        if (isAnswered) return;

        isAnswered = true;
        VocabularyItem currentItem = vocabularyList.get(currentQuestionIndex);
        
        // 显示正确答案
        tvMeaning.setText(currentItem.meaning);
        tvMeaning.setVisibility(View.VISIBLE);

        // 检查答案
        boolean isCorrect = selectedOption == currentItem.correctAnswer;
        
        if (isCorrect) {
            score += 10;
            correctAnswers++;
            ivResult.setImageResource(R.drawable.ic_check);
            tvResult.setText("正确！");
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            
            // 答对题目时增加词汇训练模块统计数
            ModuleStatisticsManager.getInstance(this).incrementVocabularyCorrectCount();
        } else {
            wrongAnswers++;
            ivResult.setImageResource(R.drawable.ic_close);
            tvResult.setText("错误！正确答案是: " + currentItem.options[currentItem.correctAnswer]);
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            
            // 记录错题
            saveWrongQuestion(currentItem, selectedOption);
        }
        
        // 保存词汇学习记录
        saveVocabularyRecord(currentItem, isCorrect);

        // 高亮正确答案和用户选择
        highlightAnswers(selectedOption, currentItem.correctAnswer);

        // 显示结果区域
        layoutResult.setVisibility(View.VISIBLE);
        
        // 更新得分显示
        tvScore.setText("得分: " + score);
        
        // 【智能任务完成跟踪】每答一题，累计计数，达到目标自动完成任务
        TaskProgressTracker.getInstance(this).recordProgress("vocabulary_training", 1);
    }

    private void saveWrongQuestion(VocabularyItem item, int userAnswerIndex) {
        WrongQuestionEntity wrongQuestion = new WrongQuestionEntity();
        wrongQuestion.setQuestionText(item.word);
        wrongQuestion.setOptions(item.options);
        wrongQuestion.setCorrectAnswerIndex(item.correctAnswer);
        wrongQuestion.setUserAnswerIndex(userAnswerIndex);
        wrongQuestion.setExplanation("单词意思：" + item.meaning);
        wrongQuestion.setCategory("词汇训练");
        wrongQuestion.setSource("VocabularyActivity");
        wrongQuestion.setWrongTime(new Date());
        wrongQuestionRepository.addWrongQuestion(wrongQuestion);
    }

    private void highlightAnswers(int selectedOption, int correctOption) {
        Button[] buttons = {btnOptionA, btnOptionB, btnOptionC, btnOptionD};
        
        for (int i = 0; i < buttons.length; i++) {
            if (i == correctOption) {
                // 正确答案显示绿色
                buttons[i].setBackgroundResource(R.drawable.btn_correct_background);
            } else if (i == selectedOption) {
                // 错误选择显示红色
                buttons[i].setBackgroundResource(R.drawable.btn_error_background);
            } else {
                buttons[i].setBackgroundResource(R.drawable.btn_default_background);
            }
            buttons[i].setEnabled(false);
        }
    }

    private void resetOptionButtons() {
        Button[] buttons = {btnOptionA, btnOptionB, btnOptionC, btnOptionD};
        
        for (Button button : buttons) {
            button.setBackgroundResource(R.drawable.btn_default_background);
            button.setEnabled(true);
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showCurrentQuestion();
    }

    private void restartGame() {
        currentQuestionIndex = 0;
        score = 0;
        isAnswered = false;
        correctAnswers = 0;
        wrongAnswers = 0;
        trainingStartTime = System.currentTimeMillis();
        Collections.shuffle(vocabularyList);
        showCurrentQuestion();
        Toast.makeText(this, "重新开始训练", Toast.LENGTH_SHORT).show();
    }

    private void finishTraining() {
        showFinalResult();
    }

    private void showFinalResult() {
        String message = "训练完成！\n总得分: " + score + "/" + (totalQuestions * 10) + "\n";
        
        if (score >= totalQuestions * 8) {
            message += "优秀！继续保持！";
        } else if (score >= totalQuestions * 6) {
            message += "良好！还有提升空间！";
        } else {
            message += "需要加强练习！";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // 保存训练总结数据（等待完成后再关闭页面）
        saveTrainingRecordAndFinish();
    }
    
    private void saveVocabularyRecord(VocabularyItem item, boolean isCorrect) {
        executorService.execute(() -> {
            try {
                // 查找是否已存在该词汇记录
                VocabularyRecordEntity existingRecord = vocabularyRecordRepository.getVocabularyByWord(item.word);
                
                if (existingRecord != null) {
                    // 更新现有记录
                    if (isCorrect) {
                        existingRecord.setCorrectCount(existingRecord.getCorrectCount() + 1);
                    } else {
                        existingRecord.setWrongCount(existingRecord.getWrongCount() + 1);
                    }
                    existingRecord.setLastStudyTime(System.currentTimeMillis());
                    
                    // 判断是否掌握（正确率超过80%）
                    int totalAttempts = existingRecord.getCorrectCount() + existingRecord.getWrongCount();
                    if (totalAttempts >= 3) {
                        double accuracy = (double) existingRecord.getCorrectCount() / totalAttempts;
                        existingRecord.setMastered(accuracy >= 0.8);
                    }
                    
                    vocabularyRecordRepository.updateVocabularyRecord(existingRecord);
                    android.util.Log.d("VocabularyActivity", "更新词汇记录: " + item.word + ", 正确:" + existingRecord.getCorrectCount() + ", 错误:" + existingRecord.getWrongCount());
                } else {
                    // 创建新记录
                    VocabularyRecordEntity newRecord = new VocabularyRecordEntity();
                    newRecord.setWord(item.word);
                    newRecord.setPronunciation(item.phonetic);
                    newRecord.setMeaning(item.meaning);
                    newRecord.setCorrectCount(isCorrect ? 1 : 0);
                    newRecord.setWrongCount(isCorrect ? 0 : 1);
                    newRecord.setMastered(false);
                    newRecord.setCreatedTime(System.currentTimeMillis());
                    newRecord.setLastStudyTime(System.currentTimeMillis());
                    
                    vocabularyRecordRepository.addVocabularyRecord(newRecord);
                    android.util.Log.d("VocabularyActivity", "新增词汇记录: " + item.word);
                }
            } catch (Exception e) {
                android.util.Log.e("VocabularyActivity", "保存词汇记录失败: " + item.word, e);
            }
        });
    }
    
    private void saveTrainingRecord() {
        executorService.execute(() -> {
            try {
                long trainingEndTime = System.currentTimeMillis();
                long responseTime = trainingEndTime - trainingStartTime;
                
                // 创建学习记录
                StudyRecordEntity studyRecord = new StudyRecordEntity();
                studyRecord.setType("词汇训练");
                studyRecord.setQuestionId(null); // 词汇训练没有特定题目ID
                studyRecord.setVocabularyId(null); // 多个词汇的综合训练
                studyRecord.setCorrect(correctAnswers > wrongAnswers);
                studyRecord.setResponseTime(responseTime);
                studyRecord.setScore(score);
                studyRecord.setCreatedTime(trainingStartTime);
                studyRecord.setStudyDate(new java.util.Date()); // 显式设置学习日期
                studyRecord.setNotes("词汇训练 - 正确:" + correctAnswers + " 错误:" + wrongAnswers);
                
                studyRecordRepository.addStudyRecord(studyRecord);
                
                // 【统一时间记录】记录词汇训练时长到用户设置
                userSettingsRepository.recordStudyTime(responseTime, "vocabulary");
                
                runOnUiThread(() -> {
                    Toast.makeText(VocabularyActivity.this, "训练数据已保存", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(VocabularyActivity.this, "保存数据时出错", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * 保存训练记录并在完成后关闭页面
     */
    private void saveTrainingRecordAndFinish() {
        executorService.execute(() -> {
            try {
                long trainingEndTime = System.currentTimeMillis();
                long responseTime = trainingEndTime - trainingStartTime;
                
                // 创建学习记录
                StudyRecordEntity studyRecord = new StudyRecordEntity();
                studyRecord.setType("词汇训练");
                studyRecord.setQuestionId(null); // 词汇训练没有特定题目ID
                studyRecord.setVocabularyId(null); // 多个词汇的综合训练
                studyRecord.setCorrect(correctAnswers > wrongAnswers);
                studyRecord.setResponseTime(responseTime);
                studyRecord.setScore(score);
                studyRecord.setCreatedTime(trainingStartTime);
                studyRecord.setStudyDate(new java.util.Date()); // 显式设置学习日期
                studyRecord.setNotes("词汇训练 - 正确:" + correctAnswers + " 错误:" + wrongAnswers);
                
                studyRecordRepository.addStudyRecord(studyRecord);
                
                // 【统一时间记录】记录词汇训练时长到用户设置
                userSettingsRepository.recordStudyTime(responseTime, "vocabulary");
                
                runOnUiThread(() -> {
                    Toast.makeText(VocabularyActivity.this, "训练数据已保存", Toast.LENGTH_SHORT).show();
                    // 数据保存完成后再关闭页面
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(VocabularyActivity.this, "保存数据时出错", Toast.LENGTH_SHORT).show();
                    // 即使出错也关闭页面
                    finish();
                });
            }
        });
    }
}