package com.example.meeting_project.boundaries;

import com.example.meeting_project.interfaces.Subscriber;

import java.util.ArrayList;

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
