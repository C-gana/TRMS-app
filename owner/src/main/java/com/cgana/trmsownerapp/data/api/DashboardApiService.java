package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.DashboardResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface DashboardApiService {
    @GET("api/mobile/dashboard/{vehicleId}")
    Call<DashboardResponse> getDashboard(
            @Path("vehicleId") String vehicleId,
            @Header("Authorization") String token
    );
}

