package com.example.meeting_project.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.chatlibrary.network.ChatApiService;
import com.example.chatlibrary.network.RetrofitClient;
import com.example.meeting_project.R;
import com.example.meeting_project.adapters.ChatMessageAdapter;
import com.example.meeting_project.managers.AppManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ChatActivity – מציג צ'אט בין שני משתמשים, טוען הודעות, שולח הודעה ומרענן כל 3 שניות.
 */
public class ChatActivity extends AppCompatActivity {

    /* ------------------ UI ------------------ */
    private TextView chatUsername;
    private ImageView chatUserImage;
    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;

    /* ------------------ DATA ---------------- */
    private final List<Message> messageList = new ArrayList<>();
    private ChatMessageAdapter messageAdapter;

    private Long chatId;          // מזהה הצ'אט בבסיס הנתונים
    private String receiverId;    // ID של הצד השני בצ'אט
    private String currentUserId; // ID של המשתמש המחובר

    private ChatApiService api;   // Retrofit API

    /* --------- Polling (כל 3 שניות) --------- */
    private final Handler handler = new Handler();
    private final Runnable messageUpdater = new Runnable() {
        @Override public void run() {
            loadMessages();
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        AppManager.setContext(getApplicationContext());

        /* ----- Bind UI ---- */
        chatUsername     = findViewById(R.id.chat_username);
        chatUserImage    = findViewById(R.id.chat_user_image);
        messageInput     = findViewById(R.id.message_input);
        sendButton       = findViewById(R.id.send_button);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);

        /* ----- Intent extras ---- */
        String otherName  = getIntent().getStringExtra("user_name");
        String otherImage = getIntent().getStringExtra("user_image");
        chatId            = getIntent().getLongExtra("chat_id", -1L);
        receiverId        = getIntent().getStringExtra("receiver_id");
        Log.d("CHAT", "ChatActivity started with chatId: " + chatId +
                ", receiverId: " + receiverId + ", otherName: " + otherName);
        currentUserId = AppManager.getAppUser().getId();

        /* ----- UI init ---- */
        if (otherName != null) chatUsername.setText(otherName);
        if (otherImage != null && !otherImage.isEmpty()) {
            Glide.with(this).load(otherImage).placeholder(R.drawable.ic_profile).into(chatUserImage);
        }

        /* ----- RecyclerView ---- */
        messageAdapter = new ChatMessageAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        /* ----- Retrofit ---- */
        api = RetrofitClient.getInstance().create(ChatApiService.class);

        /* ----- Load or create chat ---- */
        if (chatId == -1L) {
            createOrGetChat();
        } else {
            loadMessages();
        }

        sendButton.setOnClickListener(v -> sendMessage());

        /* ----- Start polling ---- */
        handler.post(messageUpdater);
    }

    /* -------------------------------------------------------------------- */
    /*  NETWORK METHODS                                                     */
    /* -------------------------------------------------------------------- */

    private void createOrGetChat() {
        Log.d("CHAT", "Creating or getting chat for user: " + currentUserId + " with receiver: " + receiverId);
        api.createChat(currentUserId, receiverId).enqueue(new Callback<Chat>() {
            @Override public void onResponse(Call<Chat> call, Response<Chat> res) {
                if (res.isSuccessful() && res.body() != null) {
                    chatId = res.body().getId();
                    loadMessages();
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to create chat", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Chat> call, Throwable t) {
                Log.e("CHAT", "createChat failure", t);
                Toast.makeText(ChatActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        if (chatId == null || chatId == -1L) return;
        api.getMessagesByChatId(chatId).enqueue(new Callback<List<Message>>() {
            @Override public void onResponse(Call<List<Message>> call, Response<List<Message>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    messageList.clear();
                    messageList.addAll(res.body());
                    messageAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }
            @Override public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e("CHAT", "loadMessages failure", t);
            }
        });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            messageInput.setError("Message cannot be empty");
            return;
        }
        if (chatId == null || chatId == -1L) {
            Toast.makeText(this, "Chat not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("CHAT", "Sending message: " + content + " in chat: " + chatId +
                " from: " + currentUserId + " to: " + receiverId);
        api.sendMessage(chatId, currentUserId, receiverId, content).enqueue(new Callback<Message>() {
            @Override public void onResponse(Call<Message> call, Response<Message> res) {
                if (res.isSuccessful() && res.body() != null) {
                    messageList.add(res.body());
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    messageInput.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Send failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Message> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* -------------------------------------------------------------------- */

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(messageUpdater);
    }
}
