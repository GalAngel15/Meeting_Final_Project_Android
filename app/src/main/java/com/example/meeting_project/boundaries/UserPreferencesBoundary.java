package com.example.meeting_project.boundaries;

import com.example.meeting_project.enums.Gender;

public class UserPreferencesBoundary {
    private Long id;
    private Long userId;

    private Gender preferredGender;  // העדפת מין (למשל MALE/FEMALE/OTHER)
    // private String preferredGender;  // משתמשים ב-String לייצוג Enum בצורה פשוטה ב-DTO
    private Integer preferredMaxDistanceKm; // רדיוס חיפוש מועדף
    private Integer minAge;  // גיל מינימלי מועדף
    private Integer maxAge;  // גיל מקסימלי מועדף
}