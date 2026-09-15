package com.example.androidexample;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UserSearchItemTest {

    @Test
    public void userSearchItemGettersReturnValues() {
        UserSearchItem item = new UserSearchItem(
                5,
                "john",
                "john@test.com",
                "buyer"
        );

        assertEquals(5, item.getId());
        assertEquals("john", item.getUsername());
        assertEquals("john@test.com", item.getEmail());
        assertEquals("buyer", item.getUserType());
    }

    @Test
    public void userSearchItemAllowsNullValues() {
        UserSearchItem item = new UserSearchItem(
                -1,
                null,
                null,
                null
        );

        assertEquals(-1, item.getId());
        assertNull(item.getUsername());
        assertNull(item.getEmail());
        assertNull(item.getUserType());
    }
}