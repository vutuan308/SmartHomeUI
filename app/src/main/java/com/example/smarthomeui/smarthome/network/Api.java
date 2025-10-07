package com.example.smarthomeui.smarthome.network;

import retrofit2.Call;
import retrofit2.http.*;

public interface Api {
    // ===== AUTH / PROFILE (đã có) =====
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/api/auth/profile")
    Call<ProfileResponse> getProfile();
    @POST("/api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    // ===== HOUSE =====
    // Danh sách nhà theo user (có phân trang)
    @GET("/api/house")
    Call<HouseListWrap> getHouses(@Query("skip") int skip,
                                  @Query("take") int take);

    // Chi tiết 1 nhà
    @GET("/api/house/{id}")
    Call<HouseDto> getHouseById(@Path("id") int id);

    // Tạo nhà
    @POST("/api/house")
    Call<HouseDto> createHouse(@Body CreateHouseReq body);

    // Cập nhật nhà
    @PUT("/api/house/{id}")
    Call<HouseDto> updateHouse(@Path("id") int id,
                               @Body UpdateHouseReq body);

    // Xoá nhà
    @DELETE("/api/house/{id}")
    Call<Void> deleteHouse(@Path("id") int id);
    // GET Nha
    @GET("/api/room")
    Call<RoomsByHouseWrap> getRoomsGrouped(@Query("skip") int skip, @Query("take") int take);
}
