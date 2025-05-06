package com.example.meeting_project.interfaces;

import com.example.meeting_project.boundaries.MbtiBoundary;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface MbtiServiceApi {

    @GET("/mbti/all")
    Call<List<MbtiBoundary>> getAllProfiles();

    @GET("/mbti/user/{userId}")
    Call<MbtiBoundary> getProfileByUserId(@Path("userId") String userId);

    @GET("/mbti/type/{mbtiType}")
    Call<MbtiBoundary> getProfileByType(@Path("mbtiType") String mbtiType);

    @PUT("/mbti/update/{userId}")
    Call<String> updateProfile(@Path("userId") String userId, @Body MbtiBoundary profile);

    @POST("/mbti/create")
    Call<String> createProfile(@Body MbtiBoundary profile);
}
