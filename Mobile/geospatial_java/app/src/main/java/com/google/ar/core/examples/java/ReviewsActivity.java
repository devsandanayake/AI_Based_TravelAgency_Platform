package com.google.ar.core.examples.java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.examples.java.geospatial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.ar.core.examples.java.ReviewsAdapter;


import java.util.ArrayList;
import java.util.List;

public class ReviewsActivity extends AppCompatActivity {

    private double latitude, longitude;
    private ListView reviewsListView;
    private TextView locationText, messageText, goodNewsText, badNewsText;
    private ImageView thumbsUpIcon, thumbsDownIcon;
    private LinearLayout reviewContainer;
    private DatabaseReference databaseReference;
    private List<String> reviewsList;
    private ReviewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reviewpage);

        // Initialize UI elements
        locationText = findViewById(R.id.locationText);
        messageText = findViewById(R.id.messageText); // Speech bubble text
        goodNewsText = findViewById(R.id.goodNewsText);
        badNewsText = findViewById(R.id.badNewsText);
        thumbsUpIcon = findViewById(R.id.thumbsUpIcon);
        thumbsDownIcon = findViewById(R.id.thumbsDownIcon);
        reviewContainer = findViewById(R.id.reviewContainer);
        reviewsListView = new ListView(this);
        reviewsList = new ArrayList<>();
        adapter = new ReviewsAdapter(this, reviewsList);
        reviewsListView.setAdapter(adapter);
        reviewContainer.addView(reviewsListView);

        // Get latitude & longitude from intent
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);

        Log.d("ReviewsActivity", "Received Lat: " + latitude + ", Lon: " + longitude);

        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Invalid location data received", Toast.LENGTH_SHORT).show();
            return;
        }

        // Display the location
        locationText.setText("Scam Reports Near: " + latitude + ", " + longitude);

        // Fetch scam reviews from Firebase
        fetchReviews();
    }

    private void fetchReviews() {
        databaseReference = FirebaseDatabase.getInstance().getReference("ScamArea");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviewsList.clear();
                boolean found = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double lat = snapshot.child("latitude").getValue(Double.class);
                    Double lon = snapshot.child("longitude").getValue(Double.class);
                    String message = snapshot.child("message").getValue(String.class);
                    String location = snapshot.child("scamLocation").getValue(String.class);
                    String weather = snapshot.child("weather").getValue(String.class);
                    String season = snapshot.child("season").getValue(String.class);

                    Boolean goodNews = snapshot.child("goodNews").getValue(Boolean.class);
                    Boolean badNews = snapshot.child("badNews").getValue(Boolean.class);

                    if (lat != null && lon != null && isNearby(lat, lon)) {
                        found = true;

                        if (message != null) {
                            messageText.setText(message);
                        }

                        goodNewsText.setText(goodNews + "%");
                        badNewsText.setText(badNews + "%");

                        if (message != null) {
                            reviewsList.add(message);
                        }
                    }
                }

                if (!found) {
                    messageText.setText("No scam reports found for this location.");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching reviews: " + databaseError.getMessage());
                Toast.makeText(ReviewsActivity.this, "Failed to fetch reviews", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isNearby(double scamLat, double scamLon) {
        double distance = calculateDistance(latitude, longitude, scamLat, scamLon);
        return distance <= 100; // Check if the scam location is within 100 meters
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Convert to meters
    }
}
