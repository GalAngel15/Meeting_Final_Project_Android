package com.example.meeting_project.APIRequests;

import com.example.meeting_project.boundaries.MatchBoundary;
import com.example.meeting_project.boundaries.MatchPercentageBoundary;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MatchApi {
    /*@POST("profile-matches/create")
    Call<List<MatchPercentageBoundary>> createPotentialMatches(@Body CreateMatchRequest request);
*/
    @GET("potentialMatches/user/{userId}")
    Call<List<MatchPercentageBoundary>> getMatchesByUserId(@Path("userId") String userId);

    @GET("potentialMatches/percentage")
    Call<List<MatchPercentageBoundary>> getMatchesByPercentage(@Query("minPercentage") int minPercentage);

    @PUT("potentialMatches/confirm")
    Call<ResponseBody> confirmMatch(@Query("userId1") String userId1,
                                    @Query("userId2") String userId2);

    @DELETE("potentialMatches/delete")
    Call<ResponseBody> deletePotentialMatch(@Query("userId1") String userId1,
                                            @Query("userId2") String userId2);

    @GET("potentialMatches/all")
    Call<List<MatchPercentageBoundary>> getAllMatches();
}
