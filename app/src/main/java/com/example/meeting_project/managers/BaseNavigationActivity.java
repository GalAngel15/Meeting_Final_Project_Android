package com.example.meeting_project.managers;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.meeting_project.R;
import com.example.meeting_project.activities.Activity_personality_result;
import com.example.meeting_project.activities.Activity_questionnaire;
import com.example.meeting_project.activities.AlertsActivity;
import com.example.meeting_project.activities.ChooseUserForChat;
import com.example.meeting_project.activities.Conversations;
import com.example.meeting_project.activities.HomeActivity;
import com.example.meeting_project.activities.ProfileActivity;
import com.example.meeting_project.activities.activity_preferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseNavigationActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageButton menuButton;

    private MaterialButton navHome, navProfile, navChats, navNotifications;

    // מיפוי של התפריט הצדדי
    private static final Map<Integer, Class<?>> drawerMap = new HashMap<>();
    private static final Map<Integer, Class<?>> bottomMap = new HashMap<>();

    static {
        drawerMap.put(R.id.nav_my_personality, Activity_personality_result.class);
        drawerMap.put(R.id.nav_edit_preferences, activity_preferences.class);
        drawerMap.put(R.id.nav_edit_intro, Activity_questionnaire.class);
        //drawerMap.put(R.id.nav_settings, SettingsActivity.class);

        bottomMap.put(R.id.navigation_home, HomeActivity.class);
        bottomMap.put(R.id.navigation_profile, ProfileActivity.class);
        bottomMap.put(R.id.navigation_chats, Conversations.class);
        bottomMap.put(R.id.navigation_notifications, AlertsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        initDrawerViews();
        initDrawerLogic();
        initBottomNavViews();
        initBottomNavLogic();
    }

    private void initDrawerViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        menuButton = findViewById(R.id.btn_menu);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void initDrawerLogic() {
        navigationView.setNavigationItemSelectedListener(item -> {
            Class<?> targetActivity = drawerMap.get(item.getItemId());
            if (targetActivity != null && !targetActivity.equals(this.getClass())) {
                startActivity(new Intent(this, targetActivity));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (getDrawerMenuItemId() != 0) {
            navigationView.setCheckedItem(getDrawerMenuItemId());
        }
    }

    private void initBottomNavViews() {
        navHome = findViewById(R.id.navigation_home);
        navProfile = findViewById(R.id.navigation_profile);
        navChats = findViewById(R.id.navigation_chats);
        navNotifications = findViewById(R.id.navigation_notifications);
    }

    private void initBottomNavLogic() {
        setButton(navHome, R.id.navigation_home);
        setButton(navProfile, R.id.navigation_profile);
        setButton(navChats, R.id.navigation_chats);
        setButton(navNotifications, R.id.navigation_notifications);

        // ניהול ה־selected
        updateBottomSelection();
    }

    private void setButton(MaterialButton button, int id) {
        button.setOnClickListener(v -> {
            Class<?> targetActivity = bottomMap.get(id);
            if (targetActivity != null && !targetActivity.equals(this.getClass())) {
                startActivity(new Intent(this, targetActivity));
                finish();
            }
        });
    }

    private void updateBottomSelection() {
        int selected = getBottomMenuItemId();
        navHome.setSelected(selected == R.id.navigation_home);
        navProfile.setSelected(selected == R.id.navigation_profile);
        navChats.setSelected(selected == R.id.navigation_chats);
        navNotifications.setSelected(selected == R.id.navigation_notifications);
    }


    // פונקציות מופשטות שכל Activity יממש:
    protected abstract @LayoutRes int getLayoutResourceId();
    protected abstract @IdRes int getDrawerMenuItemId();
    protected abstract @IdRes int getBottomMenuItemId();
}
