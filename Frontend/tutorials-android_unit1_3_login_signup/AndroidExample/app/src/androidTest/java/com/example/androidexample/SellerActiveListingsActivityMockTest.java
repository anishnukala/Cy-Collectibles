package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public class SellerActiveListingsActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path.equals("/postings/seller/1")) {
                    return json("[{" +
                            "\"postingId\":12," +
                            "\"title\":\"Mock Seller Post\"," +
                            "\"description\":\"Seller description\"," +
                            "\"price\":55," +
                            "\"genre\":\"Cards\"," +
                            "\"date\":\"2026-05-02\"," +
                            "\"imageUrl\":\"mock.png\"" +
                            "}]");
                }

                return new MockResponse().setResponseCode(404).setBody("{}");
            }
        });

        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");
    }

    @After
    public void tearDown() throws Exception {
        ApiConfig.BASE_URL = BuildConfig.BASE_URL;
        if (server != null) server.shutdown();
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private Intent sellerIntent() {
        Intent intent = new Intent(context, SellerActiveListingsActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testseller");
        intent.putExtra("USERTYPE", "seller");
        return intent;
    }

    @Test
    public void viewPostButtonExists() throws Exception {
        ActivityScenario.launch(sellerIntent());

        Thread.sleep(1000);

        onView(allOf(withText("View Post"), isAssignableFrom(Button.class)))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

}