package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userIdTv, usernameTv, userTypeTv, emailTv, flagCountTv;
    private Button editBtn, deleteBtn, logoutBtn, btnGenreSelect;
    private TextView closeBtn;

    private long userId = -1;

    private final Set<String> selectedFavourites = new HashSet<>();
    private final List<String> allGenres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userIdTv = findViewById(R.id.profile_userid);
        usernameTv = findViewById(R.id.profile_username);
        userTypeTv = findViewById(R.id.profile_usertype);
        emailTv = findViewById(R.id.profile_email);
        flagCountTv = findViewById(R.id.profile_flagcount);

        editBtn = findViewById(R.id.btn_edit_user);
        deleteBtn = findViewById(R.id.btn_delete_account);
        logoutBtn = findViewById(R.id.btn_logout);
        closeBtn = findViewById(R.id.btn_close);
        btnGenreSelect = findViewById(R.id.btn_genre_select);

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> finish());
        }

        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> goToLoginAndClearSession());
        }

        if (btnGenreSelect != null) {
            btnGenreSelect.setOnClickListener(v -> showGenreMultiSelectDialog());
        }

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "No user id saved. Please login again.", Toast.LENGTH_LONG).show();
            goToLoginAndClearSession();
            return;
        }

        userIdTv.setText("User ID: " + userId);

        fetchUserProfile(userId);
        loadFavouritesAndGenres();

        editBtn.setOnClickListener(v -> {
            Intent i = new Intent(UserProfileActivity.this, EditUserActivity.class);
            i.putExtra("USER_ID", userId);
            startActivity(i);
        });

        deleteBtn.setOnClickListener(v -> showDeleteConfirmation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != -1) {
            fetchUserProfile(userId);
            loadFavouritesAndGenres();
        }
    }

    private void fetchUserProfile(long id) {
        String url = ApiConfig.BASE_URL + "/users/" + id;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String username = response.optString("username", "N/A");
                    String userType = response.optString("userType", "N/A");
                    String emailId = response.optString("emailId", "N/A");
                    int flagCount = response.optInt("flagCount", -1);
                    long responseId = response.optLong("id", id);

                    userIdTv.setText("User ID: " + responseId);
                    usernameTv.setText("Username: " + username);
                    userTypeTv.setText("User Type: " + userType);
                    emailTv.setText("Email: " + emailId);
                    flagCountTv.setText("Flag Count: " + (flagCount == -1 ? "N/A" : flagCount));
                },
                error -> {
                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    if (status == 404) {
                        Toast.makeText(this, "User not found (404)", Toast.LENGTH_LONG).show();
                    } else if (status == 401 || status == 403) {
                        Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        goToLoginAndClearSession();
                    } else if (status == -1) {
                        String msg = (error.getMessage() != null) ? error.getMessage() : "Network error";
                        Toast.makeText(this, "Failed to load profile: " + msg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to load profile (" + status + ")", Toast.LENGTH_LONG).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void loadFavouritesAndGenres() {
        String favUrl = ApiConfig.BASE_URL + "/favourites/" + userId;

        JsonArrayRequest favReq = new JsonArrayRequest(
                Request.Method.GET,
                favUrl,
                null,
                response -> {
                    selectedFavourites.clear();

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.optJSONObject(i);
                        if (obj != null) {
                            String category = obj.optString("category", "");
                            if (!category.isEmpty()) {
                                selectedFavourites.add(category);
                            }
                        }
                    }

                    fetchGenres();
                },
                error -> {
                    selectedFavourites.clear();
                    fetchGenres();
                }
        );

        Volley.newRequestQueue(this).add(favReq);
    }

    private void fetchGenres() {
        String url = ApiConfig.BASE_URL + "/postings/genre";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    allGenres.clear();

                    for (int i = 0; i < response.length(); i++) {
                        String genre = response.optString(i, "");
                        if (!genre.isEmpty()) {
                            allGenres.add(genre);
                        }
                    }

                    updateGenreButtonText();
                },
                error -> Toast.makeText(this, "Failed to load genres", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void showGenreMultiSelectDialog() {
        if (allGenres.isEmpty()) {
            Toast.makeText(this, "Genres not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] genreArray = allGenres.toArray(new String[0]);
        boolean[] checkedItems = new boolean[genreArray.length];

        Set<String> tempSelection = new HashSet<>(selectedFavourites);

        for (int i = 0; i < genreArray.length; i++) {
            checkedItems[i] = selectedFavourites.contains(genreArray[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Favourite Genres")
                .setMultiChoiceItems(genreArray, checkedItems, (dialog, which, isChecked) -> {
                    String genre = genreArray[which];
                    if (isChecked) {
                        tempSelection.add(genre);
                    } else {
                        tempSelection.remove(genre);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Save", (dialog, which) -> saveFavouriteChanges(tempSelection))
                .show();
    }

    private void saveFavouriteChanges(Set<String> newSelection) {
        Set<String> toAdd = new HashSet<>(newSelection);
        toAdd.removeAll(selectedFavourites);

        Set<String> toRemove = new HashSet<>(selectedFavourites);
        toRemove.removeAll(newSelection);

        for (String genre : toAdd) {
            addFavourite(genre);
        }

        for (String genre : toRemove) {
            removeFavourite(genre);
        }

        selectedFavourites.clear();
        selectedFavourites.addAll(newSelection);
        updateGenreButtonText();

        Toast.makeText(this, "Favourite genres updated", Toast.LENGTH_SHORT).show();
    }

    private void addFavourite(String category) {
        try {
            String encodedCategory = URLEncoder.encode(category, "UTF-8");
            String url = ApiConfig.BASE_URL + "/favourites/" + userId + "/" + encodedCategory;

            StringRequest req = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> { },
                    error -> { }
            );

            Volley.newRequestQueue(this).add(req);
        } catch (Exception ignored) {
        }
    }

    private void removeFavourite(String category) {
        try {
            String encodedCategory = URLEncoder.encode(category, "UTF-8");
            String url = ApiConfig.BASE_URL + "/favourites/" + userId + "/" + encodedCategory;

            StringRequest req = new StringRequest(
                    Request.Method.DELETE,
                    url,
                    response -> { },
                    error -> { }
            );

            Volley.newRequestQueue(this).add(req);
        } catch (Exception ignored) {
        }
    }

    private void updateGenreButtonText() {
        if (btnGenreSelect == null) return;

        if (selectedFavourites.isEmpty()) {
            btnGenreSelect.setText("Select Genres");
            return;
        }

        List<String> selectedList = new ArrayList<>(selectedFavourites);

        if (selectedList.size() == 1) {
            btnGenreSelect.setText(selectedList.get(0));
        } else {
            btnGenreSelect.setText(selectedList.size() + " Genres Selected");
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount(userId))
                .show();
    }

    private void deleteAccount(long id) {
        String url = ApiConfig.BASE_URL + "/users/" + id;

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    goToLoginAndClearSession();
                },
                error -> {
                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    if (status == 404) {
                        Toast.makeText(this, "User not found (404)", Toast.LENGTH_LONG).show();
                    } else if (status == 401 || status == 403) {
                        Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                        goToLoginAndClearSession();
                    } else if (status == -1) {
                        String msg = (error.getMessage() != null) ? error.getMessage() : "Network error";
                        Toast.makeText(this, "Delete failed: " + msg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Delete failed (" + status + ")", Toast.LENGTH_LONG).show();
                    }
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void goToLoginAndClearSession() {
        NotificationSocketManager.getInstance().disconnect();

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}