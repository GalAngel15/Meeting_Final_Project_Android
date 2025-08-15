package com.example.meeting_project.notifications;

import android.content.Context;
import android.util.Log;


import com.example.meeting_project.APIRequests.NotificationsApi;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.Notifications_ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenUploader {

    public static void sendTokenToServer(Context ctx, String token) {
        String userId = UserSessionManager.getServerUserId(ctx);
        if (userId == null || userId.isEmpty()) {
            Log.w("FCM", "No server userId; skipping token register");
            return;
        }

        NotificationsApi api = Notifications_ApiClient.getApi();
        NotificationsApi.RegisterTokenRequest body =
                new NotificationsApi.RegisterTokenRequest(userId, token);

        api.registerToken(body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) {
                    Log.d("FCM", "Token registered OK");
                } else {
                    Log.w("FCM", "Token register failed HTTP " + resp.code());
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Token register error: " + t.getMessage());
            }
        });
    }

    public static void removeTokenToServer(Context ctx, String token) {
        String userId = UserSessionManager.getServerUserId(ctx);
        if (userId == null || userId.isEmpty()) {
            Log.w("FCM", "No server userId; skipping token register");
            return;
        }

        NotificationsApi api = Notifications_ApiClient.getApi();
        NotificationsApi.RegisterTokenRequest body =
                new NotificationsApi.RegisterTokenRequest(userId, token);

        api.unregisterToken(body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) {
                    Log.d("FCM", "Token removed successful");
                } else {
                    Log.w("FCM", "Token register failed HTTP " + resp.code());
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Token register error: " + t.getMessage());
            }
        });
    }
}
