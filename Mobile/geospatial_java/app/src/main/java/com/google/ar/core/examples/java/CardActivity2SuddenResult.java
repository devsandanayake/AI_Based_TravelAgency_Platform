package com.google.ar.core.examples.java;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.geospatial.R;

import org.json.JSONArray;
import org.json.JSONException;

public class CardActivity2SuddenResult extends AppCompatActivity {

    private TextView text1;
    private ImageView next1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card2_suddenresult);

        text1 = findViewById(R.id.text1);
        next1 = findViewById(R.id.next1);

        // Retrieve the prediction passed from CardActivity1
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("prediction")) {
            String prediction = intent.getStringExtra("prediction");
            Log.d("CardActivity2SuddenResult", "Prediction: " + prediction);

            // Format the prediction
            String formattedPrediction = formatPrediction(prediction);
            text1.setText(formattedPrediction);
        }

        // Set up click listener for the next ImageView
        next1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to HomeActivity
                Intent homeIntent = new Intent(CardActivity2SuddenResult.this, HomeActivity.class);
                startActivity(homeIntent);
                finish(); // Optionally close the current activity
            }
        });
    }

    /**
     * Formats the prediction to display each item on a new line.
     *
     * @param prediction Original prediction string
     * @return Formatted prediction string
     */
    private String formatPrediction(String prediction) {
        if (prediction == null || prediction.isEmpty()) {
            return "No prediction available.";
        }

        try {
            // Parse the prediction as a JSON array
            JSONArray jsonArray = new JSONArray(prediction);

            // Extract the first array within the JSON array
            JSONArray nestedArray = jsonArray.getJSONArray(0);
            StringBuilder formattedPrediction = new StringBuilder();

            // Loop through each item in the nested array
            for (int i = 0; i < nestedArray.length(); i++) {
                formattedPrediction.append(nestedArray.getString(i)); // Add the item
                formattedPrediction.append("\n"); // Add a newline
            }

            return formattedPrediction.toString().trim(); // Remove any trailing newline
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("CardActivity2SuddenResult", "Error parsing prediction JSON: " + e.getMessage());
            return "Error processing prediction.";
        }
    }
}
