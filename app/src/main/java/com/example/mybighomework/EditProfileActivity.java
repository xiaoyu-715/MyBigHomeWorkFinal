package com.example.mybighomework;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybighomework.database.entity.UserEntity;
import com.example.mybighomework.repository.UserRepository;
import com.example.mybighomework.repository.UserSettingsRepository;

/**
 * 编辑个人信息Activity
 */
public class EditProfileActivity extends AppCompatActivity {
    
    private ImageView ivBack;
    private TextView tvSave;
    private ImageView ivAvatar;
    private EditText etUsername;
    private TextView tvEmail;
    private EditText etPhone;
    private LinearLayout llChangePassword;
    
    private UserRepository userRepository;
    private UserSettingsRepository userSettingsRepository;
    private UserEntity currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        initViews();
        initData();
        setupClickListeners();
        loadUserData();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvSave = findViewById(R.id.tv_save);
        ivAvatar = findViewById(R.id.iv_avatar);
        etUsername = findViewById(R.id.et_username);
        tvEmail = findViewById(R.id.tv_email);
        etPhone = findViewById(R.id.et_phone);
        llChangePassword = findViewById(R.id.ll_change_password);
    }
    
    private void initData() {
        userRepository = new UserRepository(this);
        userSettingsRepository = new UserSettingsRepository(this);
    }
    
    private void setupClickListeners() {
        // 返回按钮
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // 保存按钮
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
        
        // 修改密码
        llChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });
        
        // 头像点击
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditProfileActivity.this, "头像更换功能开发中", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 加载用户数据
     */
    private void loadUserData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                currentUser = userRepository.getLoggedInUser();
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentUser != null) {
                            etUsername.setText(currentUser.getUsername());
                            tvEmail.setText(currentUser.getEmail());
                            
                            if (currentUser.getPhone() != null) {
                                etPhone.setText(currentUser.getPhone());
                            }
                        } else {
                            Toast.makeText(EditProfileActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        }).start();
    }
    
    /**
     * 保存用户信息
     */
    private void saveUserInfo() {
        if (currentUser == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入用户名");
            etUsername.requestFocus();
            return;
        }
        
        if (username.length() < 2 || username.length() > 20) {
            etUsername.setError("用户名长度应在2-20个字符之间");
            etUsername.requestFocus();
            return;
        }
        
        if (!TextUtils.isEmpty(phone) && !isValidPhone(phone)) {
            etPhone.setError("手机号格式不正确");
            etPhone.requestFocus();
            return;
        }
        
        // 在后台线程中保存
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    userRepository.updateUsername(currentUser.getId(), username);
                    userRepository.updatePhone(currentUser.getId(), phone);

                    // 同步用户名到用户设置
                    userSettingsRepository.updateUserName(username);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditProfileActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditProfileActivity.this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
    
    /**
     * 显示修改密码对话框
     */
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        
        EditText etOldPassword = dialogView.findViewById(R.id.et_old_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);
        
        new AlertDialog.Builder(this)
            .setTitle("修改密码")
            .setView(dialogView)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String oldPassword = etOldPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();
                    
                    changePassword(oldPassword, newPassword, confirmPassword);
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 修改密码
     */
    private void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if (currentUser == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证输入
        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(this, "请输入旧密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (newPassword.length() < 6) {
            Toast.makeText(this, "新密码长度至少6位", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 在后台线程中修改密码
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = userRepository.changePassword(currentUser.getId(), oldPassword, newPassword);
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                Toast.makeText(EditProfileActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "旧密码错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditProfileActivity.this, "密码修改失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
    
    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("^1[3-9]\\d{9}$");
    }
}

