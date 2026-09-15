package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AiChatActivityTest {

    private ActivityScenario<AiChatActivity> scenario;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testbuyer")
                .putString("USERTYPE", "buyer")
                .apply();

        Intent intent = new Intent(context, AiChatActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testbuyer");
        intent.putExtra("USERTYPE", "buyer");

        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    @Test
    public void aiChatScreenLoads() {
        onView(withId(R.id.btn_ai_back)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_ai_chat_title)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_ai_messages)).check(matches(isDisplayed()));
        onView(withId(R.id.edt_ai_message_input)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_send_ai_message)).check(matches(isDisplayed()));
    }

    @Test
    public void titleTextCorrect() {
        onView(withId(R.id.txt_ai_chat_title))
                .check(matches(withText("CyBot")));
    }

    @Test
    public void messageInputVisible() {
        onView(withId(R.id.edt_ai_message_input))
                .check(matches(isDisplayed()));
    }

    @Test
    public void messageInputAcceptsText() {
        onView(withId(R.id.edt_ai_message_input))
                .perform(clearText(), typeText("What should I buy?"), closeSoftKeyboard());

        onView(withId(R.id.edt_ai_message_input))
                .check(matches(withText("What should I buy?")));
    }

    @Test
    public void sendButtonVisible() {
        onView(withId(R.id.btn_send_ai_message))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyMessageInputCanStayEmpty() {
        onView(withId(R.id.edt_ai_message_input))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.edt_ai_message_input))
                .check(matches(withText("")));

        onView(withId(R.id.btn_send_ai_message))
                .check(matches(isDisplayed()));
    }

    @Test
    public void whitespaceMessageCanBeTyped() {
        onView(withId(R.id.edt_ai_message_input))
                .perform(clearText(), typeText("   "), closeSoftKeyboard());

        onView(withId(R.id.edt_ai_message_input))
                .check(matches(withText("   ")));

        onView(withId(R.id.btn_send_ai_message))
                .check(matches(isDisplayed()));
    }

    @Test
    public void backButtonClosesActivity() {
        onView(withId(R.id.btn_ai_back)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_ai_back)).perform(click());
    }

    @Test
    public void aiChatMessageUserIsSentByMe() {
        AiChatMessage message = new AiChatMessage(
                1,
                "user",
                "hello",
                "2026-05-02"
        );

        assertEquals(1, message.getAiMessageId());
        assertEquals("user", message.getRole());
        assertEquals("hello", message.getContent());
        assertEquals("2026-05-02", message.getCreatedAt());
        assertTrue(message.isSentByMe());
    }

    @Test
    public void aiChatMessageModelIsNotSentByMe() {
        AiChatMessage message = new AiChatMessage(
                2,
                "model",
                "Hi, how can I help?",
                "2026-05-02"
        );

        assertEquals(2, message.getAiMessageId());
        assertEquals("model", message.getRole());
        assertEquals("Hi, how can I help?", message.getContent());
        assertEquals("2026-05-02", message.getCreatedAt());
        assertFalse(message.isSentByMe());
    }

    @Test
    public void aiChatMessageRoleCheckIgnoresCase() {
        AiChatMessage message = new AiChatMessage(
                3,
                "USER",
                "case test",
                ""
        );

        assertTrue(message.isSentByMe());
    }

    @Test
    public void aiChatAdapterReturnsCorrectItemCount() {
        List<AiChatMessage> messages = new ArrayList<>();
        messages.add(new AiChatMessage(1, "user", "Hello", ""));
        messages.add(new AiChatMessage(2, "model", "Hi there", ""));

        AiChatAdapter adapter = new AiChatAdapter(messages);

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void aiChatAdapterReturnsZeroForNullList() {
        AiChatAdapter adapter = new AiChatAdapter(null);

        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void aiChatAdapterUsesDifferentViewTypesForUserAndAi() {
        List<AiChatMessage> messages = new ArrayList<>();
        messages.add(new AiChatMessage(1, "user", "User message", ""));
        messages.add(new AiChatMessage(2, "model", "AI message", ""));

        AiChatAdapter adapter = new AiChatAdapter(messages);

        int userType = adapter.getItemViewType(0);
        int aiType = adapter.getItemViewType(1);

        assertTrue(userType != aiType);
    }
}