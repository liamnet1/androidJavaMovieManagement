package com.example.LMDb.network;

import com.example.LMDb.models.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

// defines the API endpoints
//Retrofit Annotations: defining api endpoints and request parameters
public interface TMDbApi {
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("query") String query,
            @Query("page") int page,
            @Query("api_key") String apiKey
    );

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(
            @Query("page") int page,
            @Query("api_key") String apiKey
    );
}
