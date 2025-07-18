package com.vaibhav.skyboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private EditText editCity, editInterval, editIpAddress;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editCity = findViewById(R.id.editCity);
        editInterval = findViewById(R.id.editInterval);
        editIpAddress = findViewById(R.id.editIpAddress);
        buttonSave = findViewById(R.id.buttonSave);

        // Load the previously saved values into the EditTexts:
        SharedPreferences prefs = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
        String savedCity = prefs.getString("city", "Chandrapura");
        int savedInterval = prefs.getInt("interval", 5);
        String savedPiIp = prefs.getString("pi_ip", "192.168.29.57");


        editCity.setText(savedCity);
        editInterval.setText(String.valueOf(savedInterval));
        editIpAddress.setText(savedPiIp);

        buttonSave.setOnClickListener(v -> {
            String city = editCity.getText().toString().trim();
            String interval = editInterval.getText().toString().trim();
            String ipAddress = editIpAddress.getText().toString();

            if (!city.isEmpty() && !interval.isEmpty() && !ipAddress.isEmpty()) {
                SharedPreferences preferences = getSharedPreferences("SkyboardPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("city", city);
                editor.putInt("interval", Integer.parseInt(interval));
                editor.putString("pi_ip", ipAddress);
                editor.apply();

                Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}