package com.example.androidexample;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class AnishSystemTest {

    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private void saveSession(String userType) {
        context().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testuser")
                .putString("USERTYPE", userType)
                .apply();
    }

    private Intent chatHomeIntent(String userType) {
        saveSession(userType);

        Intent intent = new Intent(context(), ChatHomeActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", userType);
        return intent;
    }

    private Intent newChatIntent(String mode) {
        saveSession("buyer");

        Intent intent = new Intent(context(), NewChatActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", "buyer");
        intent.putExtra("MODE", mode);
        return intent;
    }

    private Intent createListingIntent() {
        saveSession("seller");

        Intent intent = new Intent(context(), CreateListingActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testseller");
        intent.putExtra("USERTYPE", "seller");
        return intent;
    }

    @Test
    public void buyerCanOpenChatSearchAndChangeFilters() {
        ActivityScenario.launch(chatHomeIntent("buyer"));

        onView(withId(R.id.edt_search_chats))
                .perform(typeText("cards"), closeSoftKeyboard());

        onView(withId(R.id.edt_search_chats))
                .check(matches(withText("cards")));

        onView(withId(R.id.chip_direct)).perform(click());
        onView(withId(R.id.chip_groups)).perform(click());
        onView(withId(R.id.chip_all)).perform(click());

        onView(withId(R.id.chip_all)).check(matches(isDisplayed()));
    }

    @Test
    public void sellerCanOpenCreateChatMenu() {
        ActivityScenario.launch(chatHomeIntent("seller"));

        onView(withId(R.id.btn_new_chat_fab)).perform(click());

        onView(withText("New Group")).check(matches(isDisplayed()));
        onView(withText("New Chat")).check(matches(isDisplayed()));
        onView(withText("CyBot AI")).check(matches(isDisplayed()));
    }

    @Test
    public void groupChatRequiresGroupNameBeforeCreate() {
        ActivityScenario.launch(newChatIntent("GROUP"));

        onView(withId(R.id.txt_new_chat_title))
                .check(matches(withText("Create Group")));

        onView(withId(R.id.btn_create_chat)).perform(click());

        onView(withId(R.id.btn_create_chat)).check(matches(isDisplayed()));

        onView(withId(R.id.edt_group_name))
                .perform(typeText("Collectors"), closeSoftKeyboard());

        onView(withId(R.id.edt_group_name))
                .check(matches(withText("Collectors")));
    }

    @Test
    public void createListingValidatesRequiredImageAndFields() {
        ActivityScenario.launch(createListingIntent());

        onView(withId(R.id.btn_create_listing)).perform(click());

        onView(withId(R.id.btn_create_listing)).check(matches(isDisplayed()));

        onView(withId(R.id.edt_title))
                .perform(typeText("Pokemon Card"), closeSoftKeyboard());

        onView(withId(R.id.edt_price))
                .perform(typeText("25"), closeSoftKeyboard());

        onView(withId(R.id.edt_description))
                .perform(typeText("Rare collectible card"), closeSoftKeyboard());

        onView(withId(R.id.edt_title)).check(matches(withText("Pokemon Card")));
        onView(withId(R.id.edt_price)).check(matches(withText("25")));
        onView(withId(R.id.edt_description)).check(matches(withText("Rare collectible card")));
    }
}