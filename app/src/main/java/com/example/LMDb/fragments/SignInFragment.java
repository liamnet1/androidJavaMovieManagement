package com.example.LMDb.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.LMDb.AuthenticationActivity;
import com.example.LMDb.HomeActivity;
import com.example.LMDb.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment implements View.OnClickListener {

    // Declare views and FirebaseAuth
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageView signInImage;
    private FirebaseAuth fbAuth;

    // Default public constructor
    public SignInFragment() {
        // Required empty public constructor
    }

    // Factory method to create a new instance (if needed to pass parameters)
    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    // Inflate the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    // Setup the views and logic after the layout is inflated
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enable Edge-to-Edge display
        EdgeToEdge.enable(requireActivity());

        // Set window insets on the root view to adjust for system bars
        View mainView = view.findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Reset the scroll position after the layout has been set
        mainView.post(() -> {
            Log.d("DEBUG", "Scrolling to top, current scrollY: " + ((ScrollView) mainView).getScrollY());
            if (mainView instanceof ScrollView) {
                ((ScrollView) mainView).scrollTo(0, 0);
                Log.d("DEBUG", "After scrolling, scrollY: " + ((ScrollView) mainView).getScrollY());
            }
        });
        // Set up switch button
        Button btnSwitchToSignUp = view.findViewById(R.id.btnSwitchToSignUp);
        btnSwitchToSignUp.setOnClickListener(v -> {
                    if (getActivity() instanceof AuthenticationActivity) {
                        ((AuthenticationActivity) getActivity()).loadFragment(new SignUpFragment());
                    }
        });
        // Set up switch button
        Button btnSwitchToPasswordRetrieval = view.findViewById(R.id.btnSwitchToPasswordRetrieval);
        btnSwitchToPasswordRetrieval.setOnClickListener(v -> {
            if (getActivity() instanceof AuthenticationActivity) {
                ((AuthenticationActivity) getActivity()).loadFragment(new PasswordRetrievalFragment());
            }
        });

        // Find views using the inflated view
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        // signInImage = view.findViewById(R.id.signInImage);

        // Initialize Firebase Authentication
        fbAuth = FirebaseAuth.getInstance();

        // Load and start the slide-up animation for the login button
        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
        btnLogin.startAnimation(slideUp);

        // Set the click listener on the login button
        btnLogin.setOnClickListener(this);
    }

    // Handle button clicks
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnLogin) {
            performLogin();
        }
    }

    // The performLogin() method
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        // Sign in using Firebase Authentication
        fbAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_LONG).show();
                            // Navigate to HomeActivity
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);
                            // Finish the hosting activity
                        } else {
                            // Login failed
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                            Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}