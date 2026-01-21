package com.cgana.trmsdriver.data.api;

import com.cgana.trmsdriver.data.model.AlightingRequest;
import com.cgana.trmsdriver.data.model.AlightingResponse;
import com.cgana.trmsdriver.data.model.BoardingRequest;
import com.cgana.trmsdriver.data.model.BoardingResponse;
import com.cgana.trmsdriver.data.model.DashboardResponse;
import com.cgana.trmsdriver.data.model.DestinationResponse;
import com.cgana.trmsdriver.data.model.JourneyDetailResponse;
import com.cgana.trmsdriver.data.model.JourneyHistoryResponse;
import com.cgana.trmsdriver.data.model.LocationUpdateRequest;
import com.cgana.trmsdriver.data.model.LocationUpdateResponse;
import com.cgana.trmsdriver.data.model.MissedStopRequest;
import com.cgana.trmsdriver.data.model.MissedStopResponse;
import com.cgana.trmsdriver.data.model.SetDestinationRequest;
import com.cgana.trmsdriver.data.model.SetDestinationResponse;
import com.cgana.trmsdriver.data.model.StatisticsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    // Module 3: Destination Selection Endpoints
    @GET("api/mobile/driver/destinations/{vehicleId}")
    Call<DestinationResponse> getDestinations(
            @Path("vehicleId") String vehicleId,
            @Header("Authorization") String token
    );

    @POST("api/mobile/driver/destination")
    Call<SetDestinationResponse> setDestination(
            @Body SetDestinationRequest request,
            @Header("Authorization") String token
    );

    // Module 4: Passenger Alighting Endpoints
    @POST("api/mobile/driver/alighting")
    Call<AlightingResponse> recordAlighting(
            @Body AlightingRequest request,
            @Header("Authorization") String token
    );

    @POST("api/mobile/driver/missed-stop")
    Call<MissedStopResponse> reportMissedStop(
            @Body MissedStopRequest request,
            @Header("Authorization") String token
    );

    // Module 5: Journey History & Statistics Endpoints
    @GET("api/mobile/driver/journey-history")
    Call<JourneyHistoryResponse> getJourneyHistory(
            @Query("vehicle_id") String vehicleId,
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("filter") String filter,
            @Header("Authorization") String token
    );

    @GET("api/mobile/driver/journey/{journeyId}")
    Call<JourneyDetailResponse> getJourneyDetail(
            @Path("journeyId") long journeyId,
            @Header("Authorization") String token
    );

    @GET("api/mobile/driver/statistics")
    Call<StatisticsResponse> getStatistics(
            @Query("vehicle_id") String vehicleId,
            @Query("period") String period,
            @Header("Authorization") String token
    );

    // Module 6: Live Location Tracking Endpoint
    @POST("api/mobile/driver/location")
    Call<LocationUpdateResponse> sendLocationUpdate(
            @Header("Authorization") String token,
            @Body LocationUpdateRequest request
    );
}

