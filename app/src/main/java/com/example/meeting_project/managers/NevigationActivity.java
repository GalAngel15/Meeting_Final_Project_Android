package com.example.meeting_project.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.example.meeting_project.R;
import com.example.meeting_project.activities.ChatActivity;
import com.example.meeting_project.activities.HomeActivity;
import com.example.meeting_project.activities.ProfileActivity;
import com.example.meeting_project.activities.AlertsActivity;
import com.google.android.material.button.MaterialButton;

public class NevigationActivity {

    // שומר איזה לחצן נבחר כרגע (אם רוצים לשמר בין הפעלות)
    private static int currentSelectedId = -1;

    public static void findNevigationButtens(Activity activity) {
        MaterialButton navigation_home = activity.findViewById(R.id.navigation_home);
        MaterialButton navigation_profile = activity.findViewById(R.id.navigation_profile);
        MaterialButton navigation_chats = activity.findViewById(R.id.navigation_chats);
        MaterialButton navigation_notifications = activity.findViewById(R.id.navigation_notifications);

        // SharedPreferences כדי לשמור ולקרוא מה היה הנבחר בפעם הקודמת
        SharedPreferences sharedPreferences =
                activity.getSharedPreferences("NavigationPrefs", Context.MODE_PRIVATE);
        int savedButtonId = sharedPreferences.getInt("selectedButtonId", R.id.navigation_home);

        // 1. מנקים מצב בחירה מכל הכפתורים
        navigation_home.setSelected(false);
        navigation_profile.setSelected(false);
        navigation_chats.setSelected(false);
        navigation_notifications.setSelected(false);

        // 2. סימון הכפתור השמור (אם קיים במפתחים המותרים)
        if (savedButtonId == R.id.navigation_home ||
                savedButtonId == R.id.navigation_profile ||
                savedButtonId == R.id.navigation_chats ||
                savedButtonId == R.id.navigation_notifications) {
            View prev = activity.findViewById(savedButtonId);
            if (prev != null) {
                prev.setSelected(true);
                currentSelectedId = savedButtonId;
            }
        }
        else {
            // ברירת מחדל: סימון ה-Home
            navigation_home.setSelected(true);
            currentSelectedId = R.id.navigation_home;
        }

        // 3. מאזין אחד לכל הכפתורים
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedId = v.getId();

                // אם הכפתור נבחר כבר, אין צורך לעשות כלום
                if (clickedId == currentSelectedId) {
                    return;
                }

                // 3.1. מבטל סימון (selected) מהכפתור הישן
                View old = activity.findViewById(currentSelectedId);
                if (old != null) {
                    old.setSelected(false);
                }

                // 3.2. מסמן (selected) את הכפתור החדש
                v.setSelected(true);
                currentSelectedId = clickedId;

                // 3.3. שומר ב־SharedPreferences כדי לשמור על הבחירה בין הפעלות
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("selectedButtonId", clickedId);
                editor.apply();

                // 3.4. מעבר ל־Activity המתאים בעזרת if-else
                Intent intent;
                if (clickedId == R.id.navigation_home) {
                    intent = new Intent(activity, HomeActivity.class);
                }
                else if (clickedId == R.id.navigation_profile) {
                    intent = new Intent(activity, ProfileActivity.class);
                }
                else if (clickedId == R.id.navigation_chats) {
                    intent = new Intent(activity, ChatActivity.class);
                }
                else if (clickedId == R.id.navigation_notifications) {
                    intent = new Intent(activity, AlertsActivity.class);
                }
                else {
                    // שום לחצן אחר לא מתאים – נעצור
                    return;
                }

                // מנקים את ה־Activity Stack ועוברים ל־המסך הרצוי
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        };

        // 4. קישור המאזין לכל כפתור
        navigation_home.setOnClickListener(listener);
        navigation_profile.setOnClickListener(listener);
        navigation_chats.setOnClickListener(listener);
        navigation_notifications.setOnClickListener(listener);
    }
}
