package com.example.meeting_project.boundaries;

public class UserAnswerBoundary {
    private Long id;
    private String userId;
    private String questionId;
    private String answer;

    public UserAnswerBoundary(Long id, String userId, String questionId, String answer) {
        this.id = id;
        this.userId = userId;
        this.questionId = questionId;
        this.answer = answer;
    }

    public Long getId() {
        return id;
    }

    public UserAnswerBoundary setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserAnswerBoundary setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getQuestionId() {
        return questionId;
    }

    public UserAnswerBoundary setQuestionId(String questionId) {
        this.questionId = questionId;
        return this;
    }

    public String getAnswer() {
        return answer;
    }

    public UserAnswerBoundary setAnswer(String answer) {
        this.answer = answer;
        return this;
    }
}
