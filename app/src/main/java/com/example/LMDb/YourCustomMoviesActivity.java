package com.example.LMDb;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.LMDb.fragments.LikedMoviesFragment;
import com.example.LMDb.fragments.MoviesWatchListFragment;
import com.example.LMDb.fragments.SignInFragment;
import com.example.LMDb.fragments.SignUpFragment;
import com.example.LMDb.fragments.UpcomingMoviesFragment;
import com.example.LMDb.fragments.WatchedMoviesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class YourCustomMoviesActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_custom_movies);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Firebase Authentication for Drawer Menu
        FirebaseAuth auth = FirebaseAuth.getInstance();
        navigationView.getMenu().clear();
        if (auth.getCurrentUser() != null) {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_in);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_out);
        }

        // Drawer Navigation Listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.main) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.home) {
                startActivity(new Intent(this, HomeActivity.class));
            } else if (id == R.id.moviesYouWatched) {
                loadFragment(new WatchedMoviesFragment());
            } else if (id == R.id.moviesYouLiked) {
                loadFragment(new LikedMoviesFragment());
            } else if (id == R.id.watchListMovies) {
                loadFragment(new MoviesWatchListFragment());
            } else if (id == R.id.userDetails) {
                Intent intent = new Intent(this, UserDetailsActivity.class);
                intent.putExtra("previousActivity", "AuthenticationActivity");
                startActivity(intent);
            } else if (id == R.id.logOut) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                recreate();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Determine which fragment to load from the intent extra
        Fragment defaultFragment;
        String fragmentToLoad = getIntent().getStringExtra("fragment");
        if (fragmentToLoad != null) {
            String key = fragmentToLoad.toLowerCase();
            if (key.equals("watchedmovies")) {
                defaultFragment = new WatchedMoviesFragment();
            } else if (key.equals("watchlist")) {
                defaultFragment = new MoviesWatchListFragment();
            } else if (key.equals("likedmovies")) {
                defaultFragment = new LikedMoviesFragment();
            } else if (key.equals("upcomingmovies")) {
                defaultFragment = new UpcomingMoviesFragment();
            } else {
                defaultFragment = new LikedMoviesFragment();
            }
        } else {
            defaultFragment = new LikedMoviesFragment();
        }

        // Load the fragment if the container is empty
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            loadFragment(defaultFragment);
        }

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.navigation_likedMovies) {
                selectedFragment = new LikedMoviesFragment();
            } else if (item.getItemId() == R.id.navigation_watchlist) {
                selectedFragment = new MoviesWatchListFragment();
            } else if (item.getItemId() == R.id.navigation_watchedMovies) {
                selectedFragment = new WatchedMoviesFragment();
            } else if (item.getItemId() == R.id.navigation_upcomingMovies) {
                selectedFragment = new UpcomingMoviesFragment();
            }
            return loadFragment(selectedFragment);
        });

        // Set bottom navigation's selected item based on the extra
        if (fragmentToLoad != null) {
            String key = fragmentToLoad.toLowerCase();
            if (key.equals("watchedmovies")) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_watchedMovies);
            } else if (key.equals("watchlist")) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_watchlist);
            } else if (key.equals("likedmovies")) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_likedMovies);
            } else if (key.equals("upcomingmovies")) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_upcomingMovies);
            }
        }

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
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh_button, menu);

        // Find the refresh button and make it visible ONLY in this activity
        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        if (refreshItem != null) {
            refreshItem.setVisible(true);
            Drawable drawable = refreshItem.getIcon();
            if (drawable != null) {
                drawable.setTint(getResources().getColor(android.R.color.white, getTheme())); // Force white tint
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            // Handle the refresh action
            recreate(); // Restarts the activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}