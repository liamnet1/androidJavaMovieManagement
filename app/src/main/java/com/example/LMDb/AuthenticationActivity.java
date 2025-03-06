package com.example.LMDb;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.LMDb.fragments.SignInFragment;
import com.example.LMDb.fragments.SignUpFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AuthenticationActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the DrawerLayout and NavigationView from the layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                 this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
         drawerLayout.addDrawerListener(toggle);
         toggle.syncState();

        // Set the correct menu based on login status
        FirebaseAuth auth = FirebaseAuth.getInstance();
        navigationView.getMenu().clear();
        if (auth.getCurrentUser() != null) {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_in);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_out);
        }

        // Set up a listener for navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.main) {
                startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
            } else if (id == R.id.home) {
                startActivity(new Intent(AuthenticationActivity.this, HomeActivity.class));
            } else if (id == R.id.signIn) {
                Intent intent = new Intent(AuthenticationActivity.this, AuthenticationActivity.class);
                intent.putExtra("fragment", "signin");
                startActivity(intent);
            } else if (id == R.id.signUp) {
                Intent intent = new Intent(AuthenticationActivity.this, AuthenticationActivity.class);
                intent.putExtra("fragment", "signup");
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish(); // Exit activity
                }
            }
        });

        // Read an extra to know which fragment to load (signin or signup)
        String fragmentToLoad = getIntent().getStringExtra("fragment");

        // Only loads the fragment if this is the first creation
        if (savedInstanceState == null) {
            Fragment fragment;
            if ("signup".equalsIgnoreCase(fragmentToLoad)) {
                fragment = new SignUpFragment();
            } else {
                // Default to SignInFragment if the extra is "signin" or missing
                fragment = new SignInFragment();
            }
            loadFragment(fragment);
        }
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.auth_fragment_container, fragment);
        transaction.commit();
    }
}