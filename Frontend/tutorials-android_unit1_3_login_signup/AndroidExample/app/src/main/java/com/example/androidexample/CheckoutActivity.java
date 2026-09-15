package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvCheckoutTitle, tvCheckoutMessage, tvTotalPrice;
    private TextView btnBackTop;
    private LinearLayout checkoutItemsContainer;
    private Button btnBackBottom, btnPlaceOrder;

    private long buyerId = -1;
    private JSONArray cartItems = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        tvCheckoutTitle = findViewById(R.id.tv_checkout_title);
        tvCheckoutMessage = findViewById(R.id.tv_checkout_message);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        checkoutItemsContainer = findViewById(R.id.checkout_items_container);

        btnBackTop = findViewById(R.id.btn_back_checkout);
        btnBackBottom = findViewById(R.id.btn_back_bottom);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        buyerId = prefs.getLong("USER_ID", -1);

        String cartJson = getIntent().getStringExtra("cartItems");
        if (cartJson != null && !cartJson.isEmpty()) {
            try {
                cartItems = new JSONArray(cartJson);
            } catch (Exception e) {
                e.printStackTrace();
                cartItems = new JSONArray();
            }
        }

        tvCheckoutTitle.setText("Checkout");
        tvCheckoutMessage.setText("Review your cart items before placing the order.");

        renderCheckoutItems();

        btnBackTop.setOnClickListener(v -> finish());
        btnBackBottom.setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> {
            if (buyerId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cartItems == null || cartItems.length() == 0) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
            intent.putExtra("cartItems", cartItems.toString());
            startActivity(intent);
        });
    }

    private void renderCheckoutItems() {
        checkoutItemsContainer.removeAllViews();

        boolean empty = (cartItems == null || cartItems.length() == 0);
        double total = 0.0;

        if (empty) {
            tvCheckoutMessage.setText("Your cart is empty.");
            tvTotalPrice.setText("Total: $0.00");
            return;
        }

        for (int i = 0; i < cartItems.length(); i++) {
            JSONObject item = cartItems.optJSONObject(i);
            if (item == null) continue;

            String title = item.optString("title", "Untitled");
            String genre = item.optString("genre", "");
            double price = item.optDouble("price", 0.0);

            total += price;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(24, 20, 24, 20);
            card.setBackgroundResource(R.drawable.bg_card);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = 18;
            card.setLayoutParams(params);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(title);
            tvTitle.setTextSize(18);
            tvTitle.setTextColor(0xFF444444);
            tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);

            TextView tvMeta = new TextView(this);
            tvMeta.setText(genre + " • $" + String.format("%.2f", price));
            tvMeta.setTextSize(14);
            tvMeta.setTextColor(0xFF666666);

            card.addView(tvTitle);
            card.addView(tvMeta);

            checkoutItemsContainer.addView(card);
        }

        tvTotalPrice.setText("Total: $" + String.format("%.2f", total));
    }
}