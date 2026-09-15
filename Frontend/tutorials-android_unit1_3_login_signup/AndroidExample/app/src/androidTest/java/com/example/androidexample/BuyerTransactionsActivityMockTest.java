package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
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
public class BuyerTransactionsActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testbuyer")
                .putString("USERTYPE", "BUYER")
                .apply();

        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path.equals("/transactions/buyer/1")) {
                    return json("[{" +
                            "\"transactionId\":101," +
                            "\"dateSold\":\"2026-05-02T10:15:30.000Z\"," +
                            "\"posting\":{" +
                            "\"title\":\"Mock Bought Item\"," +
                            "\"price\":75," +
                            "\"genre\":\"Cards\"," +
                            "\"sellerId\":9" +
                            "}" +
                            "}]");
                }

                return new MockResponse()
                        .setResponseCode(404)
                        .setBody("{}");
            }
        });

        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");
    }

    @After
    public void tearDown() throws Exception {
        ApiConfig.BASE_URL = BuildConfig.BASE_URL;

        if (server != null) {
            server.shutdown();
        }

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    @Test
    public void mockTransactionRendersCard() throws Exception {
        ActivityScenario.launch(BuyerTransactionsActivity.class);

        Thread.sleep(1500);

        onView(withText("Mock Bought Item"))
                .check(matches(isDisplayed()));

        onView(withText("Transaction ID: 101\nSeller ID: 9\nPrice: $75\nGenre: Cards\nDate Sold: 2026-05-02 10:15:30"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyTransactionsShowsMessage() throws Exception {
        server.shutdown();

        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/transactions/buyer/1")) {
                    return json("[]");
                }

                return new MockResponse()
                        .setResponseCode(404)
                        .setBody("{}");
            }
        });

        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");

        ActivityScenario.launch(BuyerTransactionsActivity.class);

        Thread.sleep(1500);

        onView(withText("No transactions found"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void failedTransactionsShowsMessage() throws Exception {
        server.shutdown();

        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/transactions/buyer/1")) {
                    return new MockResponse()
                            .setResponseCode(500)
                            .setBody("{}");
                }

                return new MockResponse()
                        .setResponseCode(404)
                        .setBody("{}");
            }
        });

        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");

        ActivityScenario.launch(BuyerTransactionsActivity.class);

        Thread.sleep(1500);

        onView(withText("Failed to load transactions"))
                .check(matches(isDisplayed()));
    }
}