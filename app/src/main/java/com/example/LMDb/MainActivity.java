package com.example.LMDb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnEnterGuest;
    private Button btnSignInPage;
    private Button btnSignUpPage;
    private Button btnMoveToHomeActivity;
    private FirebaseAuth fbAuth;

    // Drawer variables
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize toolbar and set up drawer
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set the correct menu based on login status
        fbAuth = FirebaseAuth.getInstance();
        navigationView.getMenu().clear();
        if (fbAuth.getCurrentUser() != null) {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_in);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_out);
        }

        // Navigation drawer item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.main) {
                // Already in this activity
            } else if (id == R.id.home) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            } else if (id == R.id.signIn) {
                Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                intent.putExtra("fragment", "signin");
                startActivity(intent);
            } else if (id == R.id.signUp) {
                Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                intent.putExtra("fragment", "signup");
                startActivity(intent);
            } else if (id == R.id.moviesYouWatched) {
                Intent intent = new Intent(MainActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchedmovies");
                startActivity(intent);
            } else if (id == R.id.moviesYouLiked) {
                Intent intent = new Intent(MainActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "likedmovies");
                startActivity(intent);
            } else if (id == R.id.watchListMovies) {
                Intent intent = new Intent(MainActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchlist");
                startActivity(intent);
            } else if (id == R.id.upcomingMovies) {
                Intent intent = new Intent(MainActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "upcomingmovies");
                startActivity(intent);
            } else if (id == R.id.userDetails) {
                Intent intent = new Intent(MainActivity.this, UserDetailsActivity.class);
                intent.putExtra("previousActivity", "AuthenticationActivity");
                startActivity(intent);
            } else if (id == R.id.logOut) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                // refresh the activity to update the menu
                recreate();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Handle back press to close drawer if it's open
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        // Animate TextViews for descriptions
        TextView textView1 = findViewById(R.id.tvDescription1);
        TextView textView2 = findViewById(R.id.tvDescription2);
        TextView textView3 = findViewById(R.id.tvDescription3);

        String text1 = textView1.getText().toString();
        String text2 = textView2.getText().toString();
        String text3 = textView3.getText().toString();

        textView1.setText("");
        textView2.setText("");
        textView3.setText("");

        animateTextView(textView1, text1, () -> {
            animateTextView(textView2, text2, () -> {
                animateTextView(textView3, text3, null);
            });
        });

        // Banner animation
        ImageView ivBanner = findViewById(R.id.ivBanner);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivBanner.startAnimation(fadeIn);

        // Button animations and setup
        btnEnterGuest = findViewById(R.id.btnEnterGuest);
        btnSignInPage = findViewById(R.id.btnSignInPage);
        btnSignUpPage = findViewById(R.id.btnSignUpPage);
        btnMoveToHomeActivity = findViewById(R.id.btnMoveToHomeActivity);

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        btnEnterGuest.startAnimation(slideUp);
        btnSignInPage.startAnimation(slideUp);
        btnSignUpPage.startAnimation(slideUp);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Show or hide buttons based on login status
        if (fbAuth.getCurrentUser() != null) {
            btnEnterGuest.setVisibility(View.GONE);
            btnSignInPage.setVisibility(View.GONE);
            btnSignUpPage.setVisibility(View.GONE);
            btnMoveToHomeActivity.setOnClickListener(this);
        } else {
            btnMoveToHomeActivity.setVisibility(View.GONE);
            btnEnterGuest.setOnClickListener(this);
            btnSignInPage.setOnClickListener(this);
            btnSignUpPage.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnEnterGuest) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else if (view == btnSignInPage) {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            intent.putExtra("fragment", "signin");
            startActivity(intent);
        } else if (view == btnSignUpPage) {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            intent.putExtra("fragment", "signup");
            startActivity(intent);
        } else if (view == btnMoveToHomeActivity) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }
    }

    public void animateTextView(TextView textView, String text, Runnable onComplete) {
        textView.setText("");
        Handler handler = new Handler();
        long delay = 50; // Delay in milliseconds between letters

        for (int i = 0; i < text.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> textView.append(String.valueOf(text.charAt(index))), delay * index);
        }

        if (onComplete != null) {
            handler.postDelayed(onComplete, delay * text.length());
        }
    }
}