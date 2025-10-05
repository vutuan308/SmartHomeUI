package com.example.smarthomeui.smarthome.ai_speech_reg;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface DeviceApi {
    @POST("api/device/{id}/control")
    Call<Void> control(@Path("id") int deviceId, @Body ControlRequest body);
}