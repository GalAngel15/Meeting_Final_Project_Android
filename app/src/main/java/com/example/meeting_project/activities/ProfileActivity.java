package com.example.meeting_project.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.enums.Gender;
import com.example.meeting_project.interfaces.UserApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private ImageButton menuButton;
    private EditText etFirstName, etLastName, etPersonalityType, etEmail, etPhone, etDob, etGender;
    private Button btnEditProfile;
    private boolean isEditing = false;
    private UserBoundary currentUser;
    private static final Map<Integer, Class<?>> NAV_MAP = new HashMap<>();
    static {
        NAV_MAP.put(R.id.nav_edit_preferences, activity_preferences.class);
        NAV_MAP.put(R.id.nav_edit_intro, Activity_questionnaire.class);
        NAV_MAP.put(R.id.nav_my_personality, PersonalitiesActivity.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setUpDrawer();
        //setUpBottomNav();
        loadUserData();

        btnEditProfile.setOnClickListener(v -> {
            if (!isEditing) {
                enableFields(true);
                btnEditProfile.setText("Save");
                isEditing = true;
            } else {
                saveChanges();
                enableFields(false);
                btnEditProfile.setText("Edit Profile");
                isEditing = false;
            }
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        bottomNav = findViewById(R.id.bottom_nav);
        menuButton = findViewById(R.id.btn_menu);

        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etPersonalityType = findViewById(R.id.et_personality_type);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etDob = findViewById(R.id.et_dob);
        etGender = findViewById(R.id.et_gender);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
    }

    private void setUpDrawer() {
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> targetActivity = NAV_MAP.get(itemId);
            if (targetActivity != null) {
                startActivity(new Intent(ProfileActivity.this, targetActivity));
            } else {
                Toast.makeText(this, "אין פעולה מתאימה", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /*private void setUpBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(this, HomeActivity.class));
                    return true;
                case R.id.nav_profile:
                    return true;
                default:
                    return false;
            }
        });
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }*/

    private void enableFields(boolean enable) {
        etFirstName.setEnabled(enable);
        etLastName.setEnabled(enable);
        etPersonalityType.setEnabled(enable);
        etEmail.setEnabled(enable);
        etPhone.setEnabled(enable);
        etDob.setEnabled(enable);
        etGender.setEnabled(enable);
    }

    private void loadUserData() {
        // נניח שמזהה המשתמש כבר קיים במשתנה
        String userId = UserSessionManager.getServerUserId(this);
        UserApi userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        userApi.getUserById(userId).enqueue(new Callback<UserBoundary>() {
            @Override
            public void onResponse(Call<UserBoundary> call, Response<UserBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    bindUserData(currentUser);
                }
            }

            @Override
            public void onFailure(Call<UserBoundary> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "שגיאה בטעינת פרופיל", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindUserData(UserBoundary user) {
        etFirstName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhoneNumber());
        etDob.setText(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
        etGender.setText(user.getGender().toString());
        etPersonalityType.setText(user.getMbtiId());
    }

    private void saveChanges() {
        if (currentUser == null) return;

        currentUser.setFirstName(etFirstName.getText().toString());
        currentUser.setLastName(etLastName.getText().toString());
        currentUser.setEmail(etEmail.getText().toString());
        currentUser.setPhoneNumber(etPhone.getText().toString());
        currentUser.setDateOfBirth(Date.valueOf(etDob.getText().toString()));
        currentUser.setGender(etGender.getText().toString());
        currentUser.setMbtiId(etPersonalityType.getText().toString());

        UserApi userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        userApi.updateUser(currentUser, currentUser.getId()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "הפרופיל עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "שגיאה בשרת", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
