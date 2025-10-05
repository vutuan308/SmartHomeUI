package com.example.smarthomeui.smarthome.ai_speech_reg;

import androidx.annotation.MainThread;


import com.example.smarthomeui.smarthome.ai_speech_reg.ControlRequest;
import com.example.smarthomeui.smarthome.ai_speech_reg.DeviceApi;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeviceController {
    private final DeviceApi api;


    public interface UiCallback { @MainThread void done(boolean ok); }


    public DeviceController(DeviceApi api) { this.api = api; }


    public void turnOn(int deviceId, UiCallback cb) {
        call(api.control(deviceId, new ControlRequest("turn_on")), cb);
    }


    public void turnOff(int deviceId, UiCallback cb) {
        call(api.control(deviceId, new ControlRequest("turn_off")), cb);
    }


    public void setBrightness(int deviceId, int value, UiCallback cb) {
        call(api.control(deviceId, new ControlRequest("set_brightness", clamp(value,0,100))), cb);
    }


    private void call(Call<Void> c, UiCallback cb) {
        c.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) { cb.done(resp.isSuccessful()); }
            @Override public void onFailure(Call<Void> call, Throwable t) { cb.done(false); }
        });
    }


    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}
