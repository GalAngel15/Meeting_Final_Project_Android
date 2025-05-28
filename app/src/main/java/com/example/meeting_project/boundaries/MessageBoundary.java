package com.example.meeting_project.boundaries;

import java.util.Objects;

public class MessageBoundary {
    private String senderId;
    private String content;

    public MessageBoundary(String senderId, String content) {
        this.senderId = senderId;
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public MessageBoundary setSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MessageBoundary setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageBoundary that = (MessageBoundary) o;
        return Objects.equals(getSenderId(), that.getSenderId()) && Objects.equals(getContent(), that.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSenderId(), getContent());
    }
}
