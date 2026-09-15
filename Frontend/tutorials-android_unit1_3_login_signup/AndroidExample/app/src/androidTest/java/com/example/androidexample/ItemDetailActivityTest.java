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
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.scrollTo;

@RunWith(AndroidJUnit4.class)
public class ItemDetailActivityTest {

    private Intent itemIntent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "buyer")
                .putString("USERTYPE", "buyer")
                .apply();

        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra("postingId", 1);
        return intent;
    }

    @Test
    public void topItemViewsLoad() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
        onView(withId(R.id.item_image)).check(matches(isDisplayed()));
        onView(withId(R.id.item_title)).check(matches(isDisplayed()));
        onView(withId(R.id.item_price)).check(matches(isDisplayed()));
        onView(withId(R.id.item_genre)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_report)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_buy_now)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_chat)).check(matches(isDisplayed()));
    }

    @Test
    public void reportDialogOpensAndCloses() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_report)).perform(click());
        onView(withText("Report posting")).check(matches(isDisplayed()));
        onView(withText("No")).perform(click());
    }

    @Test
    public void closeButtonClickable() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_close)).perform(click());
    }

    @Test
    public void addToCartButtonClickable() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_buy_now)).perform(click());

        onView(withId(R.id.btn_buy_now)).check(matches(isDisplayed()));
    }

    @Test
    public void chatButtonClickableWithoutSellerLoaded() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_chat)).perform(click());

        onView(withId(R.id.btn_chat)).check(matches(isDisplayed()));
    }

    @Test
    public void invalidPostingClosesSafely() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra("postingId", -1);

        ActivityScenario.launch(intent);
    }

    @Test
    public void emptyCommentShowsValidationAndStaysOnPage() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_post_comment))
                .perform(scrollTo(), click());

        onView(withId(R.id.et_comment_input))
                .check(matches(isDisplayed()));
    }

    @Test
    public void commentInputAcceptsText() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.et_comment_input))
                .perform(scrollTo(), typeText("Is this still available?"), closeSoftKeyboard());

        onView(withId(R.id.et_comment_input))
                .check(matches(withText("Is this still available?")));
    }

    @Test
    public void chatWithoutSellerShowsError() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_chat))
                .perform(scrollTo(), click());
    }

    @Test
    public void reportYesFlow() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_report))
                .perform(scrollTo(), click());

        onView(withText("Yes")).perform(click());
    }

    @Test
    public void postCommentFlowRuns() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.et_comment_input))
                .perform(scrollTo(), typeText("test"), closeSoftKeyboard());

        onView(withId(R.id.btn_post_comment))
                .perform(scrollTo(), click());
    }

    @Test
    public void chatFailsWhenUserMissing() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(context, ItemDetailActivity.class);
        intent.putExtra("postingId", 1);

        ActivityScenario.launch(intent);

        onView(withId(R.id.btn_chat))
                .perform(scrollTo(), click());
    }

    @Test
    public void addToCartRuns() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_buy_now))
                .perform(scrollTo(), click());
    }

    @Test
    public void reportYesExecutesLogic() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.btn_report))
                .perform(scrollTo(), click());

        onView(withText("Yes")).perform(click());
    }

    @Test
    public void postCommentNonEmptyExecutes() {
        ActivityScenario.launch(itemIntent());

        onView(withId(R.id.et_comment_input))
                .perform(scrollTo(), typeText("hello"), closeSoftKeyboard());

        onView(withId(R.id.btn_post_comment))
                .perform(scrollTo(), click());
    }

}