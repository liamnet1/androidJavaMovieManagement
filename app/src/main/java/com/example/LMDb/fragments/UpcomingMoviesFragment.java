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
import android.widget.TextView;

import com.example.LMDb.HomeActivity;
import com.example.LMDb.R;
import com.example.LMDb.adapters.MoviesAdapter;
import com.example.LMDb.models.Movie;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UpcomingMoviesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpcomingMoviesFragment extends Fragment {

    private RecyclerView recyclerViewMovies;
    private MoviesAdapter moviesAdapter;

    private TextView tvNoMovies;
    private Button btnGoHomePage;

    public UpcomingMoviesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_upcoming_movies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNoMovies = view.findViewById(R.id.tvNoMovies);
        btnGoHomePage = view.findViewById(R.id.btnGoHomePage);

        // Initialize the RecyclerView and adapter
        recyclerViewMovies = view.findViewById(R.id.recyclerViewMovies);
        recyclerViewMovies.setLayoutManager(new GridLayoutManager(getContext(), 2));
        moviesAdapter = new MoviesAdapter(getContext(), new ArrayList<>(), null, false,
                false, false);
        recyclerViewMovies.setAdapter(moviesAdapter);

        btnGoHomePage.setOnClickListener(v -> {
            // Navigate to HomeActivity
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
        });
    }

    public static UpcomingMoviesFragment newInstance() {
        return new UpcomingMoviesFragment();
    }
}