package com.example.meeting_project.apiClients;

import com.example.meeting_project.APIRequests.NotificationsApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Notifications_ApiClient {
    private static Retrofit retrofit;

    // חשוב: השתמש באותו baseUrl כמו ב-User_ApiClient/Match_ApiClient
    private static final String BASE_URL = ApiConfig.USERS_BASE_URL; // אם קיים שם קבוע כזה
    // אם אין, שים כאן אותו URL שמשמש בשאר ה-ApiClient שלך.

    public static NotificationsApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(NotificationsApi.class);
    }
}
