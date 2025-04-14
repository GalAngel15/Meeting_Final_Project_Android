package com.example.meeting_project;

import java.util.List;

public class QuestionMBTI {
    private String id;
    private String text;
    private List<OptionMBTI> optionMBTIS;
    private int selectedAnswer = -1;

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<OptionMBTI> getOptions() {
        return optionMBTIS;
    }
    public void setSelectedAnswer(int selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public int getSelectedAnswer() {
        return selectedAnswer;
    }
}
