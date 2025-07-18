package com.vaibhav.skyboard.client;

import android.content.Context;
import android.content.SharedPreferences;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.29.57:8084/"; // Your Pi IP and port
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("SkyboardPrefs", Context.MODE_PRIVATE);
        String savedIp = preferences.getString("pi_ip", "192.168.29.57"); // Default fallback
        String baseUrl = "http://" + savedIp + ":8084/";

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}
