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
public class SellerTransactionsActivityTest {

    private Intent sellerTransactionsIntent(long userId) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, SellerTransactionsActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", "testseller");
        intent.putExtra("USERTYPE", "seller");
        return intent;
    }

    @Test
    public void sellerTransactionsScreenLoads() {
        ActivityScenario.launch(sellerTransactionsIntent(1L));

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.transactions_container)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void invalidUserShowsEmptyMessage() {
        ActivityScenario.launch(sellerTransactionsIntent(-1L));

        onView(withText("Invalid user ID")).check(matches(isDisplayed()));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(sellerTransactionsIntent(1L));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void bottomNavigationVisible() {
        ActivityScenario.launch(sellerTransactionsIntent(1L));

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void transactionsScreenLoads() {
        ActivityScenario.launch(SellerTransactionsActivity.class);

        onView(withId(R.id.btn_close))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.bottom_nav))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void closeButtonVisible() {
        ActivityScenario.launch(sellerTransactionsIntent(1L));

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
    }
}