package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity {

    private static final String BASE_URL = BuildConfig.BASE_URL;

    private EditText etCardName, etCardNumber, etExpiry, etCvv;
    private TextView tvPaymentTotal, btnBackPayment;
    private Button btnBackBottomPayment, btnPayNow;

    private long buyerId = -1;
    private JSONArray cartItems = new JSONArray();

    private int currentPurchaseIndex = 0;
    private int successCount = 0;
    private int failCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        etCardName = findViewById(R.id.et_card_name);
        etCardNumber = findViewById(R.id.et_card_number);
        etExpiry = findViewById(R.id.et_expiry);
        etCvv = findViewById(R.id.et_cvv);

        tvPaymentTotal = findViewById(R.id.tv_payment_total);

        btnBackPayment = findViewById(R.id.btn_back_payment);
        btnBackBottomPayment = findViewById(R.id.btn_back_bottom_payment);
        btnPayNow = findViewById(R.id.btn_pay_now);

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

        showTotal();
        setupInputRestrictions();

        btnBackPayment.setOnClickListener(v -> finish());
        btnBackBottomPayment.setOnClickListener(v -> finish());

        btnPayNow.setOnClickListener(v -> {
            if (!validateInputs()) return;

            if (buyerId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cartItems == null || cartItems.length() == 0) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            btnPayNow.setEnabled(false);
            currentPurchaseIndex = 0;
            successCount = 0;
            failCount = 0;

            purchaseNextItem();
        });
    }

    private void showTotal() {
        double total = 0.0;

        for (int i = 0; i < cartItems.length(); i++) {
            JSONObject item = cartItems.optJSONObject(i);
            if (item == null) continue;
            total += item.optDouble("price", 0.0);
        }

        tvPaymentTotal.setText("Total: $" + String.format("%.2f", total));
    }

    private void setupInputRestrictions() {
        etCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        etExpiry.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        etCvv.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String digitsOnly = s.toString().replaceAll("\\D", "");
                if (digitsOnly.length() > 16) {
                    digitsOnly = digitsOnly.substring(0, 16);
                }

                etCardNumber.setText(digitsOnly);
                etCardNumber.setSelection(digitsOnly.length());

                isEditing = false;
            }
        });

        etExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replace("/", "").replaceAll("\\D", "");

                if (input.length() > 4) {
                    input = input.substring(0, 4);
                }

                if (input.length() >= 2) {
                    int month = Integer.parseInt(input.substring(0, 2));
                    if (month == 0) {
                        input = "01" + input.substring(2);
                    } else if (month > 12) {
                        input = "12" + input.substring(2);
                    }
                }

                String formatted;
                if (input.length() >= 3) {
                    formatted = input.substring(0, 2) + "/" + input.substring(2);
                } else {
                    formatted = input;
                }

                etExpiry.setText(formatted);
                etExpiry.setSelection(formatted.length());

                isFormatting = false;
            }
        });

        etCvv.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String digitsOnly = s.toString().replaceAll("\\D", "");
                if (digitsOnly.length() > 3) {
                    digitsOnly = digitsOnly.substring(0, 3);
                }

                etCvv.setText(digitsOnly);
                etCvv.setSelection(digitsOnly.length());

                isEditing = false;
            }
        });
    }

    private boolean validateInputs() {
        String cardName = etCardName.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        etCardName.setError(null);
        etCardNumber.setError(null);
        etExpiry.setError(null);
        etCvv.setError(null);

        if (TextUtils.isEmpty(cardName)) {
            etCardName.setError("Enter cardholder name");
            return false;
        }

        if (cardNumber.length() != 16) {
            etCardNumber.setError("Card number must be 16 digits");
            return false;
        }

        if (!expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            etExpiry.setError("Enter expiry in MM/YY");
            return false;
        }

        if (cvv.length() != 3) {
            etCvv.setError("CVV must be 3 digits");
            return false;
        }

        return true;
    }

    private void purchaseNextItem() {
        if (currentPurchaseIndex >= cartItems.length()) {

            if (failCount == 0) {
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(PaymentActivity.this, BuyerHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else if (successCount > 0) {
                Toast.makeText(this, "Some items were purchased, but a few failed.", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(PaymentActivity.this, BuyerHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Payment failed. Please try again.", Toast.LENGTH_LONG).show();
                btnPayNow.setEnabled(true);
            }

            return;
        }

        JSONObject item = cartItems.optJSONObject(currentPurchaseIndex);
        if (item == null) {
            failCount++;
            currentPurchaseIndex++;
            purchaseNextItem();
            return;
        }

        int postingId = item.optInt("postingId", -1);
        if (postingId == -1) {
            failCount++;
            currentPurchaseIndex++;
            purchaseNextItem();
            return;
        }

        String url = BASE_URL + "/transactions/" + postingId + "/" + buyerId;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    successCount++;
                    currentPurchaseIndex++;
                    purchaseNextItem();
                },
                error -> {
                    failCount++;
                    currentPurchaseIndex++;
                    purchaseNextItem();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}