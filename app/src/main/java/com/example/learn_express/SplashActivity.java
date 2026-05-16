package com.example.learn_express;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set the layout
        setContentView(R.layout.activity_splash);

        // Transition to MainActivity (Home Page) after a 2-second delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}