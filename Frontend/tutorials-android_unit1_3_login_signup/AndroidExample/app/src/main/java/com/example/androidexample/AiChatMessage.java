package com.example.androidexample;

public class AiChatMessage {
    private final int aiMessageId;
    private final String role;
    private final String content;
    private final String createdAt;

    public AiChatMessage(int aiMessageId, String role, String content, String createdAt) {
        this.aiMessageId = aiMessageId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getAiMessageId() {
        return aiMessageId;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isSentByMe() {
        return "user".equalsIgnoreCase(role);
    }
}