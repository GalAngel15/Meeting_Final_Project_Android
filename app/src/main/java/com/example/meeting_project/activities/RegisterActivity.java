package com.example.meeting_project.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.boundaries.UserResponse;
import com.example.meeting_project.interfaces.UserApi;
import com.example.meeting_project.managers.ImageUploadManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.sql.Date;
import java.util.Calendar;

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
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    btnUploadImage.setImageURI(uri); // מציג בתצוגה
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        initViews();
        initButtons();
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
        String birthdateStr = editTextBirthdate.getText().toString().trim();
        if (gender == null) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validateInputs(username, email, password, confirmPassword, phone,birthdateStr)) {
            registerUser(email, password, username, gender, phone);
        }
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword, String phone, String birthdateStr) {
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
        if (TextUtils.isEmpty(birthdateStr)) {
            editTextBirthdate.setError("Birthdate is required");
            editTextBirthdate.requestFocus();
            return false;
        }
        return true;
    }

    private void registerUser(String email, String password, String username,String gender, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (selectedImageUri != null && user != null) {
                            ImageUploadManager.uploadProfileImage(this, selectedImageUri, user.getUid(), new ImageUploadManager.UploadCallback() {
                                @Override
                                public void onSuccess(String imageUrl) {
                                    updateUserProfile(username, imageUrl);
                                    saveUserToDatabase(email, username, password, gender, phone, imageUrl);
                                    navigateToMbtiQuizPage();
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                        }else {
                            updateUserProfile(username);
                            saveUserToDatabase(email, username, password, gender, phone);
                        }
//                        updateUserProfile(username);
//                        saveUserToDatabase(email, username, password, gender, phone);
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void saveUserToDatabase(String email, String username,String password,String gender, String phone) {
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
        java.sql.Date sqlBirthdate = new java.sql.Date(selectedBirthdate.getTimeInMillis());
        user.setDateOfBirth(sqlBirthdate);
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

    private void saveUserToDatabase(String email, String username,String password,String gender, String phone,String imageUrl) {
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
        java.sql.Date sqlBirthdate = new java.sql.Date(selectedBirthdate.getTimeInMillis());
        user.setDateOfBirth(sqlBirthdate);
        user.setProfilePhotoUrl(imageUrl); // אם יש לך שדה כזה

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
        }
    }

    private void updateUserProfile(String username, String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username);

            if (imageUrl != null) {
                builder.setPhotoUri(Uri.parse(imageUrl));
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