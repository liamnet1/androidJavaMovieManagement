package com.example.LMDb;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

import com.bumptech.glide.Glide;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.widget.Toolbar;

public class UserDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    // UI elements for user details
    private EditText etNewYearOfBirth, etNewUserName, etNewEmail;
    private Button btnSaveChanges;
    private ImageView ivProfilePicture;
    private Button btnChooseProfilePicture;
    private Uri profileImageUri;

    // Drawer variables
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    // ActivityResultLauncher for picking an image from gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        profileImageUri = data.getData();
                        try {
                            Bitmap bitmap = ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(this.getContentResolver(), profileImageUri));
                            ivProfilePicture.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    // ActivityResultLauncher for taking a picture using the camera
    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getExtras() != null) {
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        ivProfilePicture.setImageBitmap(imageBitmap);
                    }
                } else {
                    Toast.makeText(this, "Camera action canceled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        // Initialize Drawer Menu
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        navigationView.getMenu().clear();
        if (auth.getCurrentUser() != null) {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_in);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_out);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.main) {
                startActivity(new Intent(UserDetailsActivity.this, MainActivity.class));
            } else if (id == R.id.home) {
                startActivity(new Intent(UserDetailsActivity.this, HomeActivity.class));
            } else if (id == R.id.moviesYouWatched) {
                Intent intent = new Intent(UserDetailsActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchedMovies");
                startActivity(intent);
            } else if (id == R.id.moviesYouLiked) {
                Intent intent = new Intent(UserDetailsActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "likedMovies");
                startActivity(intent);
            } else if (id == R.id.watchListMovies) {
                Intent intent = new Intent(UserDetailsActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "watchlist");
                startActivity(intent);
            } else if (id == R.id.upcomingMovies) {
                Intent intent = new Intent(UserDetailsActivity.this, YourCustomMoviesActivity.class);
                intent.putExtra("fragment", "upcomingMovies");
                startActivity(intent);
            } else if (id == R.id.userDetails) {
                // Already in this activity
            } else if (id == R.id.logOut) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(UserDetailsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserDetailsActivity.this, HomeActivity.class));
                recreate();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Handle back press to close drawer if open
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        // Initialize UserDetails UI and Logic
        // Apply slide-up animations to buttons
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Button btnSaveChangesAnim = findViewById(R.id.btnSaveChanges);
        Button btnChooseProfilePictureAnim = findViewById(R.id.btnChooseProfilePicture);
        btnSaveChangesAnim.startAnimation(slideUp);
        btnChooseProfilePictureAnim.startAnimation(slideUp);

        findViews();
        etNewEmail = findViewById(R.id.etNewEmail);
        etNewUserName = findViewById(R.id.etNewUsername);
        etNewYearOfBirth = findViewById(R.id.etNewYearOfBirth);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(this);
        btnChooseProfilePicture = findViewById(R.id.btnChooseProfilePicture);
        btnChooseProfilePicture.setOnClickListener(v -> showImagePickerDialog());

        // Load user details from Firestore
        loadDetails();
    }

    private void showImagePickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Profile Picture")
                .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0){
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                });
        builder.create().show();
    }

    private void openCamera() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onClick(View view) {
        String newUserName = etNewUserName.getText().toString().trim();
        String newYearOfBirth = etNewYearOfBirth.getText().toString().trim();

        if (newUserName.isEmpty() || newYearOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int yearOfBirth = Integer.parseInt(newYearOfBirth);

        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        String uid = fbAuth.getUid();
        FirebaseFirestore store = FirebaseFirestore.getInstance();

        if (profileImageUri != null) {
            store.collection("users").document(uid)
                    .update("userName", newUserName, "yob", yearOfBirth, "profileImageUrl", profileImageUri.toString())
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Details have been successfully saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save details. Please try again.", Toast.LENGTH_SHORT).show());
        } else {
            store.collection("users").document(uid)
                    .update("userName", newUserName, "yob", yearOfBirth)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Details have been successfully saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save details. Please try again.", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadDetails() {
        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        String uid = fbAuth.getUid();
        FirebaseFirestore store = FirebaseFirestore.getInstance();
        store.collection("users").document(uid)
                .get().addOnSuccessListener(documentSnapshot -> {
                    MyUser user = documentSnapshot.toObject(MyUser.class);
                    if (user != null) {
                        etNewEmail.setText(fbAuth.getCurrentUser().getEmail());
                        etNewUserName.setText(user.getUserName());
                        etNewYearOfBirth.setText(String.valueOf(user.getYob()));
                        if (user.getProfileImageUrl() != null) {
                            Glide.with(UserDetailsActivity.this)
                                    .load(user.getProfileImageUrl())
                                    .circleCrop() // This makes the loaded image circular
                                    .placeholder(R.drawable.default_pfp)
                                    .error(R.drawable.default_pfp)
                                    .into(ivProfilePicture);
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void findViews() {
        etNewEmail = findViewById(R.id.etEmail);
        etNewUserName = findViewById(R.id.etUsername);
        etNewYearOfBirth = findViewById(R.id.etYearOfBirth);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
    }
}