package com.cgana.trmsdriver.data.api;

import com.cgana.trmsdriver.data.model.BoardingRequest;
import com.cgana.trmsdriver.data.model.BoardingResponse;
import com.cgana.trmsdriver.data.model.DashboardResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DashboardApiService {

    @GET("api/mobile/driver/dashboard/{vehicleId}")
    Call<DashboardResponse> getDashboardStatus(
            @Path("vehicleId") String vehicleId,
            @Header("Authorization") String token
    );

    @POST("api/mobile/driver/boarding")
    Call<BoardingResponse> recordBoarding(
            @Body BoardingRequest request,
            @Header("Authorization") String token
    );
}

