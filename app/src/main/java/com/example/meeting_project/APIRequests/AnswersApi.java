package com.example.meeting_project.APIRequests;

import com.example.meeting_project.boundaries.UserAnswerBoundary;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Path;
//save answer of the questions of the server
public interface AnswersApi {

    // שמירת תשובה בודדת (POST /answers/save?userId=...&questionId=...&answer=...)
    @POST("answers/save")
    Call<String> saveUserAnswer(
            @Query("userId") String userId,
            @Query("questionId") String questionId,
            @Query("answer") String answer
    );

    // שליפת כל התשובות של משתמש (GET /answers/user/{userId})
    @GET("answers/user/{userId}")
    Call<List<UserAnswerBoundary>> getUserAnswers(@Path("userId") String userId);
}
