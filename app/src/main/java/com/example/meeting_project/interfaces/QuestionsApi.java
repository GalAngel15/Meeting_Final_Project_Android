package com.example.meeting_project.interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface QuestionsApi {
    @GET("/questions/all")
    Call<List<QuestionEntity>> getAllQuestions();
    @POST("/import")
    Call<String> importQuestions();
}

