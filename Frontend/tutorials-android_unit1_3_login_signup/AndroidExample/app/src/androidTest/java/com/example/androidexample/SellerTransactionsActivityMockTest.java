package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;
import android.content.Intent;

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
public class SellerTransactionsActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/transactions/seller/1")) {
                    return json("[{" +
                            "\"transactionId\":101," +
                            "\"buyerId\":5," +
                            "\"dateSold\":\"2026-05-02T10:15:30.000Z\"," +
                            "\"posting\":{" +
                            "\"title\":\"Mock Sold Item\"," +
                            "\"price\":75," +
                            "\"genre\":\"Cards\"" +
                            "}" +
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
        Intent intent = new Intent(context, SellerTransactionsActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "seller");
        intent.putExtra("USERTYPE", "seller");
        return intent;
    }

    @Test
    public void mockTransactionRendersCard() throws Exception {
        ActivityScenario.launch(sellerIntent());

        Thread.sleep(1000);

        onView(withText("Mock Sold Item"))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withText("Transaction ID: 101\nBuyer ID: 5\nPrice: $75\nGenre: Cards\nDate Sold: May 02 2026"))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void emptyTransactionsShowsMessage() throws Exception {
        server.shutdown();

        server = new MockWebServer();
        server.enqueue(json("[]"));
        server.start();

        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");

        ActivityScenario.launch(sellerIntent());

        Thread.sleep(1000);

        onView(withText("No transactions found"))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }
}