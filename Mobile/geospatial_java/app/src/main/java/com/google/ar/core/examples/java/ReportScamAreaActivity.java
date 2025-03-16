package com.google.ar.core.examples.java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.examples.java.geospatial.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReportScamAreaActivity extends AppCompatActivity implements LocationListener {
    private EditText scamLocation, message,warningEditText;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int IMAGE_REQUEST_CODE = 200;
    private Button reportButton;
    private ProgressBar locationProgressBar;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private LocationManager locationManager;
    private double latitude, longitude; // Add latitude and longitude fields
    ImageView next,uploadImageView;
    private RadioGroup radioGroup;
    private RadioButton radioGood, radioBad;
    private TextView weatherTextView,textViewAlertType,warningTextView;
    private Spinner seasonSpinner, alertTypeSpinner;
    private String weatherCondition;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_scam_area);
        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("ScamArea");
        storageReference = FirebaseStorage.getInstance().getReference("ScamAreaImages");

        // UI Elements
        weatherTextView = findViewById(R.id.weathertext);
        textViewAlertType= findViewById(R.id.textViewAlertType);
        seasonSpinner = findViewById(R.id.spinnerSeason);
        alertTypeSpinner = findViewById(R.id.spinnerAlertType);
        scamLocation = findViewById(R.id.editTextTextPostalAddress);
//        reporterName = findViewById(R.id.editTextText);
        message = findViewById(R.id.editTextTextMultiLine);
        warningEditText = findViewById(R.id.editTextWarning);
        reportButton = findViewById(R.id.capture_image);
        ImageView locationButton = findViewById(R.id.location);
        locationProgressBar = findViewById(R.id.locationProgressBar);
        uploadImageView = findViewById(R.id.imageView);

        // Initialize RadioGroup and RadioButtons
        radioGroup = findViewById(R.id.radioGroup);
        radioGood = findViewById(R.id.radio_good);
        radioBad = findViewById(R.id.radio_bad);

// Setup spinner adapter for spinnerSeason
        ArrayAdapter<CharSequence> seasonAdapter = ArrayAdapter.createFromResource(
                this, R.array.season_list, R.layout.spinner_item);
        seasonAdapter.setDropDownViewResource(R.layout.spinner_item);
        seasonSpinner.setAdapter(seasonAdapter);

// Setup spinner adapter for spinnerAlertType
        ArrayAdapter<CharSequence> alertTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.alert_type_list, R.layout.spinner_item);
        alertTypeAdapter.setDropDownViewResource(R.layout.spinner_item);
        alertTypeSpinner.setAdapter(alertTypeAdapter);
        findViewById(R.id.weather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchWeather();
            }
        });

        // Set click listener for location button
        locationButton.setOnClickListener(v -> getLocation());

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(ReportScamAreaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportScamAreaActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        // Add listener to RadioGroup
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_good) {
                alertTypeSpinner.setVisibility(View.GONE); // Hide alert type spinner
                textViewAlertType.setVisibility(View.GONE);
            } else {
                alertTypeSpinner.setVisibility(View.VISIBLE); // Show alert type spinner
                textViewAlertType.setVisibility(View.VISIBLE);
            }
        });
        // Handle report submission
        reportButton.setOnClickListener(v -> saveScamDetails());

        Button buttonUploadImage = findViewById(R.id.buttonUploadImage);
        buttonUploadImage.setOnClickListener(v -> openImagePicker());

        next = findViewById(R.id.next);
        next.setOnClickListener(v -> homeActivity());
    }

    private void openImagePicker() {
        // Open image picker to allow user to select an image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                uploadImageView.setImageURI(imageUri); // Show the selected image in ImageView
            }
        }
    }
    private void saveScamDetails() {
        locationProgressBar.setVisibility(View.VISIBLE);
//        String name = reporterName.getText().toString();
        String season = seasonSpinner.getSelectedItem().toString();
        String alertType = alertTypeSpinner.getSelectedItem() != null ? alertTypeSpinner.getSelectedItem().toString() : "";
        String location = scamLocation.getText().toString();
        String scamMessage = message.getText().toString();
        String warningMessage = warningEditText.getText().toString();

        if (location.isEmpty() || scamMessage.isEmpty()) {
            Snackbar.make(reportButton, "Location and message are required", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Log.d("ScamReport", "Season: " + season);
        Log.d("ScamReport", "Alert Type: " + alertType);
        Log.d("ScamReport", "Location: " + location);
        Log.d("ScamReport", "Scam Message: " + scamMessage);
        Log.d("ScamReport", "Warning Message: " + warningMessage);
        Log.d("ScamReport", "Latitude: " + latitude);
        Log.d("ScamReport", "Longitude: " + longitude);
        Log.d("ScamReport", "Weather: " + weatherCondition);
        Log.d("ScamReport", "Image URI: " + imageUri);
        Log.d("ScamReport", "Good News: " + radioGood.isChecked());
        Log.d("ScamReport", "Bad News: " + radioBad.isChecked());


        // Determine if "Good" is selected
        boolean isGoodNews = radioGroup.getCheckedRadioButtonId() == R.id.radio_good;

        // Set additional field for "Good" selection
        String message2 = isGoodNews ? "No" : "";

        // Create a unique ID for the scam report
        String id = databaseReference.push().getKey();
        Log.d("ScamReport", "ID: " + id);

        // Save latitude and longitude along with the report details
        ScamReport scamReport = new ScamReport(
                season, alertType, location, scamMessage, warningMessage, latitude, longitude, weatherCondition, "", isGoodNews, !isGoodNews

        );

        Log.d("ScamReport", "Scam Report: " + scamReport);

        if (imageUri != null) {
            StorageReference imageRef = storageReference.child(id + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get image URL after successful upload
                        String imageUrl = uri.toString();

                        // Add image URL to the scam report
                        scamReport.imageUrl = imageUrl;

                        Log.d("ScamReport", "Scam Report: " + scamReport);

                        // Save the report in Firebase
                        databaseReference.child(id).setValue(scamReport)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Data saved successfully");
                                    Snackbar.make(reportButton, "Submitted successfully", Snackbar.LENGTH_SHORT).show();
                                    locationProgressBar.setVisibility(View.GONE); // Hide ProgressBar on success
                                    Log.d("Firebase", "Data saved successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Snackbar.make(reportButton, "Failed to submit report", Snackbar.LENGTH_SHORT).show();
                                    locationProgressBar.setVisibility(View.GONE); // Hide ProgressBar on failure
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Snackbar.make(reportButton, "Failed to upload image", Snackbar.LENGTH_SHORT).show();
                        locationProgressBar.setVisibility(View.GONE); // Hide ProgressBar on failure
                    });
        } else {
            // No image selected, just save the report without image
            databaseReference.child(id).setValue(scamReport)
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(reportButton, "Submitted successfully", Snackbar.LENGTH_SHORT).show();
                        locationProgressBar.setVisibility(View.GONE); // Hide ProgressBar on success
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(reportButton, "Failed to submit report", Snackbar.LENGTH_SHORT).show();
                        locationProgressBar.setVisibility(View.GONE); // Hide ProgressBar on failure
                    });
        }
    }

    public static class ScamReport {
        public String season;
        public String alertType;
        public String location;
        public String scamMessage;
        public String warningMessage;
        public double latitude;
        public double longitude;
        public String weather;
        public String imageUrl;
        public boolean goodNews;
        public boolean badNews;
        public ScamReport() {
        }

        public ScamReport(String season, String alertType, String location, String scamMessage,
                          String warningMessage, double latitude, double longitude, String weather,
                          String imageUrl, boolean goodNews, boolean badNews) {
            this.season = season;
            this.alertType = alertType;
            this.location = location;
            this.scamMessage = scamMessage;
            this.warningMessage = warningMessage;
            this.latitude = latitude;
            this.longitude = longitude;
            this.weather = weather;
            this.imageUrl = imageUrl;
            this.goodNews = goodNews;
            this.badNews = badNews;
        }

        public ScamReport(String alertType, String season, String weatherCondition, String location, String scamMessage, int i, int i1, double latitude, double longitude, String message2, String warningMessage) {
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationProgressBar.setVisibility(View.VISIBLE);

        try {
            // Get last known location to avoid continuous updates
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                updateLocationUI(lastKnownLocation);
            } else {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            locationProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateLocationUI(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                scamLocation.setText(address);
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    private void fetchWeather() {
        OkHttpClient client = new OkHttpClient();
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=Colombo&appid=c2127d87f9931fcb548c5f8da022c99e";

        Request request = new Request.Builder().url(apiUrl).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
                        weatherCondition = weather.getString("description");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                weatherTextView.setText(weatherCondition);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}