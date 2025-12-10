package com.example.mybighomework.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码工具类
 * 用于密码的加密和验证
 */
public class PasswordUtils {
    
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * 生成随机盐值
     */
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return android.util.Base64.encodeToString(salt, android.util.Base64.DEFAULT);
    }
    
    /**
     * 使用SHA-256加密密码
     * @param password 原始密码
     * @return 加密后的密码（格式：salt$hash）
     */
    public static String hashPassword(String password) {
        try {
            String salt = generateSalt();
            String hash = hash(password, salt);
            return salt + "$" + hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码
     * @param password 输入的密码
     * @param hashedPassword 存储的加密密码
     * @return 密码是否匹配
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length != 2) {
                return false;
            }
            
            String salt = parts[0];
            String hash = parts[1];
            
            String computedHash = hash(password, salt);
            return hash.equals(computedHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 计算哈希值
     */
    private static String hash(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update((salt + password).getBytes());
        byte[] bytes = md.digest();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }
}

