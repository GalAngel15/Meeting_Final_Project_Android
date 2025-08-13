package com.example.meeting_project.notifications;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.managers.NotificationManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

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

//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage msg) {
//        // נתמך גם ברקע אם שלחת "data" בלבד; אם שלחת "notification", התנהגות משתנה לפי מצב.
//        if (msg.getData() == null) return;
//
//        String type = msg.getData().get("type");
//        if ("message".equals(type)) {
//            String chatId = msg.getData().get("chatId");
//            String from = msg.getData().get("fromName");
//            String preview = msg.getData().getOrDefault("preview", "New message");
//            NotificationHelper.showMessage(this, from, preview, chatId);
//        } else if ("match".equals(type)) {
//            String otherUserId = msg.getData().get("otherUserId");
//            String otherName = msg.getData().get("otherName");
//            NotificationHelper.showMatch(this, "It's a match!", "You and " + otherName + " matched 🎉", otherUserId);
//        }
//    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        final Context ctx = getApplicationContext();

        // דאגי שהערוצים קיימים (אנדרואיד 8+)
        com.example.meeting_project.notifications.NotificationHelper.ensureChannels(ctx);

        // data מה־FCM
        Map<String, String> data = msg.getData() != null ? msg.getData() : java.util.Collections.emptyMap();

        // טיפוס ההתראה: "message" / "match" / "like" / ...
        String type = data.get("type");

        // כותרת/תוכן: קודם מה־notification, אחרת מה־data
        String title = (msg.getNotification() != null && msg.getNotification().getTitle() != null)
                ? msg.getNotification().getTitle()
                : firstNonEmpty(data.get("title"), "התראה");
        String body = (msg.getNotification() != null && msg.getNotification().getBody() != null)
                ? msg.getNotification().getBody()
                : firstNonEmpty(data.get("body"), "");

        // המשתמש שמחובר במכשיר
        String currentUserId = com.example.meeting_project.UserSessionManager.getServerUserId(ctx);
        if (isEmpty(currentUserId)) {
            // אין משתמש מקומי – עדיין אפשר להראות נוטיפיקציה, אבל לא נשמור מקומית
            showSystemNotificationByType(ctx, type, title, body, data);
            return;
        }

        // אם השרת שלח שדה נמען – ודאי שההתראה מיועדת למשתמש הזה
        String toUserId = firstNonEmpty(data.get("toUserId"), data.get("userId"), data.get("recipientId"));
        if (!isEmpty(toUserId) && !toUserId.equals(currentUserId)) {
            // ההתראה לא שייכת למשתמש שמחובר במכשיר הזה
            return;
        }

        // 1) שמירה מקומית – כדי שיופיע במסך ההתראות (AlertsActivity)
        com.example.meeting_project.managers.NotificationManager
                .getInstance(ctx)
                .addFirebaseNotification(currentUserId, title, body, data);

        // 2) הצגה גם במגש המערכת לפי הטיפוס
        showSystemNotificationByType(ctx, type, title, body, data);
    }

    private void showSystemNotificationByType(Context ctx, String type, String title, String body, Map<String, String> data) {
        String currentUserId = com.example.meeting_project.UserSessionManager.getServerUserId(ctx);

        if ("match".equalsIgnoreCase(type)) {
            // אם קיים מזהה היוזם – דלג אם זה המשתמש הנוכחי
            String initiator = firstNonEmpty(data.get("initiatorUserId"), data.get("fromUserId"));
            if (!isEmpty(initiator) && initiator.equals(currentUserId)) {
                return; // אל תציג התראה ליוזם
            }

            String otherUserId = firstNonEmpty(data.get("otherUserId"), data.get("fromUserId"));
            NotificationHelper.showMatch(ctx, title, body, firstNonEmpty(otherUserId, ""));
            return;
        }

        if ("message".equalsIgnoreCase(type) || "chat".equalsIgnoreCase(type) || "chat_message".equalsIgnoreCase(type)) {
            String chatId = firstNonEmpty(data.get("chatId"), data.get("chat_id"), data.get("relatedId"));
            NotificationHelper.showMessage(ctx, title, body, firstNonEmpty(chatId, ""));
            return;
        }

        String chatId = firstNonEmpty(data.get("chatId"), data.get("chat_id"), data.get("relatedId"));
        NotificationHelper.showMessage(ctx, title, body, firstNonEmpty(chatId, ""));
    }


    /** עוזרים קטנים לקריאות קוד וניקוי ערכים ריקים/"null" */
    private static String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty() && !"null".equalsIgnoreCase(v)) return v.trim();
        }
        return null;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || "null".equalsIgnoreCase(s);
    }

}
