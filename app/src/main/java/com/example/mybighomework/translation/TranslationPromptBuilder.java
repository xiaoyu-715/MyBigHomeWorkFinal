package com.example.mybighomework.translation;

/**
 * 翻译Prompt构建器
 * 负责构建翻译请求的Prompt和解析AI响应
 */
public class TranslationPromptBuilder {

    // 语言显示名称
    private static final String LANG_NAME_CHINESE = "中文";
    private static final String LANG_NAME_ENGLISH = "英文";

    /**
     * 构建翻译Prompt
     * @param text 待翻译文本
     * @param sourceLang 源语言代码 (en/zh)
     * @param targetLang 目标语言代码 (en/zh)
     * @param terminologyText 格式化后的术语表文本
     * @return 完整的翻译Prompt
     */
    public String buildPrompt(String text, String sourceLang, String targetLang, String terminologyText) {
        String sourceLangName = getLanguageName(sourceLang);
        String targetLangName = getLanguageName(targetLang);

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的翻译助手。请将以下").append(sourceLangName)
              .append("文本翻译成").append(targetLangName).append("。\n\n");

        // 添加术语约束（如果有）
        if (terminologyText != null && !terminologyText.isEmpty()) {
            prompt.append("【术语约束】\n");
            prompt.append("翻译时请严格遵循以下术语对照表：\n");
            prompt.append(terminologyText).append("\n");
        }

        prompt.append("【翻译要求】\n");
        prompt.append("1. 保持原文的语义和语气\n");
        if (terminologyText != null && !terminologyText.isEmpty()) {
            prompt.append("2. 术语表中的词汇必须按照指定方式翻译\n");
            prompt.append("3. 只输出翻译结果，不要添加任何解释或说明\n\n");
        } else {
            prompt.append("2. 只输出翻译结果，不要添加任何解释或说明\n\n");
        }

        prompt.append("【待翻译文本】\n");
        prompt.append(text).append("\n\n");
        prompt.append("【翻译结果】");

        return prompt.toString();
    }

    /**
     * 解析AI响应，提取翻译结果
     * @param response AI返回的响应文本
     * @return 提取的翻译结果
     */
    public String parseTranslationResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "";
        }

        String result = response.trim();

        // 移除可能的【翻译结果】标记
        if (result.startsWith("【翻译结果】")) {
            result = result.substring("【翻译结果】".length()).trim();
        }

        // 移除可能的引号包裹
        if ((result.startsWith("\"") && result.endsWith("\"")) ||
            (result.startsWith("'") && result.endsWith("'"))) {
            result = result.substring(1, result.length() - 1);
        }

        // 移除可能的冒号开头
        if (result.startsWith(":") || result.startsWith("：")) {
            result = result.substring(1).trim();
        }

        return result.trim();
    }

    /**
     * 获取语言显示名称
     * @param langCode 语言代码
     * @return 语言显示名称
     */
    private String getLanguageName(String langCode) {
        if (TerminologyManager.LANGUAGE_CHINESE.equals(langCode)) {
            return LANG_NAME_CHINESE;
        } else if (TerminologyManager.LANGUAGE_ENGLISH.equals(langCode)) {
            return LANG_NAME_ENGLISH;
        }
        return langCode;
    }
}
