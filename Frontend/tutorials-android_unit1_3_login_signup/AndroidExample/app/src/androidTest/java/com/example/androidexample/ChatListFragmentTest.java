package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
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
public class ChatListFragmentTest {

    private MockWebServer server;

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {

                String path = request.getPath();

                // MAIN LIST
                if (path.equals("/channel/user/1")) {
                    return json("[{" +
                            "\"channelId\":1," +
                            "\"name\":\"Group Chat\"," +
                            "\"type\":\"GROUP\"," +
                            "\"unread\":true," +
                            "\"lastMessage\":\"Hello group\"," +
                            "\"lastMessageDate\":\"2024-01-01T10:00:00Z\"" +
                            "},{" +
                            "\"channelId\":2," +
                            "\"name\":\"\"," +
                            "\"type\":\"DIRECT\"," +
                            "\"unread\":false," +
                            "\"lastMessage\":\"Hi\"," +
                            "\"lastMessageDate\":\"2024-01-01T09:00:00Z\"" +
                            "}]");
                }

                // GROUP DETAILS
                if (path.equals("/channel/1")) {
                    return json("{\"members\":[" +
                            "{\"id\":1,\"username\":\"me\"}," +
                            "{\"id\":2,\"username\":\"user2\"}" +
                            "]}");
                }

                // DIRECT DETAILS
                if (path.equals("/channel/2")) {
                    return json("{\"members\":[" +
                            "{\"id\":1,\"username\":\"me\"}," +
                            "{\"id\":3,\"username\":\"john\"}" +
                            "]}");
                }

                return new MockResponse().setResponseCode(404);
            }
        });

        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        ApiConfig.BASE_URL = BuildConfig.BASE_URL;
    }

    private MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    @Test
    public void chatsLoadAndDisplay() throws InterruptedException {

        Bundle args = new Bundle();
        args.putLong("user_id", 1L);

        FragmentScenario.launchInContainer(ChatListFragment.class, args);

        Thread.sleep(2000); // wait for async

        onView(withText("Group Chat"))
                .check(matches(isDisplayed()));

        onView(withText("john"))
                .check(matches(isDisplayed()));

        onView(withText("Hello group"))
                .check(matches(isDisplayed()));
    }
}