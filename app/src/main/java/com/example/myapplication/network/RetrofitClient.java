package com.example.myapplication.network;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getInstance(Context ctx) {
        if (retrofit == null) {
            SharedPreferences sp = ctx.getSharedPreferences("eventbuddy_prefs",
                    Context.MODE_PRIVATE);

            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient ok = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String jwt = sp.getString("authToken", null);   // <-- identyczny klucz
                        Request req = chain.request();
                        if (jwt != null) {
                            req = req.newBuilder()
                                    .header("Authorization", "Bearer " + jwt)
                                    .build();
                        }
                        Response rsp = chain.proceed(req);

                        if (rsp.code() == 401) {        // jeden punkt wyjścia
                            sp.edit().remove("authToken").apply();
                            // TODO: broadcast lub eventBus do wylogowania
                        }
                        return rsp;
                    })
                    .addInterceptor(log)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/")
                    .client(ok)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

