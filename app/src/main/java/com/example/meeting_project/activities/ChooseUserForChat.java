package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatlibrary.ChatSdk;
import com.example.chatlibrary.models.Chat;
import com.example.meeting_project.R;
import com.example.meeting_project.adapters.ChooseUserAdapter;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.APIRequests.UserApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseUserForChat extends AppCompatActivity
        implements ChooseUserAdapter.OnUserClickListener {

    private RecyclerView rv;
    private String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user_for_chat);

        rv = findViewById(R.id.users_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));

        /* בדיקת משתמש נוכחי */
        if (AppManager.getAppUser() == null) {
            Toast.makeText(this, "נדרש להתחבר מחדש", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        myId = AppManager.getAppUser().getId();

        loadUsers();
    }

    /** שליפת כל המשתמשים (UserBoundary) */
    private void loadUsers() {
        UserApi api = User_ApiClient.getRetrofitInstance().create(UserApi.class);

        api.getAllUsers().enqueue(new Callback<List<UserBoundary>>() {
            @Override
            public void onResponse(Call<List<UserBoundary>> c, Response<List<UserBoundary>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<UserBoundary> fetched = new ArrayList<>(r.body()); // יצירת עותק
                    fetched.removeIf(u -> u.getId().equals(myId));          // הסרת עצמי
                    rv.setAdapter(new ChooseUserAdapter(fetched, ChooseUserForChat.this));
                } else {
                    Toast.makeText(ChooseUserForChat.this,
                            "שגיאה בטעינת משתמשים (" + r.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserBoundary>> c, Throwable t) {
                Log.e("ChooseUser", "getAllUsers failure", t);
                Toast.makeText(ChooseUserForChat.this,
                        "שגיאת רשת: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** לחיצה על משתמש – יוצרת/מאחזרת צ’אט ומעבירה ל-ChatActivity */
    @Override
    public void onUserClick(UserBoundary other) {
        ChatSdk.getInstance().createChat(myId, other.getId(), new Callback<Chat>() {
            @Override
            public void onResponse(Call<Chat> c, Response<Chat> r) {
                if (r.isSuccessful() && r.body() != null) {
                    openChatScreen(r.body(), other);
                } else {
                    Toast.makeText(ChooseUserForChat.this,
                            "שגיאה ביצירת/פתיחת שיחה (" + r.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Chat> c, Throwable t) {
                Log.e("ChooseUser", "createChat failure", t);
                Toast.makeText(ChooseUserForChat.this,
                        "שגיאת רשת: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChatScreen(Chat chat, UserBoundary other) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chat_id", chat.getId());
        intent.putExtra("user_name", other.getFirstName() + " " + other.getLastName());
        intent.putExtra("user_image", other.getProfilePhotoUrl());
        startActivity(intent);
        finish();
    }
}
