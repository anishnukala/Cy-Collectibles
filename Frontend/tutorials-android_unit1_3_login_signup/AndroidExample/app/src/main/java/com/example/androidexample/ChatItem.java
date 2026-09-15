package com.example.androidexample;

public class ChatItem {
    private final int channelId;
    private final String name;
    private final String type;
    private final boolean unread;
    private final String lastMessage;
    private final String lastMessageDate;
    private final String memberSummary;
    private final long sortTime;

    public ChatItem(int channelId,
                    String name,
                    String type,
                    boolean unread,
                    String lastMessage,
                    String lastMessageDate,
                    String memberSummary,
                    long sortTime) {
        this.channelId = channelId;
        this.name = name;
        this.type = type;
        this.unread = unread;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
        this.memberSummary = memberSummary;
        this.sortTime = sortTime;
    }

    public int getChannelId() {
        return channelId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isUnread() {
        return unread;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public String getMemberSummary() {
        return memberSummary;
    }

    public long getSortTime() {
        return sortTime;
    }
}