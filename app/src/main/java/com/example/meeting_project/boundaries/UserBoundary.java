package com.example.meeting_project.boundaries;

import java.sql.Date;
import java.util.List;


public class UserBoundary {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String gender;

    private String mbtiId;  // מחזיק את מזהה ה-MBTI בלבד
    private String profilePhotoUrl;
    private List<String> galleryUrls;

    private Double Latitude;
    private Double Longitude;
    private Date dateOfBirth;

    private List<String> likedUserIds;
    private List<String> matchedUserIds;

    private UserPreferencesBoundary preferences;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMbtiId() {
        return mbtiId;
    }

    public void setMbtiId(String mbtiId) {
        this.mbtiId = mbtiId;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<String> getLikedUserIds() {
        return likedUserIds;
    }

    public void setLikedUserIds(List<String> likedUserIds) {
        this.likedUserIds = likedUserIds;
    }

    public List<String> getMatchedUserIds() {
        return matchedUserIds;
    }

    public void setMatchedUserIds(List<String> matchedUserIds) {
        this.matchedUserIds = matchedUserIds;
    }

    public UserPreferencesBoundary getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferencesBoundary preferences) {
        this.preferences = preferences;
    }
    public List<String> getGalleryUrls() {
        return galleryUrls;
    }
    public void setGalleryUrls(List<String> galleryUrls) {
        this.galleryUrls = galleryUrls;
    }
}
