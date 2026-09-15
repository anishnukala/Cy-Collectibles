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
public class ReportedPostsActivityTest {

    private Intent reportedPostsIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, ReportedPostsActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "admin");
        intent.putExtra("USERTYPE", "admin");
        return intent;
    }

    @Test
    public void reportedPostsScreenLoads() {
        ActivityScenario.launch(reportedPostsIntent());

        onView(withId(R.id.report_purchase_root)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.title_reported_posts)).check(matches(isDisplayed()));
        onView(withId(R.id.subtitle_reported_posts)).check(matches(isDisplayed()));
        onView(withId(R.id.reported_posts_container))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void titleTextCorrect() {
        ActivityScenario.launch(reportedPostsIntent());

        onView(withId(R.id.title_reported_posts))
                .check(matches(withText("Reported Purchases")));
    }

    @Test
    public void subtitleTextCorrect() {
        ActivityScenario.launch(reportedPostsIntent());

        onView(withId(R.id.subtitle_reported_posts))
                .check(matches(withText("Verify Reported Posts")));
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(reportedPostsIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void launchWithoutAdminIdDoesNotCrash() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, ReportedPostsActivity.class);

        ActivityScenario.launch(intent);

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
    }

}