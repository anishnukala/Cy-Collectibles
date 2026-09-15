package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatItem> items;
    private final long userId;
    private final String username;
    private final String userType;

    public ChatListAdapter(Context context, List<ChatItem> items, long userId, String username, String userType) {
        this.context = context;
        this.items = items;
        this.userId = userId;
        this.username = username;
        this.userType = userType;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem item = items.get(position);

        holder.nameText.setText(item.getName());

        String lastMessage = item.getLastMessage();
        holder.messageText.setText(
                lastMessage == null || lastMessage.trim().isEmpty()
                        ? "No messages yet"
                        : lastMessage
        );

        String time = item.getLastMessageDate();
        holder.timeText.setText(
                time == null || time.trim().isEmpty() || "null".equalsIgnoreCase(time)
                        ? ""
                        : time
        );

        boolean isGroup = "GROUP".equalsIgnoreCase(item.getType());

        if (isGroup) {
            holder.avatarImage.setImageResource(R.drawable.group);
        } else {
            holder.avatarImage.setImageResource(R.drawable.direct);
        }

        String members = item.getMemberSummary();
        if (isGroup && members != null && !members.trim().isEmpty()) {
            holder.membersText.setVisibility(View.VISIBLE);
            holder.membersText.setText(members);
        } else {
            holder.membersText.setVisibility(View.GONE);
        }

        holder.unreadDot.setVisibility(item.isUnread() ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatScreenActivity.class);
            intent.putExtra("CHANNEL_ID", item.getChannelId());
            intent.putExtra("CHAT_NAME", item.getName());
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView nameText;
        TextView membersText;
        TextView messageText;
        TextView timeText;
        View unreadDot;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.img_chat_avatar);
            nameText = itemView.findViewById(R.id.txt_chat_name);
            membersText = itemView.findViewById(R.id.txt_chat_members);
            messageText = itemView.findViewById(R.id.txt_last_message);
            timeText = itemView.findViewById(R.id.txt_last_message_time);
            unreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}