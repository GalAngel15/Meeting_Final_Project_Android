package com.example.meeting_project.activities;

import static com.example.meeting_project.R.id.toolbar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.example.meeting_project.R;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // קובץ ה־XML שלך

        findView();

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // טיפול בלחיצות תפריט צד
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            // כאן תוכל להגיב לכל אייטם שנלחץ
            if (id == R.id.nav_settings) {
                // לדוגמה: פתח אקטיביטי של הגדרות
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void findView() {
        // חיבור Drawer + Toggle (אייקון המבורגר)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
    }

    // לחצן Back סוגר תפריט אם פתוח
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
