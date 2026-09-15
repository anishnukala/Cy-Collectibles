package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BuyerTransactionsActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testbuyer")
                .putString("USER_TYPE", "BUYER")
                .putString("USERTYPE", "BUYER")
                .apply();
    }

    @After
    public void tearDown() {
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    @Test
    public void buyerTransactionsScreenLoads() {
        ActivityScenario.launch(BuyerTransactionsActivity.class);

        onView(withId(R.id.transactions_container))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.bottom_nav))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.btn_close))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void buyerTransactionsRequiredViewsDisplayed() {
        ActivityScenario.launch(BuyerTransactionsActivity.class);

        onView(withId(R.id.btn_close))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(Button.class)));

        onView(withId(R.id.transactions_container))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(LinearLayout.class)));

        onView(withId(R.id.bottom_nav))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(BottomNavigationView.class)));
    }

    @Test
    public void buyerTransactionsContainerVisibleAfterLoad() {
        ActivityScenario.launch(BuyerTransactionsActivity.class);

        onView(withId(R.id.transactions_container))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void buyerTransactionsBottomNavVisible() {
        ActivityScenario.launch(BuyerTransactionsActivity.class);

        onView(withId(R.id.bottom_nav))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerTransactionsCloseButtonClickable() {
        ActivityScenario.launch(BuyerTransactionsActivity.class);

        onView(withId(R.id.btn_close))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @Test
    public void buyerTransactionsInvalidUserShowsMessage() {
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .clear()
                .putLong("USER_ID", -1L)
                .apply();

        ActivityScenario.launch(BuyerTransactionsActivity.class);

        onView(withText("Invalid user ID"))
                .check(matches(isDisplayed()));
    }
}