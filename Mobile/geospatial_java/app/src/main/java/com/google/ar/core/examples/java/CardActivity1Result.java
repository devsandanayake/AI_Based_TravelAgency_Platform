package com.google.ar.core.examples.java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.examples.java.geospatial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CardActivity1Result extends AppCompatActivity {

    ImageView next;
    LinearLayout cardContainer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card1_result);

        next = findViewById(R.id.next);
        cardContainer = findViewById(R.id.cardContainer);

        // Fetch travel areas for the current user
        fetchTravelAreas();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomeActivity();
            }
        });
    }

    public void openHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void fetchTravelAreas() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("UserId", userId);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference("travelAreas")
                    .child(userId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String timestamp = data.child("timestamp").getValue(String.class);
                            String prediction = data.child("prediction").getValue(String.class);

                            if (timestamp != null && !timestamp.isEmpty() && prediction != null && !prediction.isEmpty()) {
                                addCardToLayout(timestamp, prediction);
                            }
                        }
                    } else {
                        Toast.makeText(CardActivity1Result.this, "No travel areas found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CardActivity1Result.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCardToLayout(String timestamp, String prediction) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_item, cardContainer, false);

        TextView timestampText = cardView.findViewById(R.id.timestampText);
        timestampText.setText(timestamp);

        cardView.setOnClickListener(v -> showPredictionPopup(prediction));
        cardContainer.addView(cardView);
    }

    private void showPredictionPopup(String prediction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Travel Prediction");
        builder.setMessage(prediction);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
