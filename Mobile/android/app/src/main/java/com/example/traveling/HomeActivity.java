package com.example.traveling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements LocationListener {
    CardView card1,card2,card3,card4;
    ImageView profile,profileImage,notification;
    FirebaseStorage storage;
    StorageReference storageReference;
    TextView uname;
    FrameLayout fram1,frame2,fram3;
    LocationManager locationManager;
    String currentRoad, currentCity, currentCountry;
    boolean isNotificationVisible = false;
    double userLat;
    double userLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        fram1= findViewById(R.id.frame1);
        frame2= findViewById(R.id.frame2);
        fram3= findViewById(R.id.frame3);

        card1=findViewById((R.id.icon1));
        card2=findViewById((R.id.icon2));
        card3=findViewById((R.id.icon3));
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardresultActivity2();
            }
        });
        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardresultActivity1();
            }
        });
        card3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardActivity3();
            }
        });

        fram1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardActivity1();
            }
        });
        frame2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardActivity2();
            }
        });
        fram3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {cardActivity3();}
        });
        profile= findViewById(R.id.imageView4);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileActivity();
            }
        });
        profileImage= findViewById(R.id.imageView4);
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        String currentUserUid = getCurrentUserUid();
        loadImageFromFirebaseStorage(currentUserUid);

        //retreive uname
        uname= findViewById(R.id.username);
        retrieveAndSetUserName();

        //notification
        notification= findViewById(R.id.notification);

        // Get user location
        getUserLocation();

        // Notification icon click listener
        notification.setOnClickListener(v -> showScamReports(userLat, userLon));


        // Hide notification icon initially
        notification.setVisibility(View.GONE);

    }
    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            userLat = location.getLatitude();
            userLon = location.getLongitude();

            Log.d("Location", "Current Location: " + userLat + ", " + userLon);

            // Check Firebase for scam reports based on latitude and longitude
            checkForScamReports(userLat, userLon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void checkForScamReports(double userLat, double userLon) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ScamArea");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasScamReports = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String scamMessage = snapshot.child("message").getValue(String.class);
                    Double scamLat = snapshot.child("latitude").getValue(Double.class);
                    Double scamLon = snapshot.child("longitude").getValue(Double.class);

                    if (scamLat != null && scamLon != null) {
                        // Calculate distance between user location and scam location
                        double distance = calculateDistance(userLat, userLon, scamLat, scamLon);

                        if (distance <= 100) {  // If distance is within 100 meters
                            hasScamReports = true;
                            isNotificationVisible = true;
                            notification.setVisibility(View.VISIBLE); // Show notification icon
                            Log.d("ScamReports", "Found matching scam report within 100m radius!");
                            break;  // Exit loop if a match is found
                        }
                    }
                }

                if (!hasScamReports) {
                    isNotificationVisible = false;
                    notification.setVisibility(View.GONE);  // Hide notification icon
                    Log.d("ScamReports", "No matching scam reports found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error checking scam reports: " + databaseError.getMessage());
            }
        });
    }


    private void showScamReports(double userLat, double userLon) {
        if (isNotificationVisible) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ScamArea");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    StringBuilder scamMessages = new StringBuilder();
                    float averageRating = 0;
                    int countReportsWithin100Meters = 0;


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        String scamMessage = snapshot.child("message").getValue(String.class);
                        Double scamLat = snapshot.child("latitude").getValue(Double.class);
                        Double scamLon = snapshot.child("longitude").getValue(Double.class);
                        Long badNewsCount = snapshot.child("badNews").getValue(Long.class);
                        Long goodNewsCount = snapshot.child("goodNews").getValue(Long.class);

                        if (scamLat != null && scamLon != null) {
                            // Calculate distance between user location and scam location
                            double distance = calculateDistance(userLat, userLon, scamLat, scamLon);

                            if (distance <= 100) {  // If distance is within 100 meters
                                scamMessages.append(scamMessage).append("\n\n");

                                // Calculate the rating based on goodNews and badNews
                                if (goodNewsCount != null && badNewsCount != null) {
                                    int totalReviews = (int) (goodNewsCount + badNewsCount);
                                    float rating = (totalReviews > 0) ? ((float) goodNewsCount / totalReviews) * 5 : 0;
                                    averageRating += rating;  // Add to average rating
                                    countReportsWithin100Meters++;
                                    Log.d("Rating", "Good News: " + goodNewsCount + ", Bad News: " + badNewsCount);
                                    Log.d("Rating", "Rating for the area: " + rating + " stars");
                                }
                            }
                        }
                    }

                    // Calculate final average rating for all scam reports within 100 meters
                    if (countReportsWithin100Meters > 0) {
                        averageRating /= countReportsWithin100Meters;
                        Log.d(" averageRating Rating", " averageRating Rating for the area: " +  averageRating + " stars");
                    }
                    // Map to store location and its corresponding ratings
                    Map<String, Float> locationRatings = new HashMap<>();
                    Map<String, Integer> locationReportCounts = new HashMap<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String scamLocation = snapshot.child("scamLocation").getValue(String.class);
                        Long badNewsCount = snapshot.child("badNews").getValue(Long.class);
                        Long goodNewsCount = snapshot.child("goodNews").getValue(Long.class);

                        if (scamLocation != null && goodNewsCount != null && badNewsCount != null) {
                            int totalReviews = (int) (goodNewsCount + badNewsCount);

                            if (totalReviews > 0) {
                                // Calculate the rating for this scamLocation
                                float rating = ((float) goodNewsCount / totalReviews) * 5;

                                // If this location already exists in the map, average the rating
                                if (locationRatings.containsKey(scamLocation)) {
                                    float currentTotalRating = locationRatings.get(scamLocation) * locationReportCounts.get(scamLocation);
                                    locationReportCounts.put(scamLocation, locationReportCounts.get(scamLocation) + 1);
                                    float newTotalRating = currentTotalRating + rating;
                                    float newAverageRating = newTotalRating / locationReportCounts.get(scamLocation);

                                    locationRatings.put(scamLocation, newAverageRating);
                                } else {
                                    // Add new location with its rating
                                    locationRatings.put(scamLocation, rating);
                                    locationReportCounts.put(scamLocation, 1);  // Initialize report count for this location
                                }
                            }
                        }
                    }
                    // Sort locations by their ratings in descending order
                    List<Map.Entry<String, Float>> sortedLocations = new ArrayList<>(locationRatings.entrySet());
                    // Sort locations by their ratings in descending order
                    sortedLocations.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));


                    // Log the sorted locations and their ratings
                    for (Map.Entry<String, Float> entry : sortedLocations) {
                        Log.d("SortedLocations", "Location: " + entry.getKey() + ", Rating: " + entry.getValue());
                    }

                    // appending them to the scamMessages and displaying in a dialog
                    StringBuilder sortedLocationsMessage = new StringBuilder("\n");
                    for (Map.Entry<String, Float> entry : sortedLocations) {
                        int roundedRating = (int) Math.floor(entry.getValue());  // Cast the rating to an integer to remove decimal points
                        sortedLocationsMessage.append(entry.getKey()).append("\n ").append(roundedRating).append(" stars\n");
                    }


                    // Show scam messages and high-rated locations in an alert dialog
                    if (scamMessages.length() > 0 ) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Location Based Alert");
                        View customLayout = getLayoutInflater().inflate(R.layout.dialog_scam_report, null);
                        builder.setView(customLayout);

                        TextView scamReportMessage = customLayout.findViewById(R.id.scam_report_message);
                        RatingBar ratingBar = customLayout.findViewById(R.id.rating_bar);

                        // Set the scam messages
                        scamReportMessage.setText(scamMessages.toString());
                        ratingBar.setRating(averageRating);  // Set the average rating to the RatingBar

                        TextView toplocations = customLayout.findViewById(R.id.top_locations);
                        toplocations.setText(sortedLocationsMessage.toString());

                        AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(dialogInterface -> {
                            // Customize the button
                            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            if (positiveButton != null) {
                                positiveButton.setBackgroundResource(R.drawable.buttonshine); // Use the custom background
                                positiveButton.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Set text color to white
                            }
                        });

                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog1, which) -> {
                            // Handle OK button click here if needed
                        });

                        dialog.show();
                    } else {
                        Log.d("ScamReports", "No scam reports found within 100 meters.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Firebase", "Error retrieving reports: " + databaseError.getMessage());
                }
            });
        } else {
            Log.d("ScamReports", "Notification is not visible.");
        }
    }

    public void cardActivity1() {
        Intent intent = new Intent(this,CardActivity1.class);
        startActivity(intent);
    }
    public void cardresultActivity1() {
        Intent intent = new Intent(this,CardActivity1Result.class);
        startActivity(intent);
    }
    public void cardActivity2() {
        Intent intent = new Intent(this,CardActivity2.class);
        startActivity(intent);
    }
    public void cardresultActivity2() {
        Intent intent = new Intent(this,CardActivity2Result.class);
        startActivity(intent);
    }
    public void cardActivity3() {
        Intent intent = new Intent(this,CardActivity3.class);
        startActivity(intent);
    }

    public void profileActivity() {
        Intent intent = new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }
    private void loadImageFromFirebaseStorage(String userUid) {
        // Check if the activity is still alive
        if (!isFinishing() && !isDestroyed()) {
            StorageReference imageRef = storageReference
                    .child("profile_images/user_" + userUid + ".jpg");
            // Use Glide to load and display the image
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!isFinishing() && !isDestroyed()) {
                    String imageUrl = uri.toString();
                    Glide.with(this)
                            .load(imageUrl)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(profileImage);
                }
            });
        }
    }
    private String getCurrentUserUid() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            return user.getUid();
        } else {
            // Handle the case where the user is not authenticated
            return "";
        }

    }
    private void retrieveAndSetUserName() {
        // Get the current user's UID
        String currentUserUid = getCurrentUserUid();

        // Create a reference to the "user" node in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(currentUserUid);

        // Retrieve the user's name from the database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve user data from the snapshot
                    String userName = dataSnapshot.child("uname").getValue(String.class);
                    // Set the user's name to UsrNameTextView
                    uname.setText(userName);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.e("Firebase", "Error retrieving user name: " + databaseError.getMessage());
            }
        });
    }
    public void opencard2resultActivity() {
        Intent intent = new Intent(this,CardActivity1Result.class);
        startActivity(intent);
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the earth in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c; // convert to meters

        return distance;
    }

}