package com.example.meeting_project.APIRequests;

import com.example.meeting_project.objectOfMbtiTest.AnswerSubmission;
import com.example.meeting_project.objectOfMbtiTest.Question;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
//the mbti qustions api - personality type
public interface PersonalityApi {

    // קריאה לקבלת השאלות מה-API
    @GET("/api/personality/questions")
    Call<List<Question>> getQuestions();

    // קריאה לשליחת תשובות השאלון ל-API
    @POST("/api/personality/submit")
    Call<SubmitResponse> submitAnswers(@Body AnswerSubmission submission);
}
