package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnReplyClickListener {
        void onReplyClick(CommentItem comment);
        void onInlineReplySubmit(CommentItem comment, String replyText);
        void onInlineReplyCancel();
    }

    private final Context context;
    private final ArrayList<CommentItem> comments;
    private final OnReplyClickListener listener;

    public CommentAdapter(Context context, ArrayList<CommentItem> comments, OnReplyClickListener listener) {
        this.context = context;
        this.comments = comments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem comment = comments.get(position);

        holder.tvUsername.setText(comment.getSenderUsername());
        holder.tvContent.setText(comment.getContent());
        holder.tvDate.setText(formatDate(comment.getCreatedAt()));

        holder.replyBoxContainer.setVisibility(comment.isReplyTarget() ? View.VISIBLE : View.GONE);

        holder.btnReply.setOnClickListener(v -> listener.onReplyClick(comment));
        holder.btnCancelInlineReply.setOnClickListener(v -> listener.onInlineReplyCancel());

        holder.btnSendInlineReply.setOnClickListener(v -> {
            String replyText = holder.etInlineReply.getText().toString().trim();
            listener.onInlineReplySubmit(comment, replyText);
        });

        if (comment.getReplies().isEmpty()) {
            holder.btnShowReplies.setVisibility(View.GONE);
            holder.repliesContainer.setVisibility(View.GONE);
            holder.repliesInnerContainer.removeAllViews();
        } else {
            holder.btnShowReplies.setVisibility(View.VISIBLE);

            if (comment.isRepliesExpanded()) {
                holder.btnShowReplies.setText("Hide replies (" + comment.getReplies().size() + ")");
                holder.repliesContainer.setVisibility(View.VISIBLE);
                renderReplies(holder.repliesInnerContainer, comment.getReplies());
            } else {
                holder.btnShowReplies.setText("Show replies (" + comment.getReplies().size() + ")");
                holder.repliesContainer.setVisibility(View.GONE);
                holder.repliesInnerContainer.removeAllViews();
            }

            holder.btnShowReplies.setOnClickListener(v -> {
                comment.setRepliesExpanded(!comment.isRepliesExpanded());

                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos);
                }
            });
        }
    }

    private void renderReplies(LinearLayout container, ArrayList<CommentItem> replies) {
        container.removeAllViews();

        for (int i = 0; i < replies.size(); i++) {
            CommentItem reply = replies.get(i);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(0, 10, 0, 12);

            LinearLayout header = new LinearLayout(context);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView username = new TextView(context);
            username.setText(reply.getSenderUsername());
            username.setTextColor(0xFF111111);
            username.setTextSize(14);
            username.setTypeface(null, android.graphics.Typeface.BOLD);
            username.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1
            ));

            TextView date = new TextView(context);
            date.setText(formatDate(reply.getCreatedAt()));
            date.setTextColor(0xFF888888);
            date.setTextSize(10);

            header.addView(username);
            header.addView(date);

            TextView content = new TextView(context);
            content.setText(reply.getContent());
            content.setTextColor(0xFF333333);
            content.setTextSize(14);
            content.setPadding(0, 6, 0, 8);

            layout.addView(header);
            layout.addView(content);

            if (i < replies.size() - 1) {
                View divider = new View(context);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                ));
                divider.setBackgroundColor(0xFFE3E3E3);
                layout.addView(divider);
            }

            container.addView(layout);
        }
    }

    private String formatDate(String raw) {
        try {
            if (raw == null || raw.trim().isEmpty()) {
                return "";
            }

            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

            Date date = input.parse(raw);
            return date != null ? output.format(date) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public void setReplyTarget(Integer commentId) {
        for (CommentItem comment : comments) {
            comment.setReplyTarget(commentId != null && comment.getCommentId() == commentId);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvContent;
        TextView tvDate;
        TextView btnReply;
        TextView btnShowReplies;

        LinearLayout replyBoxContainer;
        LinearLayout repliesContainer;
        LinearLayout repliesInnerContainer;

        EditText etInlineReply;
        Button btnCancelInlineReply;
        Button btnSendInlineReply;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tv_comment_username);
            tvContent = itemView.findViewById(R.id.tv_comment_content);
            tvDate = itemView.findViewById(R.id.tv_comment_date);
            btnReply = itemView.findViewById(R.id.btn_reply_comment);
            btnShowReplies = itemView.findViewById(R.id.btn_show_replies);

            replyBoxContainer = itemView.findViewById(R.id.reply_box_container);
            repliesContainer = itemView.findViewById(R.id.replies_container);
            repliesInnerContainer = itemView.findViewById(R.id.replies_inner_container);

            etInlineReply = itemView.findViewById(R.id.et_inline_reply);
            btnCancelInlineReply = itemView.findViewById(R.id.btn_cancel_inline_reply);
            btnSendInlineReply = itemView.findViewById(R.id.btn_send_inline_reply);
        }
    }
}