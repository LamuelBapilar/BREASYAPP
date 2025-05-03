package com.example.breasyapp2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class experiment extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView textWeather;
    private final String Weather_KEY = "17561e030e960239b678acac686e1ab6"; // Replace with your OpenWeatherMap API key

    String useremail, userfname, userlname, userbday, useraddress, usergphone, usergname, Weather, City, AirQuality ;
    double Temperature, HeatIndex;
    int Humidity, AQI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experiment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textWeather = findViewById(R.id.textWeather);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetchLocation();
    }

    private void fetchLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                        fetchLocation();
                    }
                });
    }

    private void getWeather(double lat, double lon) {
        OkHttpClient client = new OkHttpClient();

        String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                "&lon=" + lon + "&units=metric&appid=" + Weather_KEY;

        String airUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat +
                "&lon=" + lon + "&appid=" + Weather_KEY;

        Request weatherRequest = new Request.Builder().url(weatherUrl).build();
        client.newCall(weatherRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(experiment.this, "❌ Weather fetch failed.", Toast.LENGTH_SHORT).show());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        String City = obj.getString("name");
                        JSONObject main = obj.getJSONObject("main");
                        Temperature = main.getDouble("temp");
                        HeatIndex = main.getDouble("feels_like");
                        Humidity = main.getInt("humidity");
                        Weather = obj.getJSONArray("weather")
                                .getJSONObject(0).getString("main");

                        // Fetch AQI data now
                        Request airRequest = new Request.Builder().url(airUrl).build();
                        client.newCall(airRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(() -> Toast.makeText(experiment.this, "🌡 Weather OK\n🌫 AQI fetch failed.", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(Call call, Response airResponse) throws IOException {
                                if (airResponse.isSuccessful()) {
                                    try {
                                        String airJson = airResponse.body().string();
                                        JSONObject airObj = new JSONObject(airJson);
                                        AQI = airObj.getJSONArray("list")
                                                .getJSONObject(0)
                                                .getJSONObject("main")
                                                .getInt("aqi");

                                        String aqiStatus;
                                        switch (AQI) {
                                            case 1:
                                                AirQuality = "Good ✅";
                                                break;
                                            case 2:
                                                AirQuality = "Fair 🙂";
                                                break;
                                            case 3:
                                                AirQuality = "Moderate 😐";
                                                break;
                                            case 4:
                                                AirQuality = "Poor 😷";
                                                break;
                                            case 5:
                                                AirQuality = "Very Poor 🚨";
                                                break;
                                            default:
                                                AirQuality = "Unknown";
                                        }

                                        String display = "📍 Location: " + City + "\n" +
                                                "🌡 Temp: " + Temperature + "°C\n" +
                                                "🥵 Feels Like: " + HeatIndex + "°C\n" +
                                                "💧 Humidity: " + Humidity + "%\n" +
                                                "🌤 Weather: " + Weather + "\n" +
                                                "🌫 Air Quality: " + AirQuality;

                                        runOnUiThread(() -> textWeather.setText(display));

                                    } catch (Exception e) {
                                        runOnUiThread(() -> Toast.makeText(experiment.this, "\n⚠️ AQI parse error.", Toast.LENGTH_SHORT).show());
                                    }
                                }
                            }
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(experiment.this, "⚠️ Weather parse error.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(experiment.this, "🚫 Weather API error.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}
