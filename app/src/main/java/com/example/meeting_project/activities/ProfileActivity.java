package com.example.meeting_project.activities;


import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.meeting_project.GlideAdapter.GlideApp;
import com.example.meeting_project.GlideAdapter.SvgSoftwareLayerSetter;
import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.MbtiBoundary;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.APIRequests.MbtiServiceApi;
import com.example.meeting_project.APIRequests.UserPreferencesApi;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.NevigationActivity;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private ImageButton menuButton;

    private ImageView profileImageView;
    private ImageView personalityImageView;
    private TextView tvFullName;
    private EditText etFirstName, etLastName, etPersonalityType, etEmail, etPhone, etDob, etGender;
    private Button btnEditProfile;


    private boolean isEditing = false;
    private UserBoundary currentUser;
    private String userId;
    private boolean isLoggedIn = false;
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
        AppManager.setContext(this.getApplicationContext());

        initViews();
        setSupportActionBar(toolbar);

        userId = UserSessionManager.getServerUserId(this);
        isLoggedIn = userId != null;
        isEditing = !isLoggedIn;
        if (isLoggedIn) {
            loadUserData();
            loadMbtiData();
        }

        setUpDrawer();
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        NevigationActivity.findNevigationButtens(this);
        loadUserData();
        AppManager.setContext(this.getApplicationContext());


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

    private void loadMbtiData() {
        MbtiServiceApi mbtiApi = User_ApiClient.getRetrofitInstance().create(MbtiServiceApi.class);
        mbtiApi.getProfileByUserId(userId)
                .enqueue(new Callback<MbtiBoundary>() {
                    @Override
                    public void onResponse(Call<MbtiBoundary> call, Response<MbtiBoundary> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String json = response.body().getCharacteristics();
                            SubmitResponse resp = new Gson().fromJson(json, SubmitResponse.class);
                            if (resp != null) {
                                // Load SVG avatar icon for personality type
                                RequestBuilder<PictureDrawable> rb = GlideApp.with(ProfileActivity.this)
                                        .as(PictureDrawable.class)
                                        .listener(new SvgSoftwareLayerSetter());
                                rb.load(resp.getAvatarSrcStatic())
                                        .placeholder(R.drawable.type_logo_placeholder)
                                        .into(personalityImageView);
                            }
                        }
                    }
                    @Override public void onFailure(Call<MbtiBoundary> call, Throwable t) {}
                });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        menuButton = findViewById(R.id.btn_menu);

        profileImageView = findViewById(R.id.profile_image);
        personalityImageView = findViewById(R.id.imagePersonalityLogo);
        tvFullName = findViewById(R.id.tv_full_name);

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
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
        String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
        tvFullName.setText(fullName);
        etFirstName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhoneNumber());
        etDob.setText(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
        etGender.setText(user.getGender().toString());
        etPersonalityType.setText(user.getMbtiType());
        // load profile picture
        Glide.with(this)
                .load(currentUser.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_placeholder_profile)
                .circleCrop()
                .into(profileImageView);
    }

    private void saveChanges() {
        if (currentUser == null) return;

        currentUser.setFirstName(etFirstName.getText().toString());
        currentUser.setLastName(etLastName.getText().toString());
        currentUser.setEmail(etEmail.getText().toString());
        currentUser.setPhoneNumber(etPhone.getText().toString());
        currentUser.setDateOfBirth(Date.valueOf(etDob.getText().toString()));
        currentUser.setGender(etGender.getText().toString());
        currentUser.setMbtiType(etPersonalityType.getText().toString());

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
