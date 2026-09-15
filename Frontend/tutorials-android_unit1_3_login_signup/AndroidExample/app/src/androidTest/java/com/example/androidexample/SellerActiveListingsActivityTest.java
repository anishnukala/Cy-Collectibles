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
public class SellerActiveListingsActivityTest {

    private Intent sellerListingsIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testseller")
                .putString("USERTYPE", "seller")
                .apply();

        Intent intent = new Intent(context, SellerActiveListingsActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testseller");
        intent.putExtra("USERTYPE", "seller");
        return intent;
    }

    @Test
    public void sellerActiveListingsScreenLoads() {
        ActivityScenario.launch(sellerListingsIntent());

        onView(withId(R.id.btn_close))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.title_active_listings))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.subtitle_active_listings))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.listings_container))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.bottom_nav))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void titleTextIsCorrect() {
        ActivityScenario.launch(sellerListingsIntent());

        onView(withId(R.id.title_active_listings))
                .check(matches(withText("Active Listings")));
    }

    @Test
    public void subtitleTextIsCorrect() {
        ActivityScenario.launch(sellerListingsIntent());

        onView(withId(R.id.subtitle_active_listings))
                .check(matches(withText("View and manage your active posts")));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(sellerListingsIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void bottomNavigationVisible() {
        ActivityScenario.launch(sellerListingsIntent());

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }
}