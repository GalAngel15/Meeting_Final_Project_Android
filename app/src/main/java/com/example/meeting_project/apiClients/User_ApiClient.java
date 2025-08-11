package com.example.meeting_project.apiClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class User_ApiClient {
    //yahav home wifi
    //private static final String BASE_URL = "http://192.168.68.100:8081/";
    //emulator address
    private static final String BASE_URL = ApiConfig.BASE_IP + ":8081/";
    private static Retrofit retrofit = null;

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd")
                    .create();

            // מוסיף לוגים
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .addInterceptor(logging) // לוגים
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create()) // קודם Scalars
                    .addConverterFactory(GsonConverterFactory.create(gson)) // ואז Gson
                    .build();
        }
        return retrofit;
    }
}


