package com.example.meeting_project.boundaries;

import com.example.meeting_project.enums.QuestionType;

import java.util.List;

public class QuestionsBoundary {
    private String id;
    private String questionText; //the question
    private QuestionType questionType; //open or amultiple choice
    private String questionCategory;//the topic of the question
    private List<String> possibleAnswers;

    public QuestionsBoundary(String id, String questionText, String questionCategory, QuestionType questionType, List<String> possibleAnswers) {
        this.id = id;
        this.questionText = questionText;
        this.questionCategory = questionCategory;
        this.questionType = questionType;
        this.possibleAnswers = possibleAnswers;
    }

    public String getId() {
        return id;
    }

    public QuestionsBoundary setId(String id) {
        this.id = id;
        return this;
    }

    public String getQuestionText() {
        return questionText;
    }

    public QuestionsBoundary setQuestionText(String questionText) {
        this.questionText = questionText;
        return this;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public QuestionsBoundary setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
        return this;
    }

    public String getQuestionCategory() {
        return questionCategory;
    }

    public QuestionsBoundary setQuestionCategory(String questionCategory) {
        this.questionCategory = questionCategory;
        return this;
    }

    public List<String> getPossibleAnswers() {
        return possibleAnswers;
    }

    public QuestionsBoundary setPossibleAnswers(List<String> possibleAnswers) {
        this.possibleAnswers = possibleAnswers;
        return this;
    }
}