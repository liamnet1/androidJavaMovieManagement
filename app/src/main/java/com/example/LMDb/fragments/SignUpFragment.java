package com.example.LMDb.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.LMDb.AuthenticationActivity;
import com.example.LMDb.HomeActivity;
import com.example.LMDb.MyUser;
import com.example.LMDb.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpFragment extends Fragment implements View.OnClickListener {

    // Declare view objects and FirebaseAuth
    private EditText etUsername, etEmail, etPassword, etConfirmPassword, etYearOfBirth;
    private Button btnSignUp;
    private FirebaseAuth fbAuth;

    // --- Factory method ---
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    // Required empty public constructor
    public SignUpFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Process fragment arguments if needed
        if (getArguments() != null) {
            // Retrieve parameters if necessary
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enable edge-to-edge display on the hosting activity
        EdgeToEdge.enable(requireActivity());

        // Adjust the padding for system window insets
        View mainView = view.findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Reset the scroll position after layout has been set
        mainView.post(() -> {
            if (mainView instanceof android.widget.ScrollView) {
                ((android.widget.ScrollView) mainView).scrollTo(0, 0);
            }
        });
        // Set up the switch button
        Button btnSwitchToSignIn = view.findViewById(R.id.btnSwitchToSignIn);
        btnSwitchToSignIn.setOnClickListener(v -> {
            if (getActivity() instanceof AuthenticationActivity) {
                ((AuthenticationActivity) getActivity()).loadFragment(new SignInFragment());
            }
        });

        // Find views using the inflated view
        findViews(view);

        // Load and start the slide up animation on the Sign Up button
        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
        btnSignUp.startAnimation(slideUp);

        // Set the click listener for the Sign Up button
        btnSignUp.setOnClickListener(this);

        // Initialize Firebase Authentication
        fbAuth = FirebaseAuth.getInstance();
    }

    // Helper method to find all necessary views from the layout
    private void findViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        etUsername = view.findViewById(R.id.etUsername);
        etYearOfBirth = view.findViewById(R.id.etYearOfBirth);
        btnSignUp = view.findViewById(R.id.btnSignUp);
    }

    // Handle button clicks
    @Override
    public void onClick(View view) {
        if (view == btnSignUp) {
            // Get input values
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();
            final String confirmPassword = etConfirmPassword.getText().toString().trim();
            final String userName = etUsername.getText().toString().trim();
            final String yearOfBirth = etYearOfBirth.getText().toString().trim();

            // Validate input fields
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(userName) || TextUtils.isEmpty(yearOfBirth)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
                return;
            }
            // Check if password and confirm password match
            if (!password.equals(confirmPassword)) {
                // An error on the confirm password field
                etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user using Firebase Authentication
            fbAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // User creation was successful
                                try {
                                    int yob = Integer.parseInt(yearOfBirth);
                                    MyUser user = new MyUser(userName, yob);

                                    // Save user data in Firestore
                                    FirebaseFirestore store = FirebaseFirestore.getInstance();
                                    store.collection("users").document(fbAuth.getUid())
                                            .set(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "User Created", Toast.LENGTH_LONG).show();
                                                        // Navigate to HomeActivity
                                                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        String errorMessage = task.getException() != null
                                                                ? task.getException().getMessage()
                                                                : "Error saving user data.";
                                                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                } catch (NumberFormatException e) {
                                    // Handle invalid year of birth input
                                    Toast.makeText(getContext(), "Invalid year of birth", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // User creation failed
                                String errorMessage = task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Error creating user.";
                                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}