package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.time.Instant;
import java.util.List;
import java.util.Objects;

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
        btnStartChat.setOnClickListener(v -> startActivity(
                new Intent(this, ChooseUserForChat.class)));
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

        ChatSdk.getInstance()
            .getChatsForUser(user.getId(), new Callback<List<Chat>>() {

                @Override
                public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Chat> chats = response.body();
                        if (chats.isEmpty()) {
                            showEmptyState();
                        } else {
                            showList(chats);
                        }
                    } else {
                        Toast.makeText(Conversations.this,
                                "שגיאה בטעינת שיחות",
                                Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                }

                @Override
                public void onFailure(Call<List<Chat>> call, Throwable t) {
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

        private String lastMessageText(Chat c) {
            List<Message> msgs = c.getAllMessages();
            if (msgs == null || msgs.isEmpty()) return null;
            return msgs.get(msgs.size()-1).getContent();
        }
        private Long lastMessageTs(Chat c) {
            List<Message> msgs = c.getAllMessages();
            if (msgs == null || msgs.isEmpty()) return null;
            Object ts = msgs.get(msgs.size()-1).getTimestamp();
            if (ts instanceof Number) return ((Number) ts).longValue();
            try { return Instant.parse(String.valueOf(ts)).toEpochMilli(); }
            catch (Exception e) { return null; }
        }
    }
}
