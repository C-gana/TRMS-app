package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.DestinationRequest;
import com.cgana.trmsownerapp.data.model.DestinationsResponse;
import com.cgana.trmsownerapp.data.model.GenericResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DestinationsApiService {
    @GET("api/mobile/destinations/{vehicleId}")
    Call<DestinationsResponse> getDestinations(
            @Path("vehicleId") String vehicleId,
            @Header("Authorization") String token
    );

    @POST("api/mobile/destinations/{vehicleId}")
    Call<GenericResponse> createDestination(
            @Path("vehicleId") String vehicleId,
            @Body DestinationRequest request,
            @Header("Authorization") String token
    );

    @PUT("api/mobile/destinations/{destinationId}")
    Call<GenericResponse> updateDestination(
            @Path("destinationId") int destinationId,
            @Body DestinationRequest request,
            @Header("Authorization") String token
    );

    @DELETE("api/mobile/destinations/{destinationId}")
    Call<GenericResponse> deleteDestination(
            @Path("destinationId") int destinationId,
            @Header("Authorization") String token
    );
}

