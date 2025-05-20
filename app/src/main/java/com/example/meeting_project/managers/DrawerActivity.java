/*package com.example.meeting_project.managers;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.meeting_project.R;
import com.google.android.material.navigation.NavigationView;

public class DrawerActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // אתחול רכיבים
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
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

        // שימוש ב-Listener רגיל (לא למבדה)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.nav_settings:
                        showToast("הגדרות");
                        break;
                    case R.id.nav_edit_preferences:
                        showToast("עריכת שאלון העדפות");
                        break;
                    case R.id.nav_edit_intro:
                        showToast("עריכת שאלון היכרות");
                        break;
                    case R.id.nav_my_personality:
                        showToast("טיפוס האישיות שלי");
                        break;
                    default:
                        return false;
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        // עדכון טקסט בכותרת התפריט (אופציונלי)
        TextView headerTitle = navigationView.getHeaderView(0).findViewById(R.id.header_title);
        headerTitle.setText("שלום, מיקה 👋"); // תעדכני לפי המשתמש
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

 */
