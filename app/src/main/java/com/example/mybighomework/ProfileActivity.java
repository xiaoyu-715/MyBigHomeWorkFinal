package com.example.mybighomework;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.entity.UserSettingsEntity;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.repository.ExamRecordRepository;
import com.example.mybighomework.repository.VocabularyRecordRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // 底部导航栏
    private LinearLayout navHome;
    private LinearLayout navReport;
    private LinearLayout navProfile;
    private LinearLayout navMore;
    
    // 用户信息
    private TextView tvUsername;
    private TextView tvUserLevel;
    private TextView tvJoinDate;
    private ImageView ivAvatar;
    private ImageView ivSettings;
    
    // 统计数据
    private TextView tvStudyDays;
    private TextView tvWordsLearned;
    private TextView tvStudyHours;
    
    // 快捷功能
    private LinearLayout llViewReport;
    private LinearLayout llStudySettings;
    
    // Repository实例
    private UserSettingsRepository userSettingsRepository;
    private ExamRecordRepository examRecordRepository;
    private VocabularyRecordRepository vocabularyRecordRepository;
    
    // 图片选择器
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // 初始化Repository
        AppDatabase database = AppDatabase.getInstance(this);
        userSettingsRepository = new UserSettingsRepository(this);
        examRecordRepository = new ExamRecordRepository(database.examDao());
        vocabularyRecordRepository = new VocabularyRecordRepository(database.vocabularyDao());
        
        // 初始化图片选择器
        initImagePicker();
        
        initViews();
        setupClickListeners();
        loadUserData();
    }
    
    /**
     * 初始化图片选择器
     */
    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // 显示选中的图片
                        ivAvatar.setImageURI(selectedImageUri);
                        // 保存头像路径到数据库
                        saveAvatarPath(selectedImageUri.toString());
                        Toast.makeText(this, "头像已更换", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }
    
    private void initViews() {
        // 底部导航栏
        navHome = findViewById(R.id.nav_home);
        navReport = findViewById(R.id.nav_report);
        navProfile = findViewById(R.id.nav_profile);
        navMore = findViewById(R.id.nav_more);
        
        // 用户信息
        tvUsername = findViewById(R.id.tv_username);
        tvUserLevel = findViewById(R.id.tv_user_level);
        tvJoinDate = findViewById(R.id.tv_join_date);
        ivAvatar = findViewById(R.id.iv_avatar);
        ivSettings = findViewById(R.id.iv_settings);
        
        // 统计数据
        tvStudyDays = findViewById(R.id.tv_study_days);
        tvWordsLearned = findViewById(R.id.tv_words_learned);
        tvStudyHours = findViewById(R.id.tv_study_hours);
        
        // 快捷功能
        llViewReport = findViewById(R.id.ll_view_report);
        llStudySettings = findViewById(R.id.ll_study_settings);
    }
    
    private void setupClickListeners() {
        // 首页导航点击事件
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        
        // 学习报告导航点击事件
        navReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ReportActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        // 个人中心导航点击事件（当前页面，无需操作）
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当前就在个人中心页面，无需操作
            }
        });
        
        // 更多功能导航点击事件
        navMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MoreActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        // 查看学习报告点击事件
        llViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ReportActivity.class);
                startActivity(intent);
            }
        });
        
        // 学习设置点击事件
        llStudySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到设置页面
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        
        // 设置按钮点击事件
        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        
        // 头像点击事件
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarChangeDialog();
            }
        });
    }
    
    /**
     * 显示头像更换对话框
     */
    private void showAvatarChangeDialog() {
        String[] options = {"从相册选择", "拍照", "取消"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更换头像")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // 从相册选择
                        openGallery();
                        break;
                    case 1: // 拍照
                        Toast.makeText(this, "拍照功能需要相机权限,暂时从相册选择", Toast.LENGTH_SHORT).show();
                        openGallery();
                        break;
                    case 2: // 取消
                        dialog.dismiss();
                        break;
                }
            })
            .show();
    }
    
    /**
     * 打开相册选择图片
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    
    /**
     * 保存头像路径到数据库
     */
    private void saveAvatarPath(String avatarPath) {
        new Thread(() -> {
            try {
                UserSettingsEntity userSettings = userSettingsRepository.getUserSettings();
                if (userSettings != null) {
                    userSettings.setUserAvatar(avatarPath);
                    userSettingsRepository.updateUserSettings(userSettings);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> 
                    Toast.makeText(this, "保存头像失败", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    
    private void loadUserData() {
        // 在后台线程中加载数据
        new Thread(() -> {
            try {
                // 获取用户设置数据
                UserSettingsEntity userSettings = userSettingsRepository.getUserSettings();
                
                // 获取词汇掌握数量
                int masteredVocabularyCount = vocabularyRecordRepository.getMasteredVocabularyCount();
                
                // 获取总考试次数
                int totalExamCount = examRecordRepository.getTotalExamCount();
                
                // 【修复】在后台线程中获取学习时长数据，避免在主线程访问数据库
                double totalHours = userSettingsRepository.getTotalStudyTimeHours();
                
                // 准备UI数据
                final String username;
                final String userLevel;
                final String joinDate;
                final int studyStreak;
                final String avatarPath;
                
                if (userSettings != null) {
                    // 准备用户名
                    String tempUsername = userSettings.getUserName();
                    username = (tempUsername != null && !tempUsername.isEmpty()) ? tempUsername : "学习者";
                    
                    // 准备用户等级
                    studyStreak = userSettings.getStudyStreak();
                    userLevel = getUserLevel(studyStreak);
                    
                    // 准备加入时间
                    long createdTime = userSettings.getRegistrationDate();
                    if (createdTime > 0) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
                        joinDate = "加入时间：" + sdf.format(new Date(createdTime));
                    } else {
                        joinDate = "加入时间：2024年1月";
                    }
                    
                    // 准备头像路径
                    avatarPath = userSettings.getUserAvatar();
                } else {
                    // 默认值
                    username = "学习者";
                    userLevel = "新手学习者";
                    joinDate = "加入时间：2024年1月";
                    studyStreak = 0;
                    avatarPath = null;
                }
                
                // 在主线程中更新UI
                runOnUiThread(() -> {
                    // 显示用户名
                    tvUsername.setText(username);
                    
                    // 加载头像
                    if (avatarPath != null && !avatarPath.isEmpty()) {
                        try {
                            ivAvatar.setImageURI(Uri.parse(avatarPath));
                        } catch (Exception e) {
                            // 如果加载失败,使用默认头像
                            e.printStackTrace();
                        }
                    }
                    
                    // 显示用户等级
                    tvUserLevel.setText(userLevel);
                    
                    // 显示加入时间
                    tvJoinDate.setText(joinDate);
                    
                    // 显示学习连续天数
                    tvStudyDays.setText(String.valueOf(studyStreak));
                    
                    // 显示学习时长
                    tvStudyHours.setText(String.format(Locale.getDefault(), "%.1f", totalHours));
                    
                    // 显示词汇掌握量
                    tvWordsLearned.setText(String.valueOf(masteredVocabularyCount));
                });
            } catch (Exception e) {
                e.printStackTrace();
                // 如果出错，在主线程中显示默认值
                runOnUiThread(() -> {
                    tvUsername.setText("学习者");
                    tvUserLevel.setText("新手学习者");
                    tvJoinDate.setText("加入时间：2024年1月");
                    tvStudyDays.setText("0");
                    tvWordsLearned.setText("0");
                    tvStudyHours.setText("0.0");
                });
            }
        }).start();
    }
    
    // 根据学习连续天数确定用户等级
    private String getUserLevel(int studyStreak) {
        if (studyStreak >= 100) {
            return "学习大师";
        } else if (studyStreak >= 50) {
            return "高级学习者";
        } else if (studyStreak >= 20) {
            return "中级学习者";
        } else if (studyStreak >= 7) {
            return "初级学习者";
        } else {
            return "新手学习者";
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他Activity返回时，重新加载用户数据
        loadUserData();
    }
}