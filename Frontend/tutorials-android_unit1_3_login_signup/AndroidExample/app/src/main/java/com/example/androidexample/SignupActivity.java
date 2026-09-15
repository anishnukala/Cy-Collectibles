package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class SignupActivity extends AppCompatActivity {

    // input fields
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;
    private EditText emailEditText;
    private Spinner userTypeSpinner;

    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // link views
        usernameEditText = findViewById(R.id.signup_username_edt);
        passwordEditText = findViewById(R.id.signup_password_edt);
        confirmEditText  = findViewById(R.id.signup_confirm_edt);
        emailEditText    = findViewById(R.id.signup_email_edt);
        userTypeSpinner  = findViewById(R.id.signup_userType_spinner);

        loginButton  = findViewById(R.id.signup_login_btn);
        signupButton = findViewById(R.id.signup_signup_btn);

        setupUserTypeSpinner(); // set dropdown values

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        signupButton.setOnClickListener(v -> doSignup());

        getWindow().setStatusBarColor(android.graphics.Color.WHITE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void setupUserTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"buyer", "seller"} // backend values
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextSize(20f);
                tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                tv.setPadding(36, 0, 36, 0);
                tv.setSingleLine(true);
                tv.setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextSize(22f);
                tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                tv.setPadding(36, 24, 0, 24);
                tv.setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);
        userTypeSpinner.setSelection(0); // default = buyer
    }

    private void doSignup() {
        // get user input
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirm  = confirmEditText.getText().toString();
        String emailId  = emailEditText.getText().toString().trim();
        String userType = (String) userTypeSpinner.getSelectedItem();

        // check all fields filled
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()
                || emailId.isEmpty() || userType == null || userType.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // check password match
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BuildConfig.BASE_URL + "/users"; // signup endpoint

        JSONObject body = new JSONObject();
        try {
            // build JSON request body
            body.put("username", username);
            body.put("password", password);
            body.put("userType", userType);
            body.put("emailId", emailId);
        } catch (JSONException e) {
            Toast.makeText(this, "App error building request", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                // success response
                resp -> {
                    Toast.makeText(this, "Signup success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                },
                // error response
                error -> {
                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    if (status == 409) {
                        Toast.makeText(this, "Username or email already exists", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (status == 400) {
                        Toast.makeText(this, "Invalid signup data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String msg = (error.getMessage() != null) ? error.getMessage() : "Network error";
                    Toast.makeText(this, "Signup failed (" + status + "): " + msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes(StandardCharsets.UTF_8); // send JSON bytes
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8"; // JSON type
            }

            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        // set timeout and retry settings
        req.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // send request
        Volley.newRequestQueue(this).add(req);
    }
}