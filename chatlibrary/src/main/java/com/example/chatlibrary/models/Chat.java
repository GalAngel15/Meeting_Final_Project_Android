package com.example.chatlibrary.models;

import java.util.ArrayList;

public class Chat {
    private String id;
    private String user1Id;
    private String username1;
    private String user2Id;
    private String username2;
    private ArrayList<Message> allMessages;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getLastMessage() {
        return allMessages.get(allMessages.size() - 1).getContent();
    }

    public String getUsername1() {
        return username1;
    }

    public void setUsername1(String username1) {
        this.username1 = username1;
    }

    public String getUsername2() {
        return username2;
    }

    public void setUsername2(String username2) {
        this.username2 = username2;
    }

    public void addMessage(Message message) {
        this.allMessages.add(message);
    }

    public ArrayList<Message> getAllMessages() {
        return this.allMessages;
    }

    public void setAllMessages(ArrayList<Message> allMessages) {
        this.allMessages = allMessages;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", user1Id='" + user1Id + '\'' +
                ", username1='" + username1 + '\'' +
                ", user2Id='" + user2Id + '\'' +
                ", username2='" + username2 + '\'' +
                ", allMessages=" + allMessages +
                '}';
    }
}
