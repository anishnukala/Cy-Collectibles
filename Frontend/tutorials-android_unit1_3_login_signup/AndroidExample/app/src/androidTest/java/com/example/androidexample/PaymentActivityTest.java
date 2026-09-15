package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PaymentActivityTest {

    private ActivityScenario<PaymentActivity> scenario;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testbuyer")
                .putString("USER_TYPE", "BUYER")
                .putString("USERTYPE", "BUYER")
                .apply();

        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("cartItems", createSampleCartItems().toString());

        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    @Test
    public void paymentScreenLoads() {
        onView(withId(R.id.tv_payment_title)).check(matches(isDisplayed()));
        onView(withText("Payment Details")).check(matches(isDisplayed()));

        onView(withId(R.id.tv_payment_total)).check(matches(isDisplayed()));
        onView(withId(R.id.et_card_name)).check(matches(isDisplayed()));
        onView(withId(R.id.et_card_number)).check(matches(isDisplayed()));
        onView(withId(R.id.et_expiry)).check(matches(isDisplayed()));
        onView(withId(R.id.et_cvv)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_pay_now)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_payment)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_bottom_payment)).check(matches(isDisplayed()));
    }

    @Test
    public void paymentTotalDisplaysCorrectAmount() {
        onView(withId(R.id.tv_payment_total))
                .check(matches(withText("Total: $35.50")));
    }

    @Test
    public void cardNameAcceptsText() {
        onView(withId(R.id.et_card_name))
                .perform(clearText(), typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.et_card_name))
                .check(matches(withText("John Doe")));
    }

    @Test
    public void cardNumberAcceptsOnlySixteenDigits() {
        onView(withId(R.id.et_card_number))
                .perform(clearText(), typeText("12345678901234567890"), closeSoftKeyboard());

        onView(withId(R.id.et_card_number))
                .check(matches(withText("1234567890123456")));
    }

    @Test
    public void cardNumberRemovesNonDigits() {
        onView(withId(R.id.et_card_number))
                .perform(clearText(), typeText("1234abcd5678"), closeSoftKeyboard());

        onView(withId(R.id.et_card_number))
                .check(matches(withText("12345678")));
    }

    @Test
    public void expiryFormatsWithSlash() {
        onView(withId(R.id.et_expiry))
                .perform(clearText(), typeText("1228"), closeSoftKeyboard());

        onView(withId(R.id.et_expiry))
                .check(matches(withText("12/28")));
    }

    @Test
    public void expiryMonthAboveTwelveBecomesTwelve() {
        onView(withId(R.id.et_expiry))
                .perform(clearText(), typeText("9928"), closeSoftKeyboard());

        onView(withId(R.id.et_expiry))
                .check(matches(withText("12/28")));
    }

    @Test
    public void expiryMonthZeroBecomesOne() {
        onView(withId(R.id.et_expiry))
                .perform(clearText(), typeText("0028"), closeSoftKeyboard());

        onView(withId(R.id.et_expiry))
                .check(matches(withText("01/28")));
    }

    @Test
    public void cvvAcceptsOnlyThreeDigits() {
        onView(withId(R.id.et_cvv))
                .perform(clearText(), typeText("12345"), closeSoftKeyboard());

        onView(withId(R.id.et_cvv))
                .check(matches(withText("123")));
    }

    @Test
    public void payNowFailsWhenCardholderNameMissing() {
        onView(withId(R.id.et_card_name))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.btn_pay_now)).perform(click());

        onView(withId(R.id.et_card_name))
                .check(matches(hasErrorText("Enter cardholder name")));
    }

    @Test
    public void payNowFailsWhenCardNumberInvalid() {
        onView(withId(R.id.et_card_name))
                .perform(clearText(), typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.et_card_number))
                .perform(clearText(), typeText("1234"), closeSoftKeyboard());

        onView(withId(R.id.btn_pay_now)).perform(click());

        onView(withId(R.id.et_card_number))
                .check(matches(hasErrorText("Card number must be 16 digits")));
    }

    @Test
    public void payNowFailsWhenExpiryInvalid() {
        onView(withId(R.id.et_card_name))
                .perform(clearText(), typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.et_card_number))
                .perform(clearText(), typeText("1234567890123456"), closeSoftKeyboard());

        onView(withId(R.id.et_expiry))
                .perform(clearText(), typeText("1"), closeSoftKeyboard());

        onView(withId(R.id.btn_pay_now)).perform(click());

        onView(withId(R.id.et_expiry))
                .check(matches(hasErrorText("Enter expiry in MM/YY")));
    }

    @Test
    public void payNowFailsWhenCvvInvalid() {
        onView(withId(R.id.et_card_name))
                .perform(clearText(), typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.et_card_number))
                .perform(clearText(), typeText("1234567890123456"), closeSoftKeyboard());

        onView(withId(R.id.et_expiry))
                .perform(clearText(), typeText("1228"), closeSoftKeyboard());

        onView(withId(R.id.et_cvv))
                .perform(clearText(), typeText("12"), closeSoftKeyboard());

        onView(withId(R.id.btn_pay_now)).perform(click());

        onView(withId(R.id.et_cvv))
                .check(matches(hasErrorText("CVV must be 3 digits")));
    }

    @Test
    public void topBackButtonClosesActivity() {
        onView(withId(R.id.btn_back_payment)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_payment)).perform(click());
    }

    @Test
    public void bottomBackButtonClosesActivity() {
        onView(withId(R.id.btn_back_bottom_payment)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_bottom_payment)).perform(click());
    }

    @Test
    public void emptyCartShowsZeroTotal() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("cartItems", new JSONArray().toString());

        scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tv_payment_total))
                .check(matches(withText("Total: $0.00")));
    }

    @Test
    public void invalidCartJsonShowsZeroTotal() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("cartItems", "not valid json");

        scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tv_payment_total))
                .check(matches(withText("Total: $0.00")));
    }

    @Test
    public void noCartIntentShowsZeroTotal() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, PaymentActivity.class);

        scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tv_payment_total))
                .check(matches(withText("Total: $0.00")));
    }

    @Test
    public void validInputsButNoUserDoesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();

        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, PaymentActivity.class);

        try {
            intent.putExtra("cartItems", createSampleCartItems().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        scenario = ActivityScenario.launch(intent);

        enterValidPaymentInfo();

        onView(withId(R.id.btn_pay_now)).perform(click());

        onView(withId(R.id.btn_pay_now)).check(matches(isDisplayed()));
    }

    @Test
    public void validInputsButEmptyCartDoesNotCrash() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("cartItems", new JSONArray().toString());

        scenario = ActivityScenario.launch(intent);

        enterValidPaymentInfo();

        onView(withId(R.id.btn_pay_now)).perform(click());

        onView(withId(R.id.btn_pay_now)).check(matches(isDisplayed()));
    }

    @Test
    public void paymentWithInvalidPostingIdReenablesButton() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();

        try {
            item.put("postingId", -1);
            item.put("title", "Invalid Item");
            item.put("genre", "Test");
            item.put("price", 10.00);
            items.put(item);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("cartItems", items.toString());

        scenario = ActivityScenario.launch(intent);

        enterValidPaymentInfo();

        onView(withId(R.id.btn_pay_now)).perform(click());

        onView(withId(R.id.btn_pay_now)).check(matches(isEnabled()));
    }

    private static void enterValidPaymentInfo() {
        onView(withId(R.id.et_card_name))
                .perform(clearText(), typeText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.et_card_number))
                .perform(clearText(), typeText("1234567890123456"), closeSoftKeyboard());

        onView(withId(R.id.et_expiry))
                .perform(clearText(), typeText("1228"), closeSoftKeyboard());

        onView(withId(R.id.et_cvv))
                .perform(clearText(), typeText("123"), closeSoftKeyboard());
    }

    private static JSONArray createSampleCartItems() throws Exception {
        JSONArray items = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("postingId", 1);
        item1.put("title", "Book One");
        item1.put("genre", "Fiction");
        item1.put("price", 20.00);
        item1.put("imageUrl", "images/book1.png");
        item1.put("description", "First book");

        JSONObject item2 = new JSONObject();
        item2.put("postingId", 2);
        item2.put("title", "Book Two");
        item2.put("genre", "Cards");
        item2.put("price", 15.50);
        item2.put("imageUrl", "images/book2.png");
        item2.put("description", "Second book");

        items.put(item1);
        items.put(item2);

        return items;
    }
}