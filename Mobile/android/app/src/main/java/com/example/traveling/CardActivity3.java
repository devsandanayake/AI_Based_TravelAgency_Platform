package com.example.traveling;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.example.traveling.databinding.Card3Binding;

public class CardActivity3 extends AppCompatActivity {
    ImageView next;
    private final int REQUEST_IMAGE_CAPTURE = 1;  // Request code for capturing an image
    private Bitmap imageBitmap = null;  // Bitmap for storing the captured image
    private Card3Binding binding;  // Data binding instance
    CardView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.card3);

        binding.captureImage.setOnClickListener(v -> takeImage());  // Capture image button

        binding.detectTextImageBtn.setOnClickListener(v ->{
                binding.progressBar.setVisibility(android.view.View.VISIBLE); // Show progress bar
                processImage();// Process image button
        });
        next=findViewById((R.id.next));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeActivity();
            }
        });
        icon=findViewById((R.id.icon));
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScamAreaActivity();
            }
        });
    }

    // Function to capture image
    private void takeImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Error launching camera", Snackbar.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                binding.imageView.setImageBitmap(imageBitmap);  // Display the image in the ImageView
            }
        }
    }

    // Function to process text recognition on the image
    private void processImage() {
        if (imageBitmap != null) {
            InputImage image = InputImage.fromBitmap(imageBitmap, 0);
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener(visionText -> {
                        // Display the recognized text
                        binding.textView.setText(visionText.getText());
                        binding.progressBar.setVisibility(android.view.View.GONE);  // Hide progress bar
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to recognize text", Snackbar.LENGTH_LONG).show();
                        binding.progressBar.setVisibility(android.view.View.GONE);
                    });
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Please select a photo", Snackbar.LENGTH_LONG).show();
            binding.progressBar.setVisibility(android.view.View.GONE);  // Hide progress bar
        }
    }
    public void homeActivity() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }
    public void ScamAreaActivity() {
        Intent intent = new Intent(this,ReportScamAreaActivity.class);
        startActivity(intent);
    }
}
