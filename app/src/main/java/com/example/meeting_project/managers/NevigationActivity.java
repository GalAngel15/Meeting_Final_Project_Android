/*package com.example.meeting_project.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.activities.Activity_quiz_mbti;
import com.example.meeting_project.R;
import com.example.meeting_project.activities.PersonalitiesActivity;
import com.google.android.material.button.MaterialButton;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meeting_project.R;
import com.example.meeting_project.activities.HomeActivity;
import com.example.meeting_project.activities.ProfileActivity;
import com.example.meeting_project.activities.ChatsActivity;
import com.example.meeting_project.activities.AlertsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class NavigationActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    // כל Activity שיירש - יספק את ה־layout שלו
    protected abstract @LayoutRes int getLayoutResourceId();

    // וכל Activity יספק את האייטם בתפריט שמתאים לו
    protected abstract @IdRes int getNavigationMenuItemId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(getNavigationMenuItemId()); // הדגשה לפי המסך הנוכחי

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == getNavigationMenuItemId()) {
                return true; // כבר במסך הזה
            }

            Intent intent = null;

            switch (itemId) {
                case R.id.nav_home:
                    intent = new Intent(this, HomeActivity.class);
                    break;
                case R.id.nav_profile:
                    intent = new Intent(this, ProfileActivity.class);
                    break;
                case R.id.nav_chats:
                    intent = new Intent(this, ChatsActivity.class);
                    break;
                case R.id.nav_notifications:
                    intent = new Intent(this, AlertsActivity.class);
                    break;
            }

            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0); // מעבר חלק
            }

            return true;
        });
    }
}

 */