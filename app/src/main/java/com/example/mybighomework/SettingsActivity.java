package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mybighomework.database.entity.UserEntity;
import com.example.mybighomework.database.entity.UserSettingsEntity;
import com.example.mybighomework.repository.UserRepository;
import com.example.mybighomework.repository.UserSettingsRepository;

public class SettingsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etUserName, etDailyGoal, etNotificationTime;
    private Switch switchNotification, switchSound;
    private Button btnSaveSettings;
    private TextView tvStudyStreak;

    // 账户管理相关
    private TextView tvAccountStatus;
    private Button btnLogin;
    private Button btnEditProfile;
    private Button btnLogout;

    private UserSettingsRepository userSettingsRepository;
    private UserRepository userRepository;
    private UserSettingsEntity currentSettings;
    private UserEntity currentUser;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> loginLauncher;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initActivityResultLaunchers();
        initViews();
        initRepository();
        loadCurrentSettings();
        setupClickListeners();
    }

    private void initActivityResultLaunchers() {
        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            // 登录成功后刷新UI
                            loadCurrentSettings();
                        }
                    }
                }
        );

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            // 编辑成功后刷新UI
                            loadCurrentSettings();
                        }
                    }
                }
        );
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etUserName = findViewById(R.id.et_user_name);
        etDailyGoal = findViewById(R.id.et_daily_goal);
        etNotificationTime = findViewById(R.id.et_notification_time);
        switchNotification = findViewById(R.id.switch_notification);
        switchSound = findViewById(R.id.switch_sound);
        btnSaveSettings = findViewById(R.id.btn_save_settings);
        tvStudyStreak = findViewById(R.id.tv_study_streak);

        // 账户管理相关
        tvAccountStatus = findViewById(R.id.tv_account_status);
        btnLogin = findViewById(R.id.btn_login);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void initRepository() {
        userSettingsRepository = new UserSettingsRepository(this);
        userRepository = new UserRepository(this);
    }

    private void loadCurrentSettings() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                currentSettings = userSettingsRepository.getUserSettings();
                currentUser = userRepository.getLoggedInUser();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentSettings != null) {
                            // 从UserEntity获取用户名，如果没有则使用UserSettings中的默认值
                            String displayUserName = (currentUser != null && currentUser.getUsername() != null) ?
                                    currentUser.getUsername() : currentSettings.getUserName();
                            etUserName.setText(displayUserName);

                            etDailyGoal.setText(String.valueOf(currentSettings.getDailyStudyGoal()));
                            etNotificationTime.setText(currentSettings.getNotificationTime());
                            switchNotification.setChecked(currentSettings.isNotificationEnabled());
                            switchSound.setChecked(currentSettings.isSoundEnabled());
                            tvStudyStreak.setText("连续学习: " + currentSettings.getStudyStreak() + " 天");
                        }

                        updateAccountUI();
                    }
                });
            }
        }).start();
    }

    private void setupClickListeners() {
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 登录按钮
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                loginLauncher.launch(intent);
            }
        });

        // 编辑个人信息按钮
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
                editProfileLauncher.launch(intent);
            }
        });

        // 退出登录按钮
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void saveSettings() {
        String userName = etUserName.getText().toString().trim();
        String dailyGoalStr = etDailyGoal.getText().toString().trim();
        String notificationTime = etNotificationTime.getText().toString().trim();
        boolean notificationEnabled = switchNotification.isChecked();
        boolean soundEnabled = switchSound.isChecked();

        // 验证用户名
        if (userName.isEmpty()) {
            etUserName.setError("用户名不能为空");
            etUserName.requestFocus();
            return;
        }

        if (userName.length() < 2 || userName.length() > 20) {
            etUserName.setError("用户名长度应在2-20个字符之间");
            etUserName.requestFocus();
            return;
        }

        // 验证每日目标
        if (dailyGoalStr.isEmpty()) {
            etDailyGoal.setError("每日目标不能为空");
            etDailyGoal.requestFocus();
            return;
        }

        int dailyGoal;
        try {
            dailyGoal = Integer.parseInt(dailyGoalStr);
            if (dailyGoal < 10 || dailyGoal > 480) {
                etDailyGoal.setError("每日目标应在10-480分钟之间");
                etDailyGoal.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etDailyGoal.setError("请输入有效的数字");
            etDailyGoal.requestFocus();
            return;
        }

        // 验证通知时间格式
        if (notificationEnabled && !notificationTime.isEmpty()) {
            if (!isValidTimeFormat(notificationTime)) {
                etNotificationTime.setError("请输入正确的时间格式 (HH:MM)");
                etNotificationTime.requestFocus();
                return;
            }
        }

        try {
            // 保存用户名到UserSettings
            userSettingsRepository.updateUserName(userName);

            // 如果用户已登录，同时更新UserEntity中的用户名
            if (currentUser != null && currentUser.isLoggedIn()) {
                userRepository.updateUsername(currentUser.getId(), userName);
            }

            // 保存每日目标
            userSettingsRepository.updateDailyStudyGoal(dailyGoal);

            // 保存通知设置
            userSettingsRepository.updateNotificationEnabled(notificationEnabled);
            if (notificationEnabled && !notificationTime.isEmpty()) {
                userSettingsRepository.updateNotificationTime(notificationTime);
            }

            // 保存声音设置
            userSettingsRepository.updateSoundEnabled(soundEnabled);

            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();

            // 返回结果
            setResult(RESULT_OK);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "保存设置时出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidTimeFormat(String time) {
        if (time == null || time.length() != 5) {
            return false;
        }

        String[] parts = time.split(":");
        if (parts.length != 2) {
            return false;
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 更新账户UI
     */
    private void updateAccountUI() {
        if (currentUser != null && currentUser.isLoggedIn()) {
            // 已登录状态
            tvAccountStatus.setText("已登录：" + currentUser.getEmail());
            btnLogin.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            // 未登录状态
            tvAccountStatus.setText("未登录");
            btnLogin.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                userRepository.logout();
                currentUser = null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
                        updateAccountUI();
                    }
                });
            }
        }).start();
    }

}