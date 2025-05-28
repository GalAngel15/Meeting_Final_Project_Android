package com.example.meeting_project.APIRequests;

import com.example.meeting_project.boundaries.QuestionsBoundary;
import com.example.meeting_project.boundaries.UserAnswerBoundary;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
//the questions of the server
public interface QuestionsApi {
    // נקודת קצה: GET /questions/all
    @GET("/questions/all")
    Call<List<QuestionsBoundary>> getAllQuestions();

    // נקודת קצה: POST /questions/post
    @POST("/questions/post")
    Call<String> postQuestion(@Body QuestionsBoundary question);

    // נקודת קצה: PUT /questions/update
    @PUT("/questions/update")
    Call<String> updateQuestion(@Body QuestionsBoundary question);

    // נקודת קצה: DELETE /questions/delete/{questionId}
    @DELETE("/questions/delete/{questionId}")
    Call<String> deleteQuestion(@Path("questionId") String questionId);

    // נקודת קצה: GET /questions/answer/{questionId}
    @GET("/questions/answer/{questionId}")
    Call<String> getAnswerByQuestion(@Path("questionId") String questionId);

    // נקודת קצה: GET /questions/user/{userId}
    @GET("/questions/user/{userId}")
    Call<List<QuestionsBoundary>> getQuestionsByUser(@Path("userId") String userId);

    // נקודת קצה: POST /questions/answer/save
    @POST("/questions/answer/save")
    Call<String> saveUserAnswer(@Body UserAnswerBoundary userAnswer);

    // נקודת קצה: GET /questions/answers/{userId}
    @GET("/questions/answers/{userId}")
    Call<List<UserAnswerBoundary>> getUserAnswers(@Path("userId") String userId);

    // נקודת קצה: POST /questions/import
    @POST("/questions/import")
    Call<String> importQuestions();

    // נקודת קצה: DELETE /questions/all
    @DELETE("/questions/all")
    Call<String> deleteAllQuestionsAndAnswers();
}

