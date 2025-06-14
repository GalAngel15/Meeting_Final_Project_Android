package com.example.meeting_project.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.meeting_project.R;
import com.example.meeting_project.managers.AppManager;

public class ChatActivity extends AppCompatActivity {

    private TextView chatUsername;
    private ImageView chatUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        AppManager.setContext(this.getApplicationContext());

        chatUsername = findViewById(R.id.chat_username);
        chatUserImage = findViewById(R.id.chat_user_image);

        // קבלת נתוני המשתמש מה-Intent
        String name = getIntent().getStringExtra("user_name");
        String imageUrl = getIntent().getStringExtra("user_image");

        if (name != null) {
            chatUsername.setText(name);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(chatUserImage);
        }
    }
}
