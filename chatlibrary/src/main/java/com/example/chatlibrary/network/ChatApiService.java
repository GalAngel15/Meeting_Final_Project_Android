package com.example.chatlibrary.network;

import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.chatlibrary.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApiService {

    // User APIs
    @POST("/users/register")
    Call<User> registerUser(@Query("username") String username,
                            @Query("passwordHash") String passwordHash);

    @GET("/users/login")
    Call<User> loginUser(@Query("username") String username,
                         @Query("password") String password);

    @GET("/users/loadUser")
    Call<User> getUserById(@Query("userId") String userId);

    @PUT("/users/logout")
    Call<User> logoutUser(@Query("id") String userId);

    @GET("/users/get-all-users")
    Call<List<User>> getAllUsers();

    // Chat APIs
    @POST("/chats/create")
    Call<Chat> createChat(@Query("user1Id") String user1Id, @Query("user2Id") String user2Id);

    @GET("/chats/{chatId}")
    Call<Chat> getChatById(@Path("chatId") String chatId);

    @GET("/chats/get-all-chats-of-user/{userId}")
    Call<List<Chat>> getChatsForUser(@Path("userId") String userId);

    // Message APIs
    @POST("/messages/send")
    Call<Message> sendMessage(@Query("chatId") String chatId,
                              @Query("senderId") String senderId,
                              @Query("receiverId") String receiverId,
                              @Query("content") String content);

    @GET("/messages/messages-in-chat/{chatId}")
    Call<List<Message>> getMessagesByChatId(@Path("chatId") String chatId);
}
