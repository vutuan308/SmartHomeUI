package com.example.smarthomeui.smarthome.ai_speech_reg;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiFactory {
    public static DeviceApi create(String baseUrl, String token) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);


        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor("Bearer " + token))
                .addInterceptor(log)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // ví dụ: "http://10.0.2.2:5149/"
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        return retrofit.create(DeviceApi.class);
    }
}
