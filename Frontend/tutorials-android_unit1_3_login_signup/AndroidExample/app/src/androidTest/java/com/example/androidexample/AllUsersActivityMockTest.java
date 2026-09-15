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
public class AllUsersActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().equals("/users")) {
                    return json("[{" +
                            "\"id\":2," +
                            "\"username\":\"buyerUser\"," +
                            "\"flagCount\":1," +
                            "\"userType\":\"buyer\"" +
                            "},{" +
                            "\"userId\":3," +
                            "\"username\":\"sellerUser\"," +
                            "\"flagCount\":4," +
                            "\"userType\":\"seller\"" +
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

    private Intent allUsersIntent() {
        Intent intent = new Intent(context, AllUsersActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void mockUsersRenderCards() throws Exception {
        ActivityScenario.launch(allUsersIntent());

        Thread.sleep(1500);

        onView(withText("buyerUser")).check(matches(isDisplayed()));
        onView(withText("User ID: 2\nFlag Count: 1\nUser Type: buyer"))
                .check(matches(isDisplayed()));

        onView(withText("sellerUser")).check(matches(isDisplayed()));
        onView(withText("User ID: 3\nFlag Count: 4\nUser Type: seller"))
                .check(matches(isDisplayed()));
    }
}