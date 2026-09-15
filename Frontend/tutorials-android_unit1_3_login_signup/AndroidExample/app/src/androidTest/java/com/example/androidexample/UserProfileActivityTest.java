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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest {

    private Intent profileIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testuser")
                .putString("USERTYPE", "buyer")
                .putString("EMAIL", "test@test.com")
                .apply();

        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", "buyer");
        return intent;
    }


    @Test
    public void genreButtonVisible() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_genre_select))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void accountButtonsVisible() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_edit_user))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_logout))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_delete_account))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void editProfileButtonClickable() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_edit_user))
                .perform(scrollTo(), click());
    }

    @Test
    public void deleteProfileDialogOpensAndCancels() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_delete_account))
                .perform(scrollTo(), click());

        onView(withText("Delete Profile")).check(matches(isDisplayed()));
        onView(withText("Cancel")).perform(click());
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void profileTopViewsExist() {
        ActivityScenario.launch(profileIntent());

        onView(withId(R.id.user_profile_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_image)).check(matches(isDisplayed()));
    }
}