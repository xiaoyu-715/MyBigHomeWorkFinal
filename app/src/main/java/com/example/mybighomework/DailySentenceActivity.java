package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.mybighomework.database.entity.DailySentenceEntity;
import com.example.mybighomework.repository.DailySentenceRepository;
import com.example.mybighomework.utils.AudioPlayerManager;
import com.example.mybighomework.utils.ShareUtils;
import com.example.mybighomework.utils.TaskCompletionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailySentenceActivity extends AppCompatActivity {

    private ImageView ivBack, ivShare;
    private TextView tvDate, tvEnglishSentence, tvChineseTranslation, tvViewAllHistory;
    private Button btnPlayAudio, btnFavorite, btnPractice;
    private LinearLayout llVocabularyList;
    private RecyclerView rvHistory;
    
    // 图片相关控件
    private CardView cardImage;
    private ImageView ivDailyImage;
    private ProgressBar pbImageLoading;

    // 数据仓库
    private DailySentenceRepository repository;
    private Handler mainHandler;
    
    // 音频播放管理器
    private AudioPlayerManager audioPlayerManager;
    
    // 当前句子数据
    private DailySentenceEntity currentSentence;
    private List<DailySentenceEntity> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_sentence);

        // 初始化
        repository = new DailySentenceRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());
        historyList = new ArrayList<>();
        audioPlayerManager = new AudioPlayerManager();
        
        initViews();
        setupClickListeners();
        setupAudioPlayer();
        
        // 初始化示例数据
        initializeSampleData();
        
        // 加载数据
        loadTodaySentence();
        loadHistoryData();
        
        // 【任务完成跟踪】打开每日一句页面即完成任务
        TaskCompletionManager.getInstance(this).markDailySentenceCompleted();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivShare = findViewById(R.id.iv_share);
        tvDate = findViewById(R.id.tv_date);
        tvEnglishSentence = findViewById(R.id.tv_english_sentence);
        tvChineseTranslation = findViewById(R.id.tv_chinese_translation);
        tvViewAllHistory = findViewById(R.id.tv_view_all_history);
        btnPlayAudio = findViewById(R.id.btn_play_audio);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnPractice = findViewById(R.id.btn_practice);
        llVocabularyList = findViewById(R.id.ll_vocabulary_list);
        rvHistory = findViewById(R.id.rv_history);
        
        // 图片相关
        cardImage = findViewById(R.id.card_image);
        ivDailyImage = findViewById(R.id.iv_daily_image);
        pbImageLoading = findViewById(R.id.pb_image_loading);
        
        // 设置RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
    }
    
    /**
     * 初始化示例数据
     */
    private void initializeSampleData() {
        repository.initializeSampleData(() -> {
            // 数据初始化完成
        });
    }

    private void setupClickListeners() {
        // 返回按钮
        ivBack.setOnClickListener(v -> finish());

        // 分享按钮
        ivShare.setOnClickListener(v -> shareSentence());

        // 播放音频按钮
        btnPlayAudio.setOnClickListener(v -> playAudio());

        // 收藏按钮
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // 练习按钮
        btnPractice.setOnClickListener(v -> startPractice());

        // 查看全部历史记录
        tvViewAllHistory.setOnClickListener(v -> viewAllHistory());
        
        // 图片点击查看大图
        ivDailyImage.setOnClickListener(v -> showFullImage());
    }

    /**
     * 加载今日句子
     */
    private void loadTodaySentence() {
        // 设置当前日期
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE);
        tvDate.setText(dateFormat.format(calendar.getTime()));

        // 从数据库加载今日句子
        repository.getTodaySentence(sentence -> {
            currentSentence = sentence;
            mainHandler.post(() -> {
                displayCurrentSentence();
            });
        });
    }
    
    /**
     * 显示当前句子
     */
    private void displayCurrentSentence() {
        if (currentSentence == null) return;
        
        // 显示句子内容
        tvEnglishSentence.setText(currentSentence.getEnglishText());
        tvChineseTranslation.setText(currentSentence.getChineseText());
        
        // 更新收藏按钮状态
        updateFavoriteButton(currentSentence.isFavorited());
        
        // 显示词汇解析
        displayVocabulary();
        
        // 加载图片
        loadImage();
    }
    
    /**
     * 加载图片
     */
    private void loadImage() {
        if (currentSentence == null) return;
        
        String imageUrl = currentSentence.getImageUrl();
        
        // 检查图片URL是否存在
        if (imageUrl == null || imageUrl.isEmpty()) {
            cardImage.setVisibility(View.GONE);
            return;
        }
        
        // 显示图片卡片
        cardImage.setVisibility(View.VISIBLE);
        
        // 显示加载进度
        pbImageLoading.setVisibility(View.VISIBLE);
        ivDailyImage.setVisibility(View.INVISIBLE);
        
        // 使用Glide加载图片
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error);
        
        Glide.with(this)
                .load(imageUrl)
                .apply(options)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, 
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        runOnUiThread(() -> {
                            pbImageLoading.setVisibility(View.GONE);
                            ivDailyImage.setVisibility(View.VISIBLE);
                            Toast.makeText(DailySentenceActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                        });
                        return false;
                    }
                    
                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                                                  com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                                  com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        runOnUiThread(() -> {
                            pbImageLoading.setVisibility(View.GONE);
                            ivDailyImage.setVisibility(View.VISIBLE);
                        });
                        return false;
                    }
                })
                .into(ivDailyImage);
    }
    
    /**
     * 加载历史记录
     */
    private void loadHistoryData() {
        repository.getRecentSentences(5, sentences -> {
            historyList.clear();
            historyList.addAll(sentences);
            mainHandler.post(() -> {
                updateHistoryRecyclerView();
            });
        });
    }
    
    /**
     * 更新历史记录RecyclerView
     */
    private void updateHistoryRecyclerView() {
        DailySentenceHistoryAdapter adapter = new DailySentenceHistoryAdapter(convertToLegacyFormat(historyList));
        rvHistory.setAdapter(adapter);
    }
    
    /**
     * 转换为旧格式（用于适配器）
     */
    private List<DailySentence> convertToLegacyFormat(List<DailySentenceEntity> entities) {
        List<DailySentence> result = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);
        
        for (DailySentenceEntity entity : entities) {
            try {
                String formattedDate = entity.getDate();
                try {
                    formattedDate = outputFormat.format(inputFormat.parse(entity.getDate()));
                } catch (Exception e) {
                    // 保持原格式
                }
                
                DailySentence sentence = new DailySentence(
                    entity.getEnglishText(),
                    entity.getChineseText(),
                    entity.getAuthor(),
                    formattedDate
                );
                result.add(sentence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 显示词汇解析
     */
    private void displayVocabulary() {
        llVocabularyList.removeAllViews();
        
        if (currentSentence == null || currentSentence.getVocabularyJson() == null) {
            return;
        }
        
        try {
            JSONArray vocabArray = new JSONArray(currentSentence.getVocabularyJson());
            for (int i = 0; i < vocabArray.length(); i++) {
                JSONObject vocabObj = vocabArray.getJSONObject(i);
                String word = vocabObj.getString("word");
                String meaning = vocabObj.getString("meaning");
                
                View vocabView = getLayoutInflater().inflate(R.layout.item_vocabulary, llVocabularyList, false);
                TextView tvWord = vocabView.findViewById(R.id.tv_word);
                TextView tvMeaning = vocabView.findViewById(R.id.tv_meaning);
                
                tvWord.setText(word);
                tvMeaning.setText(meaning);
                
                llVocabularyList.addView(vocabView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分享句子 - 显示分享选项Dialog
     */
    private void shareSentence() {
        if (currentSentence == null) {
            Toast.makeText(this, "暂无内容可分享", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建分享选项Dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_share_options, null);
        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // 设置对话框背景透明（圆角效果）
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // 分享文字
        dialogView.findViewById(R.id.ll_share_text).setOnClickListener(v -> {
            dialog.dismiss();
            shareText();
        });
        
        // 分享图片（API提供的图片）
        dialogView.findViewById(R.id.ll_share_image).setOnClickListener(v -> {
            dialog.dismiss();
            shareOriginalImage();
        });
        
        // 分享卡片（生成精美卡片）
        dialogView.findViewById(R.id.ll_share_card).setOnClickListener(v -> {
            dialog.dismiss();
            shareCard();
        });
        
        // 取消按钮
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 分享纯文字
     */
    private void shareText() {
        String shareContent = currentSentence.getEnglishText() + "\n\n" +
                             currentSentence.getChineseText() + "\n\n" +
                             "—— " + currentSentence.getAuthor() + "\n\n" +
                             "📚 来自 英语学习助手";
        
        ShareUtils.shareText(this, "每日一句", shareContent);
    }
    
    /**
     * 分享原始图片
     */
    private void shareOriginalImage() {
        String imageUrl = currentSentence.getImageUrl();
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "该句子没有配图", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载提示
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("正在准备图片...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // 下载图片并分享
        new Thread(() -> {
            try {
                // 使用Glide下载图片
                Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get();
                
                // 保存到缓存并获取Uri
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    
                    // 直接分享URL（简化实现）
                    String shareText = currentSentence.getEnglishText() + "\n" +
                                     currentSentence.getChineseText();
                    ShareUtils.shareText(this, "每日一句", shareText + "\n\n[图片]");
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    /**
     * 分享精美卡片
     */
    private void shareCard() {
        // 显示加载提示
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("正在生成分享卡片...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // 在后台生成卡片
        new Thread(() -> {
            try {
                // 获取日期
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);
                String dateStr = dateFormat.format(Calendar.getInstance().getTime());
                
                // 生成分享图片
                android.graphics.Bitmap shareBitmap = ShareUtils.generateShareImage(
                    this,
                    currentSentence.getEnglishText(),
                    currentSentence.getChineseText(),
                    currentSentence.getAuthor(),
                    dateStr
                );
                
                // 保存图片并获取Uri
                String fileName = "daily_sentence_" + System.currentTimeMillis() + ".jpg";
                android.net.Uri imageUri = ShareUtils.saveBitmapToFile(this, shareBitmap, fileName);
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    
                    if (imageUri != null) {
                        ShareUtils.shareImage(this, imageUri, "每日一句");
                    } else {
                        Toast.makeText(this, "生成卡片失败", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "生成卡片失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 设置音频播放器
     */
    private void setupAudioPlayer() {
        audioPlayerManager.setPlaybackStateListener(new AudioPlayerManager.PlaybackStateListener() {
            @Override
            public void onPlaying() {
                runOnUiThread(() -> {
                    btnPlayAudio.setText("⏸ 暂停");
                    btnPlayAudio.setEnabled(true);
                });
            }

            @Override
            public void onPaused() {
                runOnUiThread(() -> {
                    btnPlayAudio.setText("▶ 播放");
                    btnPlayAudio.setEnabled(true);
                });
            }

            @Override
            public void onStopped() {
                runOnUiThread(() -> {
                    btnPlayAudio.setText("▶ 播放");
                    btnPlayAudio.setEnabled(true);
                });
            }

            @Override
            public void onLoading() {
                runOnUiThread(() -> {
                    btnPlayAudio.setText("加载中...");
                    btnPlayAudio.setEnabled(false);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnPlayAudio.setText("▶ 播放");
                    btnPlayAudio.setEnabled(true);
                    Toast.makeText(DailySentenceActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCompletion() {
                runOnUiThread(() -> {
                    btnPlayAudio.setText("▶ 播放");
                    btnPlayAudio.setEnabled(true);
                    Toast.makeText(DailySentenceActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(int currentPosition, int duration) {
                // 可以在这里更新进度条（如果有的话）
                // 暂时不做处理
            }
        });
    }
    
    /**
     * 播放音频
     */
    private void playAudio() {
        if (currentSentence == null) {
            Toast.makeText(this, "暂无句子数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String audioUrl = currentSentence.getAudioUrl();
        
        // 检查音频URL是否存在
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(this, "该句子暂无音频", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 播放或暂停音频
        audioPlayerManager.play(audioUrl);
    }

    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        if (currentSentence == null) return;
        
        boolean newFavoriteStatus = !currentSentence.isFavorited();
        
        repository.toggleFavorite(currentSentence.getId(), newFavoriteStatus, () -> {
            currentSentence.setFavorited(newFavoriteStatus);
            mainHandler.post(() -> {
                updateFavoriteButton(newFavoriteStatus);
                String message = newFavoriteStatus ? "已添加到收藏" : "已取消收藏";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        });
    }
    
    /**
     * 更新收藏按钮状态
     */
    private void updateFavoriteButton(boolean isFavorited) {
        if (isFavorited) {
            btnFavorite.setText("已收藏");
        } else {
            btnFavorite.setText("收藏");
        }
    }

    /**
     * 开始练习
     */
    private void startPractice() {
        if (currentSentence == null) return;
        
        // 标记为已学习
        repository.markAsLearned(currentSentence.getId(), () -> {
            currentSentence.setHasLearned(true);
        });
        
        // 显示练习选项对话框
        showPracticeOptionsDialog();
    }
    
    /**
     * 显示练习选项对话框
     */
    private void showPracticeOptionsDialog() {
        String[] options = {"听写练习", "填空练习", "翻译练习"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择练习方式")
            .setItems(options, (dialog, which) -> {
                String practiceType = options[which];
                Toast.makeText(this, practiceType + "功能即将推出，敬请期待！", Toast.LENGTH_LONG).show();
                // 未来可以在这里实现不同类型的练习
                // 例如: startDictationPractice(), startFillInBlankPractice(), startTranslationPractice()
            })
            .setNegativeButton("取消", null)
            .show();
    }

    /**
     * 查看全部历史记录
     */
    private void viewAllHistory() {
        // 获取所有历史记录并显示详细信息
        repository.getAllSentences(sentences -> {
            if (sentences.isEmpty()) {
                Toast.makeText(this, "暂无历史记录", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 构建历史记录信息
            StringBuilder historyInfo = new StringBuilder();
            historyInfo.append("共有 ").append(sentences.size()).append(" 条学习记录\n\n");
            
            int learnedCount = 0;
            int favoriteCount = 0;
            for (com.example.mybighomework.database.entity.DailySentenceEntity sentence : sentences) {
                if (sentence.isHasLearned()) learnedCount++;
                if (sentence.isFavorited()) favoriteCount++;
            }
            
            historyInfo.append("✅ 已学习: ").append(learnedCount).append(" 条\n");
            historyInfo.append("❤️ 已收藏: ").append(favoriteCount).append(" 条\n\n");
            historyInfo.append("提示：在主页面左右滑动可以浏览历史记录");
            
            // 显示历史记录统计对话框
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("📚 学习历史")
                .setMessage(historyInfo.toString())
                .setPositiveButton("知道了", null)
                .show();
        });
    }
    
    /**
     * 查看大图
     */
    private void showFullImage() {
        if (currentSentence == null || currentSentence.getImageUrl() == null) {
            return;
        }
        
        // 创建全屏Dialog显示大图
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);
        
        ImageView ivFullImage = dialog.findViewById(R.id.iv_full_image);
        ImageView ivClose = dialog.findViewById(R.id.iv_close);
        ProgressBar pbLoading = dialog.findViewById(R.id.pb_loading);
        
        // 显示加载进度
        pbLoading.setVisibility(View.VISIBLE);
        
        // 加载大图
        Glide.with(this)
                .load(currentSentence.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(DailySentenceActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                  com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                  com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        pbLoading.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(ivFullImage);
        
        // 关闭按钮
        ivClose.setOnClickListener(v -> dialog.dismiss());
        
        // 点击图片关闭
        ivFullImage.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 暂停音频播放
        if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
            audioPlayerManager.pause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放音频资源
        if (audioPlayerManager != null) {
            audioPlayerManager.release();
        }
    }
}