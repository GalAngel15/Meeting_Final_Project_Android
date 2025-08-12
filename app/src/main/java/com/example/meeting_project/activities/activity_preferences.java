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

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.APIRequests.UserPreferencesApi;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.boundaries.UserPreferencesBoundary;
import com.example.meeting_project.enums.Gender;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.BaseNavigationActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class activity_preferences extends BaseNavigationActivity {

    private EditText editTextYearFrom, editTextYearTo;
    private RadioGroup radioGroupGender;
    private SeekBar seekBarDistance;
    private TextView textViewDistanceValue;
    private MaterialButton buttonSavePreferences;

    private UserApi userApi;
    private UserPreferencesApi preferencesApi;
    private String userId;
    private boolean isLoggedIn;
    private boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_preferences);
        AppManager.setContext(getApplicationContext());

        initViews();


        // init APIs
        userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        preferencesApi = User_ApiClient.getRetrofitInstance().create(UserPreferencesApi.class);

        // determine login state and default editing
        userId = UserSessionManager.getServerUserId(this);
        isLoggedIn = userId != null;
        isEditing = !isLoggedIn; // guests edit immediately, logged users locked

        // configure navigation and form after login check
        configureFormState();

        // if logged in, fetch user to validate session then lock fields
        if (isLoggedIn) {
            userApi.getUserById(userId).enqueue(new Callback<UserBoundary>() {
                @Override
                public void onResponse(Call<UserBoundary> call, Response<UserBoundary> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isEditing = false; // lock after validation
                        fetchUserPreferences();
                    } else {
                        isLoggedIn = false;
                        isEditing = true;
                        Toast.makeText(activity_preferences.this, "Session invalid", Toast.LENGTH_LONG).show();
                    }
                    configureFormState();
                }
                @Override public void onFailure(Call<UserBoundary> call, Throwable t) {
                    isLoggedIn = false;
                    isEditing = true;
                    configureFormState();
                }
            });
        }

        // listeners
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                textViewDistanceValue.setText(p + " ק״מ");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        buttonSavePreferences.setOnClickListener(v -> {
            if (isLoggedIn && !isEditing) {
                isEditing = true;
                configureFormState();
            } else {
                savePreferences();
            }
        });
    }

    private void initViews() {
        editTextYearFrom = findViewById(R.id.editTextYearFrom);
        editTextYearTo = findViewById(R.id.editTextYearTo);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        seekBarDistance = findViewById(R.id.seekBarDistance);
        textViewDistanceValue = findViewById(R.id.textViewDistanceValue);
        buttonSavePreferences = findViewById(R.id.buttonSavePreferences);
    }

    private void configureFormState() {
        editTextYearFrom.setEnabled(isEditing);
        editTextYearTo.setEnabled(isEditing);
        for (int i = 0; i < radioGroupGender.getChildCount(); i++) {
            radioGroupGender.getChildAt(i).setEnabled(isEditing);
        }
        seekBarDistance.setEnabled(isEditing);
        buttonSavePreferences.setText(isLoggedIn ?
                (isEditing ? "שמור העדפות" : "ערוך העדפות") :
                "שמור העדפות");
        textViewDistanceValue.setText(seekBarDistance.getProgress() + " ק״מ");
    }

    private void savePreferences() {
        String fromStr = editTextYearFrom.getText().toString();
        String toStr = editTextYearTo.getText().toString();
        if (fromStr.isEmpty() || toStr.isEmpty()) {
            Toast.makeText(this, "אנא מלא טווח גילאים", Toast.LENGTH_SHORT).show(); return;
        }
        int from = Integer.parseInt(fromStr), to = Integer.parseInt(toStr);
        if (from > to) { Toast.makeText(this, "טווח לא חוקי", Toast.LENGTH_SHORT).show(); return; }
        int gid = radioGroupGender.getCheckedRadioButtonId();
        if (gid == -1) { Toast.makeText(this, "בחר מגדר", Toast.LENGTH_SHORT).show(); return; }
        Gender g = Gender.valueOf((String)((RadioButton)findViewById(gid)).getTag());
        int dist = seekBarDistance.getProgress();

        UserPreferencesBoundary prefs = new UserPreferencesBoundary();
        prefs.setUserId(userId);
        prefs.setMinYear(from);
        prefs.setMaxYear(to);
        prefs.setPreferredGender(g);
        prefs.setPreferredMaxDistanceKm(dist);

        preferencesApi.createUserPreferences(prefs).enqueue(new Callback<UserPreferencesBoundary>() {
            @Override
            public void onResponse(Call<UserPreferencesBoundary> call, Response<UserPreferencesBoundary> resp) {
                if (resp.isSuccessful()) {
                    Toast.makeText(activity_preferences.this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                    if (isLoggedIn) {
                        isEditing = false;
                        configureFormState();
                    } else {
                        startActivity(new Intent(activity_preferences.this, HomeActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(activity_preferences.this, "שגיאה: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<UserPreferencesBoundary> call, Throwable t) {
                Log.e("PrefsErr", t.getMessage());
                Toast.makeText(activity_preferences.this, "שגיאה ברשת", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserPreferences() {
        preferencesApi.getUserPreferencesByUserId(userId).enqueue(new Callback<UserPreferencesBoundary>() {
            @Override
            public void onResponse(Call<UserPreferencesBoundary> call, Response<UserPreferencesBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populatePreferencesFields(response.body());
                } else {
                    Log.d("Preferences", "No preferences found or failed to load");
                }
            }

            @Override
            public void onFailure(Call<UserPreferencesBoundary> call, Throwable t) {
                Log.e("Preferences", "Failed to fetch preferences: " + t.getMessage());
            }
        });
    }

    private void populatePreferencesFields(UserPreferencesBoundary prefs) {
        editTextYearFrom.setText(String.valueOf(prefs.getMinYear()));
        editTextYearTo.setText(String.valueOf(prefs.getMaxYear()));
        seekBarDistance.setProgress(prefs.getPreferredMaxDistanceKm() != null ? prefs.getPreferredMaxDistanceKm() : 0);
        textViewDistanceValue.setText(seekBarDistance.getProgress() + " ק״מ");

        // הגדרת כפתור המגדר לפי העדפת המשתמש
        Gender gender = prefs.getPreferredGender();
        if (gender != null) {
            for (int i = 0; i < radioGroupGender.getChildCount(); i++) {
                View child = radioGroupGender.getChildAt(i);
                if (child instanceof RadioButton) {
                    RadioButton radio = (RadioButton) child;
                    if (radio.getTag() != null && radio.getTag().equals(gender.name())) {
                        radio.setChecked(true);
                        break;
                    }
                }
            }
        }
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_preferences;
    }

    @Override
    protected int getDrawerMenuItemId() {
        return R.id.nav_edit_preferences;
    }

    @Override
    protected int getBottomMenuItemId() {
        return 0;
    }

    @Override
    protected String getCurrentUserId() {
        return userId;
    }
}
