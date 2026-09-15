package com.example.androidexample;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

public class CreateMenuHelper {

    public static void showCreateMenu(Activity activity,
                                      View anchor,
                                      long userId,
                                      String username,
                                      String userType) {

        PopupMenu popupMenu = new PopupMenu(activity, anchor);

        popupMenu.getMenu().add(0, 1, 0, "New Group");
        popupMenu.getMenu().add(0, 2, 1, "New Chat");

        popupMenu.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(activity, NewChatActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            intent.putExtra("USERTYPE", userType);

            if (item.getItemId() == 1) {
                intent.putExtra("MODE", "GROUP");
                activity.startActivity(intent);
                return true;
            } else if (item.getItemId() == 2) {
                intent.putExtra("MODE", "DIRECT");
                activity.startActivity(intent);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}