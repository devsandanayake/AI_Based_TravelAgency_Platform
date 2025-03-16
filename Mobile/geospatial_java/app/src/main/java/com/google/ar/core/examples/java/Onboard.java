package com.google.ar.core.examples.java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.ar.core.examples.java.geospatial.R;

public class Onboard extends AppCompatActivity {
    ImageView next,skip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);
        next=findViewById((R.id.next));
        skip=findViewById((R.id.skip));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity2();
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
    }
    public void Activity2() {
        Intent intent = new Intent(this,MainActivity2.class);
        startActivity(intent);
    }
    public void Login() {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}