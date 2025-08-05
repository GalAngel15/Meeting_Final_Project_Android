package com.example.meeting_project.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.boundaries.UserResponse;
import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.ImageUploadManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editTextUsername, editTextEmail, editTextPhone;
    private TextInputLayout editTextPassword, editTextConfirmPassword;
    private MaterialButton buttonRegister, btnReturn;
    private RadioGroup genderGroup;
    private EditText editTextBirthdate;
    private Calendar selectedBirthdate;
    private ImageView btnUploadImage;
    //private Uri selectedImageUri = null;
    private EditText locationDisplay;
    private MaterialButton btnCurrentLocation, btnSelectOnMap;
    private FusedLocationProviderClient fusedLocationClient;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private boolean locationSelected = false;
    private Geocoder geocoder;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int MAX_IMAGES = 5;
    private final ArrayList<Uri> selectedImageUris = new ArrayList<>();

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    if (uris.size() > MAX_IMAGES) {
                        Toast.makeText(this, "You can select up to " + MAX_IMAGES + " images", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedImageUris.clear();
                    selectedImageUris.addAll(uris);

                    // לדוגמה: הצג את התמונה הראשונה כ-preview
                    btnUploadImage.setImageURI(uris.get(0));
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });
    private final ActivityResultLauncher<Intent> mapSelectionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedLatitude = data.getDoubleExtra("latitude", 0.0);
                    selectedLongitude = data.getDoubleExtra("longitude", 0.0);
                    String address = data.getStringExtra("address");

                    if (address != null) {
                        locationDisplay.setText(address);
                    } else {
                        locationDisplay.setText(String.format("Lat: %.4f, Lng: %.4f", selectedLatitude, selectedLongitude));
                    }
                    locationSelected = true;
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        initViews();
        initButtons();
        AppManager.setContext(this.getApplicationContext());

    }

    private void initViews() {
        editTextUsername = findViewById(R.id.full_name);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirm_password);
        editTextPhone = findViewById(R.id.phone);
        buttonRegister = findViewById(R.id.register_button);
        btnReturn = findViewById(R.id.btnReturnToVisitPage);
        genderGroup = findViewById(R.id.gender_group);
        editTextBirthdate = findViewById(R.id.birthdate);
        selectedBirthdate = Calendar.getInstance();
        btnUploadImage = findViewById(R.id.profile_placeholder);
        locationDisplay = findViewById(R.id.location_display);
        btnCurrentLocation = findViewById(R.id.btn_current_location);
        btnSelectOnMap = findViewById(R.id.btn_select_on_map);
    }
    private String getSelectedGender() {
        int selectedId = genderGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString(); // Male / Female
        }
        return null;
    }

    private void initButtons() {
        buttonRegister.setOnClickListener(v -> setupRegisterButton());
        btnReturn.setOnClickListener(v -> navigateToVisitPage());
        editTextBirthdate.setOnClickListener(v -> showDatePickerDialog());
        btnUploadImage.setOnClickListener(v -> uploadImages());
        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        btnSelectOnMap.setOnClickListener(v -> openMapSelection());
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // בקשת הרשאות
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        selectedLatitude = location.getLatitude();
                        selectedLongitude = location.getLongitude();
                        locationSelected = true;

                        // קבלת כתובת מהקואורדינטות
                        getAddressFromCoordinates(selectedLatitude, selectedLongitude);
                    } else {
                        Toast.makeText(this, "Unable to get current location. Please try again or select on map.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e("LOCATION", "Error getting location: " + e.getMessage());
                    Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                });
    }

    private void getAddressFromCoordinates(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                locationDisplay.setText(addressText);
            } else {
                locationDisplay.setText(String.format("Lat: %.4f, Lng: %.4f", latitude, longitude));
            }
        } catch (IOException e) {
            Log.e("GEOCODER", "Error getting address: " + e.getMessage());
            locationDisplay.setText(String.format("Lat: %.4f, Lng: %.4f", latitude, longitude));
        }
    }

    private void openMapSelection() {
        Intent intent = new Intent(this, MapSelectionActivity.class);
        mapSelectionLauncher.launch(intent);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Please select location on map.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(selectedYear, selectedMonth, selectedDay);

                    if (!isValidBirthdate(chosen)) {
                        showToast("You must be at least 18 years old and birthdate cannot be in the future");
                        return;
                    }

                    selectedBirthdate = chosen;
                    String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    editTextBirthdate.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }
    private boolean isValidBirthdate(Calendar birthdate) {
        Calendar today = Calendar.getInstance();

        if (birthdate.after(today)) {
            return false;
        }

        Calendar eighteenYearsAgo = Calendar.getInstance();
        eighteenYearsAgo.add(Calendar.YEAR, -18);

        return !birthdate.after(eighteenYearsAgo); // חייב להיות בן 18 ומעלה
    }

    private void setupRegisterButton() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = editTextPassword.getEditText().getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getEditText().getText().toString().trim();
        String gender = getSelectedGender();
        Date birthdateStr = new Date(selectedBirthdate.getTimeInMillis());
        if (gender == null) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.e("REGISTER", "setupRegisterButton called");
        if (validateInputs(username, email, password, confirmPassword, phone)) {
            Log.e("REGISTER", "Inputs are valid");
            registerUser(email, password, username, gender, phone, birthdateStr);
        }
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword, String phone) {
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Valid email is required");
            editTextEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches()) {
            editTextPhone.setError("Valid phone number is required");
            editTextPhone.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(editTextBirthdate.getText())) {
            editTextBirthdate.setError("Birthdate is required");
            editTextBirthdate.requestFocus();
            return false;
        }
        if (selectedImageUris == null || selectedImageUris.isEmpty()) {
            showToast("Please select at least one image");
            return false;
        }
        if (!locationSelected) {
            showToast("Please select your location");
            return false;
        }
        return true;
    }

    private void registerUser(String email, String password, String username, String gender, String phone, Date birthdateStr) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Log.e("REGISTER", "createUserWithEmail:onComplete:" + task.isSuccessful());
                    if (task.isSuccessful()) {
                        Log.e("REGISTER", "User registered successfully");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (!selectedImageUris.isEmpty() && user != null) {
                            ImageUploadManager.uploadMultipleImages(this, selectedImageUris, user.getUid(), new ImageUploadManager.MultipleUploadCallback() {
                                @Override
                                public void onSuccess(ArrayList<String> imageUrls) {
                                    Log.e("REGISTER", "Images uploaded successfully: " + imageUrls);
                                    updateUserProfile(username, imageUrls);
                                    saveUserToDatabase(email, username, password, gender, phone, imageUrls, birthdateStr);
                                    navigateToMbtiQuizPage();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    showToast("Failed to upload images");
                                }
                            });
                        }else {
                            updateUserProfile(username);
                            //saveUserToDatabase(email, username, password, gender, phone);
                        }
//                        updateUserProfile(username);
//                        saveUserToDatabase(email, username, password, gender, phone);
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void saveUserToDatabase(String email, String username,String password,String gender, String phone, Date birthdateStr) {
        // נניח שאת מפרקת את ה-username לשם פרטי ושם משפחה (אפשר להתאים)
        String[] nameParts = username.split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // יצירת אובייקט UserBoundary
        UserBoundary user = new UserBoundary();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setGender(gender);
        user.setPassword(password);
        user.setPhoneNumber(phone);
        user.setDateOfBirth(birthdateStr);
        // קריאה ל-UserApi
        UserApi apiService = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        Call<UserResponse> call = apiService.createUser(user);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    String userIdFromServer = userResponse.getId();
                    Log.d("REGISTER", "User saved to database with ID: " + userIdFromServer);
                    UserSessionManager.saveUserId(RegisterActivity.this, userIdFromServer);
                } else {
                    Log.e("REGISTER", "Failed to save user to database: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("REGISTER", "API call failed: " + t.getMessage(), t);
            }

        });
    }

    private void saveUserToDatabase(String email, String username,String password,String gender, String phone,List<String> imageUrls, Date birthdateStr) {
        // נניח שאת מפרקת את ה-username לשם פרטי ושם משפחה (אפשר להתאים)
        String[] nameParts = username.split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String formattedDate = sdf.format(birthdateStr);  // birthdateStr הוא java.util.Date
        Date sqlDate = Date.valueOf(formattedDate);        // הופך למדויק לשרת
        // יצירת אובייקט UserBoundary
        UserBoundary user = new UserBoundary();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setGender(gender);
        user.setPassword(password);
        user.setPhoneNumber(phone);
        user.setDateOfBirth(sqlDate);
        user.setLatitude(selectedLatitude);
        user.setLongitude(selectedLongitude);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            user.setGalleryUrls(imageUrls); // ודא שיש שדה כזה במחלקה
            user.setProfilePhotoUrl(imageUrls.get(0)); // אם יש לך שדה כזה
        }

        // קריאה ל-UserApi
        UserApi apiService = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        Call<UserResponse> call = apiService.createUser(user);

        Log.e("REGISTER", "Saving user to database with email: " + email);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    String userIdFromServer = userResponse.getId();
                    Log.e("REGISTER", "User saved to database with ID: " + userIdFromServer);
                    UserSessionManager.saveUserId(RegisterActivity.this, userIdFromServer);
                    UserSessionManager.saveFirebaseUserId(RegisterActivity.this, mAuth.getCurrentUser().getUid());
                } else {
                    Log.e("REGISTER", "Failed to save user to database: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("REGISTER", "API call failed: " + t.getMessage(), t);
            }

        });
    }
    private void updateUserProfile(String username) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            navigateToMbtiQuizPage();
                        } else {
                            showToast("Profile update failed: " + profileTask.getException().getMessage());
                        }
                    });
            UserSessionManager.saveFirebaseUserId(this, user.getUid());
        }
    }

    private void updateUserProfile(String username,  List<String> imageUrls) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username);

            if (imageUrls != null) {
                builder.setPhotoUri(Uri.parse(imageUrls.get(0))); // רק התמונה הראשונה מוצגת בפרופיל
            }

            user.updateProfile(builder.build())
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            showToast("Profile update failed");
                        }
                    });
        }
    }

    private void navigateToMbtiQuizPage() {
        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
        //Intent intent = new Intent(RegisterActivity.this, Activity_questionnaire.class);
        //Intent intent = new Intent(RegisterActivity.this, Activity_quiz_2.class);
        Intent intent = new Intent(RegisterActivity.this, TutorialForQuestions.class);
        startActivity(intent);
        finish();
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            showToast("User with this email already exists");
        } else {
            showToast("Registration failed: " + exception.getMessage());
        }
    }

    private void navigateToVisitPage() {
        Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void uploadImages() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }


}