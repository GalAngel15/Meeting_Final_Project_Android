package com.example.meeting_project.interfaces;

import com.example.meeting_project.AnswerSubmission;
import com.example.meeting_project.QuestionMBTI;
import com.example.meeting_project.SubmitResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface PersonalityApi {

    // קריאה לקבלת השאלות מה-API
    @GET("/api/personality/questions")
    Call<List<QuestionMBTI>> getQuestions();

    // קריאה לשליחת תשובות השאלון ל-API
    @POST("/api/personality/submit")
    Call<SubmitResponse> submitAnswers(@Body AnswerSubmission submission);
}
