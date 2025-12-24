package com.example.mybighomework.autoglm.model;

import java.util.List;

public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private double temperature;
    private int maxTokens;
    private boolean stream;

    public ChatRequest() {
        this.temperature = 0.7;
        this.maxTokens = 2000;
        this.stream = false;
    }

    // Getters and Setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
