package com.example.myapplication.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myapplication.MyApp;                 // globalny kontekst
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Zapisuje FCM-token lokalnie i wysyła go do backendu. */
public final class TokenRepository {

    private static final String PREFS_NAME = "eventbuddy_prefs";
    private static final String KEY_TOKEN  = "fcm_token";

    private TokenRepository() { }

    public static void register(String token) {
        Context ctx = MyApp.getContext();
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
        Log.d("FCM", "Token zapisany lokalnie: " + token);

        ApiService api = RetrofitClient.getInstance(ctx).create(ApiService.class);
        api.sendFcmToken(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c, Response<Void> r) {
                Log.d("FCM", "Token FCM wysłany do backendu: " + token);
            }

            @Override
            public void onFailure(Call<Void> c, Throwable t) {
                Log.e("FCM", "Błąd wysyłania tokenu FCM do backendu", t);
            }
        });
    }
}
