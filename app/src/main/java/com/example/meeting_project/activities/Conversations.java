package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatlibrary.ChatSdk;
import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.meeting_project.R;
import com.example.meeting_project.adapters.ConversationAdapter;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.BaseNavigationActivity;

// שים לב ל-packages. אם שונים אצלך, עשה Alt+Enter Import:
import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.apiClients.User_ApiClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Conversations extends BaseNavigationActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private Button btnStartChat;
    private View loading;
    private ConversationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViews();
        setupRecycler();
        setupClicks();
        loadChats();
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.conversations_recycler_view);
        emptyState   = findViewById(R.id.empty_state);
        btnStartChat = findViewById(R.id.btn_start_chat);
        loading      = findViewById(R.id.loading);
    }

    private void setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (adapter == null) {
            adapter = new ConversationAdapter(new ConversationDiff());
        }
        recyclerView.setAdapter(adapter);
    }

    private void setupClicks() {
        btnStartChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChooseUserForChat.class)));
    }

    private void loadChats() {
        showLoading(true);
        UserBoundary user = AppManager.getAppUser();

        if (user == null) {
            Toast.makeText(this, "נדרש להתחבר מחדש", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class)); // או LoginActivity
            finish();
            return;
        }

        ChatSdk.getInstance().getChatsForUser(user.getId(), new Callback<List<Chat>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chat>> call, @NonNull Response<List<Chat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Chat> chats = response.body();
                    if (chats.isEmpty()) {
                        showEmptyState();
                    } else {
                        showList(chats);
                        prefetchAvatarsThenApply(chats); // ← טעינה מרוכזת של אווטארים
                    }
                } else {
                    Toast.makeText(Conversations.this, "שגיאה בטעינת שיחות", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chat>> call, @NonNull Throwable t) {
                showEmptyState();
            }
        });
    }

    private void showLoading(boolean show) {
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (!show) emptyState.setVisibility(View.GONE);
    }

    /** מציג מסך ריק + כפתור "פתח צ'אט חדש" */
    private void showEmptyState() {
        showLoading(false);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        adapter.submitList(null);
    }

    /** מציג את רשימת השיחות ב-RecyclerView */
    private void showList(List<Chat> chats) {
        showLoading(false);
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.submitList(chats);
    }

    /** Batch avatars: מביא פעם אחת את כל תמונות הצד השני ומזרים לאדפטר כמפה */
    private void prefetchAvatarsThenApply(List<Chat> chats) {
        // אוסף את כל מזהי ה-user של הצד השני בכל צ'אט
        Set<String> ids = new HashSet<>();
        String me = AppManager.getAppUser().getId();
        for (Chat c : chats) {
            String other = me.equals(c.getUser1Id()) ? c.getUser2Id() : c.getUser1Id();
            if (other != null) ids.add(other);
        }
        if (ids.isEmpty()) {
            adapter.setAvatarMap(new HashMap<String, String>());
            return;
        }

        UserApi api = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        api.getUsersByIds(new ArrayList<>(ids)).enqueue(new Callback<List<UserBoundary>>() {
            @Override public void onResponse(@NonNull Call<List<UserBoundary>> call,
                                             @NonNull Response<List<UserBoundary>> res) {
                if (!res.isSuccessful() || res.body() == null) return;

                Map<String, String> map = new HashMap<>();
                for (UserBoundary u : res.body()) {
                    String url = firstUrlOrNull(u.getGalleryUrls());
                    if (u.getId() != null) {
                        map.put(u.getId(), url);
                    }
                }
                adapter.setAvatarMap(map); // ← האדפטר יעדכן תמונות מיידית
            }
            @Override public void onFailure(@NonNull Call<List<UserBoundary>> call, @NonNull Throwable t) {
                // לא קריטי — נשאר עם placeholder
            }
        });
    }

    private static String firstUrlOrNull(List<String> urls) {
        if (urls == null || urls.isEmpty()) return null;
        String s = urls.get(0);
        if (s == null) return null;
        s = s.trim();
        return (s.isEmpty() || "null".equalsIgnoreCase(s)) ? null : s;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_conversations;
    }

    @Override
    protected int getDrawerMenuItemId() {
        return 0;
    }

    @Override
    protected int getBottomMenuItemId() {
        return R.id.navigation_chats;
    }

    @Override
    protected String getCurrentUserId() {
        return AppManager.getAppUser().getId();
    }

    static class ConversationDiff extends DiffUtil.ItemCallback<Chat> {
        @Override public boolean areItemsTheSame(@NonNull Chat o, @NonNull Chat n) {
            return o.getId().equals(n.getId());
        }
        @Override public boolean areContentsTheSame(@NonNull Chat o, @NonNull Chat n) {
            // נשווה לפי טקסט ההודעה האחרונה + הזמן שלה
            String oText = lastMessageText(o);
            String nText = lastMessageText(n);
            Long   oTs   = lastMessageTs(o);
            Long   nTs   = lastMessageTs(n);
            return Objects.equals(oText, nText) && Objects.equals(oTs, nTs);
        }

        private static String lastMessageText(Chat c) {
            List<Message> msgs = c.getAllMessages();
            if (msgs == null || msgs.isEmpty()) return null;
            return msgs.get(msgs.size()-1).getContent();
        }
        private static Long lastMessageTs(Chat c) {
            List<Message> msgs = c.getAllMessages();
            if (msgs == null || msgs.isEmpty()) return null;
            Object ts = msgs.get(msgs.size()-1).getTimestamp();
            if (ts instanceof Number) return ((Number) ts).longValue();
            try { return Instant.parse(String.valueOf(ts)).toEpochMilli(); }
            catch (Exception e) { return null; }
        }
    }
}
