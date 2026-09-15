package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
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
public class BannedUserActivityMockTest {

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

                if (path.equals("/admin/users/banned")) {
                    return json("[{" +
                            "\"id\":2," +
                            "\"username\":\"bannedBuyer\"," +
                            "\"emailId\":\"buyer@test.com\"," +
                            "\"flagCount\":3," +
                            "\"userType\":\"buyer\"," +
                            "\"banned\":true," +
                            "\"userStatus\":0" +
                            "},{" +
                            "\"id\":3," +
                            "\"username\":\"deletedSeller\"," +
                            "\"emailId\":\"seller@test.com\"," +
                            "\"flagCount\":5," +
                            "\"userType\":\"seller\"," +
                            "\"banned\":false," +
                            "\"userStatus\":-1" +
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
        if (server != null) server.shutdown();
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private Intent bannedUsersIntent() {
        Intent intent = new Intent(context, BannedUserActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void mockBannedUsersRenderCards() throws Exception {
        ActivityScenario.launch(bannedUsersIntent());

        Thread.sleep(1500);

        onView(withText("bannedBuyer"))
                .check(matches(isDisplayed()));

        onView(withText("User ID: 2\nEmail: buyer@test.com\nFlag Count: 3\nUser Type: buyer\nBanned: Yes\nStatus: BANNED"))
                .check(matches(isDisplayed()));

        onView(withText("deletedSeller"))
                .check(matches(isDisplayed()));

        onView(withText("User ID: 3\nEmail: seller@test.com\nFlag Count: 5\nUser Type: seller\nBanned: No\nStatus: DELETED"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyBannedUsersShowsMessage() throws Exception {
        server.shutdown();

        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/admin/users/banned")) {
                    return json("[]");
                }

                return new MockResponse()
                        .setResponseCode(404)
                        .setBody("{}");
            }
        });

        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");

        ActivityScenario.launch(bannedUsersIntent());

        Thread.sleep(1500);

        onView(withText("No banned users found"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void failedBannedUsersShowsMessage() throws Exception {
        server.shutdown();

        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/admin/users/banned")) {
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

        ActivityScenario.launch(bannedUsersIntent());

        Thread.sleep(1500);

        onView(withText("Failed to load banned users"))
                .check(matches(isDisplayed()));
    }
}