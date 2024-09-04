package com.example.meeting_project;

import java.util.List;

public class Question {
    private String id;
    private String text;
    private List<Option> options;

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<Option> getOptions() {
        return options;
    }
}
