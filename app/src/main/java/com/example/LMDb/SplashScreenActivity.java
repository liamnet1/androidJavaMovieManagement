package com.example.LMDb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreenActivity extends AppCompatActivity {
    // Duration of the splash screen in milliseconds
    private static final long SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Find the logo ImageView
        ImageView ivLogo = findViewById(R.id.ivLogo);
        // Load the flip animation from res/anim/flip.xml
        Animation flipAnimation = AnimationUtils.loadAnimation(this, R.anim.flip);
        // Start the animation
        ivLogo.startAnimation(flipAnimation);

        // After the duration, navigate to MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // close splash screen
        }, SPLASH_DURATION);
    }
}