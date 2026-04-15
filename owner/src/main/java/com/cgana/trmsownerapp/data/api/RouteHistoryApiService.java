package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.RouteHistoryResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RouteHistoryApiService {
    @GET("api/mobile/route-history/{vehicleId}")
    Call<RouteHistoryResponse> getRouteHistory(
        @Path("vehicleId") String vehicleId,
        @Query("start_time") String startTime,
        @Query("end_time") String endTime,
        @Header("Authorization") String token
    );
}

