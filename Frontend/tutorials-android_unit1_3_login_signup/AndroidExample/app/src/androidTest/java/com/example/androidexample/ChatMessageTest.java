package com.example.androidexample;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ChatMessageTest {

    @Test
    public void chatMessageGettersReturnCorrectValues() {
        ChatMessage message = new ChatMessage(
                5,
                "anish",
                "hello",
                "Apr 30, 2:00 PM",
                2,
                true
        );

        assertEquals(5, message.getMsgId());
        assertEquals("anish", message.getSender());
        assertEquals("hello", message.getContent());
        assertEquals("Apr 30, 2:00 PM", message.getDateSent());
        assertEquals(Integer.valueOf(2), message.getParentMessageId());
        assertTrue(message.isSentByMe());
    }

    @Test
    public void chatMessageAllowsNullParent() {
        ChatMessage message = new ChatMessage(
                6,
                "bob",
                "reply",
                "",
                null,
                false
        );

        assertEquals(6, message.getMsgId());
        assertEquals("bob", message.getSender());
        assertEquals("reply", message.getContent());
        assertEquals("", message.getDateSent());
        assertNull(message.getParentMessageId());
        assertFalse(message.isSentByMe());
    }
}