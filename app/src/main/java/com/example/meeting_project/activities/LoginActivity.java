package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.boundaries.UserResponse;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.interfaces.UserApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private ProgressBar progressBar;
    private TextView checkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar);
        checkTextView = findViewById(R.id.checkTextView);

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        checkTextView.setVisibility(View.GONE);

        if (email.isEmpty() || password.isEmpty()) {
            showError("נא למלא את כל השדות");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        UserApi apiService = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        Call<UserResponse> call = apiService.loginUser(email, password);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String userId = response.body().getId();
                    UserSessionManager.saveUserId(LoginActivity.this, userId);

                    Toast.makeText(LoginActivity.this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();

                    // מעבר למסך הבית
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class); // שים לב להתאים את השם
                    startActivity(intent);
                    finish();
                } else {
                    showError("האימייל או הסיסמה שגויים");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                showError("שגיאה בשרת: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        checkTextView.setText(message);
        checkTextView.setVisibility(View.VISIBLE);
    }
}
