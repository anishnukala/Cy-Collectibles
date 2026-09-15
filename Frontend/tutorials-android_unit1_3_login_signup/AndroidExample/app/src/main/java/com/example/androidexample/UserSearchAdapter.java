package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClicked(UserSearchItem user);
    }

    private final ArrayList<UserSearchItem> users;
    private final OnUserClickListener listener;
    private final boolean multiSelect;

    private int singleSelectedUserId = -1;
    private ArrayList<UserSearchItem> selectedUsers = new ArrayList<>();

    public UserSearchAdapter(ArrayList<UserSearchItem> users, OnUserClickListener listener, boolean multiSelect) {
        this.users = users;
        this.listener = listener;
        this.multiSelect = multiSelect;
    }

    public void setSingleSelectedUserId(int singleSelectedUserId) {
        this.singleSelectedUserId = singleSelectedUserId;
    }

    public void setSelectedUsers(ArrayList<UserSearchItem> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserSearchItem user = users.get(position);

        holder.txtUsername.setText(user.getUsername());
        holder.txtDetails.setText(user.getUserType() + " • " + user.getEmail());
        holder.avatarImage.setImageResource(R.drawable.direct);

        boolean isSelected;
        if (multiSelect) {
            isSelected = false;
            for (UserSearchItem selected : selectedUsers) {
                if (selected.getId() == user.getId()) {
                    isSelected = true;
                    break;
                }
            }
        } else {
            isSelected = user.getId() == singleSelectedUserId;
        }

        holder.selectionView.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClicked(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView txtUsername;
        TextView txtDetails;
        View selectionView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.img_user_avatar);
            txtUsername = itemView.findViewById(R.id.txt_user_username);
            txtDetails = itemView.findViewById(R.id.txt_user_details);
            selectionView = itemView.findViewById(R.id.view_selected_marker);
        }
    }
}