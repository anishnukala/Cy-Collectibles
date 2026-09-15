package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public class FlagUsersActivityMockTest {

    private MockWebServer server;

    private Intent getIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, FlagUsersActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();

                if (path.equals("/users")) {
                    return json("[{" +
                            "\"id\":2," +
                            "\"username\":\"userA\"," +
                            "\"flagCount\":1," +
                            "\"userType\":\"buyer\"" +
                            "},{" +
                            "\"id\":3," +
                            "\"username\":\"userB\"," +
                            "\"flagCount\":2," +
                            "\"userType\":\"seller\"" +
                            "}]");
                }

                if (path.startsWith("/flag/user/")) {
                    return json("{}");
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
    public void teardown() throws Exception {
        ApiConfig.BASE_URL = BuildConfig.BASE_URL;

        if (server != null) {
            server.shutdown();
        }
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    @Test
    public void usersLoadAndDisplay() throws Exception {
        ActivityScenario.launch(getIntent());

        Thread.sleep(1500);

        onView(withText("userA\nFlag count: 1"))
                .check(matches(isDisplayed()));

        onView(withText("userB\nFlag count: 2"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void selectUser_opensDialog() throws Exception {
        ActivityScenario.launch(getIntent());

        Thread.sleep(1500);

        onView(withText("userA\nFlag count: 1"))
                .perform(click());

        onView(withId(R.id.btn_flag_selected))
                .perform(click());

        onView(withText("Flag user"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyReason_showsError() throws Exception {
        ActivityScenario.launch(getIntent());

        Thread.sleep(1500);

        onView(withText("userA\nFlag count: 1"))
                .perform(click());

        onView(withId(R.id.btn_flag_selected))
                .perform(click());

        onView(withId(R.id.btn_confirm_flag))
                .perform(click());

        onView(withId(R.id.et_flag_reason_dialog))
                .check(matches(hasErrorText("Reason is required")));
    }

    @Test
    public void validReason_triggersConfirmDialog() throws Exception {
        ActivityScenario.launch(getIntent());

        Thread.sleep(1500);

        onView(withText("userA\nFlag count: 1"))
                .perform(click());

        onView(withId(R.id.btn_flag_selected))
                .perform(click());

        onView(withId(R.id.et_flag_reason_dialog))
                .perform(typeText("spam"), closeSoftKeyboard());

        onView(withId(R.id.btn_confirm_flag))
                .perform(click());

        onView(withText("Confirm flag"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void confirmFlag_callsApi() throws Exception {
        ActivityScenario.launch(getIntent());

        Thread.sleep(1500);

        onView(withText("userA\nFlag count: 1"))
                .perform(click());

        onView(withId(R.id.btn_flag_selected))
                .perform(click());

        onView(withId(R.id.et_flag_reason_dialog))
                .perform(typeText("spam"), closeSoftKeyboard());

        onView(withId(R.id.btn_confirm_flag))
                .perform(click());

        onView(withText("Yes"))
                .perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.btn_flag_selected))
                .check(matches(isDisplayed()));
    }
}