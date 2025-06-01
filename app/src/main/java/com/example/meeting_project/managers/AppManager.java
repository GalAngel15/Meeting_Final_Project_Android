package com.example.meeting_project.managers;

import android.content.Context;

import com.example.meeting_project.boundaries.ChatBoundary;
import com.example.meeting_project.boundaries.ChatSubscriber;
import com.example.meeting_project.boundaries.MatchBoundary;
import com.example.meeting_project.boundaries.MatchSubscriber;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.utilities.DataFetcher;

import java.util.ArrayList;

public class AppManager {
    private static AppManager instance;
    private MatchSubscriber matchSubscriber;
    private ChatSubscriber chatSubscriber;
    private UserBoundary appUser;
    private DataFetcher dataFetcher;
    private Context appContext;
    private AppManager(){
        matchSubscriber = new MatchSubscriber();
        // TBD - check if there's a user in memory, if there is - update the appUser
    }

    public static AppManager getInstance(){
        initAppManager();
        return instance;
    }

    public static void initAppManager(){
        if(instance == null) instance = new AppManager();
    }

    public static boolean isUserLoggedIn(){
        return instance.appUser != null;
    }

    public static void userLoggedIn(UserBoundary user) {
        instance.appUser = user;
        instance.matchSubscriber = new MatchSubscriber();
        instance.chatSubscriber = new ChatSubscriber();
        instance.dataFetcher = DataFetcher.getInstance();
        instance.dataFetcher.subscribe(instance.matchSubscriber);
        instance.dataFetcher.subscribe(instance.chatSubscriber);
        instance.dataFetcher.start();
    }

    public static ArrayList<ChatBoundary> getChatInfo(){
        return instance.chatSubscriber.getChats();
    }
    public static ArrayList<MatchBoundary> getMatches(){
        return instance.matchSubscriber.getMatches();
    }
    public static UserBoundary getAppUser(){
        return instance.appUser;
    }
    public static Context getCurrentContext(){
        return instance.appContext;
    }
    public static void setContext(Context context){
        instance.appContext = context;
    }



}
