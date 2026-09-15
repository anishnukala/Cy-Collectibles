package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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
public class BanUsersActivityMockTest {

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

                if (path.equals("/users")) {
                    return json("[{" +
                            "\"id\":2," +
                            "\"username\":\"buyerUser\"," +
                            "\"flagCount\":1," +
                            "\"userType\":\"buyer\"" +
                            "},{" +
                            "\"id\":3," +
                            "\"username\":\"sellerUser\"," +
                            "\"flagCount\":4," +
                            "\"userType\":\"seller\"" +
                            "},{" +
                            "\"id\":4," +
                            "\"username\":\"adminUser\"," +
                            "\"flagCount\":0," +
                            "\"userType\":\"admin\"" +
                            "}]");
                }

                if (path.startsWith("/ban/user/")) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setBody("banned");
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

    private Intent banIntent() {
        Intent intent = new Intent(context, BanUsersActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void usersLoadAndDisplayNonAdminsOnly() throws Exception {
        ActivityScenario.launch(banIntent());

        Thread.sleep(1500);

        onView(withText("buyerUser\nFlag count: 1"))
                .check(matches(isDisplayed()));

        onView(withText("sellerUser\nFlag count: 4"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void selectUser_opensConfirmDialog() throws Exception {
        ActivityScenario.launch(banIntent());

        Thread.sleep(1500);

        onView(withText("buyerUser\nFlag count: 1"))
                .perform(click());

        onView(withId(R.id.btn_ban_selected))
                .perform(click());

        onView(withText("Confirm ban"))
                .check(matches(isDisplayed()));

        onView(withText("Ban this user?"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void confirmBan_callsApi() throws Exception {
        ActivityScenario.launch(banIntent());

        Thread.sleep(1500);

        onView(withText("buyerUser\nFlag count: 1"))
                .perform(click());

        onView(withId(R.id.btn_ban_selected))
                .perform(click());

        onView(withText("Yes"))
                .perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.btn_ban_selected))
                .check(matches(isDisplayed()));
    }
}