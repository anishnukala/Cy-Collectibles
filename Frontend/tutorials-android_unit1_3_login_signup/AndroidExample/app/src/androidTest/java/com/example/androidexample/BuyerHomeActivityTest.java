package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;

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
public class BuyerHomeActivityTest {

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
    public void buyerHomeActivity_displaysRootLayout() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.buyer_home_root))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerHomeActivity_displaysLogoAndProfileButton() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.img_logo))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_profile))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerHomeActivity_displaysSearchBar() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.search_container))
                .check(matches(isDisplayed()));

        onView(withId(R.id.et_search))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerHomeActivity_searchBarAcceptsText() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.et_search))
                .perform(replaceText("pokemon"), closeSoftKeyboard());

        onView(withId(R.id.et_search))
                .check(matches(withText("pokemon")));
    }

    @Test
    public void buyerHomeActivity_displaysFilterButtons() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.btn_category))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(Button.class)));

        onView(withId(R.id.btn_sort))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(Button.class)));
    }

    @Test
    public void buyerHomeActivity_displaysRecentlyAddedTitle() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.tv_recent))
                .check(matches(withText("Recently Added")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerHomeActivity_displaysListingsContainer() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.scroll_area))
                .check(matches(isDisplayed()));

        onView(withId(R.id.list_container))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerHomeActivity_categoryButtonTextIsCorrect() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.btn_category))
                .check(matches(withText("Category")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerHomeActivity_sortButtonTextIsCorrect() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.btn_sort))
                .check(matches(withText("Sort")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void buyerHomeActivity_searchCanAcceptGenreText() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.et_search))
                .perform(replaceText("cards"), closeSoftKeyboard());

        onView(withId(R.id.et_search))
                .check(matches(withText("cards")));
    }

    @Test
    public void buyerHomeActivity_searchCanAcceptDescriptionText() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withId(R.id.et_search))
                .perform(replaceText("rare"), closeSoftKeyboard());

        onView(withId(R.id.et_search))
                .check(matches(withText("rare")));
    }

    @Test
    public void buyerHomeActivity_recentlyAddedHeaderVisible() {
        ActivityScenario.launch(BuyerHomeActivity.class);

        onView(withText("Recently Added"))
                .check(matches(isDisplayed()));
    }
}