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
    public List<String> galleryUrls;
    private Double latitude;
    private Double longitude;
    public Date dateOfBirth;
    public List<String> likedUserIds;
    public List<String> matchedUserIds;
    public UserPreferencesBoundary preferences;

    public UserResponse(UserPreferencesBoundary preferences, List<String> matchedUserIds, List<String> likedUserIds, Date dateOfBirth, List<String> galleryUrls, String mbtiId, String gender, String phoneNumber, String id, String firstName, String lastName, String email, Double latitude, Double longitude) {
        this.preferences = preferences;
        this.matchedUserIds = matchedUserIds;
        this.likedUserIds = likedUserIds;
        this.dateOfBirth = dateOfBirth;
        this.galleryUrls = galleryUrls;
        this.mbtiId = mbtiId;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<String> getProfilePhotoUrl() {
        return galleryUrls;
    }

    public void setProfilePhotoUrl(List<String> profilePhotoUrl) {
        this.galleryUrls = profilePhotoUrl;
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
