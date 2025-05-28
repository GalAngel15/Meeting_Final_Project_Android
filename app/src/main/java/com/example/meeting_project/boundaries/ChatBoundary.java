package com.example.meeting_project.boundaries;

import java.util.ArrayList;
import java.util.Objects;

public class ChatBoundary {
    private String id;
    private ArrayList<MessageBoundary> messages;

    public ChatBoundary(String id, ArrayList<MessageBoundary> messages) {
        this.id = id;
        this.messages = messages;
    }

    public String getId() {
        return id;
    }

    public ChatBoundary setId(String id) {
        this.id = id;
        return this;
    }

    public ArrayList<MessageBoundary> getMessages() {
        return messages;
    }

    public ChatBoundary setMessages(ArrayList<MessageBoundary> messages) {
        this.messages = messages;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatBoundary that = (ChatBoundary) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getMessages(), that.getMessages());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMessages());
    }
}
