package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

public class BuyerTransactionsActivity extends AppCompatActivity {

    private static String baseUrl() {
    return ApiConfig.BASE_URL;
}

    private Button btnClose;
    private LinearLayout transactionsContainer;
    private BottomNavigationView bottomNav;

    private long userId = -1;

    /**
     * Initializes the buyer transactions screen, loads the logged-in user ID,
     * connects required UI components, validates the layout, sets the close button
     * behavior, configures bottom navigation, and loads transaction data.
     *
     * @param savedInstanceState the saved instance state bundle passed to the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_transactions);

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        userId = prefs.getLong("USER_ID", -1);

        btnClose = findViewById(R.id.btn_close);
        transactionsContainer = findViewById(R.id.transactions_container);
        bottomNav = findViewById(R.id.bottom_nav);

        if (btnClose == null || transactionsContainer == null || bottomNav == null) {
            Toast.makeText(this, "Layout error: missing view IDs", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(BuyerTransactionsActivity.this, BuyerHomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        setupBottomNav();
        loadTransactions();
    }

    /**
     * Reloads transactions whenever the activity returns to the foreground.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (transactionsContainer != null) {
            loadTransactions();
        }
    }

    /**
     * Configures the bottom navigation bar and handles navigation between the
     * buyer postings, cart, transactions, and chat sections.
     */
    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_buyer_transactions);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_buyer_postings) {
                startActivity(new Intent(BuyerTransactionsActivity.this, BuyerHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_buyer_cart) {
                startActivity(new Intent(BuyerTransactionsActivity.this, CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_buyer_transactions) {
                return true;

            } else if (id == R.id.nav_buyer_chat) {
                startActivity(new Intent(BuyerTransactionsActivity.this, ChatHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    /**
     * Sends a request to retrieve all transactions for the logged-in buyer and
     * either binds the results to the screen or shows an error message.
     */
    private void loadTransactions() {
        transactionsContainer.removeAllViews();

        if (userId == -1) {
            addEmptyMessage("Invalid user ID");
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = baseUrl() + "/transactions/buyer/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::bindTransactions,
                error -> {
                    transactionsContainer.removeAllViews();
                    addEmptyMessage("Failed to load transactions");
                    Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Clears the current list and renders a card for each transaction returned by
     * the backend. Shows a fallback message if no transactions exist or if parsing fails.
     *
     * @param transactions the JSON array containing buyer transaction data
     */
    private void bindTransactions(JSONArray transactions) {
        transactionsContainer.removeAllViews();

        if (transactions == null || transactions.length() == 0) {
            addEmptyMessage("No transactions found");
            return;
        }

        try {
            for (int i = transactions.length() - 1; i >= 0; i--) {
                JSONObject transaction = transactions.getJSONObject(i);

                int transactionId = transaction.optInt("transactionId", -1);
                String dateSold = transaction.optString("dateSold", "Unknown");

                JSONObject posting = transaction.optJSONObject("posting");
                String title = posting != null ? posting.optString("title", "Unknown") : "Unknown";
                int price = posting != null ? posting.optInt("price", 0) : 0;
                String genre = posting != null ? posting.optString("genre", "Unknown") : "Unknown";
                int sellerId = posting != null ? posting.optInt("sellerId", -1) : -1;

                addTransactionCard(transactionId, sellerId, dateSold, title, price, genre);
            }
        } catch (Exception e) {
            transactionsContainer.removeAllViews();
            addEmptyMessage("Failed to parse transactions");
            Toast.makeText(this, "Failed to parse transactions", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds a simple text message to the screen when there are no transactions or
     * when loading/parsing fails.
     *
     * @param message the message to display in the transactions container
     */
    private void addEmptyMessage(String message) {
        TextView emptyView = new TextView(this);
        emptyView.setText(message);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        emptyView.setTextColor(0xFF666666);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(10);
        emptyView.setLayoutParams(params);

        transactionsContainer.addView(emptyView);
    }

    /**
     * Creates and adds a styled transaction card showing the item title,
     * transaction ID, seller ID, price, genre, and sale date.
     *
     * @param transactionId the ID of the transaction
     * @param sellerId the ID of the seller associated with the transaction
     * @param dateSold the date the item was sold
     * @param title the title of the purchased item
     * @param price the price of the purchased item
     * @param genre the genre of the purchased item
     */
    private void addTransactionCard(int transactionId, int sellerId, String dateSold, String title, int price, String genre) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_input);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = dp(12);
        card.setLayoutParams(cardParams);

        TextView tvName = new TextView(this);
        tvName.setText(title);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvName.setTextColor(0xFF444444);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvDetails = new TextView(this);
        String formattedDate = dateSold;

        if (formattedDate.contains("T")) {
            formattedDate = formattedDate.replace("T", " ");
        }

        int dotIndex = formattedDate.indexOf(".");
        if (dotIndex != -1) {
            formattedDate = formattedDate.substring(0, dotIndex);
        }

        tvDetails.setText(
                "Transaction ID: " + transactionId +
                        "\nSeller ID: " + sellerId +
                        "\nPrice: $" + price +
                        "\nGenre: " + genre +
                        "\nDate Sold: " + formattedDate
        );
        tvDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvDetails.setTextColor(0xFF666666);

        card.addView(tvName);
        card.addView(tvDetails);
        transactionsContainer.addView(card);
    }

    /**
     * Converts a density-independent pixel value into its pixel equivalent for the
     * current device display metrics.
     *
     * @param value the density-independent pixel value to convert
     * @return the equivalent pixel value as an integer
     */
    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}