package com.example.LMDb.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.LMDb.AuthenticationActivity;
import com.example.LMDb.HomeActivity;
import com.example.LMDb.MainActivity;
import com.example.LMDb.UserDetailsActivity;
import com.example.LMDb.YourCustomMoviesActivity;
import com.example.LMDb.R;
import com.example.LMDb.adapters.MoviesAdapter;
import com.example.LMDb.adapters.MoviesAdapter.MovieOptionsListener;
import com.example.LMDb.adapters.MoviesAdapter.OptionType;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomePageMenuHelper {
    // Sets up the drawer menu (toggle and navigation listener)
    public static void setupDrawer(AppCompatActivity activity, DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar) {
        // Set up the drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Inflate the proper drawer menu based on login status
        FirebaseAuth auth = FirebaseAuth.getInstance();
        navigationView.getMenu().clear();
        if (auth.getCurrentUser() != null) {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_in);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_out);
        }

        // Set up the navigation item selection listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.main) {
                activity.startActivity(new Intent(activity, MainActivity.class));
            } else if (id == R.id.home) {
                // Already in the current activity
            } else if (id == R.id.signIn) {
                Intent intent = new Intent(activity, AuthenticationActivity.class);
                intent.putExtra("fragment", "signin");
                activity.startActivity(intent);
            } else if (id == R.id.signUp) {
                Intent intent = new Intent(activity, AuthenticationActivity.class);
                intent.putExtra("fragment", "signup");
                activity.startActivity(intent);
            } else if (id == R.id.moviesYouWatched) {
                Intent intent = new Intent(activity, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchedMovies");
                activity.startActivity(intent);
            } else if (id == R.id.moviesYouLiked) {
                Intent intent = new Intent(activity, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "likedMovies");
                activity.startActivity(intent);
            } else if (id == R.id.watchListMovies) {
                Intent intent = new Intent(activity, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchlist");
                activity.startActivity(intent);
            } else if (id == R.id.upcomingMovies) {
                Intent intent = new Intent(activity, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "upcomingMovies");
                activity.startActivity(intent);
            } else if (id == R.id.userDetails) {
                Intent intent = new Intent(activity, UserDetailsActivity.class);
                intent.putExtra("previousActivity", "AuthenticationActivity");
                activity.startActivity(intent);
            } else if (id == R.id.logOut) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();
                activity.recreate();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // Sets up the refresh item in the options menu
    public static void setupRefreshMenu(AppCompatActivity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.menu_refresh_button, menu);
        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        if (refreshItem != null) {
            refreshItem.setVisible(true);
            Drawable drawable = refreshItem.getIcon();
            if (drawable != null) {
                drawable.setTint(ContextCompat.getColor(activity, android.R.color.white));
            }
        }
    }

    // Handles refresh menu item selection
    public static boolean handleRefreshMenuItem(AppCompatActivity activity, MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            activity.recreate();
            return true;
        }
        return false;
    }

    // Shows a popup menu for movie options
    public static void showMovieOptionsPopup(Context context,
                                             View anchor,
                                             MovieOptionsListener listener,
                                             com.example.LMDb.models.Movie movie,
                                             boolean showRemoveFavouriteOption,
                                             boolean showRemoveWatchlistOption,
                                             boolean showRemoveWatchHistoryOption) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.movie_save_options_menu, popup.getMenu());
        // Adjust visibility of remove options based on flags
        popup.getMenu().findItem(R.id.action_remove_from_favourites).setVisible(showRemoveFavouriteOption);
        popup.getMenu().findItem(R.id.action_remove_from_watchlist).setVisible(showRemoveWatchlistOption);
        popup.getMenu().findItem(R.id.action_remove_from_watch_history).setVisible(showRemoveWatchHistoryOption);
        popup.setOnMenuItemClickListener(item -> {
            OptionType selectedOption = null;
            int id = item.getItemId();
            if (id == R.id.action_fav_and_history) {
                selectedOption = OptionType.FAVOURITE_AND_HISTORY;
            } else if (id == R.id.action_history_only) {
                selectedOption = OptionType.HISTORY_ONLY;
            } else if (id == R.id.action_watchlist) {
                selectedOption = OptionType.WATCHLIST;
            } else if (id == R.id.action_remove_from_favourites) {
                selectedOption = OptionType.REMOVE_FROM_FAVOURITES;
            } else if (id == R.id.action_remove_from_watchlist) {
                selectedOption = OptionType.REMOVE_FROM_WATCHLIST;
            } else if (id == R.id.action_remove_from_watch_history) {
                selectedOption = OptionType.REMOVE_FROM_WATCH_HISTORY;
            }
            if (listener != null && selectedOption != null) {
                listener.onOptionSelected(movie, selectedOption);
            }
            return true;
        });
        popup.show();
    }
}
