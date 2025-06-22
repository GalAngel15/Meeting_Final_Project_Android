package com.example.meeting_project.boundaries;

import java.util.ArrayList;
import java.util.Objects;

public class ChatBoundary {
    private Long id;
    private ArrayList<MessageBoundary> messages;

    public ChatBoundary(Long id, ArrayList<MessageBoundary> messages) {
        this.id = id;
        this.messages = messages;
    }

    public Long getId() {
        return id;
    }

    public ChatBoundary setId(Long id) {
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
