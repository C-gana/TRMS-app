package com.cgana.trmsdriver.data.api;

public class ApiConfig {
    // Change for production
//    local  10.80.182.236
//    network    192.168.43.229
    public static final String BASE_URL = "http://192.168.43.229:3000/";
    // REST paths
    public static final String AUTH_LOGIN = "api/mobile/auth/driver-login";
    public static final String LOGIN = "api/mobile/auth/driver-login";
    public static final String DUTY_STATUS = "api/mobile/driver/duty-status";
    public static final long TIMEOUT = 30; // seconds
}
