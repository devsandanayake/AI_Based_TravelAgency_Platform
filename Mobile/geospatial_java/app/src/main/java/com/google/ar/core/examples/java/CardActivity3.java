package com.google.ar.core.examples.java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.examples.java.geospatial.R;

import com.google.ar.core.examples.java.geospatial.databinding.Card3Binding;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class CardActivity3 extends AppCompatActivity {
    ImageView next;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap = null;
    private Card3Binding binding;
    CardView icon, icon2;
    Button btnChinese, btnSpanish, btnGerman, btnTamil, btnHindi;
    TextView translatedText;
    String recognizedText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.card3);

        // Initialize UI components
        btnChinese = findViewById(R.id.btn_chinese);
        btnSpanish = findViewById(R.id.btn_spanish);
        btnGerman = findViewById(R.id.btn_german);
        btnTamil = findViewById(R.id.btn_tamil);
        btnHindi = findViewById(R.id.btn_hindi);
        translatedText = findViewById(R.id.translated_text_view);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        }

        binding.captureImage.setOnClickListener(v -> takeImage());
        binding.detectTextImageBtn.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            processImage();
        });

        next = findViewById(R.id.next);
        next.setOnClickListener(v -> homeActivity());

        icon = findViewById(R.id.icon);
        icon2 = findViewById(R.id.icon2);
        icon.setOnClickListener(v -> ScamAreaActivity());
        icon2.setOnClickListener(v -> AboutAreaActivity());

        // Language translation button click listeners
        setupTranslationButtons();
    }

    private void setupTranslationButtons() {
        btnChinese.setOnClickListener(v -> translateText("zh"));
        btnSpanish.setOnClickListener(v -> translateText("es"));
        btnGerman.setOnClickListener(v -> translateText("de"));
        btnTamil.setOnClickListener(v -> translateText("ta"));
        btnHindi.setOnClickListener(v -> translateText("hi"));
    }

    private void translateText(String targetLanguage) {
        if (!recognizedText.isEmpty()) {
            // Show progress bar while translating
            binding.progressBar.setVisibility(View.VISIBLE);

            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage("en") // Assuming the recognized text is in English
                    .setTargetLanguage(targetLanguage)
                    .build();
            Translator translator = com.google.mlkit.nl.translate.Translation.getClient(options);

            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused -> translator.translate(recognizedText)
                            .addOnSuccessListener(translated -> {
                                translatedText.setText(translated);
                                binding.progressBar.setVisibility(View.GONE); // Hide progress bar
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(findViewById(android.R.id.content), "Translation failed", Snackbar.LENGTH_LONG).show();
                                binding.progressBar.setVisibility(View.GONE); // Hide progress bar
                            }))
                    .addOnFailureListener(e -> {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to download translation model", Snackbar.LENGTH_LONG).show();
                        binding.progressBar.setVisibility(View.GONE); // Hide progress bar
                    });
        } else {
            Snackbar.make(findViewById(android.R.id.content), "No text available to translate", Snackbar.LENGTH_LONG).show();
        }
    }


    private void takeImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(findViewById(android.R.id.content), "Error launching camera. Camera app not found.", Snackbar.LENGTH_LONG).show();
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
                binding.imageView.setImageBitmap(imageBitmap);
            }
        }
    }

    private void processImage() {
        if (imageBitmap != null) {
            InputImage image = InputImage.fromBitmap(imageBitmap, 0);
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .addOnSuccessListener(visionText -> {
                        recognizedText = visionText.getText();
                        binding.textView.setText(recognizedText);
                        binding.progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to recognize text", Snackbar.LENGTH_LONG).show();
                        binding.progressBar.setVisibility(View.GONE);
                    });
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Please select a photo", Snackbar.LENGTH_LONG).show();
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    public void homeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    public void ScamAreaActivity() {
        Intent intent = new Intent(this, ReportScamAreaActivity.class);
        startActivity(intent);
    }

    public void AboutAreaActivity() {
        Intent intent = new Intent(this, AboutAreaActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeImage();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Camera permission is required to capture images", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
