package com.example.mybighomework;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybighomework.repository.UserRepository;

/**
 * 注册Activity
 */
public class RegisterActivity extends AppCompatActivity {
    
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ImageView ivBack;
    
    private UserRepository userRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        initViews();
        initData();
        setupClickListeners();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        ivBack = findViewById(R.id.iv_back);
    }
    
    private void initData() {
        userRepository = new UserRepository(this);
    }
    
    private void setupClickListeners() {
        // 注册按钮
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        
        // 登录按钮
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // 返回按钮
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    /**
     * 注册
     */
    private void register() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
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
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("请输入邮箱");
            etEmail.requestFocus();
            return;
        }
        
        if (!isValidEmail(email)) {
            etEmail.setError("邮箱格式不正确");
            etEmail.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            etPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("密码长度至少6位");
            etPassword.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("请再次输入密码");
            etConfirmPassword.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次输入的密码不一致");
            etConfirmPassword.requestFocus();
            return;
        }
        
        // 在后台线程中执行注册
        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 检查邮箱是否已存在
                    if (userRepository.emailExists(email)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnRegister.setEnabled(true);
                                btnRegister.setText("注册");
                                Toast.makeText(RegisterActivity.this, "该邮箱已被注册", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    
                    // 执行注册
                    long userId = userRepository.register(email, password, username);
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("注册");
                            
                            if (userId > 0) {
                                Toast.makeText(RegisterActivity.this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
                                // 返回到登录页面
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("注册");
                            Toast.makeText(RegisterActivity.this, "注册失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

