package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(AndroidJUnit4.class)
public class LoginActivityMockTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        ApiConfig.BASE_URL = server.url("").toString().replaceAll("/$", "");

        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    @After
    public void tearDown() throws Exception {
        ApiConfig.BASE_URL = BuildConfig.BASE_URL;
        if (server != null) server.shutdown();
    }

    @Test
    public void buyerLoginStaysStable() throws Exception {
        server.enqueue(json("{\"id\":1,\"userType\":\"buyer\",\"banned\":false}"));

        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText("buyer1"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("pass"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());

        Thread.sleep(1000);
    }

    @Test
    public void sellerLoginStaysStable() throws Exception {
        server.enqueue(json("{\"id\":2,\"userType\":\"seller\",\"banned\":false}"));

        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText("seller1"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("pass"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());

        Thread.sleep(1000);
    }

    @Test
    public void adminLoginStaysStable() throws Exception {
        server.enqueue(json("{\"id\":3,\"userType\":\"admin\",\"banned\":false}"));

        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText("admin1"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("pass"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());

        Thread.sleep(1000);
    }

    @Test
    public void bannedUserStaysOnLogin() throws Exception {
        server.enqueue(json("{\"id\":4,\"userType\":\"buyer\",\"banned\":true}"));

        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText("banned"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("pass"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void missingUserIdStaysOnLogin() throws Exception {
        server.enqueue(json("{\"userType\":\"buyer\",\"banned\":false}"));

        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText("nouserid"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("pass"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void invalidLogin400StaysOnLogin() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"Invalid input\"}"));

        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText("bad"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("bad"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void invalidLogin401StaysOnLogin() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"Wrong password\"}"));

        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(replaceText("bad"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("bad"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}