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
public class NewChatActivityTest {

    private Intent newChatIntent(String mode) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testuser")
                .putString("USERTYPE", "buyer")
                .apply();

        Intent intent = new Intent(context, NewChatActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", "buyer");
        intent.putExtra("MODE", mode);
        return intent;
    }

    @Test
    public void directModeLoads() {
        ActivityScenario.launch(newChatIntent("DIRECT"));

        onView(withId(R.id.btn_back_new_chat)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_new_chat_title)).check(matches(withText("Start Chat")));
        onView(withId(R.id.edt_search_user)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_users)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_chat)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_selected_count)).check(matches(withText("No user selected")));
    }

    @Test
    public void groupModeLoads() {
        ActivityScenario.launch(newChatIntent("GROUP"));

        onView(withId(R.id.txt_new_chat_title)).check(matches(withText("Create Group")));
        onView(withId(R.id.group_name_container)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_group_name)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_selected_count)).check(matches(withText("Selected: 0")));
    }

    @Test
    public void nullModeDefaultsToDirect() {
        ActivityScenario.launch(newChatIntent(null));

        onView(withId(R.id.txt_new_chat_title)).check(matches(withText("Start Chat")));
        onView(withId(R.id.txt_selected_count)).check(matches(withText("No user selected")));
    }

    @Test
    public void emptyModeDefaultsToDirect() {
        ActivityScenario.launch(newChatIntent(""));

        onView(withId(R.id.txt_new_chat_title)).check(matches(withText("Start Chat")));
    }

    @Test
    public void searchFieldAcceptsText() {
        ActivityScenario.launch(newChatIntent("DIRECT"));

        onView(withId(R.id.edt_search_user))
                .perform(typeText("john"), closeSoftKeyboard());

        onView(withId(R.id.edt_search_user))
                .check(matches(withText("john")));
    }

    @Test
    public void searchFieldCanClearText() {
        ActivityScenario.launch(newChatIntent("DIRECT"));

        onView(withId(R.id.edt_search_user))
                .perform(typeText("seller"), closeSoftKeyboard());

        onView(withId(R.id.edt_search_user))
                .perform(clearText());

        onView(withId(R.id.edt_search_user))
                .check(matches(withText("")));
    }

    @Test
    public void groupNameFieldAcceptsText() {
        ActivityScenario.launch(newChatIntent("GROUP"));

        onView(withId(R.id.edt_group_name))
                .perform(typeText("Collectors"), closeSoftKeyboard());

        onView(withId(R.id.edt_group_name))
                .check(matches(withText("Collectors")));
    }

    @Test
    public void createDirectWithoutUserStaysOnPage() {
        ActivityScenario.launch(newChatIntent("DIRECT"));

        onView(withId(R.id.btn_create_chat)).perform(click());

        onView(withId(R.id.btn_create_chat)).check(matches(isDisplayed()));
    }

    @Test
    public void createGroupWithoutNameStaysOnPage() {
        ActivityScenario.launch(newChatIntent("GROUP"));

        onView(withId(R.id.btn_create_chat)).perform(click());

        onView(withId(R.id.btn_create_chat)).check(matches(isDisplayed()));
    }

    @Test
    public void backButtonClickable() {
        ActivityScenario.launch(newChatIntent("DIRECT"));

        onView(withId(R.id.btn_back_new_chat)).perform(click());
    }
}