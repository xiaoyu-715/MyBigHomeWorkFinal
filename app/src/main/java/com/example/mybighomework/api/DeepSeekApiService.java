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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DeepSeek API 服务类
 * 用于与 DeepSeek AI 大模型进行交互
 * 
 * 使用示例：
 * <pre>
 * DeepSeekApiService service = new DeepSeekApiService("your-api-key");
 * service.chat(messages, new DeepSeekApiService.ChatCallback() {
 *     @Override
 *     public void onSuccess(String response) {
 *         // 处理响应
 *     }
 *     
 *     @Override
 *     public void onError(String error) {
 *         // 处理错误
 *     }
 * });
 * </pre>
 */
public class DeepSeekApiService {
    
    private static final String TAG = "DeepSeekApiService";
    
    // DeepSeek API 端点
    private static final String API_ENDPOINT = "https://api.deepseek.com/v1/chat/completions";
    
    // 默认模型
    private static final String DEFAULT_MODEL = "deepseek-chat";
    
    // API Key
    private String apiKey;
    
    // 线程池
    private final ExecutorService executorService;
    
    /**
     * 构造函数
     * @param apiKey DeepSeek API Key
     */
    public DeepSeekApiService(String apiKey) {
        this.apiKey = apiKey;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * 发送聊天请求
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
                JSONObject requestBody = buildRequestBody(messages, model);
                
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
                JSONObject requestBody = buildRequestBody(messages, model);
                requestBody.put("stream", true);
                
                // 创建连接
                URL url = new URL(API_ENDPOINT);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                
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
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse stream data", e);
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
     * 构建请求体
     */
    private JSONObject buildRequestBody(List<ChatMessage> messages, String model) throws JSONException {
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
            connection.setReadTimeout(30000);
            
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
}

