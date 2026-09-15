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
public class AiChatActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setup() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path.startsWith("/ai/chat/") && request.getMethod().equals("GET")) {
                    return json("[{" +
                            "\"aiMessageId\":1," +
                            "\"role\":\"user\"," +
                            "\"content\":\"hello\"," +
                            "\"createdAt\":\"2026-05-01\"" +
                            "},{" +
                            "\"aiMessageId\":2," +
                            "\"role\":\"model\"," +
                            "\"content\":\"hi there\"," +
                            "\"createdAt\":\"2026-05-01\"" +
                            "}]");
                }

                if (path.startsWith("/ai/chat/") && request.getMethod().equals("POST")) {
                    return json("{\"response\":\"mock reply\"}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");
    }

    @After
    public void teardown() throws Exception {
        ApiConfig.BASE_URL = BuildConfig.BASE_URL;
        if (server != null) server.shutdown();
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private Intent intent() {
        Intent intent = new Intent(context, AiChatActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testbuyer");
        intent.putExtra("USERTYPE", "buyer");
        return intent;
    }

    @Test
    public void historyLoads_andDisplaysMessages() throws Exception {
        ActivityScenario.launch(intent());

        Thread.sleep(1500);

        onView(withText("hello")).check(matches(isDisplayed()));
        onView(withText("hi there")).check(matches(isDisplayed()));
    }

    @Test
    public void sendMessage_addsUserAndAiReply() throws Exception {
        ActivityScenario.launch(intent());

        Thread.sleep(1500);

        onView(withId(R.id.edt_ai_message_input))
                .perform(typeText("test message"), closeSoftKeyboard());

        onView(withId(R.id.btn_send_ai_message)).perform(click());

        Thread.sleep(1500);

        onView(withText("test message")).check(matches(isDisplayed()));
        onView(withText("mock reply")).check(matches(isDisplayed()));
    }

    @Test
    public void emptyMessage_notSent() throws Exception {
        ActivityScenario.launch(intent());

        Thread.sleep(1500);

        onView(withId(R.id.edt_ai_message_input))
                .perform(clearText());

        onView(withId(R.id.btn_send_ai_message)).perform(click());

        // nothing crashes, still on screen
        onView(withId(R.id.recycler_ai_messages))
                .check(matches(isDisplayed()));
    }

    @Test
    public void apiError_staysOnScreen() throws Exception {
        server.shutdown();

        server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(500));
        server.start();

        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");

        ActivityScenario.launch(intent());

        Thread.sleep(1500);

        onView(withId(R.id.recycler_ai_messages))
                .check(matches(isDisplayed()));

        onView(withId(R.id.edt_ai_message_input))
                .check(matches(isDisplayed()));
    }
}