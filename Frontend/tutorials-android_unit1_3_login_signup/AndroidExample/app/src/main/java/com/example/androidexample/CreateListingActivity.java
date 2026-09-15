package com.example.androidexample;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class CreateListingActivity extends AppCompatActivity {
    private LinearLayout cardAddImage;
    private ImageView ivPreview;
    private EditText edtTitle, edtPrice, edtDescription;
    private Spinner spnGenre;
    private Button btnCreate;
    private TextView btnClose;

    // Stores the image chosen by the user
    private Uri selectedImageUri = null;

    // Logged-in user id used as sellerId when creating a posting
    private long sellerId = -1;

    // Launcher used to open the gallery/file
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Save selected image URI
                    selectedImageUri = uri;

                    // Show selected image in preview
                    ivPreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        // Link Java variables to XML views
        cardAddImage = findViewById(R.id.card_add_image);
        ivPreview = findViewById(R.id.iv_listing_preview);
        edtTitle = findViewById(R.id.edt_title);
        edtPrice = findViewById(R.id.edt_price);
        edtDescription = findViewById(R.id.edt_description);
        spnGenre = findViewById(R.id.spn_genre);
        btnCreate = findViewById(R.id.btn_create_listing);
        btnClose = findViewById(R.id.btn_close);

        // Read saved user id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AUTH", MODE_PRIVATE);
        sellerId = prefs.getLong("USER_ID", -1);

        // If no user id is found, user must log in again
        if (sellerId == -1) {
            Toast.makeText(this, "No user id saved. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Genre options for the spinner dropdown
        String[] genres = {
                "Comics",
                "Cards",
                "Electronics",
                "Video Games",
                "Figures",
                "Antique",
                "Furniture",
                "Other"
        };

        // Adapter connects the genre array to the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGenre.setAdapter(adapter);

        btnClose.setOnClickListener(v -> finish());

        // When image card is clicked, open image picker
        cardAddImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // When create button is clicked, send form data to backend
        btnCreate.setOnClickListener(v -> submitPosting());
    }

    private void submitPosting() {
        // Read input values from form fields
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String genre = (String) spnGenre.getSelectedItem();

        // user must choose an image
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please insert a picture", Toast.LENGTH_SHORT).show();
            return;
        }

        // all text fields must be filled
        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty() || genre == null || genre.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        //price must be a integer
        int price;
        try {
            price = Integer.parseInt(priceStr);
            if (price <= 0) {
                Toast.makeText(this, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Price must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build JSON object for the non-file posting data
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("sellerId", (int) sellerId);   // backend expects Integer
            dataJson.put("title", title);
            dataJson.put("description", description);
            dataJson.put("price", price);
            dataJson.put("genre", genre);
        } catch (Exception e) {
            Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Variables for image upload
        byte[] imageBytes;
        String imageFileName;
        String imageMime;

        try {
            ContentResolver cr = getContentResolver();

            imageMime = cr.getType(selectedImageUri);

            // Make sure selected file is actually an image
            if (imageMime == null || !imageMime.startsWith("image/")) {
                Toast.makeText(this, "Only image files allowed", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get original file name if possible
            imageFileName = getFileName(selectedImageUri);
            if (imageFileName == null) imageFileName = "image.jpg";

            // Read image file into byte array for multipart upload
            imageBytes = readAllBytes(selectedImageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable button so user doesn't submit multiple times
        btnCreate.setEnabled(false);

        // API endpoint for creating postings
        String url = BuildConfig.BASE_URL + "/postings";

        // Create multipart request with success and error handlers
        MultipartRequest req = new MultipartRequest(
                url,
                response -> {
                    // Re-enable button after success
                    btnCreate.setEnabled(true);

                    // Backend returns 201 Created with empty response body
                    Toast.makeText(this, "Listing created", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    // Re-enable button after failure
                    btnCreate.setEnabled(true);

                    // Get HTTP status code if available
                    int status = (error.networkResponse != null) ? error.networkResponse.statusCode : -1;

                    // Try reading backend message
                    String serverMsg = MultipartRequest.tryExtractServerMessage(error);

                    if (status == 400) {
                        Toast.makeText(this, (serverMsg != null) ? serverMsg : "Invalid input", Toast.LENGTH_LONG).show();
                    } else if (status == 404) {
                        Toast.makeText(this, "Seller not found (404)", Toast.LENGTH_LONG).show();
                    } else if (status == -1) {
                        String msg = (error.getMessage() != null) ? error.getMessage() : "Network error";
                        Toast.makeText(this, "Create failed: " + msg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(
                                this,
                                "Create failed (" + status + ")" + (serverMsg != null ? (": " + serverMsg) : ""),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        // Add JSON part named "data"
        req.addFormField("data", dataJson.toString(), "application/json; charset=utf-8");

        // Add image file part named "image"
        req.addFilePart("image", imageFileName, imageMime, imageBytes);

        // Send request using Volley
        Volley.newRequestQueue(this).add(req);
    }

    private byte[] readAllBytes(Uri uri) throws Exception {
        // Opens the selected file and converts it into a byte array
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (in == null) throw new IllegalStateException("InputStream null");

            byte[] buf = new byte[8192];
            int n;

            // Read file in chunks
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }

            return out.toByteArray();
        }
    }

    private String getFileName(Uri uri) {
        // Try to get display name of the selected file from content resolver
        String result = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) {
                        result = cursor.getString(idx);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        return result;
    }
}