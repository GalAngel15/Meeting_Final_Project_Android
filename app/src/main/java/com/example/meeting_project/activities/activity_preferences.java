package com.example.meeting_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserPreferencesBoundary;
import com.example.meeting_project.enums.Gender;
import com.example.meeting_project.APIRequests.UserPreferencesApi;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.NevigationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class activity_preferences extends AppCompatActivity {

    // Navigation
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    // Form fields
    private EditText editTextYearFrom, editTextYearTo;
    private RadioGroup radioGroupGender;
    private SeekBar seekBarDistance;
    private TextView textViewDistanceValue;
    private MaterialButton buttonSavePreferences;

    // API + user
    private UserPreferencesApi userPreferencesApi;
    private String userId;

    // State
    private boolean isLoggedIn;
    private boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        AppManager.setContext(this.getApplicationContext());

        // check if user is logged in
        userId = UserSessionManager.getServerUserId(this);
        isLoggedIn = userId != null;
        isEditing = !isLoggedIn;

        bindViews();
        configureNavigation();
        configureFormState();
        setupListeners();

        userId = UserSessionManager.getServerUserId(this);
        if (userId == null) {
            Toast.makeText(this, "שגיאה: לא נמצאה כניסה למשתמש", Toast.LENGTH_LONG).show();
            finish(); // או הכוונה למסך התחברות
            return;
        }
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

    private void configureFormState() {
            // הגדרת מצב עריכה
            editTextYearFrom.setEnabled(isEditing);
            editTextYearTo.setEnabled(isEditing);
            setRadioGroupEnabled(radioGroupGender, isEditing);
            seekBarDistance.setEnabled(isEditing);

            // כפתור עריכה/שמירה
            if (isLoggedIn) {
                buttonSavePreferences.setText(isEditing ? "שמור העדפות" : "ערוך העדפות");
            } else {
                buttonSavePreferences.setText("שמור העדפות");
            }

            // הצגת ערך התחלתי של מרחק
            textViewDistanceValue.setText(seekBarDistance.getProgress() + " ק״מ");
    }

    private void setupListeners() {
        // שינוי תצוגת מרחק בזמן אמת
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewDistanceValue.setText(progress + " ק״מ");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // לחיצה על כפתור עריכה/שמירה
        buttonSavePreferences.setOnClickListener(v -> {
            if (isLoggedIn && !isEditing) {
                // מעבר למצב עריכה
                isEditing = true;
                configureFormState();
            } else {
                // שמירת הערכים לשרת
                savePreferencesToServer();
            }
        });
    }

    private void setRadioGroupEnabled(RadioGroup group, boolean enabled) {
        group.setEnabled(enabled);
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
        }
    }

    private void bindViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnMenu = findViewById(R.id.btn_menu);

        editTextYearFrom = findViewById(R.id.editTextYearFrom);
        editTextYearTo = findViewById(R.id.editTextYearTo);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        seekBarDistance = findViewById(R.id.seekBarDistance);
        textViewDistanceValue = findViewById(R.id.textViewDistanceValue);
        buttonSavePreferences = findViewById(R.id.buttonSavePreferences);
    }

    private void configureNavigation() {
        if (isLoggedIn) {
            // מופיע רק כשמחובר
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            navigationView.setVisibility(View.VISIBLE);
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
            NevigationActivity.findNevigationButtens(this);
            // פתיחת ה-Drawer
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
            btnMenu.setVisibility(View.VISIBLE);
        } else {
            // מופיע רק כשלא מחובר
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            navigationView.setVisibility(View.GONE);
            btnMenu.setVisibility(View.GONE);
        }
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
        String genderTag = (String) selectedGenderButton.getTag();
        if (genderTag == null) {
            Toast.makeText(this, "שגיאה: לא ניתן לזהות מגדר", Toast.LENGTH_SHORT).show();
            return;
        }
        Gender genderEnum = Gender.valueOf(genderTag);

        int maxDistance = seekBarDistance.getProgress();

        // יצירת אובייקט UserPreferencesBoundary
        UserPreferencesBoundary preferences = new UserPreferencesBoundary();
        preferences.setId(null);
        preferences.setUserId(userId);
        preferences.setMinYear(yearFrom);
        preferences.setMaxYear(yearTo);
        preferences.setPreferredGender(genderEnum);
        preferences.setPreferredMaxDistanceKm(maxDistance);

        // שליחת העדפות לשרת
        Call<UserPreferencesBoundary> call = userPreferencesApi.createUserPreferences(preferences);
        call.enqueue(new Callback<UserPreferencesBoundary>() {
            @Override
            public void onResponse(Call<UserPreferencesBoundary> call, Response<UserPreferencesBoundary> response) {
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
            public void onFailure(Call<UserPreferencesBoundary> call, Throwable t) {
                Log.e("API_ERROR", "שגיאה: " + t.getMessage());
                Toast.makeText(activity_preferences.this, "העדפות לא נשמרו (שגיאה ברשת)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}