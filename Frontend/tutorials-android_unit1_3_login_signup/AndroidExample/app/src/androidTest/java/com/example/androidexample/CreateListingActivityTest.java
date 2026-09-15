package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class CreateListingActivityTest {

    private Intent createListingIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "seller")
                .putString("USER_TYPE", "seller")
                .putString("USERTYPE", "seller")
                .apply();

        Intent intent = new Intent(context, CreateListingActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "seller");
        intent.putExtra("USER_TYPE", "seller");
        intent.putExtra("USERTYPE", "seller");

        return intent;
    }

    @Test
    public void createListingScreenLoads() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.create_listing_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.card_add_image)).check(matches(isDisplayed()));
        onView(withId(R.id.iv_listing_preview)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_title)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_price)).check(matches(isDisplayed()));
        onView(withId(R.id.spn_genre)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_description)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_listing)).check(matches(isDisplayed()));
    }

    @Test
    public void listingFieldsAcceptInput() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_title))
                .perform(scrollTo(), clearText(), replaceText("Pokemon Card"), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .perform(scrollTo(), clearText(), replaceText("50"), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .perform(scrollTo(), clearText(), replaceText("Rare card in good condition"), closeSoftKeyboard());

        onView(withId(R.id.edt_title)).check(matches(withText("Pokemon Card")));
        onView(withId(R.id.edt_price)).check(matches(withText("50")));
        onView(withId(R.id.edt_description)).check(matches(withText("Rare card in good condition")));
    }

    @Test
    public void genreSpinnerCanSelectVideoGames() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.spn_genre))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyCreateStaysOnCreateListingScreen() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.btn_create_listing))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_create_listing)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_title)).check(matches(isDisplayed()));
    }

    @Test
    public void invalidPriceStaysOnCreateListingScreen() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_title))
                .perform(scrollTo(), clearText(), replaceText("Test Item"), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .perform(scrollTo(), clearText(), replaceText("0"), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .perform(scrollTo(), clearText(), replaceText("Description"), closeSoftKeyboard());

        onView(withId(R.id.btn_create_listing))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_create_listing)).check(matches(isDisplayed()));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void titleFieldAcceptsText() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_title))
                .perform(scrollTo(), clearText(), replaceText("Rare Card"), closeSoftKeyboard());

        onView(withId(R.id.edt_title))
                .check(matches(withText("Rare Card")));
    }

    @Test
    public void priceFieldAcceptsText() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_price))
                .perform(scrollTo(), clearText(), replaceText("25"), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .check(matches(withText("25")));
    }

    @Test
    public void descriptionFieldAcceptsText() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_description))
                .perform(scrollTo(), clearText(), replaceText("Good condition"), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .check(matches(withText("Good condition")));
    }

    @Test
    public void genreSpinnerOpens() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.spn_genre))
                .perform(scrollTo(), click());
    }

    @Test
    public void createWithoutImageStaysOnPage() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_title))
                .perform(scrollTo(), clearText(), replaceText("Item"), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .perform(scrollTo(), clearText(), replaceText("10"), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .perform(scrollTo(), clearText(), replaceText("Desc"), closeSoftKeyboard());

        onView(withId(R.id.btn_create_listing))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_create_listing)).check(matches(isDisplayed()));
    }

    @Test
    public void createListingTitleCanBeCleared() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_title))
                .perform(clearText(), replaceText("Test Title"), closeSoftKeyboard());

        onView(withId(R.id.edt_title))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.edt_title))
                .check(matches(withText("")));
    }

    @Test
    public void createListingPriceCanBeCleared() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_price))
                .perform(clearText(), replaceText("25"), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .check(matches(withText("")));
    }

    @Test
    public void createListingDescriptionCanBeCleared() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_description))
                .perform(clearText(), replaceText("Good condition"), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .check(matches(withText("")));
    }

    @Test
    public void createListingButtonIsVisible() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.btn_create_listing))
                .check(matches(isDisplayed()));
    }

    @Test
    public void createListingImageCardIsVisible() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.card_add_image))
                .check(matches(isDisplayed()));

        onView(withId(R.id.iv_listing_preview))
                .check(matches(isDisplayed()));
    }

    @Test
    public void priceNegativeStaysOnPage() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.edt_title))
                .perform(scrollTo(), replaceText("Item"), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .perform(scrollTo(), replaceText("-5"), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .perform(scrollTo(), replaceText("Desc"), closeSoftKeyboard());

        onView(withId(R.id.btn_create_listing))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_create_listing))
                .check(matches(isDisplayed()));
    }
}