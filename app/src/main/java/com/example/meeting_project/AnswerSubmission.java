package com.example.meeting_project;

import java.util.List;

public class AnswerSubmission {
    private List<Answer> answers;
    private String gender;  // הוסף שדה מגדר

    public AnswerSubmission(List<Answer> answers, String gender) {
        this.answers = answers;
        this.gender = gender;  // אתחול שדה המגדר
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public String getGender() {
        return gender;
    }

    // מחלקת Answer המייצגת כל תשובה
    public static class Answer {
        private String id;
        private int value;

        public Answer(String id, int value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public int getValue() {
            return value;
        }
    }
}
