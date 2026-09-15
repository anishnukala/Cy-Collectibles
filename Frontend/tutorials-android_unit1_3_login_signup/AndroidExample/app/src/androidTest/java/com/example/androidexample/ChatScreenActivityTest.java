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
public class ChatScreenActivityTest {

    private Intent chatScreenIntent(int channelId, long userId) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, ChatScreenActivity.class);
        intent.putExtra("CHANNEL_ID", channelId);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", "buyer");
        intent.putExtra("CHAT_NAME", "Test Chat");
        return intent;
    }

    @Test
    public void chatScreenLoads() {
        ActivityScenario.launch(chatScreenIntent(1, 1L));

        onView(withId(R.id.btn_chat_back)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_chat_screen_title)).check(matches(withText("Test Chat")));
        onView(withId(R.id.recycler_messages)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_message_input)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_send_message)).check(matches(isDisplayed()));
    }

    @Test
    public void chatNameDefaultsWhenMissing() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, ChatScreenActivity.class);
        intent.putExtra("CHANNEL_ID", 1);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", "buyer");

        ActivityScenario.launch(intent);

        onView(withId(R.id.txt_chat_screen_title)).check(matches(withText("Chat")));
    }

    @Test
    public void messageInputAcceptsText() {
        ActivityScenario.launch(chatScreenIntent(1, 1L));

        onView(withId(R.id.edt_message_input))
                .perform(typeText("hello"), closeSoftKeyboard());

        onView(withId(R.id.edt_message_input))
                .check(matches(withText("hello")));
    }

    @Test
    public void emptyMessageSendDoesNotCrash() {
        ActivityScenario.launch(chatScreenIntent(1, 1L));

        onView(withId(R.id.btn_send_message)).perform(click());

        onView(withId(R.id.edt_message_input)).check(matches(isDisplayed()));
    }

    @Test
    public void missingChatInfoStaysOnScreen() {
        ActivityScenario.launch(chatScreenIntent(-1, -1L));

        onView(withId(R.id.edt_message_input)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_send_message)).check(matches(isDisplayed()));
    }

    @Test
    public void cancelReplyButtonExists() {
        ActivityScenario.launch(chatScreenIntent(1, 1L));

        onView(withId(R.id.reply_preview_container))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void backButtonClickable() {
        ActivityScenario.launch(chatScreenIntent(1, 1L));

        onView(withId(R.id.btn_chat_back)).perform(click());
    }
}