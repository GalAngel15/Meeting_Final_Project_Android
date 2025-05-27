package com.example.meeting_project.managers;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.meeting_project.R;
import com.example.meeting_project.activities.Activity_questionnaire;
import com.example.meeting_project.activities.PersonalitiesActivity;
import com.example.meeting_project.activities.activity_preferences;
import com.google.android.material.navigation.NavigationView;
import java.util.HashMap;
import java.util.Map;
import androidx.core.view.GravityCompat;

public class DrawerActivity extends AppCompatActivity {
    private ImageButton menuButton;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private static final Map<Integer, Class<?>> NAV_MAP = new HashMap<>();

    static {
        //NAV_MAP.put(R.id.nav_settings, nav_settings.class);
        NAV_MAP.put(R.id.nav_edit_preferences, activity_preferences.class);
        NAV_MAP.put(R.id.nav_edit_intro, Activity_questionnaire.class);
        NAV_MAP.put(R.id.nav_my_personality, PersonalitiesActivity.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // אתחול רכיבים
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuButton = findViewById(R.id.btn_menu);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // כפתור המבורגר
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // האזנה לפריטים בתפריט הצד
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Class<?> targetActivity = NAV_MAP.get(itemId);

                if (targetActivity != null) {
                    Intent intent = new Intent(DrawerActivity.this, targetActivity);
                    startActivity(intent);
                } else {
                    showToast("פעולה לא זמינה");
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });


        // עדכון טקסט בכותרת התפריט (אופציונלי)
        TextView headerTitle = navigationView.getHeaderView(0).findViewById(R.id.header_title);
        headerTitle.setText("שלום,  👋"); // תעדכני לפי המשתמש
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}


