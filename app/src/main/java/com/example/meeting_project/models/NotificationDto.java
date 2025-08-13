package com.example.meeting_project.models;

public class NotificationDto {
    public String id;
    public String type;        // "MESSAGE" / "LIKE" / "MATCH" ...
    public String title;
    public String body;
    public String fromUserId;
    public String fromUserName;
    public String fromUserImage;
    public String relatedId;   // למשל chatId / matchId
    public long   timestamp;   // millis
    public boolean read;       // אם השרת מסמן נקרא/לא נקרא
}
