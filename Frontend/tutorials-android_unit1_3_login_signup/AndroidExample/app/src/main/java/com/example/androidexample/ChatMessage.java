package com.example.androidexample;

public class ChatMessage {
    private final int msgId;
    private final String sender;
    private final String content;
    private final String dateSent;
    private final Integer parentMessageId;
    private final boolean sentByMe;

    public ChatMessage(int msgId,
                       String sender,
                       String content,
                       String dateSent,
                       Integer parentMessageId,
                       boolean sentByMe) {
        this.msgId = msgId;
        this.sender = sender;
        this.content = content;
        this.dateSent = dateSent;
        this.parentMessageId = parentMessageId;
        this.sentByMe = sentByMe;
    }

    public int getMsgId() {
        return msgId;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getDateSent() {
        return dateSent;
    }

    public Integer getParentMessageId() {
        return parentMessageId;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }
}