package com.example.meeting_project.apiClients;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Match_ApiClient {
    //yahav home wifi
    //private static final String BASE_URL = "http://192.168.68.100:8084/";
    //emulator address
    private static final String BASE_URL = "http://10.0.2.2:8084/";//change the port
    private static Retrofit retrofit = null;

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)  // הוסיפי כאן
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
