package com.example.smarthomeui.smarthome.ai_speech_reg;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthInterceptor implements Interceptor {
    private final String bearerToken; // dạng: "Bearer <token>"


    public AuthInterceptor(String bearerToken) {
        this.bearerToken = bearerToken;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request().newBuilder()
                .addHeader("Authorization", bearerToken)
                .addHeader("Content-Type", "application/json")
                .build();
        return chain.proceed(req);
    }
}
