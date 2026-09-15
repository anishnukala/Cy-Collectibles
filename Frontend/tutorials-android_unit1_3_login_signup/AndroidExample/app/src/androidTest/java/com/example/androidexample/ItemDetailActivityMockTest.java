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
public class ItemDetailActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putLong("USER_ID", 1L)
                .apply();

        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path.equals("/postings/1")) {
                    return json("{\"title\":\"Pokemon Card\",\"genre\":\"Cards\",\"price\":50," +
                            "\"description\":\"Rare card\",\"imageUrl\":\"\",\"sellerId\":2}");
                }

                if (path.equals("/users/2")) {
                    return json("{\"username\":\"seller123\"}");
                }

                if (path.equals("/comments/posting/1")) {
                    return json("[{\"commentId\":1,\"senderUsername\":\"user1\",\"content\":\"Nice item\"," +
                            "\"parentCommentId\":null,\"createdAt\":\"now\"}]");
                }

                if (path.equals("/postings/ai/1")) {
                    return json("{\"summary\":\"Good listing.\"}");
                }

                if (path.equals("/postings/report/1")) {
                    return new MockResponse().setResponseCode(200).setBody("ok");
                }

                return new MockResponse().setResponseCode(404);
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

    private void launchActivity() {
        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra("postingId", 1);
        ActivityScenario.launch(intent);
    }

    @Test
    public void reportDialogOpensAndConfirms() {
        launchActivity();

        onView(withId(R.id.btn_report))
                .perform(scrollTo(), click());

        onView(withText("Report posting"))
                .check(matches(isDisplayed()));

        onView(withText("Yes"))
                .perform(click());

        onView(withId(R.id.btn_report))
                .perform(scrollTo())
                .check(matches(withText("Reported")));
    }

    @Test
    public void chatButtonClickable() {
        launchActivity();

        onView(withId(R.id.btn_chat))
                .perform(scrollTo(), click());
    }

    @Test
    public void askAiButtonWorks() {
        launchActivity();

        onView(withId(R.id.btn_ask_ai))
                .perform(scrollTo(), click());

        onView(withId(R.id.tv_ai_summary))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void closeButtonClickable() {
        launchActivity();

        onView(withId(R.id.btn_close))
                .perform(click());
    }

    @Test
    public void buyNowButtonClickable() {
        launchActivity();

        onView(withId(R.id.btn_buy_now))
                .perform(scrollTo(), click());
    }

    @Test
    public void itemImageVisible() {
        launchActivity();

        onView(withId(R.id.item_image))
                .check(matches(isDisplayed()));
    }

    @Test
    public void commentInputVisible() {
        launchActivity();

        onView(withId(R.id.et_comment_input))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void postButtonVisible() {
        launchActivity();

        onView(withId(R.id.btn_post_comment))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void aiBoxVisible() {
        launchActivity();

        onView(withId(R.id.ai_summary_box))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }
}