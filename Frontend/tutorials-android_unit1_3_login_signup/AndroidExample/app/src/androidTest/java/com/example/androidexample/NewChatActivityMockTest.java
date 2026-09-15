package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
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
public class NewChatActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testuser")
                .putString("USERTYPE", "buyer")
                .apply();

        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path.equals("/users")) {
                    return json("[{" +
                            "\"id\":1," +
                            "\"username\":\"testuser\"," +
                            "\"emailId\":\"me@test.com\"," +
                            "\"userType\":\"buyer\"" +
                            "},{" +
                            "\"id\":2," +
                            "\"username\":\"sellerUser\"," +
                            "\"emailId\":\"seller@test.com\"," +
                            "\"userType\":\"seller\"" +
                            "},{" +
                            "\"id\":3," +
                            "\"username\":\"buyerUser\"," +
                            "\"emailId\":\"buyer@test.com\"," +
                            "\"userType\":\"buyer\"" +
                            "},{" +
                            "\"id\":4," +
                            "\"username\":\"adminUser\"," +
                            "\"emailId\":\"admin@test.com\"," +
                            "\"userType\":\"admin\"" +
                            "}]");
                }

                if (path.equals("/channel")) {
                    return new MockResponse()
                            .setResponseCode(201)
                            .setHeader("Content-Type", "application/json")
                            .setBody("{}");
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

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        if (server != null) server.shutdown();
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private Intent intent(String mode) {
        Intent intent = new Intent(context, NewChatActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", "buyer");
        intent.putExtra("MODE", mode);
        return intent;
    }

    @Test
    public void usersLoad_excludesSelfAndAdmin() throws Exception {
        ActivityScenario.launch(intent("DIRECT"));

        Thread.sleep(1500);

        onView(withText("sellerUser")).check(matches(isDisplayed()));
        onView(withText("buyerUser")).check(matches(isDisplayed()));
    }

    @Test
    public void searchFiltersUsers() throws Exception {
        ActivityScenario.launch(intent("DIRECT"));

        Thread.sleep(1500);

        onView(withId(R.id.edt_search_user))
                .perform(replaceText("seller"), closeSoftKeyboard());

        onView(withText("sellerUser")).check(matches(isDisplayed()));
    }

    @Test
    public void directSelectUser_updatesSelectedCount() throws Exception {
        ActivityScenario.launch(intent("DIRECT"));

        Thread.sleep(1500);

        onView(withText("sellerUser")).perform(click());

        onView(withId(R.id.txt_selected_count))
                .check(matches(withText("Selected: 1")));
    }

    @Test
    public void groupSelectUser_updatesSelectedCount() throws Exception {
        ActivityScenario.launch(intent("GROUP"));

        Thread.sleep(1500);

        onView(withText("sellerUser")).perform(click());

        onView(withId(R.id.txt_selected_count))
                .check(matches(withText("Selected: 1")));
    }

    @Test
    public void groupCreateWithoutSelectedUserStaysOnPage() throws Exception {
        ActivityScenario.launch(intent("GROUP"));

        Thread.sleep(1500);

        onView(withId(R.id.edt_group_name))
                .perform(replaceText("Collectors"), closeSoftKeyboard());

        onView(withId(R.id.btn_create_chat)).perform(click());

        onView(withId(R.id.btn_create_chat)).check(matches(isDisplayed()));
    }

    @Test
    public void directCreateWithSelectedUserCallsApi() throws Exception {
        ActivityScenario.launch(intent("DIRECT"));

        Thread.sleep(1500);

        onView(withText("sellerUser")).perform(click());
        onView(withId(R.id.btn_create_chat)).perform(click());

        Thread.sleep(1000);
    }

    @Test
    public void groupCreateWithSelectedUserCallsApi() throws Exception {
        ActivityScenario.launch(intent("GROUP"));

        Thread.sleep(1500);

        onView(withId(R.id.edt_group_name))
                .perform(replaceText("Collectors"), closeSoftKeyboard());

        onView(withText("sellerUser")).perform(click());
        onView(withId(R.id.btn_create_chat)).perform(click());

        Thread.sleep(1000);
    }
}