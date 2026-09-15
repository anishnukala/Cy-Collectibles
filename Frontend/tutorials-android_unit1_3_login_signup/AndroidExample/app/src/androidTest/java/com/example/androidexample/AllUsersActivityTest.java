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
import android.widget.LinearLayout;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;

@RunWith(AndroidJUnit4.class)
public class AllUsersActivityTest {

    private Intent allUsersIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, AllUsersActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void titleTextCorrect() {
        ActivityScenario.launch(allUsersIntent());

        onView(withId(R.id.title_all_users))
                .check(matches(withText("All Users")));
    }

    @Test
    public void subtitleTextCorrect() {
        ActivityScenario.launch(allUsersIntent());

        onView(withId(R.id.subtitle_active_listings))
                .check(matches(withText("View all User accounts")));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(allUsersIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void bottomNavigationVisible() {
        ActivityScenario.launch(allUsersIntent());

        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void usersContainerVisible() {
        ActivityScenario.launch(allUsersIntent());

        onView(withId(R.id.users_container))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void allUsersScreenLoads() {
        ActivityScenario.launch(allUsersIntent());

        onView(withId(R.id.all_users_root))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.btn_close))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        onView(withId(R.id.title_all_users))
                .check(matches(withText("All Users")));

        onView(withId(R.id.bottom_nav))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void usersContainerExists() {
        ActivityScenario.launch(allUsersIntent());

        onView(withId(R.id.users_container))
                .check(matches(isAssignableFrom(LinearLayout.class)));
    }
}