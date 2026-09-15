package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;
import android.content.Intent;

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
public class ReportedPostsActivityMockTest {

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

                if (path.equals("/admin/postings/reported")) {
                    return json("[{" +
                            "\"postingId\":12," +
                            "\"sellerId\":5," +
                            "\"title\":\"Mock Reported Post\"," +
                            "\"genre\":\"Cards\"," +
                            "\"price\":40," +
                            "\"description\":\"Reported description\"," +
                            "\"imageUrl\":\"mock.png\"," +
                            "\"date\":\"2026-05-02\"" +
                            "}]");
                }

                if (path.equals("/verify/posting/1/12")) {
                    return new MockResponse().setResponseCode(200).setBody("verified");
                }

                if (path.equals("/ban/posting/1/12")) {
                    return json("{}");
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

    private Intent reportedIntent() {
        Intent intent = new Intent(context, ReportedPostsActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void mockReportedPostRendersCard() throws Exception {
        ActivityScenario.launch(reportedIntent());

        Thread.sleep(1000);

        onView(withText("Mock Reported Post"))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withText("Seller ID: 5\nGenre: Cards\nPrice: $40\nDate Posted: 2026-05-02"))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void viewPostButtonVisible() throws Exception {
        ActivityScenario.launch(reportedIntent());

        Thread.sleep(1000);

        onView(withText("View Post"))
                .perform(scrollTo())
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }
}