package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.interfaces.UserApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editTextUsername, editTextEmail;
    private TextInputLayout editTextPassword, editTextConfirmPassword;
    private MaterialButton buttonRegister, btnReturn;
    private RadioGroup genderGroup;

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
        buttonRegister = findViewById(R.id.register_button);
        btnReturn = findViewById(R.id.btnReturnToVisitPage);
        genderGroup = findViewById(R.id.gender_group);
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
    }

    private void setupRegisterButton() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getEditText().getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getEditText().getText().toString().trim();
        String gender = getSelectedGender();
        if (gender == null) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validateInputs(username, email, password, confirmPassword)) {
            registerUser(email, password, username, gender);
        }
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
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

        return true;
    }

    private void registerUser(String email, String password, String username,String gender) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUserProfile(username);
                        saveUserToDatabase(email, username, password, gender);
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void saveUserToDatabase(String email, String username,String password,String gender) {
        // נניח שאת מפרקת את ה-username לשם פרטי ושם משפחה (אפשר להתאים)
        String[] nameParts = username.split(" ", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // יצירת אובייקט UserBoundary
        UserBoundary user = new UserBoundary();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setGender(gender); // כאן הוספנו את המין!
        user.setPhoneNumber(null); // אפשר להוסיף בהמשך
        user.setPassword(password); // אין צורך להעביר סיסמה לשרת, מנוהלת בפיירבייס
        user.setMbtiId(null); // אפשר להוסיף בהמשך אחרי מבחן האישיות
        user.setProfilePhotoUrl(null); // אפשר להוסיף בהמשך
        user.setLocation(null); // אפשר להוסיף בהמשך
        user.setDateOfBirth(null); // אפשר להוסיף בהמשך
        user.setLikedUserIds(null);
        user.setMatchedUserIds(null);
        user.setPreferences(null); // אפשר להוסיף בהמשך

        // קריאה ל-UserApi
        UserApi apiService = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        Call<String> call = apiService.createUser(user);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("REGISTER", "User saved to database successfully");
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        UserSessionManager.saveUserId(RegisterActivity.this, uid);
                    }
                } else {
                    Log.e("REGISTER", "Failed to save user to database: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
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

}