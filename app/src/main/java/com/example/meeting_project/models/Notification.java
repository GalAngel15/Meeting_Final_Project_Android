package com.example.meeting_project.models;

import java.io.Serializable;
import java.util.Map;

public class Notification implements Serializable {

    public enum NotificationType {
        MESSAGE("הודעה חדשה", "message"),
        LIKE("מישהו עשה לך לייק", "like"),
        MATCH("יש לך מאטץ' חדש!", "match");

        private final String displayName;
        private final String firebaseType;

        NotificationType(String displayName, String firebaseType) {
            this.displayName = displayName;
            this.firebaseType = firebaseType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFirebaseType() {
            return firebaseType;
        }

        // המרה מ-Firebase type לסוג התראה
        public static NotificationType fromFirebaseType(String firebaseType) {
            for (NotificationType type : values()) {
                if (type.firebaseType.equals(firebaseType)) {
                    return type;
                }
            }
            return MESSAGE; // ברירת מחדל
        }
    }

    private String id;
    private String userId;          // מקבל ההתראה
    private String fromUserId;      // שולח ההתראה
    private String fromUserName;    // שם השולח
    private String fromUserImage;   // תמונה של השולח
    private NotificationType type;
    private String title;
    private String message;
    private boolean isRead;
    private long timestamp;
    private String relatedId;       // chat ID או match ID
    private Map<String, String> firebaseData; // הנתונים מ-Firebase

    // בנאי ריק
    public Notification() {
        this.id = generateId();
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // בנאי מלא
    public Notification(String userId, String fromUserId, String fromUserName,
                        String fromUserImage, NotificationType type, String title,
                        String message, String relatedId) {
        this();
        this.userId = userId;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.fromUserImage = fromUserImage;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedId = relatedId;
    }

    // בנאי מ-Firebase data
    public static Notification fromFirebaseData(String userId, String title, String body, Map<String, String> data) {
        Notification notification = new Notification();
        notification.userId = userId;
        notification.title = title;
        notification.message = body;
        notification.firebaseData = data;

        String typeStr = data.get("type");
        notification.type = NotificationType.fromFirebaseType(typeStr);

        switch (notification.type) {
            case MESSAGE:
                notification.relatedId = data.get("chatId");
                notification.fromUserId = data.get("fromUserId"); // ← להוסיף
                notification.fromUserName = data.get("fromName");
                break;

            case MATCH:
                notification.fromUserId = data.get("otherUserId");
                notification.fromUserName = data.get("otherName");
                notification.relatedId = "match_" + userId + "_" + notification.fromUserId;
                break;

            case LIKE:
                notification.fromUserId = data.get("fromUserId"); // אם יש
                notification.fromUserName = data.get("fromName"); // אם יש
                break;
        }

        return notification;
    }

    // יצירת ID ייחודי
    private String generateId() {
        return "notif_" + System.currentTimeMillis() + "_" + Math.random();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public String getFromUserImage() { return fromUserImage; }
    public void setFromUserImage(String fromUserImage) { this.fromUserImage = fromUserImage; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public Map<String, String> getFirebaseData() { return firebaseData; }
    public void setFirebaseData(Map<String, String> firebaseData) { this.firebaseData = firebaseData; }

    // פונקציה ליצירת זמן יחסי (לפני 5 דקות, אתמול וכו')
    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "עכשיו";
        } else if (minutes < 60) {
            return "לפני " + minutes + " דקות";
        } else if (hours < 24) {
            return "לפני " + hours + " שעות";
        } else if (days < 7) {
            return "לפני " + days + " ימים";
        } else {
            return "לפני יותר משבוע";
        }
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", fromUserId='" + fromUserId + '\'' +
                ", fromUserName='" + fromUserName + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", timestamp=" + timestamp +
                ", relatedId='" + relatedId + '\'' +
                '}';
    }
}