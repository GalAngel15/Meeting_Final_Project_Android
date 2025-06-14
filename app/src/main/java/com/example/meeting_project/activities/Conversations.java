package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatlibrary.ChatSdk;
import com.example.chatlibrary.models.Chat;
import com.example.meeting_project.R;
import com.example.meeting_project.adapters.ConversationAdapter;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.managers.AppManager;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Conversations extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private Button btnStartChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        recyclerView = findViewById(R.id.conversations_recycler_view);
        emptyState   = findViewById(R.id.empty_state);
        btnStartChat = findViewById(R.id.btn_start_chat);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // לחיצה על "פתח צ'אט חדש"
        btnStartChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChooseUserForChat.class)));

    /*  ודא שיש משתמש טעון ב-AppManager, אחרת החזר למסך הבית / התחברות */
        UserBoundary currentUser = AppManager.getAppUser();
        if (currentUser == null) {
            Toast.makeText(this, "נדרש להתחבר מחדש", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class)); // או LoginActivity
            finish();
            return;   // חשוב! למנוע המשך ביצוע
        }

        ChatSdk.getInstance()
                .getChatsForUser(currentUser.getId(), new Callback<List<Chat>>() {

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
                        Toast.makeText(Conversations.this,
                                "שגיאת רשת: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
    }


    /** מציג מסך ריק + כפתור "פתח צ'אט חדש" */
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }

    /** מציג את רשימת השיחות ב-RecyclerView */
    private void showList(List<Chat> chats) {
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(new ConversationAdapter(chats));
    }
}
