package com.example.androidexample;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class ForgotPasswordActivityTest {

    @Test
    public void forgotPasswordScreenLoads() {
        ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withText("Forgot")).check(matches(isDisplayed()));
        onView(withText("Password")).check(matches(isDisplayed()));
        onView(withText("Please enter your registered email ID")).check(matches(isDisplayed()));
        onView(withId(R.id.forgot_email_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.reset_password_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.back_to_login_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void emailFieldAcceptsText() {
        ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withId(R.id.forgot_email_edt))
                .perform(typeText("test@test.com"), closeSoftKeyboard());

        onView(withId(R.id.forgot_email_edt))
                .check(matches(withText("test@test.com")));
    }

    @Test
    public void emptyEmailShowsValidation() {
        ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withId(R.id.reset_password_btn)).perform(click());

        onView(withId(R.id.forgot_email_edt))
                .check(matches(hasErrorText("Email required")));
    }

    @Test
    public void invalidEmailShowsValidation() {
        ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withId(R.id.forgot_email_edt))
                .perform(typeText("bademail"), closeSoftKeyboard());

        onView(withId(R.id.reset_password_btn)).perform(click());

        onView(withId(R.id.forgot_email_edt))
                .check(matches(hasErrorText("Enter valid email")));
    }

    @Test
    public void validEmailKeepsScreenAlive() {
        ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withId(R.id.forgot_email_edt))
                .perform(typeText("test@test.com"), closeSoftKeyboard());

        onView(withId(R.id.reset_password_btn)).perform(click());

        onView(withId(R.id.reset_password_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void backToLoginButtonClickable() {
        ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withId(R.id.back_to_login_btn)).perform(click());
    }
}