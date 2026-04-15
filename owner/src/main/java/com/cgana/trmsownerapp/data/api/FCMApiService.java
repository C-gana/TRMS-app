package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.FCMTokenRequest;
import com.cgana.trmsownerapp.data.model.GenericResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FCMApiService {
    @POST("api/mobile/fcm-token")
    Call<GenericResponse> registerFCMToken(
            @Body FCMTokenRequest request,
            @Header("Authorization") String token
    );
}

