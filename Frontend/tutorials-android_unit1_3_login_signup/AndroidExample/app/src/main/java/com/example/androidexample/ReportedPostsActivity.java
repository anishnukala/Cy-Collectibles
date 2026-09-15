package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ReportedPostsActivity extends AppCompatActivity {

    private long adminId = -1;

    private TextView btnClose;
    private TextView tvEmptyReportedPosts;
    private LinearLayout reportedPostsContainer;

    private JSONArray reportedPosts = new JSONArray();

    private static String baseUrl() {
        return ApiConfig.BASE_URL;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_posts);

        btnClose = findViewById(R.id.btn_close);
        tvEmptyReportedPosts = findViewById(R.id.tv_empty_reported_posts);
        reportedPostsContainer = findViewById(R.id.reported_posts_container);

        adminId = getIntent().getLongExtra("USER_ID", -1);

        btnClose.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReportedPosts();
    }

    private void loadReportedPosts() {
        String url = baseUrl() + "/admin/postings/reported";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("REPORTED_POSTS_COUNT", "Count: " + response.length());
                    reportedPosts = response;
                    bindCards();
                },
                error -> {
                    reportedPostsContainer.removeAllViews();
                    tvEmptyReportedPosts.setText("Failed to load reported posts");
                    tvEmptyReportedPosts.setVisibility(TextView.VISIBLE);

                    if (error.networkResponse != null) {
                        Log.e("REPORTED_POSTS_ERROR", "Code: " + error.networkResponse.statusCode);
                    } else {
                        Log.e("REPORTED_POSTS_ERROR", "Network error", error);
                    }

                    Toast.makeText(this, "Failed to load reported posts", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void bindCards() {
        reportedPostsContainer.removeAllViews();

        if (reportedPosts == null || reportedPosts.length() == 0) {
            tvEmptyReportedPosts.setText("No reported posts found");
            tvEmptyReportedPosts.setVisibility(TextView.VISIBLE);
            return;
        }

        tvEmptyReportedPosts.setVisibility(TextView.GONE);

        try {
            List<JSONObject> postsList = new ArrayList<>();

            for (int i = 0; i < reportedPosts.length(); i++) {
                postsList.add(reportedPosts.getJSONObject(i));
            }

            postsList.sort((a, b) -> {
                String dateA = a.optString("date", "");
                String dateB = b.optString("date", "");
                return dateB.compareTo(dateA);
            });

            for (JSONObject post : postsList) {
                int postingId = post.optInt("postingId", -1);
                int sellerId = post.optInt("sellerId", -1);
                String title = post.optString("title", "Untitled");
                String genre = post.optString("genre", "Unknown");
                int price = post.optInt("price", 0);
                String description = post.optString("description", "");
                String imageUrl = post.optString("imageUrl", "");
                String date = post.optString("date", "Unknown");

                addReportedPostCard(postingId, sellerId, title, genre, price, description, imageUrl, date);
            }

        } catch (Exception e) {
            Log.e("REPORTED_POSTS_PARSE", "Failed to parse reported posts", e);
            reportedPostsContainer.removeAllViews();
            tvEmptyReportedPosts.setText("Failed to parse reported posts");
            tvEmptyReportedPosts.setVisibility(TextView.VISIBLE);
            Toast.makeText(this, "Failed to parse reported posts", Toast.LENGTH_SHORT).show();
        }
    }

    private void addReportedPostCard(
            int postingId,
            int sellerId,
            String title,
            String genre,
            int price,
            String description,
            String imageUrl,
            String date
    ) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout card = (LinearLayout) inflater.inflate(
                R.layout.item_reported_post,
                reportedPostsContainer,
                false
        );

        TextView tvTitle = card.findViewById(R.id.tv_report_title);
        TextView tvDetails = card.findViewById(R.id.tv_report_details);
        AppCompatButton btnViewPost = card.findViewById(R.id.btn_view_post);
        AppCompatButton btnVerifyPost = card.findViewById(R.id.btn_verify_post);
        AppCompatButton btnBanPost = card.findViewById(R.id.btn_delete_post);

        tvTitle.setText(title);
        tvDetails.setText(
                "Seller ID: " + sellerId +
                        "\nGenre: " + genre +
                        "\nPrice: $" + price +
                        "\nDate Posted: " + date
        );

        btnViewPost.setOnClickListener(v -> {
            if (postingId == -1) {
                Toast.makeText(this, "Invalid posting id", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ReportedPostsActivity.this, ItemDetailActivity.class);
            intent.putExtra("postingId", postingId);
            intent.putExtra("title", title);
            intent.putExtra("genre", genre);
            intent.putExtra("price", price);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        btnVerifyPost.setOnClickListener(v -> verifyPost(postingId));
        btnBanPost.setOnClickListener(v -> showBanConfirmDialog(postingId));

        reportedPostsContainer.addView(card);
    }

    private void verifyPost(int postingId) {
        String url = baseUrl() + "/verify/posting/" + adminId + "/" + postingId;

        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    Toast.makeText(this, "Post verified", Toast.LENGTH_SHORT).show();
                    loadReportedPosts();
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("VERIFY_POST_ERROR", "Code: " + error.networkResponse.statusCode);
                    } else {
                        Log.e("VERIFY_POST_ERROR", "Network error", error);
                    }
                    Toast.makeText(this, "Failed to verify post", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showBanConfirmDialog(int postingId) {
        new AlertDialog.Builder(this)
                .setTitle("Ban post")
                .setMessage("Ban this reported post?")
                .setPositiveButton("Yes", (dialog, which) -> banPost(postingId))
                .setNegativeButton("No", null)
                .show();
    }

    private void banPost(int postingId) {
        String url = baseUrl() + "/ban/posting/" + adminId + "/" + postingId;

        JSONObject body = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Post banned successfully", Toast.LENGTH_SHORT).show();
                    loadReportedPosts();
                },
                error -> {
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;

                        if (code == 403) {
                            Toast.makeText(this, "Only admin can ban posts", Toast.LENGTH_SHORT).show();
                        } else if (code == 404) {
                            Toast.makeText(this, "Admin or post not found", Toast.LENGTH_SHORT).show();
                        } else if (code == 409) {
                            Toast.makeText(this, "Post already banned or cannot be banned", Toast.LENGTH_SHORT).show();
                        } else if (code == 415) {
                            Toast.makeText(this, "Unsupported media type", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Ban failed (" + code + ")", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = response.data == null
                            ? ""
                            : new String(
                            response.data,
                            Charset.forName(
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8")
                            )
                    );

                    if (jsonString.isEmpty()) {
                        return Response.success(
                                new JSONObject(),
                                HttpHeaderParser.parseCacheHeaders(response)
                        );
                    }

                    return Response.success(
                            new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response)
                    );
                } catch (Exception e) {
                    return Response.success(
                            new JSONObject(),
                            HttpHeaderParser.parseCacheHeaders(response)
                    );
                }
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}