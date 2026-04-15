package com.cgana.trmsownerapp.data.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private Retrofit retrofit;

    private RetrofitClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(ApiConfig.TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.TIMEOUT, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public AuthApiService getAuthApi() {
        return retrofit.create(AuthApiService.class);
    }

    public DashboardApiService getDashboardApi() {
        return retrofit.create(DashboardApiService.class);
    }

    public JourneysApiService getJourneysApi() {
        return retrofit.create(JourneysApiService.class);
    }

    public AnalyticsApiService getAnalyticsApi() {
        return retrofit.create(AnalyticsApiService.class);
    }

    public DestinationsApiService getDestinationsApi() {
        return retrofit.create(DestinationsApiService.class);
    }

    public AlertsApiService getAlertsApi() {
        return retrofit.create(AlertsApiService.class);
    }

    public FCMApiService getFCMApi() {
        return retrofit.create(FCMApiService.class);
    }

    public RouteHistoryApiService getRouteHistoryApi() {
        return retrofit.create(RouteHistoryApiService.class);
    }
}

