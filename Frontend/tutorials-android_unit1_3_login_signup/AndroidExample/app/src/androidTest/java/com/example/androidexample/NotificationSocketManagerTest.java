package com.example.androidexample;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NotificationSocketManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .clear()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "testuser")
                .putString("USERTYPE", "buyer")
                .putString("USER_TYPE", "buyer")
                .apply();

        NotificationSocketManager.getInstance().disconnect();
    }

    @After
    public void tearDown() {
        NotificationSocketManager.getInstance().disconnect();

        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    @Test
    public void getInstanceReturnsNonNullManager() {
        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        assertNotNull(manager);
    }

    @Test
    public void getInstanceReturnsSameSingleton() {
        NotificationSocketManager manager1 = NotificationSocketManager.getInstance();
        NotificationSocketManager manager2 = NotificationSocketManager.getInstance();

        assertSame(manager1, manager2);
    }

    @Test
    public void disconnectWhenNotConnectedDoesNotCrash() {
        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.disconnect();

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWithZeroUserIdDoesNotConnect() {
        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.connect(context, 0L);

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWithNegativeUserIdDoesNotConnect() {
        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.connect(context, -1L);

        assertFalse(manager.isConnected());
    }

    @Test
    public void multipleDisconnectCallsDoNotCrash() {
        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.disconnect();
        manager.disconnect();
        manager.disconnect();

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectThenDisconnectDoesNotCrash() {
        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.connect(context, 1L);
        manager.disconnect();

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWithDifferentUsersDoesNotCrash() {
        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.connect(context, 1L);
        manager.connect(context, 2L);
        manager.disconnect();

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWithBuyerSessionDoesNotCrash() {
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("USERTYPE", "buyer")
                .apply();

        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.connect(context, 1L);
        manager.disconnect();

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWithSellerSessionDoesNotCrash() {
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("USERTYPE", "seller")
                .apply();

        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.connect(context, 1L);
        manager.disconnect();

        assertFalse(manager.isConnected());
    }

    @Test
    public void connectWithAdminSessionDoesNotCrash() {
        SharedPreferences prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("USERTYPE", "admin")
                .apply();

        NotificationSocketManager manager = NotificationSocketManager.getInstance();

        manager.connect(context, 1L);
        manager.disconnect();

        assertFalse(manager.isConnected());
    }

    @Test
    public void getNotificationIntent_messageOpensChatHome() {
        Intent intent = NotificationSocketManager.getInstance()
                .getNotificationIntent(context, "MESSAGE");

        assertEquals(ChatHomeActivity.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void getNotificationIntent_adminOpensAdminHome() {
        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putString("USERTYPE", "admin")
                .apply();

        Intent intent = NotificationSocketManager.getInstance()
                .getNotificationIntent(context, "NOTICE");

        assertEquals(AdminHomeActivity.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void getNotificationIntent_sellerOpensSellerHome() {
        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putString("USERTYPE", "seller")
                .apply();

        Intent intent = NotificationSocketManager.getInstance()
                .getNotificationIntent(context, "NOTICE");

        assertEquals(SellerHomeActivity.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void getNotificationIntent_buyerOpensBuyerHome() {
        context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .putString("USERTYPE", "buyer")
                .apply();

        Intent intent = NotificationSocketManager.getInstance()
                .getNotificationIntent(context, "NOTICE");

        assertEquals(BuyerHomeActivity.class.getName(), intent.getComponent().getClassName());
    }
}