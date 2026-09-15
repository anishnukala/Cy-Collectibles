package com.example.androidexample;

import android.content.Context;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CommentAdapterTest {

    private CommentAdapter.OnReplyClickListener listener() {
        return new CommentAdapter.OnReplyClickListener() {
            @Override
            public void onReplyClick(CommentItem comment) { }

            @Override
            public void onInlineReplySubmit(CommentItem comment, String replyText) { }

            @Override
            public void onInlineReplyCancel() { }
        };
    }

    @Test
    public void getItemCountReturnsSize() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<CommentItem> list = new ArrayList<>();
        list.add(new CommentItem(1, "user1", "hello", null, "now", 0));

        CommentAdapter adapter = new CommentAdapter(context, list, listener());

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void onCreateViewHolderWorks() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<CommentItem> list = new ArrayList<>();
        list.add(new CommentItem(1, "user1", "hello", null, "now", 0));

        CommentAdapter adapter = new CommentAdapter(context, list, listener());

        RecyclerView parent = new RecyclerView(context);
        parent.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));

        assertNotNull(adapter.onCreateViewHolder(parent, 0));
    }

    @Test
    public void setReplyTargetWorks() {
        Context context = ApplicationProvider.getApplicationContext();

        ArrayList<CommentItem> list = new ArrayList<>();
        list.add(new CommentItem(1, "user", "text", null, "now", 0));

        CommentAdapter adapter = new CommentAdapter(context, list, listener());

        adapter.setReplyTarget(1);
        adapter.setReplyTarget(null);

        assertEquals(1, adapter.getItemCount());
    }
}