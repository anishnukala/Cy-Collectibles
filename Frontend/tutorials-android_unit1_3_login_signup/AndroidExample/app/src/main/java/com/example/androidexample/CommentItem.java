package com.example.androidexample;

import java.util.ArrayList;

public class CommentItem {
    private final int commentId;
    private final String senderUsername;
    private final String content;
    private final Integer parentCommentId;
    private final String createdAt;
    private final int depth;

    private boolean isReplyTarget;
    private boolean repliesExpanded;

    private final ArrayList<CommentItem> replies;

    public CommentItem(int commentId, String senderUsername, String content,
                       Integer parentCommentId, String createdAt, int depth) {
        this.commentId = commentId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
        this.depth = depth;
        this.isReplyTarget = false;
        this.repliesExpanded = false;
        this.replies = new ArrayList<>();
    }

    public int getCommentId() {
        return commentId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getContent() {
        return content;
    }

    public Integer getParentCommentId() {
        return parentCommentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isReplyTarget() {
        return isReplyTarget;
    }

    public void setReplyTarget(boolean replyTarget) {
        isReplyTarget = replyTarget;
    }

    public boolean isRepliesExpanded() {
        return repliesExpanded;
    }

    public void setRepliesExpanded(boolean repliesExpanded) {
        this.repliesExpanded = repliesExpanded;
    }

    public ArrayList<CommentItem> getReplies() {
        return replies;
    }

    public void addReply(CommentItem reply) {
        replies.add(reply);
    }
}