package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
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
public class EditUserActivityMockTest {

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
                String method = request.getMethod();

                if (path.equals("/users/1") && method.equals("GET")) {
                    return json("{\"id\":1,\"username\":\"olduser\",\"emailId\":\"old@test.com\"}");
                }

                if (path.equals("/users/1") && method.equals("PATCH")) {
                    return json("{\"id\":1,\"username\":\"newuser\",\"emailId\":\"new@test.com\"}");
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

        if (server != null) {
            server.shutdown();
        }

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    private static MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    @Test
    public void usernameEditorOpens() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_username))
                .perform(scrollTo(), click());

        onView(withId(R.id.layout_username_editor))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void emailEditorOpens() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_email))
                .perform(scrollTo(), click());

        onView(withId(R.id.layout_email_editor))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void passwordEditorOpens() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_password))
                .perform(scrollTo(), click());

        onView(withId(R.id.layout_password_editor))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void updateUsernameSuccess() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_username))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_username))
                .perform(clearText(), replaceText("newuser"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_username))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.tv_username_value))
                .check(matches(withText("newuser")));
    }

    @Test
    public void updateEmailSuccess() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_email))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_email))
                .perform(clearText(), replaceText("new@test.com"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_email))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.tv_email_value))
                .check(matches(withText("new@test.com")));
    }

    @Test
    public void emptyUsernameDoesNotSubmit() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_username))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_username))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.btn_save_username))
                .perform(scrollTo(), click());
    }

    @Test
    public void emptyEmailDoesNotSubmit() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_email))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_email))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.btn_save_email))
                .perform(scrollTo(), click());
    }

    @Test
    public void emptyPasswordDoesNotSubmit() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_password))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_save_password))
                .perform(scrollTo(), click());
    }

    @Test
    public void updateUsernameError409RunsErrorBranch() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                String method = request.getMethod();

                if (path.equals("/users/1") && method.equals("GET")) {
                    return json("{\"id\":1,\"username\":\"olduser\",\"emailId\":\"old@test.com\"}");
                }

                if (path.equals("/users/1") && method.equals("PATCH")) {
                    return new MockResponse()
                            .setResponseCode(409)
                            .setHeader("Content-Type", "application/json")
                            .setBody("{\"message\":\"Username already exists\"}");
                }

                return new MockResponse().setResponseCode(404).setBody("{}");
            }
        });

        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_username))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_username))
                .perform(clearText(), replaceText("takenuser"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_username))
                .perform(scrollTo(), click());

        Thread.sleep(1000);
    }

    @Test
    public void updatePasswordSuccess() throws Exception {
        ActivityScenario.launch(EditUserActivity.class);

        Thread.sleep(1000);

        onView(withId(R.id.btn_edit_password))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_old_password))
                .perform(replaceText("oldpass"), closeSoftKeyboard());

        onView(withId(R.id.edit_new_password))
                .perform(replaceText("newpass"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_password))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.layout_password_editor))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }


}