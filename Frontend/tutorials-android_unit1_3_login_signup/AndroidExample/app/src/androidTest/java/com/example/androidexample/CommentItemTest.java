package com.example.androidexample;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CommentItemTest {

    @Test
    public void commentItemGettersReturnValues() {
        CommentItem item = new CommentItem(
                1,
                "buyer",
                "Nice item",
                null,
                "May 2",
                0
        );

        assertEquals(1, item.getCommentId());
        assertEquals("buyer", item.getSenderUsername());
        assertEquals("Nice item", item.getContent());
        assertNull(item.getParentCommentId());
        assertEquals("May 2", item.getCreatedAt());
        assertEquals(0, item.getDepth());
    }

    @Test
    public void commentItemReplyValuesWork() {
        CommentItem item = new CommentItem(
                2,
                "seller",
                "Reply text",
                1,
                "May 3",
                1
        );

        assertEquals(2, item.getCommentId());
        assertEquals("seller", item.getSenderUsername());
        assertEquals("Reply text", item.getContent());
        assertEquals(Integer.valueOf(1), item.getParentCommentId());
        assertEquals("May 3", item.getCreatedAt());
        assertEquals(1, item.getDepth());
    }

    @Test
    public void commentItemAllowsNullValues() {
        CommentItem item = new CommentItem(
                -1,
                null,
                null,
                null,
                null,
                0
        );

        assertEquals(-1, item.getCommentId());
        assertNull(item.getSenderUsername());
        assertNull(item.getContent());
        assertNull(item.getParentCommentId());
        assertNull(item.getCreatedAt());
        assertEquals(0, item.getDepth());
    }
}