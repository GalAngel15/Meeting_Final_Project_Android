package com.example.meeting_project.APIRequests;

import com.example.meeting_project.boundaries.MatchBoundary;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MatchApi {
    @GET("/matches/{userId}")
    Call<List<MatchBoundary>> getMatches(@Path("userId") String userId);
}
