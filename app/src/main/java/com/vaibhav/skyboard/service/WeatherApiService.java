package com.vaibhav.skyboard.service;

import com.vaibhav.skyboard.model.WeatherApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("api/weather")
    Call<WeatherApiResponse> getWeatherByCity(@Query("city") String cityName);

}
