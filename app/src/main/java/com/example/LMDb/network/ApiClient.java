package com.example.LMDb.network;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Network client class for making api calls
public class ApiClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3/"; // url to access the api
    public static final String API_KEY = "f604fabe1e91542555daa89594cd7afb"; // v3 API key

    private static Retrofit retrofit = null;

    // Retrofit: For making network requests
    public static Retrofit getClient() {
        if (retrofit == null) {
            // OkHttp: For HTTP client functionality
            OkHttpClient client = new OkHttpClient.Builder().build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
