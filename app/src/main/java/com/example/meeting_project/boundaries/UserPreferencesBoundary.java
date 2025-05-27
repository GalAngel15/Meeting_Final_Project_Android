package com.example.meeting_project.boundaries;

import com.example.meeting_project.enums.Gender;

public class UserPreferencesBoundary {
    private String id;
    private String userId;

    private Gender preferredGender;  // העדפת מין (למשל MALE/FEMALE/OTHER)
    // private String preferredGender;  // משתמשים ב-String לייצוג Enum בצורה פשוטה ב-DTO
    private Integer preferredMaxDistanceKm; // רדיוס חיפוש מועדף
    private Integer minYear;  // גיל מינימלי מועדף
    private Integer maxYear;    // גיל מקסימלי מועדף


    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPreferredGender(Gender preferredGender) {
        this.preferredGender = preferredGender;
    }

    public void setPreferredMaxDistanceKm(Integer preferredMaxDistanceKm) {
        this.preferredMaxDistanceKm = preferredMaxDistanceKm;
    }

    public void setMinYear(Integer minYear) {
        this.minYear = minYear;
    }

    public void setMaxYear(Integer maxYear) {
        this.maxYear = maxYear;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Gender getPreferredGender() {
        return preferredGender;
    }

    public Integer getPreferredMaxDistanceKm() {
        return preferredMaxDistanceKm;
    }

    public Integer getMinYear() {
        return minYear;
    }

    public Integer getMaxYear() {
        return maxYear;
    }

}