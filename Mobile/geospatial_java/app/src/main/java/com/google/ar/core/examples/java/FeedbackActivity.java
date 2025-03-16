package com.google.ar.core.examples.java;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.examples.java.geospatial.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private EditText[] inputFields;
    private Button submitButton;
    private ProgressBar progressBar;
    private static final String URL = "https://us-central1-custom-repeater-446305-a4.cloudfunctions.net/agency2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);

        inputFields = new EditText[]{
                findViewById(R.id.input_q1), findViewById(R.id.input_q2), findViewById(R.id.input_q3), findViewById(R.id.input_q4),
                findViewById(R.id.input_q5), findViewById(R.id.input_q6), findViewById(R.id.input_q7), findViewById(R.id.input_q8),
                findViewById(R.id.input_q9), findViewById(R.id.input_q10), findViewById(R.id.input_q11), findViewById(R.id.input_q12),
                findViewById(R.id.input_q13), findViewById(R.id.input_q14), findViewById(R.id.input_q15), findViewById(R.id.input_q16)
        };

        submitButton = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progressBar);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs(v)) {
                    sendFeedbackData(v);
                }
            }
        });
    }

    private boolean validateInputs(View view) {
        for (EditText inputField : inputFields) {
            if (inputField.getText().toString().trim().isEmpty()) {
                Snackbar.make(view, "All fields must be filled out", Snackbar.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void sendFeedbackData(final View view) {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        JSONObject postData = new JSONObject();
        try {
            JSONObject features = new JSONObject();
            String[] keys = {"Favorite Activities", "Budget", "Number of Persons", "Transportation Options",
                    "Month", "Place", "Health Condition", "Accomadation Preferences",
                    "Safty Concersn", "Dieatary Needs ", "Pet Friendly Option", "Connectivity",
                    "Language Preference ", "Shoppping Preferences", "Accessibility needs", "Agency"};

            for (int i = 0; i < inputFields.length; i++) {
                features.put(keys[i], inputFields[i].getText().toString().trim());
            }

            postData.put("features", features);
            Log.d("RequestData", postData.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Log.d("VolleyResponse", "Response: " + response);
                    Snackbar.make(view, "Thank you for your feedback!", Snackbar.LENGTH_LONG).show();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Snackbar.make(view, "Please try again later.", Snackbar.LENGTH_LONG).show();
                }) {
            @Override
            public byte[] getBody() {
                return postData.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return response.statusCode == 200 ? Response.success(new String(response.data), HttpHeaderParser.parseCacheHeaders(response)) : Response.error(new VolleyError(response));
            }
        };

        queue.add(stringRequest);
    }
}