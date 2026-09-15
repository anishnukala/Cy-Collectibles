package com.example.androidexample;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CartManagerTest {

    @Test
    public void addToCartCanBeCalled() {
        Context context = ApplicationProvider.getApplicationContext();

        CartManager.addToCart(
                context,
                -999L,
                -999,
                response -> { },
                error -> { }
        );

        assertTrue(true);
    }

    @Test
    public void getCartCanBeCalled() {
        Context context = ApplicationProvider.getApplicationContext();

        CartManager.getCart(
                context,
                -999L,
                response -> { },
                error -> { }
        );

        assertTrue(true);
    }

    @Test
    public void clearCartCanBeCalled() {
        Context context = ApplicationProvider.getApplicationContext();

        CartManager.clearCart(
                context,
                -999L,
                response -> { },
                error -> { }
        );

        assertTrue(true);
    }

    @Test
    public void removeFromCartCanBeCalled() {
        Context context = ApplicationProvider.getApplicationContext();

        CartManager.removeFromCart(
                context,
                -999L,
                -999,
                response -> { },
                error -> { }
        );

        assertTrue(true);
    }
}