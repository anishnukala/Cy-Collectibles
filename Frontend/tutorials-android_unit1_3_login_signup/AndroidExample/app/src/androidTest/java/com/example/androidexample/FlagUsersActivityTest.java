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
public class FlagUsersActivityTest {

    private Intent flagIntent(String userType, long userId) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, FlagUsersActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", userType);
        return intent;
    }

    @Test
    public void flagUsersScreenLoads() {
        ActivityScenario.launch(flagIntent("admin", 1L));

        onView(withId(R.id.flag_user_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_flag_title)).check(matches(withText("Flag Users")));
        onView(withId(R.id.users_radio_group))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        onView(withId(R.id.btn_flag_selected)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(flagIntent("admin", 1L));

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void flagButtonWithoutSelectionStaysOnPage() {
        ActivityScenario.launch(flagIntent("admin", 1L));

        onView(withId(R.id.btn_flag_selected)).perform(click());

        onView(withId(R.id.btn_flag_selected)).check(matches(isDisplayed()));
    }

    @Test
    public void missingAdminIdStaysOnPage() {
        ActivityScenario.launch(flagIntent("admin", -1L));

        onView(withId(R.id.btn_flag_selected)).perform(click());

        onView(withId(R.id.btn_flag_selected)).check(matches(isDisplayed()));
    }

    @Test
    public void nonAdminUserStaysOnPage() {
        ActivityScenario.launch(flagIntent("buyer", 1L));

        onView(withId(R.id.btn_flag_selected)).perform(click());

        onView(withId(R.id.btn_flag_selected)).check(matches(isDisplayed()));
    }

    @Test
    public void bottomNavigationVisible() {
        ActivityScenario.launch(flagIntent("admin", 1L));

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void usersRadioGroupVisible() {
        ActivityScenario.launch(flagIntent("admin", 1L));

        onView(withId(R.id.users_radio_group))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }
}