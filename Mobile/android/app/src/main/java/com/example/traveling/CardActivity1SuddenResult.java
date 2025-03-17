package com.example.traveling;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CardActivity1SuddenResult extends AppCompatActivity {

    private TextView text;
    private ImageView next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card1_suddenresult);

        text = findViewById(R.id.text);
        next = findViewById(R.id.next);

        // Retrieve the prediction passed from CardActivity1
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("prediction")) {
            String prediction = intent.getStringExtra("prediction");
            Log.d("CardActivity1SuddenResult", "Prediction: " + prediction);

            // Format the prediction
            String formattedPrediction = formatPrediction(prediction);
            text.setText(formattedPrediction);
        }

        // Set up click listener for the next ImageView
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to HomeActivity
                Intent homeIntent = new Intent(CardActivity1SuddenResult.this, HomeActivity.class);
                startActivity(homeIntent);
                finish(); // Optionally close the current activity
            }
        });
    }

    /**
     * Formats the prediction to add "Day" in front of numbers and splits into new lines.
     *
     * @param prediction Original prediction string
     * @return Formatted prediction string
     */
    private String formatPrediction(String prediction) {
        if (prediction == null || prediction.isEmpty()) {
            return "";
        }

        // Split the prediction into individual activities
        String[] activities = prediction.split(", ");
        StringBuilder formattedPrediction = new StringBuilder();

        for (String activity : activities) {
            if (!activity.trim().isEmpty()) {
                formattedPrediction.append(activity.trim().replaceFirst("(\\d+)", "$1"));
                formattedPrediction.append("\n"); // Add a newline after each activity
            }
        }

        return formattedPrediction.toString().trim(); // Remove any trailing newline
    }
}
