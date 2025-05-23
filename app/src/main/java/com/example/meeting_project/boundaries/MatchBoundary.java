package com.example.meeting_project.boundaries;

import java.util.ArrayList;

public class MatchBoundary{
    private static ArrayList<MatchBoundary> matches;
    private String matchedUserId;
    private String timestamp;

    public MatchBoundary(String matchedUserId, String timestamp) {
        this.matchedUserId = matchedUserId;
        this.timestamp = timestamp;
    }
    public static ArrayList<MatchBoundary> getMatches() {
        return matches;
    }
    public static void setMatches(ArrayList<MatchBoundary> matches) {
        MatchBoundary.matches = matches;
    }

    public String getMatchedUserId() {
        return matchedUserId;
    }

    public MatchBoundary setMatchedUserId(String matchedUserId) {
        this.matchedUserId = matchedUserId;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public MatchBoundary setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "MatchBoundary{" +
                "matchedUserId='" + matchedUserId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
