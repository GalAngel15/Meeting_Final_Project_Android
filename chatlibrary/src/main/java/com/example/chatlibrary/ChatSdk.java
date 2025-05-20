package com.example.chatlibrary;

import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.chatlibrary.models.User;
import com.example.chatlibrary.network.ChatApiService;
import com.example.chatlibrary.network.RetrofitClient;

import java.util.List;

import retrofit2.Callback;

public class ChatSdk {

    private static ChatSdk instance;
    private final ChatApiService apiService;

    private ChatSdk() {
        apiService = RetrofitClient.getInstance().create(ChatApiService.class);
    }

    public static ChatSdk getInstance() {
        if (instance == null) {
            instance = new ChatSdk();
        }
        return instance;
    }

    public void registerUser(String username, String passwordHash, Callback<User> callback) {
        apiService.registerUser(username, passwordHash).enqueue(callback);
    }

    public void loginUser(String username, String password, Callback<User> callback) {
        apiService.loginUser(username, password).enqueue(callback);
    }
    public void loadUser(String userId, Callback<User> callback){
        apiService.getUserById(userId).enqueue(callback);
    }

    public void logoutUser(String userId, Callback<User> callback) {
        apiService.logoutUser(userId).enqueue(callback);
    }

    public void getAllUsers(Callback<List<User>> callback) {
        apiService.getAllUsers().enqueue(callback);
    }

    public void createChat(String user1Id, String user2Id, Callback<Chat> callback) {
        apiService.createChat(user1Id, user2Id).enqueue(callback);
    }

    public void sendMessage(String chatId, String senderId, String receiverId, String content, Callback<Message> callback) {
        apiService.sendMessage(chatId, senderId, receiverId, content).enqueue(callback);
    }

    public void getMessagesByChatId(String chatId, Callback<List<Message>> callback) {
        apiService.getMessagesByChatId(chatId).enqueue(callback);
    }

    public void getChatsForUser(String currentUserId, Callback<List<Chat>> callback) {
        apiService.getChatsForUser(currentUserId).enqueue(callback);
    }
}

