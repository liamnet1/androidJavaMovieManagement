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
import com.example.LMDb.helpers.HomePageMenuHelper;
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

    // UI elements
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerViewMovies;
    private EditText etSearch;
    private Button btnSearch, btnSpeak;
    private TextView tvPageNumber;
    private Button btnNext, btnPrevious, btnNext5, btnPrevious5;

    // API and Adapter
    private TMDbApi tmdbApi;
    private MoviesAdapter moviesAdapter;

    // Pagination & Query
    private int currentPage = 1;
    private String currentQuery = "";
    private int totalPages = 1;

    // Speech Recognition
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Setup toolbar, drawer and navigation using helper class
        setupToolbarAndDrawer();
        setupNavigationView();

        // Initialize UI elements and features
        initializeUI();
        setupRecyclerView();
        setupSpeechRecognition();
        setupSearchButton();
        setupPaginationButtons();

        // Fetch the initial popular movies
        fetchPopularMovies(1);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    finish();
                }
            }
        });
    }

    // Menu helper methods

    private void setupToolbarAndDrawer() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        // Delegate drawer setup to helper
        HomePageMenuHelper.setupDrawer(this, drawerLayout, navigationView, toolbar);
    }

    private void setupNavigationView() {

    }

    // Methods

    private void initializeUI() {
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerViewMovies = findViewById(R.id.recyclerViewMovies);
        tvPageNumber = findViewById(R.id.tvPageNumber);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext5 = findViewById(R.id.btnNext5);
        btnPrevious5 = findViewById(R.id.btnPrevious5);
        btnSpeak = findViewById(R.id.btnSpeak);
        // Tint the speech button's icon white
        btnSpeak.getCompoundDrawables()[0].setTint(ContextCompat.getColor(this, R.color.white));
    }

    private void setupRecyclerView() {
        recyclerViewMovies.setLayoutManager(new GridLayoutManager(this, 2));
        moviesAdapter = new MoviesAdapter(this, new ArrayList<>(), this, false, false, false);
        recyclerViewMovies.setAdapter(moviesAdapter);
    }

    private void setupSpeechRecognition() {
        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String recognizedText = matches.get(0);
                            etSearch.setText(recognizedText);
                            currentQuery = recognizedText.trim();
                            if (!currentQuery.isEmpty()) {
                                currentPage = 1;
                                tvPageNumber.setText(String.valueOf(currentPage));
                                fetchMovies(currentQuery, currentPage);
                            }
                        }
                    }
                }
        );
        btnSpeak.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
            try {
                speechRecognizerLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> {
            currentQuery = etSearch.getText().toString().trim();
            if (!currentQuery.isEmpty()) {
                currentPage = 1;
                tvPageNumber.setText(String.valueOf(currentPage));
                fetchMovies(currentQuery, currentPage);
            } else {
                Toast.makeText(HomeActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPaginationButtons() {
        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                tvPageNumber.setText(String.valueOf(currentPage));
                if (!currentQuery.isEmpty()) {
                    fetchMovies(currentQuery, currentPage);
                } else {
                    fetchPopularMovies(currentPage);
                }
                updatePaginationButtons();
            } else {
                Toast.makeText(HomeActivity.this, "No more pages", Toast.LENGTH_SHORT).show();
            }
        });
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                tvPageNumber.setText(String.valueOf(currentPage));
                if (!currentQuery.isEmpty()) {
                    fetchMovies(currentQuery, currentPage);
                } else {
                    fetchPopularMovies(currentPage);
                }
                updatePaginationButtons();
            }
        });
        btnNext5.setOnClickListener(v -> {
            int newPage = currentPage + 5;
            currentPage = (newPage >= totalPages) ? totalPages : newPage;
            tvPageNumber.setText(String.valueOf(currentPage));
            if (!currentQuery.isEmpty()) {
                fetchMovies(currentQuery, currentPage);
            } else {
                fetchPopularMovies(currentPage);
            }
            updatePaginationButtons();
        });
        btnPrevious5.setOnClickListener(v -> {
            currentPage = Math.max(1, currentPage - 5);
            tvPageNumber.setText(String.valueOf(currentPage));
            if (!currentQuery.isEmpty()) {
                fetchMovies(currentQuery, currentPage);
            } else {
                fetchPopularMovies(currentPage);
            }
            updatePaginationButtons();
        });
    }

    // API Methods

    private void fetchMovies(String query, int page) {
        TMDbApi apiService = ApiClient.getClient().create(TMDbApi.class);
        Call<MovieResponse> call = apiService.searchMovies(query, page, API_KEY);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalPages = response.body().getTotalPages();
                    List<Movie> movies = response.body().getResults();
                    moviesAdapter.updateMovies(movies);
                    recyclerViewMovies.scrollToPosition(0);
                    updatePaginationButtons();
                } else {
                    try {
                        String errorMsg = response.errorBody().string();
                        Toast.makeText(HomeActivity.this, "Failed to retrieve movies: " + errorMsg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this, "Failed to retrieve movies (unknown error)", Toast.LENGTH_SHORT).show();
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
                    totalPages = response.body().getTotalPages();
                    List<Movie> movies = response.body().getResults();
                    moviesAdapter.updateMovies(movies);
                    recyclerViewMovies.scrollToPosition(0);
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
        btnNext.setEnabled(currentPage < totalPages);
        btnPrevious.setEnabled(currentPage > 1);
        btnPrevious5.setEnabled(currentPage > 1);
        btnNext5.setEnabled(currentPage < totalPages);
    }

    // Options Menu and Refresh Button

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        HomePageMenuHelper.setupRefreshMenu(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (HomePageMenuHelper.handleRefreshMenuItem(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Movie Options Listener

    @Override
    public void onOptionSelected(Movie movie, MoviesAdapter.OptionType option) {
        switch (option) {
            case FAVOURITE_AND_HISTORY:
                saveMovieToFirebase(movie, true, true);
                break;
            case HISTORY_ONLY:
                saveMovieToFirebase(movie, false, true);
                break;
            case WATCHLIST:
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
        if (!addToFavourites && !addToHistory) {
            db.collection("users").document(uid).collection("watchlist")
                    .document(String.valueOf(movie.getId()))
                    .set(movie)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to watchlist", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add to watchlist", Toast.LENGTH_SHORT).show());
        }
    }
}