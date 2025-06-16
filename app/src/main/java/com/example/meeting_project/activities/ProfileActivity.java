package com.example.meeting_project.activities;

import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.meeting_project.APIRequests.MbtiServiceApi;
import com.example.meeting_project.GlideAdapter.GlideApp;
import com.example.meeting_project.GlideAdapter.SvgSoftwareLayerSetter;
import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.MbtiService_ApiClient;
import com.example.meeting_project.apiClients.User_ApiClient;
import com.example.meeting_project.boundaries.UserBoundary;
import com.example.meeting_project.boundaries.MbtiBoundary;
import com.example.meeting_project.APIRequests.UserApi;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage, personalityImage;
    private TextView tvFullName;
    private EditText etFirstName, etLastName, etPersonalityType, etEmail, etPhone, etDob, etGender;
    private Button btnEditProfile;
    private ImageButton btnMenu;
    private DrawerLayout drawerLayout;

    private UserApi userApi;
    private boolean isEditMode = false;  // מעקב אחר מצב העריכה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupDrawer();
        setupEditButton();

        userApi = User_ApiClient.getRetrofitInstance().create(UserApi.class);
        String userId = UserSessionManager.getServerUserId(this);
        if (userId != null) {
            fetchUserProfile(userId);
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        tvFullName = findViewById(R.id.tv_full_name);
        profileImage = findViewById(R.id.profile_image);
        personalityImage = findViewById(R.id.iv_personality_type);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etPersonalityType = findViewById(R.id.et_personality_type);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etDob = findViewById(R.id.et_dob);
        etGender = findViewById(R.id.et_gender);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnMenu = findViewById(R.id.btn_menu);
        drawerLayout = findViewById(R.id.drawer_layout);

        // כברירת מחדל - השדות נעולים לעריכה
        setFieldsEditable(false);
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
    }

    private void setupEditButton() {
        btnEditProfile.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            setFieldsEditable(isEditMode);
            btnEditProfile.setText(isEditMode ? "Save" : "Edit Profile");
            if (!isEditMode) {
                saveProfileChanges();  // בעתיד כאן תוכל לשמור לשרת
            }
        });
    }

    private void setFieldsEditable(boolean editable) {
        etFirstName.setEnabled(editable);
        etLastName.setEnabled(editable);
        etPersonalityType.setEnabled(false); // שדה זה תמיד לא ניתן לעריכה
        etEmail.setEnabled(editable);
        etPhone.setEnabled(editable);
        etDob.setEnabled(editable);
        etGender.setEnabled(editable);
    }

    private void saveProfileChanges() {
        // כאן אפשר יהיה להוסיף שמירה לשרת בעתיד (כרגע השארתי ריק)
        Toast.makeText(this, "Profile updated locally", Toast.LENGTH_SHORT).show();
    }

    private void fetchUserProfile(String userId) {
        Call<UserBoundary> call = userApi.getUserById(userId);
        call.enqueue(new Callback<UserBoundary>() {
            @Override
            public void onResponse(Call<UserBoundary> call, Response<UserBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserBoundary> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(UserBoundary user) {
        loadUserData(user);
        loadProfileImage(user);
        loadMbtiData(user.getId());
    }

    private void loadUserData(UserBoundary user) {
        tvFullName.setText(user.getFirstName() + " " + user.getLastName());
        etFirstName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
        etPersonalityType.setText(user.getMbtiType());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhoneNumber());
        etDob.setText(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
        etGender.setText(user.getGender());
    }

    private void loadProfileImage(UserBoundary user) {
        List<String> galleryUrls = user.getGalleryUrls();
        if (galleryUrls != null && !galleryUrls.isEmpty()) {
            Glide.with(this)
                    .load(galleryUrls.get(0))
                    .placeholder(R.drawable.ic_placeholder_profile)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_placeholder_profile);
        }
    }

    private void loadMbtiData(String userId) {
        MbtiServiceApi mbtiApi = MbtiService_ApiClient.getRetrofitInstance().create(MbtiServiceApi.class);
        mbtiApi.getProfileByUserId(userId)
                .enqueue(new Callback<MbtiBoundary>() {
                    @Override
                    public void onResponse(Call<MbtiBoundary> call, Response<MbtiBoundary> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String characteristicsJson = response.body().getCharacteristics();
                            Log.e("characteristicsJson", characteristicsJson);

                            SubmitResponse submitResponse = new Gson().fromJson(characteristicsJson, SubmitResponse.class);
                            if (submitResponse != null && submitResponse.getAvatarSrcStatic() != null) {
                                loadSvgImage(submitResponse.getAvatarSrcStatic(), personalityImage);
                            } else {
                                Log.e("MBTI", "SubmitResponse or avatar URL is null");
                            }
                        } else {
                            Log.e("MBTI", "Response unsuccessful or empty body");
                        }
                    }
                    @Override
                    public void onFailure(Call<MbtiBoundary> call, Throwable t) {
                        Log.e("MBTI", "API call failed: " + t.getMessage());
                        Toast.makeText(ProfileActivity.this, "Failed to load MBTI data", Toast.LENGTH_SHORT).show();}
                });
    }
    private void loadSvgImage(String url, ImageView imageView) {
        RequestBuilder<PictureDrawable> requestBuilder = GlideApp.with(this)
                .as(PictureDrawable.class)
                .listener(new SvgSoftwareLayerSetter());

        requestBuilder.load(url)
                .placeholder(R.drawable.type_logo_placeholder)
                .error(R.drawable.type_logo_placeholder)
                .into(imageView);
    }
}
