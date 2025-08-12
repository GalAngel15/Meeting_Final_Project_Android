package com.example.meeting_project.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.meeting_project.models.Notification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationManager {

    private static final String TAG = "NotificationManager";
    private static final String PREFS_NAME = "notifications_prefs";
    private static final String NOTIFICATIONS_KEY = "notifications_list";

    private static NotificationManager instance;
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private List<NotificationChangeListener> listeners;

    public interface NotificationChangeListener {
        void onNotificationsChanged();
        void onUnreadCountChanged(int count);
    }

    private NotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.listeners = new ArrayList<>();
    }

    public static synchronized NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
        return instance;
    }

    // הוספת מאזין לשינויים
    public void addListener(NotificationChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // הסרת מאזין
    public void removeListener(NotificationChangeListener listener) {
        listeners.remove(listener);
    }

    // הוספת התראה חדשה
    public void addNotification(Notification notification) {
        List<Notification> notifications = getAllNotifications();

        // בדיקה אם ההתראה כבר קיימת (למניעת כפילויות)
        boolean exists = notifications.stream()
                .anyMatch(n -> n.getFromUserId().equals(notification.getFromUserId())
                        && n.getType().equals(notification.getType())
                        && n.getRelatedId().equals(notification.getRelatedId()));

        if (!exists) {
            notifications.add(0, notification); // הוספה בתחילת הרשימה
            saveNotifications(notifications);
            notifyListeners();
            Log.d(TAG, "Added notification: " + notification.getTitle());
        }
    }

    // קבלת כל ההתראות של משתמש מסוים
    public List<Notification> getNotificationsForUser(String userId) {
        List<Notification> allNotifications = getAllNotifications();
        return allNotifications.stream()
                .filter(n -> n.getUserId().equals(userId))
                .sorted(Comparator.comparingLong(Notification::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    // קבלת כמות התראות לא נקראו
    public int getUnreadCount(String userId) {
        return (int) getNotificationsForUser(userId).stream()
                .filter(n -> !n.isRead())
                .count();
    }

    // סימון התראה כנקראה
    public void markAsRead(String notificationId) {
        List<Notification> notifications = getAllNotifications();
        boolean changed = false;

        for (Notification notification : notifications) {
            if (notification.getId().equals(notificationId)) {
                notification.setRead(true);
                changed = true;
                break;
            }
        }

        if (changed) {
            saveNotifications(notifications);
            notifyListeners();
        }
    }

    // סימון כל ההתראות של משתמש כנקראו
    public void markAllAsReadForUser(String userId) {
        List<Notification> notifications = getAllNotifications();
        boolean changed = false;

        for (Notification notification : notifications) {
            if (notification.getUserId().equals(userId) && !notification.isRead()) {
                notification.setRead(true);
                changed = true;
            }
        }

        if (changed) {
            saveNotifications(notifications);
            notifyListeners();
        }
    }

    // מחיקת התראה
    public void deleteNotification(String notificationId) {
        List<Notification> notifications = getAllNotifications();
        notifications.removeIf(n -> n.getId().equals(notificationId));
        saveNotifications(notifications);
        notifyListeners();
    }

    // מחיקת כל ההתראות של משתמש
    public void deleteAllForUser(String userId) {
        List<Notification> notifications = getAllNotifications();
        notifications.removeIf(n -> n.getUserId().equals(userId));
        saveNotifications(notifications);
        notifyListeners();
    }

    // יצירת התראות על בסיס נתונים מהשרת
    public void createNotificationFromMessage(String userId, String fromUserId, String fromUserName,
                                              String fromUserImage, String chatId, String messageContent) {
        String title = fromUserName + " שלח/ה לך הודעה";
        String message = messageContent.length() > 50 ?
                messageContent.substring(0, 50) + "..." : messageContent;

        Notification notification = new Notification(
                userId, fromUserId, fromUserName, fromUserImage,
                Notification.NotificationType.MESSAGE, title, message, chatId
        );

        addNotification(notification);
    }

    public void createNotificationFromLike(String userId, String fromUserId, String fromUserName,
                                           String fromUserImage) {
        String title = fromUserName + " עשה/תה לך לייק!";
        String message = "לחץ כדי לראות את הפרופיל";

        Notification notification = new Notification(
                userId, fromUserId, fromUserName, fromUserImage,
                Notification.NotificationType.LIKE, title, message, fromUserId
        );

        addNotification(notification);
    }

    public void createNotificationFromMatch(String userId, String matchUserId, String matchUserName,
                                            String matchUserImage, String matchId) {
        String title = "מאטץ' חדש!";
        String message = "יש לך מאטץ' עם " + matchUserName + "!";

        Notification notification = new Notification(
                userId, matchUserId, matchUserName, matchUserImage,
                Notification.NotificationType.MATCH, title, message, matchId
        );

        addNotification(notification);
    }

    // פונקציות עזר פרטיות
    private List<Notification> getAllNotifications() {
        String json = sharedPreferences.getString(NOTIFICATIONS_KEY, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type listType = new TypeToken<List<Notification>>(){}.getType();
            List<Notification> notifications = gson.fromJson(json, listType);
            return notifications != null ? notifications : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing notifications: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveNotifications(List<Notification> notifications) {
        try {
            String json = gson.toJson(notifications);
            sharedPreferences.edit().putString(NOTIFICATIONS_KEY, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving notifications: " + e.getMessage());
        }
    }

    private void notifyListeners() {
        for (NotificationChangeListener listener : listeners) {
            try {
                listener.onNotificationsChanged();
                // אפשר להוסיף כאן לוגיקה לחישוב unread count ספציפי למשתמש
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener: " + e.getMessage());
            }
        }
    }

    // פונקציות עזר נוספות
    public boolean hasUnreadNotifications(String userId) {
        return getUnreadCount(userId) > 0;
    }

    public void clearAllNotifications() {
        sharedPreferences.edit().remove(NOTIFICATIONS_KEY).apply();
        notifyListeners();
    }
}