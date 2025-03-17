package com.google.ar.core.examples.java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Switch;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import com.google.ar.core.examples.java.geospatial.GeospatialActivity;
import com.google.ar.core.examples.java.geospatial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.graphics.Color;

public class HomeActivity extends AppCompatActivity {
    CardView card1, card2, card3, card4;
    ImageView profile, profileImage, notification;
    FirebaseStorage storage;
    StorageReference storageReference;
    TextView uname;
    FrameLayout fram1, frame2, frame3, fram4;
    String currentRoad, currentCity, currentCountry;

    private Switch notificationSwitch;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static final String CHANNEL_ID = "scam_alert_channel";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private ImageView notificationIcon;
    private ListView notificationListView;
    private List<ScamAlert> userAlerts = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private TextView notificationCount;
    private int unreadCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        fram1 = findViewById(R.id.frame1);
        frame2 = findViewById(R.id.frame2);
        frame3 = findViewById(R.id.frame3);
        fram4 = findViewById(R.id.frame4);
        notificationSwitch = findViewById(R.id.notification_switch);

        card1 = findViewById((R.id.icon1));
        card2 = findViewById((R.id.icon2));
        card3 = findViewById((R.id.icon3));
        card4 = findViewById((R.id.icon4));

        checkLocationPermission();


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
        card4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardActivity4();
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
        frame3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardActivity3();
            }
        });
        fram4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardActivity4();
            }
        });
        profile = findViewById(R.id.imageView4);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileActivity();
            }
        });
        profileImage = findViewById(R.id.imageView4);
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        String currentUserUid = getCurrentUserUid();
        loadImageFromFirebaseStorage(currentUserUid);

        //retreive uname
        uname = findViewById(R.id.username);
        retrieveAndSetUserName();

        //notification
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fetchUserLocation();
            }
        });

        notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(v -> showNotificationHistory());
        createNotificationChannel();
        loadUserAlerts();
        notificationCount = findViewById(R.id.notification_count);
    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed with location fetching
            fetchUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation(); // Permission granted, fetch location
            } else {
                Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchUserLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double userLat = location.getLatitude();
                double userLon = location.getLongitude();
                checkForScamReports(userLat, userLon);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Scam Alerts",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for scam alerts in your area");
            channel.enableVibration(true);
            channel.enableLights(true);
            
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showScamNotification(String location, String alertType, String warningMessage) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scam Alert: " + alertType)
            .setContentText("Warning: " + warningMessage + " at " + location)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
        notificationIcon.setVisibility(View.VISIBLE);
    }

    private void showAlertDialog(double scamLat, double scamLon, String scamLocation, String alertType, 
                               String warningMessage, String scamMessage, String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.scam_alert_dialog, null);
        
        ImageView scamImage = dialogView.findViewById(R.id.scam_image);
        TextView locationText = dialogView.findViewById(R.id.location_text);
        TextView alertTypeText = dialogView.findViewById(R.id.alert_type_text);
        TextView warningText = dialogView.findViewById(R.id.warning_text);
        TextView scamMessageText = dialogView.findViewById(R.id.scam_message_text);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        TextView confirmCount = dialogView.findViewById(R.id.confirm_count);

        // Load image with Glide and fallback
        Glide.with(this)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.no_image_icon)
            .error(R.drawable.no_image_icon)
            .into(scamImage);

        locationText.setText("Location: " + scamLocation);
        alertTypeText.setText("Alert Type: " + alertType);
        warningText.setText("Warning: " + warningMessage);
        scamMessageText.setText("Details: " + scamMessage);

        // Get confirmation count from Firebase
        DatabaseReference scamAreaQueryRef = FirebaseDatabase.getInstance().getReference("ScamArea");
        scamAreaQueryRef.orderByChild("location").equalTo(scamLocation)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        confirmCount.setText("No confirmations yet");
                        return;
                    }
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("confirmations").exists()) {
                            long totalConfirms = snapshot.child("confirmations").getChildrenCount();
                            long positiveConfirms = 0;
                            for (DataSnapshot confirm : snapshot.child("confirmations").getChildren()) {
                                Boolean value = confirm.getValue(Boolean.class);
                                if (value != null && value) {
                                    positiveConfirms++;
                                }
                            }
                            float percentage = totalConfirms > 0 ? (float) positiveConfirms / totalConfirms * 100 : 0;
                            confirmCount.setText(String.format("Confirmed by %.1f%% of users", percentage));
                        } else {
                            confirmCount.setText("No confirmations yet");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    confirmCount.setText("Error loading confirmations");
                }
            });

        builder.setView(dialogView)
            .setPositiveButton("Confirm", (dialog, which) -> {
                // Save confirmation to Firebase
                String userId = getCurrentUserUid();
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference scamAreaConfirmRef = FirebaseDatabase.getInstance().getReference("ScamArea");
                scamAreaConfirmRef.orderByChild("location").equalTo(scamLocation)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                Toast.makeText(HomeActivity.this, "Error: Alert not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String scamId = snapshot.getKey();
                                if (scamId != null && !scamId.isEmpty()) {
                                    DatabaseReference confirmRef = FirebaseDatabase.getInstance()
                                        .getReference("ScamArea")
                                        .child(scamId)
                                        .child("confirmations")
                                        .child(userId);
                                    confirmRef.setValue(true)
                                        .addOnSuccessListener(aVoid -> {
                                            updateConfirmationPercentage(scamId);
                                            Toast.makeText(HomeActivity.this, "Confirmation saved", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firebase", "Error saving confirmation: " + e.getMessage());
                                            Toast.makeText(HomeActivity.this, "Error saving confirmation", Toast.LENGTH_SHORT).show();
                                        });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Firebase", "Error querying alerts: " + databaseError.getMessage());
                            Toast.makeText(HomeActivity.this, "Error accessing alerts", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("False Alert", (dialog, which) -> {
                // Save false alert to Firebase
                String userId = getCurrentUserUid();
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference scamAreaFalseRef = FirebaseDatabase.getInstance().getReference("ScamArea");
                scamAreaFalseRef.orderByChild("location").equalTo(scamLocation)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                Toast.makeText(HomeActivity.this, "Error: Alert not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String scamId = snapshot.getKey();
                                if (scamId != null && !scamId.isEmpty()) {
                                    DatabaseReference confirmRef = FirebaseDatabase.getInstance()
                                        .getReference("ScamArea")
                                        .child(scamId)
                                        .child("confirmations")
                                        .child(userId);
                                    confirmRef.setValue(false)
                                        .addOnSuccessListener(aVoid -> {
                                            updateConfirmationPercentage(scamId);
                                            Toast.makeText(HomeActivity.this, "False alert saved", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firebase", "Error saving false alert: " + e.getMessage());
                                            Toast.makeText(HomeActivity.this, "Error saving false alert", Toast.LENGTH_SHORT).show();
                                        });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Firebase", "Error querying alerts: " + databaseError.getMessage());
                            Toast.makeText(HomeActivity.this, "Error accessing alerts", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNeutralButton("Close", null)
            .show();
    }

    private void checkForScamReports(double userLat, double userLon) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ScamArea");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double lat = snapshot.child("latitude").getValue(Double.class);
                    Double lon = snapshot.child("longitude").getValue(Double.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String alertType = snapshot.child("alertType").getValue(String.class);
                    String warningMessage = snapshot.child("warningMessage").getValue(String.class);
                    String scamMessage = snapshot.child("scamMessage").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    if (lat != null && lon != null && calculateDistance(userLat, userLon, lat, lon) <= 100000) {
                        showScamNotification(location, alertType, warningMessage);
                        saveAlertToUser(location, alertType, warningMessage, scamMessage, imageUrl, lat, lon);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving reports: " + databaseError.getMessage());
            }
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    public void cardActivity1() {
        Intent intent = new Intent(this, CardActivity1.class);
        startActivity(intent);
    }

    public void cardresultActivity1() {
        Intent intent = new Intent(this, CardActivity1Result.class);
        startActivity(intent);
    }

    public void cardActivity2() {
        Intent intent = new Intent(this, CardActivity2.class);
        startActivity(intent);
    }

    public void cardresultActivity2() {
        Intent intent = new Intent(this, CardActivity2Result.class);
        startActivity(intent);
    }

    public void cardActivity3() {
        Intent intent = new Intent(this, CardActivity3.class);
        startActivity(intent);
    }

    public void cardActivity4() {
        Intent intent = new Intent(this, com.google.ar.core.codelabs.hellogeospatial.HelloGeoActivity.class);
        startActivity(intent);
    }

    public void profileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
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
            }).addOnFailureListener(e -> {
                // Handle the case where the image doesn't exist
                if (!isFinishing() && !isDestroyed()) {
                    // Load a default profile image
                    Glide.with(this)
                            .load(R.drawable.profile)
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
        Intent intent = new Intent(this, CardActivity1Result.class);
        startActivity(intent);
    }

    private void loadUserAlerts() {
        String userId = getCurrentUserUid();
        if (userId.isEmpty()) return;

        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("UserAlerts")
            .child(userId);

        alertsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userAlerts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ScamAlert alert = snapshot.getValue(ScamAlert.class);
                    if (alert != null) {
                        userAlerts.add(alert);
                    }
                }
                updateNotificationIcon();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading alerts: " + databaseError.getMessage());
            }
        });
    }

    private void updateNotificationIcon() {
        if (!userAlerts.isEmpty()) {
            notificationIcon.setVisibility(View.VISIBLE);
            updateUnreadCount();
        } else {
            notificationIcon.setVisibility(View.GONE);
            notificationCount.setVisibility(View.GONE);
        }
    }

    private void updateUnreadCount() {
        unreadCount = 0;
        for (ScamAlert alert : userAlerts) {
            if (!alert.isRead) {
                unreadCount++;
            }
        }
        
        if (unreadCount > 0) {
            notificationCount.setVisibility(View.VISIBLE);
            notificationCount.setText(String.valueOf(unreadCount));
        } else {
            notificationCount.setVisibility(View.GONE);
        }
    }

    private void showNotificationHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.notification_history_dialog, null);
        
        notificationListView = dialogView.findViewById(R.id.notification_list);
        NotificationAdapter adapter = new NotificationAdapter();
        notificationListView.setAdapter(adapter);

        notificationListView.setOnItemClickListener((parent, view, position, id) -> {
            ScamAlert alert = userAlerts.get(position);
            if (!alert.isRead) {
                markAlertAsRead(alert);
            }
            showAlertDialog(alert.latitude, alert.longitude, alert.location, 
                          alert.alertType, alert.warningMessage, alert.scamMessage, alert.imageUrl);
        });

        builder.setView(dialogView)
            .setTitle("Scam Alerts")
            .setPositiveButton("Close", null)
            .show();
    }

    private void markAlertAsRead(ScamAlert alert) {
        String userId = getCurrentUserUid();
        if (userId.isEmpty()) {
            Log.e("Firebase", "User ID is empty");
            return;
        }

        if (alert == null || alert.id == null || alert.id.isEmpty()) {
            Log.e("Firebase", "Alert or Alert ID is null/empty");
            return;
        }

        DatabaseReference alertRef = FirebaseDatabase.getInstance().getReference("UserAlerts")
            .child(userId)
            .child(alert.id);

        alertRef.child("isRead").setValue(true)
            .addOnSuccessListener(aVoid -> {
                alert.isRead = true;
                updateUnreadCount();
                Log.d("Firebase", "Alert marked as read successfully");
            })
            .addOnFailureListener(e -> {
                Log.e("Firebase", "Error marking alert as read: " + e.getMessage());
            });
    }

    private void updateConfirmationPercentage(String scamId) {
        DatabaseReference scamRef = FirebaseDatabase.getInstance().getReference("ScamArea")
            .child(scamId)
            .child("confirmations");

        scamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long totalConfirms = dataSnapshot.getChildrenCount();
                long positiveConfirms = 0;
                for (DataSnapshot confirm : dataSnapshot.getChildren()) {
                    Boolean value = confirm.getValue(Boolean.class);
                    if (value != null && value) {
                        positiveConfirms++;
                    }
                }
                float percentage = totalConfirms > 0 ? (float) positiveConfirms / totalConfirms * 100 : 0;
                
                // Update the percentage in the ScamArea node
                DatabaseReference percentageRef = FirebaseDatabase.getInstance().getReference("ScamArea")
                    .child(scamId)
                    .child("confirmationPercentage");
                percentageRef.setValue(percentage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error updating confirmation percentage: " + databaseError.getMessage());
            }
        });
    }

    private void saveAlertToUser(String location, String alertType, String warningMessage, 
                               String scamMessage, String imageUrl, double lat, double lon) {
        String userId = getCurrentUserUid();
        if (userId.isEmpty()) return;

        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("UserAlerts")
            .child(userId);

        // Check for recent alerts at this location
        alertsRef.orderByChild("location").equalTo(location)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean shouldAdd = true;
                    Date now = new Date();
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ScamAlert existingAlert = snapshot.getValue(ScamAlert.class);
                        if (existingAlert != null && existingAlert.location != null) {
                            // Check if location matches
                            if (existingAlert.location.equals(location)) {
                                try {
                                    Date alertDate = dateFormat.parse(existingAlert.timestamp);
                                    long diffInMillis = now.getTime() - alertDate.getTime();
                                    if (diffInMillis < 24 * 60 * 60 * 1000) { // 24 hours
                                        shouldAdd = false;
                                        Log.d("Firebase", "Alert already exists for this location within 24 hours");
                                        break;
                                    }
                                } catch (Exception e) {
                                    Log.e("Date", "Error parsing date", e);
                                }
                            }
                        }
                    }

                    if (shouldAdd) {
                        String alertId = alertsRef.push().getKey();
                        if (alertId != null) {
                            ScamAlert newAlert = new ScamAlert(location, alertType, warningMessage, 
                                                             scamMessage, imageUrl, lat, lon, 
                                                             dateFormat.format(now));
                            newAlert.id = alertId; // Set the ID before saving
                            alertsRef.child(alertId).setValue(newAlert)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Alert saved successfully with ID: " + alertId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Error saving alert: " + e.getMessage());
                                });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Firebase", "Error checking existing alerts: " + databaseError.getMessage());
                }
            });
    }

    private class NotificationAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return userAlerts.size();
        }

        @Override
        public Object getItem(int position) {
            return userAlerts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(HomeActivity.this)
                    .inflate(R.layout.notification_item, parent, false);
            }

            ScamAlert alert = userAlerts.get(position);
            TextView locationText = convertView.findViewById(R.id.notification_location);
            TextView timeText = convertView.findViewById(R.id.notification_time);
            TextView typeText = convertView.findViewById(R.id.notification_type);

            locationText.setText(alert.location);
            timeText.setText(alert.timestamp);
            typeText.setText(alert.alertType);

            // Set background color for unread notifications
            if (!alert.isRead) {
                convertView.setBackgroundColor(Color.parseColor("#E3F2FD"));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
        }
    }

    private static class ScamAlert {
        public String id;
        public String location;
        public String alertType;
        public String warningMessage;
        public String scamMessage;
        public String imageUrl;
        public double latitude;
        public double longitude;
        public String timestamp;
        public boolean isRead;

        public ScamAlert() {
            // Required empty constructor for Firebase
        }

        public ScamAlert(String location, String alertType, String warningMessage, 
                        String scamMessage, String imageUrl, double lat, double lon, 
                        String timestamp) {
            this.location = location;
            this.alertType = alertType;
            this.warningMessage = warningMessage;
            this.scamMessage = scamMessage;
            this.imageUrl = imageUrl;
            this.latitude = lat;
            this.longitude = lon;
            this.timestamp = timestamp;
            this.isRead = false;
        }
    }
}