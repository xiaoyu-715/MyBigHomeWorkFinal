package com.example.mybighomework.translation;

import android.content.Context;

import com.example.mybighomework.api.ZhipuAIService;

import java.util.ArrayList;
import java.util.List;

/**
 * 智谱AI翻译服务
 * 封装翻译逻辑，整合术语管理和Prompt构建
 */
public class ZhipuTranslationService {

    private static final String TAG = "ZhipuTranslationService";

    private final ZhipuAIService aiService;
    private final TerminologyManager terminologyManager;
    private final TranslationPromptBuilder promptBuilder;

    /**
     * 翻译回调接口
     */
    public interface TranslationCallback {
        /**
         * 翻译成功回调
         * @param translatedText 翻译结果
         */
        void onSuccess(String translatedText);

        /**
         * 翻译失败回调
         * @param error 错误信息
         */
        void onError(String error);
    }

    /**
     * 构造函数
     * @param aiService 智谱AI服务实例
     */
    public ZhipuTranslationService(ZhipuAIService aiService) {
        this.aiService = aiService;
        this.terminologyManager = new TerminologyManager();
        this.promptBuilder = new TranslationPromptBuilder();
    }

    /**
     * 构造函数（完整依赖注入）
     * @param aiService 智谱AI服务实例
     * @param terminologyManager 术语管理器
     * @param promptBuilder Prompt构建器
     */
    public ZhipuTranslationService(ZhipuAIService aiService, 
                                   TerminologyManager terminologyManager,
                                   TranslationPromptBuilder promptBuilder) {
        this.aiService = aiService;
        this.terminologyManager = terminologyManager;
        this.promptBuilder = promptBuilder;
    }

    /**
     * 执行翻译
     * @param text 待翻译文本
     * @param sourceLang 源语言代码 (en/zh)
     * @param targetLang 目标语言代码 (en/zh)
     * @param callback 翻译回调
     */
    public void translate(String text, String sourceLang, String targetLang, 
                         TranslationCallback callback) {
        // 参数校验
        if (text == null || text.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("请输入要翻译的内容");
            }
            return;
        }

        if (aiService == null) {
            if (callback != null) {
                callback.onError("翻译服务未初始化");
            }
            return;
        }

        // 获取术语表文本
        String terminologyText = terminologyManager.formatTermsForPrompt(sourceLang, targetLang);

        // 构建翻译Prompt
        String prompt = promptBuilder.buildPrompt(text.trim(), sourceLang, targetLang, terminologyText);

        // 构建消息列表
        List<ZhipuAIService.ChatMessage> messages = new ArrayList<>();
        messages.add(new ZhipuAIService.ChatMessage("user", prompt));

        // 调用AI服务
        aiService.chat(messages, new ZhipuAIService.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                // 解析响应，提取翻译结果
                String translatedText = promptBuilder.parseTranslationResponse(response);
                
                if (callback != null) {
                    if (translatedText != null && !translatedText.isEmpty()) {
                        callback.onSuccess(translatedText);
                    } else {
                        callback.onError("翻译结果为空");
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    // 转换错误信息为用户友好的提示
                    String userFriendlyError = convertToUserFriendlyError(error);
                    callback.onError(userFriendlyError);
                }
            }
        });
    }

    /**
     * 将技术错误信息转换为用户友好的提示
     * @param error 原始错误信息
     * @return 用户友好的错误提示
     */
    private String convertToUserFriendlyError(String error) {
        if (error == null) {
            return "翻译失败，请重试";
        }

        String lowerError = error.toLowerCase();

        if (lowerError.contains("timeout") || lowerError.contains("timed out")) {
            return "翻译请求超时，请重试";
        }

        if (lowerError.contains("network") || lowerError.contains("connect") || 
            lowerError.contains("unreachable") || lowerError.contains("no internet")) {
            return "网络连接失败，请检查网络设置";
        }

        if (lowerError.contains("401") || lowerError.contains("unauthorized") ||
            lowerError.contains("invalid api key")) {
            return "API配置错误，请联系开发者";
        }

        if (lowerError.contains("429") || lowerError.contains("rate limit")) {
            return "请求过于频繁，请稍后重试";
        }

        if (lowerError.contains("500") || lowerError.contains("502") || 
            lowerError.contains("503") || lowerError.contains("504")) {
            return "服务器暂时不可用，请稍后重试";
        }

        // 默认错误信息
        return "翻译失败：" + error;
    }

    /**
     * 获取术语管理器
     * @return 术语管理器实例
     */
    public TerminologyManager getTerminologyManager() {
        return terminologyManager;
    }

    /**
     * 获取Prompt构建器
     * @return Prompt构建器实例
     */
    public TranslationPromptBuilder getPromptBuilder() {
        return promptBuilder;
    }

    /**
     * 关闭翻译服务，释放资源
     * 应在Activity销毁时调用
     */
    public void shutdown() {
        if (aiService != null) {
            aiService.shutdown();
        }
    }
}
