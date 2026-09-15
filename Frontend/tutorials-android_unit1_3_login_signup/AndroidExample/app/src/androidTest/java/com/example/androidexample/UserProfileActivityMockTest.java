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
public class UserProfileActivityMockTest {

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

                if (path.equals("/users/1")) {
                    return json("{\"id\":1,\"username\":\"testuser\",\"userType\":\"buyer\",\"emailId\":\"test@test.com\",\"flagCount\":2}");
                }

                if (path.equals("/favourites/1")) {
                    return json("[{\"category\":\"Cards\"}]");
                }

                if (path.equals("/postings/genre")) {
                    return json("[\"Cards\",\"Books\",\"Games\"]");
                }

                return new MockResponse().setResponseCode(200).setBody("{}");
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

    private Intent profileIntent() {
        return new Intent(context, UserProfileActivity.class);
    }

    @Test
    public void mockProfileLoadsUserData() throws Exception {
        ActivityScenario.launch(profileIntent());

        Thread.sleep(1000);

        onView(withId(R.id.profile_username))
                .check(matches(withText("Username: testuser")));

        onView(withId(R.id.profile_usertype))
                .check(matches(withText("User Type: buyer")));

        onView(withId(R.id.profile_email))
                .check(matches(withText("Email: test@test.com")));

        onView(withId(R.id.profile_flagcount))
                .check(matches(withText("Flag Count: 2")));
    }

    @Test
    public void genreButtonShowsFavourite() throws Exception {
        ActivityScenario.launch(profileIntent());

        Thread.sleep(1000);

        onView(withId(R.id.btn_genre_select))
                .perform(scrollTo())
                .check(matches(withText("Cards")));
    }

    @Test
    public void genreDialogOpens() throws Exception {
        ActivityScenario.launch(profileIntent());

        Thread.sleep(1000);

        onView(withId(R.id.btn_genre_select))
                .perform(scrollTo(), click());

        onView(withText("Select Favourite Genres"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void editButtonClickable() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_edit_user))
                .perform(scrollTo(), click());
    }

    @Test
    public void deleteDialogOpens() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_delete_account))
                .perform(scrollTo(), click());

        onView(withText("Delete Profile"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void logoutButtonClickable() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_logout))
                .perform(scrollTo(), click());
    }
}