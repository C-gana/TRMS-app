package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.AnalyticsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AnalyticsApiService {
    @GET("api/mobile/analytics/{vehicleId}")
    Call<AnalyticsResponse> getAnalytics(
            @Path("vehicleId") String vehicleId,
            @Query("period") String period, // "today", "week", or "month"
            @Header("Authorization") String token
    );
}

