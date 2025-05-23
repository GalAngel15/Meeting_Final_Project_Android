package com.example.meeting_project.boundaries;

import com.example.meeting_project.interfaces.Subscriber;

import java.util.ArrayList;

public class MatchSubscriber implements Subscriber {

    @Override
    public void actOnUpdate() {
        // send api request to update the user's matches
        //update matches in app storage.
        //optional : send notification to the user.
    }
}
