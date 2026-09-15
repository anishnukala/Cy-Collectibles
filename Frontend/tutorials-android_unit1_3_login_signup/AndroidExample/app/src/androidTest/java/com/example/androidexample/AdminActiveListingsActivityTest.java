package com.example.androidexample;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class AdminActiveListingsActivityTest {

    private Intent adminListingsIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, AdminActiveListingsActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void adminActiveListingsScreenLoads() {
        ActivityScenario.launch(adminListingsIntent());

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withText("Active Listings")).check(matches(isDisplayed()));
        onView(withId(R.id.listings_container))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(adminListingsIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void bottomNavigationVisible() {
        ActivityScenario.launch(adminListingsIntent());

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void listingsContainerExists() {
        ActivityScenario.launch(adminListingsIntent());

        onView(withId(R.id.listings_container))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void activeListingsTitleTextVisible() {
        ActivityScenario.launch(adminListingsIntent());

        onView(withText("Active Listings"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void navBannedVisible() {
        ActivityScenario.launch(adminListingsIntent());

        onView(withId(R.id.nav_banned))
                .check(matches(isDisplayed()));
    }

    @Test
    public void navUsersVisible() {
        ActivityScenario.launch(adminListingsIntent());

        onView(withId(R.id.nav_users))
                .check(matches(isDisplayed()));
    }

    @Test
    public void adminListingsLaunchWithNullUsernameAndUserType() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, AdminActiveListingsActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", (String) null);
        intent.putExtra("USERTYPE", (String) null);

        ActivityScenario.launch(intent);

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
    }
}