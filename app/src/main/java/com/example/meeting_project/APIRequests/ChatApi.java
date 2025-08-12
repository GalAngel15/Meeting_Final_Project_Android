package com.example.meeting_project.APIRequests;

import com.example.meeting_project.boundaries.ChatBoundary;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {

    // Get all chats for a user
    @GET("chats/get-all-chats-of-user/{userId}")
    Call<List<ChatBoundary>> getChatsForUser(@Path("userId") String userId);

    // Create or get a chat between two users
    @POST("chats/create")
    Call<ChatBoundary> createOrGetChat(
            @Query("user1Id") String user1Id,
            @Query("user2Id") String user2Id
    );

    // Get a specific chat by its ID
    @GET("chats/{chatId}")
    Call<ChatBoundary> getChatById(@Path("chatId") String chatId);

    // Update the last message in a chat
    @PUT("chats/{chatId}/update-last-message")
    Call<ChatBoundary> updateLastMessage(
            @Path("chatId") String chatId,
            @Query("lastMessage") String lastMessage
    );

    // Delete all chats (and all messages)
    @DELETE("chats/delete-all-chats")
    Call<Void> deleteAllChats();
}
