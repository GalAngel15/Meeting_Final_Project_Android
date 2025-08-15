package com.example.meeting_project.notifications;

import android.content.Context;
import android.util.Log;

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

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        final Context ctx = getApplicationContext();

        // ודא שהערוצים קיימים
        NotificationHelper.ensureChannels(ctx);

        // data מה־FCM
        Map<String, String> data = msg.getData() != null ? msg.getData() : java.util.Collections.emptyMap();

        // טיפוס ההתראה
        String type = data.get("type");

        // כותרת/תוכן
        String title = (msg.getNotification() != null && msg.getNotification().getTitle() != null)
                ? msg.getNotification().getTitle()
                : firstNonEmpty(data.get("title"), "התראה");
        String body = (msg.getNotification() != null && msg.getNotification().getBody() != null)
                ? msg.getNotification().getBody()
                : firstNonEmpty(data.get("body"), "");

        // המשתמש שמחובר במכשיר
        String currentUserId = UserSessionManager.getServerUserId(ctx);
        if (isEmpty(currentUserId)) {
            Log.d("FCM", "No current user - showing system notification only");
            showSystemNotificationByType(ctx, type, title, body, data);
            return;
        }

        // ** בדיקות חזקות למניעת התראות עצמיות **
        String fromUserId = firstNonEmpty(data.get("fromUserId"), data.get("senderId"));
        String toUserId = firstNonEmpty(data.get("toUserId"), data.get("userId"), data.get("recipientId"));

        // אם יש שולח מפורש - ודא שזה לא המשתמש הנוכחי
        if (!isEmpty(fromUserId) && fromUserId.equals(currentUserId)) {
            Log.d("FCM", "Notification from current user to themselves - ignoring");
            return;
        }

        // אם יש נמען מפורש - ודא שזה המשתמש הנוכחי
        if (!isEmpty(toUserId) && !toUserId.equals(currentUserId)) {
            Log.d("FCM", "Notification not for current user - ignoring");
            return;
        }

        Log.d("FCM", String.format("Processing notification: type=%s, from=%s, to=%s, current=%s",
                type, fromUserId, toUserId, currentUserId));

        // 1) שמירה מקומית
        NotificationManager.getInstance(ctx)
                .addFirebaseNotification(currentUserId, title, body, data);

        // 2) הצגה במגש המערכת
        showSystemNotificationByType(ctx, type, title, body, data);
    }


    private void showSystemNotificationByType(Context ctx, String type, String title, String body, Map<String, String> data) {
        String currentUserId = UserSessionManager.getServerUserId(ctx);

        // בדיקה נוספת - אל תציג אם המשתמש הנוכחי הוא השולח
        String fromUserId = firstNonEmpty(data.get("fromUserId"), data.get("senderId"));
        if (!isEmpty(fromUserId) && !isEmpty(currentUserId) && fromUserId.equals(currentUserId)) {
            Log.d("FCM", "Skipping system notification - current user is sender");
            return;
        }

        if ("match".equalsIgnoreCase(type)) {
            String initiator = firstNonEmpty(data.get("initiatorUserId"), data.get("fromUserId"));
            if (!isEmpty(initiator) && !isEmpty(currentUserId) && initiator.equals(currentUserId)) {
                Log.d("FCM", "Skipping match notification - current user is initiator");
                return;
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

        // ברירת מחדל
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
