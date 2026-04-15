package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.AcknowledgeRequest;
import com.cgana.trmsownerapp.data.model.AlertsResponse;
import com.cgana.trmsownerapp.data.model.GenericResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AlertsApiService {
    @GET("api/mobile/alerts/{vehicleId}")
    Call<AlertsResponse> getAlerts(
            @Path("vehicleId") String vehicleId,
            @Query("unread") Boolean unread,
            @Header("Authorization") String token
    );

    @PUT("api/mobile/alerts/{alertId}/acknowledge")
    Call<GenericResponse> acknowledgeAlert(
            @Path("alertId") int alertId,
            @Body AcknowledgeRequest request,
            @Header("Authorization") String token
    );
}

