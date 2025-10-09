package com.example.smarthomeui.smarthome.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // NHỚ có dấu / cuối
    private static final String BASE_URL = "https://039dfec96ea6.ngrok-free.app/";

    private static Retrofit authedRetrofit;
    private static Retrofit noAuthRetrofit;

    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(com.example.smarthomeui.smarthome.network.HouseListWrap.class,
                        new com.example.smarthomeui.smarthome.network.HouseListWrap.Deserializer())
                .registerTypeAdapter(com.example.smarthomeui.smarthome.network.RoomsByHouseWrap.class,
                        new com.example.smarthomeui.smarthome.network.RoomsByHouseWrap.Deserializer())
                .registerTypeAdapter(com.example.smarthomeui.smarthome.network.RoomListWrap.class,
                        new com.example.smarthomeui.smarthome.network.RoomListWrap.Deserializer())
                .create();
    }

    /** Retrofit KHÔNG auth (login/register) */
    public static Retrofit getClientNoAuth() {
        if (noAuthRetrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().build();
            noAuthRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(buildGson()))
                    .client(client)
                    .build();
        }
        return noAuthRetrofit;
    }

    /** Retrofit CÓ auth: tự gắn Bearer token từ UserManager */
    public static Retrofit getClient(Context ctx) {
        if (authedRetrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor((Interceptor) chain -> {
                        Request req = chain.request();
                        String token = new com.example.smarthomeui.smarthome.utils.UserManager(
                                ctx.getApplicationContext()).getAccessToken();
                        if (token != null && !token.isEmpty()) {
                            req = req.newBuilder()
                                    .addHeader("Authorization",
                                            token.startsWith("Bearer ") ? token : ("Bearer " + token))
                                    .build();
                        }
                        return chain.proceed(req);
                    })
                    .build();

            authedRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(buildGson()))
                    .client(client)
                    .build();
        }
        return authedRetrofit;
    }
}
