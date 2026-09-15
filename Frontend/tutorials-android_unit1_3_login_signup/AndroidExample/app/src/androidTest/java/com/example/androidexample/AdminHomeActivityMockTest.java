package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
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
public class AdminHomeActivityMockTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        Intents.init();

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

                if (path.equals("/users/count")) {
                    return json("{\"numUsers\":7}");
                }

                if (path.equals("/postings/count")) {
                    return json("{\"numpostings\":4}");
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

        try {
            Intents.release();
        } catch (Exception ignored) {
        }

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

    private Intent adminIntent() {
        Intent intent = new Intent(context, AdminHomeActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void adminActionButtonsVisible() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_reported_posts))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_flag_users))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_ban_users))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void reportedPostsButtonOpensReportedPostsActivity() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_reported_posts))
                .perform(scrollTo(), click());

        intended(hasComponent(ReportedPostsActivity.class.getName()));
    }

    @Test
    public void flagUsersButtonOpensFlagUsersActivity() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_flag_users))
                .perform(scrollTo(), click());

        intended(hasComponent(FlagUsersActivity.class.getName()));
    }

    @Test
    public void banUsersButtonOpensBanUsersActivity() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_ban_users))
                .perform(scrollTo(), click());

        intended(hasComponent(BanUsersActivity.class.getName()));
    }

    @Test
    public void profileButtonOpensUserProfileActivity() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_profile))
                .perform(click());

        intended(hasComponent(UserProfileActivity.class.getName()));
    }

    @Test
    public void bottomNavListingsOpensAdminActiveListingsActivity() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.nav_listings))
                .perform(click());

        intended(hasComponent(AdminActiveListingsActivity.class.getName()));
    }

    @Test
    public void bottomNavBannedOpensBannedUserActivity() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.nav_banned))
                .perform(click());

        intended(hasComponent(BannedUserActivity.class.getName()));
    }

    @Test
    public void bottomNavUsersOpensAllUsersActivity() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.nav_users))
                .perform(click());

        intended(hasComponent(AllUsersActivity.class.getName()));
    }
}