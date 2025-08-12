package com.example.meeting_project.models;

import java.io.Serializable;

public class Notification implements Serializable {

    public enum NotificationType {
        MESSAGE("הודעה חדשה"),
        LIKE("מישהו עשה לך לייק"),
        MATCH("יש לך מאטץ' חדש!");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
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