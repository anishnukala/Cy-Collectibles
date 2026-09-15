package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageLongClickListener {
        void onMessageLongClick(ChatMessage message);
    }

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final OnMessageLongClickListener listener;

    public ChatMessageAdapter(List<ChatMessage> messages, OnMessageLongClickListener listener) {
        this.messages = messages;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSentByMe() ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String parentPreview = findParentPreview(message.getParentMessageId());
        String dateTime = message.getDateSent();

        if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder h = (SentMessageViewHolder) holder;
            h.messageText.setText(message.getContent());

            if (parentPreview != null) {
                h.replyPreview.setVisibility(View.VISIBLE);
                h.replyPreview.setText(parentPreview);
            } else {
                h.replyPreview.setVisibility(View.GONE);
            }

            if (dateTime != null && !dateTime.trim().isEmpty()) {
                h.timeText.setVisibility(View.VISIBLE);
                h.timeText.setText(dateTime);
            } else {
                h.timeText.setVisibility(View.GONE);
            }

            h.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onMessageLongClick(message);
                return true;
            });

        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder h = (ReceivedMessageViewHolder) holder;
            h.senderText.setText(message.getSender());
            h.messageText.setText(message.getContent());

            if (parentPreview != null) {
                h.replyPreview.setVisibility(View.VISIBLE);
                h.replyPreview.setText(parentPreview);
            } else {
                h.replyPreview.setVisibility(View.GONE);
            }

            if (dateTime != null && !dateTime.trim().isEmpty()) {
                h.timeText.setVisibility(View.VISIBLE);
                h.timeText.setText(dateTime);
            } else {
                h.timeText.setVisibility(View.GONE);
            }

            h.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onMessageLongClick(message);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    private String findParentPreview(Integer parentMessageId) {
        if (parentMessageId == null) return null;

        for (ChatMessage msg : messages) {
            if (msg.getMsgId() == parentMessageId) {

                String sender = msg.getSender() == null ? "" : msg.getSender().trim();
                String text = msg.getContent() == null ? "" : msg.getContent().trim();

                if (sender.isEmpty() && text.isEmpty()) return null;

                return "Replying to: " + sender + " : " + text;
            }
        }

        return null;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView replyPreview, messageText, timeText;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            replyPreview = itemView.findViewById(R.id.txt_sent_reply_preview);
            messageText = itemView.findViewById(R.id.txt_sent_message);
            timeText = itemView.findViewById(R.id.txt_sent_time);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderText, replyPreview, messageText, timeText;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.txt_received_sender);
            replyPreview = itemView.findViewById(R.id.txt_received_reply_preview);
            messageText = itemView.findViewById(R.id.txt_received_message);
            timeText = itemView.findViewById(R.id.txt_received_time);
        }
    }
}