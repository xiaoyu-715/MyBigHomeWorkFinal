package com.example.mybighomework.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 用户实体类
 * 用于登录注册功能
 */
@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class UserEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String email; // 邮箱，作为登录账号
    private String password; // 密码（已加密）
    private String username; // 用户名
    private String phone; // 手机号
    private String avatarPath; // 头像路径
    private long createTime; // 创建时间
    private long lastLoginTime; // 最后登录时间
    private boolean isLoggedIn; // 是否已登录
    
    public UserEntity() {
        this.createTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
        this.isLoggedIn = false;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAvatarPath() {
        return avatarPath;
    }
    
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public long getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}

