package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserPreferencesBoundary;
import com.example.meeting_project.enums.Gender;
import com.example.meeting_project.interfaces.UserPreferencesApi;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class activity_preferences extends AppCompatActivity {

    private EditText editTextYearFrom, editTextYearTo;
    private RadioGroup radioGroupGender;
    private SeekBar seekBarDistance;
    private TextView textViewDistanceValue;
    private MaterialButton buttonSavePreferences;
    private UserPreferencesApi userPreferencesApi;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        userId = UserSessionManager.getUserId(this);
        if (userId == null) {
            Toast.makeText(this, "שגיאה: לא נמצאה כניסה למשתמש", Toast.LENGTH_LONG).show();
            finish(); // או הכוונה למסך התחברות
            return;
        }

        editTextYearFrom = findViewById(R.id.editTextYearFrom);
        editTextYearTo = findViewById(R.id.editTextYearTo);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        seekBarDistance = findViewById(R.id.seekBarDistance);
        textViewDistanceValue = findViewById(R.id.textViewDistanceValue);
        buttonSavePreferences = findViewById(R.id.buttonSavePreferences);

        userPreferencesApi = User_ApiClient.getRetrofitInstance().create(UserPreferencesApi.class);

        // עדכון תצוגת המרחק לפי SeekBar
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewDistanceValue.setText(progress + " ק״מ");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        buttonSavePreferences.setOnClickListener(v -> savePreferencesToServer());
    }
    private void savePreferencesToServer() {
        String yearFromStr = editTextYearFrom.getText().toString();
        String yearToStr = editTextYearTo.getText().toString();

        if (yearFromStr.isEmpty() || yearToStr.isEmpty()) {
            Toast.makeText(this, "אנא מלא טווח גילאים", Toast.LENGTH_SHORT).show();
            return;
        }

        int yearFrom = Integer.parseInt(yearFromStr);
        int yearTo = Integer.parseInt(yearToStr);
        if (yearFrom > yearTo) {
            Toast.makeText(this, "שנת התחלה גדולה משנת סיום", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "אנא בחר מגדר מועדף", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String genderPreference = selectedGenderButton.toString().trim();
        Gender genderEnum= Gender.valueOf(genderPreference.toUpperCase());

        int maxDistance = seekBarDistance.getProgress();

        // יצירת אובייקט UserPreferencesBoundary
        UserPreferencesBoundary preferences = new UserPreferencesBoundary();
        preferences.setUserId(userId);
        preferences.setMinYear(yearFrom);
        preferences.setMaxYear(yearTo);
        preferences.setPreferredGender(genderEnum);
        preferences.setPreferredMaxDistanceKm(maxDistance);

        // שליחת העדפות לשרת
        Call<String> call = userPreferencesApi.createUserPreferences(preferences);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(activity_preferences.this, "העדפות נשמרו בהצלחה!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity_preferences.this, HomeActivity.class); // החלף בשם Activity הרלוונטי אם שונה
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(activity_preferences.this, "שגיאה בשמירה: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API_ERROR", "שגיאה: " + t.getMessage());
                Toast.makeText(activity_preferences.this, "העדפות לא נשמרו (שגיאה ברשת)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}