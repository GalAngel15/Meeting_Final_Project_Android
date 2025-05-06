package com.example.meeting_project.boundaries;

import java.sql.Date;
import java.util.List;

public class UserResponse {
    public String id;
    public String firstName;
    public String lastName;
    public String email;
    public String phoneNumber;
    public String gender;
    public String mbtiId;
    public String profilePhotoUrl;
    public String location;
    public Date dateOfBirth;
    public List<String> likedUserIds;
    public List<String> matchedUserIds;
    public UserPreferencesBoundary preferences;

    public UserResponse(UserPreferencesBoundary preferences, List<String> matchedUserIds, List<String> likedUserIds, Date dateOfBirth, String location, String profilePhotoUrl, String mbtiId, String gender, String phoneNumber, String id, String firstName, String lastName, String email) {
        this.preferences = preferences;
        this.matchedUserIds = matchedUserIds;
        this.likedUserIds = likedUserIds;
        this.dateOfBirth = dateOfBirth;
        this.location = location;
        this.profilePhotoUrl = profilePhotoUrl;
        this.mbtiId = mbtiId;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public UserPreferencesBoundary getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferencesBoundary preferences) {
        this.preferences = preferences;
    }

    public List<String> getMatchedUserIds() {
        return matchedUserIds;
    }

    public void setMatchedUserIds(List<String> matchedUserIds) {
        this.matchedUserIds = matchedUserIds;
    }

    public List<String> getLikedUserIds() {
        return likedUserIds;
    }

    public void setLikedUserIds(List<String> likedUserIds) {
        this.likedUserIds = likedUserIds;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getMbtiId() {
        return mbtiId;
    }

    public void setMbtiId(String mbtiId) {
        this.mbtiId = mbtiId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
