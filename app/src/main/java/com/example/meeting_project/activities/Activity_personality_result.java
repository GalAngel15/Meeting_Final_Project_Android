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
import com.example.meeting_project.GlideAdapter.GlideApp;
import com.example.meeting_project.GlideAdapter.SvgSoftwareLayerSetter;
import com.example.meeting_project.R;
import com.example.meeting_project.managers.AppManager;
import com.example.meeting_project.managers.NevigationActivity;
import com.example.meeting_project.objectOfMbtiTest.SubmitResponse;
import com.example.meeting_project.objectOfMbtiTest.Trait;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Activity_personality_result extends AppCompatActivity {
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ImageButton menuButton;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    private TextView tvNiceName, tvSnippet;
    private ImageView ivAvatar;
    private LinearLayout scalesContainer, traitsContainer;
    private Button btnNextTest;

    private static final Map<Integer, Class<?>> NAV_MAP = new HashMap<>();
    static {
        NAV_MAP.put(R.id.nav_edit_preferences, activity_preferences.class);
        NAV_MAP.put(R.id.nav_edit_intro, Activity_questionnaire.class);
        NAV_MAP.put(R.id.nav_my_personality, PersonalitiesActivity.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personality_result);
        initViews();
        // הגדר את ה־NavigationView
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        NevigationActivity.findNevigationButtens(this);

        // 2. הגדרת ה-Toolbar כ-ActionBar
        setSupportActionBar(toolbar);

        // 3. יצירת ה-Toggle והוספתו ל-DrawerLayout
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,   // ודא שיש לך מחרוזות אלו ב־strings.xml
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // טיפול בלחיצות תפריט צד

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> targetActivity = NAV_MAP.get(itemId);

            if (targetActivity != null) {
                Intent intent = new Intent(Activity_personality_result.this, targetActivity);
                startActivity(intent);
            } else {
                Toast.makeText(this, "אין פעולה מתאימה", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        setBtnClick();
        // קבל את ה־JSON
        String json = getIntent().getStringExtra("submitResponseJson");

        // המר אותו חזרה לאובייקט
        Gson gson = new Gson();
        SubmitResponse response = gson.fromJson(json, SubmitResponse.class);
        AppManager.setContext(this.getApplicationContext());


        if (response != null) {
            tvNiceName.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
            tvSnippet.setText(response.getSnippet());

            Log.d("AvatarDebug", "Avatar URL: " + response.getAvatarSrcStatic());
            // Load SVG image
            loadSvgImage(response.getAvatarSrcStatic(), ivAvatar);

            showScales(response.getScales());
            showTraits(response.getTraits());
        }

//        if (response != null) {
//            tvNiceName.setText(response.getNiceName() + " (" + response.getFullCode() + ")");
//            tvSnippet.setText(response.getSnippet());
//            // Load SVG image using Glide
//            Glide.with(this).load(Uri.parse(response.getAvatarSrcStatic())).into(ivAvatar);
//            showScales(response.getScales());
//            showTraits(response.getTraits());
//        }
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

    private void setBtnClick() {
        btnNextTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_personality_result.this, Activity_questionnaire.class);

                startActivity(intent);
            }
        });
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
        btnNextTest = findViewById(R.id.btnNextTest);
        drawerLayout = findViewById(R.id.drawer_layout);
        menuButton = findViewById(R.id.btn_menu);
        navigationView = findViewById(R.id.navigation_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
    }

}