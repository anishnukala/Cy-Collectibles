package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

import android.content.Context;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class)
public class CartActivityTest {

    private ActivityScenario<CartActivity> scenario;

    @Before
    public void setUp() {
        Intents.init();

        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testbuyer")
                .putString("USER_TYPE", "BUYER")
                .putString("USERTYPE", "BUYER")
                .apply();

        scenario = ActivityScenario.launch(CartActivity.class);
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
    public void cartScreenLoads() {
        onView(withId(R.id.tv_cart_title)).check(matches(isDisplayed()));
        onView(withText("My Cart")).check(matches(isDisplayed()));
        onView(withId(R.id.tv_cart_subtitle)).check(matches(isDisplayed()));
        onView(withText("Items selected and ready for checkout")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_buy_now)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_clear_cart)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    @Test
    public void cartScrollViewVisible() {
        onView(withId(R.id.scroll_cart)).check(matches(isDisplayed()));
    }

    @Test
    public void buyNowButtonVisible() {
        onView(withId(R.id.btn_buy_now)).check(matches(isDisplayed()));
        onView(withText("Buy Now")).check(matches(isDisplayed()));
    }

    @Test
    public void clearCartButtonVisible() {
        onView(withId(R.id.btn_clear_cart)).check(matches(isDisplayed()));
        onView(withText("Clear")).check(matches(isDisplayed()));
    }

    @Test
    public void backButtonVisibleAndClickable() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back)).perform(click());
    }

    @Test
    public void emptyCartStateShowsEmptyMessageAndDisablesButtons() {
        scenario.onActivity(activity -> {
            try {
                setCartItems(activity, new JSONArray());
                callRenderCart(activity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.tv_empty)).check(matches(isDisplayed()));
        onView(withText("Your cart is empty.")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_buy_now)).check(matches(not(isEnabled())));
        onView(withId(R.id.btn_clear_cart)).check(matches(not(isEnabled())));
    }

    @Test
    public void cartItemRendersTitleGenrePriceAndButtons() {
        scenario.onActivity(activity -> {
            try {
                JSONArray items = new JSONArray();

                JSONObject item = new JSONObject();
                item.put("postingId", 10);
                item.put("title", "Test Book");
                item.put("genre", "Fiction");
                item.put("price", 12.0);
                item.put("imageUrl", "images/test.png");
                item.put("description", "A test book description");

                items.put(item);

                setCartItems(activity, items);
                callRenderCart(activity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withText("Test Book")).check(matches(isDisplayed()));
        onView(withText("Fiction • $12.0")).check(matches(isDisplayed()));
        onView(withText("Item Detail")).check(matches(isDisplayed()));
        onView(withText("Remove")).check(matches(isDisplayed()));
    }

    @Test
    public void nonEmptyCartEnablesButtons() {
        scenario.onActivity(activity -> {
            try {
                JSONArray items = new JSONArray();

                JSONObject item = new JSONObject();
                item.put("postingId", 22);
                item.put("title", "Cart Item");
                item.put("genre", "Cards");
                item.put("price", 5.0);
                item.put("imageUrl", "images/item.png");
                item.put("description", "Description");

                items.put(item);

                setCartItems(activity, items);
                callRenderCart(activity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.btn_buy_now)).check(matches(isEnabled()));
        onView(withId(R.id.btn_clear_cart)).check(matches(isEnabled()));
    }

    @Test
    public void nonEmptyCartShowsButtons() {
        scenario.onActivity(activity -> {
            try {
                JSONArray items = new JSONArray();

                JSONObject item = new JSONObject();
                item.put("postingId", 99);
                item.put("title", "Detail Test Item");
                item.put("genre", "Action");
                item.put("price", 15.0);
                item.put("imageUrl", "images/detail.png");
                item.put("description", "Detail description");

                items.put(item);

                setCartItems(activity, items);
                callRenderCart(activity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.btn_buy_now)).check(matches(isEnabled()));
        onView(withId(R.id.btn_clear_cart)).check(matches(isEnabled()));
    }

    @Test
    public void buyNowWithItemsOpensCheckoutActivity() {
        scenario.onActivity(activity -> {
            try {
                JSONArray items = new JSONArray();

                JSONObject item = new JSONObject();
                item.put("postingId", 101);
                item.put("title", "Checkout Item");
                item.put("genre", "Drama");
                item.put("price", 20.0);
                item.put("imageUrl", "images/checkout.png");
                item.put("description", "Checkout description");

                items.put(item);

                setCartItems(activity, items);
                callRenderCart(activity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.btn_buy_now)).perform(click());

        intended(hasComponent(CheckoutActivity.class.getName()));
    }

    private static void setCartItems(CartActivity activity, JSONArray items) throws Exception {
        Field field = CartActivity.class.getDeclaredField("cartItems");
        field.setAccessible(true);
        field.set(activity, items);
    }

    private static void callRenderCart(CartActivity activity) throws Exception {
        Method method = CartActivity.class.getDeclaredMethod("renderCart");
        method.setAccessible(true);
        method.invoke(activity);
    }
}