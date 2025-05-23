package com.example.meeting_project.interfaces;

import com.example.meeting_project.boundaries.UserPreferencesBoundary;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserPreferencesApi {

    // יצירת העדפות משתמש (POST /user-preferences/create)
    @POST("/user-preferences/create")
    Call<UserPreferencesBoundary> createUserPreferences(@Body UserPreferencesBoundary preferences);

    // שליפת העדפות משתמש לפי userId (GET /user-preferences/{userId})
    @GET("/user-preferences/{userId}")
    Call<UserPreferencesBoundary> getUserPreferencesByUserId(@Path("userId") String userId);

    // עדכון העדפות משתמש לפי userId (PUT /user-preferences/update/{userId})
    @PUT("/user-preferences/update/{userId}")
    Call<String> updateUserPreferences(@Body UserPreferencesBoundary preferences, @Path("userId") String userId);
}
