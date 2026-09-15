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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ChatHomeActivityTest {

    private Intent chatIntent(String userType) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "test")
                .putString("USERTYPE", userType)
                .apply();

        Intent intent = new Intent(context, ChatHomeActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "test");
        intent.putExtra("USERTYPE", userType);

        return intent;
    }

    @Test
    public void chatHomeLoadsForBuyer() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_chat_title)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_search_chats)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_all)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_direct)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_groups)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_new_chat_fab)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void chatHomeLoadsForSeller() {
        ActivityScenario.launch(chatIntent("seller"));

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_chat_title)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_search_chats)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_all)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_direct)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_groups)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_new_chat_fab)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void chipDirectClickable() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.chip_direct)).perform(click());
        onView(withId(R.id.chip_direct)).check(matches(isDisplayed()));
    }

    @Test
    public void chipGroupsClickable() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.chip_groups)).perform(click());
        onView(withId(R.id.chip_groups)).check(matches(isDisplayed()));
    }

    @Test
    public void chipAllClickable() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.chip_all)).perform(click());
        onView(withId(R.id.chip_all)).check(matches(isDisplayed()));
    }

    @Test
    public void allChipsClickableInSequence() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.chip_direct)).perform(click());
        onView(withId(R.id.chip_groups)).perform(click());
        onView(withId(R.id.chip_all)).perform(click());

        onView(withId(R.id.chip_all)).check(matches(isDisplayed()));
    }

    @Test
    public void buyerBottomNavVisible() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void sellerBottomNavVisible() {
        ActivityScenario.launch(chatIntent("seller"));

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(chatIntent("seller"));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void createMenuDisplaysOptions() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.btn_new_chat_fab)).perform(click());

        onView(withText("New Group")).check(matches(isDisplayed()));
        onView(withText("New Chat")).check(matches(isDisplayed()));
        onView(withText("CyBot AI")).check(matches(isDisplayed()));
    }

    @Test
    public void createMenuNewChatClickable() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.btn_new_chat_fab)).perform(click());
        onView(withText("New Chat")).perform(click());
    }

    @Test
    public void createMenuNewGroupClickable() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.btn_new_chat_fab)).perform(click());
        onView(withText("New Group")).perform(click());
    }

    @Test
    public void createMenuCyBotClickable() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.btn_new_chat_fab)).perform(click());
        onView(withText("CyBot AI")).perform(click());
    }

    @Test
    public void buyerBottomNavItemsClickable() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.nav_buyer_postings)).perform(click());
    }

    @Test
    public void sellerBottomNavItemsClickable() {
        ActivityScenario.launch(chatIntent("seller"));

        onView(withId(R.id.nav_seller_dashboard)).perform(click());
    }
}