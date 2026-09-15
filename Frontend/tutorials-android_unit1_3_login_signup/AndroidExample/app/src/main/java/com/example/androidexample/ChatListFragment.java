package com.example.androidexample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_USER_TYPE = "user_type";
    private static final String ARG_FILTER = "filter";
    private static final String ARG_SEARCH = "search";

    private static final long REFRESH_INTERVAL_MS = 3000;

    private long userId = -1;
    private String username = "";
    private String userType = "";
    private String filter = "ALL";
    private String search = "";

    private RecyclerView recyclerChats;
    private TextView emptyText;

    private final List<ChatItem> visibleChats = new ArrayList<>();
    private final List<ChatItem> pendingResolvedChats = new ArrayList<>();
    private ChatListAdapter adapter;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private boolean autoRefreshStarted = false;
    private int requestGeneration = 0;
    private int pendingChannelRequests = 0;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) return;
            loadRealChats(false);
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    public ChatListFragment() { }

    public static ChatListFragment newInstance(long userId, String username, String userType, String filter, String search) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_USER_TYPE, userType);
        args.putString(ARG_FILTER, filter);
        args.putString(ARG_SEARCH, search);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID, -1);
            username = getArguments().getString(ARG_USERNAME, "");
            userType = getArguments().getString(ARG_USER_TYPE, "");
            filter = getArguments().getString(ARG_FILTER, "ALL");
            search = getArguments().getString(ARG_SEARCH, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerChats = view.findViewById(R.id.recycler_chats);
        emptyText = view.findViewById(R.id.txt_empty);

        recyclerChats.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ChatListAdapter(
                requireContext(),
                visibleChats,
                userId,
                username,
                userType
        );
        recyclerChats.setAdapter(adapter);

        loadRealChats(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh();
    }

    private void startAutoRefresh() {
        if (autoRefreshStarted) return;
        autoRefreshStarted = true;
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    private void stopAutoRefresh() {
        autoRefreshStarted = false;
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void loadRealChats(boolean showErrors) {
        if (userId == -1) {
            showEmpty("Missing user info");
            return;
        }

        final int generation = ++requestGeneration;
        pendingResolvedChats.clear();
        pendingChannelRequests = 0;

        String url = ApiConfig.BASE_URL + "/channel/user/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!isAdded()) return;
                    bindChannels(response, generation);
                },
                error -> {
                    if (!isAdded()) return;
                    Log.e("CHAT_LIST", "Failed to load channels", error);
                    if (showErrors && visibleChats.isEmpty()) {
                        showEmpty("Failed to load chats");
                        Toast.makeText(requireContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void bindChannels(JSONArray response, int generation) {
        if (generation != requestGeneration) return;

        if (response == null || response.length() == 0) {
            updateChatList(new ArrayList<>());
            return;
        }

        for (int i = 0; i < response.length(); i++) {
            JSONObject obj = response.optJSONObject(i);
            if (obj == null) continue;

            int channelId = obj.optInt("channelId", -1);
            String name = sanitize(obj.optString("name", ""));
            String type = obj.optString("type", "");
            boolean unread = obj.optBoolean("unread", false);
            String lastMessage = sanitize(obj.optString("lastMessage", ""));
            String lastMessageDateRaw = obj.optString("lastMessageDate", "");
            String formattedDate = formatDate(lastMessageDateRaw);
            long sortTime = parseDateMillis(lastMessageDateRaw);

            pendingChannelRequests++;

            if ("GROUP".equalsIgnoreCase(type)) {
                resolveGroupDetails(channelId, name, unread, lastMessage, formattedDate, sortTime, generation);
            } else {
                resolveDirectChatName(channelId, unread, lastMessage, formattedDate, sortTime, generation);
            }
        }

        if (pendingChannelRequests == 0) {
            updateChatList(new ArrayList<>());
        }
    }

    private void resolveDirectChatName(int channelId,
                                       boolean unread,
                                       String lastMessage,
                                       String formattedDate,
                                       long sortTime,
                                       int generation) {
        String url = ApiConfig.BASE_URL + "/channel/" + channelId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!isAdded() || generation != requestGeneration) return;

                    String displayName = "Direct Chat";

                    JSONArray members = response.optJSONArray("members");
                    if (members != null) {
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.optJSONObject(i);
                            if (member == null) continue;

                            int memberId = member.optInt("id", -1);
                            String memberUsername = member.optString("username", "User");

                            if (memberId != (int) userId) {
                                displayName = memberUsername;
                                break;
                            }
                        }
                    }

                    pendingResolvedChats.add(new ChatItem(
                            channelId,
                            displayName,
                            "DIRECT",
                            unread,
                            lastMessage,
                            formattedDate,
                            "",
                            sortTime
                    ));

                    onChannelResolved(generation);
                },
                error -> {
                    if (!isAdded() || generation != requestGeneration) return;

                    pendingResolvedChats.add(new ChatItem(
                            channelId,
                            "Direct Chat",
                            "DIRECT",
                            unread,
                            lastMessage,
                            formattedDate,
                            "",
                            sortTime
                    ));

                    onChannelResolved(generation);
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void resolveGroupDetails(int channelId,
                                     String name,
                                     boolean unread,
                                     String lastMessage,
                                     String formattedDate,
                                     long sortTime,
                                     int generation) {
        String url = ApiConfig.BASE_URL + "/channel/" + channelId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!isAdded() || generation != requestGeneration) return;

                    String groupName = name.isEmpty() ? "Unnamed Group" : name;
                    String memberSummary = "";

                    JSONArray members = response.optJSONArray("members");
                    if (members != null && members.length() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.optJSONObject(i);
                            if (member == null) continue;

                            String memberUsername = member.optString("username", "").trim();
                            if (memberUsername.isEmpty()) continue;

                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            sb.append(memberUsername);
                        }
                        memberSummary = sb.toString();
                    }

                    pendingResolvedChats.add(new ChatItem(
                            channelId,
                            groupName,
                            "GROUP",
                            unread,
                            lastMessage,
                            formattedDate,
                            memberSummary,
                            sortTime
                    ));

                    onChannelResolved(generation);
                },
                error -> {
                    if (!isAdded() || generation != requestGeneration) return;

                    pendingResolvedChats.add(new ChatItem(
                            channelId,
                            name.isEmpty() ? "Unnamed Group" : name,
                            "GROUP",
                            unread,
                            lastMessage,
                            formattedDate,
                            "",
                            sortTime
                    ));

                    onChannelResolved(generation);
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void onChannelResolved(int generation) {
        if (generation != requestGeneration) return;

        pendingChannelRequests--;

        if (pendingChannelRequests <= 0) {
            applyFilterAndSearch();
        }
    }

    private void applyFilterAndSearch() {
        List<ChatItem> filteredList = new ArrayList<>();
        String q = search == null ? "" : search.trim().toLowerCase();

        for (ChatItem item : pendingResolvedChats) {
            boolean filterMatch =
                    "ALL".equalsIgnoreCase(filter) ||
                            item.getType().equalsIgnoreCase(filter);

            if (!filterMatch) continue;

            boolean searchMatch;
            if (q.isEmpty()) {
                searchMatch = true;
            } else if ("GROUP".equalsIgnoreCase(item.getType())) {
                searchMatch = item.getName().toLowerCase().contains(q);
            } else {
                searchMatch =
                        item.getName().toLowerCase().contains(q) ||
                                item.getLastMessage().toLowerCase().contains(q);
            }

            if (searchMatch) {
                filteredList.add(item);
            }
        }

        Collections.sort(filteredList, (a, b) -> Long.compare(b.getSortTime(), a.getSortTime()));
        updateChatList(filteredList);
    }

    private void updateChatList(List<ChatItem> newList) {
        if (isSameList(newList, visibleChats)) {
            return;
        }

        visibleChats.clear();
        visibleChats.addAll(newList);
        adapter.notifyDataSetChanged();

        if (visibleChats.isEmpty()) {
            showEmpty("No chats found");
        } else {
            recyclerChats.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    private boolean isSameList(List<ChatItem> first, List<ChatItem> second) {
        if (first.size() != second.size()) return false;

        for (int i = 0; i < first.size(); i++) {
            ChatItem a = first.get(i);
            ChatItem b = second.get(i);

            if (a.getChannelId() != b.getChannelId()) return false;
            if (!safeEquals(a.getName(), b.getName())) return false;
            if (!safeEquals(a.getType(), b.getType())) return false;
            if (a.isUnread() != b.isUnread()) return false;
            if (!safeEquals(a.getLastMessage(), b.getLastMessage())) return false;
            if (!safeEquals(a.getLastMessageDate(), b.getLastMessageDate())) return false;
            if (!safeEquals(a.getMemberSummary(), b.getMemberSummary())) return false;
        }

        return true;
    }

    private boolean safeEquals(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        return a.equals(b);
    }

    private String sanitize(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return "";
        return trimmed;
    }

    private String formatDate(String raw) {
        if (raw == null || raw.trim().isEmpty() || "null".equalsIgnoreCase(raw)) {
            return "";
        }

        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            Date date = input.parse(raw);
            if (date == null) return raw;

            SimpleDateFormat output = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            return output.format(date);
        } catch (Exception e1) {
            try {
                SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
                Date date = input2.parse(raw);
                if (date == null) return raw;

                SimpleDateFormat output = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                return output.format(date);
            } catch (Exception e2) {
                return raw;
            }
        }
    }

    private long parseDateMillis(String raw) {
        if (raw == null || raw.trim().isEmpty() || "null".equalsIgnoreCase(raw)) {
            return 0L;
        }

        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            Date date = input.parse(raw);
            return date == null ? 0L : date.getTime();
        } catch (Exception e1) {
            try {
                SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
                Date date = input2.parse(raw);
                return date == null ? 0L : date.getTime();
            } catch (Exception e2) {
                return 0L;
            }
        }
    }

    private void showEmpty(String message) {
        recyclerChats.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }
}