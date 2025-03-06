package com.example.LMDb.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.LMDb.R;
import com.example.LMDb.models.Movie;

import java.util.List;

import android.widget.PopupMenu;
import android.widget.ImageButton;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> moviesList;
    private List<Movie> movies;

    private MovieOptionsListener optionsListener;

    private boolean showRemoveFavouriteOption;
    private boolean showRemoveWatchlistOption;
    private boolean showRemoveWatchHistoryOption;

    // Constructor (Initializes the adapter with context, movie list, options listener, and option flags)
    public MoviesAdapter(Context context, List<Movie> moviesList, MovieOptionsListener listener,
                         boolean showRemoveFavouriteOption, boolean showRemoveWatchlistOption,
                         boolean showRemoveWatchHistoryOption) {
        this.context = context;
        this.moviesList = moviesList;
        this.optionsListener = listener;
        this.showRemoveFavouriteOption = showRemoveFavouriteOption;
        this.showRemoveWatchlistOption = showRemoveWatchlistOption;
        this.showRemoveWatchHistoryOption = showRemoveWatchHistoryOption;
    }

    // Method to update the movie list
    public void updateMovies(List<Movie> movies) {
        this.moviesList.clear();
        this.moviesList.addAll(movies);
        this.movies = movies;
        notifyDataSetChanged();
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the custom layout for each movie item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = moviesList.get(position);
        holder.tvTitle.setText(movie.getTitle());
        // Load the movie poster using Glide
        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
        Glide.with(context)
                .load(posterUrl)
                .placeholder(R.drawable.no_image_available) // placeholder
                .into(holder.ivPoster);

        // Set up the options button's click listener
        holder.btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnOptions);
            popup.getMenuInflater().inflate(R.menu.movie_save_options_menu, popup.getMenu());
            // Set visibility of remove options according to the flags
            popup.getMenu().findItem(R.id.action_remove_from_favourites)
                    .setVisible(showRemoveFavouriteOption);
            popup.getMenu().findItem(R.id.action_remove_from_watchlist)
                    .setVisible(showRemoveWatchlistOption);
            popup.getMenu().findItem(R.id.action_remove_from_watch_history)
                    .setVisible(showRemoveWatchHistoryOption);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    OptionType selectedOption = null;
                    if (item.getItemId() == R.id.action_fav_and_history) {
                        selectedOption = OptionType.FAVOURITE_AND_HISTORY;
                    } else if (item.getItemId() == R.id.action_history_only) {
                        selectedOption = OptionType.HISTORY_ONLY;
                    } else if (item.getItemId() == R.id.action_watchlist) {
                        selectedOption = OptionType.WATCHLIST;
                    } else if (item.getItemId() == R.id.action_remove_from_favourites) {
                        selectedOption = MoviesAdapter.OptionType.REMOVE_FROM_FAVOURITES;
                    } else if (item.getItemId() == R.id.action_remove_from_watchlist) {
                        selectedOption = OptionType.REMOVE_FROM_WATCHLIST;
                    } else if (item.getItemId() == R.id.action_remove_from_watch_history) {
                        selectedOption = OptionType.REMOVE_FROM_WATCH_HISTORY;
                    } else {
                        return false;
                    }

                    if (optionsListener != null && selectedOption != null) {
                        optionsListener.onOptionSelected(movie, selectedOption);
                    }
                    return true;
                }
            });
            popup.show();
        });
    }

    // Returns the size of the movie list
    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    // ViewHolder class to hold references to each item's views
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public ImageView ivPoster;
        public ImageButton btnOptions;

        public MovieViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            ivPoster = itemView.findViewById(R.id.ivMoviePoster);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }

    // Callback interface for option selections
    public interface MovieOptionsListener {
        void onOptionSelected(Movie movie, OptionType option);
    }

    // Enum for the different option types
    public enum OptionType {
        FAVOURITE_AND_HISTORY,
        HISTORY_ONLY,
        WATCHLIST,
        REMOVE_FROM_FAVOURITES,
        REMOVE_FROM_WATCHLIST,
        REMOVE_FROM_WATCH_HISTORY
    }
}
