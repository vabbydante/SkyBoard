package com.vaibhav.skyboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vaibhav.skyboard.adapter.WeatherAdapter;
import com.vaibhav.skyboard.client.RetrofitClient;
import com.vaibhav.skyboard.helper.WeatherCacheHelper;
import com.vaibhav.skyboard.model.WeatherApiResponse;
import com.vaibhav.skyboard.model.WeatherData;
import com.vaibhav.skyboard.service.WeatherApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView weatherRecyclerView;
    private WeatherAdapter weatherAdapter;
    private List<WeatherData> weatherList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int refreshInterval;
    private boolean isFirstLaunch = true;
    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;
    private boolean isAutoRefreshPaused = false;
    private Menu menu; // Store reference to menu



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherRecyclerView = findViewById(R.id.weatherRecyclerView);
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weatherList = WeatherCacheHelper.loadWeatherList(this);
        weatherAdapter = new WeatherAdapter(this, weatherList);
        weatherRecyclerView.setAdapter(weatherAdapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            SharedPreferences refreshPrefs = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
            String savedCityPref = refreshPrefs.getString("city", "Chandrapura");
            refreshInterval = refreshPrefs.getInt("interval", 5);
            fetchWeatherData(savedCityPref);
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                weatherList.remove(position);
                weatherAdapter.notifyItemRemoved(position);
                WeatherCacheHelper.saveWeatherList(MainActivity.this, weatherList);
                Toast.makeText(MainActivity.this, "Card removed. Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(weatherRecyclerView);
    }

    private void fetchWeatherData(String cityName) {
        WeatherApiService apiService = RetrofitClient.getRetrofitInstance(MainActivity.this).create(WeatherApiService.class);
        Call<WeatherApiResponse> call = apiService.getWeatherByCity(cityName);

        call.enqueue(new Callback<WeatherApiResponse>() {
            @Override
            public void onResponse(Call<WeatherApiResponse> call, Response<WeatherApiResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    WeatherApiResponse apiResponse = response.body();

                    WeatherData newCard = new WeatherData(
                            apiResponse.location,
                            apiResponse.temperature,
                            apiResponse.feelsLike,
                            apiResponse.condition,
                            apiResponse.humidity,
                            apiResponse.sunrise,
                            apiResponse.sunset,
                            apiResponse.windSpeed,
                            apiResponse.timestamp,
                            apiResponse.iconUrl
                    );

                    weatherList.add(0, newCard);  // Newest card on top
                    weatherAdapter.notifyItemInserted(0);
                    WeatherCacheHelper.saveWeatherList(MainActivity.this, weatherList);
                    weatherRecyclerView.scrollToPosition(0);
                } else {
                    Toast.makeText(MainActivity.this, "Error while fetching Response for: " + cityName + " Check city name!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherApiResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to load weather data. " +t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // For Settings Icon functionality:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        SharedPreferences preferences = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
        boolean isPaused = preferences.getBoolean("isPaused", false);

        if (isPaused) {
            menu.findItem(R.id.action_toggle_refresh).setIcon(R.drawable.ic_play);
            refreshHandler.removeCallbacks(refreshRunnable);
        } else {
            menu.findItem(R.id.action_toggle_refresh).setIcon(R.drawable.ic_pause_foreground);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_toggle_refresh) {
            toggleAutoRefresh();
            return true;
        } else if (item.getItemId() == R.id.action_clear_all) {
            Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
            weatherRecyclerView.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    weatherList.clear();
                    weatherAdapter.notifyDataSetChanged();
                    WeatherCacheHelper.saveWeatherList(MainActivity.this, weatherList);
                    Toast.makeText(MainActivity.this, "All weather cards cleared.", Toast.LENGTH_SHORT).show();
                    weatherRecyclerView.setAlpha(1.0f); // Reset just in case
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
        String savedCity = preferences.getString("city", "Chandrapura");
        refreshInterval = preferences.getInt("interval", 5);
        boolean isPaused = preferences.getBoolean("isPaused", false);
        fetchWeatherData(savedCity);
        if (!isPaused) {
            refreshRunnable = new Runnable() {
                @Override
                public void run() {
                    SharedPreferences preferences = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
                    String savedCity = preferences.getString("city", "Chandrapura");
                    refreshInterval = preferences.getInt("interval", 5);
                    fetchWeatherData(savedCity);
                    refreshHandler.postDelayed(this, refreshInterval * 60 * 1000);
                }
            };
            refreshHandler.postDelayed(refreshRunnable, refreshInterval * 60 * 1000);
        }
        if (refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }

//        refreshRunnable = new Runnable() {
//            @Override
//            public void run() {
//                SharedPreferences preferences = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
//                String savedCity = preferences.getString("city", "Chandrapura");
//                refreshInterval = preferences.getInt("interval", 5);
//                fetchWeatherData(savedCity);
//                refreshHandler.postDelayed(this, (long) refreshInterval * 60 * 1000); // interval in milliseconds
//            }
//        };

        // Start first delayed run
        //refreshHandler.postDelayed(refreshRunnable, (long) refreshInterval * 60 * 1000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void toggleAutoRefresh() {
        SharedPreferences preferences = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        /*if (isAutoRefreshPaused) {
            // Resume
            refreshHandler.postDelayed(refreshRunnable, refreshInterval * 60 * 1000);
            isAutoRefreshPaused = false;
            menu.findItem(R.id.action_toggle_refresh).setIcon(R.drawable.ic_pause_foreground);
            Toast.makeText(this, "Auto-Refresh Resumed", Toast.LENGTH_SHORT).show();
        } else {
            // Pause
            refreshHandler.removeCallbacks(refreshRunnable);
            isAutoRefreshPaused = true;
            menu.findItem(R.id.action_toggle_refresh).setIcon(R.drawable.ic_play);
            Toast.makeText(this, "Auto-Refresh Paused", Toast.LENGTH_SHORT).show();
        }
        editor.putBoolean("isPaused", isAutoRefreshPaused);
        editor.apply();*/

        // new code
        // get isPaused value from SharedPreferences:
        boolean isPaused = false; // initializing isPaused as 'false' by default...
        isPaused = preferences.getBoolean("isPaused", false);
        if(isPaused) {
            refreshHandler.removeCallbacks(refreshRunnable);
            isPaused = false;
            menu.findItem(R.id.action_toggle_refresh).setIcon(R.drawable.ic_play);
            Toast.makeText(this, "Auto-Refresh Paused", Toast.LENGTH_SHORT).show();
        } else {
            refreshHandler.postDelayed(refreshRunnable, refreshInterval * 60 * 1000);
            isPaused = true;
            menu.findItem(R.id.action_toggle_refresh).setIcon(R.drawable.ic_pause_foreground);
            Toast.makeText(this, "Auto-Refresh Resumed", Toast.LENGTH_SHORT).show();
        }
        editor.putBoolean("isPaused", isPaused);
        editor.apply();
    }
}
