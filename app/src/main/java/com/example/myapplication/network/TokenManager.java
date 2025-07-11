package com.example.myapplication.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "eventbuddy_prefs";
    //private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_TOKEN = "authToken";

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }
}
