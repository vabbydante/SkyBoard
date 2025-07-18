package com.vaibhav.skyboard.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaibhav.skyboard.model.WeatherData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
public class WeatherCacheHelper {
    private static final String PREF_NAME = "SkyboardPrefs";
    private static final String KEY_CACHE = "WeatherCache";

    public static void saveWeatherList(Context context, List<WeatherData> weatherList) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(weatherList);
        editor.putString(KEY_CACHE, json);
        editor.apply();
    }

    public static List<WeatherData> loadWeatherList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CACHE, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<WeatherData>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }
}
