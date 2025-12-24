package com.example.mybighomework.autoglm.network;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 多模态模型客户端
 * 用于与智谱AI的autoglm-phone模型通信，支持发送文本和图片的多模态消息
 * 
 * 需求: 3.1-3.7
 */
public class MultimodalModelClient {
    
    private static final String TAG = "MultimodalModelClient";
    
    // API配置
    private static final String API_ENDPOINT = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String MODEL_NAME = "autoglm-phone";
    private static final int TIMEOUT_SECONDS = 60;
    private static final int MAX_TOKENS = 3000;
    
    // 历史消息中保留图片的最大数量（超过后清理旧图片）
    private static final int MAX_HISTORY_IMAGES = 2;
    
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final List<Message> conversationHistory;
    
    /**
     * 构造函数
     * @param apiKey 智谱AI API密钥
     */
    public MultimodalModelClient(String apiKey) {
        this.apiKey = apiKey;
        this.conversationHistory = new ArrayList<>();
        this.gson = new GsonBuilder().create();
        
        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 发送多模态请求
     * @param userText 用户文本消息
     * @param screenshot 当前屏幕截图（可为null）
     * @return AI响应文本
     * @throws IOException 网络请求失败时抛出
     */
    public String sendRequest(String userText, Bitmap screenshot) throws IOException {
        // 构建用户消息
        Message userMessage = createUserMessage(userText, screenshot);
        conversationHistory.add(userMessage);
        
        // 清理历史消息中的旧图片
        cleanupHistoryImages();
        
        // 构建请求体
        ChatRequestBody requestBody = buildRequestBody();
        String jsonBody = gson.toJson(requestBody);
        
        Log.d(TAG, "Sending request to API...");
        
        // 发送HTTP请求
        Request request = new Request.Builder()
                .url(API_ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                Log.e(TAG, "API Error: " + response.code() + " - " + errorBody);
                throw new IOException("API调用失败: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body() != null ? response.body().string() : "";
            ChatResponseBody chatResponse = gson.fromJson(responseBody, ChatResponseBody.class);
            
            if (chatResponse != null && chatResponse.choices != null && !chatResponse.choices.isEmpty()) {
                String assistantContent = chatResponse.choices.get(0).message.content;
                
                // 将助手响应添加到历史
                Message assistantMessage = new Message("assistant", assistantContent);
                conversationHistory.add(assistantMessage);
                
                Log.d(TAG, "Received response: " + assistantContent.substring(0, Math.min(100, assistantContent.length())) + "...");
                return assistantContent;
            } else {
                throw new IOException("API响应格式错误");
            }
        }
    }

    
    /**
     * 使用完整历史发送请求
     * @param history 完整的对话历史
     * @param screenshot 当前屏幕截图
     * @return AI响应文本
     * @throws IOException 网络请求失败时抛出
     */
    public String sendRequestWithHistory(List<Message> history, Bitmap screenshot) throws IOException {
        // 清空当前历史，使用传入的历史
        conversationHistory.clear();
        conversationHistory.addAll(history);
        
        // 如果最后一条是用户消息且有截图，添加图片
        if (!conversationHistory.isEmpty() && screenshot != null) {
            Message lastMessage = conversationHistory.get(conversationHistory.size() - 1);
            if ("user".equals(lastMessage.role) && lastMessage.content instanceof String) {
                // 将最后一条用户消息转换为多模态消息
                conversationHistory.remove(conversationHistory.size() - 1);
                Message multimodalMessage = createUserMessage((String) lastMessage.content, screenshot);
                conversationHistory.add(multimodalMessage);
            }
        }
        
        // 清理历史消息中的旧图片
        cleanupHistoryImages();
        
        // 构建请求体
        ChatRequestBody requestBody = buildRequestBody();
        String jsonBody = gson.toJson(requestBody);
        
        Log.d(TAG, "Sending request with history to API...");
        
        // 发送HTTP请求
        Request request = new Request.Builder()
                .url(API_ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                Log.e(TAG, "API Error: " + response.code() + " - " + errorBody);
                throw new IOException("API调用失败: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body() != null ? response.body().string() : "";
            ChatResponseBody chatResponse = gson.fromJson(responseBody, ChatResponseBody.class);
            
            if (chatResponse != null && chatResponse.choices != null && !chatResponse.choices.isEmpty()) {
                String assistantContent = chatResponse.choices.get(0).message.content;
                
                // 将助手响应添加到历史
                Message assistantMessage = new Message("assistant", assistantContent);
                conversationHistory.add(assistantMessage);
                
                return assistantContent;
            } else {
                throw new IOException("API响应格式错误");
            }
        }
    }
    
    /**
     * 创建用户消息（支持文本和图片）
     */
    private Message createUserMessage(String text, Bitmap screenshot) {
        if (screenshot == null) {
            // 纯文本消息
            return new Message("user", text);
        } else {
            // 多模态消息（文本+图片）
            List<ContentItem> contentItems = new ArrayList<>();
            
            // 添加文本内容
            ContentItem textItem = new ContentItem();
            textItem.type = "text";
            textItem.text = text;
            contentItems.add(textItem);
            
            // 添加图片内容
            ContentItem imageItem = new ContentItem();
            imageItem.type = "image_url";
            imageItem.imageUrl = new ImageUrl("data:image/png;base64," + bitmapToBase64(screenshot));
            contentItems.add(imageItem);
            
            return new Message("user", contentItems);
        }
    }
    
    /**
     * 构建API请求体
     */
    private ChatRequestBody buildRequestBody() {
        ChatRequestBody body = new ChatRequestBody();
        body.model = MODEL_NAME;
        body.messages = new ArrayList<>();
        
        // 添加系统提示词
        body.messages.add(new Message("system", SYSTEM_PROMPT));
        
        // 添加对话历史
        body.messages.addAll(conversationHistory);
        
        body.maxTokens = MAX_TOKENS;
        body.temperature = 0.0;
        body.topP = 0.85;
        body.frequencyPenalty = 0.2;
        body.stream = false;
        
        return body;
    }
    
    /**
     * 清理历史消息中的旧图片，只保留最近的几张
     * 需求: 3.5
     */
    private void cleanupHistoryImages() {
        int imageCount = 0;
        
        // 从后往前遍历，保留最近的图片
        for (int i = conversationHistory.size() - 1; i >= 0; i--) {
            Message message = conversationHistory.get(i);
            
            if (message.content instanceof List) {
                @SuppressWarnings("unchecked")
                List<ContentItem> contentItems = (List<ContentItem>) message.content;
                
                boolean hasImage = false;
                for (ContentItem item : contentItems) {
                    if ("image_url".equals(item.type)) {
                        hasImage = true;
                        break;
                    }
                }
                
                if (hasImage) {
                    imageCount++;
                    
                    // 如果超过最大图片数量，移除图片只保留文本
                    if (imageCount > MAX_HISTORY_IMAGES) {
                        String textContent = extractTextFromContent(contentItems);
                        message.content = textContent;
                        Log.d(TAG, "Cleaned up old image from history message at index " + i);
                    }
                }
            }
        }
    }
    
    /**
     * 从多模态内容中提取文本
     */
    private String extractTextFromContent(List<ContentItem> contentItems) {
        StringBuilder sb = new StringBuilder();
        for (ContentItem item : contentItems) {
            if ("text".equals(item.type) && item.text != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(item.text);
            }
        }
        return sb.toString();
    }
    
    /**
     * 将Bitmap图像转换为Base64编码字符串
     * 需求: 3.3
     * @param bitmap 要转换的图像
     * @return Base64编码的字符串
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 使用PNG格式避免压缩伪影
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }
    
    /**
     * 将Base64编码字符串转换回Bitmap
     * 用于测试往返一致性
     */
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        byte[] decodedBytes = Base64.decode(base64String, Base64.NO_WRAP);
        return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    
    /**
     * 获取当前对话历史
     */
    public List<Message> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * 清空对话历史
     */
    public void clearHistory() {
        conversationHistory.clear();
    }
    
    /**
     * 添加消息到历史
     */
    public void addToHistory(Message message) {
        conversationHistory.add(message);
    }

    
    // ==================== 内部数据类 ====================
    
    /**
     * 消息类，支持文本和多模态内容
     */
    public static class Message {
        public String role;
        public Object content; // 可以是String或List<ContentItem>
        
        public Message() {}
        
        public Message(String role, String textContent) {
            this.role = role;
            this.content = textContent;
        }
        
        public Message(String role, List<ContentItem> multimodalContent) {
            this.role = role;
            this.content = multimodalContent;
        }
        
        /**
         * 获取文本内容（如果是多模态消息则提取文本部分）
         */
        public String getTextContent() {
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof List) {
                @SuppressWarnings("unchecked")
                List<ContentItem> items = (List<ContentItem>) content;
                StringBuilder sb = new StringBuilder();
                for (ContentItem item : items) {
                    if ("text".equals(item.type) && item.text != null) {
                        if (sb.length() > 0) sb.append("\n");
                        sb.append(item.text);
                    }
                }
                return sb.toString();
            }
            return "";
        }
        
        /**
         * 检查是否包含图片
         */
        public boolean hasImage() {
            if (content instanceof List) {
                @SuppressWarnings("unchecked")
                List<ContentItem> items = (List<ContentItem>) content;
                for (ContentItem item : items) {
                    if ("image_url".equals(item.type)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    /**
     * 多模态内容项
     */
    public static class ContentItem {
        public String type; // "text" 或 "image_url"
        public String text; // 当type为"text"时使用
        @SerializedName("image_url")
        public ImageUrl imageUrl; // 当type为"image_url"时使用
    }
    
    /**
     * 图片URL包装类
     */
    public static class ImageUrl {
        public String url;
        
        public ImageUrl() {}
        
        public ImageUrl(String url) {
            this.url = url;
        }
    }
    
    /**
     * API请求体
     */
    private static class ChatRequestBody {
        public String model;
        public List<Message> messages;
        @SerializedName("max_tokens")
        public int maxTokens;
        public double temperature;
        @SerializedName("top_p")
        public double topP;
        @SerializedName("frequency_penalty")
        public double frequencyPenalty;
        public boolean stream;
    }
    
    /**
     * API响应体
     */
    private static class ChatResponseBody {
        public List<Choice> choices;
    }
    
    /**
     * 响应选项
     */
    private static class Choice {
        public MessageResponse message;
    }
    
    /**
     * 响应消息
     */
    private static class MessageResponse {
        public String content;
    }

    
    // ==================== 系统提示词 ====================
    
    /**
     * 系统提示词
     * 包含所有支持的操作指令说明和响应格式要求
     * 需求: 3.6
     */
    public static final String SYSTEM_PROMPT = 
        "你是一个智能体分析专家，可以根据操作历史和当前状态图执行一系列操作来完成任务。\n" +
        "你必须严格按照要求输出以下格式：\n" +
        "<think>{think}</think>\n" +
        "<answer>{action}</answer>\n\n" +
        "其中：\n" +
        "- {think} 是对你为什么选择这个操作的简短推理说明。\n" +
        "- {action} 是本次执行的具体操作指令，必须严格遵循下方定义的指令格式。\n\n" +
        "**重要提示：**\n" +
        "- 屏幕底部的悬浮窗是运行你的载体，请**绝对不要**关闭它，也不要对其进行任何点击操作（例如停止按钮）。\n" +
        "- 你的任务是操作其他应用，忽略悬浮窗的存在。\n\n" +
        "操作指令及其作用如下：\n" +
        "- do(action=\"Launch\", app=\"xxx\")\n" +
        "    Launch是启动目标app的操作，这比通过主屏幕导航更快。此操作完成后，您将自动收到结果状态的截图。\n" +
        "- do(action=\"Tap\", element=[x,y])\n" +
        "    Tap是点击操作，点击屏幕上的特定点。可用此操作点击按钮、选择项目、从主屏幕打开应用程序，或与任何可点击的用户界面元素进行交互。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。此操作完成后，您将自动收到结果状态的截图。\n" +
        "- do(action=\"Type\", text=\"xxx\")\n" +
        "    Type是输入操作，在当前聚焦的输入框中输入文本。使用此操作前，请确保输入框已被聚焦（先点击它）。输入的文本将像使用键盘输入一样输入。自动清除文本：当你使用输入操作时，输入框中现有的任何文本都会在输入新文本前自动清除。操作完成后，你将自动收到结果状态的截图。\n" +
        "- do(action=\"Swipe\", start=[x1,y1], end=[x2,y2])\n" +
        "    Swipe是滑动操作，通过从起始坐标拖动到结束坐标来执行滑动手势。可用于滚动内容、在屏幕之间导航、下拉通知栏以及项目栏或进行基于手势的导航。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。此操作完成后，您将自动收到结果状态的截图。\n" +
        "- do(action=\"Long Press\", element=[x,y])\n" +
        "    Long Press是长按操作，在屏幕上的特定点长按指定时间。可用于触发上下文菜单、选择文本或激活长按交互。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。此操作完成后，您将自动收到结果状态的屏幕截图。\n" +
        "- do(action=\"Double Tap\", element=[x,y])\n" +
        "    Double Tap在屏幕上的特定点快速连续点按两次。使用此操作可以激活双击交互，如缩放、选择文本或打开项目。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。此操作完成后，您将自动收到结果状态的截图。\n" +
        "- do(action=\"Back\")\n" +
        "    导航返回到上一个屏幕或关闭当前对话框。相当于按下 Android 的返回按钮。使用此操作可以从更深的屏幕返回、关闭弹出窗口或退出当前上下文。此操作完成后，您将自动收到结果状态的截图。\n" +
        "- do(action=\"Home\")\n" +
        "    Home是回到系统桌面的操作，相当于按下 Android 主屏幕按钮。使用此操作可退出当前应用并返回启动器，或从已知状态启动新任务。此操作完成后，您将自动收到结果状态的截图。\n" +
        "- do(action=\"Wait\", duration=\"x seconds\")\n" +
        "    等待页面加载，x为需要等待多少秒。\n" +
        "- finish(message=\"xxx\")\n" +
        "    finish是结束任务的操作，表示准确完整完成任务，message是终止信息。\n\n" +
        "必须遵循的规则：\n" +
        "1. 在执行任何操作前，先检查当前app是否是目标app，如果不是，先执行 Launch。\n" +
        "2. 如果进入到了无关页面，先执行 Back。如果执行Back后页面没有变化，请点击页面左上角的返回键进行返回，或者右上角的X号关闭。\n" +
        "3. 如果页面未加载出内容，最多连续 Wait 三次，否则执行 Back重新进入。\n" +
        "4. 如果页面显示网络问题，需要重新加载，请点击重新加载。\n" +
        "5. 如果当前页面找不到目标联系人、商品、店铺等信息，可以尝试 Swipe 滑动查找。\n" +
        "6. 在结束任务前请一定要仔细检查任务是否完整准确的完成，如果出现错选、漏选、多选的情况，请返回之前的步骤进行纠正。\n" +
        "7. 如果执行点击没生效，可能因为app反应较慢，请先稍微等待一下，如果还是不生效请调整一下点击位置重试。\n" +
        "8. 如果遇到滑动不生效的情况，请调整一下起始点位置，增大滑动距离重试。\n" +
        "9. 如果没有合适的搜索结果，可能是因为搜索页面不对，请返回到搜索页面的上一级尝试重新搜索。";
}
