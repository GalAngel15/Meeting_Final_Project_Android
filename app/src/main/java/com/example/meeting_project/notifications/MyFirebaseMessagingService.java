package com.example.meeting_project.notifications;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.ensureChannels(this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        TokenUploader.sendTokenToServer(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        // נתמך גם ברקע אם שלחת "data" בלבד; אם שלחת "notification", התנהגות משתנה לפי מצב.
        if (msg.getData() == null) return;

        String type = msg.getData().get("type");
        if ("message".equals(type)) {
            String chatId = msg.getData().get("chatId");
            String from = msg.getData().get("fromName");
            String preview = msg.getData().getOrDefault("preview", "New message");
            NotificationHelper.showMessage(this, from, preview, chatId);
        } else if ("match".equals(type)) {
            String otherUserId = msg.getData().get("otherUserId");
            String otherName = msg.getData().get("otherName");
            NotificationHelper.showMatch(this, "It's a match!", "You and " + otherName + " matched 🎉", otherUserId);
        }
    }
}
