package com.google.ar.core.examples.java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.examples.java.geospatial.R;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AboutAreaActivity extends AppCompatActivity {
    private Spinner spinnerArea, spinnerSeason, spinnerAlertType;
    private EditText weatherText;
    private ImageView weatherImage;
    private TextView responseText;
    ProgressBar bar;
    ImageView next;
    private Button submitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_area);
        // Initialize Views
        spinnerArea = findViewById(R.id.spinnerArea);
        spinnerSeason = findViewById(R.id.spinnerSeason);
        spinnerAlertType = findViewById(R.id.spinnerAlertType);
        weatherText = findViewById(R.id.weathertext);
        weatherImage = findViewById(R.id.weather);
        responseText = findViewById(R.id.response);
        submitButton = findViewById(R.id.submit);
        bar = findViewById(R.id.progressBar);

        // Fetch current weather on weather image click
        weatherImage.setOnClickListener(v -> fetchWeatherCondition());

        // Submit data to URL
        submitButton.setOnClickListener(v -> submitData());
        next=findViewById((R.id.next));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeActivity();
            }
        });

// Setup spinner adapter for spinnerArea
        ArrayAdapter<CharSequence> areaAdapter = ArrayAdapter.createFromResource(
                this, R.array.area_list, R.layout.spinner_item);
        areaAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerArea.setAdapter(areaAdapter);

// Setup spinner adapter for spinnerSeason
        ArrayAdapter<CharSequence> seasonAdapter = ArrayAdapter.createFromResource(
                this, R.array.season_list, R.layout.spinner_item);
        seasonAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerSeason.setAdapter(seasonAdapter);

// Setup spinner adapter for spinnerAlertType
        ArrayAdapter<CharSequence> alertTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.alert_type_list, R.layout.spinner_item);
        alertTypeAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerAlertType.setAdapter(alertTypeAdapter);


    }
//    private void fetchWeatherCondition() {
//
//        String currentWeather = "Sunny";
//        weatherText.setText(currentWeather);
//        Toast.makeText(this, "Current weather set to: " + currentWeather, Toast.LENGTH_SHORT).show();
//    }
private void fetchWeatherCondition() {
    // Show progress bar
    bar.setVisibility(View.VISIBLE);
    String apiKey = "c2127d87f9931fcb548c5f8da022c99e";
    String city = "Colombo";

    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";

    // Fetch weather in background thread
    new FetchWeatherTask().execute(url);
}

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String response = null;
            try {
                // Make HTTP request to OpenWeatherMap API
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());

                int data = reader.read();
                StringBuilder responseData = new StringBuilder();
                while (data != -1) {
                    responseData.append((char) data);
                    data = reader.read();
                }

                // Parse the weather data from the response
                JSONObject jsonResponse = new JSONObject(responseData.toString());
                JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
                String description = weather.getString("description");

                // Return "Sunny" or "Rainy" based on the weather description
                if (description.contains("rain")) {
                    response = "Rainy";
                } else {
                    response = "Sunny";
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "Sunny"; // Default to "Sunny" in case of error
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Hide progress bar
            bar.setVisibility(View.GONE);

            // Set the weather text
            weatherText.setText(result);
            Toast.makeText(AboutAreaActivity.this, "Current weather set to: " + result, Toast.LENGTH_SHORT).show();
        }
    }

    private void submitData() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE)); // Show ProgressBar

        try {
            String area = spinnerArea.getSelectedItem().toString();
            String season = spinnerSeason.getSelectedItem().toString();
            String weather = weatherText.getText().toString();
            String alertType = spinnerAlertType.getSelectedItem().toString();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("area", area);
            jsonObject.put("season", season);
            jsonObject.put("weather", weather);
            jsonObject.put("alert type", alertType);

            new Thread(() -> {
                try {
                    URL url = new URL("https://us-central1-custom-repeater-446305-a4.cloudfunctions.net/scamalerts1");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // Write JSON data to request body
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.close();

                    // Read response
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    // Extract the "prediction" value from the JSON response
                    JSONObject responseJson = new JSONObject(response.toString());
                    String result = responseJson.optString("prediction", "No result found.");

                    // Update response on UI thread
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar
                        responseText.setText(result);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE); // Hide ProgressBar
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    public void homeActivity() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }
}