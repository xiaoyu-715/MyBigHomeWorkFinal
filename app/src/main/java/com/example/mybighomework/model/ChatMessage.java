package com.example.mybighomework.model;

/**
 * 聊天消息模型类
 */
public class ChatMessage {
    
    // 消息类型
    public static final int TYPE_SENT = 0;      // 发送的消息
    public static final int TYPE_RECEIVED = 1;  // 接收的消息
    
    private int type;           // 消息类型
    private String content;     // 消息内容
    private long timestamp;     // 时间戳
    private boolean showGeneratePlanButton; // 是否显示生成学习计划按钮
    
    public ChatMessage() {
    }
    
    public ChatMessage(int type, String content, long timestamp) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.showGeneratePlanButton = false;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isShowGeneratePlanButton() {
        return showGeneratePlanButton;
    }
    
    public void setShowGeneratePlanButton(boolean showGeneratePlanButton) {
        this.showGeneratePlanButton = showGeneratePlanButton;
    }
}

