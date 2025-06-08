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

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
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
    private Toolbar toolbar;
    private ImageButton btnMenu;

    // Form fields
    private EditText editTextYearFrom, editTextYearTo;
    private RadioGroup radioGroupGender;
    private SeekBar seekBarDistance;
    private TextView textViewDistanceValue;
    private MaterialButton buttonSavePreferences;

    // API + user
    private UserPreferencesApi userPreferencesApi;
    private UserApi userApi;

    // State
    private String userId;
    private boolean isLoggedIn;
    private boolean isEditing;
    private ActionBarDrawerToggle toggle;

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

        // init APIs
        userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        userPreferencesApi = User_ApiClient.getRetrofitInstance().create(UserPreferencesApi.class);

        // determine login state
        userId = UserSessionManager.getServerUserId(this);
        isLoggedIn = userId != null;
        // default editing for guest, locked for logged user
        isEditing = !isLoggedIn;

        // update UI after checking login
        if (isLoggedIn) {
            loadUserData();
            isEditing=true; // allow editing for logged in users
            setRadioGroupEnabled(radioGroupGender, isEditing);
        } else {
            // guest flow
            configureNavigation();
            configureFormState();
        }
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

    // enable/disable fields and button text
    private void configureFormState() {
        editTextYearFrom.setEnabled(isEditing);
        editTextYearTo.setEnabled(isEditing);
        radioGroupGender.setEnabled(isEditing);
        for (int i = 0; i < radioGroupGender.getChildCount(); i++) {
            radioGroupGender.getChildAt(i).setEnabled(isEditing);
        }
        seekBarDistance.setEnabled(isEditing);

        if (isLoggedIn) {
            buttonSavePreferences.setText(isEditing ? "שמור העדפות" : "ערוך העדפות");
        } else {
            buttonSavePreferences.setText("שמור העדפות");
        }
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
    // fetch user profile to confirm session
    private void loadUserData() {
        userApi.getUserById(userId)
                .enqueue(new Callback<UserBoundary>() {
                    @Override
                    public void onResponse(Call<UserBoundary> call, Response<UserBoundary> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // valid session
                            isLoggedIn = true;
                            isEditing = false;
                        } else {
                            // session invalid
                            isLoggedIn = false;
                            isEditing = true;
                            Toast.makeText(activity_preferences.this,
                                    "שגיאה: לא נמצאה כניסה תקפה", Toast.LENGTH_LONG).show();
                        }
                        // update UI
                        configureNavigation();
                        configureFormState();
                    }
                    @Override
                    public void onFailure(Call<UserBoundary> call, Throwable t) {
                        // network or server error, treat as guest
                        isLoggedIn = false;
                        isEditing = true;
                        configureNavigation();
                        configureFormState();
                    }
                });
    }

    private void bindViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnMenu = findViewById(R.id.btn_menu);
        toolbar = findViewById(R.id.toolbar);
        editTextYearFrom = findViewById(R.id.editTextYearFrom);
        editTextYearTo = findViewById(R.id.editTextYearTo);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        seekBarDistance = findViewById(R.id.seekBarDistance);
        textViewDistanceValue = findViewById(R.id.textViewDistanceValue);
        buttonSavePreferences = findViewById(R.id.buttonSavePreferences);
    }

    // show/hide nav based on login
    private void configureNavigation() {
        if (isLoggedIn) {
            setSupportActionBar(toolbar);
            btnMenu.setVisibility(View.VISIBLE);
            navigationView.setVisibility(View.VISIBLE);

            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
            NevigationActivity.findNevigationButtens(this);

            toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        } else {
            btnMenu.setVisibility(View.GONE);
            navigationView.setVisibility(View.GONE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
        preferences.setId(userId);
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