package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
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
public class BuyerHomeActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
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

                if (path.startsWith("/postings/genre")) {
                    return json("[\"Cards\",\"Books\"]");
                }

                if (path.startsWith("/postings")) {
                    return json("[{" +
                            "\"postingId\":10," +
                            "\"title\":\"Mock Pokemon Card\"," +
                            "\"genre\":\"Cards\"," +
                            "\"price\":25," +
                            "\"description\":\"Rare card\"," +
                            "\"imageUrl\":\"/mock.png\"" +
                            "},{" +
                            "\"postingId\":11," +
                            "\"title\":\"Mock Book\"," +
                            "\"genre\":\"Books\"," +
                            "\"price\":12," +
                            "\"description\":\"Good book\"," +
                            "\"imageUrl\":\"/book.png\"" +
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

    @Test
    public void mockPostingsRenderCards() throws Exception {
        ActivityScenario.launch(BuyerHomeActivity.class);

        Thread.sleep(1500);

        onView(withText("Mock Pokemon Card"))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withText("Mock Book"))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void searchFiltersMatchingPosting() throws Exception {
        ActivityScenario.launch(BuyerHomeActivity.class);

        Thread.sleep(1500);

        onView(withId(R.id.et_search))
                .perform(replaceText("pokemon"), closeSoftKeyboard());

        onView(withText("Mock Pokemon Card")).check(matches(isDisplayed()));
    }

    @Test
    public void searchNoMatchShowsEmptyMessage() throws Exception {
        ActivityScenario.launch(BuyerHomeActivity.class);

        Thread.sleep(1500);

        onView(withId(R.id.et_search))
                .perform(replaceText("basketball"), closeSoftKeyboard());

        onView(withText("No matching listings found."))
                .check(matches(isDisplayed()));
    }

    @Test
    public void categoryMenuOpensAfterGenresLoad() throws Exception {
        ActivityScenario.launch(BuyerHomeActivity.class);

        Thread.sleep(1500);

        onView(withId(R.id.btn_category)).perform(click());

        onView(withText("All")).check(matches(isDisplayed()));
        onView(withText("Cards")).check(matches(isDisplayed()));
        onView(withText("Books")).check(matches(isDisplayed()));
    }

    @Test
    public void sortMenuOpens() throws Exception {
        ActivityScenario.launch(BuyerHomeActivity.class);

        Thread.sleep(1500);

        onView(withId(R.id.btn_sort)).perform(click());

        onView(withText("Newest")).check(matches(isDisplayed()));
        onView(withText("Oldest")).check(matches(isDisplayed()));
        onView(withText("Price: Low to High")).check(matches(isDisplayed()));
        onView(withText("Title: A-Z")).check(matches(isDisplayed()));
    }
}