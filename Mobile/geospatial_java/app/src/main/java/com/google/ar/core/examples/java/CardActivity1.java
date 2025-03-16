package com.google.ar.core.examples.java;

import static androidx.databinding.adapters.TextViewBindingAdapter.setText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.ar.core.examples.java.geospatial.R;
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

public class CardActivity1 extends AppCompatActivity {

    private CardView icon2;
    private ImageView next;
    private Button submitButton;
    private ProgressBar progressBar;
    private RadioGroup group1, group2;

    private EditText EditText1,EditText2,EditText3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card1);

        next = findViewById(R.id.next);
        icon2 = findViewById(R.id.icon4);
        submitButton = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);

        // Initialize the RadioGroups

        group1 = findViewById(R.id.group1);
        group2 = findViewById(R.id.group2);
        EditText1= findViewById(R.id.input_q1);
        EditText2= findViewById(R.id.input_q2);
        EditText3= findViewById(R.id.input_q3);


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
                progressBar.setVisibility(View.VISIBLE);

                // Retrieve selected answers from each RadioGroup
                String destinationPreferences = EditText1.getText().toString().trim();
                String travelDatesAndDuration = getSelectedOption(group1);
                String activitiesAndInterests = EditText3.getText().toString().trim();
                String specialRequirements = EditText2.getText().toString().trim();
                String month = getSelectedOption(group2);
                String climate = "Cloudy";

                // Create JSON object to send
                JSONObject postData = new JSONObject();
                try {
                    postData.put("Destination", destinationPreferences);
                    postData.put("Month", month);
                    postData.put("Travel Duration", travelDatesAndDuration);
                    postData.put("Climate", climate);
                    postData.put("Activites", activitiesAndInterests);
                    postData.put("Special Requirements", specialRequirements);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("CardActivity1", "Payload: " + postData.toString());

                // Send the POST request
                String url = "https://smarttripplannerupdated-357877744933.us-central1.run.app/";
                RequestQueue queue = Volley.newRequestQueue(CardActivity1.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("CardActivity1", "Response: " + response.toString());


                                // Get the current user's ID from Firebase Authentication
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();

                                    // Reference to Firebase Realtime Database
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                            .getReference("travelAreas").child(userId);

                                    String autoGeneratedId = databaseReference.push().getKey(); // Generate unique ID

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
                                                            Toast.makeText(CardActivity1.this, "Data saved to Firebase!", Toast.LENGTH_SHORT).show();
                                                            Log.d("CardActivity1", "Data saved to Firebase!");

                                                            String prediction = "";
                                                            try {
                                                                prediction = response.getString("prediction");
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            Log.d("CardActivity1", "Prediction: " + prediction);

                                                            Intent intent = new Intent(CardActivity1.this, CardActivity1SuddenResult.class);
                                                            intent.putExtra("prediction", prediction);
                                                            startActivity(intent);
                                                            progressBar.setVisibility(View.GONE);
                                                        } else {
                                                            Toast.makeText(CardActivity1.this, "Failed to save data to Firebase.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(CardActivity1.this, "Failed to generate unique ID for Firebase.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(CardActivity1.this, "User not logged in!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(CardActivity1.this, "Submission Failed!, try again", Toast.LENGTH_SHORT).show();
                                Log.e("CardActivity1", "Error: " + error.toString());
                            }
                        }
                );

                queue.add(jsonObjectRequest);
            }
        });
    }

    public void opencard2resultActivity() {
        Intent intent = new Intent(this, CardActivity1Result.class);
        startActivity(intent);
    }
    public void openhomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private String getSelectedOption(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = findViewById(selectedId);
            return selectedButton.getText().toString();
        }
        return "";
    }
}