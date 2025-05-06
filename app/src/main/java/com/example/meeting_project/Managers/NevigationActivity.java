package com.example.meeting_project.Managers;

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

public class NevigationActivity extends AppCompatActivity {
    public static void findNevigationButtens(Activity activity) {

    MaterialButton navigation_home = activity.findViewById(R.id.navigation_home);
    MaterialButton navigation_profile = activity.findViewById(R.id.navigation_profile);

    SharedPreferences sharedPreferences = activity.getSharedPreferences("NavigationPrefs", Context.MODE_PRIVATE);
    int selectedButtonId = sharedPreferences.getInt("selectedButtonId", R.id.navigation_home);

    // Reset all buttons to their default color
    setButtonColor(navigation_home, R.color.inactiveButtonColor);
    setButtonColor(navigation_profile, R.color.inactiveButtonColor);

    // Set the selected button color
    setButtonColor(activity.findViewById(selectedButtonId), R.color.activeButtonColor);

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Reset all buttons to their default color
            setButtonColor(navigation_home, R.color.inactiveButtonColor);
            setButtonColor(navigation_profile, R.color.inactiveButtonColor);

            // Set the selected button color
            setButtonColor(v, R.color.activeButtonColor);

            // Save the selected button ID
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("selectedButtonId", v.getId());
            editor.apply();

            Intent intent;
            if (v.getId() == R.id.navigation_home) {
                intent = new Intent(activity, Activity_quiz_mbti.class);
            } else if (v.getId() == R.id.navigation_profile) {
                intent = new Intent(activity, PersonalitiesActivity.class);
            }else {
                return;
            }
            // Use Intent.FLAG_ACTIVITY_CLEAR_TOP to clear the back stack and navigate to the selected activity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }
    };

        navigation_home.setOnClickListener(listener);
        navigation_profile.setOnClickListener(listener);
    }

    public static void setButtonColor(View button, int color) {
        button.setBackgroundColor(button.getContext().getResources().getColor(color, null));
    }
}