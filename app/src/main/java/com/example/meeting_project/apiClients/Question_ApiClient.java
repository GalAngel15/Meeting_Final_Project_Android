package com.example.meeting_project.apiClients;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Question_ApiClient {
    //yahav home wifi
    //private static final String BASE_URL = "http://192.168.1.166:8082/";
    //emulator address
    private static final String BASE_URL = "http://10.0.2.2:8082/";
    private static Retrofit retrofit = null;

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
