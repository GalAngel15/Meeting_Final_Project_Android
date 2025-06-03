package com.example.meeting_project.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatlibrary.models.Chat;
import com.example.meeting_project.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import com.example.meeting_project.activities.ChatActivity;
import com.example.meeting_project.activities.HomeActivity;
import com.example.meeting_project.activities.ProfileActivity;
import com.example.meeting_project.activities.AlertsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class NevigationActivity {
    public static void findNevigationButtens(Activity activity) {
        MaterialButton navigation_home = activity.findViewById(R.id.navigation_home);
        MaterialButton navigation_profile = activity.findViewById(R.id.navigation_profile);
        MaterialButton navigation_chats = activity.findViewById(R.id.navigation_chats);
        MaterialButton navigation_notifications = activity.findViewById(R.id.navigation_notifications);

        SharedPreferences sharedPreferences = activity.getSharedPreferences("NavigationPrefs", Context.MODE_PRIVATE);
        int selectedButtonId = sharedPreferences.getInt("selectedButtonId", R.id.navigation_home);

        // Reset all buttons to their default color
        setButtonColor(navigation_home, R.color.inactiveButtonColor);
        setButtonColor(navigation_profile, R.color.inactiveButtonColor);
        setButtonColor(navigation_chats, R.color.inactiveButtonColor);
        setButtonColor(navigation_notifications, R.color.inactiveButtonColor);

        // Set the selected button color
        //setButtonColor(activity.findViewById(selectedButtonId), R.color.activeButtonColor);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset all buttons to their default color
                setButtonColor(navigation_home, R.color.inactiveButtonColor);
                setButtonColor(navigation_profile, R.color.inactiveButtonColor);
                setButtonColor(navigation_chats, R.color.inactiveButtonColor);
                setButtonColor(navigation_notifications, R.color.inactiveButtonColor);

                // Set the selected button color
                setButtonColor(v, R.color.activeButtonColor);

                // Save the selected button ID
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("selectedButtonId", v.getId());
                editor.apply();

                Intent intent;
                if (v.getId() == R.id.navigation_home) {
                    intent = new Intent(activity, HomeActivity.class);
                } else if (v.getId() == R.id.navigation_profile) {
                    intent = new Intent(activity, ProfileActivity.class);
                } else if (v.getId() == R.id.navigation_chats) {
                    intent = new Intent(activity, ChatActivity.class);
                } else if (v.getId() == R.id.navigation_notifications) {
                    intent = new Intent(activity, AlertsActivity.class);
                } else {
                    return;
                }
                // Use Intent.FLAG_ACTIVITY_CLEAR_TOP to clear the back stack and navigate to the selected activity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        };

        navigation_home.setOnClickListener(listener);
        navigation_profile.setOnClickListener(listener);
        navigation_chats.setOnClickListener(listener);
        navigation_notifications.setOnClickListener(listener);
    }

    public static void setButtonColor(View button, int color) {
        button.setBackgroundColor(button.getContext().getResources().getColor(color, null));
    }
}