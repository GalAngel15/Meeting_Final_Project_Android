package com.example.meeting_project.boundaries;

import com.example.meeting_project.interfaces.Subscriber;

import java.util.ArrayList;

public class MatchSubscriber implements Subscriber {

    private ArrayList<MatchBoundary> matches;

    public MatchSubscriber() {
        matches = new ArrayList<>();
    }

    public ArrayList<MatchBoundary> getMatches() {
        return matches;
    }

    public MatchSubscriber setMatches(ArrayList<MatchBoundary> matches) {
        this.matches = matches;
        return this;
    }



    @Override
    public void actOnUpdate() {
        // send api request to update the user's matches
        //update matches in app storage.
        //optional : send notification to the user.
        // use match client api to get matches from server
    }
}
