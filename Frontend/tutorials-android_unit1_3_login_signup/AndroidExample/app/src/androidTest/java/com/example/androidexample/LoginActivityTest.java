package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private ActivityScenario<LoginActivity> scenario;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intents.init();
        scenario = ActivityScenario.launch(LoginActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        try {
            Intents.release();
        } catch (Exception ignored) {
        }

        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    @Test
    public void loginScreenLoads() {
        onView(withId(R.id.login_tab_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_signup_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.forgot_password_txt)).check(matches(isDisplayed()));
    }

    @Test
    public void loginButtonsHaveCorrectText() {
        onView(withId(R.id.login_tab_btn))
                .check(matches(withText("LOGIN")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_signup_btn))
                .check(matches(withText("SIGNUP")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_login_btn))
                .check(matches(withText("LOGIN")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void usernameHintIsCorrect() {
        onView(withId(R.id.login_username_edt))
                .check(matches(withHint("Username")));
    }

    @Test
    public void passwordHintIsCorrect() {
        onView(withId(R.id.login_password_edt))
                .check(matches(withHint("Password")));
    }

    @Test
    public void forgotPasswordTextIsCorrect() {
        onView(withId(R.id.forgot_password_txt))
                .check(matches(withText("Forgot Password?")));
    }

    @Test
    public void loginFieldsStartEmpty() {
        onView(withId(R.id.login_username_edt))
                .check(matches(withText("")));

        onView(withId(R.id.login_password_edt))
                .check(matches(withText("")));
    }

    @Test
    public void usernameFieldAcceptsText() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), replaceText("testuser"), closeSoftKeyboard());

        onView(withId(R.id.login_username_edt))
                .check(matches(withText("testuser")));
    }

    @Test
    public void passwordFieldAcceptsText() {
        onView(withId(R.id.login_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void bothLoginFieldsAcceptText() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), replaceText("buyer1"), closeSoftKeyboard());

        onView(withId(R.id.login_username_edt))
                .check(matches(withText("buyer1")));

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void loginUsernameCanBeCleared() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), replaceText("buyer1"), closeSoftKeyboard());

        onView(withId(R.id.login_username_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.login_username_edt))
                .check(matches(withText("")));
    }

    @Test
    public void loginPasswordCanBeCleared() {
        onView(withId(R.id.login_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .check(matches(withText("")));
    }

    @Test
    public void emptyLoginStaysOnLoginPage() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void loginFailsWhenUsernameOnly() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), typeText("user"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn))
                .perform(click());

        onView(withId(R.id.login_password_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void loginFailsWhenPasswordOnly() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), typeText("pass"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn))
                .perform(click());

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void partialLoginStaysOnPage() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), typeText("user"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn))
                .perform(click());

        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void invalidCredentialsSubmitDoesNotCrash() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), replaceText("notARealUserForTest"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), replaceText("wrongPassword123"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn))
                .perform(click());

        onView(withId(R.id.login_username_edt))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signupButtonOpensSignupActivity() {
        onView(withId(R.id.login_signup_btn))
                .check(matches(isDisplayed()))
                .perform(click());

        intended(hasComponent(SignupActivity.class.getName()));
    }

    @Test
    public void forgotPasswordOpensForgotPasswordActivity() {
        onView(withId(R.id.forgot_password_txt))
                .check(matches(isDisplayed()))
                .perform(click());

        intended(hasComponent(ForgotPasswordActivity.class.getName()));
    }

    @Test
    public void loginScreenMainViewsRemainVisibleAfterTyping() {
        onView(withId(R.id.login_username_edt))
                .perform(clearText(), replaceText("buyer1"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.login_tab_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_signup_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.forgot_password_txt)).check(matches(isDisplayed()));
    }
}