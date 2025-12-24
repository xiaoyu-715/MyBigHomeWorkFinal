package com.example.mybighomework.translation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 术语管理器
 * 管理专业术语映射表，支持英译中和中译英两个方向
 */
public class TerminologyManager {

    // 英译中术语表
    private static final Map<String, String> EN_TO_ZH_TERMS;
    
    // 中译英术语表
    private static final Map<String, String> ZH_TO_EN_TERMS;

    static {
        // 初始化英译中术语表
        Map<String, String> enToZh = new HashMap<>();
        enToZh.put("machine learning", "机器学习");
        enToZh.put("artificial intelligence", "人工智能");
        enToZh.put("deep learning", "深度学习");
        enToZh.put("neural network", "神经网络");
        enToZh.put("natural language processing", "自然语言处理");
        enToZh.put("computer vision", "计算机视觉");
        enToZh.put("reinforcement learning", "强化学习");
        enToZh.put("supervised learning", "监督学习");
        enToZh.put("unsupervised learning", "无监督学习");
        enToZh.put("convolutional neural network", "卷积神经网络");
        enToZh.put("recurrent neural network", "循环神经网络");
        enToZh.put("transformer", "Transformer模型");
        enToZh.put("attention mechanism", "注意力机制");
        enToZh.put("gradient descent", "梯度下降");
        enToZh.put("backpropagation", "反向传播");
        enToZh.put("overfitting", "过拟合");
        enToZh.put("underfitting", "欠拟合");
        enToZh.put("regularization", "正则化");
        enToZh.put("dropout", "随机失活");
        enToZh.put("batch normalization", "批量归一化");
        EN_TO_ZH_TERMS = Collections.unmodifiableMap(enToZh);

        // 初始化中译英术语表
        Map<String, String> zhToEn = new HashMap<>();
        zhToEn.put("机器学习", "machine learning");
        zhToEn.put("人工智能", "artificial intelligence");
        zhToEn.put("深度学习", "deep learning");
        zhToEn.put("神经网络", "neural network");
        zhToEn.put("自然语言处理", "natural language processing");
        zhToEn.put("计算机视觉", "computer vision");
        zhToEn.put("强化学习", "reinforcement learning");
        zhToEn.put("监督学习", "supervised learning");
        zhToEn.put("无监督学习", "unsupervised learning");
        zhToEn.put("卷积神经网络", "convolutional neural network");
        zhToEn.put("循环神经网络", "recurrent neural network");
        zhToEn.put("Transformer模型", "transformer");
        zhToEn.put("注意力机制", "attention mechanism");
        zhToEn.put("梯度下降", "gradient descent");
        zhToEn.put("反向传播", "backpropagation");
        zhToEn.put("过拟合", "overfitting");
        zhToEn.put("欠拟合", "underfitting");
        zhToEn.put("正则化", "regularization");
        zhToEn.put("随机失活", "dropout");
        zhToEn.put("批量归一化", "batch normalization");
        ZH_TO_EN_TERMS = Collections.unmodifiableMap(zhToEn);
    }

    // 语言常量（与 ML Kit TranslateLanguage 保持一致）
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_CHINESE = "zh";

    /**
     * 获取英译中术语表
     * @return 英译中术语映射
     */
    public Map<String, String> getEnToZhTerms() {
        return EN_TO_ZH_TERMS;
    }

    /**
     * 获取中译英术语表
     * @return 中译英术语映射
     */
    public Map<String, String> getZhToEnTerms() {
        return ZH_TO_EN_TERMS;
    }

    /**
     * 根据翻译方向获取术语表
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 对应方向的术语映射表
     */
    public Map<String, String> getTermsForDirection(String sourceLang, String targetLang) {
        if (LANGUAGE_ENGLISH.equals(sourceLang) && LANGUAGE_CHINESE.equals(targetLang)) {
            return EN_TO_ZH_TERMS;
        } else if (LANGUAGE_CHINESE.equals(sourceLang) && LANGUAGE_ENGLISH.equals(targetLang)) {
            return ZH_TO_EN_TERMS;
        }
        // 不支持的语言方向，返回空映射
        return Collections.emptyMap();
    }

    /**
     * 格式化术语表为Prompt文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 格式化后的术语表文本
     */
    public String formatTermsForPrompt(String sourceLang, String targetLang) {
        Map<String, String> terms = getTermsForDirection(sourceLang, targetLang);
        
        if (terms.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : terms.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(" → ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
