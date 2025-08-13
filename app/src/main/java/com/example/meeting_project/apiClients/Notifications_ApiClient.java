package com.example.meeting_project.apiClients;

import com.example.meeting_project.APIRequests.NotificationsApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Notifications_ApiClient {
    private static Retrofit retrofit;

    private static final String BASE_URL = ApiConfig.USERS_BASE_URL;

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
