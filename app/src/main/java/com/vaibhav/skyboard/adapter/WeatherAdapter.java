package com.vaibhav.skyboard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.vaibhav.skyboard.R;
import com.vaibhav.skyboard.model.WeatherData;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private Context context;
    private List<WeatherData> weatherList;

    public WeatherAdapter(Context context, List<WeatherData> weatherList) {
        this.context = context;
        this.weatherList = weatherList;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weather_card, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        WeatherData data = weatherList.get(position);

        holder.locationText.setText(data.getLocation());
        holder.tempText.setText(data.getTemperature() + " (Feels like " + data.getFeelsLike() + ")");
        holder.conditionText.setText(data.getCondition());
        holder.humidityWindText.setText("Humidity: " + data.getHumidity() + "   Wind: " + data.getWindSpeed());
        holder.sunriseSunsetText.setText("Sunrise: " + data.getSunrise() + "  Sunset: " + data.getSunset());
        holder.timestampText.setText(data.getTimestamp());
        holder.weatherCardInnerLayout.setBackgroundResource(getBackgroundResourceForCondition(data.getCondition()));

        Glide.with(context)
                .load(data.getIconUrl())
                .into(holder.weatherIcon);
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView locationText, tempText, conditionText, humidityWindText, sunriseSunsetText, timestampText;
        ImageView weatherIcon;
        CardView weatherCard;
        ConstraintLayout weatherCardInnerLayout;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            locationText = itemView.findViewById(R.id.locationText);
            tempText = itemView.findViewById(R.id.tempText);
            conditionText = itemView.findViewById(R.id.conditionText);
            humidityWindText = itemView.findViewById(R.id.humidityWindText);
            sunriseSunsetText = itemView.findViewById(R.id.sunriseSunsetText);
            timestampText = itemView.findViewById(R.id.timestampText);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
            weatherCard = itemView.findViewById(R.id.weatherCard);
            weatherCardInnerLayout = itemView.findViewById(R.id.weatherCardInnerLayout);
        }
    }

    // For gradient weather:
    private int getBackgroundResourceForCondition(String condition) {
        condition = condition.toLowerCase();
        if (condition.contains("sun") || condition.contains("clear")) {
            return R.drawable.bg_gradient_sunny;
        } else if (condition.contains("cloud")) {
            return R.drawable.bg_gradient_cloudy;
        } else if (condition.contains("rain") || condition.contains("drizzle") || condition.contains("storm")) {
            return R.drawable.bg_gradient_rainy;
        } else {
            return R.drawable.bg_gradient_cloudy; // Fallback
        }
    }
}
