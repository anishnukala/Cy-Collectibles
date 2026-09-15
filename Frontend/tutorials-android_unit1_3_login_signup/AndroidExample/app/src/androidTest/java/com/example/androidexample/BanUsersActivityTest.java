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
public class BanUsersActivityTest {

    private Intent banUsersIntent(long userId, String userType) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, BanUsersActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", userType);
        return intent;
    }

    @Test
    public void banUsersScreenLoads() {
        ActivityScenario.launch(banUsersIntent(1L, "admin"));

        onView(withId(R.id.ban_user_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_ban_title)).check(matches(isDisplayed()));
        onView(withId(R.id.users_radio_group)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_ban_selected)).check(matches(isDisplayed()));
    }

    @Test
    public void titleTextCorrect() {
        ActivityScenario.launch(banUsersIntent(1L, "admin"));

        onView(withId(R.id.tv_ban_title))
                .check(matches(withText("Ban Users")));
    }

    @Test
    public void banButtonTextCorrect() {
        ActivityScenario.launch(banUsersIntent(1L, "admin"));

        onView(withId(R.id.btn_ban_selected))
                .check(matches(withText("Ban Selected User")));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(banUsersIntent(1L, "admin"));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void banWithoutSelectingUserStaysOnPage() {
        ActivityScenario.launch(banUsersIntent(1L, "admin"));

        onView(withId(R.id.btn_ban_selected)).perform(click());

        onView(withId(R.id.btn_ban_selected)).check(matches(isDisplayed()));
    }

    @Test
    public void nonAdminCannotBanAndStaysOnPage() {
        ActivityScenario.launch(banUsersIntent(1L, "buyer"));

        onView(withId(R.id.btn_ban_selected)).perform(click());

        onView(withId(R.id.btn_ban_selected)).check(matches(isDisplayed()));
    }

    @Test
    public void missingAdminIdStaysOnPage() {
        ActivityScenario.launch(banUsersIntent(-1L, "admin"));

        onView(withId(R.id.btn_ban_selected)).perform(click());

        onView(withId(R.id.btn_ban_selected)).check(matches(isDisplayed()));
    }
}