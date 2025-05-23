package com.example.meeting_project.managers;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.Chat;
import com.example.meeting_project.R;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import com.example.meeting_project.activities.HomeActivity;
import com.example.meeting_project.activities.ProfileActivity;
import com.example.meeting_project.activities.AlertsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public abstract class NevigationActivity extends AppCompatActivity {

    private static final Map<Integer, Class<? extends Activity>> NAV_MAP = new HashMap<>();
    static {
        NAV_MAP.put(R.id.nav_home, HomeActivity.class);
        NAV_MAP.put(R.id.nav_profile, ProfileActivity.class);
        NAV_MAP.put(R.id.nav_chats, Chat.class);           // ודא ש-ChatActivity אכן Activity
        NAV_MAP.put(R.id.nav_notifications, AlertsActivity.class);
    }

    protected BottomNavigationView bottomNavigationView;
    protected abstract @LayoutRes int getLayoutResourceId();
    protected abstract @IdRes int getNavigationMenuItemId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(getNavigationMenuItemId());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == getNavigationMenuItemId()) return true;

            Class<? extends Activity> target = NAV_MAP.get(id);
            if (target != null) {
                Intent intent = new Intent(this, target)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}