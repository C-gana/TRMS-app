package com.cgana.trmsdriver.ui.login;

import androidx.annotation.Nullable;

import com.cgana.trmsdriver.data.api.ApiConfig;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.Driver;
import com.cgana.trmsdriver.data.model.LoginResponse;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class LoginRepository {

    interface AuthService {
        @POST(ApiConfig.AUTH_LOGIN)
        Call<LoginResponse> login(@Body LoginRequest request);
    }

    private final AuthService service;
    private final TokenManager tokenManager;

    public LoginRepository(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(AuthService.class);
    }

    public Call<LoginResponse> login(String phone, String password) {
        return service.login(new LoginRequest(phone, password));
    }

    public void persistLogin(@Nullable String token, @Nullable Driver driver, boolean remember) {
        if (token != null) {
            tokenManager.saveToken(token);
        }
        if (driver != null) {
            tokenManager.saveDriver(driver);
        }
        // Remember me handling
        if (!remember) {
            // Could implement session-only storage here
        }
    }
}

