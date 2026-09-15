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
public class SellerHomeActivityTest {

    private Intent sellerIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "seller")
                .putString("USERTYPE", "seller")
                .apply();

        Intent intent = new Intent(context, SellerHomeActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "seller");
        intent.putExtra("USERTYPE", "seller");
        return intent;
    }

    @Test
    public void sellerHomeLoads() {
        ActivityScenario.launch(sellerIntent());

        onView(withId(R.id.seller_home_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_listing)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_dashboard_title)).check(matches(isDisplayed()));
        onView(withId(R.id.stat_chats_value)).check(matches(isDisplayed()));
        onView(withId(R.id.stat_active_listings_value)).check(matches(isDisplayed()));
        onView(withId(R.id.stat_sold_value)).check(matches(isDisplayed()));
        onView(withId(R.id.stat_transactions_value)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void createListingButtonClickable() {
        ActivityScenario.launch(sellerIntent());

        onView(withId(R.id.btn_create_listing)).perform(click());
    }

    @Test
    public void profileButtonClickable() {
        ActivityScenario.launch(sellerIntent());

        onView(withId(R.id.btn_profile)).perform(click());
    }
}