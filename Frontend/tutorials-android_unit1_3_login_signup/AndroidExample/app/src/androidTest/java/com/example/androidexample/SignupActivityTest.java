package com.example.androidexample;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class SignupActivityTest {

    @Rule
    public ActivityScenarioRule<SignupActivity> activityRule =
            new ActivityScenarioRule<>(SignupActivity.class);

    @Test
    public void signupScreenLoads() {
        onView(withId(R.id.signup_login_btn))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(Button.class)));

        onView(withId(R.id.signup_tab_btn))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(Button.class)));

        onView(withId(R.id.signup_username_edt))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(EditText.class)));

        onView(withId(R.id.signup_email_edt))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(EditText.class)));

        onView(withId(R.id.signup_password_edt))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(EditText.class)));

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(EditText.class)));

        onView(withId(R.id.signup_userType_spinner))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(Spinner.class)));

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()))
                .check(matches(isAssignableFrom(Button.class)));
    }

    @Test
    public void usernameFieldAcceptsText() {
        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), replaceText("newuser"), closeSoftKeyboard());

        onView(withId(R.id.signup_username_edt))
                .check(matches(withText("newuser")));
    }

    @Test
    public void emailFieldAcceptsText() {
        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), replaceText("newuser@test.com"), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .check(matches(withText("newuser@test.com")));
    }

    @Test
    public void passwordFieldAcceptsText() {
        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .check(matches(withText("password123")));
    }

    @Test
    public void confirmPasswordFieldAcceptsText() {
        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(withText("password123")));
    }

    @Test
    public void fullSignupFormAcceptsInput() {
        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), replaceText("newuser"), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), replaceText("newuser@test.com"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_username_edt))
                .check(matches(withText("newuser")));

        onView(withId(R.id.signup_email_edt))
                .check(matches(withText("newuser@test.com")));

        onView(withId(R.id.signup_password_edt))
                .check(matches(withText("password123")));

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(withText("password123")));
    }

    @Test
    public void spinnerDefaultsToBuyer() {
        onView(withId(R.id.signup_userType_spinner))
                .check(matches(withSpinnerText(containsString("buyer"))));
    }

    @Test
    public void emptySignupKeepsUserOnSignupScreen() {
        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_username_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupFailsWhenEmpty() {
        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        onView(withId(R.id.signup_username_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptySignupStaysOnPage() {
        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void mismatchedPasswordsStayOnSignupScreen() {
        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), replaceText("newuser"), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), replaceText("newuser@test.com"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), replaceText("wrongpass"), closeSoftKeyboard());

        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupFailsWhenPasswordsDontMatch() {
        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), replaceText("user"), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), replaceText("a@a.com"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), replaceText("123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), replaceText("456"), closeSoftKeyboard());

        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void passwordMismatchStaysOnPage() {
        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), replaceText("user"), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), replaceText("test@test.com"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), replaceText("123456"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), replaceText("wrong"), closeSoftKeyboard());

        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void loginButtonIsClickable() {
        onView(withId(R.id.signup_login_btn))
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @Test
    public void signupButtonsHaveCorrectText() {
        onView(withId(R.id.signup_login_btn))
                .check(matches(withText("LOGIN")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_tab_btn))
                .check(matches(withText("SIGNUP")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_signup_btn))
                .check(matches(withText("SIGNUP")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupUsernameCanBeCleared() {
        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), replaceText("abc"), closeSoftKeyboard());

        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.signup_username_edt))
                .check(matches(withText("")));
    }

    @Test
    public void signupEmailCanBeCleared() {
        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), replaceText("abc@test.com"), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .check(matches(withText("")));
    }

    @Test
    public void signupPasswordCanBeCleared() {
        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .check(matches(withText("")));
    }

    @Test
    public void signupConfirmPasswordCanBeCleared() {
        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(withText("")));
    }
}