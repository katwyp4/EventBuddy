package com.example.myapplication.network;

import com.example.myapplication.data.ExpenseDto;
import com.example.myapplication.model.Event;
import com.example.myapplication.model.LoginResponse;
import com.example.myapplication.model.PaginatedResponse;
import com.example.myapplication.model.PollOption;
import com.example.myapplication.model.RegisterResponse;


import com.example.myapplication.data.CreateMessageDto;
import com.example.myapplication.data.MessageDto;
import com.example.myapplication.data.CreateExpenseDto;



import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @GET("/api/events/{id}")
    Call<Event> getEvent(@Path("id") Long eventId);

    @Multipart
    @POST("/api/events/with-image")
    Call<Event> createEventWithImage(
            @Part MultipartBody.Part image,
            @Part("event") RequestBody eventJson
    );

    @GET("api/events/{eventId}/datePollOptions")
    Call<List<PollOption>> getDatePollOptions(@Path("eventId") Long eventId);

    @GET("api/events/{eventId}/locationPollOptions")
    Call<List<PollOption>> getLocationPollOptions(@Path("eventId") Long eventId);

    @POST("api/polls/{pollId}/options/{optionId}/vote")
    Call<Void> vote(@Path("pollId") Long pollId, @Path("optionId") Long optionId);




    @GET("/api/messages")
    Call<List<MessageDto>> getMessages(@Query("eventId") long eventId);

    @GET("/api/messages/latest")
    Call<List<MessageDto>> getLatest(@Query("eventId") long eventId,
                                     @Query("after")  String afterIso);

    @POST("/api/messages")
    Call<MessageDto> sendMessage(@Body CreateMessageDto body);

    @GET("/api/expenses/event/{eventId}")
    Call<List<ExpenseDto>> getExpensesForEvent(@Path("eventId") Long eventId);

    @GET("/api/expenses/{eventId}/balances")
    Call<Map<String, Double>> getSettlement(@Path("eventId") Long eventId);

    @POST("/api/expenses")
    Call<ExpenseDto> addExpense(@Body CreateExpenseDto dto);

}
