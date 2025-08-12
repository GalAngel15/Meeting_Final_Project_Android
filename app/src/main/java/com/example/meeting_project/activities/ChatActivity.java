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
 * ChatActivity
 *
 * Responsibilities:
 * - Render chat header (peer name + avatar) from Intent extras
 * - Create chat if needed, or load messages for existing chatId
 * - Poll messages every 3 seconds (starts in onStart, stops in onStop)
 * - Send new messages safely (guards, debounce, scroll to bottom)
 *
 * Notes:
 * - Bind Glide to a View (Glide.with(chatUserImage)) to avoid lifecycle crashes
 * - Guard UI callbacks with isAlive()
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

    private String chatId;          // DB chat id
    private String receiverId;      // peer user id
    private String currentUserId;   // logged-in user id
    private String otherName;       // header title
    private String otherImage;      // header avatar url

    /* ------------------ NETWORK ------------- */
    private ChatApiService api;

    /* --------- Polling (every 3 seconds) --------- */
    private final Handler handler = new Handler();
    private final Runnable messageUpdater = new Runnable() {
        @Override public void run() {
            loadMessages();                     // safe: early-returns when not ready
            handler.postDelayed(this, 3000L);   // schedule next tick
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        AppManager.setContext(getApplicationContext());

        bindViews();
        readIntentExtras();
        initApi();
        initRecycler();
        initHeader();
        initSendButton();

        currentUserId = AppManager.getAppUser().getId();

        // Create or load chat
        if (isNullOrEmpty(chatId)) {
            createOrGetChat();
        } else {
            loadMessages();
        }
    }

    /* ---------------- Lifecycle & Polling ---------------- */

    @Override
    protected void onStart() {
        super.onStart();
        startPolling(); // start periodic refresh when screen visible
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPolling();  // stop periodic refresh to avoid background work
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // keep your cleanup (explicit remove to be extra safe)
        handler.removeCallbacks(messageUpdater);
    }

    /* ---------------------- Setup / Init ---------------------- */

    private void bindViews() {
        // IDs kept exactly as in your XML
        chatUsername     = findViewById(R.id.chat_username);
        chatUserImage    = findViewById(R.id.chat_user_image);
        messageInput     = findViewById(R.id.message_input);
        sendButton       = findViewById(R.id.send_button);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
    }

    private void readIntentExtras() {
        otherName  = getIntent().getStringExtra("user_name");
        otherImage = getIntent().getStringExtra("user_image");
        chatId     = getIntent().getStringExtra("chat_id");
        receiverId = getIntent().getStringExtra("receiver_id");

        Log.d("CHAT", "ChatActivity started with chatId=" + chatId +
                ", receiverId=" + receiverId + ", otherName=" + otherName +
                ", otherImage=" + otherImage);
    }

    private void initApi() {
        api = RetrofitClient.getInstance().create(ChatApiService.class);
    }

    private void initRecycler() {
        // RecyclerView ----
        messageAdapter = new ChatMessageAdapter(messageList);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(lm);
        chatRecyclerView.setAdapter(messageAdapter);

        chatRecyclerView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
                chatRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });

    }

    private void initHeader() {
        if (!isNullOrEmpty(otherName)) {
            chatUsername.setText(otherName);
        }
        // Bind Glide to the view (safer than Glide.with(this))
        if (isNullOrEmpty(otherImage) || "null".equalsIgnoreCase(otherImage)) {
            Glide.with(chatUserImage).clear(chatUserImage);
            chatUserImage.setImageResource(R.drawable.ic_profile);
        } else {
            Glide.with(chatUserImage)
                    .load(otherImage)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(chatUserImage);
        }
    }

    private void initSendButton() {
        sendButton.setOnClickListener(v -> sendMessage());
    }

    /* ---------------------- Polling helpers ---------------------- */

    private void startPolling() {
        // Avoid multiple posts if onStart called repeatedly
        handler.removeCallbacks(messageUpdater);
        handler.post(messageUpdater);
    }

    private void stopPolling() {
        handler.removeCallbacks(messageUpdater);
    }

    /* ---------------------- Network: create/load ---------------------- */

    private void createOrGetChat() {
        if (isNullOrEmpty(receiverId)) {
            toast("Missing receiver");
            return;
        }
        Log.d("CHAT", "Creating or getting chat for user=" + currentUserId + " with receiver=" + receiverId);

        api.createChat(currentUserId, receiverId).enqueue(new Callback<Chat>() {
            @Override public void onResponse(Call<Chat> call, Response<Chat> res) {
                if (!isAlive()) return;
                if (res.isSuccessful() && res.body() != null) {
                    chatId = res.body().getId();
                    loadMessages();
                } else {
                    toast("Failed to create chat");
                }
            }
            @Override public void onFailure(Call<Chat> call, Throwable t) {
                if (!isAlive()) return;
                Log.e("CHAT", "createChat failure", t);
                toast("Network error");
            }
        });
    }

    private void loadMessages() {
        if (!isAlive()) return;
        if (isNullOrEmpty(chatId)) return;

        api.getMessagesByChatId(chatId).enqueue(new Callback<List<Message>>() {
            @Override public void onResponse(Call<List<Message>> call, Response<List<Message>> res) {
                if (!isAlive()) return;
                if (res.isSuccessful() && res.body() != null) {
                    messageList.clear();
                    messageList.addAll(res.body());
                    messageAdapter.notifyDataSetChanged();
                    scrollToBottom();
                } else {
                    Log.w("CHAT", "loadMessages: server responded " + res.code());
                }
            }
            @Override public void onFailure(Call<List<Message>> call, Throwable t) {
                if (!isAlive()) return;
                Log.e("CHAT", "loadMessages failure", t);
            }
        });
    }

    /* ---------------------- Network: send ---------------------- */

    private void sendMessage() {
        if (!isAlive()) {
            Log.w("CHAT", "Skip send: activity not alive");
            return;
        }

        String content = safeText(messageInput);
        if (content == null) {
            messageInput.setError("Message cannot be empty");
            return;
        }
        if (isNullOrEmpty(chatId)) {
            toast("Chat not ready");
            return;
        }
        if (isNullOrEmpty(receiverId)) {
            Log.e("CHAT", "Abort send: receiverId is null/empty");
            toast("Can't send: missing receiver");
            return;
        }

        // Debounce: disable input while sending to avoid duplicate posts
        setSendingUi(true);

        Log.d("CHAT", "Sending message: " + content + " in chat: " + chatId +
                " from: " + currentUserId + " to: " + receiverId);

        api.sendMessage(chatId, currentUserId, receiverId, content).enqueue(new Callback<Message>() {
            @Override public void onResponse(Call<Message> call, Response<Message> res) {
                setSendingUi(false);
                if (!isAlive()) return;

                if (res.isSuccessful() && res.body() != null) {
                    messageList.add(res.body());
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    scrollToBottom();
                    messageInput.setText("");
                } else {
                    toast("Send failed");
                }
            }
            @Override public void onFailure(Call<Message> call, Throwable t) {
                setSendingUi(false);
                if (!isAlive()) return;
                toast("Network error");
            }
        });
    }

    private void setSendingUi(boolean sending) {
        sendButton.setEnabled(!sending);
        messageInput.setEnabled(!sending);
    }

    /* ---------------------- UI helpers ---------------------- */

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            chatRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String safeText(EditText et) {
        String s = et.getText() == null ? null : et.getText().toString().trim();
        return TextUtils.isEmpty(s) ? null : s;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isAlive() {
        return !isFinishing() && !isDestroyed();
    }
}
