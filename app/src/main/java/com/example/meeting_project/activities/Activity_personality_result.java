package com.example.meeting_project.activities;

import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.meeting_project.APIRequests.MbtiServiceApi;
import com.example.meeting_project.GlideAdapter.GlideApp;
import com.example.meeting_project.GlideAdapter.SvgSoftwareLayerSetter;
import com.example.meeting_project.R;
import com.example.meeting_project.UserSessionManager;
import com.example.meeting_project.apiClients.MbtiService_ApiClient;
import com.example.meeting_project.boundaries.MbtiBoundary;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.BaseNavigationActivity;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.example.meeting_project.objectOfMbtiTest.Trait;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Activity_personality_result extends BaseNavigationActivity {
    private TextView tvNiceName, tvSnippet;
    private ImageView ivAvatar;
    private LinearLayout scalesContainer, traitsContainer;

    private MbtiServiceApi mbtiApi;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_personality_result);
        initViews();

        mbtiApi = MbtiService_ApiClient.getRetrofitInstance().create(MbtiServiceApi.class);
        userId = UserSessionManager.getServerUserId(this);
        if (userId != null) {
            fetchMbtiData(userId);
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSvgImage(String url, ImageView imageView) {
        RequestBuilder<PictureDrawable> requestBuilder = GlideApp.with(this)
                .as(PictureDrawable.class)
                .listener(new SvgSoftwareLayerSetter());

        requestBuilder
                .load(url)
                .centerInside()
                .error(R.drawable.ic_error) // Replace with your error drawable
                .into(imageView);
    }

    private void showScales(List<String> scales) {
        scalesContainer.removeAllViews();
        for (String scale : scales) {
            TextView tv = new TextView(this);
            tv.setText("- " + scale);
            tv.setTextSize(16);
            scalesContainer.addView(tv);
        }
    }

    private void showTraits(List<Trait> traits) {
        traitsContainer.removeAllViews();
        for (Trait trait : traits) {
            LinearLayout traitLayout = new LinearLayout(this);
            traitLayout.setOrientation(LinearLayout.VERTICAL);
            traitLayout.setPadding(0, 16, 0, 16);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(trait.getLabel() + " (" + trait.getPct() + "%)");
            tvTitle.setTextSize(18);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 200);
            iv.setLayoutParams(lp);
            Glide.with(this).load(trait.getImageSrc()).into(iv); // submit respons with the function getAvatarSrcStatic

            TextView tvDesc = new TextView(this);
            tvDesc.setText(trait.getSnippet());
            tvDesc.setTextSize(14);

            traitLayout.addView(tvTitle);
            traitLayout.addView(iv);
            traitLayout.addView(tvDesc);

            traitsContainer.addView(traitLayout);
        }
    }

    private void initViews() {
        tvNiceName = findViewById(R.id.tvNiceName);
        tvSnippet = findViewById(R.id.tvSnippet);
        ivAvatar = findViewById(R.id.ivAvatar);
        scalesContainer = findViewById(R.id.scalesContainer);
        traitsContainer = findViewById(R.id.traitsContainer);
    }

    private void fetchMbtiData(String userId) {
        mbtiApi.getProfileByUserId(userId).enqueue(new Callback<MbtiBoundary>() {
            @Override
            public void onResponse(Call<MbtiBoundary> call, Response<MbtiBoundary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String characteristicsJson = response.body().getCharacteristics();
                    Log.e("characteristicsJson", characteristicsJson);

                    SubmitResponse submitResponse = new Gson().fromJson(characteristicsJson, SubmitResponse.class);
                    if (submitResponse != null) {
                        displaySubmitResponse(submitResponse);
                    } else {
                        Log.e("MBTI", "SubmitResponse is null");
                    }
                } else {
                    Log.e("MBTI", "Response unsuccessful or empty body");
                }
            }

            @Override
            public void onFailure(Call<MbtiBoundary> call, Throwable t) {
                Log.e("MBTI", "API call failed: " + t.getMessage());
                Toast.makeText(Activity_personality_result.this, "Failed to load MBTI data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySubmitResponse(SubmitResponse response) {
        tvNiceName.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
        tvSnippet.setText(response.getSnippet());
        Log.d("AvatarDebug", "Avatar URL: " + response.getAvatarSrcStatic());
        loadSvgImage(response.getAvatarSrcStatic(), ivAvatar);
        showScales(response.getScales());
        showTraits(response.getTraits());
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_personality_result;
    }

    @Override
    protected int getDrawerMenuItemId() {
        return R.id.nav_my_personality;
    }

    @Override
    protected int getBottomMenuItemId() {
        return 0;  // כי במסך הזה אין סימון בתפריט התחתון
    }

    @Override
    protected String getCurrentUserId() {
        return userId;
    }
}