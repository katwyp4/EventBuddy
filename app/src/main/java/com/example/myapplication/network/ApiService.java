package com.example.myapplication.network;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/auth/register")
    Call<String> registerUser(
            @Query("username") String username,
            @Query("password") String password,
            @Query("firstName") String firstName,
            @Query("lastName") String lastName
    );

    @POST("/auth/login")
    Call<String> loginUser(
            @Query("username") String username,
            @Query("password") String password
    );

}
