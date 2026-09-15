package com.example.androidexample;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ChatItemTest {

    @Test
    public void chatItemGettersReturnCorrectValues() {
        ChatItem item = new ChatItem(
                10,
                "Test Chat",
                "GROUP",
                true,
                "Last message",
                "Apr 30, 2:00 PM",
                "user1, user2",
                12345L
        );

        assertEquals(10, item.getChannelId());
        assertEquals("Test Chat", item.getName());
        assertEquals("GROUP", item.getType());
        assertTrue(item.isUnread());
        assertEquals("Last message", item.getLastMessage());
        assertEquals("Apr 30, 2:00 PM", item.getLastMessageDate());
        assertEquals("user1, user2", item.getMemberSummary());
        assertEquals(12345L, item.getSortTime());
    }

    @Test
    public void chatItemAllowsNullFields() {
        ChatItem item = new ChatItem(
                1,
                null,
                null,
                false,
                null,
                null,
                null,
                0L
        );

        assertEquals(1, item.getChannelId());
        assertNull(item.getName());
        assertNull(item.getType());
        assertFalse(item.isUnread());
        assertNull(item.getLastMessage());
        assertNull(item.getLastMessageDate());
        assertNull(item.getMemberSummary());
        assertEquals(0L, item.getSortTime());
    }

}