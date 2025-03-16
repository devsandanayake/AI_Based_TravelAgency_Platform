package com.example.traveling;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ReportScamAreaActivity extends AppCompatActivity implements LocationListener {

    private EditText reporterName, scamLocation, message;
    private static final int LOCATION_REQUEST_CODE = 100;
    private Button reportButton;
    private ProgressBar locationProgressBar;
    private DatabaseReference databaseReference;
    private LocationManager locationManager;
    private double latitude, longitude; // Add latitude and longitude fields
    ImageView next;
    private RadioGroup radioGroup;
    private RadioButton radioGood, radioBad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_scam_area);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("ScamArea");

        // UI Elements
        scamLocation = findViewById(R.id.editTextTextPostalAddress);
        reporterName = findViewById(R.id.editTextText);
        message = findViewById(R.id.editTextTextMultiLine);
        reportButton = findViewById(R.id.capture_image);
        ImageView locationButton = findViewById(R.id.location);
        locationProgressBar = findViewById(R.id.locationProgressBar);

        // Initialize RadioGroup and RadioButtons
        radioGroup = findViewById(R.id.radioGroup);
        radioGood = findViewById(R.id.radio_good);
        radioBad = findViewById(R.id.radio_bad);

        // Set click listener for location button
        locationButton.setOnClickListener(v -> getLocation());

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(ReportScamAreaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportScamAreaActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        // Handle report submission
        reportButton.setOnClickListener(v -> saveScamDetails());

        next = findViewById(R.id.next);
        next.setOnClickListener(v -> homeActivity());
    }

    private void saveScamDetails() {
        String name = reporterName.getText().toString();
        String location = scamLocation.getText().toString();
        String scamMessage = message.getText().toString();

        if (location.isEmpty() || scamMessage.isEmpty()) {
            Snackbar.make(reportButton, "Location and message are required", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Capture selected radio button value
        int goodNews = 0, badNews = 0;
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_good) {
            goodNews = 1; // Good news selected
        } else if (selectedId == R.id.radio_bad) {
            badNews = 1; // Bad news selected
        }

        // Create a unique ID for the scam report
        String id = databaseReference.push().getKey();

        // Save latitude and longitude along with the report details
        ScamReport scamReport = new ScamReport(name, location, scamMessage, goodNews, badNews, latitude, longitude);

        // Save the report in Firebase
        databaseReference.child(id).setValue(scamReport)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(reportButton, "Submitted successfully", Snackbar.LENGTH_SHORT).show();
                    // Trigger the GET request
                    makeGetRequest();
                })
                .addOnFailureListener(e -> Snackbar.make(reportButton, "Failed to submit report", Snackbar.LENGTH_SHORT).show());
    }

    private void makeGetRequest() {
        new Thread(() -> {
            try {
                // URL of the API
                URL url = new URL("https://us-central1-custom-repeater-446305-a4.cloudfunctions.net/scamalerts2");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Configure the request
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // Set timeout (optional)
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();

                    // Log or use the response as needed
                    runOnUiThread(() -> Snackbar.make(reportButton, "GET request successful", Snackbar.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Snackbar.make(reportButton, "GET request failed", Snackbar.LENGTH_SHORT).show());
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Snackbar.make(reportButton, "GET request error", Snackbar.LENGTH_SHORT).show());
            }
        }).start();
    }

    public static class ScamReport {
        public String reporterName;
        public String scamLocation;
        public String message;
        public int goodNews;
        public int badNews;
        public double latitude;
        public double longitude; // Add fields for latitude and longitude

        public ScamReport() {
        }

        public ScamReport(String reporterName, String scamLocation, String message, int goodNews, int badNews, double latitude, double longitude) {
            this.reporterName = reporterName;
            this.scamLocation = scamLocation;
            this.message = message;
            this.goodNews = goodNews;
            this.badNews = badNews;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationProgressBar.setVisibility(View.VISIBLE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        } catch (Exception e) {
            e.printStackTrace();
            locationProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                scamLocation.setText(address);
                // Capture latitude and longitude
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            locationProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // You can handle this if needed
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Handle if GPS is disabled
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Snackbar.make(reportButton, "Location permission denied", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public void homeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
