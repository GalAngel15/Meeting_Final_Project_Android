package com.example.meeting_project.apiClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MbtiService_ApiClient {
    //yahav home wifi
    //private static final String BASE_URL = "http://192.168.1.166:8083/";
    //emulator address
    private static final String BASE_URL = "http://10.0.2.2:8083/";
    private static Retrofit retrofit= null;

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}

