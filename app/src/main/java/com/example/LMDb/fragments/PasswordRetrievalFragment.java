package com.example.LMDb.fragments;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.LMDb.AuthenticationActivity;
import com.example.LMDb.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PasswordRetrievalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PasswordRetrievalFragment extends Fragment {

    // Factory method parameters (if needed to pass parameters)
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // Required empty public constructor
    public PasswordRetrievalFragment() { }

    // Optional factory method
    public static PasswordRetrievalFragment newInstance(String param1, String param2) {
        PasswordRetrievalFragment fragment = new PasswordRetrievalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_retrieval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enable Edge-to-Edge display for the host activity
        EdgeToEdge.enable(requireActivity());

        // Apply window insets to the root view
        View mainView = view.findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Reset the scroll position
        mainView.post(() -> {
            if (mainView instanceof ScrollView) {
                ((ScrollView) mainView).scrollTo(0, 0);
            }
        });

        // Set up the "Back to Sign In" button
        Button btnBackToSignIn = view.findViewById(R.id.btnBackToSignIn);
        if (btnBackToSignIn != null) {
            btnBackToSignIn.setOnClickListener(v -> {
                if (getActivity() instanceof AuthenticationActivity) {
                    ((AuthenticationActivity) getActivity()).loadFragment(new SignInFragment());
                }
            });
        }

        // Set up the "Reset Password" functionality
        Button btnResetPassword = view.findViewById(R.id.btnResetPassword);
        if (btnResetPassword != null) {
            btnResetPassword.setOnClickListener(v -> {
                EditText emailEditText = view.findViewById(R.id.emailEditText);
                if (emailEditText == null) return; // Safety check
                String email = emailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required");
                    emailEditText.requestFocus();
                    return;
                }

                // Send password reset email using Firebase Authentication
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
    }
}