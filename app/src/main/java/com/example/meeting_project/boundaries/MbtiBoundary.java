package com.example.meeting_project.boundaries;

import com.example.meeting_project.enums.PersonalityType;

/**
 * MbtiBoundary class represents the boundary object for MBTI personality type.
 * It contains the user's ID, personality type, and characteristics.
 */

public class MbtiBoundary {
    private Long id;
    private String userId;
    private PersonalityType personalityType;
    private String characteristics;

    public MbtiBoundary(Long id, String userId, PersonalityType personalityType, String characteristics) {
        this.id = id;
        this.userId = userId;
        this.personalityType = personalityType;
        this.characteristics = characteristics;
    }

    public Long getId() {
        return id;
    }

    public MbtiBoundary setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public MbtiBoundary setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public PersonalityType getPersonalityType() {
        return personalityType;
    }
    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }
}