package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.JourneysResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JourneysApiService {
    @GET("api/mobile/journeys/{vehicleId}")
    Call<JourneysResponse> getJourneys(
            @Path("vehicleId") String vehicleId,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("page") int page,
            @Query("limit") int limit,
            @Header("Authorization") String token
    );

    @GET("api/mobile/export/journeys/{vehicleId}")
    Call<ResponseBody> exportJourneys(
            @Path("vehicleId") String vehicleId,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Header("Authorization") String token
    );
}

