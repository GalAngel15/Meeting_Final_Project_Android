package com.example.meeting_project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
//    private static final String BASE_URL = "http://192.168.56.1:3000/";
//    private static final String BASE_URL = "http://192.168.68.111:3000/";
//    private static final String BASE_URL = "http://192.168.1.166:3000/";
      private static final String BASE_URL = "http://192.168.154.1:3000/";

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
