package com.cgana.trmsownerapp.data.api;

import com.cgana.trmsownerapp.data.model.LoginRequest;
import com.cgana.trmsownerapp.data.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("api/mobile/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}

