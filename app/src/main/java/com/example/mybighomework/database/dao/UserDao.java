package com.example.mybighomework.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mybighomework.database.entity.UserEntity;

/**
 * 用户数据访问对象
 */
@Dao
public interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);
    
    @Update
    void update(UserEntity user);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(int userId);
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    UserEntity getLoggedInUser();
    
    @Query("UPDATE users SET isLoggedIn = 0")
    void logoutAllUsers();
    
    @Query("UPDATE users SET isLoggedIn = 1, lastLoginTime = :loginTime WHERE id = :userId")
    void setUserLoggedIn(int userId, long loginTime);
    
    @Query("UPDATE users SET username = :username WHERE id = :userId")
    void updateUsername(int userId, String username);
    
    @Query("UPDATE users SET phone = :phone WHERE id = :userId")
    void updatePhone(int userId, String phone);
    
    @Query("UPDATE users SET avatarPath = :avatarPath WHERE id = :userId")
    void updateAvatar(int userId, String avatarPath);
    
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    void updatePassword(int userId, String newPassword);
    
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean emailExists(String email);
    
    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUser(int userId);
}

