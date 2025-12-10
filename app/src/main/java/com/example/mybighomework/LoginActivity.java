package com.example.mybighomework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mybighomework.database.entity.UserEntity;
import com.example.mybighomework.repository.UserRepository;

/**
 * 登录Activity
 */
public class LoginActivity extends AppCompatActivity {
    
    private EditText etEmail;
    private EditText etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvSkip;
    private ImageView ivBack;
    
    private UserRepository userRepository;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        initData();
        setupClickListeners();
        loadRememberedAccount();
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvSkip = findViewById(R.id.tv_skip);
        ivBack = findViewById(R.id.iv_back);
    }
    
    private void initData() {
        userRepository = new UserRepository(this);
        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
    }
    
    private void setupClickListeners() {
        // 登录按钮
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        
        // 注册按钮
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        
        // 跳过登录
        tvSkip.setOnClickListener(new View.OnClickListener() {
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
     * 加载记住的账号
     */
    private void loadRememberedAccount() {
        boolean remember = sharedPreferences.getBoolean("remember", false);
        if (remember) {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");
            etEmail.setText(email);
            etPassword.setText(password);
            cbRemember.setChecked(true);
        }
    }
    
    /**
     * 保存账号密码
     */
    private void saveAccount(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (cbRemember.isChecked()) {
            editor.putBoolean("remember", true);
            editor.putString("email", email);
            editor.putString("password", password);
        } else {
            editor.putBoolean("remember", false);
            editor.remove("email");
            editor.remove("password");
        }
        editor.apply();
    }
    
    /**
     * 登录
     */
    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // 验证输入
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
        
        // 在后台线程中执行登录
        btnLogin.setEnabled(false);
        btnLogin.setText("登录中...");
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserEntity user = userRepository.login(email, password);
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("登录");
                            
                            if (user != null) {
                                // 保存账号密码
                                saveAccount(email, password);
                                
                                Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                                
                                // 返回到设置页面
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "邮箱或密码错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("登录");
                            Toast.makeText(LoginActivity.this, "登录失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

