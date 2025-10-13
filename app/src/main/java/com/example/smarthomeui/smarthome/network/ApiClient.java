package com.example.smarthomeui.smarthome.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // https://coppiced-unintimated-lottie.ngrok-free.dev/
    private static final String BASE_URL = "https://coppiced-unintimated-lottie.ngrok-free.dev/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
