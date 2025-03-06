package com.example.LMDb;

import static com.example.LMDb.network.ApiClient.API_KEY;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
// import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LMDb.adapters.MoviesAdapter;
import com.example.LMDb.models.Movie;
import com.example.LMDb.models.MovieResponse;
import com.example.LMDb.network.ApiClient;
import com.example.LMDb.network.TMDbApi;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.speech.RecognizerIntent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements MoviesAdapter.MovieOptionsListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // UI elements for API integration
    private RecyclerView recyclerViewMovies;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvPageNumber;
    private Button btnNext, btnPrevious, btnNext5, btnPrevious5;

    // API interface instance
    private TMDbApi tmdbApi;
    // RecyclerView adapter
    private MoviesAdapter moviesAdapter;

    // Variables to track pagination and current query
    private int currentPage = 1;
    private String currentQuery = "";

    // Variable of total pages from the search querry's content
    private int totalPages = 1;

    // Speech variables
    private Button btnSpeak;
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize the toolbar, drawer, and navigation view
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Set up the drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

        // Initialize the API interface from ApiClient
        tmdbApi = ApiClient.getClient().create(TMDbApi.class);
        // Fetch popular movies when HomeActivity starts
        fetchPopularMovies(1);

        // Set up a listener for menu item selection in the drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.main) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            } else if (id == R.id.home) {
                // Already in this activity
            } else if (id == R.id.signIn) {
                Intent intent = new Intent(HomeActivity.this, AuthenticationActivity.class);
                intent.putExtra("fragment", "signin");
                startActivity(intent);
            } else if (id == R.id.signUp) {
                Intent intent = new Intent(HomeActivity.this, AuthenticationActivity.class);
                intent.putExtra("fragment", "signup");
                startActivity(intent);
            } else if (id == R.id.moviesYouWatched) {
                Intent intent = new Intent(HomeActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchedMovies");
                startActivity(intent);
            } else if (id == R.id.moviesYouLiked) {
                Intent intent = new Intent(HomeActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "likedMovies");
                startActivity(intent);
            } else if (id == R.id.watchListMovies) {
                Intent intent = new Intent(HomeActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchlist");
                startActivity(intent);
            } else if (id == R.id.upcomingMovies) {
                Intent intent = new Intent(HomeActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "upcomingMovies");
                startActivity(intent);
            } else if (id == R.id.userDetails) {
                Intent intent = new Intent(HomeActivity.this, UserDetailsActivity.class);
                intent.putExtra("previousActivity", "AuthenticationActivity");
                startActivity(intent);
            } else if (id == R.id.logOut) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                // refresh the activity to update the menu
                recreate();
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

        // API integration setup

        // Initialize UI elements for search and RecyclerView
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerViewMovies = findViewById(R.id.recyclerViewMovies);
        tvPageNumber = findViewById(R.id.tvPageNumber);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext5 = findViewById(R.id.btnNext5);
        btnPrevious5 = findViewById(R.id.btnPrevious5);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnSpeak.getCompoundDrawables()[0].setTint(ContextCompat.getColor(this, R.color.white));

        // Set up RecyclerView (using a LinearLayoutManager)
        recyclerViewMovies.setLayoutManager(new GridLayoutManager(this, 2));
        // Initialize the adapter with an empty list that is updated when data comes in
        moviesAdapter = new MoviesAdapter(this, new ArrayList<>(), this, false,
                false, false);
        recyclerViewMovies.setAdapter(moviesAdapter);

        // Register ActivityResultLauncher for speech recognition
        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            // Declare and initialize recognizedText
                            String recognizedText = matches.get(0);
                            etSearch.setText(matches.get(0));
                            currentQuery = recognizedText.trim();
                            if (!currentQuery.isEmpty()) {
                                // Reset page to 1 and trigger the search automatically
                                currentPage = 1;
                                tvPageNumber.setText(String.valueOf(currentPage));
                                fetchMovies(currentQuery, currentPage);
                            }
                        }
                    }
                }
        );

        // Set the speak button click listener
        btnSpeak.setOnClickListener(v -> {
            // Create an intent for speech recognition
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
            try {
                speechRecognizerLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the Search button click listener to trigger an API call
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuery = etSearch.getText().toString().trim();
                if (!currentQuery.isEmpty()) {
                    // Reset to page 1 for a new search
                    currentPage = 1;
                    tvPageNumber.setText(String.valueOf(currentPage));
                    fetchMovies(currentQuery, currentPage);
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up pagination button click listeners
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < totalPages) {  // Only increment if there are more pages
                    currentPage++;
                    tvPageNumber.setText(String.valueOf(currentPage));
                    if (!currentQuery.isEmpty()) {
                        fetchMovies(currentQuery, currentPage);
                    } else {
                        fetchPopularMovies(currentPage);
                    }
                    updatePaginationButtons();  // Refresh button states
                } else {
                    // Show a message (buttons should be disabled)
                    Toast.makeText(HomeActivity.this, "No more pages", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentQuery.isEmpty()) {
                    if (currentPage > 1) {
                        currentPage--;
                    }
                    tvPageNumber.setText(String.valueOf(currentPage));
                    fetchMovies(currentQuery, currentPage);
                    updatePaginationButtons();
                } else if (currentPage > 1) {
                    currentPage--;
                    tvPageNumber.setText(String.valueOf(currentPage));
                    fetchPopularMovies(currentPage);
                    updatePaginationButtons();
                }
            }
        });

        btnNext5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calculate the intended new page
                int newPage = currentPage + 5;
                if (newPage >= totalPages) {
                    // If adding 5 exceeds total pages, jump to the final page
                    currentPage = totalPages;
                } else {
                    currentPage = newPage;
                }
                tvPageNumber.setText(String.valueOf(currentPage));
                if (!currentQuery.isEmpty()) {
                    fetchMovies(currentQuery, currentPage);
                } else {
                    fetchPopularMovies(currentPage);
                }
                updatePaginationButtons();  // Refresh button states
            }
        });

        btnPrevious5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentQuery.isEmpty()) {
                    currentPage = Math.max(1, currentPage - 5);
                    tvPageNumber.setText(String.valueOf(currentPage));
                    fetchMovies(currentQuery, currentPage);
                    updatePaginationButtons();
                } else {
                    currentPage = Math.max(1, currentPage - 5);
                    tvPageNumber.setText(String.valueOf(currentPage));
                    fetchPopularMovies(currentPage);
                    updatePaginationButtons();
                }
            }
        });
    }

    @Override
    public void onOptionSelected(Movie movie, MoviesAdapter.OptionType option) {
        // Handle the selection
        switch (option) {
            case FAVOURITE_AND_HISTORY:
                // Save movie as favourite and add to watch history in Firebase
                saveMovieToFirebase(movie, true, true);
                break;
            case HISTORY_ONLY:
                // Save movie only in watch history
                saveMovieToFirebase(movie, false, true);
                break;
            case WATCHLIST:
                // Save movie to the watchlist
                saveMovieToFirebase(movie, false, false);
                break;
        }
    }

    private void saveMovieToFirebase(Movie movie, boolean addToFavourites, boolean addToHistory) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (addToFavourites) {
            db.collection("users").document(uid).collection("favourites")
                    .document(String.valueOf(movie.getId()))
                    .set(movie)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to favourites", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add favourite", Toast.LENGTH_SHORT).show());
        }
        if (addToHistory) {
            db.collection("users").document(uid).collection("watchHistory")
                    .document(String.valueOf(movie.getId()))
                    .set(movie)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to watch history", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add watch history", Toast.LENGTH_SHORT).show());
        }
        if (!addToFavourites && !addToHistory) {  // WATCHLIST case
            db.collection("users").document(uid).collection("watchlist")
                    .document(String.valueOf(movie.getId()))
                    .set(movie)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to watchlist", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add to watchlist", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * @param query The search term entered by the user.
     * @param page  The page number to fetch (for pagination).
     */
    private void fetchMovies(String query, int page) {
        // Create the API call using the searchMovies method from TMDbApi interface
        TMDbApi apiService = ApiClient.getClient().create(TMDbApi.class);
        Call<MovieResponse> call = apiService.searchMovies(query, page, API_KEY);  // Use query from EditText
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Retrieve the total pages that the TMDb Api is providing
                    totalPages = response.body().getTotalPages();
                    // Retrieve the list of movies from the response
                    List<Movie> movies = response.body().getResults();
                    // Update the adapter with the new list of movies
                    moviesAdapter.updateMovies(movies);
                    // Scroll to the top after loading new results
                    recyclerViewMovies.scrollToPosition(0);
                    // calling updatePaginationButton when there are no more pages left
                    updatePaginationButtons();
                } else {
                    try {
                        String errorMsg = response.errorBody().string();
                        Toast.makeText(HomeActivity.this,
                                "Failed to retrieve movies: " + errorMsg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this,
                                "Failed to retrieve movies (unknown error)", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchPopularMovies(int page) {
        if (tmdbApi == null) {
            tmdbApi = ApiClient.getClient().create(TMDbApi.class);
        }

        Call<MovieResponse> call = tmdbApi.getPopularMovies(page, API_KEY);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Retrieve the total pages that the TMDb Api is providing
                    totalPages = response.body().getTotalPages();
                    // Retrieve the list of movies from the response
                    List<Movie> movies = response.body().getResults();
                    // Update the adapter with the new list of movies
                    moviesAdapter.updateMovies(movies);
                    // Scroll to the top after loading new results
                    recyclerViewMovies.scrollToPosition(0);
                    // calling updatePaginationButton when there are no more pages left
                    updatePaginationButtons();
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to retrieve popular movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updatePaginationButtons() {
        // Disable "Next" if currentPage is the final page, otherwise its enabled
        btnNext.setEnabled(currentPage < totalPages);
        // disable the "Back" and "-5" buttons if the current page is 1
        btnPrevious.setEnabled(currentPage > 1);
        btnPrevious5.setEnabled(currentPage > 1);
        // the +5 button is enabled only if adding 5 pages will not reach or exceed the final page
        // If currentPage is the final page, +5 is disabled
        if (currentPage >= totalPages) {
            btnNext5.setEnabled(false);
        } else {
            btnNext5.setEnabled(true);
        }
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