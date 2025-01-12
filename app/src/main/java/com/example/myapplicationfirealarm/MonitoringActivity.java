package com.example.myapplicationfirealarm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MonitoringActivity extends AppCompatActivity {

    private TextView temperatureTextView, smokeTextView, nitrogenTextView, sulphurTextView;
    private Handler handler;
    private Runnable updateSensorData;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String AIR_QUALITY_API_URL = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=52.52&longitude=13.41&current=european_aqi,us_aqi,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide&hourly=pm10,pm2_5";
    private static final String TEMPERATURE_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m&hourly=temperature_2m&timezone=Europe%2FLondon";

    // Threshold Values
    private static final int TEMPERATURE_THRESHOLD = 45; // degrees Celsius
    private static final double CO_THRESHOLD = 10.0;     // μg/m³
    private static final double NO2_THRESHOLD = 5.0;     // μg/m³
    private static final double SO2_THRESHOLD = 1.0;     // μg/m³

    private boolean alertSent = false; // To prevent sending multiple alerts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitoring_activity);

        temperatureTextView = findViewById(R.id.temperatureTextView);
        smokeTextView = findViewById(R.id.smokeTextView);
        nitrogenTextView = findViewById(R.id.nitrogenTextView);
        sulphurTextView = findViewById(R.id.sulphurTextView);

        handler = new Handler();

        // Fetch sensor data periodically
        updateSensorData = new Runnable() {
            @Override
            public void run() {
                System.out.println("Fetching sensor data");
                fetchAirQualityData();
                fetchTemperatureData();
//                fetchSensorData();
                handler.postDelayed(this, 2000);
            }
        };

        handler.post(updateSensorData);
    }

//    private void fetchSensorData() {
//        temperatureTextView.setText("Temperature: " + 50 + "°C");
//        smokeTextView.setText("CO Level: " + 1000 + " μg/m³");
//        nitrogenTextView.setText("Nitrogen Dioxide: " + 50 + " μg/m³");
//        sulphurTextView.setText("Sulphur Dioxide: " + 45 + " μg/m³");
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, AIR_QUALITY_API_URL, null,
//                response -> {
//                    try {
//                        JSONObject current = response.getJSONObject("current");
//
//                        double coValue = current.getDouble("carbon_monoxide");
//                        double no2Value = current.getDouble("nitrogen_dioxide");
//                        double so2Value = current.getDouble("sulphur_dioxide");
//
//                        int temperature = 45; // Static value for testing, replace with actual sensor reading
//                        runOnUiThread(() -> {
//                            temperatureTextView.setText("Temperature: " + temperature + "°C");
//                            smokeTextView.setText("CO Level: " + coValue + " μg/m³");
//                            nitrogenTextView.setText("Nitrogen Dioxide: " + no2Value + " μg/m³");
//                            sulphurTextView.setText("Sulphur Dioxide: " + so2Value + " μg/m³");
//                        });
//
//                        // Check conditions and send alert if needed
//                        checkAndSendAlert(temperature, coValue, no2Value, so2Value);
//
//                    } catch (Exception e) {
//                        Log.e("API_ERROR", "Error parsing data", e);
//                        temperatureTextView.setText("Temperature: " + 50 + "°C");
//                    }
//                },
//                error -> Log.e("API_ERROR", "Network error: " + error.toString()));
//        temperatureTextView.setText("Temperature: " + 50 + "°C");
//
//        queue.add(request);
//    }

    private void fetchAirQualityData() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, AIR_QUALITY_API_URL, null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current");

                        double coValue = current.getDouble("carbon_monoxide");
                        double no2Value = current.getDouble("nitrogen_dioxide");
                        double so2Value = current.getDouble("sulphur_dioxide");

                        runOnUiThread(() -> {
                            smokeTextView.setText("CO Level: " + coValue + " μg/m³");
                            nitrogenTextView.setText("Nitrogen Dioxide: " + no2Value + " μg/m³");
                            sulphurTextView.setText("Sulphur Dioxide: " + so2Value + " μg/m³");
                        });

                        checkAndSendAlert(0, coValue, no2Value, so2Value);

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error parsing air quality data", e);
                    }
                },
                error -> Log.e("API_ERROR", "Network error: " + error.toString()));

        queue.add(request);
    }

    private void fetchTemperatureData() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, TEMPERATURE_API_URL, null,
                response -> {
                    try {

                        JSONObject current = response.getJSONObject("current");
                        System.out.println("current" + current);
                        double temperature = (double) (current.get("temperature_2m"));
                        Log.i("temperature", "temp: "+ temperature);
                        System.out.println("temperature" + temperature);

                        runOnUiThread(() -> {
                            temperatureTextView.setText("Temperature: " + temperature + "°C");
                        });

                        checkAndSendAlert(temperature, 0.0, 0.0, 0.0);

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error parsing temperature data", e);
                    }
                },
                error -> Log.e("API_ERROR", "Network error: " + error.toString()));

        queue.add(request);
    }

    private void checkAndSendAlert(double temperature, double coValue, double no2Value, double so2Value) {
        if (!alertSent && (temperature >= TEMPERATURE_THRESHOLD ||
                coValue >= CO_THRESHOLD ||
                no2Value >= NO2_THRESHOLD ||
                so2Value >= SO2_THRESHOLD)) {

            // Check permission before sending SMS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
            } else {
                sendFireAlert();
            }
        }
    }

    private void sendFireAlert() {
        String message = "Fire Alert: Elevated sensor values detected. Immediate action is required! send automatically!";
        String[] emergencyContacts = {
                "+1234567890", // Fire Department
                "+0987654321", // Police Department
                "+1122334455"  // User Contact
        };

        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (String contact : emergencyContacts) {
                smsManager.sendTextMessage(contact, null, message, null, null);
            }
            Log.d("ALERT", "Emergency alerts sent successfully!");
            alertSent = true; // Prevent multiple alert
        } catch (Exception e) {
            Log.e("ALERT", "Failed to send alert: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendFireAlert();
            } else {
                Log.e("ALERT", "Permission denied to send SMS.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop updates when activity is destroyed
        handler.removeCallbacks(updateSensorData);
    }
}
