package com.example.androidexample;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class AdminHomeActivityTest {

    private Intent adminIntent() {
        Intent intent = new Intent(
                androidx.test.platform.app.InstrumentationRegistry
                        .getInstrumentation()
                        .getTargetContext(),
                AdminHomeActivity.class
        );

        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void adminHomeLoads() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.tv_dashboard_title)).check(matches(isDisplayed()));
        onView(withText("Admin Dashboard")).check(matches(isDisplayed()));
        onView(withId(R.id.tv_active_users_count)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_active_listings_count)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_reported_posts)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_flag_users)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_ban_users)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void adminActionButtonsClickable() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_reported_posts)).perform(click());
    }

    @Test
    public void flagUsersButtonClickable() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_flag_users)).perform(click());
    }

    @Test
    public void banUsersButtonClickable() {
        ActivityScenario.launch(adminIntent());

        onView(withId(R.id.btn_ban_users)).perform(click());
    }
}