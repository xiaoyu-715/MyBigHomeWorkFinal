package com.example.mybighomework;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 翻译文本预处理工具类
 * 用于优化OCR识别文本和翻译输入，提升翻译质量
 */
public class TranslationTextProcessor {

    /**
     * 预处理文本，优化翻译质量
     */
    public static String preprocessText(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        // 1. 去除多余空格
        text = removeExtraSpaces(text);

        // 2. 修复常见OCR错误
        text = fixCommonOcrErrors(text);

        // 3. 智能处理换行
        text = handleLineBreaks(text);

        // 4. 处理标点符号
        text = normalizePunctuation(text);

        return text.trim();
    }

    /**
     * 去除多余空格
     */
    private static String removeExtraSpaces(String text) {
        // 移除行首行尾空格
        text = text.trim();
        
        // 将多个空格替换为单个空格
        text = text.replaceAll("\\s+", " ");
        
        // 中文字符间不需要空格
        text = text.replaceAll("([\\u4e00-\\u9fa5])\\s+([\\u4e00-\\u9fa5])", "$1$2");
        
        return text;
    }

    /**
     * 修复常见的OCR识别错误
     */
    private static String fixCommonOcrErrors(String text) {
        // 修复常见的数字和字母混淆
        text = text.replaceAll("(?<![0-9])O(?![0-9a-zA-Z])", "0"); // O -> 0
        text = text.replaceAll("(?<![a-zA-Z])l(?=\\d)", "1"); // l -> 1
        
        // 修复常见的标点符号识别错误
        text = text.replaceAll("'", "'");
        text = text.replaceAll("\u201c", "\"");  // 左双引号
        text = text.replaceAll("\u201d", "\"");  // 右双引号
        text = text.replaceAll("\u2014", "-");   // 破折号
        
        return text;
    }

    /**
     * 智能处理换行符
     * 保留段落换行，移除段内不必要的换行
     */
    private static String handleLineBreaks(String text) {
        // 将多个连续换行符替换为段落标记
        text = text.replaceAll("\n{2,}", "###PARAGRAPH###");
        
        // 处理单个换行符
        // 如果换行前后都是中文或英文，则可能是OCR错误导致的分行
        text = text.replaceAll("([\\u4e00-\\u9fa5a-zA-Z])\\s*\n\\s*([\\u4e00-\\u9fa5a-zA-Z])", "$1$2");
        
        // 恢复段落换行
        text = text.replaceAll("###PARAGRAPH###", "\n\n");
        
        return text;
    }

    /**
     * 标准化标点符号
     */
    private static String normalizePunctuation(String text) {
        // 统一中文标点
        text = text.replaceAll("，\\s*", "，");
        text = text.replaceAll("。\\s*", "。");
        text = text.replaceAll("！\\s*", "！");
        text = text.replaceAll("？\\s*", "？");
        
        // 统一英文标点后的空格
        text = text.replaceAll("([,;:])([^\\s])", "$1 $2");
        text = text.replaceAll("([.!?])([A-Z])", "$1 $2");
        
        return text;
    }

    /**
     * 智能分句
     * 将长文本分割成适合翻译的句子
     */
    public static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        
        if (TextUtils.isEmpty(text)) {
            return sentences;
        }

        // 预处理文本
        text = preprocessText(text);

        // 按中英文句子分隔符分割
        String[] parts = text.split("(?<=[。！？.!?])\\s*");
        
        StringBuilder currentSentence = new StringBuilder();
        
        for (String part : parts) {
            // 如果当前句子加上新部分超过200字符，先保存当前句子
            if (currentSentence.length() > 0 && 
                currentSentence.length() + part.length() > 200) {
                sentences.add(currentSentence.toString().trim());
                currentSentence = new StringBuilder();
            }
            
            currentSentence.append(part).append(" ");
            
            // 如果句子够长且有明确结束标记，保存它
            if (currentSentence.length() > 50 && 
                (part.endsWith("。") || part.endsWith("！") || 
                 part.endsWith("？") || part.endsWith(".") || 
                 part.endsWith("!") || part.endsWith("?"))) {
                sentences.add(currentSentence.toString().trim());
                currentSentence = new StringBuilder();
            }
        }
        
        // 添加剩余内容
        if (currentSentence.length() > 0) {
            sentences.add(currentSentence.toString().trim());
        }
        
        return sentences;
    }

    /**
     * 合并OCR识别的文本块
     * 智能判断文本块之间的关系
     */
    public static String mergeTextBlocks(List<String> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return "";
        }

        if (blocks.size() == 1) {
            return preprocessText(blocks.get(0));
        }

        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < blocks.size(); i++) {
            String block = blocks.get(i).trim();
            
            if (block.isEmpty()) {
                continue;
            }

            result.append(block);
            
            // 判断是否需要添加空格或换行
            if (i < blocks.size() - 1) {
                String nextBlock = blocks.get(i + 1).trim();
                
                if (!nextBlock.isEmpty()) {
                    // 如果当前块以标点结束，添加空格
                    if (block.matches(".*[。！？.!?,;:]$")) {
                        result.append(" ");
                    }
                    // 如果是中英文混合，添加空格
                    else if (endsWithLetter(block) && startsWithLetter(nextBlock)) {
                        result.append(" ");
                    }
                    // 中文之间不加空格
                    else if (isChinese(block.charAt(block.length() - 1)) && 
                             isChinese(nextBlock.charAt(0))) {
                        // 不添加空格
                    }
                    else {
                        result.append(" ");
                    }
                }
            }
        }
        
        return preprocessText(result.toString());
    }

    /**
     * 判断字符串是否以字母结尾
     */
    private static boolean endsWithLetter(String text) {
        if (text.isEmpty()) return false;
        char last = text.charAt(text.length() - 1);
        return Character.isLetter(last);
    }

    /**
     * 判断字符串是否以字母开头
     */
    private static boolean startsWithLetter(String text) {
        if (text.isEmpty()) return false;
        char first = text.charAt(0);
        return Character.isLetter(first);
    }

    /**
     * 判断是否为中文字符
     */
    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    /**
     * 检测文本语言
     */
    public static String detectLanguage(String text) {
        if (TextUtils.isEmpty(text)) {
            return "unknown";
        }

        int chineseCount = 0;
        int englishCount = 0;
        
        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                chineseCount++;
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                englishCount++;
            }
        }

        if (chineseCount > englishCount) {
            return "zh";
        } else if (englishCount > chineseCount) {
            return "en";
        }
        
        return "unknown";
    }

    /**
     * 格式化翻译结果
     * 使翻译结果更加规范和易读
     */
    public static String formatTranslationResult(String translation) {
        if (TextUtils.isEmpty(translation)) {
            return translation;
        }

        // 首字母大写
        translation = capitalizeFirstLetter(translation);
        
        // 确保句子结尾有标点
        if (!translation.matches(".*[.!?。！？]$")) {
            // 判断语言类型
            if (detectLanguage(translation).equals("zh")) {
                translation += "。";
            } else {
                translation += ".";
            }
        }

        return translation.trim();
    }

    /**
     * 首字母大写
     */
    private static String capitalizeFirstLetter(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        // 找到第一个字母
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                if (Character.isLowerCase(c)) {
                    return text.substring(0, i) + 
                           Character.toUpperCase(c) + 
                           text.substring(i + 1);
                }
                break;
            }
        }

        return text;
    }
}

