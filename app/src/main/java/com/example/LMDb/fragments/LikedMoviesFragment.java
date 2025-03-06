package com.example.LMDb.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.LMDb.HomeActivity;
import com.example.LMDb.R;
import com.example.LMDb.adapters.MoviesAdapter;
import com.example.LMDb.models.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LikedMoviesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LikedMoviesFragment extends Fragment implements MoviesAdapter.MovieOptionsListener {

    private RecyclerView recyclerViewMovies;
    private MoviesAdapter moviesAdapter;
    private TextView tvPageNumber;
    private Button btnNext, btnPrevious, btnNext5, btnPrevious5;

    private EditText etSearch;
    private Button btnSearch;

    private TextView tvNoMovies;
    private Button btnGoHomePage;

    // A local list to hold all retrieved movies
    private List<Movie> allMovies = new ArrayList<>();
    // Variables for pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private final int itemsPerPage = 20;

    public LikedMoviesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_liked_movies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize search views:
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);

        tvNoMovies = view.findViewById(R.id.tvNoMovies);
        btnGoHomePage = view.findViewById(R.id.btnGoHomePage);
        // Initialize UI elements
        recyclerViewMovies = view.findViewById(R.id.recyclerViewMovies);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnNext5 = view.findViewById(R.id.btnNext5);
        btnPrevious5 = view.findViewById(R.id.btnPrevious5);
        // Set up the RecyclerView
        recyclerViewMovies.setLayoutManager(new GridLayoutManager(getContext(), 2));
        moviesAdapter = new MoviesAdapter(getContext(), new ArrayList<>(), this, true,
                false, false);
        recyclerViewMovies.setAdapter(moviesAdapter);

        // Load saved movies from Firebase
        loadLikedMovies();

        btnGoHomePage.setOnClickListener(v -> {
            // Navigate to HomeActivity
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
        });

        // Set up the search button click listener
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) {
                filterMovies(query);
            } else {
                // If empty, show all movies (or reload the page)
                updatePage();
            }
        });

        // Set up pagination button click listeners
        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                updatePage();
            } else {
                Toast.makeText(getContext(), "No more pages", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                updatePage();
            }
        });

        btnNext5.setOnClickListener(v -> {
            int newPage = currentPage + 5;
            if (newPage >= totalPages) {
                currentPage = totalPages;
            } else {
                currentPage = newPage;
            }
            updatePage();
        });

        btnPrevious5.setOnClickListener(v -> {
            currentPage = Math.max(1, currentPage - 5);
            updatePage();
        });
    }

    private void loadLikedMovies() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).collection("favourites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allMovies = queryDocumentSnapshots.toObjects(Movie.class);
                    // Calculate total pages
                    totalPages = (int) Math.ceil((double) allMovies.size() / itemsPerPage);
                    currentPage = 1;
                    // Display textView and button if no movies are displayed
                    if (allMovies.isEmpty()) {
                        recyclerViewMovies.setVisibility(View.GONE);
                        tvNoMovies.setVisibility(View.VISIBLE);
                        btnGoHomePage.setVisibility(View.VISIBLE);
                    } else {
                        recyclerViewMovies.setVisibility(View.VISIBLE);
                        tvNoMovies.setVisibility(View.GONE);
                        btnGoHomePage.setVisibility(View.GONE);
                    }

                    updatePage();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load favourites", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterMovies(String query) {
        List<Movie> filtered = new ArrayList<>();
        for (Movie m : allMovies) {
            // Compare movie title
            if (m.getTitle().toLowerCase().contains(query)) {
                filtered.add(m);
            }
        }
        moviesAdapter.updateMovies(filtered);
    }

    private void updatePage() {
        // Update the displayed page number
        if (tvPageNumber != null) {
            tvPageNumber.setText(String.valueOf(currentPage));
        }
        // Calculate the subset indices
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(currentPage * itemsPerPage, allMovies.size());
        List<Movie> pageMovies = new ArrayList<>();
        if (fromIndex < toIndex) {
            pageMovies = allMovies.subList(fromIndex, toIndex);
        }
        moviesAdapter.updateMovies(pageMovies);

        // Scroll the RecyclerView to the top after updating the data
        recyclerViewMovies.scrollToPosition(0);

        // Update the button states
        btnNext.setEnabled(currentPage < totalPages);
        btnNext5.setEnabled(currentPage < totalPages);
        btnPrevious.setEnabled(currentPage > 1);
        btnPrevious5.setEnabled(currentPage > 1);
    }

    @Override
    public void onOptionSelected(Movie movie, MoviesAdapter.OptionType option) {
        if (option == MoviesAdapter.OptionType.REMOVE_FROM_FAVOURITES) {
            removeMovieFromFavourites(movie);
        }
    }

    private void removeMovieFromFavourites(Movie movie) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .collection("favourites")
                .document(String.valueOf(movie.getId()))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Removed from favourites", Toast.LENGTH_SHORT).show();
                    // Optionally reload the movies list:
                    loadLikedMovies();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to remove movie", Toast.LENGTH_SHORT).show();
                });
    }

    public static LikedMoviesFragment newInstance() {
        return new LikedMoviesFragment();
    }
}