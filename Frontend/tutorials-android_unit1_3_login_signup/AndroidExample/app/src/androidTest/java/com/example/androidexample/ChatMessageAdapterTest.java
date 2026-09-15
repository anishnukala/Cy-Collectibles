package com.example.androidexample;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ChatMessageAdapterTest {

    @Test
    public void getItemCountReturnsSize() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "me", "hello", "now", null, true));
        messages.add(new ChatMessage(2, "bob", "hi", "later", null, false));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void getItemCountReturnsZeroForNullList() {
        ChatMessageAdapter adapter = new ChatMessageAdapter(null, null);

        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void sentMessageReturnsSentViewType() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "me", "hello", "now", null, true));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        assertEquals(1, adapter.getItemViewType(0));
    }

    @Test
    public void receivedMessageReturnsReceivedViewType() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "bob", "hello", "now", null, false));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        assertEquals(2, adapter.getItemViewType(0));
    }

    @Test
    public void createSentViewHolderWorks() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "me", "hello", "now", null, true));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        FrameLayout parent = new FrameLayout(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );

        assertNotNull(adapter.onCreateViewHolder(parent, 1));
    }

    @Test
    public void createReceivedViewHolderWorks() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "bob", "hello", "now", null, false));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        FrameLayout parent = new FrameLayout(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );

        assertNotNull(adapter.onCreateViewHolder(parent, 2));
    }

    @Test
    public void bindSentMessageWorks() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "me", "hello", "now", null, true));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        FrameLayout parent = new FrameLayout(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );

        androidx.recyclerview.widget.RecyclerView.ViewHolder holder =
                adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

        adapter.onBindViewHolder(holder, 0);

        assertNotNull(holder.itemView);
    }

    @Test
    public void bindReceivedMessageWorks() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "bob", "hello", "now", null, false));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        FrameLayout parent = new FrameLayout(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );

        androidx.recyclerview.widget.RecyclerView.ViewHolder holder =
                adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

        adapter.onBindViewHolder(holder, 0);

        assertNotNull(holder.itemView);
    }

    @Test
    public void bindReplyPreviewWorks() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "bob", "original", "now", null, false));
        messages.add(new ChatMessage(2, "me", "reply", "later", 1, true));

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, null);

        FrameLayout parent = new FrameLayout(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );

        androidx.recyclerview.widget.RecyclerView.ViewHolder holder =
                adapter.onCreateViewHolder(parent, adapter.getItemViewType(1));

        adapter.onBindViewHolder(holder, 1);

        assertNotNull(holder.itemView);
    }

    @Test
    public void longClickCallsListener() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(1, "me", "hello", "now", null, true));

        final boolean[] clicked = {false};

        ChatMessageAdapter adapter = new ChatMessageAdapter(messages, message -> clicked[0] = true);

        FrameLayout parent = new FrameLayout(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );

        androidx.recyclerview.widget.RecyclerView.ViewHolder holder =
                adapter.onCreateViewHolder(parent, adapter.getItemViewType(0));

        adapter.onBindViewHolder(holder, 0);
        holder.itemView.performLongClick();

        assertTrue(clicked[0]);
    }
}