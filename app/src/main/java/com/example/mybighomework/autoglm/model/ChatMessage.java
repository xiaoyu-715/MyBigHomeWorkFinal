package com.example.mybighomework.autoglm.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable {
    private String id;
    private String role; // "user" 或 "assistant" 或 "system"
    private String content;
    private long timestamp;
    private MessageType type;
    private String metadata;

    public enum MessageType {
        TEXT, IMAGE, AUDIO
    }

    public ChatMessage() {
        this.id = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.type = MessageType.TEXT;
    }

    public ChatMessage(String role, String content) {
        this();
        this.role = role;
        this.content = content;
    }

    protected ChatMessage(Parcel in) {
        id = in.readString();
        role = in.readString();
        content = in.readString();
        timestamp = in.readLong();
        type = MessageType.valueOf(in.readString());
        metadata = in.readString();
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(role);
        dest.writeString(content);
        dest.writeLong(timestamp);
        dest.writeString(type.name());
        dest.writeString(metadata);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
