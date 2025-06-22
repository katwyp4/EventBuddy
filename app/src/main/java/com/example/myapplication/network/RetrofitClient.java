package com.example.myapplication.network;

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

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = new TokenManager(context);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            clientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                String token = tokenManager.getToken();
                if (token != null) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();
                Response response = chain.proceed(request);

                // Sprawdź czy odpowiedź to 401 lub 403 - Unauthorized
                if (response.code() == 401) { //usunięto 403, bo powodowało błedy gdy próbował wyświetlić coś do czego nie był uprawniony, ale token jeszcze nie wygasł
                    Log.d("LOGOWANIE", "LOGOWANIE");
                    tokenManager.clearToken();

                    // Przekieruj użytkownika do LoginActivity
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        Intent intent = new Intent(context, com.example.myapplication.LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        Toast.makeText(context, "Sesja wygasła. Zaloguj się ponownie.", Toast.LENGTH_LONG).show();
                    });
                }

                return response;
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/") // lokalny backend
                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

