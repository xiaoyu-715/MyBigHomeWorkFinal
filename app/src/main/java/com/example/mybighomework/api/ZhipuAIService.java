package com.example.mybighomework.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.List;

/**
 * 智谱AI API 服务类
 * 用于AI对话、批改翻译和写作
 * 使用免费模型 glm-4-flash
 */
public class ZhipuAIService {
    
    private static final String TAG = "ZhipuAIService";
    
    // 智谱AI API 端点
    private static final String API_ENDPOINT = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    
    // 默认模型（使用免费的glm-4-flash）
    private static final String DEFAULT_MODEL = "glm-4-flash";
    
    // API Key
    private String apiKey;
    
    // 线程池
    private final ExecutorService executorService;
    
    /**
     * 构造函数
     * @param apiKey 智谱AI API Key
     */
    public ZhipuAIService(String apiKey) {
        this.apiKey = apiKey;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * 批改翻译
     * @param userTranslation 用户的翻译
     * @param referenceTranslation 参考译文
     * @param callback 回调接口
     */
    public void gradeTranslation(String userTranslation, String referenceTranslation, GradeCallback callback) {
        String prompt = "你是一位专业的英语老师，请批改以下翻译答案。\n\n" +
                "【参考译文】\n" + referenceTranslation + "\n\n" +
                "【学生译文】\n" + userTranslation + "\n\n" +
                "请从以下几个维度评分（满分15分）：\n" +
                "1. 准确性（5分）：译文是否准确表达原文意思\n" +
                "2. 流畅性（5分）：译文是否通顺自然\n" +
                "3. 用词（5分）：用词是否恰当、地道\n\n" +
                "请严格按照以下JSON格式输出：\n" +
                "{\n" +
                "  \"score\": 分数（0-15之间的数字），\n" +
                "  \"comment\": \"评语（100字以内，包含优点和不足）\"\n" +
                "}\n\n" +
                "注意：只输出JSON，不要包含其他文字。";
        
        chat(prompt, callback);
    }
    
    /**
     * 批改写作
     * @param essay 用户的作文
     * @param topic 作文题目
     * @param callback 回调接口
     */
    public void gradeWriting(String essay, String topic, GradeCallback callback) {
        String prompt = "你是一位专业的英语老师，请批改以下英语作文。\n\n" +
                "【作文题目】\n" + topic + "\n\n" +
                "【学生作文】\n" + essay + "\n\n" +
                "请从以下几个维度评分（满分15分）：\n" +
                "1. 内容（4分）：是否切题，内容是否充实\n" +
                "2. 结构（4分）：结构是否清晰，逻辑是否连贯\n" +
                "3. 语法（4分）：语法是否正确\n" +
                "4. 词汇（3分）：词汇使用是否恰当、丰富\n\n" +
                "请严格按照以下JSON格式输出：\n" +
                "{\n" +
                "  \"score\": 分数（0-15之间的数字），\n" +
                "  \"comment\": \"评语（150字以内，包含各维度的具体评价）\"\n" +
                "}\n\n" +
                "注意：只输出JSON，不要包含其他文字。";
        
        chat(prompt, callback);
    }
    
    /**
     * 发送聊天请求
     * @param prompt 提示词
     * @param callback 回调接口
     */
    private void chat(String prompt, GradeCallback callback) {
        executorService.execute(() -> {
            try {
                // 构建请求体
                JSONObject requestBody = buildRequestBody(prompt);
                
                // 发送请求
                String response = sendRequest(requestBody.toString());
                
                // 解析响应
                String content = parseResponse(response);
                
                // 解析评分结果
                GradeResult result = parseGradeResult(content);
                
                // 回调成功
                if (callback != null) {
                    callback.onSuccess(result);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Grade request failed", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 构建请求体
     */
    private JSONObject buildRequestBody(String prompt) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", DEFAULT_MODEL);
        
        JSONArray messagesArray = new JSONArray();
        JSONObject messageObj = new JSONObject();
        messageObj.put("role", "user");
        messageObj.put("content", prompt);
        messagesArray.put(messageObj);
        
        requestBody.put("messages", messagesArray);
        
        return requestBody;
    }
    
    /**
     * 发送 HTTP 请求
     */
    private String sendRequest(String requestBody) throws IOException {
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // 设置请求方法和头部
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);  // 批改可能需要更长时间
            
            // 发送请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return response.toString();
            } else {
                // 读取错误信息
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                reader.close();
                
                throw new IOException("HTTP " + responseCode + ": " + errorResponse.toString());
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 解析响应
     */
    private String parseResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        
        if (choices.length() > 0) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.getString("content");
        }
        
        throw new JSONException("No content in response");
    }
    
    /**
     * 解析评分结果
     */
    private GradeResult parseGradeResult(String content) throws JSONException {
        // 尝试提取JSON部分（AI可能会返回额外的文字）
        String jsonStr = content.trim();
        
        // 如果包含```json标记，提取其中的JSON
        if (jsonStr.contains("```json")) {
            int startIndex = jsonStr.indexOf("```json") + 7;
            int endIndex = jsonStr.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                jsonStr = jsonStr.substring(startIndex, endIndex).trim();
            }
        } else if (jsonStr.contains("```")) {
            int startIndex = jsonStr.indexOf("```") + 3;
            int endIndex = jsonStr.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                jsonStr = jsonStr.substring(startIndex, endIndex).trim();
            }
        }
        
        // 提取JSON对象
        int jsonStart = jsonStr.indexOf('{');
        int jsonEnd = jsonStr.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            jsonStr = jsonStr.substring(jsonStart, jsonEnd + 1);
        }
        
        JSONObject jsonResult = new JSONObject(jsonStr);
        
        float score = (float) jsonResult.getDouble("score");
        String comment = jsonResult.getString("comment");
        
        // 确保分数在合理范围内
        if (score < 0) score = 0;
        if (score > 15) score = 15;
        
        return new GradeResult(score, comment);
    }
    
    /**
     * 设置 API Key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * 关闭服务
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // ==================== 通用聊天功能 ====================
    
    /**
     * 发送聊天请求（通用方法）
     * @param messages 消息列表
     * @param callback 回调接口
     */
    public void chat(List<ChatMessage> messages, ChatCallback callback) {
        chat(messages, DEFAULT_MODEL, callback);
    }
    
    /**
     * 发送聊天请求（指定模型）
     * @param messages 消息列表
     * @param model 模型名称
     * @param callback 回调接口
     */
    public void chat(List<ChatMessage> messages, String model, ChatCallback callback) {
        executorService.execute(() -> {
            try {
                // 构建请求体
                JSONObject requestBody = buildChatRequestBody(messages, model);
                
                // 发送请求
                String response = sendRequest(requestBody.toString());
                
                // 解析响应
                String content = parseResponse(response);
                
                // 回调成功
                if (callback != null) {
                    callback.onSuccess(content);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Chat request failed", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 发送流式聊天请求
     * @param messages 消息列表
     * @param callback 流式回调接口
     */
    public void chatStream(List<ChatMessage> messages, StreamCallback callback) {
        chatStream(messages, DEFAULT_MODEL, callback);
    }
    
    /**
     * 发送流式聊天请求（指定模型）
     * @param messages 消息列表
     * @param model 模型名称
     * @param callback 流式回调接口
     */
    public void chatStream(List<ChatMessage> messages, String model, StreamCallback callback) {
        executorService.execute(() -> {
            BufferedReader reader = null;
            HttpURLConnection connection = null;
            
            try {
                // 构建请求体（开启流式）
                JSONObject requestBody = buildChatRequestBody(messages, model);
                requestBody.put("stream", true);
                
                // 创建连接
                URL url = new URL(API_ENDPOINT);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);
                
                // 发送请求体
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                // 读取流式响应
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            
                            // 检查是否结束
                            if ("[DONE]".equals(data)) {
                                if (callback != null) {
                                    callback.onComplete();
                                }
                                break;
                            }
                            
                            // 解析流式数据
                            try {
                                JSONObject jsonData = new JSONObject(data);
                                JSONArray choices = jsonData.getJSONArray("choices");
                                if (choices.length() > 0) {
                                    JSONObject choice = choices.getJSONObject(0);
                                    JSONObject delta = choice.getJSONObject("delta");
                                    
                                    if (delta.has("content")) {
                                        String content = delta.getString("content");
                                        if (callback != null) {
                                            callback.onChunk(content);
                                        }
                                    }
                                    
                                    // 检查是否是最后一个chunk
                                    String finishReason = choice.optString("finish_reason", null);
                                    if ("stop".equals(finishReason)) {
                                        if (callback != null) {
                                            callback.onComplete();
                                        }
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse stream data: " + data, e);
                            }
                        }
                    }
                } else {
                    // 读取错误信息
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    
                    if (callback != null) {
                        callback.onError("HTTP " + responseCode + ": " + errorResponse.toString());
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Stream chat request failed", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close resources", e);
                }
            }
        });
    }
    
    /**
     * 构建聊天请求体
     */
    private JSONObject buildChatRequestBody(List<ChatMessage> messages, String model) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        
        JSONArray messagesArray = new JSONArray();
        for (ChatMessage message : messages) {
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", message.getRole());
            messageObj.put("content", message.getContent());
            messagesArray.put(messageObj);
        }
        requestBody.put("messages", messagesArray);
        
        return requestBody;
    }
    
    /**
     * 聊天消息类
     */
    public static class ChatMessage {
        private String role;
        private String content;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 聊天回调接口
     */
    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    /**
     * 流式回调接口
     */
    public interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(String error);
    }
    
    /**
     * 评分结果类
     */
    public static class GradeResult {
        private float score;
        private String comment;
        
        public GradeResult(float score, String comment) {
            this.score = score;
            this.comment = comment;
        }
        
        public float getScore() {
            return score;
        }
        
        public String getComment() {
            return comment;
        }
    }
    
    /**
     * 评分回调接口
     */
    public interface GradeCallback {
        void onSuccess(GradeResult result);
        void onError(String error);
    }
}

