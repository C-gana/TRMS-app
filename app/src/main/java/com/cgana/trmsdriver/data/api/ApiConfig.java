package com.cgana.trmsdriver.data.api;

public class ApiConfig {
    // Change for production
//    local  10.80.182.236
//    network    192.168.43.229
    public static final String BASE_URL = "http://192.168.43.229:3000/";
    // REST paths
    public static final String AUTH_LOGIN = "api/drivers/login";
    public static final long TIMEOUT = 30; // seconds
}
