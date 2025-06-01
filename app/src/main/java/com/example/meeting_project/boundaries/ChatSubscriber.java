package com.example.meeting_project.boundaries;

import android.widget.Toast;

import com.example.chatlibrary.ChatSdk;
import com.example.chatlibrary.models.Chat;
import com.example.chatlibrary.models.Message;
import com.example.meeting_project.APIRequests.ChatApi;
import com.example.meeting_project.interfaces.Subscriber;
import com.example.meeting_project.managers.AppManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatSubscriber implements Subscriber {
    private ArrayList<ChatBoundary> chats;

    public ChatSubscriber() {
        chats = new ArrayList<>();
    }

    public ArrayList<ChatBoundary> getChats() {
        return chats;
    }

    public ChatSubscriber setChats(ArrayList<ChatBoundary> chats) {
        this.chats = chats;
        return this;
    }

    @Override
    public void actOnUpdate() {
        ChatSdk chatSdk = ChatSdk.getInstance();
        chatSdk.getChatsForUser(AppManager.getAppUser().getId(), new Callback<List<Chat>>() {

            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                chats.clear();
                if (response.body() == null) return;
                if (response.body().isEmpty()) return;

                for (Chat chat : response.body()) {
                    ArrayList<MessageBoundary> messages = new ArrayList<>();
                    for (Message message : chat.getAllMessages()) {
                        messages.add(new MessageBoundary(message.getSenderId(), message.getContent()));
                    }
                    chats.add(new ChatBoundary(chat.getId(), messages));
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                Toast.makeText(AppManager.getCurrentContext(), "Failed to get chats", Toast.LENGTH_SHORT).show();
            }
        });
        // use chat client api to get chats from server
        // only update UI with the new chats
        // it should work as follows:
        // get chats from server -> chatsFromServer
        // check if chats.equals(chatsFromServer)
        // if false -> this.chats = chatsFromServer
        // OPTIONAL -> if the chats are not equal send a notification to the user
        // else do nothing
    }
}
