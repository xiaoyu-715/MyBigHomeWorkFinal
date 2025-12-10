package com.example.mybighomework.repository;

import android.content.Context;

import com.example.mybighomework.database.AppDatabase;
import com.example.mybighomework.database.dao.UserDao;
import com.example.mybighomework.database.entity.UserEntity;
import com.example.mybighomework.repository.UserSettingsRepository;
import com.example.mybighomework.utils.PasswordUtils;

/**
 * 用户数据仓库
 */
public class UserRepository {

    private UserDao userDao;
    private Context context;

    public UserRepository(Context context) {
        this.context = context;
        AppDatabase database = AppDatabase.getInstance(context);
        userDao = database.userDao();
    }
    
    /**
     * 用户注册
     * @param email 邮箱
     * @param password 密码
     * @param username 用户名
     * @return 注册成功返回用户ID，失败返回-1
     */
    public long register(String email, String password, String username) {
        // 检查邮箱是否已存在
        if (userDao.emailExists(email)) {
            return -1;
        }
        
        // 创建新用户
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(PasswordUtils.hashPassword(password));
        user.setUsername(username);
        
        return userDao.insert(user);
    }
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回null
     */
    public UserEntity login(String email, String password) {
        UserEntity user = userDao.getUserByEmail(email);

        if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
            // 登出所有其他用户
            userDao.logoutAllUsers();
            // 设置当前用户为已登录
            userDao.setUserLoggedIn(user.getId(), System.currentTimeMillis());
            // 重新获取用户信息
            UserEntity loggedInUser = userDao.getUserById(user.getId());

            // 同步用户名到用户设置
            if (loggedInUser != null && loggedInUser.getUsername() != null) {
                UserSettingsRepository userSettingsRepository = new UserSettingsRepository(context);
                userSettingsRepository.updateUserName(loggedInUser.getUsername());
            }

            return loggedInUser;
        }

        return null;
    }
    
    /**
     * 获取当前登录的用户
     */
    public UserEntity getLoggedInUser() {
        return userDao.getLoggedInUser();
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        userDao.logoutAllUsers();
    }
    
    /**
     * 更新用户信息
     */
    public void updateUser(UserEntity user) {
        userDao.update(user);
    }
    
    /**
     * 更新用户名
     */
    public void updateUsername(int userId, String username) {
        userDao.updateUsername(userId, username);
    }
    
    /**
     * 更新手机号
     */
    public void updatePhone(int userId, String phone) {
        userDao.updatePhone(userId, phone);
    }
    
    /**
     * 更新头像
     */
    public void updateAvatar(int userId, String avatarPath) {
        userDao.updateAvatar(userId, avatarPath);
    }
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功返回true
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        UserEntity user = userDao.getUserById(userId);
        if (user != null && PasswordUtils.verifyPassword(oldPassword, user.getPassword())) {
            String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
            userDao.updatePassword(userId, hashedNewPassword);
            return true;
        }
        return false;
    }
    
    /**
     * 检查邮箱是否已存在
     */
    public boolean emailExists(String email) {
        return userDao.emailExists(email);
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(int userId) {
        userDao.deleteUser(userId);
    }
}

