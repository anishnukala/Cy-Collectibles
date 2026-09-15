package com.example.androidexample;

public class UserSearchItem {
    private final int id;
    private final String username;
    private final String email;
    private final String userType;

    public UserSearchItem(int id, String username, String email, String userType) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userType = userType;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getUserType() { return userType; }
}