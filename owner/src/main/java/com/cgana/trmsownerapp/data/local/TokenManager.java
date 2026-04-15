package com.cgana.trmsownerapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.cgana.trmsownerapp.data.model.User;
import com.google.gson.Gson;

public class TokenManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER = "user_data";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TokenManager(Context context) {
        gson = new Gson();

        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to regular SharedPreferences
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        sharedPreferences.edit().putString(KEY_USER, userJson).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void saveFCMToken(String fcmToken) {
        sharedPreferences.edit().putString(KEY_FCM_TOKEN, fcmToken).apply();
    }

    public String getFCMToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    public boolean isMockFCMToken() {
        String token = getFCMToken();
        return token != null && token.equals("mock_fcm_token_placeholder");
    }

    public void clearAuth() {
        sharedPreferences.edit().clear().apply();
    }
}

