package com.example.mybighomework.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 解析工具类
 * 支持自定义分隔符，处理特殊字符和转义
 */
public class CsvParser {
    private static final String TAG = "CsvParser";
    
    /**
     * 解析结果回调接口
     */
    public interface ParseCallback<T> {
        /**
         * 解析单行数据
         * @param fields 字段数组
         * @param lineNumber 行号（从1开始）
         * @return 解析后的对象，返回null表示跳过该行
         */
        T parseLine(String[] fields, int lineNumber);
    }
    
    /**
     * 解析进度回调接口
     */
    public interface ProgressCallback {
        void onProgress(int current, int total);
    }
    
    /**
     * 流式解析回调接口 - 用于大文件处理，避免内存溢出
     */
    public interface StreamingCallback<T> {
        /**
         * 处理解析后的单条数据
         * @param item 解析后的对象
         * @param lineNumber 行号
         */
        void onItem(T item, int lineNumber);
        
        /**
         * 解析完成回调
         * @param totalProcessed 成功处理的总数
         */
        void onComplete(int totalProcessed);
    }
    
    /**
     * 解析CSV文件
     * @param inputStream 输入流
     * @param delimiter 分隔符
     * @param skipHeader 是否跳过首行（标题行）
     * @param callback 解析回调
     * @return 解析结果列表
     */
    public static <T> List<T> parse(InputStream inputStream, String delimiter, 
                                     boolean skipHeader, ParseCallback<T> callback) {
        return parse(inputStream, delimiter, skipHeader, callback, null);
    }
    
    /**
     * 解析CSV文件（带进度回调）
     */
    public static <T> List<T> parse(InputStream inputStream, String delimiter,
                                     boolean skipHeader, ParseCallback<T> callback,
                                     ProgressCallback progressCallback) {
        List<T> results = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            int totalLines = 0;
            
            // 如果需要进度回调，先计算总行数（可选优化）
            // 这里简化处理，不预先计算总行数
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // 跳过标题行
                if (skipHeader && lineNumber == 1) {
                    continue;
                }
                
                try {
                    String[] fields = splitLine(line, delimiter);
                    T result = callback.parseLine(fields, lineNumber);
                    if (result != null) {
                        results.add(result);
                    }
                    
                    // 进度回调（每1000行回调一次）
                    if (progressCallback != null && lineNumber % 1000 == 0) {
                        progressCallback.onProgress(lineNumber, -1);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "解析第 " + lineNumber + " 行失败: " + e.getMessage());
                    // 继续处理下一行
                }
            }
            
            Log.d(TAG, "CSV解析完成，共处理 " + lineNumber + " 行，成功 " + results.size() + " 条");
            
        } catch (IOException e) {
            Log.e(TAG, "读取CSV文件失败", e);
        }
        
        return results;
    }
    
    /**
     * 流式解析CSV文件 - 用于大文件处理，避免内存溢出
     * 边读边处理，不在内存中保存所有结果
     * 
     * @param inputStream 输入流
     * @param delimiter 分隔符
     * @param skipHeader 是否跳过首行
     * @param parseCallback 解析回调
     * @param streamingCallback 流式处理回调
     * @param progressCallback 进度回调（可选）
     */
    public static <T> void parseStreaming(InputStream inputStream, String delimiter,
                                          boolean skipHeader, ParseCallback<T> parseCallback,
                                          StreamingCallback<T> streamingCallback,
                                          ProgressCallback progressCallback) {
        int processedCount = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8), 8192)) {
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // 跳过标题行
                if (skipHeader && lineNumber == 1) {
                    continue;
                }
                
                try {
                    String[] fields = splitLine(line, delimiter);
                    T result = parseCallback.parseLine(fields, lineNumber);
                    if (result != null) {
                        streamingCallback.onItem(result, lineNumber);
                        processedCount++;
                    }
                    
                    // 进度回调（每5000行回调一次，减少回调频率）
                    if (progressCallback != null && lineNumber % 5000 == 0) {
                        progressCallback.onProgress(lineNumber, -1);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "解析第 " + lineNumber + " 行失败: " + e.getMessage());
                }
            }
            
            Log.d(TAG, "CSV流式解析完成，共处理 " + lineNumber + " 行，成功 " + processedCount + " 条");
            
        } catch (IOException e) {
            Log.e(TAG, "读取CSV文件失败", e);
        }
        
        // 通知完成
        if (streamingCallback != null) {
            streamingCallback.onComplete(processedCount);
        }
    }
    
    /**
     * 分割CSV行
     * 处理引号内的分隔符和转义字符
     */
    public static String[] splitLine(String line, String delimiter) {
        if (line == null || line.isEmpty()) {
            return new String[0];
        }
        
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 处理引号
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 转义的引号 ""
                    currentField.append('"');
                    i++; // 跳过下一个引号
                } else {
                    // 切换引号状态
                    inQuotes = !inQuotes;
                }
            } else if (!inQuotes && matchesDelimiter(line, i, delimiter)) {
                // 遇到分隔符（不在引号内）
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
                i += delimiter.length() - 1; // 跳过分隔符
            } else {
                currentField.append(c);
            }
        }
        
        // 添加最后一个字段
        fields.add(currentField.toString().trim());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * 检查是否匹配分隔符
     */
    private static boolean matchesDelimiter(String line, int index, String delimiter) {
        if (index + delimiter.length() > line.length()) {
            return false;
        }
        return line.substring(index, index + delimiter.length()).equals(delimiter);
    }
    
    /**
     * 安全获取字段值
     */
    public static String getField(String[] fields, int index, String defaultValue) {
        if (fields == null || index < 0 || index >= fields.length) {
            return defaultValue;
        }
        String value = fields[index];
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }
    
    /**
     * 安全获取整数字段
     */
    public static int getIntField(String[] fields, int index, int defaultValue) {
        String value = getField(fields, index, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 安全获取浮点数字段
     */
    public static float getFloatField(String[] fields, int index, float defaultValue) {
        String value = getField(fields, index, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 将对象序列化为CSV行
     */
    public static String toCsvLine(String[] fields, String delimiter) {
        if (fields == null || fields.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(escapeField(fields[i], delimiter));
        }
        return sb.toString();
    }
    
    /**
     * 转义字段值
     */
    private static String escapeField(String field, String delimiter) {
        if (field == null) {
            return "";
        }
        
        // 如果包含分隔符、引号或换行符，需要用引号包围
        if (field.contains(delimiter) || field.contains("\"") || 
            field.contains("\n") || field.contains("\r")) {
            // 转义内部引号
            String escaped = field.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return field;
    }
}
