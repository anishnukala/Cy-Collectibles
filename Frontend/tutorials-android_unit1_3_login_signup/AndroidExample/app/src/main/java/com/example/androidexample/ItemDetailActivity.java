package com.example.androidexample;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import io.noties.markwon.Markwon;

public class ItemDetailActivity extends AppCompatActivity implements CommentAdapter.OnReplyClickListener {

    private TextView btnClose;
    private TextView itemTitle, itemPrice, itemGenre, itemDescription, itemSellerUsername, tvAiSummary;
    private ImageView itemImage;
    private Button btnBuyNow, btnChat, btnReport, btnPostComment, btnAskAi;
    private EditText etCommentInput;
    private RecyclerView rvComments;

    private int postingId = -1;
    private long userId = -1;
    private int sellerId = -1;

    private Integer selectedParentCommentId = null;
    private String selectedReplyUsername = null;

    private final ArrayList<CommentItem> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        btnClose = findViewById(R.id.btn_close);
        itemImage = findViewById(R.id.item_image);
        itemTitle = findViewById(R.id.item_title);
        itemPrice = findViewById(R.id.item_price);
        itemGenre = findViewById(R.id.item_genre);
        itemDescription = findViewById(R.id.item_description);
        itemSellerUsername = findViewById(R.id.item_seller_username);

        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnChat = findViewById(R.id.btn_chat);
        btnReport = findViewById(R.id.btn_report);
        btnPostComment = findViewById(R.id.btn_post_comment);
        btnAskAi = findViewById(R.id.btn_ask_ai);

        etCommentInput = findViewById(R.id.et_comment_input);
        rvComments = findViewById(R.id.rv_comments);
        tvAiSummary = findViewById(R.id.tv_ai_summary);

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        postingId = getIntent().getIntExtra("postingId", -1);

        Log.d("ItemDetailActivity", "postingId=" + postingId);
        Log.d("ItemDetailActivity", "userId=" + userId);

        btnClose.setOnClickListener(v -> finish());

        if (postingId == -1) {
            Toast.makeText(this, "Invalid posting", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupCommentsRecycler();
        loadPostingDetails();
        loadComments();

        btnBuyNow.setOnClickListener(v -> addItemToCart());
        btnChat.setOnClickListener(v -> openChatPage());
        btnReport.setOnClickListener(v -> showReportConfirmation());
        btnPostComment.setOnClickListener(v -> postComment());
        btnAskAi.setOnClickListener(v -> loadAiSummary());
    }

    private void setupCommentsRecycler() {
        commentAdapter = new CommentAdapter(this, commentList, this);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
    }

    private void loadPostingDetails() {
        String url = ApiConfig.BASE_URL + "/postings/" + postingId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String title = response.optString("title", "Item");
                    String genre = response.optString("genre", "");
                    int price = response.optInt("price", 0);
                    String description = response.optString("description", "");
                    String imageUrl = response.optString("imageUrl", "");
                    sellerId = response.optInt("sellerId", -1);

                    Log.d("ItemDetailActivity", "sellerId=" + sellerId);

                    itemTitle.setText(title);
                    itemPrice.setText("$" + price);
                    itemGenre.setText(genre);

                    if (description != null && !description.trim().isEmpty()) {
                        itemDescription.setText(description);
                    } else {
                        itemDescription.setText("No description available.");
                    }

                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        String fullImageUrl = ApiConfig.BASE_URL + imageUrl;

                        Glide.with(this)
                                .load(fullImageUrl)
                                .placeholder(android.R.drawable.picture_frame)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(itemImage);
                    } else {
                        itemImage.setImageResource(android.R.drawable.ic_dialog_alert);
                    }

                    if (sellerId != -1) {
                        loadSellerDetails(sellerId);
                    } else {
                        itemSellerUsername.setText("Seller: Unknown");
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to load item details", Toast.LENGTH_SHORT).show();
                    Log.e("ItemDetailActivity", "Error loading posting details", error);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadSellerDetails(int sellerId) {
        String url = ApiConfig.BASE_URL + "/users/" + sellerId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String username = response.optString("username", "Unknown");
                    itemSellerUsername.setText("Seller: " + username);
                },
                error -> {
                    itemSellerUsername.setText("Seller: Unknown");
                    Log.e("ItemDetailActivity", "Error loading seller details", error);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadComments() {
        String url = ApiConfig.BASE_URL + "/comments/posting/" + postingId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> parseCommentsResponse(response),
                error -> {
                    Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                    Log.e("ItemDetailActivity", "Error loading comments", error);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void parseCommentsResponse(JSONArray response) {
        commentList.clear();

        ArrayList<CommentItem> allComments = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);

                int commentId = obj.optInt("commentId", -1);
                String senderUsername = obj.optString("senderUsername", "Unknown");
                String content = obj.optString("content", "");
                Integer parentCommentId = obj.isNull("parentCommentId")
                        ? null
                        : obj.optInt("parentCommentId");
                String createdAt = obj.optString("createdAt", "");

                CommentItem item = new CommentItem(
                        commentId,
                        senderUsername,
                        content,
                        parentCommentId,
                        createdAt,
                        parentCommentId == null ? 0 : 1
                );

                allComments.add(item);

            } catch (JSONException e) {
                Log.e("ItemDetailActivity", "Failed parsing comment", e);
            }
        }

        // Main comments only: newest to oldest
        for (CommentItem comment : allComments) {
            if (comment.getParentCommentId() == null) {
                commentList.add(0, comment);
            }
        }

        // Replies are attached under their parent, not shown as main comments
        for (CommentItem reply : allComments) {
            if (reply.getParentCommentId() != null) {
                for (CommentItem parent : commentList) {
                    if (parent.getCommentId() == reply.getParentCommentId()) {
                        parent.addReply(reply);
                        break;
                    }
                }
            }
        }

        commentAdapter.notifyDataSetChanged();

        if (selectedParentCommentId != null) {
            commentAdapter.setReplyTarget(selectedParentCommentId);
        }
    }

    private void loadAiSummary() {
        if (postingId == -1) {
            Toast.makeText(this, "Invalid posting", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "/postings/ai/" + postingId;

        btnAskAi.setEnabled(false);
        btnAskAi.setText("Thinking...");
        tvAiSummary.setVisibility(View.VISIBLE);
        tvAiSummary.setText("Generating AI summary...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String summary = response.optString("summary", "");

                    if (summary.trim().isEmpty()) {
                        tvAiSummary.setText("No AI summary available.");
                    } else {
                        Markwon markwon = Markwon.create(this);

                        String fixed = fixMarkdown(summary.trim());
                        markwon.setMarkdown(tvAiSummary, fixed);
                    }

                    btnAskAi.setEnabled(true);
                    btnAskAi.setText("Ask AI");
                },
                error -> {
                    tvAiSummary.setText("Failed to load AI summary.");
                    btnAskAi.setEnabled(true);
                    btnAskAi.setText("Ask AI");

                    if (error.networkResponse != null) {
                        Log.e("ItemDetailActivity", "AI status code: " + error.networkResponse.statusCode);
                        if (error.networkResponse.data != null) {
                            Log.e("ItemDetailActivity", "AI error body: " + new String(error.networkResponse.data));
                        }
                    } else {
                        Log.e("ItemDetailActivity", "AI request failed without network response", error);
                    }

                    Toast.makeText(this, "Failed to load AI summary", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private String fixMarkdown(String text) {
        return text.replaceAll("###(.*?)###", "### $1");
    }

    private void postComment() {
        String content = etCommentInput.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedParentCommentId = null;
        selectedReplyUsername = null;

        postCommentWithContent(content);
        etCommentInput.setText("");
    }

    private void postCommentWithContent(String content) {
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("ItemDetailActivity", "userId is -1");
            return;
        }

        if (postingId == -1) {
            Toast.makeText(this, "Invalid posting", Toast.LENGTH_SHORT).show();
            Log.e("ItemDetailActivity", "postingId is -1");
            return;
        }

        String url = ApiConfig.BASE_URL + "/comments/posting/" + postingId;

        JSONObject body = new JSONObject();
        try {
            body.put("senderId", (int) userId);
            body.put("content", content);

            if (selectedParentCommentId != null) {
                body.put("parentCommentId", selectedParentCommentId);
            } else {
                body.put("parentCommentId", JSONObject.NULL);
            }
        } catch (JSONException e) {
            Log.e("ItemDetailActivity", "JSON creation failed", e);
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ItemDetailActivity", "POST URL: " + url);
        Log.d("ItemDetailActivity", "POST BODY: " + body.toString());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d("ItemDetailActivity", "Comment posted successfully");
                    clearReplySelection();
                    loadComments();
                    Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("ItemDetailActivity", "Failed to post comment", error);

                    if (error.networkResponse != null) {
                        Log.e("ItemDetailActivity", "Status code: " + error.networkResponse.statusCode);

                        if (error.networkResponse.data != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e("ItemDetailActivity", "Error body: " + errorBody);
                        }
                    } else {
                        Log.e("ItemDetailActivity", "No networkResponse. Possible connection issue.");
                    }

                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onReplyClick(CommentItem comment) {
        selectedParentCommentId = comment.getCommentId();
        selectedReplyUsername = comment.getSenderUsername();

        commentAdapter.setReplyTarget(selectedParentCommentId);
    }

    @Override
    public void onInlineReplySubmit(CommentItem comment, String replyText) {
        selectedParentCommentId = comment.getCommentId();
        selectedReplyUsername = comment.getSenderUsername();

        if (replyText == null || replyText.trim().isEmpty()) {
            Toast.makeText(this, "Reply cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        postCommentWithContent(replyText.trim());
    }

    @Override
    public void onInlineReplyCancel() {
        clearReplySelection();
    }

    private void clearReplySelection() {
        selectedParentCommentId = null;
        selectedReplyUsername = null;

        if (commentAdapter != null) {
            commentAdapter.setReplyTarget(null);
        }
    }

    private void openChatPage() {
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sellerId == -1) {
            Toast.makeText(this, "Seller information not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ItemDetailActivity.this, ChatHomeActivity.class);
        intent.putExtra("sellerId", sellerId);
        intent.putExtra("postingId", postingId);
        startActivity(intent);
    }

    private void addItemToCart() {
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (postingId == -1) {
            Toast.makeText(this, "Invalid posting", Toast.LENGTH_SHORT).show();
            return;
        }

        CartManager.addToCart(
                this,
                userId,
                postingId,
                response -> Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
        );
    }

    private void showReportConfirmation() {
        if (postingId == -1) {
            Toast.makeText(this, "Invalid posting", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Report posting")
                .setMessage("Are you sure you want to report this posting?")
                .setPositiveButton("Yes", (dialog, which) -> reportPosting())
                .setNegativeButton("No", null)
                .show();
    }

    private void reportPosting() {
        String url = ApiConfig.BASE_URL + "/postings/report/" + postingId;

        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    Toast.makeText(this, "Posting reported successfully", Toast.LENGTH_SHORT).show();
                    btnReport.setEnabled(false);
                    btnReport.setText("Reported");
                },
                error -> Toast.makeText(this, "Failed to report posting", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}