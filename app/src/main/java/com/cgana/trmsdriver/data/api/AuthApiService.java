package com.cgana.trmsdriver.data.api;

import com.cgana.trmsdriver.data.model. DutyStatusRequest;
import com.cgana.trmsdriver.data. model.DutyStatusResponse;
import com.cgana.trmsdriver.data.model.LoginRequest;
import com.cgana.trmsdriver.data.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/mobile/auth/driver-login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/mobile/auth/login")
    Call<LoginResponse> unifiedLogin(@Body LoginRequest request);

    @POST("api/mobile/driver/duty-status")
    Call<DutyStatusResponse> updateDutyStatus(
            @Body DutyStatusRequest request,
            @Header("Authorization") String token
    );
}
