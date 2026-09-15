package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class PrajwalSystemTest {

    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private void saveSession(String userType) {
        SharedPreferences prefs = context().getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testuser")
                .putString("USERTYPE", userType)
                .putString("USER_TYPE", userType)
                .apply();
    }

    private Intent buyerHomeIntent() {
        saveSession("buyer");

        Intent intent = new Intent(context(), BuyerHomeActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", "buyer");
        return intent;
    }

    private Intent sellerHomeIntent() {
        saveSession("seller");

        Intent intent = new Intent(context(), SellerHomeActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testseller");
        intent.putExtra("USERTYPE", "seller");
        return intent;
    }

    private Intent chatIntent(String userType) {
        saveSession(userType);

        Intent intent = new Intent(context(), ChatHomeActivity.class);
        intent.putExtra("USER_ID", 1L);
        intent.putExtra("USERNAME", "testuser");
        intent.putExtra("USERTYPE", userType);
        return intent;
    }

    private Intent paymentIntentWithCart() {
        saveSession("buyer");

        Intent intent = new Intent(context(), PaymentActivity.class);

        JSONArray cart = new JSONArray();

        try {
            JSONObject item1 = new JSONObject();
            item1.put("postingId", 1);
            item1.put("title", "Pokemon Card");
            item1.put("price", 10.50);

            JSONObject item2 = new JSONObject();
            item2.put("postingId", 2);
            item2.put("title", "Game Figure");
            item2.put("price", 20.00);

            cart.put(item1);
            cart.put(item2);
        } catch (Exception ignored) { }

        intent.putExtra("cartItems", cart.toString());
        return intent;
    }

    @Test
    public void buyerCanSearchListingsAndSeeNavigation() {
        ActivityScenario.launch(buyerHomeIntent());

        onView(withId(R.id.buyer_home_root))
                .check(matches(isDisplayed()));

        onView(withId(R.id.et_search))
                .perform(clearText(), replaceText("pokemon"), closeSoftKeyboard());

        onView(withId(R.id.et_search))
                .check(matches(withText("pokemon")));

        onView(withId(R.id.list_container))
                .check(matches(isDisplayed()));

        onView(withId(R.id.bottom_navigation))
                .check(matches(isDisplayed()));
    }

    @Test
    public void sellerDashboardShowsStatsAndCreateListingButton() {
        ActivityScenario.launch(sellerHomeIntent());

        onView(withId(R.id.seller_home_root))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tv_dashboard_title))
                .check(matches(withText("Dashboard")));

        onView(withId(R.id.btn_create_listing))
                .check(matches(isDisplayed()))
                .check(matches(withText("+ Create Listing")));

        onView(withId(R.id.stat_active_listings_value))
                .check(matches(isDisplayed()));

        onView(withId(R.id.stat_transactions_value))
                .check(matches(isDisplayed()));
    }

    @Test
    public void chatSearchAndFilterFlowWorksForBuyer() {
        ActivityScenario.launch(chatIntent("buyer"));

        onView(withId(R.id.txt_chat_title))
                .check(matches(withText("Chats")));

        onView(withId(R.id.edt_search_chats))
                .perform(clearText(), replaceText("seller"), closeSoftKeyboard());

        onView(withId(R.id.edt_search_chats))
                .check(matches(withText("seller")));

        onView(withId(R.id.chip_direct))
                .perform(click());

        onView(withId(R.id.chip_direct))
                .check(matches(isDisplayed()));

        onView(withId(R.id.chip_groups))
                .perform(click());

        onView(withId(R.id.chip_groups))
                .check(matches(isDisplayed()));
    }

    @Test
    public void paymentRejectsInvalidCardNumber() {
        ActivityScenario.launch(paymentIntentWithCart());

        onView(withId(R.id.tv_payment_total))
                .check(matches(withText("Total: $30.50")));

        onView(withId(R.id.et_card_name))
                .perform(clearText(), replaceText("John Doe"), closeSoftKeyboard());

        onView(withId(R.id.et_card_number))
                .perform(clearText(), replaceText("123"), closeSoftKeyboard());

        onView(withId(R.id.btn_pay_now))
                .perform(click());

        onView(withId(R.id.et_card_number))
                .check(matches(hasErrorText("Card number must be 16 digits")));
    }

    @Test
    public void signupPasswordMismatchStaysOnSignupPage() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.signup_username_edt))
                .perform(clearText(), replaceText("prajwaluser"), closeSoftKeyboard());

        onView(withId(R.id.signup_email_edt))
                .perform(clearText(), replaceText("prajwal@test.com"), closeSoftKeyboard());

        onView(withId(R.id.signup_password_edt))
                .perform(clearText(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.signup_confirm_edt))
                .perform(clearText(), replaceText("wrongpass"), closeSoftKeyboard());

        onView(withId(R.id.signup_signup_btn))
                .perform(click());

        onView(withId(R.id.signup_signup_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.signup_confirm_edt))
                .check(matches(isDisplayed()));
    }
}