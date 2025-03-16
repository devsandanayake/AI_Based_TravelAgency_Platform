package com.example.traveling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class CardActivity2 extends AppCompatActivity {
    CardView icon2;
    ImageView next;
    private Button submitButton;
    private RadioGroup group1, group2, group3, group4, group5, group6 , group7 , group8 , group9 , group10 , group11 , group12 , group13 , group14 , group15;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card2);
        next=findViewById((R.id.next));
        icon2=findViewById((R.id.icon4));
        submitButton = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        // Initialize the RadioGroups
        group1 = findViewById(R.id.group1);
        group2 = findViewById(R.id.group2);
        group3 = findViewById(R.id.group3);
        group4 = findViewById(R.id.group4);
        group5 = findViewById(R.id.group5);
        group6 = findViewById(R.id.group6);
        group7 = findViewById(R.id.group7);
        group8 = findViewById(R.id.group8);
        group9 = findViewById(R.id.group9);
        group10 = findViewById(R.id.group10);
        group11 = findViewById(R.id.group11);
        group12 = findViewById(R.id.group12);
        group13 = findViewById(R.id.group13);
        group14 = findViewById(R.id.group14);
        group15 = findViewById(R.id.group15);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openhomeActivity();
            }
        });
        icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opencard2resultActivity();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the progress bar while submitting
                progressBar.setVisibility(View.VISIBLE);

                // Retrieve selected answers from each RadioGroup
                String FavoriteActivities = getSelectedOption(group1);
                String Budget = getSelectedOption(group2);
                String NumberofPersons = getSelectedOption(group3);
                String TransportationOptions = getSelectedOption(group4);
                String Month = getSelectedOption(group5);
                String Place = getSelectedOption(group6);
                String HealthCondition = getSelectedOption(group7);
                String AccomadationPreferences = getSelectedOption(group8);
                String SaftyConcersn = getSelectedOption(group9);
                String DieataryNeeds = getSelectedOption(group10);
                String PetFriendlyOption = getSelectedOption(group11);
                String Connectivity = getSelectedOption(group12);
                String LanguagePreference = getSelectedOption(group13);
                String ShopppingPreferences = getSelectedOption(group14);
                String Accessibilityneeds = getSelectedOption(group15);

                // Create JSON object to send
                JSONObject postData = new JSONObject();

                // Create JSON object to send
                try {
                    JSONObject features = new JSONObject();
                    features.put("Favorite Activities", FavoriteActivities);
                    features.put("Budget", Budget);
                    features.put("Number of Persons", Integer.parseInt(NumberofPersons)); // Ensure it's an integer
                    features.put("Transportation Options", TransportationOptions);
                    features.put("Month", Month);
                    features.put("Place", Place);
                    features.put("Health Condition", HealthCondition);
                    features.put("Accomadation Preferences", AccomadationPreferences);
                    features.put("Safty Concersn", SaftyConcersn);
                    features.put("Dieatary Needs ", DieataryNeeds);
                    features.put("Pet Friendly Option", PetFriendlyOption);
                    features.put("Connectivity", Connectivity);
                    features.put("Language Preference ", LanguagePreference);
                    features.put("Shoppping Preferences", ShopppingPreferences);
                    features.put("Accessibility needs", Accessibilityneeds);

                    // Wrap the features object into the main JSON payload
                    postData.put("features", features);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d("CardActivity2", "Post Data: " + postData.toString());

                // Send the POST request
                String url = "https://us-central1-custom-repeater-446305-a4.cloudfunctions.net/agency";
                RequestQueue queue = Volley.newRequestQueue(CardActivity2.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("CardActivity2", "Response: " + response.toString());
                                progressBar.setVisibility(View.GONE); // Hide the progress bar
                                Toast.makeText(CardActivity2.this, "Submission Successful!", Toast.LENGTH_SHORT).show();
                                // Get the current user's ID from Firebase Authentication
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();

                                    // Reference to Firebase Realtime Database
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                            .getReference("travel_agency").child(userId);

                                    String autoGeneratedId = databaseReference.push().getKey();

                                    if (autoGeneratedId != null) {
                                        // Create a HashMap for structured data
                                        HashMap<String, Object> firebaseData = new HashMap<>();
                                        try {
                                            // Generate the timestamp in ISO 8601 format
                                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                                            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                            String formattedTimestamp = isoFormat.format(new Date());

                                            firebaseData.put("prediction", response.getString("prediction")); // Ensure response JSON contains "prediction"
                                            firebaseData.put("timestamp", formattedTimestamp); // Store formatted timestamp
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        // Save data with the generated unique ID
                                        databaseReference.child(autoGeneratedId).setValue(firebaseData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(CardActivity2.this, "Data saved to Firebase!", Toast.LENGTH_SHORT).show();
                                                            Log.d("CardActivity2", "Data saved to Firebase!");

                                                            String prediction = "";
                                                            try {
                                                                prediction = response.getString("prediction");
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            Log.d("CardActivity2", "Prediction: " + prediction);

                                                            Intent intent = new Intent(CardActivity2.this, CardActivity2SuddenResult.class);
                                                            intent.putExtra("prediction", prediction);
                                                            startActivity(intent);
                                                        } else {
                                                            Toast.makeText(CardActivity2.this, "Failed to save data to Firebase.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(CardActivity2.this, "Failed to generate unique ID for Firebase.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(CardActivity2.this, "User not logged in!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },


                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressBar.setVisibility(View.GONE); // Hide the progress bar
                                Toast.makeText(CardActivity2.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
                                Log.e("CardActivity1", "Error: " + error.toString());
                            }
                        }
                );

                // Add the request to the RequestQueue
                queue.add(jsonObjectRequest);
            }
        });
    }
    public void opencard2resultActivity() {
        Intent intent = new Intent(this,CardActivity2Result.class);
        startActivity(intent);
    }
    public void openhomeActivity() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }
    // Helper method to get selected option text from a RadioGroup
    private String getSelectedOption(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = findViewById(selectedId);
            return selectedButton.getText().toString();
        }
        return "";
    }
}