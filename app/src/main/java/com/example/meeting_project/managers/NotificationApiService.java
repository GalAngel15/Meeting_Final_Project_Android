package com.example.meeting_project.managers;

import android.util.Log;
import android.content.Context;

import com.example.meeting_project.APIRequests.NotificationsApi;
import com.example.meeting_project.apiClients.Notifications_ApiClient;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.models.Notification;
import com.example.meeting_project.models.NotificationDto;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationApiService {

    private static final String TAG = "NotificationApiService";

    public interface TokenCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private static NotificationsApi api() {
        return Notifications_ApiClient.getApi();
    }

    /** רישום טוקן מפורש לשרת */
    public static void registerToken(String userId, String token, TokenCallback callback) {
        NotificationsApi.RegisterTokenRequest req = new NotificationsApi.RegisterTokenRequest(userId, token);
        api().registerToken(req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) {
                    Log.d(TAG, "Token registered successfully");
                    if (callback != null) callback.onSuccess();
                } else {
                    String err = "HTTP " + resp.code();
                    Log.e(TAG, "Failed to register token: " + err);
                    if (callback != null) callback.onFailure(err);
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error registering token", t);
                if (callback != null) callback.onFailure(t.getMessage());
            }
        });
    }

    /** ביטול רישום טוקן מפורש לשרת */
    public static void unregisterToken(String userId, String token, TokenCallback callback) {
        NotificationsApi.RegisterTokenRequest req = new NotificationsApi.RegisterTokenRequest(userId, token);
        api().unregisterToken(req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) {
                    Log.d(TAG, "Token unregistered successfully");
                    if (callback != null) callback.onSuccess();
                } else {
                    String err = "HTTP " + resp.code();
                    Log.e(TAG, "Failed to unregister token: " + err);
                    if (callback != null) callback.onFailure(err);
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error unregistering token", t);
                if (callback != null) callback.onFailure(t.getMessage());
            }
        });
    }

    /** רישום הטוקן הנוכחי (לקרוא בעת התחברות/הפעלת האפליקציה) */
    public static void registerCurrentUserToken(Context ctx, TokenCallback callback) {
        String userId = UserSessionManager.getServerUserId(ctx);
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure("User not logged in");
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> registerToken(userId, token, callback))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Fetching FCM registration token failed", e);
                    if (callback != null) callback.onFailure("Failed to get FCM token");
                });
    }

    /** ביטול רישום הטוקן הנוכחי (לקרוא בעת התנתקות) */
    public static void unregisterCurrentUserToken(Context ctx, TokenCallback callback) {
        String userId = UserSessionManager.getServerUserId(ctx);
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure("User not logged in");
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> unregisterToken(userId, token, callback))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Fetching FCM registration token failed", e);
                    if (callback != null) callback.onFailure("Failed to get FCM token");
                });
    }

    // --- חדש: שליפת התראות למשתמש (Server-first) ---
    public static void fetchUserNotifications(Context ctx, String userId, FetchCallback cb) {
        api().getUserNotifications(userId).enqueue(new Callback<List<NotificationDto>>() {
            @Override
            public void onResponse(Call<List<NotificationDto>> call, Response<List<NotificationDto>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    if (cb != null) cb.onFailure("Response not successful");
                    return;
                }
                List<Notification> mapped = new ArrayList<>();
                for (NotificationDto dto : resp.body()) {
                    mapped.add(mapDto(dto, userId));
                }

                // מחליף את כל ההתראות המקומיות של המשתמש ברשימה החדשה
                NotificationManager.getInstance(ctx).upsertFromServer(userId, mapped);

                if (cb != null) cb.onSuccess(mapped);
            }

            @Override
            public void onFailure(Call<List<NotificationDto>> call, Throwable t) {
                if (cb != null) cb.onFailure(t.getMessage());
            }
        });
    }

    // --- מיפוי DTO -> Notification (מודל האפליקציה) ---
    private static Notification mapDto(NotificationDto dto, String fallbackUserId) {
        Notification n = new Notification();
        n.setId(dto.id != null ? dto.id : ("notif_" + UUID.randomUUID()));
        n.setUserId(fallbackUserId);
        n.setFromUserId(dto.fromUserId);
        n.setFromUserName(dto.fromUserName);
        n.setFromUserImage(dto.fromUserImage);
        n.setTitle(dto.title);
        n.setMessage(dto.body);
        n.setRelatedId(dto.relatedId);
        n.setRead(dto.read);
        n.setTimestamp(dto.timestamp != 0 ? dto.timestamp : System.currentTimeMillis());
        n.setType(mapType(dto.type));
        return n;
    }

    // ממפה מחרוזת שרת ל-Enum שלך
    private static Notification.NotificationType mapType(String s) {
        if (s == null) return Notification.NotificationType.MESSAGE;
        switch (s.toLowerCase(Locale.ROOT)) {
            case "message": return Notification.NotificationType.MESSAGE;
            case "like":    return Notification.NotificationType.LIKE;
            case "match":   return Notification.NotificationType.MATCH;
            default:        return Notification.NotificationType.MESSAGE;
        }
    }

    public interface FetchCallback {
        void onSuccess(List<Notification> notifications);
        void onFailure(String error);
    }
}