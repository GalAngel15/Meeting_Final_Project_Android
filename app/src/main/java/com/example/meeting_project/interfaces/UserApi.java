package com.example.meeting_project.interfaces;

import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.boundaries.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {

    // יצירת משתמש חדש (POST /users/create)
    @POST("/users/create")
    Call<UserResponse> createUser(@Body UserBoundary user);

    // קבלת משתמש לפי ID (GET /users/{id})
    @GET("/users/{id}")
    Call<UserBoundary> getUserById(@Path("id") String id);

    // עדכון משתמש לפי ID (PUT /users/update/id/{id})
    @PUT("/users/update/id/{id}")
    Call<String> updateUser(@Body UserBoundary user, @Path("id") String id);

    // קבלת משתמשים לפי טיפוס MBTI (GET /users/mbti/{mbtiType})
    @GET("/users/mbti/{mbtiType}")
    Call<List<UserBoundary>> getUsersByMBTI(@Path("mbtiType") String mbtiType);

    // קבלת משתמשים בטווח גילאים (GET /users/age-range?minAge=...&maxAge=...)
    @GET("/users/age-range")
    Call<List<UserBoundary>> getUsersByAgeRange(@Query("minAge") int minAge, @Query("maxAge") int maxAge);

    // קבלת משתמשים לפי מיקום (GET /users/location/{location})
    @GET("/users/location/{location}")
    Call<List<UserBoundary>> getUsersByLocation(@Path("location") String location);

    // קבלת משתמשים לפי מגדר (GET /users/gender/{gender})
    @GET("/users/gender/{gender}")
    Call<List<UserBoundary>> getUsersByGender(@Path("gender") String gender);

    // קבלת רשימת ההתאמות של משתמש (GET /users/matches/{userId})
    @GET("/users/matches/{userId}")
    Call<List<String>> getAllMatches(@Path("userId") String userId);

    // עדכון מיקום של משתמש (PUT /users/location/{userId}?location=...)
    @PUT("/users/location/{userId}")
    Call<String> updateUserLocation(@Path("userId") String userId, @Query("location") String location);

    // עדכון תמונת פרופיל של משתמש (PUT /users/profile-photo/{userId}?photoUrl=...)
    @PUT("/users/profile-photo/{userId}")
    Call<String> updateProfilePhoto(@Path("userId") String userId, @Query("photoUrl") List<String> galleryUrls);

    // עדכון סיסמה של משתמש (PUT /users/password/{userId}?password=...)
    @PUT("/users/password/{userId}")
    Call<String> updateUserPassword(@Path("userId") String userId, @Query("password") String password);

    // מחיקת משתמש לפי ID (DELETE /users/{userId})
    @DELETE("/users/{userId}")
    Call<String> deleteUser(@Path("userId") String userId);

    // הוספת התאמה בין שני משתמשים (POST /users/users/{userId}/match/{matchedUserId})
    @POST("/users/users/{userId}/match/{matchedUserId}")
    Call<String> addMatch(@Path("userId") String userId, @Path("matchedUserId") String matchedUserId);

    // מחיקת כל המשתמשים (DELETE /users/all)
    @DELETE("/users/all")
    Call<String> deleteAllUsers();

    // קבלת כל המשתמשים (GET /users/all)
    @GET("/users/all")
    Call<List<UserBoundary>> getAllUsers();
}

