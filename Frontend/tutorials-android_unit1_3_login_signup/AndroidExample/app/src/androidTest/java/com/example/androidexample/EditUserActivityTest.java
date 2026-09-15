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
public class EditUserActivityTest {

    private Intent editUserIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testuser")
                .putString("EMAIL", "test@test.com")
                .apply();

        return new Intent(context, EditUserActivity.class);
    }

    @Test
    public void editUserScreenLoads() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.edit_user_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_username_value)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_email_value)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_username)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_password)).check(matches(isDisplayed()));
    }

    @Test
    public void usernameEditorOpens() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_username)).perform(click());

        onView(withId(R.id.layout_username_editor)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_username)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save_username)).check(matches(isDisplayed()));
    }

    @Test
    public void emailEditorOpens() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_email)).perform(click());

        onView(withId(R.id.layout_email_editor)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save_email)).check(matches(isDisplayed()));
    }

    @Test
    public void passwordEditorOpens() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_password)).perform(click());

        onView(withId(R.id.layout_password_editor)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_old_password)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_new_password)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save_password)).check(matches(isDisplayed()));
    }

    @Test
    public void usernameFieldAcceptsText() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_username)).perform(click());

        onView(withId(R.id.edit_username))
                .perform(clearText(), typeText("newname"), closeSoftKeyboard());

        onView(withId(R.id.edit_username)).check(matches(withText("newname")));
    }

    @Test
    public void emailFieldAcceptsText() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_email)).perform(click());

        onView(withId(R.id.edit_email))
                .perform(clearText(), typeText("new@test.com"), closeSoftKeyboard());

        onView(withId(R.id.edit_email)).check(matches(withText("new@test.com")));
    }

    @Test
    public void passwordFieldsAcceptText() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_password)).perform(click());

        onView(withId(R.id.edit_old_password))
                .perform(typeText("oldpass"), closeSoftKeyboard());

        onView(withId(R.id.edit_new_password))
                .perform(typeText("newpass"), closeSoftKeyboard());

        onView(withId(R.id.edit_old_password)).check(matches(withText("oldpass")));
        onView(withId(R.id.edit_new_password)).check(matches(withText("newpass")));
    }

    @Test
    public void usernameEditorTogglesClosed() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_username)).perform(click());
        onView(withId(R.id.btn_edit_username)).perform(click());

        onView(withId(R.id.layout_username_editor)).check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void emptyUsernameShowsValidationAndStaysOpen() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_username)).perform(click());

        onView(withId(R.id.edit_username))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.btn_save_username)).perform(click());

        onView(withId(R.id.layout_username_editor))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyEmailShowsValidationAndStaysOpen() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_email)).perform(click());

        onView(withId(R.id.edit_email))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.btn_save_email)).perform(click());

        onView(withId(R.id.layout_email_editor))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyPasswordShowsValidationAndStaysOpen() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_password)).perform(click());

        onView(withId(R.id.edit_old_password))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.edit_new_password))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.btn_save_password)).perform(click());

        onView(withId(R.id.layout_password_editor))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emailEditorTogglesClosed() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_email)).perform(click());
        onView(withId(R.id.btn_edit_email)).perform(click());

        onView(withId(R.id.layout_email_editor))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void passwordEditorTogglesClosed() {
        ActivityScenario.launch(editUserIntent());

        onView(withId(R.id.btn_edit_password)).perform(click());
        onView(withId(R.id.btn_edit_password)).perform(click());

        onView(withId(R.id.layout_password_editor))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }
}