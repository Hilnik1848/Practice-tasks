package com.example.travenor.Models;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("id")
    private int id;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("message_text")
    private String messageText;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("chat_id")
    private int chatId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getChatId() {
        return chatId;
    }
}