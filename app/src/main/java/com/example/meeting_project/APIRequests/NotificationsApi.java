package com.example.meeting_project.APIRequests;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotificationsApi {

    @POST("api/notifications/register-token")
    Call<Void> registerToken(@Body RegisterTokenRequest body);

    @POST("api/notifications/unregister-token")
    Call<Void> unregisterToken(@Body RegisterTokenRequest body);

    class RegisterTokenRequest {
        public String userId;
        public String token;
        public RegisterTokenRequest(String userId, String token) {
            this.userId = userId; this.token = token;
        }
    }
}
