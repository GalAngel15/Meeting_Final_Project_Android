package com.example.meeting_project.boundaries;

import java.util.Date;

public class MatchPercentageBoundary {
    private String userId1;
    private String userId2;
    private Double matchPercentage;

    public Date getMatchCreatedAt() {
        return matchCreatedAt;
    }

    public void setMatchCreatedAt(Date matchCreatedAt) {
        this.matchCreatedAt = matchCreatedAt;
    }

    public Boolean getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(Boolean matchStatus) {
        this.matchStatus = matchStatus;
    }

    public Double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(Double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    private Boolean matchStatus;
    private Date matchCreatedAt;

    public MatchPercentageBoundary(String userId1, String userId2, Double matchPercentage, Boolean matchStatus, Date matchCreatedAt) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.matchPercentage = matchPercentage;
        this.matchStatus = matchStatus;
        this.matchCreatedAt = matchCreatedAt;
    }

}
