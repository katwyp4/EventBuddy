package com.example.myapplication.network;

import com.example.myapplication.model.Event;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.model.PaginatedResponse;
import com.example.myapplication.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("/auth/register")
    Call<RegisterResponse> registerUser(
            @Field("email")     String email,
            @Field("password")  String password,
            @Field("firstName") String firstName,
            @Field("lastName")  String lastName
    );

    @FormUrlEncoded
    @POST("/auth/login")
    Call<LoginResponse> loginUser(
            @Field("email")    String email,
            @Field("password") String password
    );

    @GET("api/events")
    Call<PaginatedResponse<Event>> getEvents(@Query("page") int page, @Query("size") int size);

    @POST("api/events")
    Call<Event> createEvent(@Body Event event);


}
