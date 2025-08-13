package com.example.meeting_project.notifications;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;

import com.example.meeting_project.R;
import com.example.meeting_project.activities.ChatActivity;
import com.example.meeting_project.activities.HomeActivity;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationHelper {

    private static final String CHANNEL_MESSAGES = "messages";
    private static final String CHANNEL_MATCHES = "matches";
    private static final AtomicInteger ID_GEN = new AtomicInteger(1000);

    public static void ensureChannels(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);

            NotificationChannel msg = new NotificationChannel(
                    CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH);
            msg.enableLights(true); msg.setLightColor(Color.BLUE);
            nm.createNotificationChannel(msg);

            NotificationChannel match = new NotificationChannel(
                    CHANNEL_MATCHES, "Matches", NotificationManager.IMPORTANCE_DEFAULT);
            match.enableLights(true); match.setLightColor(Color.MAGENTA);
            nm.createNotificationChannel(match);
        }
    }

    public static void showMessage(Context ctx, String title, String body, String chatId) {
        Intent open = new Intent(ctx, ChatActivity.class);
        open.putExtra("chatId", chatId);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                ctx, chatId.hashCode(), open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_MESSAGES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        if (!canPostNotifications(ctx)) return;
        try {
            NotificationManagerCompat.from(ctx).notify(ID_GEN.getAndIncrement(), b.build());
        } catch (SecurityException ignored) { /* המשתמש חסם התראות */ }
    }

    public static void showMatch(Context ctx, String title, String body, String otherUserId) {
        Intent open = new Intent(ctx, HomeActivity.class);
        open.putExtra("openMatchedUserId", otherUserId);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                ctx, otherUserId.hashCode(), open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_MATCHES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi);

        if (!canPostNotifications(ctx)) return;
        try {
            NotificationManagerCompat.from(ctx).notify(ID_GEN.getAndIncrement(), b.build());
        } catch (SecurityException ignored) { /* המשתמש חסם התראות */ }
    }

    private static boolean canPostNotifications(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return NotificationManagerCompat.from(ctx).areNotificationsEnabled();
    }
}

