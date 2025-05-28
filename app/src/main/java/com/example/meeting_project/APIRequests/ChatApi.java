package com.example.meeting_project.APIRequests;

import com.example.meeting_project.boundaries.ChatBoundary;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ChatApi {
    @GET("/chats/{userId}")
    Call<List<ChatBoundary>> getChats(@Path("userId") String userId);
}
