package com.example.smarthomeui.smarthome.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Api {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("/api/auth/profile")
    Call<ProfileResponse> getProfile(@Header("Authorization") String authToken);
}
