package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CheckoutActivityTest {

    private ActivityScenario<CheckoutActivity> scenario;

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

        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra("cartItems", createSampleCartItems().toString());

        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        try {
            Intents.release();
        } catch (Exception ignored) {
        }

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
    public void checkoutScreenLoads() {
        onView(withId(R.id.tv_checkout_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_checkout_message)).check(matches(isDisplayed()));
        onView(withId(R.id.scroll_checkout)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_total_price)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_place_order)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_bottom)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_checkout)).check(matches(isDisplayed()));
    }

    @Test
    public void titleTextCorrect() {
        onView(withId(R.id.tv_checkout_title))
                .check(matches(withText("Checkout")));
    }

    @Test
    public void messageTextCorrectForNonEmptyCart() {
        onView(withId(R.id.tv_checkout_message))
                .check(matches(withText("Review your cart items before placing the order.")));
    }

    @Test
    public void totalPriceDisplaysCorrectAmount() {
        onView(withId(R.id.tv_total_price))
                .check(matches(withText("Total: $35.50")));
    }

    @Test
    public void checkoutItemsRenderTitleAndPrice() {
        onView(withText("Book One")).check(matches(isDisplayed()));
        onView(withText("Fiction • $20.00")).check(matches(isDisplayed()));

        onView(withText("Book Two")).check(matches(isDisplayed()));
        onView(withText("Cards • $15.50")).check(matches(isDisplayed()));
    }

    @Test
    public void placeOrderButtonVisible() {
        onView(withId(R.id.btn_place_order)).check(matches(isDisplayed()));
        onView(withText("Place Order")).check(matches(isDisplayed()));
    }

    @Test
    public void bottomBackButtonVisible() {
        onView(withId(R.id.btn_back_bottom)).check(matches(isDisplayed()));
        onView(withText("Back")).check(matches(isDisplayed()));
    }

    @Test
    public void topBackButtonClosesActivity() {
        onView(withId(R.id.btn_back_checkout)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_checkout)).perform(click());
    }

    @Test
    public void bottomBackButtonClosesActivity() {
        onView(withId(R.id.btn_back_bottom)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back_bottom)).perform(click());
    }

    @Test
    public void placeOrderWithItemsOpensPaymentActivity() {
        Intents.init();

        onView(withId(R.id.btn_place_order)).perform(click());

        intended(hasComponent(PaymentActivity.class.getName()));
    }

    @Test
    public void emptyCartShowsEmptyMessageAndZeroTotal() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra("cartItems", new JSONArray().toString());

        scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tv_checkout_title))
                .check(matches(withText("Checkout")));

        onView(withId(R.id.tv_checkout_message))
                .check(matches(withText("Your cart is empty.")));

        onView(withId(R.id.tv_total_price))
                .check(matches(withText("Total: $0.00")));
    }

    @Test
    public void invalidCartJsonShowsEmptyState() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra("cartItems", "not valid json");

        scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tv_checkout_message))
                .check(matches(withText("Your cart is empty.")));

        onView(withId(R.id.tv_total_price))
                .check(matches(withText("Total: $0.00")));
    }

    @Test
    public void noCartIntentShowsEmptyState() {
        Context context = ApplicationProvider.getApplicationContext();

        if (scenario != null) {
            scenario.close();
        }

        Intent intent = new Intent(context, CheckoutActivity.class);

        scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tv_checkout_message))
                .check(matches(withText("Your cart is empty.")));

        onView(withId(R.id.tv_total_price))
                .check(matches(withText("Total: $0.00")));
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