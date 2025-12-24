package com.example.mybighomework;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.DictionaryWordEntity;
import com.example.mybighomework.database.entity.WordLearningProgressEntity;
import com.example.mybighomework.database.repository.BookRepository;
import com.example.mybighomework.database.repository.LearningProgressRepository;
import com.example.mybighomework.utils.TaskProgressTracker;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 词书学习Activity
 * 支持从词书数据库加载单词进行训练
 */
public class BookLearningActivity extends AppCompatActivity {

    // UI组件
    private ImageView btnBack, btnPlay;
    private TextView tvTitle, tvProgress, tvScore;
    private ProgressBar progressBar;
    private TextView tvWord, tvPhonetic, tvMeaning;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private LinearLayout layoutOptions, layoutResult;
    private ImageView ivResult;
    private TextView tvResult;
    private Button btnNext, btnRestart, btnFinish;
    private LinearLayout layoutLoading;

    // 数据
    private String bookId;
    private String bookName;
    private String mode; // "learn" 或 "review"
    private List<DictionaryWordEntity> wordList = new ArrayList<>();
    private List<QuestionItem> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private int correctCount = 0;
    private int wrongCount = 0;
    private int totalQuestions = 20;
    private boolean isAnswered = false;
    private long startTime;

    // 数据库
    private BookRepository bookRepository;
    private LearningProgressRepository progressRepository;
    private ExecutorService executor;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    // 题目数据类
    private static class QuestionItem {
        DictionaryWordEntity word;
        String[] options;
        int correctAnswer;

        QuestionItem(DictionaryWordEntity word, String[] options, int correctAnswer) {
            this.word = word;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_learning);

        // 获取参数
        bookId = getIntent().getStringExtra("book_id");
        bookName = getIntent().getStringExtra("book_name");
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "learn";

        if (bookId == null) {
            Toast.makeText(this, "词书ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initData();
        setupClickListeners();
        loadWords();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnPlay = findViewById(R.id.btn_play);
        tvTitle = findViewById(R.id.tv_title);
        tvProgress = findViewById(R.id.tv_progress);
        tvScore = findViewById(R.id.tv_score);
        progressBar = findViewById(R.id.progress_bar);
        tvWord = findViewById(R.id.tv_word);
        tvPhonetic = findViewById(R.id.tv_phonetic);
        tvMeaning = findViewById(R.id.tv_meaning);
        btnOptionA = findViewById(R.id.btn_option_a);
        btnOptionB = findViewById(R.id.btn_option_b);
        btnOptionC = findViewById(R.id.btn_option_c);
        btnOptionD = findViewById(R.id.btn_option_d);
        layoutOptions = findViewById(R.id.layout_options);
        layoutResult = findViewById(R.id.layout_result);
        ivResult = findViewById(R.id.iv_result);
        tvResult = findViewById(R.id.tv_result);
        btnNext = findViewById(R.id.btn_next);
        btnRestart = findViewById(R.id.btn_restart);
        btnFinish = findViewById(R.id.btn_finish);
        layoutLoading = findViewById(R.id.layout_loading);

        tvTitle.setText(bookName != null ? bookName : "词书学习");
    }

    private void initData() {
        AppDatabase database = AppDatabase.getInstance(this);
        bookRepository = new BookRepository(database);
        progressRepository = new LearningProgressRepository(database);
        executor = Executors.newSingleThreadExecutor();
        startTime = System.currentTimeMillis();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> showExitConfirmDialog());
        btnPlay.setOnClickListener(v -> playPronunciation());
        btnOptionA.setOnClickListener(v -> selectOption(0));
        btnOptionB.setOnClickListener(v -> selectOption(1));
        btnOptionC.setOnClickListener(v -> selectOption(2));
        btnOptionD.setOnClickListener(v -> selectOption(3));
        btnNext.setOnClickListener(v -> nextQuestion());
        btnRestart.setOnClickListener(v -> restartLearning());
        btnFinish.setOnClickListener(v -> finishLearning());
    }

    private void loadWords() {
        showLoading();
        executor.execute(() -> {
            try {
                List<DictionaryWordEntity> words = bookRepository.getWordsForBookSync(bookId);
                
                if (words == null || words.isEmpty()) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(this, "该词书暂无单词数据", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                wordList = words;
                generateQuestions();

                runOnUiThread(() -> {
                    hideLoading();
                    showCurrentQuestion();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "加载单词失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void generateQuestions() {
        questionList.clear();
        
        // 打乱单词顺序
        List<DictionaryWordEntity> shuffled = new ArrayList<>(wordList);
        Collections.shuffle(shuffled);
        
        // 取前N个单词生成题目
        int count = Math.min(shuffled.size(), totalQuestions);
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            DictionaryWordEntity word = shuffled.get(i);
            String[] options = new String[4];
            int correctAnswer = random.nextInt(4);

            // 设置正确答案
            options[correctAnswer] = word.getTranslation();
            if (options[correctAnswer] == null || options[correctAnswer].isEmpty()) {
                options[correctAnswer] = "暂无释义";
            }

            // 生成干扰选项
            List<String> usedTranslations = new ArrayList<>();
            usedTranslations.add(options[correctAnswer]);

            for (int j = 0; j < 4; j++) {
                if (j == correctAnswer) continue;

                // 随机选择其他单词的翻译作为干扰项
                String distractor = null;
                int attempts = 0;
                while (distractor == null && attempts < 50) {
                    int randomIndex = random.nextInt(wordList.size());
                    String trans = wordList.get(randomIndex).getTranslation();
                    if (trans != null && !trans.isEmpty() && !usedTranslations.contains(trans)) {
                        distractor = trans;
                        usedTranslations.add(trans);
                    }
                    attempts++;
                }

                if (distractor == null) {
                    distractor = "选项" + (j + 1);
                }
                options[j] = distractor;
            }

            questionList.add(new QuestionItem(word, options, correctAnswer));
        }

        totalQuestions = questionList.size();
    }

    private void showCurrentQuestion() {
        if (currentIndex >= questionList.size()) {
            showFinalResult();
            return;
        }

        QuestionItem question = questionList.get(currentIndex);
        DictionaryWordEntity word = question.word;

        // 更新进度
        tvProgress.setText((currentIndex + 1) + "/" + totalQuestions);
        progressBar.setProgress((currentIndex + 1) * 100 / totalQuestions);
        tvScore.setText("得分: " + score);

        // 显示单词
        tvWord.setText(word.getWord());
        String phonetic = word.getPhoneticUs();
        if (phonetic == null || phonetic.isEmpty()) {
            phonetic = word.getPhoneticUk();
        }
        tvPhonetic.setText(phonetic != null ? phonetic : "");
        tvMeaning.setVisibility(View.GONE);

        // 设置选项
        btnOptionA.setText("A. " + question.options[0]);
        btnOptionB.setText("B. " + question.options[1]);
        btnOptionC.setText("C. " + question.options[2]);
        btnOptionD.setText("D. " + question.options[3]);

        // 重置状态
        resetOptionButtons();
        layoutOptions.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        isAnswered = false;
    }


    private void selectOption(int selected) {
        if (isAnswered) return;
        isAnswered = true;

        QuestionItem question = questionList.get(currentIndex);
        boolean isCorrect = selected == question.correctAnswer;

        // 显示正确答案
        tvMeaning.setText(question.word.getTranslation());
        tvMeaning.setVisibility(View.VISIBLE);

        if (isCorrect) {
            score += 10;
            correctCount++;
            ivResult.setImageResource(R.drawable.ic_check);
            tvResult.setText("正确！");
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            wrongCount++;
            ivResult.setImageResource(R.drawable.ic_close);
            tvResult.setText("错误！正确答案是: " + question.options[question.correctAnswer]);
            tvResult.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        // 高亮答案
        highlightAnswers(selected, question.correctAnswer);
        layoutResult.setVisibility(View.VISIBLE);
        tvScore.setText("得分: " + score);

        // 保存学习进度
        saveProgress(question.word, isCorrect);

        // 任务进度追踪
        TaskProgressTracker.getInstance(this).recordProgress("vocabulary_training", 1);
    }

    private void highlightAnswers(int selected, int correct) {
        Button[] buttons = {btnOptionA, btnOptionB, btnOptionC, btnOptionD};
        for (int i = 0; i < buttons.length; i++) {
            if (i == correct) {
                buttons[i].setBackgroundResource(R.drawable.btn_correct_background);
            } else if (i == selected) {
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
        currentIndex++;
        showCurrentQuestion();
    }

    private void restartLearning() {
        currentIndex = 0;
        score = 0;
        correctCount = 0;
        wrongCount = 0;
        startTime = System.currentTimeMillis();
        generateQuestions();
        showCurrentQuestion();
        Toast.makeText(this, "重新开始学习", Toast.LENGTH_SHORT).show();
    }

    private void finishLearning() {
        showFinalResult();
    }

    private void showFinalResult() {
        String message = "学习完成！\n" +
                "总得分: " + score + "/" + (totalQuestions * 10) + "\n" +
                "正确: " + correctCount + " 错误: " + wrongCount + "\n";

        int accuracy = totalQuestions > 0 ? (correctCount * 100 / totalQuestions) : 0;
        if (accuracy >= 80) {
            message += "优秀！继续保持！";
        } else if (accuracy >= 60) {
            message += "良好！还有提升空间！";
        } else {
            message += "需要加强练习！";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void saveProgress(DictionaryWordEntity word, boolean isCorrect) {
        executor.execute(() -> {
            try {
                String userId = "default_user";
                progressRepository.updateProgress(userId, word.getId(), bookId, isCorrect);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void playPronunciation() {
        if (isPlaying) {
            Toast.makeText(this, "正在播放中", Toast.LENGTH_SHORT).show();
            return;
        }

        String word = tvWord.getText().toString().trim();
        if (word.isEmpty()) return;

        try {
            releaseMediaPlayer();
            mediaPlayer = new MediaPlayer();
            String encodedWord = URLEncoder.encode(word, "UTF-8");
            String url = "https://dict.youdao.com/dictvoice?audio=" + encodedWord + "&type=1";
            
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                isPlaying = true;
                btnPlay.setEnabled(false);
                btnPlay.setAlpha(0.5f);
                mp.start();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlay.setEnabled(true);
                btnPlay.setAlpha(1.0f);
                releaseMediaPlayer();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPlaying = false;
                btnPlay.setEnabled(true);
                btnPlay.setAlpha(1.0f);
                Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(this, "播放出错", Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }

    private void showExitConfirmDialog() {
        if (currentIndex > 0) {
            new AlertDialog.Builder(this)
                .setTitle("确认退出")
                .setMessage("学习进度将会保存，确定要退出吗？")
                .setPositiveButton("退出", (d, w) -> finish())
                .setNegativeButton("继续学习", null)
                .show();
        } else {
            finish();
        }
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        layoutLoading.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        showExitConfirmDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
