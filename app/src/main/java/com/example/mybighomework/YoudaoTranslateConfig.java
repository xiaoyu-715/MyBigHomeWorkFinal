package com.example.mybighomework;

/**
 * 有道翻译配置类
 * 存储有道智云API的配置信息
 */
public class YoudaoTranslateConfig {
    
    /**
     * 有道智云应用ID（appKey）
     */
    public static final String APP_KEY = "2e83b10bbd2c6350";
    
    /**
     * 有道智云应用密钥（appSecret）
     * 注意：请妥善保管，不要泄露
     */
    public static final String APP_SECRET = "2lkngznQyAwzTTq8jPV7rUBEZlHK4WEB";
    
    /**
     * 有道图片翻译API地址
     */
    public static final String API_URL = "https://openapi.youdao.com/ocrtransapi";
    
    /**
     * 应用名称（英文字符串）
     */
    public static final String APP_SOURCE = "MyBigHomeWork";
    
    /**
     * 默认超时时间（毫秒）
     */
    public static final int DEFAULT_TIMEOUT = 15000;
    
    /**
     * 是否启用服务端渲染
     * 0: 不渲染，只返回文本
     * 1: 服务端渲染，返回图片
     */
    public static final String SERVER_RENDER = "1";  // 启用服务端渲染
    
    /**
     * 翻译模式
     * 0: NMT模型（默认）
     * 1: 有道翻译大模型pro版
     * 2: 有道翻译大模型lite版
     */
    public static final String TRANSLATE_MODE = "0";
    
    /**
     * 签名类型
     */
    public static final String SIGN_TYPE = "v3";
    
    /**
     * 响应格式
     */
    public static final String DOC_TYPE = "json";
    
    /**
     * 图片类型：Base64编码
     */
    public static final String IMG_TYPE = "1";
    
    /**
     * 检查配置是否有效
     */
    public static boolean isConfigValid() {
        return APP_KEY != null && 
               !APP_KEY.isEmpty() && 
               !APP_KEY.equals("YOUR_YOUDAO_APP_KEY") &&
               APP_SECRET != null &&
               !APP_SECRET.isEmpty();
    }
}

