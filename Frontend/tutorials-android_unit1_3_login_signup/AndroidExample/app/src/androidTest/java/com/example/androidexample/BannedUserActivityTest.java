package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class BannedUserActivityTest {

    private Intent bannedUsersIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, BannedUserActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void bannedUsersScreenLoads() {
        ActivityScenario.launch(bannedUsersIntent());

        onView(withId(R.id.banned_user_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.title_banned_users)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void titleTextCorrect() {
        ActivityScenario.launch(bannedUsersIntent());

        onView(withId(R.id.title_banned_users))
                .check(matches(withText("Banned Users")));
    }

    @Test
    public void subtitleTextVisible() {
        ActivityScenario.launch(bannedUsersIntent());

        onView(withText("View all banned accounts"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void bottomNavigationVisible() {
        ActivityScenario.launch(bannedUsersIntent());

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void bannedUsersContainerExists() {
        ActivityScenario.launch(bannedUsersIntent());

        onView(withId(R.id.banned_users_container))
                .check(matches(isAssignableFrom(View.class)));
    }
}