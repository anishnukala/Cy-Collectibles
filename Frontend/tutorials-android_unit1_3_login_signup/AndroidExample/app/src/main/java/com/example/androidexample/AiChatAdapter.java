package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.noties.markwon.Markwon;

public class AiChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final List<AiChatMessage> messages;
    private Markwon markwon;

    public AiChatAdapter(List<AiChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSentByMe() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (markwon == null) {
            markwon = Markwon.create(parent.getContext());
        }

        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ai_message_sent, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ai_message_received, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AiChatMessage message = messages.get(position);
        String content = message.getContent();

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).messageText.setText(content);
        } else if (holder instanceof AiViewHolder) {
            markwon.setMarkdown(((AiViewHolder) holder).messageText, content);
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.txt_ai_sent_message);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.txt_ai_received_message);
        }
    }
}