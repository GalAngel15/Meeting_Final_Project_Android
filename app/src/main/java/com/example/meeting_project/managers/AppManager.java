package com.example.meeting_project.managers;

import com.example.meeting_project.boundaries.MatchSubscriber;

public class AppManager {
    private static AppManager instance;
    private MatchSubscriber matchSubscriber;
    private AppManager(){
        matchSubscriber = new MatchSubscriber();

    }

    public static AppManager getInstance(){
        if(instance == null) instance = new AppManager();
        return instance;
    }


}
